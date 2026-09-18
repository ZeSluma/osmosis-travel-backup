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
