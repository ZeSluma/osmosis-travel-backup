# Active Plan — GATE 1 Security and Architecture

Status: **BLOCKED** after completed read-only closure review. [Closure evidence](../../evidence/GATE-1/2026-09-17_security-closure/REPORT.md) and [ADR0007](../../decisions/0007-security-closure-disposition.md) supersede initial pending-review status. B1 is unnecessary sensitive launcher test controls; B2 is Kotlin security remediation MUST_FIX_BEFORE_IMPLEMENTATION. Explicit narrow authorization and SC09/SC10 evidence are the next work, not functional extension. The initial verification remains historical.

2026-09-17 rebase: design artifacts now drafted in ADR0003/0004 and docs/design; this does not satisfy all acceptance criteria or authorize code. See [intent audit](../../evidence/GATE-1/2026-09-17_intent-rebase/INTENT_AUDIT.md) and [accepted gate dependencies](../REVISED_GATE_DEPENDENCIES.md). Hardware is PAUSED_BY_USER; resume only after explicit return. No ADB/camera actions belong to this planning task.

Read PROJECT_STATE.yaml and completed GATE-0 build, disposition and hardware evidence before starting. Preserve the unchanged baseline and read-only upstream boundary.

## Required review

Accepted entry decisions: gate order, default phone plus independent SSD/cloud redundancy, mandatory capture-day organization, refined unknown semantics and explicit-only future cleanup. Review [ADR0005](../../decisions/0005-gate-safety-and-optional-features.md), cleanup design and GPS/diagnostics design; no policy question about default copy count remains open.

- Permissions, exported components, network scope, credential storage and logging/privacy.
- Kotlin advisory, dependency provenance, action pinning, CI cache trust and fork signing design.
- Persistent job/ledger ownership, Android lifecycle and network binding.
- Separate observed background/return failure, AUTO_RECONNECT failure and AUTO_RESUME failure; preserve manual range-resume evidence.
- Completed-file/UI recognition after restart: file and MediaStore persist, but observed UI recognition failed.
- Camera-side sleep/standby as a separate low-confidence risk; characterize idle/display timeout versus actual transport/session loss and active-transfer exposure without assuming causality.
- Storage boundaries, integrity/completion criteria and failure-recovery decisions.

## Acceptance and restrictions

R-037 review additionally covers [capture-day resolution](../../design/CAPTURE_DAY_ORGANIZATION.md): Pocket timestamp/offset/naming trust, deterministic uncertainty rules, stable allocation across retries, parent-sidecar grouping and mixed-MIME phone storage feasibility. CD01-CD08 define downstream acceptance; actual hardware remains paused and NOT_TESTED. Clock synchronization on baseline connection must be considered before any later timestamp experiment.

The connectedDevice/per-Network architecture, credential/cleartext/logging/GPS/ledger models and accepted policy boundaries now have explicit evidence-backed dispositions in the closure report. Runtime identity/enumeration/lifecycle/migration/permission assumptions are assigned there to later G2/G3/G7/G4/G5/G6/G9 tests and remain NOT_TESTED. B1 attack-surface acceptability and B2 remediation are actual current closure conditions. GATE-9 placement/confirmation defaults remain ADR0006; its capability proof is not performed in G1. No one-tap capability claim before lifecycle/recovery and full snapshot verification.

Persist the security/architecture decisions and threat/failure review required by QUALITY_GATES.md. Do not mark untested behavior PASS. GATE-0 known-good identifies a measured baseline with disclosed limitations, not production backup assurance.

implementation_authorized and release_allowed remain false. No functional fixes, dependency/configuration changes or release actions are authorized by this plan. Functional extension requires explicit subsequent authorization after GATE-1 criteria are satisfied.
