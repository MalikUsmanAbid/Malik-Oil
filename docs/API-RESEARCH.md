# Current implementation
Free RSS/news evidence + normal Gemini synthesis is now implemented and live-tested in3languages. Paid grounding is no longer called. See FREE-WEB-RESEARCH.md for exact evidence, limits and verification. Older sections below describe the superseded grounding implementation.

# Live verification correction
Owner-supplied key saved with Windows DPAPI; no key printed or archived. Live account checks:2.5Flash-Lite404 unavailable to new users;3.5Flash-Lite basic generation200;3.5Flash-Lite Google Search429 RESOURCE_EXHAUSTED. Default adapter now pins3.5Flash-Lite; quota errors explain Search entitlement. Prior2.5recommendation below is historical and superseded for this account. Live web research is BLOCKED by provider free-tier Search access, not a missing credential. No billing, model fallback, deployment or product UI changes. Owner should rotate the key shared in chat using the masked local helper.

# Malik Oil — API AI, charts and WhatsApp
Updated 6 November 2026 (Asia/Karachi).

## What is implemented
- Pinned Gemini Developer API `gemini-2.5-flash-lite` + Google Search research adapter. Owner-authenticated Worker POST `/v1/research`; loopback demo proxy uses the same adapter. No local Ollama required.
- Public commodity/market/language only: no private chat, phone numbers, ledger or audit is attached. Fresh API check timestamp, source links, grounding supports and Google Search suggestions. Search grounding missing => generated claims withheld. AI never creates/approves rates or triggers alerts.
- D1 migration `0002_research_budget.sql`: atomic 20 research attempts per UTC day, including provider failures, no automatic retry or paid fallback. This is an app safety cap, not a promise of zero billing on a paid Google project.
- Three language selections: English, Urdu script with RTL, Roman Urdu. Main navigation and research screens localized; some secondary forms, validation, technical notes and legacy text remain English. Preference saved locally. Samundri is the home/research default; user can choose a Punjab mandi. No GPS permission or fabricated distance ranking.
- Stock-style line chart: same commodity/mandi/grade/source only; observation timestamps, PKR/kg, accessible observation table. Minimum four observations. Browser 1D/1W/1M/All filters over latest 60 synthetic observations. Android history max500 actual approved observations. No invented OHLC candles, trades or volume. Browser data remain synthetic and separate from production.
- AI first version is bounded, on-demand sourced research; not a full autonomous multi-step Perplexity clone, not continuous monitoring. No API credentials or cloud runtime means not connected.

## Best free API choice for this use case
Recommendation: Gemini 2.5 Flash-Lite, conditional on access in your actual Google project. Official pricing currently lists free input/output and up to500 grounded requests/day shared with2.5Flash, subject to the account's model quotas. This is not a speed/quality benchmark. Newer model free inference does not imply free Search grounding. Direct Gemini deprecations page says2.5Flash-Lite is not deprecated; other Google/Firebase lifecycle pages conflict, so validate actual account access before enabling. No automatic upgrade/fallback is implemented.
- Pricing: https://ai.google.dev/gemini-api/docs/pricing
- Model lifecycle: https://ai.google.dev/gemini-api/docs/deprecations
- Grounding API/schema/display: https://ai.google.dev/gemini-api/docs/generate-content/google-search
- Unpaid data terms: https://ai.google.dev/gemini-api/terms (Google may use unpaid prompts/responses to improve products; public queries only)
- OpenRouter search incurs extra cost even with free models: https://openrouter.ai/docs/guides/features/plugins/web-search
- Groq Compound old endpoint no longer supported: https://console.groq.com/docs/deprecations ; do not copy outdated free Compound tutorials.

## Enable local API testing without deploying
1. Create a Gemini API key in Google AI Studio: https://aistudio.google.com/apikey . Use an unbilled free-tier project; do not enable billing. Check model access/quotas there. Do not send the key in chat.
2. In permanent source folder `.preview/research.local.json` create this PRIVATE ignored file with your real key (the marker below is documentation, not a working key):
   `{ "apiKey": "YOUR_REAL_KEY", "freeTierConfirmed": true }`
3. From project root run `powershell -File tools/start-preview.ps1` (or `pwsh -File ...`). Backend dependencies must be installed once via `npm ci` in backend. It restarts only the recorded matching preview process; preserves demo database. Key goes to the server environment, never HTML/APK.
4. Open http://127.0.0.1:8765/ and AI research, select language/commodity/mandi. No Firebase required for this synthetic local UI. Missing key => clear503; quota =>429; unavailable model/key =>502, no fallback. Sources must still be checked for actual observation date and matching quality/unit.
5. Cloud later: Firebase owner config + explicit deployment authorization still needed. Store `GEMINI_API_KEY` as Worker secret, `ENABLE_RESEARCH=true`, apply both migrations. Never put key in Android or commit `.dev.vars`. Cloud key is not configured by local preview setup. No deployment performed.

## WhatsApp: your actual situation
You use WhatsApp Business, are a member rather than admin, and want automatic reading of an existing group. That does not establish official Cloud API group entitlement. Meta Groups documentation was blocked by429 in this research, so exact current eligibility/participant limits are deliberately not claimed. No connection, QR session, history access or messages are active. Being made admin alone is not established as sufficient.
Official checks before future integration: Meta Business portfolio/WABA, number onboarding/coexistence and Groups eligibility, whether that existing group can be accessed, member/admin permission, webhook credentials and approved hosting. Do not migrate/delete your current WhatsApp account for this scaffold.
- Meta Groups docs: https://developers.facebook.com/documentation/business-messaging/whatsapp/groups
- Export guide: https://faq.whatsapp.com/1180414079177245
Working interim path: share selected rate text to installed Malik Oil APK or group menu > More > Export chat > Without media, then paste only intended explicit rate rows into import. Existing parser accepts `sarson | Samundri | standard | 8000 | 40kg | ISO timestamp with timezone`; it is not an arbitrary-chat AI parser. Every accepted row waits for review. Automatic personal/group ingestion remains pending entitlement and implementation, not silently replaced by a fake connection.

## Recommended useful next modules
Prioritize source age/quality and transport-adjusted comparison over speculative forecasts. AMIS/PITB describes135 Punjab markets, not guaranteed oilseed coverage, live ticks or a verified public API: https://www.pitb.gov.pk/agri_mrkting . Samundri/Faisalabad region is a research focus, not a claimed nearest-mandi distance calculation. Next: verified daily source adapters, yield/quality adjustments, inventory and purchase/sale ledger, then audited eligible messaging and24/7 cloud runtime. Free tiers have quotas, no uptime guarantee, and no unbounded real-time research.

## Verification — 6 November 2026
TypeScript check PASS; 11 Node tests PASS including real local D1 notification flow and3research adapter tests. Six preview tests PASS, seven SQLite alert tests PASS. Research budget permits20attempts and denies21st, new day resets. Dependency audit zero vulnerabilities after pinned dev dependency undici7.30.1 override. Browser JavaScript syntax PASS; HTTP HTML/script/state200, missing key503, cross-origin403, shared CLI missing key503 PASS. Android assemble+lint PASS (0errors,5warnings); signature/alignment PASS. No real key/provider call, no physical phone/emulator test, browser automation timed out (visual layouts/click interactions not certified). No deployment.

## Secure key-entry alternative
Run `powershell.exe -NoProfile -File tools/setup-research-key.ps1`. Paste key into the hidden prompt, confirm the unbilled/free project with YES. Windows DPAPI encrypts the credential to `.preview/gemini.credential.xml`, decryptable by the same Windows user on this computer. Startup prefers this over the legacy JSON file. The helper restarts preview and runs one public Samundri/sarson research check, recording only sanitized status in `.preview/key-setup-result.json`. Key is never printed or passed as a command argument. `-SelfTest` verifies an isolated encrypted round-trip without real credentials. Live completion still requires the owner to enter the actual key in the local prompt. No product UI changes.
