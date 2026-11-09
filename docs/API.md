# API and smart alert specification

All `/v1/*` routes require `Authorization: Bearer <Firebase ID token>` from the single configured owner. JSON mutation bodies use `Content-Type: application/json`. No browser CORS access is enabled. Data responses are no-store. Errors never contain credentials.

| Method | Path | Behavior |
|---|---|---|
| GET | /health | Public service/version liveness only; not a source freshness or DB readiness check |
| GET / POST | /v1/commodities | List or save `{id,name}` |
| GET | /v1/rates | Latest quote per stream, max 200; `stale` flag |
| GET | /v1/candidates | Pending review, max 100 |
| POST | /v1/candidates | Validate and queue one structured quote |
| POST | /v1/import/whatsapp | `{source,text}`, explicit pipe-separated rows, max 20 parsed observations |
| POST | /v1/candidates/{id}/approve | Idempotent approval; DB trigger evaluates alert |
| GET | /v1/alerts | Recent 100 alerts with signed percent |
| POST | /v1/devices | `{id:UUID,token:FCM_TOKEN}` registration/rotation |
| DELETE | /v1/devices/{id} | Disable delivery to one installation |
| GET | /v1/deliveries | Recent delivery status; no token exposure |
| GET | /v1/audit | Recent 100 action records |
| GET | /v1/summary | Deterministic 24h observation count and min/max by commodity |
| POST | /v1/analytics/margin | Explicit business scenario calculator |
| POST | /v1/ai/summary | Disabled by default; optional AI binding/model required |

A structured candidate has:

```json
{
 "commodity":"sarson", "market":"Lahore", "grade":"standard",
 "source":"Owner manual", "sourceUrl":"",
 "amount":8000, "unit":"40kg", "observedAt":1792300000,
 "sourceEventId":"unique-source-observation-id"
}
```

Example is synthetic; supply a real observation and timestamp. `sourceEventId` must be stable when retrying the same source observation. Server computes a SHA-256 fingerprint over normalized stream, observation time, normalized price and source event ID. Resubmitting that candidate returns the same ID. Changing the source ID does not produce another alert for an equal timestamp. Different source labels are different streams; choose stable labels.

Margin input example: `{quantityKg:1000,buyPkrKg:200,sellPkrKg:210,transportPkr:2000,otherCostsPkr:500}` as JSON with quoted keys. Output: landed cost PKR 202,500; revenue PKR 210,000; gross margin PKR 7,500; break-even PKR 202.5/kg. Nothing is persisted as an accounting transaction.

## Exact 3% rules

1. Normalize declared PKR units to integer paisa/kg, rounding once to nearest paisa. Preserve grade/market/source stream identity.
2. First fresh approved quote seeds the stream baseline and latest observation watermark. No alert.
3. A later fresh quote alerts when `abs(new_price - baseline_price) * 100 >= baseline_price * 3`. Positive movement = rise; negative = fall. Equality at 3% qualifies.
4. Compare with first fresh quote or last **alerted quote**, not every last tick. Thus cumulative 1% steps eventually cross 3%.
5. On alert, atomically replace baseline with new quote. Same price thereafter produces no repeat. A reverse movement of at least 3% from the new baseline produces a new alert; no blanket cooldown suppresses it.
6. Old or equal observation timestamps are historical only and never advance the watermark, baseline or alerts. A quote more than 24h old on receipt is historical only; more than five minutes in the future is rejected by ingest. SQL also gates fresh observations.
7. More than 24h between qualifying observations starts a new baseline without a misleading gap alert. Within the gap limit, staleness rules still apply. The 24h policy is a documented MVP default; changing it requires deliberate code/config work.
8. First exact example: 100 → 102.99: no alert; → 103: +3% alert and new baseline 103; → 99.91: -3% alert; → 99.91 again: no alert.

Owner approval order is authoritative; approve chronologically to get chronological live alert evaluation. An older quote approved later cannot rewind history. Corrections require a new source observation/version; there is no silent rewrite or retroactive alert replay.

Current endpoints are bounded snapshots, not a complete paginated history API. Alerts are fixed at 3% both directions. Current routes do not expose a lower threshold. Quiet hours, per-item threshold tuning, source reliability scoring and scheduled digests are future modules.
