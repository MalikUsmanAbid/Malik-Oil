PRAGMA foreign_keys=ON;
CREATE TABLE commodities(id TEXT PRIMARY KEY, name TEXT NOT NULL);
INSERT INTO commodities VALUES ('sarson','Sarson'),('canola','Canola'),('til','Til'),('taramira','Taramira'),('alsi','Alsi'),('other','Other');
CREATE TABLE rates(
 id TEXT PRIMARY KEY, stream TEXT NOT NULL, commodity TEXT NOT NULL REFERENCES commodities(id),
 market TEXT NOT NULL, grade TEXT NOT NULL, source TEXT NOT NULL, source_url TEXT NOT NULL,
 price_paisa_kg INTEGER NOT NULL CHECK(price_paisa_kg>0), observed_at INTEGER NOT NULL,
 received_at INTEGER NOT NULL, fingerprint TEXT NOT NULL UNIQUE
);
CREATE INDEX rates_stream_time ON rates(stream,observed_at DESC);
CREATE TABLE anchors(stream TEXT PRIMARY KEY, price INTEGER NOT NULL, observed_at INTEGER NOT NULL, latest_at INTEGER NOT NULL);
CREATE TABLE alerts(id TEXT PRIMARY KEY, stream TEXT NOT NULL, rate_id TEXT NOT NULL UNIQUE REFERENCES rates(id), old_price INTEGER NOT NULL, new_price INTEGER NOT NULL, created_at INTEGER NOT NULL);
CREATE INDEX alerts_time ON alerts(created_at DESC);
CREATE TABLE candidates(id TEXT PRIMARY KEY, fingerprint TEXT UNIQUE NOT NULL, payload TEXT NOT NULL, created_at INTEGER NOT NULL, approved_at INTEGER);
CREATE TABLE audit(id INTEGER PRIMARY KEY AUTOINCREMENT, actor TEXT NOT NULL, action TEXT NOT NULL, object_id TEXT NOT NULL, created_at INTEGER NOT NULL);
CREATE TABLE devices(id TEXT PRIMARY KEY, token TEXT NOT NULL UNIQUE, active INTEGER NOT NULL DEFAULT 1, updated_at INTEGER NOT NULL);
CREATE TABLE deliveries(alert_id TEXT NOT NULL REFERENCES alerts(id), device_id TEXT NOT NULL REFERENCES devices(id), state TEXT NOT NULL DEFAULT 'pending', attempts INTEGER NOT NULL DEFAULT 0, next_at INTEGER NOT NULL DEFAULT 0, lease_until INTEGER NOT NULL DEFAULT 0, lease_id TEXT, last_code INTEGER, PRIMARY KEY(alert_id,device_id));
CREATE INDEX delivery_due ON deliveries(state,next_at,lease_until);
-- One INSERT transaction serializes quote, baseline, alert, audit and delivery intent.
CREATE TRIGGER evaluate_rate AFTER INSERT ON rates BEGIN
 INSERT INTO audit(actor,action,object_id,created_at) VALUES('owner','rate.approved',NEW.id,NEW.received_at);
 INSERT INTO anchors SELECT NEW.stream,NEW.price_paisa_kg,NEW.observed_at,NEW.observed_at WHERE NEW.received_at-NEW.observed_at BETWEEN -300 AND 86400 ON CONFLICT DO NOTHING;
 INSERT INTO alerts
 SELECT NEW.id,NEW.stream,NEW.id,a.price,NEW.price_paisa_kg,NEW.received_at FROM anchors a
 WHERE a.stream=NEW.stream AND NEW.observed_at>a.latest_at
 AND NEW.received_at-NEW.observed_at BETWEEN -300 AND 86400
 AND NEW.observed_at-a.latest_at<=86400
 AND ABS(NEW.price_paisa_kg-a.price)*100>=a.price*3;
 INSERT INTO deliveries(alert_id,device_id)
 SELECT NEW.id,id FROM devices WHERE active=1 AND EXISTS(SELECT 1 FROM alerts WHERE id=NEW.id);
 UPDATE anchors SET price=NEW.price_paisa_kg, observed_at=NEW.observed_at
 WHERE stream=NEW.stream AND NEW.observed_at>latest_at
 AND NEW.received_at-NEW.observed_at BETWEEN -300 AND 86400
 AND (NEW.observed_at-latest_at>86400 OR EXISTS(SELECT 1 FROM alerts WHERE id=NEW.id));
 UPDATE anchors SET latest_at=NEW.observed_at WHERE stream=NEW.stream AND NEW.observed_at>latest_at
 AND NEW.received_at-NEW.observed_at BETWEEN -300 AND 86400;
END;
