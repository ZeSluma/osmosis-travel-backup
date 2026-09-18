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
