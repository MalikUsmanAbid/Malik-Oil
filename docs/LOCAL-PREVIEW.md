# Malik Oil local test preview

Open **http://127.0.0.1:8765/** on this laptop.

This is a synthetic browser workflow preview, not the Android APK. It uses the project's existing
SQL alert trigger inside a separate local database. No Firebase, live mandi feed, WhatsApp access
or FCM notification connection is active. Use sample figures here.

## Try it

1. Rates: select +3% candidate, or enter a sample amount/unit/market.
2. Review: approve the sample rate.
3. Alerts: inspect rise/fall history. Repeat the last approval to check duplicate suppression.
4. Business analysis: enter costs and calculate landed cost, break-even and gross margin.

Quick +/-3% controls round outward to the nearest paisa so the minimum 3% threshold is reached.

## Restart if the preview is closed

From the permanent project root:

```powershell
python tools/preview/server.py --port 8765
```

Keep that terminal running; Ctrl+C stops a foreground preview. The current session was started as
a hidden local Python process, with logs/PID in ignored .preview/. Closing the page does not stop
that process. No cloud hosting/deployment is involved.

## Verification and native limitation

Five isolated tests passed. HTTP service/margin checks and foreign-Origin rejection passed;
browser script syntax passed. The native browser-panel request was queued. A Windows screenshot
request timed out, and browser recovery later detected user input, so automated visual inspection
and clicking through the live browser are not claimed.

The actual APK was not run: no adb-connected Android device was available; this AMD laptop
reported VirtualizationFirmwareEnabled=false and HypervisorPresent=false. Suitable device/runtime
setup is needed for native testing. This preview does not establish Android cache, permissions,
background behavior, Firebase login or FCM delivery success.

Test command: `python tools/preview/test_preview.py`.
