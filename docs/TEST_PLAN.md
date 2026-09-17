# Test Plan

Current execution policy (2026-09-17): GATE-2 implementation is authorized; AGENTS.md now permits continuous progression through sufficiently defined non-destructive gates after dependency/evidence checks. Earlier statements requiring a new permission solely at a gate boundary are historical and superseded. Hardware remains paused; no main mutation, merge, release or real deletion is authorized. Current implementation/evidence status is PROJECT_STATE.yaml and the active GATE-2 plan.

## Security closure test obligations

[Closure checklist and proof ownership](evidence/GATE-1/2026-09-17_security-closure/REPORT.md) supplements TEST_MATRIX. SC09 and SC10 have scoped PASS evidence in the B1/B2 report and ADR0008: eight policy tests, empty-emulator launcher tests, debug/release manifests/compilation, resolved Kotlin2.4.20, 265 baseline tests plus eight security tests, and no new lint errors. SC01-SC08 remain NOT_TESTED; physical hardware remains paused. New processor compatibility is reviewed if a processor is later introduced.

| ID | Required assertion / ownership |
|---|---|
| SC01 | G2: synthetic credential migration interrupted before/after durable encrypted write/plaintext retirement remains recoverable; per-camera isolation and key invalidation require honest re-pair, never secret logging |
| SC02 | G2: inspect app backups/D2D rules and test no credential/ledger/WAL/export leakage; restored state cannot carry verification/deletion authority; actual S25 behavior separately authorized |
| SC03 | G7/N: min29 and target36 exact-IP XML plus HTTP/native preview/TCP/UDP adapters reject wrong Network/epoch/host/port/redirect; cloud always TLS and separate routing |
| SC04 | G7/X: visible service start, notification denial, screen off, OS kill, force-stop/Task Manager and multi-GB tests preserve one owner/partial truth, respect stop, no promised automatic resurrection |
| SC05 | G7/G4/GD: synthetic sensitive canaries cannot reach normal/verbose/Logcat/export; retention bounds, expiry, disk-full and logging-disabled backup preserve correct truth |
| SC06 | G7/G4/GD: GPS OFF backup with location denied; separate opt-in precise/approximate/revoked GPS, foreground-start/background continuation and BLE arbitration |
| SC07 | G7/N05: Android16 restriction simulation and target37 grant/deny/revoke/upgrade matrix; restore settings; no implicit hardware resume |
| SC08 | G2/G3: all supported schema migrations and crash/ENOSPC/corruption paths preserve identity/path/member/replica truth; no destructive fallback or false VERIFIED |
| SC09 | G1/B1: authorized hardening then source plus debug/release merged-manifest audit and external intent/replay/oversize tests; launcher cannot expose test credentials, pairing override or cleanup authority; legitimate known-camera shortcut still works |
| SC10 | G1/B2: authorized fixed Kotlin candidate from trusted caches, clean build/unit/lint comparison preserving original debt, K2/JVM21 and future processor compatibility; no suppressions to fabricate PASS |

## Rebased acceptance matrix

The authoritative proposed test additions are [TEST_MATRIX](design/TEST_MATRIX.md), tied to R-016..R-041 and the explicit [accepted gate dependencies](plans/REVISED_GATE_DEPENDENCIES.md). Use fault-injectable state-machine boundaries, not tests that merely reproduce UI code. Every new case remains NOT_TESTED until executed and persisted. Current hardware session is PAUSED_BY_USER; do not run ADB, device setup, recording, power-setting changes or new transfers until explicit return.

Source-level existence of reconnect/resume/MediaStore lookup is not a behavioral PASS. Keep observed foreground loss, Android background loss, sleep hypothesis, auto-reconnect, auto-resume and restart UI recognition distinct. No destructive source/format tests on current camera media. Source identity/recording-member completeness and notification/ledger truth are acceptance conditions, not optional late enhancements.

## Philosophy

ADR0005 reconciliation adds AS01-AS03 (classification and independent redundancy), CL01-CL12 (future destructive snapshot gate) and GD01-GD05 (GPS/verbose independence, privacy and session diagnosis). All are NOT_TESTED. Fake destructive tests precede any separately authorized S25/Pocket HIL with disposable new recordings; no current media is a test deletion target. GATE-1 entry readiness is a documentation audit, not execution or PASS of these cases.

R-037 adds CD01-CD08 in TEST_MATRIX: automatic same/different-day folders, delayed sync, path-stable restart/resume, required companions, midnight/travel/DST, uncertain/conflicting timestamps and later SSD/cloud roots. Hardware cases require explicit resume and remain NOT_TESTED. [Capture-day policy](design/CAPTURE_DAY_ORGANIZATION.md) defines exact fallback oracles and clock-sync confounders.

Testing is evidence for gate closure, not ceremony.

## GATE 0 baseline

Run on unchanged baseline:

- Gradle wrapper validation
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`
- lint/static-analysis task(s) that exist in the project
- dependency/secret/security baseline
- inspect CI parity

If an upstream lint/static task already fails, record the exact baseline failure instead of silently fixing it during GATE 0.

## Target hardware

Primary:

- Samsung Galaxy S25 Ultra
- DJI Osmo Pocket 4P

Later:

- Hagibis/P310 NVMe SSD
- OneDrive

## Mandatory failure matrix

At appropriate gates test:

- camera Wi-Fi drop
- BLE loss
- HTTP interruption
- HTTP range resume
- HTTP 404
- HTTP 500
- process kill
- screen off
- Android kill
- low internal storage
- SSD removal
- SSD reconnect
- cloud unavailable
- duplicate execution
- pre-existing destination file
- large single file
- large batch
- restart between pipeline steps

## Ledger tests

When the ledger is implemented, unit-test state transitions such as:

- DISCOVERED → TRANSFERRING
- TRANSFERRING → COMPLETE
- COMPLETE → VERIFIED
- TRANSFERRING → INTERRUPTED/FAILED
- interrupted → resumable
- VERIFIED → no unnecessary re-transfer

Invalid transitions must be rejected or repaired safely.

## Integrity tests

Verify:

- partial file never becomes VERIFIED
- wrong final size never becomes VERIFIED
- checksum policy where source/transport supports meaningful checksum validation
- duplicate destination behavior
- interrupted writes
- restart recovery

## Evidence layout

Persist gate evidence under:

`docs/evidence/GATE-N/YYYY-MM-DD_<short-description>/`

Each run should include enough information to reproduce:

- commit
- branch
- app/build version
- device
- Android version
- test commands
- results
- relevant sanitized logs
- conclusion

## GATE-2 executed software scope

[Evidence](evidence/GATE-2/2026-09-17_ledger/REPORT.md) records313 LF unit tests, real Room emulator migration/constraints/rollback/full/corruption/restart checks and synthetic Keystore SC01/SC02 cases. This does not mark OEM D2D, physical S25/Pocket integration or SC03-SC08 broader later-gate behavior PASS. G2 status remains BLOCKED for mandatory HIL; read HARDWARE_PENDING before resuming.
