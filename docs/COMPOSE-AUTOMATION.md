# Compose owner automation
This change targets GitHub main a583748 and the root app/ Compose project. The earlier android/ project and backend had been replaced upstream. Legacy experiments remain on a local wip branch and are excluded from this delivery.

## Use
Unlock the workspace with the Android device credential. Reports contains the Add commodity field, exact WhatsApp Business group title, capture consent, save settings, Android notification access, daily schedule, research now and Listen/Stop controls. Sarson is initially registered. Custom IDs use lowercase letters/digits/underscore/hyphen and a maximum of 20 registered commodities.

Incoming supported Business group MessagingStyle notifications on Android 9+ are filtered by package, exact title, group marker, opt-in start time and commodity keywords. SHA256 of group/message timestamp/body suppresses repeated notification delivery. The encrypted inbox retains 200 items. Explicit six-field rows with actual observation dates enter pending review. General text creates conservative editable drafts, including Urdu digits and explicit 40 کلو units. Missing grade/date, multiple locations and price ranges are held for owner review. Approval updates price history and source-isolated alerts. Notification times never substitute for rate observation dates.

Owner snapshots use Android Keystore AES-GCM and retain bounded rates, candidates, inbox, reports, sessions and alert data. The private UI requires the phone credential and screenshot capture is disabled. No phone access is granted during development. Group notifications depend on Android/WhatsApp delivery and notification content.

The persisted JobScheduler job targets the next 18:00 Asia/Karachi and needs network availability. Each run collects bounded public RSS headlines for registered commodities, creates a dated research session and saves a report. Battery/force-stop restrictions may delay or prevent execution. The Research now action runs the same source collection. Public headlines are labeled with their publication dates and source links. They are not a verified all-Punjab numeric rate feed or hosted AI synthesis. No key is embedded, no paid provider is invoked and no deployment is required. Production starts without generated example prices or fabricated grounded stories. Existing synthetic fixtures are opt-in tests only.

Daily summaries include owner-reviewed observations for the report day and actual public search results. CSV cells escape formula-like labels and preserve source/time/id. Existing Compose charts and report UI are retained. Installed offline TTS voices provide Listen/Stop with bounded queued chunks. Activity pause/disposal stops playback.

## Build
Use tools/build-apk.ps1 -Lint, or ./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug from the project root with JDK 17+ and Android SDK 35/Build Tools 36. Gradle wrapper 9.3.1 is included. Compose compiler matches AGP built-in Kotlin 2.2.10. Local debug signing uses the Android default debug keystore. Release signing is left unconfigured. The upstream application ID com.aistudio.malikoil.mktops is preserved, which differs from the original legacy 0.1 APK.

## Verification
Current Compose compilation, 16 JVM tests, debug APK packaging and lint passed (0 errors; 32 warnings). APK signature v2 and 16KB zip alignment checks passed. APK SHA256: 4A7EF2F103FDD4587B95F3F09C29E609A8DA67824383EEDD32A5B95BBE5E0378. Older legacy build counts are not evidence for this current layout. Live public RSS endpoint returned HTTP 200. Physical notification delivery, offline voice playback, owner unlock lifecycle and daily job delivery remain unverified without a connected phone. No cloud billing/deployment or live group activation occurred.
