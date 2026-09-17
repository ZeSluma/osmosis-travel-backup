# ADR 0001 — Fork and upstream boundary

**Status:** Accepted

## Decision

- `KonradIT/osmosis` is upstream and read-only.
- `ZeSluma/osmosis-travel-backup` is the only project write target.
- Project changes must remain as thin and isolated from upstream as practical.

## Rationale

The project must preserve upgradeability, auditability and rollback while protecting upstream from accidental mutation.

## Consequences

- no direct writes to upstream
- explicit upstream-sync process
- minimal protocol rewrites
- project-specific functionality isolated where practical
- every change remains attributable to the user-controlled fork
