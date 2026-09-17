# Operations

## Session bootstrap

Every engineering session starts by reading:

- `AGENTS.md`
- `PROJECT_STATE.yaml`
- core docs
- active plan
- relevant evidence/incidents
- current branch/commit
- open PRs/issues
- relevant workflows

Do not rely on chat history as the only state source.

## Branching

- never write to upstream
- never mutate `main` directly during bootstrap/feature work
- create purpose-specific branches
- first branch: `bootstrap/project-initialization`

Before branch creation, refresh `main`.

## Upstream synchronization

Treat upstream as read-only.

When syncing:

1. inspect upstream changes
2. assess interaction with custom project changes
3. use an explicit sync/merge branch
4. run relevant regression gates
5. never silently overwrite project-specific behavior

## Incident handling

For a real defect:

1. preserve current state/evidence
2. record incident under `docs/incidents/`
3. reproduce where possible
4. add a regression test where practical
5. fix minimally
6. rerun affected gates
7. update state/risk documentation

## Rollback

Maintain:

- known-good version
- known-good commit
- release artifact identity
- APK SHA256

Rollback must not depend on remembering a chat conversation.

## Release naming

Artifacts must identify version and/or commit.

Avoid ambiguous names such as:

- `final.apk`
- `new.apk`
- `latest-final.apk`

## Hardware evidence

Current hardware pause is recorded in PROJECT_STATE. A planning/documentation instruction does not implicitly resume phone/camera testing. Preserve unresolved runtime causes as unconfirmed and distinguish published evidence from uncommitted local observations. The 2026-09-17 intent rebase explicitly authorizes publishing the preserved evidence with one governance/planning commit; it does not authorize further media transfer or power/permission experiments.

Hardware-required gates must state exact devices used.

No hardware PASS by inference.

## ChatGPT / Codex execution model

Normal ChatGPT conversation may be used for planning, research and review.

Repository mutation should be performed only through a verified write-capable engineering environment, currently intended to be Codex or an equivalent user-authorized Git environment.

The repository remains the persistent source of truth.
