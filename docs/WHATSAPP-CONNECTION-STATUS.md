## Updated local option
An opt-in Android notification collector is now implemented and built, not yet activated on a real phone. It reads supported new notifications from one selected WhatsApp Business group, with owner-device unlock and encrypted local retention. It is not the official Groups API or guaranteed 24/7 history synchronization. See WHATSAPP-FREE-PHONE-CONNECTOR.md.

# WhatsApp connection status — 7 November 2026
Owner uses WhatsApp Business app, is a group member rather than administrator, and wants an always-connected collector. No group connection is configured. Existing share/export/row-checker import is manual and review-first.

Verified primary references: Meta official WhatsApp Business Platform Postman workspace and Meta-hosted Cloud API receiving-messages documentation describe incoming webhook integration. Groups API and Business-app coexistence documentation both returned HTTP 429 during review; current account eligibility and pre-existing-group access therefore remain unverified. Do not imply that Business-app membership alone supplies API group access, that the Groups API does not exist, or that a QR login creates an official permanent group connector.

A supported 24/7 design, if the account and target conversation are eligible, requires a Meta business/app/number setup, authorized events, a public HTTPS webhook, signature verification, durable event-id deduplication/queueing, reconnection/health monitoring and owner review of extracted rate observations. No token can guarantee perpetual access. Public webhook hosting is deployment; it is explicitly on hold. Do not enable billing or send messages.

Next independent action: owner can securely save a free Tavily key using Setup-Tavily.cmd. Setup auto-tests the exact Samundri sarson question in Deeper mode; at most 3 basic search credits. Encryption self-test and test-script syntax check passed, but no live Tavily request can be tested until owner key exists. Official mandi search found historical reports, not a verified current Samundri quote.

References:
https://www.postman.com/meta/whatsapp-business-platform/overview
https://whatsapp.github.io/WhatsApp-Nodejs-SDK/receivingMessages/
https://developers.facebook.com/documentation/business-messaging/whatsapp/groups
https://developers.facebook.com/documentation/business-messaging/whatsapp/embedded-signup/onboarding-business-app-users
