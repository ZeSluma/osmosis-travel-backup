# Automatic camera-to-phone closure audit — 2026-09-25

## Scope

This is a fresh mandatory closure audit of the integrated acceptance chain, not a reuse of prior
PASS labels: known-camera recovery → fresh trusted inventory → durable reconciliation → automatic
plan → one fenced strict writer → partial/failure preservation → local integrity receipt → derived
product state.

## Findings fixed in this audit

1. Historical unresolved observations correctly made global completeness false, but also blocked
   fresh DOWNLOAD candidates with a complete current observation. `automaticDownloadEligible`
   separates those claims: fresh work needs current full coverage and zero current unresolved
   identities; history still blocks sync, redundancy and cleanup.
2. A terminal paginated page could bypass the service plan coordinator. It now returns through the
   active session/source fence, aggregates every current page, dispatches only after final trusted
   planning, and cannot repaint a replacement grid.
3. An empty/partial live-source projection could formerly run an empty strict batch and complete
   the scheduler. The dispatcher now refuses unless every planned asset has a live source path
   before acquiring a transfer lease.

## Required audit coverage

| Area | Result |
|---|---|
| Service/resource ownership | Camera resource, durable session, ledger plan, automatic writer and external replica owners are application/service scoped. Activity paths only request scans/pages and observe fenced durable projection; terminal pages no longer mutate the ledger directly. |
| Callback/generation fencing | BLE, GATT, AP and datalink generation fences; source/session fence for paginated pages; writer lease and stale completion fencing are covered. |
| Recovery/replacement/user stop | Bounded known-camera recovery, replacement epochs, explicit-stop refusal and fresh revalidation are internally tested; controlled S25 recovery is already target-proven. |
| Durable inventory/plan/transfer | Incomplete observations remain untrusted; current-safe work is distinct from historical completion; partial/interrupted results remain review-required. |
| Process recreation | Durable ledger/partial and replica rehydration coverage is retained; no process restart infers resume safety. |
| Replica/integrity/status | Independent SAF staging/readback and fail-closed status derivation remain covered; no unfinished receipt, replica or incomplete inventory can promote completeness. |
| Focused faults | Current additions cover historic ambiguity, paginated/replacement page callbacks and empty/partial source projection. |

## Reproducible internal evidence

`./gradlew.bat :app:testDebugUnitTest :app:assembleDebug` passed on the final source state with
**481 JVM tests, zero failures and zero errors**. Focused policy/runtime/datalink/resource/
strict-transfer checks also passed.

## Remaining boundary

The remaining claim is physical only: on the S25/Pocket, the two current safe candidates must
start service-owned strict downloads after normal trusted enumeration. The same observation must
show that historical ambiguity still leaves Camera Sync Pending, Redundancy Pending and Safe to
Clear No. It is queued as `G7-LIFECYCLE-RECOVERY`; no deletion, overwrite, identity inference or
verification shortcut is involved.

## 2026-09-25 final amendment — target transfer and user-facing state copy

The consolidated target session subsequently demonstrated the previously pending current-safe path:
a new 101 MB recording was discovered in a seven-item manifest, strict-transferred automatically
without a Download tap, checkpointed durably, and read back to `TRANSFERRED_UNVERIFIED`. The UI
correctly retained Camera Sync/Redundancy/Safe-to-Clear as pending/no because historical identity
ambiguity is still open. See
[`CAMERA_LIFECYCLE_AND_TRANSFER.md`](../2026-09-25_hardware-batch/CAMERA_LIFECYCLE_AND_TRANSFER.md).

The user also identified a product clarity gap: technical summary tokens such as
`identity unresolved` were truthful but not useful. `BackupStatusCopy` is now the single
privacy-safe UI-copy boundary. It translates only durable flags and counts to German
action-oriented wording, without source names, paths, credentials, GPS, or a completion claim.
`BackupStatusCopyTest`, the state matrix, and the full final checkpoint
`./gradlew.bat :app:testDebugUnitTest :app:assembleDebug` passed with **493 JVM tests, zero
failures/errors**. The generated APK SHA-256 is
`6C55E53D5BBBE29B676DE6602AD0096649D9EB1EAC351D7B35BD0D1EDCFFA8E2`.
