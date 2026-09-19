# S25 backup-status projection regression — 2026-09-19

## Reproducible target evidence

The signer-compatible debug APK connected to the saved Pocket and showed a populated six-item
grid. The target UI dump nevertheless recorded `backupSummary` with `text=""`.  This is a
product regression: the user could not see Camera Sync, redundancy, Safe-to-Clear, or automatic
plan state even though connection and inventory presentation were live.

## Cause

`MainActivity.onCameraChosen` installed `sourceAssociation` before its asynchronous credential
continuation. `connectAndOffload` then called `teardownOffload`, whose deliberately fail-closed
`releaseTransport` clears the association and strict-transfer capability. The subsequent trusted
datalink observation therefore could not publish into `CameraBackupPlanCoordinator`; the UI
subsequently returned from `refreshBackupLabels` because no ledger session existed.

The capability flag was additionally set in the UI `onReady` observer, which is ordered after the
service-owned trusted-observation publisher. A newly valid plan could therefore be rejected by
the first dispatch attempt.

## Repair and internal verification

* `CameraSessionResources.configureSelectedCamera` reestablishes a source association only after
  transport release; the service exposes the boundary and the Activity invokes it before GATT.
* Strict automatic-transfer support is configured before datalink starts, rather than in UI
  `onReady`.
* A no-ledger grid renders an explicit pending/awaiting-trusted-inventory summary rather than
  appearing blank.
* Focused resource/dispatch/datalink tests passed.
* `:app:testDebugUnitTest :app:assembleDebug` passed with 472 tests, zero failures and zero
  errors.

## Target retest — PASS for status projection

The repaired signer-compatible APK was installed in place with no data clear. The S25 then showed
the connected Pocket grid and the nonempty summary:

`Camera sync: pending · Redundancy: pending · Safe to clear: no · Auto: plan complete=false;
reason=IDENTITY_UNRESOLVED; current-unresolved=0; history-unresolved=3; download=2; verify=3;
revalidate=1; review=0`

The service was foreground-active with `startRequested=true`. The plan correctly did not start a
writer because its historical evidence remains unresolved. This is a fail-closed outcome: it does
not claim complete inventory, verified backup, redundancy, cleanup eligibility, or successful
automatic transfer. The blank projection defect itself is closed.

## Follow-on target recovery finding

The same S25 session showed that foreground/background retention passed. A controlled Pocket
power-cycle did not automatically restore the camera: it remained greyed out until the user used
Rescan. That Rescan made the saved camera active and it was then selected automatically exactly
once; Connecting → Revalidating restored the six-item grid and its fail-closed product summary.

The observation reopened the live AP-loss recovery path. A repaired build now treats loss of the
selected camera AP from an active grid as a bounded BLE discovery/rebuild instead of merely
reissuing the stale Wi-Fi request. The new path is capped at three scans, respects user stop and
does not permit transfer until a fresh trusted enumeration. Full internal verification is 473 JVM
tests with zero failures/errors plus a debug build. Physical retest remains pending.

## Final power-cycle retest — PASS

After the final in-place, signer-compatible update, the app again automatically connected on
launch and displayed the six-item grid with its status projection. The user then powered the
Pocket off, waited twelve seconds, and powered it on without interacting with the app. The app
automatically displayed `Connecting`, then `Revalidating`; the camera reacted and the six-item
grid returned. The projection remained `Camera Sync Pending`, `Redundancy Pending`, `Safe to
Clear No`, with the existing `IDENTITY_UNRESOLVED` plan reason and no writer falsely started.

This is a target PASS for known-camera recovery after controlled loss. The implementation is
bounded at five scan windows, fenced by the durable session epoch, and does not retry after an
explicit user stop.
