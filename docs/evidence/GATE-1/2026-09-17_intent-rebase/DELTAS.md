# Reviewable requirements and architecture delta

Baseline for review: published 0a848e536004f4f03a51254452f6bf431a956d8f plus preserved pre-existing local hardware evidence. No upstream/app/build/workflow edits. Use Git diff for exact wording; the following maps each intended change rather than silently replacing the old model.

| Document/section | Before | After / scope |
|---|---|---|
| REQUIREMENTS product goal | Ideally one action; reliable backup workflow | Intentional Open starts/attaches to self-healing transactional sync; normal manual queue/rescan/retry prohibited |
| R001/R002 | Minimal connection flow / relevant enumeration | Automatic known-camera connect and complete original/recording-member snapshot independent of UI |
| New R016-R036 | Many contracts implicit or postponed | Lifecycle owner, bounded recovery, strong identity, asset scope/generation, range/integrity/crash/storage, replica routing, taxonomy, action UX, completion, Android17, resources/diagnostics/EOL/sleep explicit |
| REQUIREMENTS sequencing | Historical numeric list | List retained as history, explicit dependency proposal and one-tap prerequisite constraint |
| ARCHITECTURE | Suggested modules, long-transfer owner unspecified | Service-hosted fenced single writer, UI observers, connection/protocol/enumerator/planner/transfer/ledger/verifier/destination/diagnostics boundaries; A-Q answered |
| Android execution | No approved camera execution ADR; implicit dataSync assumption unsafe | ADR0003 chooses connectedDevice as conditional design, compares UIDT/dataSync/WorkManager/CDM, stop/screen-off limits explicit |
| Network ownership | Process-binding observation; routing issue noted | ADR0004 per-Network camera traffic, separate cloud client, sequential default, no unproven S25 dual-Wi-Fi claim |
| Ledger/identity | Broad ledger intention | Tables/unique constraints/epochs/recording groups, provenance/confidence, journaled MediaStore reconciliation, migration requirements |
| Verification/completion | General integrity goal | Exact size/range/version/readable/publish rule, local-hash limitation, sealed snapshot and required-recording/destination predicates |
| Asset policy | Undefined all originals | User-confirmed non-regenerable originals/audio/metadata; known previews excluded, unknown blocks classification; exhaustive target inventory still pending |
| Quality gates | Lifecycle gate after one-tap/SSD/cloud | Explicit proposed order 0-1-2-3-7-4-(5,6)-8 with IDs/history preserved, adoption pending |
| SECURITY/RISK/TEST_PLAN | Initial baseline issues and broad matrix | Threat/disposition mapping, all hardware failure classes and new identity/enum/routing/platform/crash risks, fault/HIL test oracles |
| PROJECT_STATE | Prepared unpublished G0 PASS; G1 NOT_TESTED | Preserve scoped G0 PASS/G1 not accepted, hardware PAUSED_BY_USER, proposal/decision/evidence pointers and blockers; both authorizations false |

Pre-existing uncommitted GATE-0 evidence is included intact as measured provenance in this single reconciliation commit. New notes clarify publication/pause state only; no original failure/result is erased. Unresolved sleep/foreground-root-cause observations remain unresolved. No camera/phone file is accessed or modified in this task.
