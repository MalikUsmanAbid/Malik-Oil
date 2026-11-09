# Setup and private release

No command in this project was used to create a cloud resource or deploy. The following cloud steps are instructions for a later owner-approved setup. Do not paste secrets into chat or commit them.

## 1. Local verification

Install Node 22+ and Python 3. Open a terminal in `backend/`, run `npm ci`, `npm run check`, `npm test`, `python test/test_alerts.py`, `npm run migrate:local`, then `npm run dev`. Wrangler may ask to allow its native workerd/esbuild install scripts; approve only those expected installed dependencies if required by your npm configuration. `/health` should respond locally; `/v1/rates` returns 503 until configured. Do not add a hardcoded token to bypass this.

The local database is under `.wrangler/`, excluded from delivery archives and Git. Local mutations do not change the cloud database. No application data is seeded except the commodity registry.

## 2. Firebase owner project (after consent)

Create/select a Spark project. Register Android package `com.malikoil.intelligence`. Enable email/password Auth. Create the owner account explicitly in Firebase console; record its exact UID. Do not put the owner's password in a source file. Configure secure account recovery. No sign-up button is present in the app; backend UID allowlist is the actual access boundary.

Copy the Firebase project ID and owner UID to `backend/wrangler.jsonc`. Keep `AUTH_NOT_BEFORE` at 0 initially; use it for session invalidation later. Copy public Firebase Android API key, mobile app ID, project ID and sender/project number to private local Gradle properties using `android/owner-config.properties.example` as field guidance. These are app configuration; do not use the service-account private key here. Restrict API key usage to the appropriate Firebase APIs/app where supported.

Enable FCM HTTP v1 API. Create a service account with only the needed FCM sending permission (e.g. Firebase Cloud Messaging API Admin for the chosen project, review organization policy). Keep PEM key and service-account email in secure local storage. Later set Worker secrets `FCM_PRIVATE_KEY` and `FCM_CLIENT_EMAIL`. Never ship this key in Android resources or google-services.json. Rotate/revoke any exposed key.

## 3. Cloudflare backend (only after deployment consent)

Keep Workers on Free. Authenticate the Cloudflare CLI yourself, create one D1 database named malik-oil, copy its actual ID to `database_id`, review region/residency choices, then apply migrations remotely. Example commands from `backend/`, to be run only after authorization:

```powershell
npx wrangler login
npx wrangler d1 create malik-oil
# Edit database_id, FIREBASE_PROJECT_ID and OWNER_UID using returned/actual values.
npx wrangler d1 migrations apply DB --remote
npx wrangler secret put FCM_CLIENT_EMAIL
npx wrangler secret put FCM_PRIVATE_KEY
npm run deploy
```

Enter secrets through prompts. For the private key, supply the full PKCS8 PEM, preserving line breaks or using literal `\n` escapes; never pass a secret in command-line arguments. The Worker converts literal newline escapes. Copy the returned workers.dev HTTPS URL into `MALIK_API_URL`. Custom domain is optional and may cost money. The only cron is a five-minute notification-outbox dispatch; it does not collect AMIS rates.

Optional cloud AI: leave disabled for initial pilot. After owner opts in, add `"ai":{"binding":"AI"}` to Wrangler config, select a currently free-eligible model ID using the official catalog, set AI_MODEL and ENABLE_CLOUD_AI=true, and re-deploy after approval. A model/provider failure can be handled by calling `/v1/summary`. No private chat upload is part of this endpoint.

## 4. Android APK

This machine now has Temurin JDK 21 (compiling to Java 17), SDK Platform 35 and Build Tools 35.0.0. Android Studio is optional for editing; the checked-in build helper also builds from a terminal. Open `android/`, synchronize Gradle. Wrapper pins Gradle 8.11.1 and its distribution checksum. Configure `local.properties` with your actual SDK path if Android Studio does not do so. Pass owner Firebase public configuration and API URL through local Gradle properties; do not alter the source to embed passwords or service keys.

From `android/` on Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

APK path: `app/build/outputs/apk/debug/app-debug.apk`. A build with no Firebase config opens a setup screen; it does not invent a working account. A private pilot needs the actual approved cloud backend URL. Release APK: use Android Studio Generate Signed Bundle/APK and create an owner-held signing keystore with a strong password; preserve an encrypted backup for future updates. No release key or signed APK is included. Do not distribute the debug-signed APK as a final release.

Install on a Google Play services capable Android 8+ device. Sign in as owner. Grant notification permission on Android 13+. Confirm device registration succeeds. Select Rates or Alerts to populate encrypted offline snapshots, then turn off connectivity to check the dated cache display. Synthetic rates used for testing must have current timestamps, clearly labeled source TEST and an isolated test project.

## 5. Private pilot acceptance

- Owner login works and a second Firebase account cannot read, import, approve or register a device.
- Approve synthetic 100, 102.99, 103, 99.91 observations with increasing times in one test stream: exactly two alerts, both signs correct.
- Retry approval/import: no repeated alert or normal repeated notification. Observe device receive and background/foreground behavior, including permission denied/offline/force-stop.
- Rotate FCM token, disable installation and inspect delivery retry/failure history. Verify the 5-minute dispatch delay is acceptable.
- Offline rates retain original observed time and cache saved time; no stale quote looks live. Signout clears cache and notification shade; offline signout best-effort device unregister may need console/API cleanup.
- Export and restore a D1 backup to a separate test database. Back up signing key. Check provider usage dashboards and document actual source coverage.

A useful next implementation requires choosing actual mandi/grade naming and an approved AMIS or other data source fixture, then testing that adapter against saved source pages. No need to connect personal WhatsApp account credentials for the initial MVP.

## 6. Play Store later

Use AAB with proper signing/Play App Signing, current target API, privacy policy and accurate Data Safety disclosures, developer verification and applicable testing tracks. Registration/testing is governed by the current official Play Console requirements; API 35 scaffold settings must be reviewed at that stage. Private owner access remains enforced by backend UID even if the listing is later publicly discoverable.

## Verified local build update · 5 November 2026

Run `tools/build-apk.ps1 -Lint` from the project root to rebuild using the installed toolchain.
The initial debug APK and signature/package/checksum evidence are recorded in docs/APK-BUILD.md.
Import real non-secret app config with tools/configure_firebase.py only after the owner supplies it;
local import does not create resources or deploy. No Firebase service account key is needed now.
