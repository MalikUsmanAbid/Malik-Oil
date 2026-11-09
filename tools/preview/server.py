"""Loopback-only synthetic preview. Never imports Firebase credentials or production data."""
import argparse
import hashlib
import json
import math
import os
import shutil
import subprocess
import sqlite3
import time
import uuid
from session_vault import SessionVault, Conflict
from daily_preview import DailyPreview, workbook
from decimal import Decimal, ROUND_HALF_UP
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
HTML = Path(__file__).with_name('index.html')


def connect(path):
    db = sqlite3.connect(path)
    db.row_factory = sqlite3.Row
    if not db.execute("SELECT 1 FROM sqlite_master WHERE name='rates'").fetchone():
        db.executescript((ROOT / 'backend/migrations/0001_init.sql').read_text(encoding='utf-8'))
        now = int(time.time())
        db.execute('INSERT INTO rates VALUES(?,?,?,?,?,?,?,?,?,?,?)',
                   ('demo-seed', 'sarson|lahore|standard|DEMO', 'sarson', 'Lahore',
                    'standard', 'DEMO synthetic', '', 20000, now - 120, now, 'demo-seed'))
        db.commit()
    db.executescript((ROOT / "backend/migrations/0002_research_budget.sql").read_text(encoding="utf-8"))
    db.executescript((ROOT / "backend/migrations/0003_search_budget.sql").read_text(encoding="utf-8"))
    return db


def research_usage(db):
    day = time.strftime('%Y-%m-%d', time.gmtime())
    row = db.execute('SELECT calls FROM research_usage WHERE day=?', (day,)).fetchone()
    used = row[0] if row else 0
    return {'used': used, 'limit': 20, 'remaining': max(0, 20-used), 'resetAt': (int(time.time())//86400+1)*86400000}


def state(db):
    rows = lambda sql: [dict(row) for row in db.execute(sql)]
    return {
        'mode': 'synthetic-local-preview',
        'researchUsage': research_usage(db),
        'webSearchConfigured': bool(os.environ.get('TAVILY_API_KEY')) and os.environ.get('TAVILY_FREE_TIER_CONFIRMED') == 'true',
        'researchConfigured': os.environ.get('ENABLE_RESEARCH') == 'true' and bool(os.environ.get('GEMINI_API_KEY')), 
        'commodities': rows('SELECT * FROM commodities ORDER BY name'),
        'rates': rows('SELECT * FROM rates ORDER BY observed_at DESC LIMIT 60'),
        'candidates': rows('SELECT * FROM candidates WHERE approved_at IS NULL ORDER BY created_at DESC'),
        'alerts': rows('SELECT a.*,r.commodity,r.market FROM alerts a JOIN rates r ON r.id=a.rate_id ORDER BY a.created_at DESC,a.rowid DESC'),
        'audit': rows('SELECT * FROM audit ORDER BY id DESC LIMIT 30'),
    }


def candidate(db, data):
    commodity = str(data.get('commodity', 'sarson'))
    if not db.execute('SELECT 1 FROM commodities WHERE id=?', (commodity,)).fetchone():
        raise ValueError('Select a known commodity')
    market = str(data.get('market', 'Lahore')).strip()
    if not market or len(market) > 80 or any(not (c.isalnum() or c in ' ._-') for c in market):
        raise ValueError('Use a market name of 1 to 80 letters/numbers')
    unit = data.get('unit', '40kg')
    if unit not in ('kg', '40kg', '100kg'):
        raise ValueError('Unit must be kg, 40kg or 100kg')
    amount = Decimal(str(data.get('amount', 0)))
    if not amount.is_finite() or not 0 < amount <= 100000000:
        raise ValueError('Enter a positive amount below PKR 100 million')
    divisor = {'kg': 1, '40kg': 40, '100kg': 100}[unit]
    price = int((amount * 100 / divisor).quantize(Decimal('1'), rounding=ROUND_HALF_UP))
    if price < 1:
        raise ValueError('Amount is below one paisa/kg')
    now = int(time.time())
    latest = db.execute('SELECT MAX(observed_at) FROM rates').fetchone()[0] or now - 1
    observed = max(now, latest + 1)
    if observed > now + 299:
        raise ValueError('Please wait before creating more test observations')
    quote = dict(commodity=commodity, market=market, grade='standard', source='DEMO synthetic',
                 sourceUrl='', price=price, observed=observed, amount=str(amount), unit=unit)
    identifier = uuid.uuid4().hex
    fingerprint = hashlib.sha256(identifier.encode()).hexdigest()
    db.execute('INSERT INTO candidates VALUES(?,?,?,?,NULL)',
               (identifier, fingerprint, json.dumps(quote), now))
    db.commit()
    return {'id': identifier}


def approve(db, identifier):
    row = db.execute('SELECT * FROM candidates WHERE id=?', (identifier,)).fetchone()
    if row is None:
        raise ValueError('Candidate not found')
    q = json.loads(row['payload'])
    stream = q['commodity'] + '|' + q['market'].lower() + '|standard|DEMO'
    with db:
        db.execute('INSERT INTO rates VALUES(?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(fingerprint) DO NOTHING',
                   (identifier, stream, q['commodity'], q['market'], q['grade'], q['source'], '',
                    q['price'], q['observed'], int(time.time()), row['fingerprint']))
        db.execute('UPDATE candidates SET approved_at=? WHERE id=? AND approved_at IS NULL',
                   (int(time.time()), identifier))
    return {'approved': True, 'id': identifier}


def margin(data):
    keys = ['quantityKg', 'buyPkrKg', 'sellPkrKg', 'transportPkr', 'otherCostsPkr']
    values = {key: float(data.get(key, 0)) for key in keys}
    if any(not math.isfinite(v) or v < 0 or v > 100000000 for v in values.values()) or values['quantityKg'] <= 0:
        raise ValueError('Quantity must be positive and costs must be nonnegative finite numbers')
    quantity = values['quantityKg']
    landed = quantity * values['buyPkrKg'] + values['transportPkr'] + values['otherCostsPkr']
    revenue = quantity * values['sellPkrKg']
    return dict(landedCostPkr=landed, revenuePkr=revenue, grossMarginPkr=revenue-landed,
                breakEvenPkrKg=landed/quantity)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--port', type=int, default=8765)
    args = parser.parse_args()
    directory = ROOT / '.preview'
    directory.mkdir(exist_ok=True)
    db = connect(directory / 'synthetic.sqlite3')
    origin = f'http://127.0.0.1:{args.port}'
    db.execute('CREATE TABLE IF NOT EXISTS research_usage(day TEXT PRIMARY KEY,calls INTEGER NOT NULL)')

    class Handler(BaseHTTPRequestHandler):
        def setup(self):
            super().setup()
            self.connection.settimeout(10)

        def log_message(self, *_):
            pass

        def respond(self, body, status=200, content_type='application/json'):
            raw = body if isinstance(body, bytes) else json.dumps(body).encode()
            self.send_response(status)
            self.send_header('Content-Type', content_type)
            self.send_header('Cache-Control', 'no-store')
            self.send_header('Referrer-Policy', 'no-referrer')
            self.send_header('X-Frame-Options', 'DENY')
            self.send_header('X-Content-Type-Options', 'nosniff')
            self.send_header('Content-Security-Policy', "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'")
            self.end_headers()
            self.wfile.write(raw)

        def valid_host(self):
            return self.headers.get('Host') == f'127.0.0.1:{args.port}'

        def do_GET(self):
            if not self.valid_host():
                return self.respond({'error': 'Loopback preview only'}, 403)
            if self.path == '/':
                return self.respond(HTML.read_bytes(), content_type='text/html; charset=utf-8')
            if self.path == '/brand-icon.png':
                return self.respond((ROOT / 'assets/branding/malik-oil-icon-polished.png').read_bytes(),content_type='image/png')
            if self.path == '/demo/daily':
                try: return self.respond(DailyPreview(directory / 'daily-reports.bin').read())
                except (ValueError, OSError): return self.respond({'error':'Encrypted report preview unavailable; existing file preserved'},503)
            if self.path.startswith('/demo/daily/export/'):
                day=self.path.removeprefix('/demo/daily/export/')
                try:
                    report=next((r for r in DailyPreview(directory / 'daily-reports.bin').read()['reports'] if r['date']==day),None)
                    if report is None: return self.respond({'error':'Saved report not found'},404)
                    return self.respond(workbook(report),content_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
                except (ValueError,OSError): return self.respond({'error':'Report export unavailable'},503)
            if self.path == '/demo/sessions':
                try: return self.respond(SessionVault(ROOT / '.preview/research-sessions.bin').read())
                except (ValueError, OSError): return self.respond({'error': 'Encrypted sessions unavailable; existing history preserved'}, 503)
            assets = {'/daily.js': ('daily.js', 'text/javascript; charset=utf-8'), '/daily-labels.js': ('daily-labels.js', 'text/javascript; charset=utf-8'), '/plugins.js': ('plugins.js', 'text/javascript; charset=utf-8'), '/workspace.js': ('workspace.js', 'text/javascript; charset=utf-8'), '/labels.js': ('labels.js', 'text/javascript; charset=utf-8'), '/styles.css': ('styles.css', 'text/css; charset=utf-8')}
            if self.path in assets:
                name, mime = assets[self.path]
                return self.respond(HTML.with_name(name).read_bytes(), content_type=mime)
            if self.path == '/demo/state':
                return self.respond(state(db))
            self.respond({'error': 'Not found'}, 404)

        def do_POST(self):
            if not self.valid_host() or self.headers.get('Origin') != origin:
                return self.respond({'error': 'Same-origin local preview required'}, 403)
            try:
                if self.headers.get('Transfer-Encoding') is not None or len(self.headers.get_all('Content-Length', [])) != 1:
                    raise ValueError('One Content-Length required; transfer encoding unsupported')
                length = int(self.headers.get('Content-Length', 0))
                if not 0 < length <= (2097152 if self.path == '/demo/sessions' else 65536) or self.headers.get('Content-Type') != 'application/json':
                    raise ValueError('JSON body required, maximum 64KB')
                data = json.loads(self.rfile.read(length))
                if not isinstance(data, dict):
                    raise ValueError('Object required')
                if self.path == '/demo/daily':
                    try: return self.respond(DailyPreview(directory / 'daily-reports.bin').action(data))
                    except Conflict as error: return self.respond({'error':str(error)},409)
                if self.path == '/demo/sessions':
                    try: return self.respond(SessionVault(ROOT / '.preview/research-sessions.bin').save(data.get('sessions'), data.get('revision')))
                    except Conflict as error: return self.respond({'error': str(error)}, 409)
                if self.path == '/demo/candidates':
                    result = candidate(db, data)
                elif self.path == '/demo/approve':
                    result = approve(db, str(data.get('id', '')))
                elif self.path == '/demo/research':
                    if os.environ.get('ENABLE_RESEARCH') != 'true' or not os.environ.get('GEMINI_API_KEY'):
                        return self.respond({'error': 'Gemini API key setup required. No local Ollama or paid fallback.'}, 503)
                    day = time.strftime('%Y-%m-%d', time.gmtime())
                    slot = db.execute('INSERT INTO research_usage VALUES(?,1) ON CONFLICT(day) DO UPDATE SET calls=calls+1 WHERE calls<20 RETURNING calls', (day,)).fetchone()
                    db.commit()
                    if not slot:
                        return self.respond({'error': 'Daily research safety limit reached. No paid fallback.', 'code': 'RESEARCH_DAILY_LIMIT', **research_usage(db)}, 429)
                    if os.environ.get('TAVILY_API_KEY') and os.environ.get('TAVILY_FREE_TIER_CONFIRMED') == 'true':
                        credits = 3 if data.get('depth') == 'deep' else 1
                        reserved = db.execute('INSERT INTO search_usage VALUES(?,?) ON CONFLICT(month) DO UPDATE SET credits=credits+excluded.credits WHERE credits+excluded.credits<=900 RETURNING credits', (day[:7], credits)).fetchone()
                        db.commit()
                        if not reserved:
                            return self.respond({'error': 'Monthly search safety cap reached (900 credits); no paid fallback'}, 429)
                    node = shutil.which('node')
                    cli = ROOT / 'backend/node_modules/tsx/dist/cli.mjs'
                    if not node or not cli.exists():
                        return self.respond({'error': 'Local research adapter dependencies missing; run npm ci in backend'}, 503)
                    try:
                        run = subprocess.run([node, str(cli), str(Path(__file__).with_name('research-cli.ts'))], input=json.dumps(data), capture_output=True, text=True, encoding='utf-8', timeout=55, cwd=ROOT / 'backend', creationflags=getattr(subprocess, 'CREATE_NO_WINDOW', 0))
                        result = json.loads(run.stdout)
                    except (subprocess.TimeoutExpired, json.JSONDecodeError):
                        return self.respond({'error': 'Research provider unavailable or timed out'}, 502)
                    return self.respond(result, result.pop('status', 200))
                elif self.path == '/demo/margin':
                    result = margin(data)
                else:
                    return self.respond({'error': 'Not found'}, 404)
                self.respond(result)
            except OSError:
                self.respond({'error': 'Local storage unavailable; existing history preserved'}, 503)
            except (ValueError, ArithmeticError, TypeError) as error:
                self.respond({'error': str(error)}, 400)

    print(f'Malik Oil LOCAL SYNTHETIC PREVIEW: {origin}', flush=True)
    print('No Firebase, cloud deployment, live market feed or FCM connection.', flush=True)
    HTTPServer(('127.0.0.1', args.port), Handler).serve_forever()


if __name__ == '__main__':
    main()
