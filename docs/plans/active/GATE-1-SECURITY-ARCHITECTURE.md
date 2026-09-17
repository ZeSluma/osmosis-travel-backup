# Active Plan — GATE 1 Security and Architecture

Status: NOT_TESTED. Administrative successor to completed GATE-0; no review or functional implementation is implied by this plan's creation.

2026-09-17 rebase: design artifacts now drafted in ADR0003/0004 and docs/design; this does not satisfy all acceptance criteria or authorize code. See [intent audit](../../evidence/GATE-1/2026-09-17_intent-rebase/INTENT_AUDIT.md) and [explicit gate proposal](../REVISED_GATE_DEPENDENCIES.md). Hardware is PAUSED_BY_USER; resume only after explicit return. No ADB/camera actions belong to this planning task.

Read PROJECT_STATE.yaml and completed GATE-0 build, disposition and hardware evidence before starting. Preserve the unchanged baseline and read-only upstream boundary.

## Required review

- Permissions, exported components, network scope, credential storage and logging/privacy.
- Kotlin advisory, dependency provenance, action pinning, CI cache trust and fork signing design.
- Persistent job/ledger ownership, Android lifecycle and network binding.
- Separate observed background/return failure, AUTO_RECONNECT failure and AUTO_RESUME failure; preserve manual range-resume evidence.
- Completed-file/UI recognition after restart: file and MediaStore persist, but observed UI recognition failed.
- Camera-side sleep/standby as a separate low-confidence risk; characterize idle/display timeout versus actual transport/session loss and active-transfer exposure without assuming causality.
- Storage boundaries, integrity/completion criteria and failure-recovery decisions.

## Acceptance and restrictions

R-037 review additionally covers [capture-day resolution](../../design/CAPTURE_DAY_ORGANIZATION.md): Pocket timestamp/offset/naming trust, deterministic uncertainty rules, stable allocation across retries, parent-sidecar grouping and mixed-MIME phone storage feasibility. CD01-CD08 define downstream acceptance; actual hardware remains paused and NOT_TESTED. Clock synchronization on baseline connection must be considered before any later timestamp experiment.

Before GATE-1 closure: review selected connectedDevice execution and per-Network routing designs, resolve redundancy policy, validate asset identity/enumeration capability assumptions with separately authorized experiments, disposition security risks, and explicitly adopt/revise the gate dependency proposal. Asset inclusion policy is user-confirmed, but target inventory remains unverified. Record which decisions are approved and which HIL proofs remain future gate criteria. No one-tap capability claim before lifecycle/recovery and full snapshot verification.

Persist the security/architecture decisions and threat/failure review required by QUALITY_GATES.md. Do not mark untested behavior PASS. GATE-0 known-good identifies a measured baseline with disclosed limitations, not production backup assurance.

implementation_authorized and release_allowed remain false. No functional fixes, dependency/configuration changes or release actions are authorized by this plan. Functional extension requires explicit subsequent authorization after GATE-1 criteria are satisfied.
