# Governance bootstrap evidence — 2026-09-17

## Initial worktree initialization — identity and scope

- Working repository: `ZeSluma/osmosis-travel-backup`.
- Origin fetch/push URL: `https://github.com/ZeSluma/osmosis-travel-backup.git`.
- Upstream `KonradIT/osmosis` remains strictly read-only; only HTTP GET requests were made.
- Initial branch: `main`; initial tracked worktree clean. The user-provided `CODEX_HANDOFF_ALL_IN_ONE.md` was already untracked and remains unmodified.
- Local HEAD, local main, origin/main, live fork main and live upstream main: `2fcdbc97e6dbefc875d425368be67cf32b50bb06`.
- GitHub API confirms fork=true, default branch=main, parent=source=`KonradIT/osmosis`.
- No bootstrap branch existed locally or in the live fork branch list; the fork had only main and no open PRs/issues.
- `git ls-tree -r --name-only main -- AGENTS.md PROJECT_STATE.yaml CHANGELOG.md docs` found only existing upstream `docs/01-protocol-map.md`. Worktree discovery found no governance files or existing plans, decisions, evidence or incidents to reconcile.
- Created local `bootstrap/project-initialization` from verified main. Main was not moved.
- Extracted the 17 authorized governance files from the complete supplied handoff. Updated PROJECT_STATE and appended dated observations; preserved upstream documentation.
- No commit, push, PR, merge, release, application change or build execution was performed.

## Read-only commands and sources

Initial commands: `git status`, `git rev-parse --show-toplevel`, `git remote -v`, `git branch --show-current`, `git rev-parse HEAD`.

Additional inspection: `git branch -a -vv`, `git rev-parse main`, `git ls-tree`, file discovery, `git log -5 --oneline`, workflow/build/manifest/network-policy reads and targeted README/ROADMAP searches.

Live GET endpoints (GitHub REST API, 2026-09-17):

- `https://api.github.com/repos/ZeSluma/osmosis-travel-backup`
- `https://api.github.com/repos/ZeSluma/osmosis-travel-backup/branches?per_page=100`
- `https://api.github.com/repos/ZeSluma/osmosis-travel-backup/pulls?state=open&per_page=100`
- `https://api.github.com/repos/ZeSluma/osmosis-travel-backup/issues?state=open&per_page=100`
- Corresponding repository, branches, pulls and issues endpoints for `KonradIT/osmosis`.
- `https://api.github.com/repos/KonradIT/osmosis/releases/latest`
- `https://api.github.com/repos/KonradIT/osmosis/commits/v1.4.4`

Latest upstream release is v1.4.4, published 2026-09-07, resolving to `b07196504c220782563cbe42a5b300e991f6e192`, behind current main. It is not a project known-good version. Open upstream issues: #40 Mini Pro 5 Wi-Fi password; #32 Osmo Action 1 support. Open PRs: #24 v2 WLM API (explicitly untested), #11 Osmo Action 1 WIP, #8 RSDK device. No direct Pocket 4P blocker was established from these descriptions; they provide no target-hardware assurance. PR base SHAs are PR metadata, not substitutes for the live main branch SHA.

## Rechecked baseline observations

- `app/build.gradle`: compile/target SDK 36, min SDK 29, Java/Kotlin 21, versionName 1.4.4, versionCode 29. Listed dependencies match the historical reconstruction.
- README describes Pocket 4 / 4 Pro as hardware verified upstream. This is not evidence for this project's S25 Ultra + Pocket 4P gate.
- ROADMAP documents Activity-started bare Thread downloads, existing range resume and nonpersistent downloaded ticks. Runtime behavior remains NOT_TESTED.
- Manifest: allowBackup=false; MainActivity exported; preview activity, GPS service and FileProvider non-exported. Bluetooth, location, network/Wi-Fi, Internet, notification and foreground-service permissions present.
- Network security globally permits cleartext for local camera HTTP. Scope review remains open; no configuration change was made.
- Unit CI validates wrapper, uses JDK 21 and runs testDebugUnitTest. Actions use mutable version tags. The test workflow has no explicit permissions block.
- Release workflow is tag-triggered, has contents:write, consumes signing secrets and creates a draft release. Fork secrets were neither read nor assumed available. No workflow was triggered deliberately.

## Operational findings and reconciliation

During initial initialization, `git ls-remote` failed because that Git runtime could not find `remote-https`. Live identity and refs were instead verified with GitHub's read-only REST API. Initial API access was sandbox-blocked; an approved read-only network invocation succeeded. Branch creation initially encountered the sandbox's read-only .git boundary and succeeded after approval. No dependency/tool installation or Git configuration change was made. This historical runtime failure is not a current HTTPS-access blocker: the user confirmed a successful HTTPS clone from `https://github.com/ZeSluma/osmosis-travel-backup.git`. GitHub push authentication not yet verified.

ADR 0002 requires GitHub verification before treating repository mutation as remotely successful. This bootstrap PASS covers local initialization and validation only. Remote publication is unverified and remains an explicit open operational item. No remote mutation is claimed.

## Gate report

| Field | Result |
|---|---|
| GATE | GATE-0 |
| IMPLEMENTATION | Governance only; functional implementation NOT AUTHORIZED |
| UNIT TESTS | NOT_TESTED |
| REGRESSION | NOT_TESTED |
| SECURITY | INITIAL_REVIEW; observations only; dependency/secret scans NOT_TESTED |
| HARDWARE TEST | NOT_TESTED |
| OPEN FINDINGS | Build/wrapper/unit/lint/security and hardware evidence missing; remote governance publication unverified. Separately, fork signing remains a future release prerequisite, not a GATE-0 blocker |
| EVIDENCE | This report plus the unchanged source commit; no APK or hardware evidence |
| GATE STATUS | NOT_TESTED |
| NEXT ACTION | Await explicit push authorization, then verify fork publication; reproduce unchanged baseline without functional fixes |

Known-good version and commit remain null. Release remains prohibited. NOT_TESTED is never PASS.

## Initial worktree validation (before commit review)

PASS: all 17 expected governance files exist, plus this evidence report. Fifteen match their handoff templates; PROJECT_STATE and BASELINE_RECONSTRUCTION contain the explicit dated reconciliation. An allowlist check found no other new files except the pre-existing handoff. Conflict-marker, trailing-whitespace and required-state checks passed. The active plan and evidence paths exist.

`git diff --exit-code main --` and `git diff --check main` passed: all tracked source, app, Gradle, protocol, workflow and upstream documentation files remain unchanged. New governance files are untracked, so they were checked separately by the allowlist/template validation; the empty tracked diff alone is not evidence of their content. Branch is `bootstrap/project-initialization`; HEAD and main remain the verified candidate SHA. These checks establish local documentation consistency only, not any Android test or security gate PASS.

## Commit review reconciliation — 2026-09-17

The user accepted the pre-flight and authorized one local governance commit, with no push. The HTTPS-access blocker was removed on the user's confirmed clone evidence; only push authentication remains unverified. Release signing is tracked separately as a future release prerequisite in PROJECT_STATE, SECURITY, QUALITY_GATES, RISK_REGISTER and the active plan. The original handoff comparisons above describe the initial extraction; the additional policy clarifications are intentional user-requested changes.

The reviewed 18 governance files were staged, excluding the local handoff. The first authorized commit attempt failed with `Author identity unknown`; no commit was created by that attempt. The user subsequently configured repository-local author identity, which was verified before retrying the authorized commit. PROJECT_STATE records the committed snapshot as LOCAL_COMMIT_ONLY; the containing Git commit supplies its identity. No push is authorized or claimed; GitHub publication remains unverified under ADR 0002. GATE-0 remains NOT_TESTED, known-good fields remain null, and implementation and release authorization remain false.
