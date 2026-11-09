# Implementation Plan: Local Workflow Test Preview

**Date**: 2026-11-05 | **Spec**: spec.md

## Summary

Use a standard-library Python HTTP server plus a small browser page, isolated from production.
Reuse backend/migrations/0001_init.sql in a separate .preview/synthetic.sqlite3 database. No
Firebase options or credentials are read. This is not native Android emulation.

## Technical Context

Python 3 sqlite3/http.server/decimal; vanilla browser DOM and fetch. Bind 127.0.0.1:8765 only.
Use exact Host and same-Origin checks, JSON/64KB limits and textContent for user-provided labels.
Keep .preview/ ignored. Production Android/backend code and APK remain unchanged.

## Constitution Check

PASS: owner production auth preserved; all prices labeled synthetic; no remote changes, live
notifications or credentials; source and tests stay in the existing permanent project.

## Project Structure

- tools/preview/server.py: isolated local API and existing SQL trigger execution.
- tools/preview/index.html: rates/review/alerts/margin test controls.
- tools/preview/test_preview.py: five isolated logic checks.
- docs/LOCAL-PREVIEW.md: limitations, native blocker and restart/test instructions.

## Verification

Five tests cover 3% both directions/duplicate approval, cumulative crossing, market case,
invalid amounts and margin math. Local HTTP checks cover service state, margin result and
cross-origin rejection. Browser script syntax passes. Windows browser capture failed and a
subsequent input guard detected user activity, so no visual/UI automation pass is claimed.
The native preview panel was queued through the app tool; a clickable URL is also provided.
