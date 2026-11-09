# Malik Oil security review and repairs

Scope: native Android, Worker API/auth, free web research, loopback preview, local credential/config tooling and distributable artifacts. This is an implementation review and local regression test pass, not an independent penetration-test certification. No deployment, billing change or UI redesign.

## Findings and fixes

1. Android session/cache boundary (high impact, fixed): pending API results could return after logout/account change. Responses are now checked against the initiating user before network use and before returning; cache rendering checks the current user too. Known401/403 or Firebase invalid-user denials clear the denied user's local session, cached data and notifications, preventing later offline fallback from restoring denied data. A late denial cannot sign out a different current user. Normal network failures may still use that owner's encrypted offline cache, as intended. Pure helper tests cover changed/missing sessions and explicit denials; real lifecycle/Firebase testing remains pending.
2. Unbounded responses (medium, fixed): Android success/error bodies now have2MiB/64KiB caps; cache file reads capped8MiB. Gemini JSON capped256KiB, feeds1.5MB with declared/streamed size checks and cancellation. Existing request cap64KiB retained. Oversized bearer tokens rejected before JWT work. Responses remain encrypted at rest only in Android cache; public research snippets themselves are not private business data.
3. Evidence integrity (medium, fixed): numeric claims previously passed substring matching (e.g.1inside100). Match whole numeric tokens now, including Urdu/Arabic digits. Exact evidence quotes/source IDs remain required. Trusted system instructions separated from untrusted feed data. This reduces integrity risk; it does not prove semantic truth or eliminate model hallucination. AI cannot approve rates or change baselines.
4. Sensitive mobile display (medium privacy hardening): FLAG_SECURE protects sensitive screens/task-switcher captures. No visual redesign. Screenshots/nonsecure casting may be blocked; actual device behavior untested.
5. URL/input handling (medium/low, fixed): imported provenance URLs reject embedded username/password and values over2048characters. Android API/source navigation requires valid HTTPS without userinfo. Shared incoming text capped50000characters. SQL remains parameterized; no arbitrary source fetching was added.
6. Loopback server resource/framing (low/local, fixed):10-second socket timeout, duplicate Content-Length/Transfer-Encoding rejection, no-referrer and frame-denial headers. Host/origin checks, no-store, nosniff and CSP retained. The preview uses a same-machine trust boundary, not Firebase login: another local process/user may access loopback endpoints or spoof Origin. Keep it bound127.0.0.1 and use only synthetic data; it must not be exposed as a production owner portal. Authenticated Worker remains separate.

## Existing controls checked
- Firebase signature validation pinnedRS256, issuer/project/audience/ownerUID, expiry and revocation cutoff. Missing setup/token fails closed before database/provider access. No auth-bypass found in reviewed routes.
- Android cache AES-GCM with Android Keystore, UID-bound encrypted payload; backups disabled/excluded; push service unexported and notifications carry opaque alert IDs. Backend stores durable deduplication and notification leases; no token logging found.
- Source HTTPS host allowlist with redirect checks, byte/time limits, stale/future filtering, XML entity declaration rejection. Secrets sent only to Gemini, never public feeds. Returned web content uses text nodes; AI output is not executed.
- Gemini credential uses Windows DPAPI, Git-ignored .preview files. Current key not found in scanned plaintext project files or decompressed APK/source archive. DPAPI does not protect against malware running as the same Windows user.
- npm audit all dependencies:0known vulnerabilities at review time. Android dependency vulnerability scanning was not performed; lint is not such a scan.

## Verification
- Full backend18tests PASS; current TypeScript check and10targeted security/research regressions PASS after final backend edits.
- Python6preview behavior tests,3HTTP security tests,7SQLite alert tests and4Firebase-config importer tests PASS.
- HTTP security checks reject wrongHost/crossOrigin, duplicate lengths, chunked framing, malformedJSON and oversized body declarations; isolation headers verified.
- Android assemble/lint and security helper tests: final artifact results recorded in APK-BUILD.md. Lint warnings retained; no broad suppression.
- Live free-research smoke after final backend edits:HTTP200,free-web-research,2sources/2evidence-linked findings. No paid search/billing.
- APK signature/alignment and sanitized archive/key scans recorded alongside delivered artifact.

## Residual risks / owner action
- The Gemini key previously posted in chat is exposed. Local encryption cannot revoke that copy. Owner must revoke it in Google AI Studio and replace it using tools/setup-research-key.ps1. No account revocation or key rotation was performed by this audit.
- Debug APK/setup mode is for testing, not a production-signed release. No physical device/emulator screenshot, offline-race, Firebase sign-in/revocation or real FCM delivery test was possible. Firebase/cloud deployment remains unconfigured.
- JWT verification checks a configured AUTH_NOT_BEFORE cutoff; it does not query Firebase for live disable/revocation state on every request. Stolen valid tokens can remain usable until expiry/cutoff update. Production hardening should include tested revocation operations, app/device protections and retention policies.
- No biometric app lock was added. A signed-in owner on an unlocked device can read that owner's offline cache by design; rely on device lock until a separately agreed app-lock flow is implemented.
- Public news research is best-effort and currently headline-based; not verified Samundri mandi pricing. Quotas/source outages return a labeled fallback. Security fixes do not establish data accuracy or24/7uptime.

References: https://developer.android.com/security/fraud-prevention/activities ; https://firebase.google.com/docs/auth/admin/verify-id-tokens ; https://firebase.google.com/docs/auth/admin/manage-sessions
