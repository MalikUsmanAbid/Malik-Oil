# Research Decisions

## Build toolchain

**Decision**: Retain AGP 8.9.2 / Gradle 8.11.1 / Platform and Build Tools 35. Use the installed,
checksum-verified Temurin JDK 21 with Java/Kotlin compilation target 17.
**Rationale**: Official AGP compatibility specifies JDK 17 minimum, Gradle 8.11.1 and API 35. The
existing installed JDK avoids another unrelated installation; actual compilation verifies this
combination. The research review suggested JDK 17 from the original docs; observed installation
and compatible compilation target take precedence.
**Alternatives considered**: Latest AGP/API migration or a second JDK installation; unnecessary
for the requested artifact. [Official compatibility](https://developer.android.com/build/releases/agp-8-9-0-release-notes).

## Setup APK and security

**Decision**: Deliver a debug-signed setup-mode artifact with real service fields empty.
**Rationale**: This produces a local test installation without fabricated credentials or an auth
bypass. Signature/package verification is separate from device behavior.
**Alternatives considered**: Fake Firebase identifiers, shared tokens or deploying a demo backend;
rejected because they conflict with owner privacy and deployment hold.

## Permanent project and configuration

**Decision**: Keep source and Spec Kit files in Documents/Codex/Projects/Malik-Oil; import real
Android config only into ignored local properties. Request google-services.json for the exact
package and the owner UID; never request the owner's password.
**Rationale**: The existing configuration helper and backend allowlist already fit this flow.
**Alternatives considered**: ZIP-only storage or embedding server keys in Android; rejected.
[Firebase setup](https://firebase.google.com/docs/android/setup),
[token verification](https://firebase.google.com/docs/auth/admin/verify-id-tokens).

## Network failure recovery

**Decision**: Retry the failed official dependency downloads in a fresh build process while
reusing successfully cached artifacts.
**Rationale**: The previous log ended in DNS failures for Google-hosted AARs; current direct HTTPS
checks return 200. This is not evidence of a source compiler failure.
**Alternatives considered**: Disabling TLS, unverified mirrors or changing SDK libraries; rejected.

## Deferred integrations

**Decision**: No live Firebase, Cloudflare, WhatsApp, market-source or JARVICS integration is
activated in this feature. Follow the existing roadmap later.
**Rationale**: The owner explicitly prohibited deployment and supplied no real configuration.
**Alternatives considered**: Temporary remote demo; rejected. No in-scope unknown remains;
missing live credentials are a documented dependency for a separate phase, not invented values.

Research agent review was completed; conclusions were reconciled against installed toolchain
and current build evidence. Broader official-source limits remain in docs/RESEARCH.md.
