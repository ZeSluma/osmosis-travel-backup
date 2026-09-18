# Bundled hardware session and UI correction

Tested target build: 8144ded3609e0d28c4a331211123122589958852. Detailed ordered observations and sanitized artifacts are in TARGET_SESSION.md. Previous REPORT.md describes the earlier software-only checkpoint, not current hardware status.

| Check | Result and scope |
|---|---|
| Installation/schema6/history | PASS; existing references retained |
| Enumeration-loss correction | PASS for four observed members; snapshot remains INCOMPLETE |
| A normal transfer | PASS;103945077 bytes, pending0, CONFIRMED transfer integrity |
| B interruption | PASS;330769591 of1164588515 bytes, PARTIAL,pending1 |
| Shared process restart | PASS; destinations, journal and independent evidence retained |
| B retry safety | PASS; review required, no download/new allocation or truncation |
| Overall source verification | UNPROVEN; zero source-equivalence proofs, overall UNVERIFIED |
| Successful production resume | NOT_TESTED; safe refusal is not resume success |
| Persistent A/B UI distinction | FAIL on target; corrected in software, target retest pending |

The two original references remain38447651 and3071380142 bytes,pending0. No camera original modified; no test artifact removed. Neither this session nor UI labels authorize completion/cleanup.

## Evidence-driven correction

Read-only display projection derives states from exact current snapshot identity, persisted replica and published journal. Filename alone cannot bind a badge. Stale-session callbacks cannot repaint a new grid. Partial B displays28% and review required; transferred A remains explicitly unconfirmed. Missing/ambiguous bindings fail closed. No schema, transfer policy, dependencies or Gradle changes.

Six targeted unit tests PASS. Synthetic German grid-cell rendering differentiates A/B, preserves selection/filename space and checks publication-gap review; emulator suite PASS7.06s including installs. Launcher security instrumentation PASS3.05s. No real media loaded for these tests.

Final isolated LF checkpoint command: `./gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest compileReleaseKotlin lintDebug --continue --console=plain`. Duration56s, exit1 solely lint.382 tests,0 failures/errors/skips; debug/test builds and release compilation PASS. Lint retains5 errors79 warnings2 hints, unchanged from prior checkpoint; no baseline configuration suppression.

UI APK SHA256: `fd703d0eee44423f4ef998adec4e836e87f399a8a645cee0c6332d34962a45e9`.
Before attempted installation, read-only live audit again confirmed two settled attempts (A PUBLISHED,B PARTIAL), one integrity receipt, zero source proofs and unchanged references. `adb -d install -r <isolated-LF>/app/build/outputs/apk/debug/app-debug.apk` returned exit1, `adb.exe: no devices found`; launch was not executed. The correction is NOT installed on the S25. No repeated detection/scan loop.

## Remaining evidence boundary

G3 remains NOT_TESTED; implementation authorized, release false. ETag presence in200 responses does not establish immutable version semantics, and no initial B validator value was retained. A later header cannot retroactively prove B's original source continuity. Never append to B on the strength of filename, expected size, same path or current ETag alone. Future successful resume requires independently justified source-version continuity plus retained-prefix verification and an implemented, tested append path; current adapter deliberately blocks it.

Human stop: target disconnected. Next bounded check is install this tested build, preserve metadata/ledger, then observe A/B status labels after one normal connection. Hypothesis: prior UI failure was absent ledger-derived rendering; different labels distinguish corrected integration from a callback/identity-binding failure and decide whether more UI engineering is required. No download/retry, camera-state micro-test or repeated identical rescan is part of that check. No dependent gate passes until remaining G3 criteria are evidenced.

## 2026-09-18 prototype checkpoint

The current prototype checkpoint is documented in `PROTOTYPE_SPRINT.md`. Full unit/build and synthetic emulator coverage passed after the cancellation-after-blocking-read fix. This preserves the strict fail-closed publication boundary: receipt-before-publication crash recovery remains review-required because a durable complete-file digest is not yet stored for a new-process readback. GATE-3 remains `NOT_TESTED`; no result in this software checkpoint constitutes target-hardware proof.

## 2026-09-18 final MVP installation boundary

The final requested APK (`1C087E…B860`) cannot be installed over the currently installed debug package because Android reports a signing mismatch. The planned `-r` install was refused before any application lifecycle or media operation. An uninstall would erase the private database required for the migration/history-preservation check, so it is not authorized and was not attempted. The consolidated target session is **BLOCKED** before its remaining checks; no existing result is changed. See `TARGET_SESSION.md`.

## 2026-09-18 signer-compatible final-build validation

| Check | Result |
|---|---|
| Exact requested APK | BLOCKED for in-place update: signer mismatch; no uninstall attempted |
| Signer-compatible replacement | PASS: same package/version, matching existing certificate, source tree matches `6c02889`, `adb install -r` success |
| Private ledger migration | PASS: ledger persisted and SQLite header migrated from schema6 to7 |
| Protected references | PASS: both filename/size references unchanged |
| Final UI persistent-state distinction | PASS: four visible assets show distinct partial, transfer-unconfirmed and local-copy-unconfirmed states without selection or transfer |
| Historical A transfer / B interruption / restart / safe retry refusal | PASS in their existing scoped evidence; deliberately not repeated |
| Pocket immutable source-version contract | INCONCLUSIVE: ETag presence alone is not trusted continuity evidence |
| Successful production append/resume | NOT_TESTED; remains blocked by the source-continuity contract |

No app data, local media, camera original, source/configuration, main branch or release state changed.
