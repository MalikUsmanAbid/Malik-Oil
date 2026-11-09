# Local Build and Configuration Contract

From project root, `tools/build-apk.ps1 -Lint` resolves a local JDK/SDK, uses the checked-in Gradle
wrapper, runs assembleDebug and lintDebug, and copies the APK only on successful completion.
Optional OutputDirectory selects the artifact destination; default is ignored artifacts/.
VerboseBuild enables diagnostic logging. A failed build returns failure and must not be claimed
as a new artifact. Config-free APK startup remains setup-required.

`python tools/configure_firebase.py CONFIG_JSON --owner-uid REAL_UID` is local-only. It validates
exact Android package identity and required identifiers before writing ignored local properties;
optional UID updates backend public configuration. No remote resources, account or deployment are
created. Invalid input must not replace existing files. Existing API URL is preserved.

Published backend endpoints and alert behavior are unchanged: see docs/API.md. This feature
adds no external endpoint, production webhook or JARVICS transport.
