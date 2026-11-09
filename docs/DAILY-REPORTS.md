# Daily app reports (18:00 Pakistan time)
The owner chose in-app Excel + summary at 6 pm, not a Codex chat automation. The Android app now has a private report screen, local owner-reviewed rate book, manual Generate now, saved summaries, XLSX export and an optional daily local schedule. No billing, cloud hosting or AI call is needed to generate reports.

## Setup and daily use
Update-install the APK. Open Daily Excel reports, confirm Android device credentials, choose sarson for initial testing (or another supported commodity/all), check Enable daily local report, then Save daily schedule. Android notification permission is optional for a generic report-ready notification; the file can still be opened from the app. Scheduling is inexact and battery/force-stop/power restrictions can delay or prevent delivery. The target is 18:00 Asia/Karachi, not an exact alarm guarantee. Opening the app after 18:00 can trigger a missing-current-day catch-up report. No exact-alarm permission or OS restriction bypass is used. A failed catch-up generation retries no more often than every 15 minutes.

A newly installed app has no reviewed rates. Generate now then honestly reports no numeric observations; it does not invent a graph. Review a captured group message into explicit rows, or enter rows manually: commodity | market | grade | PKR amount | kg/40kg/100kg | actual ISO observation timestamp with timezone. Confirm the original source, market, grade, unit and observation date before Approve rows. Message/capture time is not the rate observation date. Local report approval does not publish backend rates or trigger the 3% alert engine.

## Excel output
- Summary: report day, generated time, reviewed/comparable counts, up to eight quoted rate/source/change lines and unreviewed message count. Summary follows English/Urdu/Roman Urdu selection.
- Rates: latest reviewed observations for the day, original amount/unit, PKR per 40kg, previous available matching observation/date and percentage change, source and message fingerprint.
- Trends: 30-day calendar with missing observations as gaps, and a native Excel line chart for at most four separate commodity/market/grade/source streams. It is historical owner-reviewed data, not a forecast or all-Punjab live coverage guarantee.
- Unreviewed: separately labeled group messages for the capture day; long bodies split into numbered parts. Messages are not a count of verified market quotes.

No rate is carried forward into today when there is no reviewed observation. Comparison uses the previous available observation from the same stream, not an assumed yesterday price. Source text is stored as literal inline strings; no macros or external links are embedded. Report is a snapshot; regenerate in the app after inputs change. The on-device exporter uses portable Java ZIP/OpenXML so it runs offline on Android without a server or paid SDK. Independent workbook inspection/rendering uses the bundled spreadsheet tooling.

Latest seven report files remain encrypted under Android Keystore, bound to the current local/Firebase actor. Exports are plaintext at the explicitly selected document-picker destination. Up to 2,000 locally reviewed rows are retained; source keys and observation dates remain attached. Normal restart retains local data; existing sign-out/access-denial cleanup clears it. Phone/laptop cloud synchronization is not configured.

## Punjab feed and unknown group name
Nearby priority defaults to Samundri, Faisalabad, Gojra, Toba Tek Singh, Tandlianwala, Jaranwala and Dijkot; it is a configurable preference order, not a measured distance. Text aliases tag city mentions only, not the verified location of each quoted price. Unrecognized cities/messages are not deleted. Sarson testing view is a keyword filter; it is not autonomous commodity/rate verification.

For a group whose exact name is unknown (possibly Ramzan), explicitly opt into two-minute title discovery. Message capture pauses; only up to 20 WhatsApp Business group titles are retained encrypted, with no message-body retention. Refresh after a supported group notification, select the correct exact title, then separately consent/start message capture. Discovery expires by time; no fuzzy matching or assumed group activation. Clear inbox also clears discovered names.

## Verification limits
No actual group connection or phone schedule is activated during development. A physical Android device is required to verify credential unlock, notification access/binding/WhatsApp format, alarm delivery, phone Keystore persistence and document export. No connected device was available at the prior check. Native compiler/JVM tests and workbook inspection do not establish those device behaviors.

Android scheduling reference:
https://developer.android.com/develop/background-work/services/alarms

## Browser preview
Run tools/start-preview.ps1 and open http://127.0.0.1:8765/#reports. Daily reports has Load reviewed SAMPLE rates, Generate today, saved summaries, 30-day matching-stream history, exact observation table and Download Excel. Explicit reviewed rows can also be entered. Windows DPAPI protects a separate .preview/daily-reports.bin; it is not synchronized with the Android device, research sessions or synthetic alert database. Reports retain at most seven daily snapshots. Browser workbook export is a computed snapshot with numeric cells and native Excel chart, not the Android exporter or a background phone job.

The WhatsApp panel tests exact selected title, enabled/disabled capture, deduplication and nearby priority using SAMPLE text only. No WhatsApp/phone API or old-history access is present in the browser. The 18:00 checkbox remembers a simulated preference; it does not schedule a Windows/Codex task. Enable the actual local daily alarm separately in Android. A local same-account preview is not a remotely hosted owner login. No API calls or billing/deployment are required for this panel.
