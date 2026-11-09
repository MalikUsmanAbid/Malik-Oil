# Tasks: Private Android APK Build

**Input**: spec.md, plan.md, research.md, data-model.md and contracts/local-build.md.
**Tests**: Artifact inspection and isolated configuration tests are explicitly specified.

## Phase 1: Setup

- [X] T001 Preserve permanent Git project and deployment hold in PROJECT.md and AGENTS.md.
- [X] T002 Initialize Spec Kit and adopt principles in .specify/memory/constitution.md.

## Phase 2: Foundational

- [X] T003 Verify JDK/SDK/Gradle installation and document pinned versions in specs/001-private-android-apk/research.md.
- [X] T004 Protect local configuration in .gitignore and load optional fields in android/app/build.gradle.kts.

## Phase 3: User Story 1 — Verified APK (P1)

Goal: deliver one verified setup-mode APK. Independent test: assembly, lint, package/signature/hash.

- [X] T005 [US1] Preserve setup-required branch and correct email input/offline stale age in android/app/src/main/java/com/malikoil/intelligence/MainActivity.kt.
- [X] T006 [US1] Resolve observed failures and assemble debug APK through tools/build-apk.ps1.
- [X] T007 [US1] Fix actionable Android lint errors in android/app/src/main/ and pass lintDebug through tools/build-apk.ps1.
- [X] T008 [US1] Record package identity, minimum API 26, target API 35, debug signature and 64-character hexadecimal SHA-256 in docs/APK-BUILD.md.

## Phase 4: User Story 2 — Firebase Preparation (P2)

Goal: local setup guidance without passwords/remote actions. Independent test: isolated fixture import.

- [X] T009 [US2] Add isolated success/wrong-app/invalid-UID/missing-identifier checks in tools/test_configure_firebase.py; verify regressions fail before fixing.
- [X] T010 [US2] Validate all four nonempty identifiers and optional UID of at most 128 characters before writes in tools/configure_firebase.py; pass isolated checks.
- [X] T011 [P] [US2] Provide exact package, app-config and owner UID steps in docs/FIREBASE-OWNER-STEPS.md.

## Phase 5: User Story 3 — Saved Project (P2)

Goal: editable source and clean backup. Independent test: inspect folder and archive exclusions.

- [X] T012 [US3] Keep requirements, design and contracts in specs/001-private-android-apk/.
- [X] T013 [US3] Refresh current build status and reproducible commands in README.md, docs/STATUS.md, docs/SETUP.md and PROJECT.md.
- [X] T014 [US3] Deliver APK separately and source archive excluding caches, local configuration, build products and signing keys; document delivery in docs/APK-BUILD.md.

## Phase 6: Polish and Cross-Cutting Concerns

- [X] T015 Verify archive exclusions and reported checks against specs/001-private-android-apk/quickstart.md and docs/APK-BUILD.md.

## Dependencies and Execution Order

T001–T004 precede feature work. US1 follows T005 -> T006 -> T007 -> T008. US2 follows T009 ->
T010; T011 is independent documentation. US3 follows saved specifications, then T013/T014 after
build evidence and config checks. T015 follows all three stories.

## Parallel Examples

US1 toolchain/download inspection may proceed alongside US2's isolated tests; neither modifies
the other's files. US2 checklist T011 can be reviewed while the APK compiles. US3 can prepare
archive inclusion rules while compilation runs; final archive waits for completed evidence.

## Implementation Strategy

Deliver the verified setup APK first, then safe local Firebase preparation and a complete project
backup. Existing completed work is marked only after inspecting evidence. Never deploy as part
of a validation checkpoint. Device/live-provider tests remain explicitly outside this feature.
