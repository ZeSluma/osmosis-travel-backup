# Autonomous backup MVP — initial software slice

Branch: `codex/autonomous-backup-mvp`. Status: **IN PROGRESS; SOFTWARE-ONLY FOUNDATION**.

## Implemented and checked

- A complete/revalidated ledger plan can automatically initiate only `DOWNLOAD` work through the
  application-owned scheduler. Incomplete/untrusted inventory, explicit stop and stale work leases
  produce no camera IO.
- The phone-to-external core stages a new target, copies from a measured phone receipt, checks
  expected length and SHA-256, independently reads the target back, and only then finalizes it.
  It does not claim Pocket source equivalence.
- Android external storage is selected with a persistable SAF tree grant, not a raw mount path.
  Missing permission/provider capability/collision leaves destination or replica unavailable and
  records no verified external proof. Destination state and append-only replica integrity evidence
  are persisted in Room schema 8.
- External replica allocation retains the ledger capture-day path as a strict `YYYY-MM-DD/file`
  SAF directory layout. Malformed calendar/path input and either file/directory collision are
  refused rather than flattened or overwritten.
- Existing GATE-7 service ownership provides a second opaque scheduler generation; old callback
  completion cannot release/complete a replacement operation. User stop fences both camera and
  replica scheduling.
- The gallery observes a read-only ledger-derived summary for camera sync, redundancy, and
  informational cleanup eligibility. It remains false for incomplete inventory or an
  `UNKNOWN_POTENTIALLY_REQUIRED` blocker and has no cleanup action.

Focused JVM checkpoint: backup package (9), connection package (13), and automatic-plan (2):
**24 tests, zero failures/errors**. `assembleDebug` passed before the final bridge correction;
the final focused compile/test checkpoint passed after it. The API 36 isolated emulator also
passed the deterministic Room schema 8→9 migration/restart test: replica-operation journal
creation, destination identity persistence, and an explicitly PARTIAL checkpointed replica
operation survive reopen without promotion. This evidence does not prove real
Pocket discovery, S25 external-host behavior, hub/SSD behavior, SAF-provider rename semantics or
real-media copy/recovery.

## Next software work

Exercise the Room migration and Android SAF provider with deterministic instrumentation/fakes,
persist replica operation progress/recovery, surface derived backend completion state, and add an
end-to-end fault matrix. Queue physical uncertainty only after those software paths are exhausted.
