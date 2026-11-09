## Punjab feed and optional title discovery update
Configurable nearby priority and a sarson keyword view are added; original messages and unknown cities are retained. When group name is unknown, an explicit two-minute helper may retain up to 20 Business group titles only, with message capture paused. Owner must select the exact group and separately enable collection. This optional discovery supersedes the earlier no-group-title-scanning statement below; no unsolicited discovery is enabled. See DAILY-REPORTS.md.

# Free phone notification collector
## Scope
This is an optional Android 9+ notification reader for messages already delivered to the owner's WhatsApp Business app. It is not an official WhatsApp group API, history sync, QR session, cloud collector or guarantee of uninterrupted service. It does not send messages or upload captured content. No billing or deployment is required for local collection.

## Setup on the actual phone
1. Update-install the latest Malik Oil APK (same package/signing key; uninstalling would erase app data).
2. Open WhatsApp group notifications · free, from setup/home or Plugins.
3. Confirm the phone's PIN/password/pattern with Android. A device screen lock is required. The private inbox is locked again when leaving the app.
4. Read the disclosure. Enter the exact, case-sensitive unique group name, check local-collection consent, then Start selected-group collection.
5. Open Android notification access and manually grant access to Malik Oil. Android access can expose all app notifications; our code filters the WhatsApp Business package, explicit group marker and chosen group title before retaining content. Honour OS restrictions; the app does not bypass them.
6. Return, unlock and check status. Reconnect listener can request a system rebind. When the next supported group notification arrives, Refresh inbox. Stop collection disables persistence; Android permission can also be revoked in system settings.

## Privacy and provenance
- Default disabled. No automatic grant, scanning of all group names, contacts collection, replies, clipboard reading or remote upload.
- WhatsApp Business package only. Normal WhatsApp, other apps, private conversations and summary notifications are rejected. Only supported MessagingStyle group notifications with a matching title are handled.
- Current owner/device actor binding is checked; local device-owner mode uses Android's device-credential confirmation. Config and inbox use existing UID-bound Android Keystore AES-GCM cache. Auth logout/denial cleanup removes config/inbox; the listener then has no enabled configuration.
- Messages before explicit start, empty/oversize text and future timestamps are rejected. Deduplication uses group, message timestamp and text; it is not an official WhatsApp message ID. Edited messages can appear separately.
- Latest 200 matching messages retained in encrypted local storage, 20 displayed per page. No sender names are separately retained. Notification time is not the mandi observation date. Copy/check/edit actions do not approve a rate.
- Unique group titles are required: notifications cannot reliably distinguish two groups sharing a name. Hidden/suppressed/muted/unsupported notifications, OS process restrictions, no network or missing message content can cause gaps. Old chat history, media OCR and missing notifications are not recovered.
- Browser preview cannot access Android WhatsApp notifications. Laptop/phone synchronization is not configured.

## Verification
Capture policy tests cover package/group filters, privacy exclusions, timestamp validation, size bounds, Unicode names, duplicate updates and retention. APK build/lint and JVM results are recorded in APK-BUILD.md. Device unlock, permission-grant/binding, actual WhatsApp notification format, encrypted phone persistence and sustained delivery require a real device. No actual group has been connected during development.

Primary references:
https://developer.android.com/reference/android/service/notification/NotificationListenerService
https://developer.android.com/reference/android/app/Notification.MessagingStyle
https://developer.android.com/reference/android/app/KeyguardManager
