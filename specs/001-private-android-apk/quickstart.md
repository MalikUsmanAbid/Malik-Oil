# Quickstart Validation

From the permanent project root:

```powershell
.\tools\build-apk.ps1 -Lint
python tools/test_configure_firebase.py
```

Expected: Gradle reports BUILD SUCCESSFUL, the APK appears in artifacts/, lint has no errors,
and isolated configuration self-checks pass. No Firebase config is required to compile; inspect
MainActivity's setup-required branch rather than claiming a phone test was run.

After assembly, run the installed Build Tools 35 apksigner verify command and aapt dump badging
against the APK. Expected package com.malikoil.intelligence, min API 26, target 35, valid debug
signature. Record hash and actual check results in docs/APK-BUILD.md.

For real Firebase setup later, follow docs/FIREBASE-OWNER-STEPS.md and import the real config
with tools/configure_firebase.py. Do not deploy. Inspect the source backup for excluded private
config/caches. Live login, cache interaction and FCM receipt require a configured backend and
actual device and must stay pending until performed.
