# Active plan — GATE-2 ledger and missing-asset planner

Status: BLOCKED pending physical S25/Pocket evidence; software foundation verified (313 LF tests, builds, scoped emulator/security checks). See [report](../../evidence/GATE-2/2026-09-17_ledger/REPORT.md). Implementation authorized by the 2026-09-17 GATE-2 request. Subsequent global continuous execution policy supersedes the request's gate-boundary reauthorization rule. Gate dependencies and hardware evidence are not waived. GATE-0/GATE-1 PASS; parent a58d858ecfd404579960d9a32960e151071f9fd3; baseline main 2fcdbc97e6dbefc875d425368be67cf32b50bb06.

Implement and verify Room schema/migrations, private/no-backup storage, transactional fenced single writer, composite candidate identity with explicit ambiguity, six asset classes, proven recording relationships, immutable capture-day/path reservations, incomplete/complete enumeration snapshots and deterministic missing/new planning. No name/size/UI-based verification. No source deletion, transfer-engine rewrite or background service in GATE-2.

Test all cases from the user request: inventory/type combinations, changed/reused identity, repeated scans, removed/new assets, unknown/excluded assets, capture-time priority/timezones/conflicts, parent members, missing companions, rollback/FKs, migrations, restart/process persistence and stale/concurrent requests. Validate debug/release compilation, authoritative LF unit regression, B1 intent checks, lint delta, dependency resolution and privacy/scope.

Persist original failures and corrections under docs/evidence/GATE-2/2026-09-17_ledger/. Use coherent commits, push only gate-2/persistent-ledger-sync-planner and independently verify remote SHA. Main and upstream remain unchanged. Handoff stays local/untracked.

Physical S25/Pocket testing explicitly resumed on 2026-09-17. Verified APK installed and read-only preconnection audit complete; inventory/idempotence/restart/new-media/time/type/group observations are pending in the [hardware report](../../evidence/GATE-2/2026-09-17_hardware/REPORT.md). GATE-2 cannot PASS without mandatory evidence. Real camera deletion, merge and release remain unauthorized.
