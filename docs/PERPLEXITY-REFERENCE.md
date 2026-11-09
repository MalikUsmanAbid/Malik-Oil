# Conversational research reference — 7 November 2026
The owner supplied a screenshot and https://github.com/kevinmichaelchen/perplexity-opencode . The repository is an OpenCode search-nudging plugin, not the complete Perplexity app. It requires a Perplexity API key/MCP service. We inspected its README, package metadata and entry point; no plugin was installed and no external source code was vendored.

## Implemented
- Browser: readable conversational answers, question bubbles, inline clickable citations, actual source counts, source drawer with exact evidence, search details and real elapsed time; copy and text export.
- Browser: up to 8 in-memory sessions, 12 answers/session, session switching, new research and automatic separation when commodity/market changes. Refresh clears sessions; no browser research history is persisted.
- Three UI languages: English, Urdu RTL, Roman Urdu. Research request language remains attached to each answer.
- Quick: one basic Tavily search. Deeper: up to 3 basic queries, including nearby Faisalabad/AMIS search for Samundri rate questions; provider page extracts when returned. Results can include historical/unknown-date content, explicitly labeled. It is bounded multi-query research, not an autonomous recursive agent.
- Synthesis exposes only findings passing source-ID, exact evidence-quote and numeric-token validation. These checks do not prove every interpretation correct. Unverified historical/nearby rates cannot become approved observations automatically.
- Existing 20-attempt/day cap plus new atomic 900-search-credit/month reservation per database. Failed attempts are conservatively charged against the cap. Other apps/databases can use the same account quota; owner must leave pay-as-you-go OFF. No billing API or deployment performed.
- Native Android: quick/deeper controls, consecutive answers on the research screen, new research, source labels and source links. Its session history does not persist outside that screen. Browser's multi-session sidebar is not a full native replica.

## Verified and outstanding
TypeScript, 28 backend tests, 2 monthly-budget tests, 6 preview tests, 7 SQL alert tests and 3 HTTP security tests passed. Browser checks cover citations, sessions/export, follow-up/reset and research layout in 9 language/viewport combinations. Android build/lint passed: 0 errors, 1 dependency-version warning. Physical device/Firebase delivery remain unverified.
Live loopback research returned cited findings using RSS + Gemini. Tavily credential is absent; real Tavily search/page extraction remains untested until owner securely saves their own key with tools/setup-tavily-key.ps1. No live mandi rate verified.
Not implemented: proprietary Perplexity backend/model behavior, plugins marketplace, teammates, scheduled agents, cloud-synced sessions, voice/files/image research, cross-device sharing and full native UI parity. No nonfunctional placeholder controls are shown for them. These need separate integration/design slices; this is not A-to-Z product parity.

Official search API behavior reviewed:
https://docs.tavily.com/documentation/api-reference/endpoint/search
