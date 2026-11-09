# Tasks: Local Workflow Test Preview

- [X] T001 Inspect installed runtime/device and record the native blocker in docs/LOCAL-PREVIEW.md.
- [X] T002 Implement isolated loopback API with shared SQL rules in tools/preview/server.py.
- [X] T003 Add rates/review/alerts/margin controls in tools/preview/index.html.
- [X] T004 Pass isolated and HTTP checks in tools/preview/test_preview.py; check browser script syntax.
- [X] T005 Start preview and queue native browser panel; document link/restart in docs/LOCAL-PREVIEW.md.

Dependencies: T001 -> T002 -> T003 -> T004 -> T005. No deployment or production authentication changes.
