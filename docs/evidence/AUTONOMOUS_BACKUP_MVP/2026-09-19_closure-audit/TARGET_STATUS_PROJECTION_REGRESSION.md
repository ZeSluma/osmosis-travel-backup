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

## 2026-09-25 automatic-download execution finding

The recovered target session displayed the six-item grid and the durable status projection:
`Camera Sync Pending`, `Redundancy Pending`, `Safe to Clear No`, `Auto Plan Complete False`,
`IDENTITY_UNRESOLVED`, `current unresolved=0`, `history unresolved=3`, `download=2`,
`verify=3`, `revalidate=1`, `review=0`.

No automatic download started. This is not recorded as a transfer failure or a false completion:
the current implementation refuses automatic paths whenever the plan's `enumerationComplete` is
false, and historical unresolved observations make that plan incomplete. The next software task
is to audit whether the two current, concretely observed new assets can be dispatched safely while
the three historical observations remain unresolved and continue to block Camera Sync Complete,
redundancy, and Safe-to-Clear. No camera deletion, overwrite, identity inference, verification
promotion, or manual retest was performed.

## 2026-09-25 safe-current dispatch repair — internal PASS, target pending

The audit found that `LedgerRepository.plan` had one global `enumerationComplete` bit. It was
correct for overall completeness, but it also prevented a new strictly non-resume `DOWNLOAD`
whose current source observation was complete and had no current unresolved identity. That joined
two distinct safety claims unnecessarily.

The repair adds an explicit `automaticDownloadEligible` property. It is true only when the current
snapshot proves complete pages, stores, recording members and stable generation, has no coverage
or enumeration failure, and has zero unresolved identities *from that snapshot*. Historical
ambiguity remains represented by `enumerationComplete=false`, so Camera Sync Complete, redundancy,
Safe-to-Clear, deletion eligibility and any unproven resume path remain blocked. The runtime and
automatic plan consume only this narrow eligibility for fresh `DOWNLOAD` scheduling; they retain
their existing writer and epoch fences.

Focused planner/policy/runtime/dispatcher tests and the full JVM/debug build passed with **479
tests, zero failures and zero errors**. The remaining target observation is deliberately narrow:
after a normal trusted connection, the two current candidates may begin service-owned strict
downloads, while the summary must remain Camera Sync Pending, Redundancy Pending and Safe to Clear
No until the historical identity ambiguity is actually resolved. No camera-original mutation,
overwrite, identity inference or VERIFIED promotion is authorized by this repair.

## 2026-09-25 paginated-inventory dispatch repair — internal PASS, target pending

The mandatory ownership audit found a second execution gap: a user-requested next manifest page
was reconciled directly by `MainActivity`. If that page was terminal, it could make the durable
inventory complete but did not call the service dispatcher; it also retained only the terminal
page in the service file map. This could leave automatic work absent or conservatively fail with
`TRANSFER_SOURCE_CHANGED` for earlier pages.

The Activity now hands a fetched page back to `CameraBackupPlanCoordinator`. That service bridge
requires the active ledger session and source association, accumulates only that session's observed
pages, and invokes dispatch only after its terminal trusted ledger plan is durable. A transport
release clears the page map. The UI applies a page to its grid only after the same service-owned
session/source fence, so a stale callback cannot repaint a replacement camera grid. The added
resource regression proves current pages aggregate, a stale session/source page is rejected, and
release clears retained source data. Focused connection/dispatch tests and the full JVM/debug build
passed with **480 tests, zero failures and zero errors**.

The target proof remains the same non-destructive observation: allow normal complete enumeration
and confirm service-owned work starts for the two current candidates while historical ambiguity
continues to keep Camera Sync Pending, Redundancy Pending and Safe to Clear No.
