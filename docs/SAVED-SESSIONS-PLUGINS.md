# Saved sessions and built-in plugins — 7 November 2026
## What works
Android research now has compact session actions, question bubbles, readable cited answers, source/evidence dialogs, copy, new/search/rename/delete/export sessions, quick/deeper controls and a Plugins screen. Setup-mode APK offers an interface/offline-plugin preview, with cloud research disabled until Firebase/backend are configured. No authentication bypass was added.

Android sessions are AES-GCM encrypted with Android Keystore using the existing SecureCache, stored under a UID-derived file name with UID-bound payload checks. Corrupt history is preserved and reported rather than silently replaced. Sign out/access-denial cleanup clears cached history by existing policy. Normal app restart retains it. Research controls are guarded while a request is in progress; request fields are snapshotted to preserve question/answer association.

Browser sessions are Windows-DPAPI encrypted at .preview/research-sessions.bin, available only through the loopback preview. Browser localStorage contains no conversation history. Refresh/restart restores sessions. Revision checks prevent two open tabs from silently overwriting each other. On conflict, export unsaved answers then reload. Encryption is at rest; the local preview is not a deployed authenticated cloud service.

Both platforms support at most 8 sessions and 12 answers/session. Full page bodies are omitted from stored turns; evidence quotes and citation links are retained. Storage is bounded; full storage requires owner export/deletion, never silent eviction. Exported files are plain text by explicit owner action. No automatic laptop/phone synchronization is configured.

## Built-in plugins
1. Web researcher: existing server API with citations, quick/deeper and quotas. Gemini/Tavily stay server-side. Real Tavily still needs the owner's key; no billing enabled.
2. Saved-session search: local search of saved questions/answers; no history uploaded to an AI provider for searching.
3. Rate converter: kg/40kg/100kg, decimal-safe rounding independently from the original amount.
4. WhatsApp row checker: validates owner-selected explicit rows, commodities/units, genuine dated timestamps, 20-row/50,000-character bounds and future-date rejection. Browser copies checked rows. Authenticated Android can open the existing owner-review import; no rate approval or group connection occurs automatically.

These are allowlisted app tools, not a third-party executable plugin marketplace. No external plugin account, downloaded code, live WhatsApp group connection, billing or deployment was added.

## Verification
Windows DPAPI round-trip across new vault instances, plaintext exclusion, tamper preservation, revision conflicts and deletion passed (3 tests). JavaScript converters/row checks passed (2 tests). Preview business regression passed (6); HTTP security including cross-origin vault rejection passed (4). Browser tests passed session reload/rename/delete/export/search, working plugins, safe rendering, follow-up and 9 research language/viewport combinations. Android JVM tests: 12 passed. Android build/lint and final artifact hash are recorded in APK-BUILD.md.

Physical-device rendering, actual Android Keystore restart and document-picker writes remain unverified because no connected Android device/emulator is available. Cloud Firebase/Tavily end-to-end setup remains pending. Compilation/JVM checks do not establish these device/provider behaviors.
