# Official-source research · checked 18 October 2026

## Proposed starting infrastructure

| Component | No-cost allowance / behavior | Practical implication |
|---|---|---|
| Cloudflare Workers Free | 100,000 requests/day; 10 ms CPU/invocation; 128 MB; 50 subrequests/invocation; five Cron Triggers/account | Small authenticated API plus five-minute outbox tick; no browser scraping or local-model inference inside Worker. Quota is shared with other account workers. |
| D1 Free | 5 million rows read/day, 100,000 rows written/day, 5 GB total storage | Indexes/triggers/audit add writes. Exceeding daily quota blocks queries until reset; storage saturation blocks writes. Monitor actual row metrics. |
| Firebase Spark | FCM no cost; most Auth options no cost; no payment method for Spark | Use email/password, not SMS. No Firebase Cloud Functions or Cloud Run assumed. |
| Workers AI optional | 10,000 neurons/day free; exhausted free quota fails; some models require paid access | Disabled by default; select a current free-eligible model explicitly. No default obsolete model ID. Fall back to deterministic summary. |
| APK distribution | Build and sideload privately | Preserve signing key. Installation is manual. No Play account needed for initial APK. |
| Google Play later | US$25 one-time developer registration; personal-account testing requirements apply | Play Store distribution is not entirely free. Check current account, identity, testing and target API requirements before release. |

References: [Workers limits](https://developers.cloudflare.com/workers/platform/limits/), [D1 pricing and quota failure behavior](https://developers.cloudflare.com/d1/platform/pricing/), [Firebase pricing](https://firebase.google.com/pricing), [Workers AI pricing](https://developers.cloudflare.com/workers-ai/platform/pricing/), [Play Console setup](https://support.google.com/googleplay/android-developer/answer/6112435).

Free allocation resets at midnight UTC where specified (05:00 Pakistan time). No-cost capacity is not a service-level guarantee. Keep the Free/Spark plans; do not attach paid inference or turn on usage billing silently. This design avoids assuming a free always-running VM. Cron starts work on demand; it is not a resident process.

Sizing example (planning estimate, not a provider guarantee): six items × 20 markets × four observations/day = 480 quotes/day, 14,400/month. Even roughly 20 row writes/quote is 9,600/day, but indexed scans, retries and other account workloads must be measured. Polling each quote every five minutes would be 34,560 quotes/day before trigger/index overhead and is unsuitable for this initial free design. Poll only when sources update; publish actual coverage/freshness.

## Punjab market sources

[PITB AMIS overview](https://www.pitb.gov.pk/agri_mrkting) describes dissemination from 135 Punjab markets. The official [AMIS price page example](https://www.amis.pk/ViewPrices.aspx?commodityId=57&searchType=0) describes daily/selected-date auction or wholesale prices, with Rs/100kg unless otherwise specified. The example is rice, not an oilseed feed or proof of coverage for all six requested commodities.

The official AMIS/PITB page bodies could not be fetched reliably during this run; search indexing exposed the above descriptions. No stable public JSON API, permission for automated reuse, live freshness guarantee, oilseed IDs, or complete current market coverage was established. Therefore no scraper or invented mapping is included. Register an adapter only after checking access terms/robots, actual commodity mapping, market and grade fields, units, source dates and sample snapshots. If AMIS is unavailable or a commodity missing, display unavailable/stale and permit explicit owner observations; do not label them official rates.

## WhatsApp

[WhatsApp export help](https://faq.whatsapp.com/1180414079177245) is the owner-mediated import path. MVP accepts pasted/shared text with explicit structured rate rows, not every locale's complete WhatsApp export syntax. Arbitrary lines are counted as ignored; AI never silently converts a private chat into approved prices. Binary ZIP/media and exported TXT attachments need a later Android document-picker/parser module.

Meta's [official API collection](https://www.postman.com/meta/whatsapp-business-platform/overview) and [webhook payload reference](https://www.postman.com/meta/whatsapp-business-platform/folder/tduohwq/webhook-payload-reference) describe Business Platform messaging and webhook subscriptions. The [current Groups documentation](https://developers.facebook.com/docs/whatsapp/cloud-api/groups) and [business messaging documentation](https://developers.facebook.com/documentation/business-messaging/whatsapp/overview) returned rate-limit/fetch errors. Exact current Groups/coexistence eligibility was not verified. Do not infer that an API business number can read existing personal/group histories. Existing private groups remain manual export/import; supported business-number inbound messages can be integrated only after official account eligibility, permissions and sender consent are verified. No WhatsApp Web session scraping, accessibility capture or notification listener is included. Business messaging may incur charges; it is excluded from the guaranteed no-cost path.

## Authentication, notifications and AI

[Firebase token verification](https://firebase.google.com/docs/auth/admin/verify-id-tokens) supports third-party JWT validation for custom backends; verify signature, RS256, issuer, audience, expiry, subject and auth time. Scaffold uses Google securetoken JWKS and a single owner UID. Firebase verification alone does not automatically enforce token revocation; this scaffold additionally uses operator-managed `AUTH_NOT_BEFORE`.

[FCM trusted server requirements](https://firebase.google.com/docs/cloud-messaging/server-environment) require server-held credentials, registration tokens and retries. [Android FCM setup](https://firebase.google.com/docs/cloud-messaging/android/get-started) covers device setup/notification permission. FCM HTTP v1 is implemented; provider acceptance is recorded as `sent`, not proof of display/read. Android permission, offline state, force stop, device battery restrictions and lack of Google Play services can affect delivery.

[Android Gradle Plugin 8.9 compatibility](https://developer.android.com/build/releases/agp-8-9-0-release-notes) specifies Gradle 8.11.1, JDK 17 and maximum API 35. These are deliberate scaffold build pins, not a claim of newest versions or future Play eligibility. Wrapper files come from [Gradle v8.11.1](https://github.com/gradle/gradle/tree/v8.11.1); distribution checksum is pinned.

[Ollama's official API source](https://github.com/ollama/ollama/blob/main/docs/api.md) documents local chat calls. The optional script calls loopback only; install your own compatible model and review its license/hardware needs. Laptop-local inference stops when the laptop is off; the cloud API, deterministic alert engine and configured cloud AI remain independent. No OpenAI API, paid model, API key or ChatGPT subscription entitlement is assumed.
