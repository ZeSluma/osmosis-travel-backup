# Active Plan — GATE 1 Security and Architecture

Status: **PASS** under [ADR0008](../../decisions/0008-b1-b2-security-remediation.md). The user explicitly authorized only B1/B2 security implementation on gate-1/security-foundation; [verification evidence](../../evidence/GATE-1/2026-09-17_B1-B2/REPORT.md) closes both blockers. This scope is complete. Await explicit GATE-2 authorization; no automatic functional extension. The prior read-only blocked result remains historical.

2026-09-17 rebase: design artifacts now drafted in ADR0003/0004 and docs/design; this does not satisfy all acceptance criteria or authorize code. See [intent audit](../../evidence/GATE-1/2026-09-17_intent-rebase/INTENT_AUDIT.md) and [accepted gate dependencies](../REVISED_GATE_DEPENDENCIES.md). Hardware is PAUSED_BY_USER; resume only after explicit return. The later explicit B1/B2 authorization permitted isolated-emulator ADB security checks, not physical-device or camera testing.

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

The connectedDevice/per-Network architecture, credential/cleartext/logging/GPS/ledger models and accepted policy boundaries now have explicit evidence-backed dispositions in the closure report. Runtime identity/enumeration/lifecycle/migration/permission assumptions are assigned there to later G2/G3/G7/G4/G5/G6/G9 tests and remain NOT_TESTED. B1 attack-surface acceptability and B2 remediation have verified closure evidence under ADR0008. GATE-9 placement/confirmation defaults remain ADR0006; its capability proof is not performed in G1. No one-tap capability claim before lifecycle/recovery and full snapshot verification.

Persist the security/architecture decisions and threat/failure review required by QUALITY_GATES.md. Do not mark untested behavior PASS. GATE-0 known-good identifies a measured baseline with disclosed limitations, not production backup assurance.

implementation_authorized and release_allowed remain false for general product/release work. The completed B1/B2 exception does not authorize further fixes or features. The plan is retained here for traceability until a separately authorized next-gate plan replaces it.
