# ADR 0002 — Development execution model

**Status:** Accepted for current project operations

## Decision

Use normal ChatGPT conversation for:

- planning
- requirements
- research
- architecture review
- project-state review

Use a verified write-capable engineering environment, currently intended to be Codex, for:

- branch creation
- file edits
- builds
- tests
- commits
- repository mutations

## Rationale

The normal ChatGPT GitHub connector produced branch-write failures and inconsistent session/plugin state during setup. Repeated connector reinstall attempts are not an acceptable engineering dependency.

## Consequences

- repository state remains authoritative
- Codex must bootstrap from repository docs before implementing
- Chat history is supporting context, not source of truth
- no repository mutation is considered successful until verified in GitHub
