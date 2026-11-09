# Malik Oil · Private Owner Rate Intelligence

MVP source scaffold with a verified debug setup APK, updated 5 November 2026 (Asia/Karachi). Nothing deployed; no real credentials, account creation, production messages or live prices included.

## Start here

- [Architecture, security, roadmap](docs/ARCHITECTURE.md)
- [Setup and APK build](docs/SETUP.md)
- [Official-source research and free limits](docs/RESEARCH.md)
- [API and alert rules](docs/API.md)
- [Verification and remaining work](docs/STATUS.md)

## Included

Native Kotlin Android project: owner email/password login, latest rates, alerts, commodity additions, manual rates, WhatsApp pasted/shared-text import, approval queue, Android Keystore encrypted offline cache, FCM token registration and notification deduplication. Backend: Cloudflare Workers TypeScript, D1 migration, Firebase JWT owner allowlist, audit records, bidirectional 3% alert evaluation in an atomic SQL transaction, delivery leases and retries, market summaries, business margin calculator API, optional cloud AI endpoint. Optional laptop-local Ollama summary tool and future JARVICS event/source adapter contracts.

Initial items: sarson, canola, til, taramira, alsi and other. Add named commodities without changing the schema. Explicit units: kg, 40kg and 100kg, all PKR. No implicit maund conversion.

## Local backend

Requires Node 22+ and Python 3 for SQL tests. From `backend/`:

```powershell
npm ci
npm run check
npm test
python test/test_alerts.py
npm run migrate:local
npm run dev
```

`GET http://127.0.0.1:8787/health` is public. Every data endpoint fails closed until Firebase project and owner UID are configured. There is deliberately no development auth bypass. Local tests require no credentials. See setup for authenticated device testing.

## Scope boundary

This is an actionable scaffold, not a production-complete market feed or an already-running service. A debug setup APK has been compiled and verified; see [APK build report](docs/APK-BUILD.md). AMIS collection, unrestricted WhatsApp history access, full accounting, on-phone LLM inference and JARVICS transport are not implemented. Review the status file before deployment. The laptop can be off after an authorized cloud deployment; a free service cannot promise an uptime SLA or instant data from every Punjab mandi.

## Local workflow preview

For a synthetic browser workflow test, run `python tools/preview/server.py --port 8765`, then open http://127.0.0.1:8765/. This is not native Android execution. See [preview notes](docs/LOCAL-PREVIEW.md).

## Current local build

JDK/SDK are installed on this machine. From this project root, run `tools/build-apk.ps1 -Lint`.
Real Firebase preparation: [owner steps](docs/FIREBASE-OWNER-STEPS.md). Spec Kit feature: [private APK](specs/001-private-android-apk/spec.md).
