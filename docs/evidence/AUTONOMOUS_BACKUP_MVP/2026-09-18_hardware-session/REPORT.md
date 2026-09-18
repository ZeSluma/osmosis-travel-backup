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
