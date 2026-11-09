# Feature Data Model

No business-database schema changes are needed.

## Build artifact / verification record

Package identity is exactly `com.malikoil.intelligence`; version is 0.1.0, code 1. Minimum API is
26; target API is 35. Record APK filename, positive byte size, 64-character hexadecimal SHA-256,
debug signature verification result, build/lint results, toolchain versions and test limitations.
States: source-only -> building -> assembled -> inspected -> delivered. A failed build cannot
advance to assembled. Device/live-service checks remain separately pending.

## Local Android configuration

Fields are MALIK_FIREBASE_API_KEY, MALIK_FIREBASE_APP_ID, MALIK_FIREBASE_PROJECT_ID,
MALIK_FIREBASE_SENDER_ID and optional MALIK_API_URL. Configuration is absent by default. Import
requires a client whose Android package exactly matches the app and all four nonempty Firebase
identifiers. Optional owner UID is nonempty and at most 128 characters. Validate all supplied
values before writing either local app properties or backend identifiers. Wrong input leaves
prior settings unchanged. No owner password or server private key is part of this entity.

## Project bundle

Editable source, specs, docs, lockfiles and Gradle wrapper are included. Dependency caches,
private local properties, runtime state, signing keys and build products are excluded from source
archives. APK is delivered separately. Existing rate/alert entities remain unchanged; see
backend/migrations/0001_init.sql and docs/ARCHITECTURE.md.
