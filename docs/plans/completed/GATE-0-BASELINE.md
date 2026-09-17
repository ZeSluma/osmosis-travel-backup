# Completed Plan — GATE 0 Baseline

Completed 2026-09-17: PASS for unchanged-baseline reproducibility, with runtime failures carried forward as documented debt. Final evidence: `docs/evidence/GATE-0/2026-09-17_hardware/REPORT.md`. Acceptance criteria below are unchanged; this is not feature-completeness or release approval.

## Goal

Establish a reproducible, unchanged Osmosis baseline on the user fork before any functional project extension.

## Scope

Allowed:

- repository/governance initialization
- source reconstruction
- build/test reproduction
- security baseline observation
- target-hardware baseline testing
- evidence capture

Not allowed:

- persistent ledger implementation
- new backup orchestration
- P310 support
- OneDrive support
- functional app behavior changes

## Step 1 — Re-verify repository

- verify fork identity
- verify parent/source
- refresh current `main`
- capture exact commit
- inspect branch list
- inspect relevant PRs/issues/workflows
- inspect latest upstream release/changes
- reconcile against `docs/BASELINE_RECONSTRUCTION.md`

## Step 2 — Bootstrap branch

Create:

`bootstrap/project-initialization`

from the verified current `main`.

## Step 3 — Persist governance

Add/reconcile project governance files only.

Verify diff contains no functional Android source changes.

## Step 4 — Reproduce build

At exact baseline commit:

- validate wrapper
- JDK 21
- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`
- run available lint/static checks
- record all results

Do not "fix" upstream baseline failures during the measurement run. Record them first.

## Step 5 — Baseline security/release observation

Capture:

- permissions
- exported components
- cleartext config
- dependencies
- CI/action posture
- secret/signing assumptions

No release is authorized.

Fork release signing is a future release prerequisite, not a condition for GATE 0 PASS. Record its availability and assumptions without requiring a release signing setup for the unchanged debug baseline.

## Step 6 — Target hardware baseline

On Samsung Galaxy S25 Ultra + DJI Osmo Pocket 4P, capture:

- install/launch
- camera connection
- browse/enumeration
- normal original download
- large-file behavior
- existing resume behavior after intentional interruption where feasible

## Acceptance criteria

GATE 0 may PASS only when:

- exact baseline commit is known
- unchanged debug build is reproducible
- unit-test status is known
- static/lint status is known
- key security baseline is documented
- target hardware baseline is evidenced
- no project functional changes were introduced
- evidence is persisted in repo

## On GATE 0 PASS

Set:

- `known_good_commit`
- `known_good_version` if meaningful
- GATE 0 status PASS

Then enter GATE 1.

Functional feature implementation should remain blocked until GATE 1 security/architecture baseline is complete.
