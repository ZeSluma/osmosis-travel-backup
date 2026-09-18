# Autonomous camera → phone → SSD backup MVP

Status: ACTIVE (software-first); branch: `codex/autonomous-backup-mvp`.

## Boundary

GATE-7 remains closed at `4f13086`. Its service-owned epoch/recovery contract is reused, not
reopened. This plan adds persistent orchestration and replica truth on top of the established
strict camera-to-phone transfer boundary. It does not authorize deletion, a release, an unsafe
Pocket append/resume, or a claim that a physical Pocket/S25/SSD behaves like a fake.

## Ordered vertical slices

1. Define independent phone/SSD integrity and derived completion semantics; stage, checksum and
   read back replicas before finalization. Persist destination identity and replica evidence.
2. Add Android Storage Access Framework destination registration/reacquisition and safe temporary
   replica allocation. Permission loss, absence, low space and unexpected files fail closed.
3. Connect complete trusted ledger plans to one service-owned automatic transfer/replication
   orchestrator. Incomplete enumeration, unknown required assets, user stop and stale epochs block IO.
4. Add deterministic end-to-end fault simulations: camera loss/replacement/revalidation, process
   restoration, stale transfer/replica callbacks, destination loss/reappearance and corruption.
5. Bind a minimal persistent observer projection in the UI. Then run focused, build and emulator
   checkpoints and queue only unresolved physical facts into the one hardware batch.

## Proof vocabulary

`PHONE_VERIFIED` and `SSD_VERIFIED` mean independently read-back content equality to a measured
local phone proof. They do not establish Pocket source equivalence. `CAMERA_SYNC_COMPLETE` and
`BACKUP_REDUNDANCY_COMPLETE` require a trusted fresh inventory with no unknown-required blocker;
`SAFE_TO_CLEAR_CAMERA` is informational and additionally requires fresh source revalidation.
