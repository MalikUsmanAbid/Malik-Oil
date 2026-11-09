# Malik Oil project instructions

This is the canonical source project for the owner's private Android rate-intelligence app and cloud-backend scaffold.

- Keep owner-only backend authorization, the 3% bidirectional alert policy, source provenance, audit records and encrypted offline cache intact.
- Never invent production credentials, real mandi observations, source API mappings or JARVICS endpoints.
- Cloud deployment, remote database mutations, live messaging integration and Play publication are on hold until the owner explicitly requests them. Local builds and fixes are authorized.
- Use `tools/build-apk.ps1 -Lint` for Android build and lint. App configuration can be imported using `tools/configure_firebase.py`; never include a server private key in Android or source archives.
- `android/firebase.local.properties`, `android/local.properties`, caches, build products and keystores must remain out of Git and source archives.
- No Firebase configuration means a setup-mode APK; do not describe it as a connected production app.
- Backend checks from `backend/`: `npm ci`, `npm run check`, `npm test`, `python test/test_alerts.py`. No deployment is needed for these checks.
- Keep docs/STATUS.md and docs/APK-BUILD.md consistent with actually completed checks. A compiler or lint pass does not establish on-device/Firebase delivery success.

- Spec Kit is initialized. Resume the active feature through .specify/feature.json; do not reinitialize.
