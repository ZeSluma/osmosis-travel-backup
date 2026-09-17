# GATE-9 reconciliation, then GATE-1 initial read-only verification

2026-09-17. Starting branch `bootstrap/project-initialization`, HEAD `b7a8224299be9bf219cc255d238e91cc90880977`; local main/merge-base `2fcdbc97e6dbefc875d425368be67cf32b50bb06`. Governance reconciliation is separate from the verification work below; neither authorizes functional code, deletion, hardware resumption, dependency changes, merge or release.

## 1. Accepted decision reconciliation

ADR0006 records accepted GATE-9 SAFE CAMERA CLEANUP, graph dependencies, one explicit default confirmation, optional enhanced confirmation OFF, live safety revocation and exact plan/proof-generation authorization binding. G9 requires G4 and its G2/G3/G7 prerequisites plus policy-sufficient G5 OR G6; both secondary gates only when policy requires both. G8 is independent.

Continuation decision: a still-live, bounded, explicitly confirmed operation may continue only its original remaining set after complete read-only reconciliation proves unchanged material safety inputs. Expected authorized removals are not scope expansion. Process death, camera restart, cancellation, expiry or any safety-relevant change requires fresh validation and one confirmation of the remaining plan. Already confirmed removals are neither resent nor reconfirmed. CL11/CL12 supplement CL01-CL10 and mandatory non-critical-media S25/Pocket HIL. Policy adoption is not a destructive capability PASS.

Current requirements, architecture, gate graph/criteria, operations, security, ledger/state-machine/test designs and project state reconcile these decisions. Historical evidence and completed plans remain untouched. Existing capture-day/source-priority/sidecar policy is preserved.

## 2. Fresh GATE-1 bootstrap and verification scope

Read PROJECT_STATE, AGENTS, core governance, active G1 plan, ADR0003-0006, relevant design/evidence and workflows. Reconcile unchanged baseline with accepted requirements. Hardware remains PAUSED_BY_USER. Verification started as official-document/source comparison, not app execution. Earlier GATE-0 build, secret-scan and cache results are explicitly historical evidence, not newly run tests.

The following findings are a first substantive review. Status describes verification work, not feature approval: OBSERVED means read-only source fact, DOCUMENTED means design decision, NOT_TESTED means no behavioral proof. Overall GATE-1 remains NOT_TESTED because acceptance review is incomplete; no arbitrary architecture PASS.

| ID / objective | Evidence and current disposition | Required closure work |
|---|---|---|
| G1-01 Android execution | ADR0003 connectedDevice owner matches the official external-device use case. Manifest currently has only location-typed GpsService, not a backup service. DOCUMENTED candidate, NOT_TESTED implementation | Specify complete permission/start/stop contract and validate S25 lifecycle eligibility through authorized experiments; no inferred survival guarantee |
| G1-02 Network ownership | ApJoiner.onAvailable binds the process; proposed per-Network adapters separate camera/Internet. Official Network exposes bound HTTP/TCP/UDP APIs. OBSERVED baseline, target NOT_TESTED | Complete all traffic-path/redirect/DNS and callback ownership audit; prove network-loss fencing and S25 concurrency/fallback |
| G1-03 Ledger | Design covers identity, recording membership, frozen capture-day path, replicas, transactions and separate cleanup authority. Room not installed. DOCUMENTED only | Review schema uniqueness/migrations, cross-system URI journal and fencing; prove identity/enumeration assumptions and define migration/fault oracles before implementation |
| G1-04 Credentials | MainActivity reads/writes camera pass values in private SharedPreferences (lines 760,791,1915); no claim encrypted. OBSERVED | Review CredentialStore design below, migration/recovery semantics and secret-flow tests; no secret read/creation now |
| G1-05 Cleartext | network_security_config.xml globally permits cleartext. Required local camera HTTP does not justify cloud cleartext. OBSERVED | Define narrow enforceable endpoint boundary and assess numeric/dynamic camera-address compatibility; test rather than assume domain rules cover subnet |
| G1-06 Components/intents | Manifest exports launcher only; preview/GPS service/FileProvider non-exported. MainActivity consumes test extras without a visible build-type guard at those branches and can log supplied pin. OBSERVED attack-surface concern, exploitability NOT_TESTED | Audit external intent reachability, onNewIntent, debug-only controls, validated inputs and all mutation paths. Do not execute test hooks or inject secrets |
| G1-07 Diagnostics/privacy | Raw-string FileLog/logcat and gzip export remain baseline; count rotation is not byte/time bound. GPS/backup resource arbitration exists but telemetry independent behavior not established end-to-end. OBSERVED | Enforce proposed typed allowlist at every sink and explicit sanitized export; permission split, GPS OFF and verbose OFF tests remain pending |
| G1-08 Kotlin/cache | 1.9.24 is affected; no KAPT or remote task-cache config in repository. Existing measured local build-cache-disabled evidence retained. APPLICABLE_REDUCED_EXPOSURE, not fixed | Approve compatible patched toolchain/Room processor plan and CI trust policy before increasing cache/processor exposure; no upgrade now |
| G1-09 CI/dependencies/signing | setup-java cache:gradle is present; action references are major tags, test workflow has no explicit permissions block, release tag workflow requests contents:write. OBSERVED | Audit cache trust boundaries and token defaults, action/dependency pin/verification policy, separately define fork signing. No signing secrets or CI settings changed |
| G1-10 Android17 | Current target36; official guidance requires local-network permission at target37, not prematurely at36. DOCUMENTED strategy retained | Test Android16 compatibility restriction separately when hardware resumes, future target37 grant/deny/revoke; do not flash/change current phone |
| G1-11 Destructive boundary | Existing protocol/GUI deletes are not safety-gated project cleanup. Partial-page absence and handle identity limits remain. G9 accepted, NOT_TESTED | G1 reviews design only; actual destructive capability/TOCTOU/sidecar/replica proof belongs to G9 after predecessors, disposable test media and explicit authorization |

## 3. Credential handling design for GATE-1 review

Propose a platform Android Keystore-backed per-install key wrapping authenticated encrypted credential blobs in app-private storage, with unique encryption nonces and camera identity bound as authenticated context. Ledger carries opaque references, not secrets. Do not require per-use biometric unlock for ordinary already-authorized backup without evaluating screen-off behavior; enhanced cleanup confirmation is a separate optional preference. Hardware-backed key availability is a runtime property, not assumed from the phone model.

Migration must read old private preference only inside the app, durably store and verify decryptable ciphertext, then retire the old plaintext entry; crash leaves a recoverable migration journal, never silently loses working credentials. Key invalidation, restore/reinstall or decryption failure requires honest re-pair/re-entry, not fabricated bad-password diagnosis or automatic camera resets. Exclude key material/credential blobs from diagnostic exports and backup/transfer rules. allowBackup=false is an observed setting, not proof of every OEM data-transfer behavior. No key or credential was created/read/rotated in this task. [Android Keystore](https://developer.android.com/privacy-and-security/keystore) is the primary platform basis; cryptographic implementation/migration validation is outstanding.

## 4. Cleartext/component boundary design

Camera HTTP is restricted by the proposed adapter to validated paired-session endpoints on the owned camera Network, strict port/protocol allowlist, no cross-host redirect, bounded input/response parsing and explicit range/identity checks. It must never carry cloud credentials or arbitrary URL requests. Cloud/auth uses an independent TLS client and no camera routing context. Per-Network routing is not authentication or encryption and cannot defeat a hostile camera/AP by itself. Evaluate default-deny plus verified narrow platform exceptions; if dynamic numeric addressing prevents a safe platform rule, document residual risk and reviewed app-layer enforcement rather than assert protection already exists. [Network security configuration](https://developer.android.com/privacy-and-security/security-config) supports declarative policy; current XML is broader.

Future execution host stays non-exported, notification actions explicit and appropriately immutable; no external intent may mint cleanup approval. FileProvider is non-exported with temporary URI grants and an observed narrow `shared_logs/` external-cache path, but that does not sanitize content. Debug control extras need an explicit release-surface disposition. Existing no-source-change instruction prevents implementing these controls now.

## 5. Advisory disposition refreshed

Current [GHSA-r937-wjx7-w2jp](https://github.com/advisories/GHSA-r937-wjx7-w2jp) lists kotlin-gradle-plugin versions below 2.4.20-Beta1 affected, with that patched version; the repository pins 1.9.24. The primary [JetBrains fix](https://github.com/JetBrains/kotlin/commit/bf51df665b458fda7c3eaf436c4d88dc119d7ec6) restricts deserialized KAPT incremental-cache classes. No KAPT plugin/processors or org.gradle.caching/buildCache configuration found in the inspected repository; absence of KAPT reduces the reviewed attack-path exposure, not package applicability.

No new build ran, so runtime cache-disabled evidence refers to the historical isolated baseline only; arbitrary global overrides/current host cache contents are not newly attested. CI restores Gradle caches and must not be described as cache-free. Do not introduce KAPT/shared untrusted build metadata under this disposition. Later remediation must select a stable compatible patched toolchain and review processor/cache provenance before approval; advisory's first fixed beta is not an automatic version recommendation. No dependency changed and no blanket security PASS.

## 6. Primary platform references refreshed

- [Foreground service types](https://developer.android.com/develop/background-work/services/fgs/service-types): connectedDevice has explicit type/permission/runtime prerequisites; GPS location service is a separate permission/lifecycle concern. This supports the type choice, not proven background reliability.
- [Network](https://developer.android.com/reference/android/net/Network): network-specific HTTP, sockets and DNS support routing ownership; OEM concurrent connectivity remains a hardware question.
- [Room migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions): schema/migration testing supports the proposed persistence plan; no DB/schema has been implemented or tested.
- [Local network permission](https://developer.android.com/privacy-and-security/local-network-permission): target37 migration needs local-network authorization, while target36 should not add that permission; existing Android16 simulation plan retained, NOT RUN.

## 7. Open work and hardware questions

GATE-1 closure still needs a completed security/threat/permission review, credential/migration and cleartext boundary approval, exported-intent/debug-hook disposition, compatible dependency/processor and CI cache policy, ledger proof/validation plan and explicit ownership of remaining HIL criteria. Downstream feature absence is not itself a reason to implement during G1.

Separately scheduled hardware questions: S25 service/screen-off/permission and per-Network behavior; complete Pocket inventory/strong object and storage identity; capture timestamp/zone and mixed-MIME paths; sleep versus awake session-loss causes; GPS/offload arbitration; future delete addressing/sidecar effects. No experiment is resumed by this report. GATE-9 evidence later must use intentionally disposable media, never current travel originals.

## 8. Verification limits and next action

Governance-only scope, Markdown links, current policy consistency, requirement IDs, authorization/status flags and unchanged baseline files are checked before commit. YAML uses structural assertions, not a claimed full parser. No app tests/builds, exploit attempts, device logging, deletes or secret scans newly run. Earlier reports remain historical and are not edited to adopt later decisions.

Checks completed: 17 changed governance/state/evidence files; relative links and 41 unique requirement IDs pass; `git diff --check` exits 0 (existing CRLF conversion warnings only). No modified/deleted baseline tracked files and no historical GATE-0/completed-plan changes. Fresh read-only GitHub API reports zero open fork issues/PRs. `git ls-remote` confirms pre-publication branch b7a8224299be9bf219cc255d238e91cc90880977 and main 2fcdbc97e6dbefc875d425368be67cf32b50bb06. Handoff excluded.

Next: continue G1-01..G1-11 review and resolve design dispositions; seek explicit hardware return only when an experiment is needed. GATE-1 overall NOT_TESTED, GATE-9 NOT_TESTED, implementation_authorized=false, release_allowed=false. One documentation commit will persist the reconciliation and separately identified initial G1 evidence; remote identity/main checked after push.
