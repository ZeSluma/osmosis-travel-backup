# GATE-1 architecture/security closure review

Date: 2026-09-17. Reviewed HEAD `43033f8eb68d075fad99f06feba3fd270d2fe36e`, branch `bootstrap/project-initialization`; main and merge-base `2fcdbc97e6dbefc875d425368be67cf32b50bb06`. Origin is `https://github.com/ZeSluma/osmosis-travel-backup.git`. Upstream remains read only. This is a completed review with **GATE-1 BLOCKED**, not a completed implementation or a security certification.

The repeated user attachment contained the same closure request and was processed once. Hardware remains PAUSED_BY_USER. No ADB, media access, exploit attempt, credential access, app build, dependency change or camera interaction occurred. Historical GATE-0 evidence is preserved, including PASS with limitations and separate unresolved sleep/background/foreground observations.

## Evidence and checklist

Detailed evidence: [credentials, transport and execution](PLATFORM.md), [components, diagnostics and GPS](SURFACES.md), [dependencies and ledger](DEPENDENCIES_LEDGER.md). [ADR0007](../../../decisions/0007-security-closure-disposition.md) records the disposition. DESIGN_RESOLVED below is a review outcome, not an additional gate state or runtime PASS.

| Acceptance criterion | Disposition / closure evidence |
|---|---|
| Credential model | DESIGN_RESOLVED: per-camera Keystore-protected blobs, migration and invalidation contract in PLATFORM; storage/migration runtime NOT_TESTED |
| Cleartext boundary | DESIGN_RESOLVED: finite numeric-host platform exception plus mandatory per-Network endpoint guard; all-client enforcement NOT_TESTED |
| Network ownership | DESIGN_RESOLVED: official Network APIs support service-owned manager and scoped transports; concurrency and loss fencing NOT_TESTED |
| Android execution | DESIGN_RESOLVED: connectedDevice contract, bounded recovery and durable restart described; S25 lifecycle NOT_TESTED |
| Component/intent attack surface acceptable | **FAIL, B1**: exported launcher exposes sensitive test-control extras without visible build/caller gate; no exploit attempted |
| Logging/privacy design | DESIGN_RESOLVED: all-sink typed allowlist, bounded two-layer diagnostics; existing unsafe sinks remain baseline debt and need enforcement before product acceptance |
| GPS separation | DESIGN_RESOLVED: optional location owner; source call sites are separate from backup, no background-location requirement for continued visible-start location FGS; end-to-end tests pending |
| Dependency disposition explicit | RESOLVED as **MUST_FIX_BEFORE_IMPLEMENTATION, B2** for Kotlin advisory; candidate identified, no upgrade/test performed |
| Android 17 plan | DESIGN_RESOLVED: target37 permission migration and N05 tests; no current manifest change |
| Ledger security model | DESIGN_RESOLVED: minimum app-private operational data, secrets excluded, no unjustified DB encryption, backup exclusion and non-destructive migrations |
| Runtime assumptions assigned | DONE in the matrix below; none promoted to PASS |

## Concrete blockers and remediation boundaries

- **B1 / RISK-041:** external launcher intent reaches pairing override/logging, Wi-Fi inputs, scan/offload and paging test controls. Launcher export is necessary; that sensitive control surface is not. Close with separately authorized narrowly scoped security hardening, source/release-merged-manifest audit and adversarial intent tests proving no external pairing override, credential logging, arbitrary network endpoint or destructive authority. A build-type guard alone is insufficient for a distributed debug artifact: isolate/remove or require an appropriate restricted test entry point. Legitimate camera shortcuts must validate bounded identifiers against known cameras and cannot authorize cleanup.
- **B2 / RISK-042:** Kotlin plugin1.9.24 is affected. Choose MUST_FIX_BEFORE_IMPLEMENTATION because future compiler/ledger tooling must not expand the affected cache surface. Require an authorized dependency-only security change, compatible fixed version, clean trusted-cache build/unit/lint comparison and processor review. This is a conservative project disposition, not evidence of exploitation. The advisory decision is complete; its remediation is not. No dependency edit is authorized in this task.

These are not blockers that can be closed by more prose. Missing future ledger/service implementation or paused hardware is not itself a GATE-1 blocker under this review's architecture-only criteria. Signing remains a release prerequisite. G1 closure requires B1 remediation and B2's pre-implementation condition satisfied (or a new explicit evidence-backed policy decision); no implicit risk acceptance is recorded.

## Deferred proof ownership (all NOT_TESTED for target design)

| Proof / acceptance oracle | Owning gate / tests |
|---|---|
| Credential migration crash points, key invalidation, no secret in any sink/backup/export; stable per-camera association | G2 security foundation, SC01/SC02; required before enabling the credential store |
| Ledger uniqueness, fenced owner, migration rollback and URI/DB publication reconciliation | G2/G3, SC08, I/T/S matrix |
| Network-bound HTTP/UDP/TCP/preview, redirect rejection, stale callback fencing; numeric XML policy on min29/target36 | G7, SC03, N01-N04; before one-tap G4 acceptance |
| Visible-start FGS, notifications denied, screen off, low memory, process death, force-stop, Task Manager, multi-GB and battery | G7, SC04, X01-X07; no FGS immortality claim |
| Complete inventory, strong camera/storage/asset identity, six asset classes, required companions | G4, I01-I04/AS01-AS03; G2 models conservative unknown identity |
| Capture time/offset/clock disagreement, midnight/travel/DST, sidecars and mixed-MIME stable destination | G4 CD01-CD08; G2/G3/G7 persistence checks precede it |
| GPS OFF and verbose OFF independent backup; opted-in location/approximate/denied/revoked and BLE arbitration | G7 then G4, SC05/SC06, GD01-GD05 |
| Android16 local-network restriction and target37 permission grant/deny/revoke | G7 compatibility work / N05 / SC07 before any target37 migration; no device flashing now |
| Camera sleep versus display timeout, foreground awake drop, session-state candidate, background return | G7 N/P matrix; distinct causes remain unconfirmed, no pause-time root-cause conclusion |
| Separate SSD/cloud storage domain, TLS and routing, credential isolation | G5/G6 before replica acceptance; cloud concurrency optional, sequential default |
| Exact-scope destructive authority, collateral effects, live proof revocation/partial continuation | G9 CL01-CL12 after accepted prerequisites; disposable media and separate authorization only |

## Verification record

Fresh read-only checks: `git branch --show-current`, `git rev-parse HEAD main origin/main`, `git merge-base HEAD main`, `git remote get-url origin`, and `git diff --name-only --diff-filter=MD <baseline>` exit0: correct branch/base, no modified/deleted upstream-tracked file. `git ls-remote origin refs/heads/main refs/heads/bootstrap/project-initialization` exit0 confirmed the identities above before editing. Read-only GitHub issues endpoint returned zero open issues/PRs. Source/merged-debug XML, Kotlin call sites and both workflows inspected. A mistaken read of nonexistent `.gradle.kts` files was corrected to the existing Groovy `.gradle` files; it was not a build failure.

Existing debug APK SHA256 freshly matches baseline evidence: `58e5cfde7e8398660db997dd48f69e37eef1611311130480fb98a03762ab1a3b`. No app execution or rebuild. Component findings use source and existing merged-debug artifact; release merged manifest/runtime are not newly verified. Wrapper hash independently matches official Gradle8.8 wrapper JAR checksum; distribution still8.14.5. OSV direct-coordinate query and primary advisory/source checks are recorded in DEPENDENCIES_LEDGER. No new full-history secret scan; historical redacted scan has explicitly bounded coverage.

GATE: GATE-1. IMPLEMENTATION: none. UNIT TESTS: not rerun (documentation only). REGRESSION: functional baseline files unchanged. SECURITY: reviewed, B1/B2 open. HARDWARE TEST: NOT_TESTED for target architecture; paused. GATE STATUS: BLOCKED. NEXT ACTION: explicit narrow security-remediation scope for B1/B2, then re-review G1; do not start functional extension or release.

Pre-commit validation: 16 governance/state/evidence files; relative Markdown links resolve, state keys are unique, G0 PASS/G1 BLOCKED/hardware PAUSED and both authorization=false assertions hold. YAML validation used structural assertions, not a full parser. `git diff --check` passed; line-ending warnings reflect existing checkout policy, no configuration change. No modified/deleted baseline tracked files; no historical G0/completed-plan edits. Handoff remains untracked and excluded from the explicit stage list. New evidence contains only source descriptions and sanitized lookup/hash summaries. Commit/push is limited to the named bootstrap branch; publication SHA and independent remote-main check are reported after publication rather than embedded self-referentially in this commit.
