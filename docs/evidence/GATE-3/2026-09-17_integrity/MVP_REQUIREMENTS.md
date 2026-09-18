# Internal MVP requirement matrix

Scope: local Pocket-to-phone backup prototype. Evidence labels retain their scope; no item below grants GATE-3 PASS.

| Requirement | Internal disposition | Evidence / boundary |
|---|---|---|
| Source observation, complete/incomplete distinction, identity reconciliation | PASS — emulator/simulation | Ledger enumerator/reconciliation tests preserve historic assets and reject incomplete deletion inference. Pocket paging/session behavior remains hardware proof pending. |
| Required original types, unknown assets, relationships and capture-day paths | PASS — emulator/simulation | Ledger model/instrumentation covers video/photo/RAW/audio/metadata/exclusions, unknown review, DJI filename local-day evidence and stable reservation paths. Real Pocket sidecar semantics remain hardware pending. |
| Automatic backup planning | PASS — software | Complete enumerations map only safe `DOWNLOAD` plan items into the UI worklist; incomplete, partial, unknown and ambiguous items are excluded. User still explicitly starts the queued strict batch. |
| Owned transfer, progress/checkpoint, interruption safety and final publication | PASS — emulator/simulation | Strict transfer and pending MediaStore fixtures cover range/body faults, cancellation, no duplicate allocation, readback and publication gaps. Target provider behavior is hardware pending. |
| Resume | PASS — simulated; hardware pending | Guarded fake transport resumes only on immutable source revision, prefix hash and exact `206` contract. Pocket adapter intentionally refuses append until real continuity semantics are qualified. |
| Transfer integrity / source equivalence / overall verification | PASS — emulator/simulation | Orthogonal evidence tests retain transfer confirmation while overall remains `UNVERIFIED` without source equivalence. |
| Restart/process death/migrations | PASS — emulator/simulation | Ledger, integrity and resume process fixtures cover retained partial/evidence through schema 7. Target migration/UI binding remains hardware pending. |
| UI product state | PASS — software/emulator | Ledger badges distinguish new, partial-review, transferred-unverified, existing-unverified and review-required. Final S25 rendering is hardware pending. |
| Lifecycle/background reconnect and camera sleep recovery | DEFERRED | GATE-7 architecture/product work; baseline target observations show failures and no hardware claim is made. |
| SSD/cloud redundancy and camera cleanup | DEFERRED | Later gates. Phone-only evidence cannot satisfy redundancy or safe-to-clear predicates. |

The only hardware batch is `G3-PROTOTYPE-TOMORROW` in `docs/hardware/VALIDATION_QUEUE.json`.
