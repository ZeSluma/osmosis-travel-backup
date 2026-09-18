# Autonomous backup MVP — hardware-session evidence

Status: **IN PROGRESS; NO PRODUCT HARDWARE RESULT YET**.

## Current observation

The user reports that the intended S25 Ultra currently exposes the SSD through the intended
dongle/hub and that existing SSD data is browseable. This is recorded only as a user-reported
Android storage observation. It does **not** establish an Osmosis SAF grant, destination identity,
write/readback capability, replica verification, reconnect behavior, or redundancy.

## Agent observation

The available ADB transport currently lists only the isolated API-36 emulator. No physical S25
transport is visible to the validation environment, so no device/app/SSD state has been inspected
and no SSD content has been created, changed, or enumerated.

## Next boundary

Obtain non-destructive access to the physical S25, retain the currently connected SSD, then use an
empty dedicated Osmosis test directory to establish the configured SAF destination before any
disconnect/reconnect action.

## Scope update — SSD deferred

The user reports that the intended dongle/hub currently provides no usable SSD path with either
the S25 or PC. `MVP-SSD-BATCH` is therefore deferred as an external hardware-topology blocker.
This is not evidence of an Osmosis, Android SAF, SSD, or specific-dongle defect. Camera/S25
validation remains active; `BACKUP_REDUNDANCY_COMPLETE` and `SAFE_TO_CLEAR_CAMERA` remain
unpromoted.

## Pocket → S25 observation, build identity unconfirmed

The user created one new approximately 151 MB Pocket recording. In the currently installed,
unidentified Osmosis build, automatic connection was not observed. One tap on the pre-existing
known-camera entry connected without a rescan; five videos were enumerated and the new recording
appeared. It remained not downloaded and no automatic transfer was observed.

These establish only **known-camera reuse, real post-connect inventory, and new-asset discovery**
for the installed build. They do not establish automatic connection, automatic planning/execution,
or a failure of the current repository state because the installed package/version/signing identity
has not been accessible through ADB.

Static analysis of the current repository found and corrected two autonomous-path defects: the
initial UI projection could query before asynchronous durable plan creation completed, and a fresh
user launcher open could remain blocked behind a previously persisted explicit stop. The correction
re-triggers projection only after plan persistence, uses a fresh epoch only for a non-recreation
launcher start, and accepts case-insensitive saved/live MAC matching. Focused JVM test checkpoint:
39 tests, zero failures/errors. The prepared debug APK is
`app/build/outputs/apk/debug/app-debug.apk`, SHA-256
`36A30E81E77D315A05101F4F098E4D3FD67711874FE20823D16289D9208B4C3E`.

At the time of this observation, build attribution was not possible because the S25 had not yet
been visible through ADB. The subsequent section records the now-completed non-destructive
build-identification and in-place update.

## S25 build verification and in-place update

The physical S25 became ADB-visible as `SM_S938B`. The installed Osmosis package was
`versionCode=29`, `versionName=1.4.4`, last updated at 10:42, with APK SHA-256
`8E82875415F53B10713F39A3310CA6A753B69C2CCC2AAE81CC10946285A4EF3A`.
It was not the current locally assembled artifact. Both installed and prepared APKs use the same
Android Debug signing certificate SHA-256
`9c51946929452911a8554b4f83a24bcbf71172af60d8991488c3d2bc2ede35bc`.

The current artifact SHA-256
`36A30E81E77D315A05101F4F098E4D3FD67711874FE20823D16289D9208B4C3E` was installed with
`adb install -r`; installation succeeded and the package update time became 14:32. No uninstall,
clear-data, media operation, or database-content read occurred. The expected Room ledger file was
absent before and after update, so no persisted ledger could be proven present or lost. The app
process is stable and Bluetooth Scan/Connect permissions are granted.

## Automatic reconnect: empty inventory safety incident

With the current compatible build and persisted known-camera onboarding, the Pocket connected
automatically after reopening Osmosis. The user did not tap Rescan, the camera row, media, or
Download. This is a scoped **PASS** for automatic known-camera connection.

The resulting gallery was empty although the immediately preceding real inventory had five videos,
including a newly recorded asset and protected partial/local-copy evidence. This is a scoped
**FAIL-CLOSED / INCOMPLETE_UNTRUSTED** post-connect enumeration observation, not an empty camera,
deletion, completed sync, or cleanup eligibility. No known asset is inferred deleted and neither
redundancy nor Safe-to-Clear is promoted.

ADB evidence captured while the failure was present: package `1.4.4 (29)` updated at 14:32;
`CameraConnectionService` foreground host active; durable runtime epoch `4`, state `READY`,
`user_stopped=false`, and no active transfer. This rules out an explicit stop or an obviously stale
session as the direct UI cause, but does not establish Pocket media API readiness. No media names,
database contents, credentials, or GPS data were collected. The available log buffer contained no
sanitized enumeration event for this interval.

Source inspection then established a reproducible fail-open transition: a zero-result response was
passed to the grid as an authoritative empty list, and ledger completeness was derived from
`moreAvailable` rather than the enumerator's `pagesEnded` result. The completion call also hard-coded
store/member/generation coverage to false, preventing a subsequently trusted automatic plan from
becoming eligible. The repair performs one bounded fresh-session revalidation for empty/incomplete
post-connect results; retained zero/incomplete results stay `REVALIDATING` and render an explicit
untrusted state instead of "no media"; only a non-empty, complete, non-failed response may make the
session and durable plan trusted. The repair is software-proven by targeted JVM source-session,
recovery, enumerator, planner, and backup tests; API-36 process-restoration remains PASS. The
recreation/background harness was updated to establish its synthetic live session after the
intentional launcher epoch and to grant declared Bluetooth runtime prerequisites on its
emulator-only path. Its direct instrumentation process still exits before the assertion with only
`Process crashed` and no usable stacktrace after two distinct harness repairs; retain this as an
isolated emulator-harness investigation, not a product or hardware claim. The focused JVM
recreation/observer and process-restoration contracts remain passing.

APK SHA-256 `E137FE19F67444BDC69894E90BC401B28FEB07EBB142803213C1BF262D72B0C6` was built
signer-compatible for an in-place S25 update. The S25 became unavailable to ADB before installation,
so no update or data change occurred after this build.

## Final software checkpoint and validation artifact

The final software checkpoint ran `:app:testDebugUnitTest :app:assembleDebug` on 2026-09-18 and
completed with 453 JVM tests, zero failures/errors and `BUILD SUCCESSFUL`. The former Windows-only
golden failures were caused by CRLF fixture lines retaining `\\r` in the test expectation; the test
now normalizes fixture line endings before comparison. It does not change manifest decoding or any
production behavior.

The final signer-compatible debug validation artifact is
`app/build/outputs/apk/debug/app-debug.apk`, SHA-256
`544D36B98B308E8DF0A9A6A356D6107FAD55B34FD981E1A2847392909E2FB915`.
The associated one-session procedure is
`docs/hardware/FINAL_CONSOLIDATED_VALIDATION_PROCEDURE.md`. This is software evidence only; all
physical results remain pending or explicitly deferred in the validation queue.

## Final-artifact S25/Pocket observation — advertisement unavailable

On 2026-09-18 the final artifact SHA-256
`544D36B98B308E8DF0A9A6A356D6107FAD55B34FD981E1A2847392909E2FB915` was installed in place
on physical S25 `SM-S938B` without uninstalling or clearing data. The package retained its known
camera entry, Bluetooth was enabled, and `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` were granted.
With the Pocket powered on, the UI showed **0/1 saved cameras in range** and **0 new**; the saved
camera row was disabled. No scan result existed from which automatic selection could safely begin.

Classify this observation as **INCONCLUSIVE / source-advertisement unavailable**, not a false
empty inventory or a failure to connect to a visible saved camera. No Rescan, camera-row tap,
media operation, deletion, or storage operation was performed. The next discriminating observation
is to establish that the Pocket is awake and advertising BLE, then perform one fresh app-open scan;
only then can the automatic-known-camera selection be evaluated.

## Software follow-up — independent SSD catch-up

The real session showed camera connection/revalidation and later phone-grid availability must not be
used as a prerequisite for phone-to-SSD work. The product requirement is now explicit: an approved
SAF tree is probed by the application at startup and on USB/media availability hints; it is not a
manual per-sync mode. A usable provider schedules only durable, independently verified phone
receipts from the latest complete source snapshot. This survives camera disconnect and process
recreation. A missing, revoked, unprobeable, or insufficient-capacity destination remains
unavailable, never blocks camera-to-phone work, and cannot promote redundancy or Safe-to-Clear.
The SSD copy retains the exact durable Osmosis relative path and creates a missing capture-day
directory under the user-approved tree; collisions are review-required rather than overwritten.

Software evidence on 2026-09-18: targeted SSD/recovery tests plus the integrated
`:app:testDebugUnitTest :app:assembleDebug` checkpoint, **458 JVM tests, zero failures/errors,
BUILD SUCCESSFUL**. The Android-test restart source for latest-complete-snapshot selection compiles.
The resulting debug APK SHA-256 is
`27C877C412EFAF673AEE3788CE1874A1FD644293DB9FA04936267D20177CD600`.
Physical USB/provider behavior remains explicitly deferred to `MVP-SSD-INDEPENDENT-CATCHUP`.
