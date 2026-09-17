# Quality Gates

## Gate state model

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
- architecture decision on persistent orchestration
- architecture decision on storage and network binding
- threat/failure review

Functional backup extension remains blocked until GATE 1 is complete.

## GATE 2 — Persistent ledger + all-new backup

Required:

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

This gate is optional and must not compromise the reliability of the one-tap path.
