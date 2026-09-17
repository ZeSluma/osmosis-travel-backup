# Quality Gates

## 2026-09-17 intent rebase notice

The user accepted order **0 -> 1 -> 2 -> 3 -> 7 -> 4 -> (5,6) -> 8**, retaining all gate IDs and historical evidence. [ADR0005](decisions/0005-gate-safety-and-optional-features.md) records adoption; [dependency graph and accepted scopes](plans/REVISED_GATE_DEPENDENCIES.md) govern execution. GATE-7 precedes GATE-4 because autonomous one-tap backup requires proven lifecycle-safe execution, automatic reconnect and recoverable synchronization. Sections remain in numeric ID order for lookup, not execution. GATE-0 remains baseline reproduction, not product-completeness approval. R-016..R-041 and TEST_MATRIX define current additions.

## Gate state model

R-037 automatic capture-day organization is an additional product acceptance requirement: G1 reviews source/timezone/fallback and destination feasibility; G2 persists/reserves paths; G3/G7 prove crash/resume stability; G4 proves real capture-day grouping; G5/G6 reuse it for replicas. CD01-CD08 remain NOT_TESTED. This changes no historical GATE-0 outcome or gate ID/order adoption status.

Each gate is one of:

- PASS
- FAIL
- BLOCKED
- NOT_TESTED

`NOT_TESTED` is never `PASS`.

Every gate report must include:

- GATE
- IMPLEMENTATION
- UNIT TESTS
- REGRESSION
- SECURITY
- HARDWARE TEST
- OPEN FINDINGS
- EVIDENCE
- GATE STATUS
- NEXT ACTION

## GATE 0 — Unchanged baseline

Goal: prove the unmodified fork baseline is reproducible.

Required:

- exact repository and commit identity
- Gradle wrapper validation
- `assembleDebug`
- `testDebugUnitTest`
- lint/static checks where defined
- dependency/secret/security baseline
- S25 Ultra + Pocket 4P connect/browse/download evidence
- resumable download baseline evidence
- no project feature code

Gate closes only when baseline is reproducible and evidence is persisted.

Fork release signing is a future release prerequisite. Its absence alone does not prevent GATE 0 from passing; GATE 0 records signing assumptions without requiring release readiness.

## GATE 1 — Security and architecture baseline

Required:

- permission review
- exported-component review
- network-security review
- credential/storage plan
- logging/privacy plan
- dependency/supply-chain review
- build/signing/CI review
- confirm accepted policy implementation design: two independent copies, six asset classes, capture-day paths, GPS/logging independence and future cleanup isolation
- architecture decision on persistent orchestration
- architecture decision on storage and network binding
- threat/failure review

Functional backup extension remains blocked until GATE 1 is complete.

## GATE 2 — Persistent ledger + all-new backup

Required:

- durable foundations and recording/asset identity/state model, including capture-day/path and classification provenance; broad one-open/all-new acceptance is GATE-4
- persistent media identity/state model
- idempotent re-run behavior
- new/unverified-only transfer
- unit tests for state transitions
- restart/recovery tests
- target-hardware integration evidence

## GATE 3 — Integrity + failure handling

Required:

- transfer-complete rules
- integrity policy
- interrupted-transfer safety
- safe retry/resume
- process-death behavior
- failure matrix coverage
- no false VERIFIED state

## GATE 4 — One-tap BACKUP CAMERA

Prerequisites: GATE-2, GATE-3 and GATE-7 PASS. Full snapshot/recording enumeration, automatic new/unverified-only planning, stable capture-day layout and GPS/Save logs OFF tests must pass.

Required:

- minimal user action
- deterministic orchestration
- clear progress/status
- safe recovery
- no hidden destructive behavior
- target hardware evidence

## GATE 5 — P310 / external storage

Required:

- explicit storage permission/access model
- transfer verification
- disconnect/reconnect behavior
- duplicate-run behavior
- low-space behavior
- real P310 evidence

## GATE 6 — OneDrive

Required:

- secure auth/token storage
- Internet/network transition design
- cloud unavailable must not invalidate local success
- retry policy
- privacy/logging review
- real account integration evidence

## GATE 7 — Robust foreground/background operation

Required:

- process death
- screen off
- Android lifecycle/background restrictions
- notification/foreground-service correctness
- resumability
- battery/network behavior
- large-file evidence

## GATE 8 — Optional zero-touch detection

Required:

- explicit user approval
- reliable camera detection
- bounded battery/privacy cost
- safe false-positive behavior
- rollback/disable path

This gate is optional and must not compromise the reliability of the one-tap path. It never authorizes automatic deletion.

## Proposed GATE-9 — Explicit verified-snapshot cleanup

Placement proposal only, not yet adopted/authorized: after GATE-4 and policy-required GATE-5 and/or GATE-6, with G2/G3/G7 dependencies already passed. GATE-8 is independent. Require reviewed strong source/storage identity and group collateral effects, current two-domain replica proof, exhaustive pre/post enumeration, explicit scope-bound confirmation, durable partial-operation recovery, security of every destructive entry point, CL01-CL10 and S25/Pocket HIL using separately authorized disposable recordings. No real destructive use before dedicated acceptance and explicit authorization. Protocol ambiguity leaves the action unavailable. No format fallback. Historical gate records stay intact.
