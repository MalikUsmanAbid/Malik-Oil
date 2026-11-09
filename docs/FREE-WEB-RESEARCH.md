# Free web research — implementation and verification

Replaced unavailable Google Search grounding with public RSS discovery plus ordinary Gemini3.5Flash-Lite API generation, without the google_search tool. No billing, deployment, product UI changes or additional credentials.

Public feeds: two GoogleNews RSS searches (selected mandi/commodity and Pakistan oilseed context), Dawn business feed, BusinessRecorder latest-news feed. New requests fetch again. Only fixed HTTPS hosts; redirects checked,10-second fetch deadlines and1.5MB per-feed cap, maximum8evidence sources and one35-second AI request. Feed outages are reported. GoogleNews RSS is a best-effort public feed, not a supported unlimited search API or full-web index. No CAPTCHA bypass or arbitrary URL fetching. GDELT was researched but live probe returned no useful matching results, so not used. AMIS/PITB live probes failed; no verified mandi collector claim.

Sources have title, publisher, publication time, check-time and headline-only/feed-excerpt label. Records older than30days, far-future dates and irrelevant records discarded. Feed excerpts are not full article retrieval; GoogleNews links may redirect to publisher articles. Structured AI findings must use an actual source ID and an exact copied evidence quote. Unsupported numeric tokens, including Urdu digits, are withheld. This checks citation/evidence linkage, not complete logical entailment; AI interpretation still needs owner review. No automatic rate approvals, alerts or trading orders.

If Gemini quota/network/format/evidence validation fails, return explicitly labeled sources-only result. Never silently convert model memory to current prices. No matching sources => no AI call. Existing owner auth and20attempt/day budget remain.

Live tests: local POST /demo/research, Samundri/sarson, en/ur/roman all HTTP200 mode free-web-research,2sources,2validated findings. Live supporting headlines dated5November2026 concerned Pakistan oilseed production targets; none established a current Samundri mandi quote. Urdu initially had a format failure; explicit JSON Schema output was added and all three languages then passed. Article body/source availability differs by request. At test time the useful results were headline-only. Preview remains synthetic for its rates/chart; research now retrieves public live news evidence separately.

Verification: TypeScript compilation and full backend suite, including7research tests for network boundaries, freshness, malformed citations, secret isolation, quota fallback and no-source handling. See STATUS for final result. No on-device/APK or visual browser interaction test in this change. Existing UI labels may still refer to Google Search because owner requested no UI work; runtime narrative/notice explicitly identifies RSS research. APK code unchanged; source/backend and preview updated.

Key is held in Windows DPAPI storage, excluded from source archives. Previously shared key should be rotated through tools/setup-research-key.ps1.

References:
https://ai.google.dev/gemini-api/docs/structured-output
https://ai.google.dev/gemini-api/docs/pricing
https://news.google.com/rss/search?q=Pakistan%20oilseed&hl=en-PK&gl=PK&ceid=PK:en
https://www.dawn.com/feeds/business
https://www.brecorder.com/feeds/latest-news
https://docs.searxng.org/dev/search_api.html (public instance formats often disabled; not used)
https://blog.gdeltproject.org/gdelt-doc-2-0-api-debuts/ (alternative researched; not used)
