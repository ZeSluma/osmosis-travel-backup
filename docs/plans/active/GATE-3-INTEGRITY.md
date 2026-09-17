# Active GATE-3 — strict integrity and safe failure handling

## Current checkpoint (supersedes historical preparation below)

Bundled target session completed on8144ded: A normal transfer/file integrity, B interruption, shared restart and safe retry refusal PASS within their scopes. Original references preserved. Four members observed but snapshot INCOMPLETE; source proof absent, overall UNVERIFIED. Successful resume remains NOT_TESTED. Raster A/B distinction failed on target; read-only ledger badges now pass six new unit tests and emulator rendering. Full382 tests/debug/test build/release compile PASS; five baseline lint errors unchanged. See docs/evidence/GATE-3/2026-09-17_integrity/SESSION_DISPOSITION.md.

Target disconnected before UI APK update (`adb: no devices found`); no install occurred. Human stop: reconnect S25, then install and perform one scoped A/B label observation with no transfer/retry. Do not repeat already settled Playback observations. A current ETag alone cannot establish original B source continuity; no append or gate PASS on safe refusal alone. G3 NOT_TESTED, implementation true, release false. No further media transfer is authorized by the completed two-test queue.

G2 PASS at136b02b3951b18def39ace3062ca164584d1f2cd. Branch codex/gate-3-transfer-integrity. Implementation authorized; release false; G3 overall NOT_TESTED. ADR0013 resolves the former entry decision: independently confirmed transfer integrity is retained, but source uncertainty means overall UNVERIFIED. No transfer-only completion or cleanup authority.

## Current software scope

Schema6 additively stores transfer receipts, source-equivalence evidence and durable transfer attempts. Strict HTTP range/length validation precedes allocation/write; owned pending destinations are journaled, synced, closed and read back before publication. Pocket4Pro explicit full-video UI uses this path; existing unverified copies are preserved without transfer. Unsupported types/trimmed requests require review in this scoped path. Other camera legacy behavior is outside this change. No G7 service/automatic-recovery claim.

Interrupted owned partials are retained. Without proven prefix integrity and source continuity, retry does not append, truncate, reallocate or use the legacy downloader. Safe blocking is implemented; production successful resume remains UNPROVEN, not PASS. Current Pocket evidence does not supply immutable-version proof. Unknown-source behavior is not permission to weaken the integrity contract.

SOFTWARE_PROVEN and EMULATOR_PROVEN results are recorded separately in docs/evidence/GATE-3/2026-09-17_integrity/REPORT.md. Target S25 still has G2/schema5; no real media read/hash/download/delete or new install occurred during software development. Previous MediaStore fixture failure was traced to an inserted provider row without a materialized file; creating only the new owned empty file before inspection passed the synthetic regression. Original failures remain in the report.

## Work classification / continuation

- A: no false overall verification; reject unsafe range append, ambiguous local adoption and invalid ownership; preserve partials/history through failure/restart.
- B: strict explicit transfer UI, durable journal/publication, process-death tests, regression checkpoint; then consolidate remaining actual Pocket HTTP/source-continuity and S25 storage/lifecycle evidence.
- C: optimize conservative full-file readback/checkpoint overhead; broad sidecar/types UI in G4, lifecycle/automatic session recovery in G7, unrelated baseline lint debt.

Follow docs/EXECUTION_POLICY.md. Continue useful software work before requesting hardware. Do not repeat identical rescans, reopen passed gates, or interpret simulation as hardware proof. No main/merge/release/destructive work. Existing no-download/no-media-mutation restriction remains binding until explicit scoped authorization changes it.

## Pending consolidated hardware validation

User has returned and authorized only the queue's new non-critical test downloads. Existing reference files and camera originals remain protected. This supersedes the earlier no-download restriction only for those tests; no broadened authorization. Deliver the ordered session procedure before physical testing, then collect each outcome independently and use the combined evidence for software continuation.

Queue details and independent PASS/FAIL/INCONCLUSIVE criteria: docs/evidence/GATE-3/2026-09-17_integrity/HARDWARE_QUEUE.md. Target installation/migration, preserved old copies, real response metadata, new-owned staging/publication, interrupted partial protection and restart evidence should share one prepared session. Successful source-continuity/resume is not assumed; if the target exposes no trustworthy proof, retain the blocked result and investigate in software. Never request repeated rescans as a substitute for that missing proof.
