# Feature Specification: Private Android APK Build

**Feature Branch**: `master` (feature directory is independent of branch naming)

**Created**: 2026-11-04

**Status**: Implemented for local APK delivery; live integrations remain deferred

**Input**: User description: "JDK aur Android SDK setup karke Malik Oil ka APK build karo aur errors khud fix karo. Firebase setup mein jo meri taraf se chahiye woh bata do, deployment abhi mat karna. Files save karo; ye bhi ek project hai. Complete karo."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Obtain a verified private APK (Priority: P1)

The owner receives an Android installation file from the saved Malik Oil project.

**Why this priority**: Source alone cannot be installed by the owner.

**Independent Test**: Produce the APK, inspect package/signature and check setup-required code;
report actual phone testing separately.

**Acceptance Scenarios**:

1. **Given** the project and Windows laptop, **When** prerequisites are installed and build errors
   fixed, **Then** the owner receives a reproducible installation file without cloud deployment.
2. **Given** no real service configuration, **When** the app opens, **Then** it requests setup
   rather than showing invented accounts or live market data.
3. **Given** an interrupted download, **When** work resumes, **Then** failures are repaired and
   only completed checks are reported as passed.

### User Story 2 - Prepare real Firebase setup (Priority: P2)

The owner knows which app configuration and owner identifier are needed without sharing passwords.

**Why this priority**: Real account setup is the next owner-dependent step.

**Independent Test**: Check the owner instructions and exercise local import with isolated fixtures.

**Acceptance Scenarios**:

1. **Given** correct app configuration, **When** imported, **Then** local settings are saved and
   excluded from source archives; no cloud resource is created.
2. **Given** configuration for another app, **When** imported, **Then** it is rejected without
   replacing existing local settings.
3. **Given** the checklist, **When** the owner follows it, **Then** app registration, owner UID
   and email/password enablement are explicit; no owner password or server key is requested now.

### User Story 3 - Keep a standalone editable project (Priority: P2)

The owner retains source, requirements, plans, build instructions and a source backup.

**Why this priority**: Future work must resume from a permanent editable project.

**Independent Test**: Inspect the project and archive for required files and excluded private data.

**Acceptance Scenarios**:

1. **Given** the permanent project, **When** opened later, **Then** source, specs, plan, tasks and
   one-command build instructions remain together.
2. **Given** the source archive, **When** inspected, **Then** it contains no credentials,
   machine-local SDK paths, dependency caches, build products or signing keys.

### Edge Cases

- Broken downloads cannot be reported as a successful APK build.
- Missing credentials must not introduce an authentication bypass.
- Offline rate age must keep advancing as the phone clock advances.
- A debug signature must not be described as final owner-controlled release signing.
- Without a phone/emulator, runtime behavior remains unverified.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Produce an Android APK using locally installed prerequisites.
- **FR-002**: Retain `com.malikoil.intelligence` package identity and Android 8+ support.
- **FR-003**: Show setup-required status without real configuration; never create fake credentials.
- **FR-004**: Preserve owner-only access, source provenance and deterministic 3% alerts.
- **FR-005**: Provide Firebase app-registration/configuration, owner UID and email/password steps;
  the owner's password stays with the owner.
- **FR-006**: Reject wrong-app imports before replacing any local configuration.
- **FR-007**: Preserve editable source, specs and plans in the permanent folder; exclude private
  configuration, caches, build products and signing material from the source archive.
- **FR-008**: Pass APK assembly and applicable static checks; record package, signature and checksum
  evidence, separately from device/live-service verification.
- **FR-009**: Do not deploy, create cloud accounts/resources, mutate remote data, send live messages
  or publish during this feature.
- **FR-010**: Identify remaining live-integration and device-testing dependencies in delivery notes.

### Key Entities

- **Build artifact**: App version, package identity, signature type and checksum.
- **Owner configuration**: Real app identifiers and owner UID; never an account password.
- **Verification record**: Installed toolchain, observed check results and explicit pending checks.
- **Saved project**: Editable source, requirements, plans, tasks and archive.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Deliver one APK whose package, supported Android version and signature can be read
  from the artifact.
- **SC-002**: Provide one documented command to rebuild from the saved project.
- **SC-003**: An isolated wrong-app import check leaves existing settings unchanged.
- **SC-004**: The source archive contains zero private credentials, signing keys or dependency caches.
- **SC-005**: Perform zero cloud deployments/resource changes; list untested services/device behavior.

## Assumptions

- This feature completes local APK delivery of the existing MVP, not the full future roadmap.
- No real Firebase configuration or running backend URL has been supplied.
- A debug-signed setup-mode APK is the first test artifact.
- Existing backend/market code is preserved; live integrations remain later work.
