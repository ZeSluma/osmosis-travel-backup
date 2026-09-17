# GATE-3 prototype software checkpoint

Date: 2026-09-18. Scope: safe, non-destructive software preparation for one bundled Pocket 4P/S25 validation session. This is not target-hardware proof and does not close GATE-3.

## Implemented boundary

`CheckedCopy` now rechecks cancellation immediately after a blocking read and before writing that buffer. A cancelled owner therefore cannot expose unjournaled bytes, advance progress, or create a durable checkpoint from a buffer returned after cancellation. The targeted regression test injects cancellation during `InputStream.read` and proves zero bytes written and zero checkpoints.

The remaining crash boundary after a successful readback and persisted transfer receipt but before `preparePublication` remains deliberately fail-closed. The schema stores a confirmation receipt and local revision, but not a durable complete-file digest that a new process can read back against. Restart recovery therefore refuses to publish that state rather than infer local integrity from length or historic receipt alone. This is recorded as prototype hardening debt, not a completion path.

## Software evidence

* Targeted `CheckedCopyTest`, `GuardedResumeTest`, and `StrictTransferBatchTest`: PASS.
* Full `testDebugUnitTest`, `assembleDebug`, and `assembleDebugAndroidTest` in the isolated LF checkout: PASS.
* Emulator `ledgerPhase suite`: PASS. It exercises synthetic complete/incomplete enumeration, identity ambiguity, transfer fault injection, publication recovery, schema migrations through 7, local reconciliation, capture-day grouping, and integrity/source-verification separation.
* All emulator fixtures are guarded to the `ranchu` SDK device and use a separate no-backup database. They are not Pocket evidence.

## Prototype limits retained honestly

* Pocket source-version continuity is not proven. The Pocket adapter does not enable append/resume, and overall verification remains `UNVERIFIED` without independent source equivalence.
* An incomplete or empty enumeration remains untrusted and cannot infer deletion/completeness.
* A persisted partial does not retry automatically unless the guarded resume contract is available.
* Receipt-before-publication crash recovery remains review-required until a durable complete-file revalidation contract is designed.

## Consolidated next hardware session

Install one debug build, preserve protected references, connect once, and observe the final ledger-derived status labels. With two newly recorded non-critical assets, exercise one full strict transfer and one interrupted transfer. Reconnect only once as required by the session flow. Test append/resume only if the final adapter exposes a newly justified immutable source-version contract; otherwise verify safe refusal without creating a second local copy. Restart once and inspect persisted local/journal state. The detailed PASS/FAIL/INCONCLUSIVE criteria remain in `docs/hardware/VALIDATION_QUEUE.json`.

No camera media, protected local media, main, release, or dependency configuration was changed.
