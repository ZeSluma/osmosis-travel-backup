# Operations

Current execution policy (2026-09-17): GATE-2 implementation is authorized; AGENTS.md now permits continuous progression through sufficiently defined non-destructive gates after dependency/evidence checks. Earlier statements requiring a new permission solely at a gate boundary are historical and superseded. Hardware explicitly resumed for non-destructive GATE-2 validation on 2026-09-17; earlier pause statements below are historical. No main mutation, merge, release or real deletion is authorized. Current implementation/evidence status is PROJECT_STATE.yaml, the active GATE-2 plan and docs/evidence/GATE-2/2026-09-17_hardware/REPORT.md.

## Current security closure disposition

[ADR0008](decisions/0008-b1-b2-security-remediation.md): GATE-1 PASS; explicitly authorized B1/B2 scope is completed on gate-1/security-foundation. GATE-2 is now explicitly authorized and software-verified under ADR0009; its physical hardware evidence remains pending. The current continuous policy governs later defined-gate progression. Historical ADR0007 blockers are closed by persisted verification. Do not execute removed debug intent hooks, inspect real credential values through tools, enable untrusted/shared build metadata or introduce KAPT. G2 app-side credential migration is tested with synthetic values; it is not authorization to export credentials. Use synthetic canaries for privacy tests. No source, dependency, Gradle or workflow edits were made by the closure review.

Credential loss means normal re-pair/re-entry after explanation, never automatic camera reset. Planned migration retires plaintext only after durable decryptable ciphertext; no backup/export of credentials/ledger state. Runtime diagnostics must be bounded/sanitized and user-exported; existing raw Save Logs is not compliant by assumption. Signing uses a separately controlled fork identity before release; no upstream-key assumption. Hardware remains paused, media untouched and unresolved sleep/foreground/background causes separate. Implementation follows the continuous policy and active gate scope; release authorization remains false.

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

## Accepted review and future cleanup operations

ADR0005 adopts gate order 0-1-2-3-7-4-(5,6)-8 and default two independent outside-camera copies. ADR0006 accepts GATE-9 SAFE CAMERA CLEANUP with G4 and policy-sufficient G5/G6 prerequisites; G8 is not required. Do not interpret architecture acceptance, safe status or backup completion as permission to run destructive commands. G2 software work is implemented under the active plan; hardware remains paused and unresolved causes remain unconfirmed.

Future cleanup requires explicit user initiation/confirmation of a freshly revalidated exact snapshot. No format, automatic background delete or widened retry scope. On interruption retain the per-item audit and reconcile read-only. ADR0006 allows continuation under unchanged, still-live bounded operation authorization only; process/camera restart, cancellation, expiry or safety change requires new confirmation of the remaining intended set. Never reconfirm/resend already proven removals. If identity, replicas or post-enumeration cannot be verified, report action required/unverified; never success. No camera-original or completed-copy changes are authorized now.

GPS Sync is optional recording telemetry; Save logs is temporary advanced diagnosis. Neither is a runtime requirement for backup. After an explicit hardware return, controlled foreground-drop reproduction may use reviewed/sanitized temporary upstream logs only if required; record/restore toggle state, terminate verbose capture, correlate connection/session/visibility events and never upload automatically. Do not persist raw secrets, coordinates or media while collecting evidence. No log mode is a reliability workaround.

## ChatGPT / Codex execution model

Normal ChatGPT conversation may be used for planning, research and review.

Repository mutation should be performed only through a verified write-capable engineering environment, currently intended to be Codex or an equivalent user-authorized Git environment.

The repository remains the persistent source of truth.

Public distribution and final production cloud OAuth/deep-link/signing configuration require the future R-043 [Product Identity / Distribution phase](plans/PRODUCT_IDENTITY_DISTRIBUTION.md): final independent app/signing identity, tested development-install migration, credits/license preservation and explicit DJI non-affiliation. Planning only; no current branding change, production auth registration or release authorization.
