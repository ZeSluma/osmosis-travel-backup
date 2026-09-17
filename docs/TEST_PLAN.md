# Test Plan

## Rebased acceptance matrix

The authoritative proposed test additions are [TEST_MATRIX](design/TEST_MATRIX.md), tied to R-016..R-036 and the explicit [gate dependency proposal](plans/REVISED_GATE_DEPENDENCIES.md). Use fault-injectable state-machine boundaries, not tests that merely reproduce UI code. Every new case remains NOT_TESTED until executed and persisted. Current hardware session is PAUSED_BY_USER; do not run ADB, device setup, recording, power-setting changes or new transfers until explicit return.

Source-level existence of reconnect/resume/MediaStore lookup is not a behavioral PASS. Keep observed foreground loss, Android background loss, sleep hypothesis, auto-reconnect, auto-resume and restart UI recognition distinct. No destructive source/format tests on current camera media. Source identity/recording-member completeness and notification/ledger truth are acceptance conditions, not optional late enhancements.

## Philosophy

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
