# Implementation Plan: Private Android APK Build

**Branch**: `master` | **Date**: 2026-11-04 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification in specs/001-private-android-apk/spec.md.

## Summary

Finish the existing native Android scaffold with the installed verified toolchain. Repair
network/build/lint failures, retain setup mode without credentials, inspect the resulting APK,
and preserve source and Spec Kit artifacts in the permanent project. No remote action is included.

## Technical Context

**Language/Version**: Kotlin 2.1.10; JVM target 17; installed Temurin JDK 21.0.12.1+1.
**Primary Dependencies**: AGP 8.9.2, Gradle 8.11.1, Firebase BOM 33.10.0, coroutines 1.10.1.
**Storage**: Existing encrypted Android private-file cache and D1 backend schema; no schema change.
**Testing**: Gradle assembleDebug/lintDebug, SDK package/signature inspection, isolated Python
configuration-import self-check; existing 15 backend/SQL checks are preserved.
**Target Platform**: Windows x64 build host; Android API 26 minimum, compile/target API 35.
**Project Type**: Native mobile app with existing independent TypeScript Worker backend.
**Performance Goals**: Reproducible local build with cached dependencies; no invented phone timing
or live service SLA. Build duration depends on machine and upstream downloads.
**Constraints**: No credentials supplied; no deployment; official-source software and verified
archive checksums; local/private files excluded from source distribution.
**Scale/Scope**: One owner, one test APK; full live market/accounting roadmap remains separate.

## Constitution Check

Pre-research and post-design gates: PASS. Exact owner authorization is preserved; no fake rates,
keys, cloud resources or paid inference are introduced. Build and provider/device verification are
reported separately. Source remains modular; private caches/configuration are excluded. No
constitution exception is needed.

## Project Structure

### Documentation (this feature)

```text
specs/001-private-android-apk/
  spec.md
  plan.md
  research.md
  data-model.md
  quickstart.md
  contracts/local-build.md
  checklists/requirements.md
  tasks.md
```

### Source Code (repository root)

```text
android/app/src/main/java/com/malikoil/intelligence/
android/app/build.gradle.kts
tools/build-apk.ps1
tools/configure_firebase.py
tools/test_configure_firebase.py
backend/src/
backend/test/
docs/FIREBASE-OWNER-STEPS.md
docs/APK-BUILD.md
PROJECT.md
```

**Structure Decision**: Extend the existing Android/backend layout and standard tooling. Keep
build/configuration helpers small, with no extra service or deployment pipeline.

## Delivery Sequence

1. Confirm pinned SDK/Gradle/JDK and protected local property loading.
2. Rebuild against real official dependencies, fixing observed failures only.
3. Run Android lint; correct actionable issues without blanket suppression.
4. Verify artifact identity, signature and SHA-256; state lack of device/live-provider tests.
5. Exercise the configuration helper in an isolated temporary project.
6. Refresh status, Firebase steps and source archive; check exclusions and convergence.

## Complexity Tracking

No violations or extra architectural components. Research is consolidated in research.md; existing
market/API contracts remain in docs/API.md and contracts/.
