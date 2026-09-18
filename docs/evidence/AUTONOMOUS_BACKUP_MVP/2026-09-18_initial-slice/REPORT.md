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
  records no verified external proof. Destination state, append-only replica integrity evidence,
  and durable staged-operation state are persisted through Room schema 9.
- External replica allocation retains the ledger capture-day path as a strict `YYYY-MM-DD/file`
  SAF directory layout. Malformed calendar/path input and either file/directory collision are
  refused rather than flattened or overwritten.
- Existing GATE-7 service ownership provides a second opaque scheduler generation; old callback
  completion cannot release/complete a replacement operation. User stop fences both camera and
  replica scheduling.
- Scanner, GATT and camera-AP joiner allocation/start now route through the application/service
  façade using application context. The Activity supplies observer callbacks, but no longer
  constructs or starts those camera effects directly.
- The gallery observes a read-only ledger-derived summary for camera sync, redundancy, and
  informational cleanup eligibility. It remains false for incomplete inventory or an
  `UNKNOWN_POTENTIALLY_REQUIRED` blocker and has no cleanup action.
- A cross-layer deterministic recovery scenario exercises active loss, replacement session,
  stale completion rejection, incomplete revalidation, unavailable SSD and explicit stop. It
  proves the software-state interaction only, not the physical radio or USB behavior.
- A known-camera availability policy automatically selects the most-recent configured camera only
  after it advertises through the normal permission-gated scan. It is suppressed by explicit user
  stop or an in-flight connection. Real Pocket wake/advertisement behavior is not claimed.
- A retained external-replica `INTENT`, `COPYING` or `PARTIAL` operation suppresses new allocation
  for that asset/destination. Provider-specific append recovery remains unavailable until it can
  be safely reconciled; the system chooses durable review over duplicate output.
- External allocation now checks persisted read/write grant and the matching SAF provider root's
  advertised available bytes before it stages a document. Permission loss, unknown capacity and
  insufficient capacity record an unavailable reason and allocate nothing.
- Deterministic replica fault injection covers truncated/corrupted content, cancellation, durable
  sync failure, and finalization/rename failure. Every such path remains incomplete or rejected
  and never sets external verification.

Early focused JVM checkpoint: backup package, connection package and automatic-plan tests passed
with zero failures/errors. `assembleDebug` passed before the final bridge correction; the current
focused checkpoint is recorded below. The API 36 isolated emulator also
passed the deterministic Room schema 8→9 migration/restart test: replica-operation journal
creation, destination identity persistence, and an explicitly PARTIAL checkpointed replica
operation survive reopen without promotion. This evidence does not prove real
Pocket discovery, S25 external-host behavior, hub/SSD behavior, SAF-provider rename semantics or
real-media copy/recovery.

### Current integrated emulator checkpoint

After the service-effect ownership refactor, the current debug and instrumentation APKs passed on
the same isolated API 36 emulator: `lifecyclePhase=recreate`
reported `PASS: G7 recreation/background retained fenced session and single writer`; and
`backupPhase=replicaMigration` reported `PASS: replica migration, destination persistence, partial
restart and missing reconciliation`. The emulator was granted only synthetic BLE permissions to avoid
the platform dialog; it used no camera, hub or SSD.

The replica fixture now also re-enumerates under a fresh ledger lease after restart and confirms
that a retained `PARTIAL` staged operation can become `MISSING` only through explicit inspection.
That makes a later allocation eligible without falsely verifying either the staged object or SSD
redundancy. This is a deterministic synthetic provider outcome, not a real-SSD reconnect claim.

### Consolidated focused JVM checkpoint

`testDebugUnitTest` selecting the backup package, connection package and
`AutomaticBackupPlanTest` passed **36 tests, zero failures/errors** across 14 result files. This
is the current autonomous-backup/lifecycle/planner regression scope; it deliberately does not
reclassify the repository-wide CRLF-sensitive golden-test baseline.

## Next software work

Extract the remaining protocol callback/revalidation coordinator from the Activity, expand
deterministic SAF/provider fault injection, and complete a requirement-by-requirement closure
audit. Queue physical uncertainty only after those software paths are exhausted.
