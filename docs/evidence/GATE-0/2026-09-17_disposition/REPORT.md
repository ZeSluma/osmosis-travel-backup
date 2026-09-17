# GATE-0 findings disposition — 2026-09-17

## Scope and decision

Analysis only, on `ZeSluma/osmosis-travel-backup`, branch `bootstrap/project-initialization`, starting at `63e4e5fd04e01d01e11072b0aab3463612390ddb`. Baseline main is `2fcdbc97e6dbefc875d425368be67cf32b50bb06`. GitHub API independently confirms fork main and upstream main still share this SHA; their test/fixture/lint source is therefore identical. Upstream was read-only. No application, dependency, Gradle, main or workflow changes were made.

**Disposition:** the 14 unit failures are ENVIRONMENT_SPECIFIC; clean LF baseline tests pass. The five lint errors are documented pre-existing debt, with permission review deferred to GATE-1. The Kotlin package advisory is APPLICABLE_REDUCED_EXPOSURE. No newly confirmed immediate software/security blocker was found for controlled unchanged-baseline verification. The only outstanding mandatory GATE-0 evidence category is actual S25 Ultra + Pocket 4P hardware verification, including connection, browse, original/large-file download and interrupted resume. GATE-0 remains BLOCKED until that evidence satisfies the criteria; no hardware PASS is inferred.

This acceptance is limited to measuring the unchanged baseline. It is not approval to ship, use the app as the sole backup, weaken INV-001 through INV-008, enable functional implementation, or declare known-good. Later hardware findings can introduce new blockers.

## 1. Unit tests — ENVIRONMENT_SPECIFIC

Original working checkout: `core.autocrlf=true`, from the bundled Git system configuration. core.eol and core.attributesfile are unset; there is no tracked .gitattributes or .git/info/attributes. Effective text/eol/working-tree-encoding attributes for golden files are unspecified. All 14 golden files are index LF / worktree CRLF. No project or global Git setting was changed.

ManifestGoldenTest reads golden resources as bytes, calls `trimEnd('\n')`, then splits on `\n`. CR from CRLF therefore remains in every expected row. The actual decoded records do not include CR. All original failing expected/actual assertions were compared in memory: removing only CR from expected makes each exactly equal to actual, with no field-value differences. No fixture names containing captured media metadata or full assertion dumps are republished. `test-mapping.json` records each exact test/file, CR counts, and matching baseline/current Git blob identities.

All tests below are in `dev.konraditurbe.osmosis.camera.ManifestGoldenTest`. Golden paths are relative to `app/src/test/resources/manifests/golden/`.

| Failing test | Exact golden file | Original expected CR count | LF rerun |
|---|---|---:|---|
| nano newest page | nano_45.bin.golden.txt | 45 | PASS |
| nano with favourites and bursts | nano_delete.bin.golden.txt | 45 | PASS |
| xtra newest page | xtra_13.bin.golden.txt | 13 | PASS |
| xtra full card | xtra_delete.bin.golden.txt | 45 | PASS |
| action 6 sd | oa6_sd_3.bin.golden.txt | 3 | PASS |
| action 6 internal | oa6_internal_2.bin.golden.txt | 2 | PASS |
| pocket 3 five with a star | op3_5_starred.bin.golden.txt | 5 | PASS |
| pocket 3 nine with a panorama | op3_9_pano.bin.golden.txt | 9 | PASS |
| pocket 3 nine stars moved | op3_9_stars_moved.bin.golden.txt | 9 | PASS |
| pocket 3 eleven panos | op3_11_panos.bin.golden.txt | 11 | PASS |
| pocket 3 fifteen | op3_15.bin.golden.txt | 15 | PASS |
| pocket 3 twentynine | op3_29.bin.golden.txt | 29 | PASS |
| pocket 4 | op4_45.bin.golden.txt | 45 | PASS |
| action 4 with stars | oa4_45.bin.golden.txt | 45 | PASS |

A separate temporary local clone outside the project was created with:

```text
git clone --no-hardlinks --no-checkout -c core.autocrlf=false -- <PROJECT_ROOT> <TEMP_LF_CHECKOUT>
git -C <TEMP_LF_CHECKOUT> checkout --detach 2fcdbc97e6dbefc875d425368be67cf32b50bb06
```

Before execution it was clean and all golden resources were index LF / worktree LF. The same verified JDK 21, SDK and isolated dependency cache from the baseline run were selected through process environment variables. In that checkout, the same command `./gradlew.bat testDebugUnitTest --console=plain --no-daemon` completed with exit 0 in 66.886 seconds: **265 tests, 0 failures, 0 errors, 0 skipped**, 40 suites, 25 executed tasks. Its tracked source remained clean after execution. See lf-checkout.json, lf-unit-tests.log and lf-test-summary.json.

The sandbox account could not initially inspect the temporary clone's Git status because the clone belonged to the execution user. The status/diff checks were rerun as that same owning user and succeeded; no global safe.directory exception was added.

Together, exact CR-only assertion differences, identical baseline blobs and the clean LF passing run establish CRLF conversion as the sole cause of these 14 observed failures in this environment. Upstream has the same parser and lacks the same line-ending policy: the portability weakness is upstream baseline debt, while the observed failure classification is ENVIRONMENT_SPECIFIC, not a project decoder regression or remaining PROJECT_BLOCKER. The original CRLF run remains FAIL in historical evidence; it is not relabeled PASS. Optional future test portability maintenance is separate from this disposition.

## 2. Five lint errors

All five have identical file blobs in HEAD and baseline origin/main; upstream main has the same commit. Raw lint severity remains Error. Disposition does not suppress or fix the errors.

| Rule ID | File and line | Description | Severity | Baseline unchanged | Category | Disposition |
|---|---|---|---|---|---|---|
| CoarseFineLocation | app/src/main/AndroidManifest.xml:13 | Fine location declared without coarse location | Error | Yes | Permission correctness / Android compatibility; security permission review | DEFER_TO_GATE-1 |
| UseAppTint | app/src/main/res/layout/activity_preview.xml:210 | android:tint where app:tint is required | Error | Yes | UI correctness / compatibility / maintainability | BASELINE_DEBT |
| UseAppTint | app/src/main/res/layout/activity_preview.xml:219 | android:tint where app:tint is required | Error | Yes | UI correctness / compatibility / maintainability | BASELINE_DEBT |
| UseAppTint | app/src/main/res/layout/activity_preview.xml:229 | android:tint where app:tint is required | Error | Yes | UI correctness / compatibility / maintainability | BASELINE_DEBT |
| UseAppTint | app/src/main/res/layout/item_camera.xml:60 | android:tint where app:tint is required | Error | Yes | UI correctness / compatibility / maintainability | BASELINE_DEBT |

The fine/coarse issue matters especially for the GPS permission path. On API >=31 the baseline camera scan requests Bluetooth scan/connect; the fine-location-only GPS request is a separate path. This does not prove camera connection succeeds on S25 Ultra. The required hardware test must establish that. If permissions prevent the required baseline flow, the observed runtime failure becomes a GATE-0 blocker. The four tint issues do not by themselves demonstrate data-loss or transfer failure. Their underlying UI behavior remains hardware-observable debt. The 79 warnings and 2 hints remain in the original lint evidence; lint as a tool result remains FAIL.

The active plan requires unit and static/lint **status to be known**, a reproducible debug build, documented security baseline and actual hardware evidence. It does not require zero upstream lint issues. TEST_PLAN explicitly says to record upstream lint failures without silently fixing them. These findings therefore do not independently require a source modification to close GATE-0.

## 3. Kotlin advisory — APPLICABLE_REDUCED_EXPOSURE

`build.gradle` pins `org.jetbrains.kotlin.android` 1.9.24, resolving the Kotlin Gradle plugin of that version. The refreshed [OSV record](https://api.osv.dev/v1/vulns/GHSA-r937-wjx7-w2jp) aliases CVE-2026-53914 and explicitly includes 1.9.24 in its affected range. The package-level advisory applies; no upgrade or NOT_APPLICABLE claim is made.

The linked [JetBrains fix](https://github.com/JetBrains/kotlin/commit/bf51df665b458fda7c3eaf436c4d88dc119d7ec6) restricts classes deserialized from KAPT incremental caches. That narrows the concrete reviewed attack path compared with the advisory's broad build-cache wording. This project applies Kotlin Android but not KAPT, declares no annotation processors, and has no KAPT tasks in the recorded compile/test run.

Cache distinctions:

- Gradle task-output build caching is disabled in the observed local invocation: `gradlew.bat help --info --console=plain --no-daemon` reports `Build cache is disabled` and exits 0. There is no org.gradle.caching property, --build-cache argument or buildCache settings block. No remote Gradle cache endpoint is configured.
- The isolated GRADLE_USER_HOME has no gradle.properties/init.gradle/init.gradle.kts or init.d overrides. Dependency downloads and ordinary Kotlin compiler incremental data are not proof that Gradle task-output caching is enabled.
- Baseline and LF rerun use a cache populated locally by this workflow from the configured repositories, not imported CI/team task-output or KAPT metadata. No untrusted/shared KAPT cache input was identified in this single-user local run. Local cache tampering would require an additional attacker write path; a fresh cache is exposure reduction, not a security boundary against a compromised host.
- CI explicitly uses actions/setup-java@v4 with `cache: gradle`, which [restores Gradle dependency caches](https://raw.githubusercontent.com/actions/setup-java/v4/README.md). Therefore a blanket statement that CI uses no shared caches would be wrong. It does not itself configure a remote Gradle task-output cache or enable KAPT. The workflows contain no custom cache import and no pull_request_target trigger. CI cache contents/poisoning resistance were not exhaustively audited and remain a Gate-1 supply-chain task.

Disposition: affected package, reduced current exposure, no demonstrated exploit path in the measured unchanged local workflow. Gate-1 must review the advisory, cache trust boundaries and compatible patched toolchain options before introducing KAPT/shared build caches or granting release trust. No blanket dependency-security PASS is recorded. [Gradle build-cache documentation](https://docs.gradle.org/current/userguide/build_cache.html) explains the distinction between opt-in build caching and ordinary execution caches.

## 4. Expanded security baseline

Official Gitleaks v8.30.1 was downloaded outside the repository and verified against the exact official release checksum before execution. An initial checksum comparison treated the HTTP byte-array response as text and failed; UTF-8 decoding resolved it, with no unverified binary execution. `gitleaks git --redact=100 --log-opts=--all --report-format=json --report-path=<TEMP>/scan.json .` exited 0 with **zero findings across reachable Git history**. Only version, checksum, command, count and sanitized locations would be recorded; secret values, author identities and raw report contents were not exposed. This expands the earlier narrow signature scan; it is still not proof that arbitrary secrets cannot exist in binary media, unreachable objects or unknown formats.

Source review confirms password responses are handled without logging the password value; GATT dispatch itself does not log notification bytes. Nevertheless camera MAC/SSID/media names and unknown protocol payloads reach logcat, and optional FileLog has no central redaction. This is pre-existing privacy debt, not evidence of a committed active secret or demonstrated remote exfiltration. Turning off Save logs alone does not eliminate logcat output. Ordinary app-private SharedPreferences stores Wi-Fi credentials; allowBackup=false reduces backup exposure but is not Keystore encryption. Exported launcher, non-exported services/provider and restricted shared_logs provider path remain as recorded.

No confirmed active credential exposure, malicious wrapper, unauthenticated Internet-facing service or immediate software blocker to controlled local baseline testing was identified. Logging/privacy, permissions, cleartext scope, credential storage, action pinning, dependency verification and the advisory remain mandatory GATE-1 review work. Full production hardening is not a GATE-0 claim.

For hardware evidence, use non-sensitive test media, retain camera originals, avoid optional GPS and raw log sharing, and sanitize any necessary diagnostic excerpts locally. These are constraints on evidence collection, not an invariant waiver or remediation of the underlying logging code. Any observed credential/GPS leak, false-success data loss, or failure of required connection/download/resume behavior must be recorded as a new blocker before gate closure. Current source integrity/idempotency limitations remain recorded baseline debt and feed GATE-1 design and GATE-2/3 requirements; independent checking of test transfers is still necessary.

## 5. Gate disposition and follow-up

| Item | Measurement | Disposition for GATE-0 |
|---|---|---|
| Source identity / unchanged build | Verified; debug build PASS | Satisfied |
| Wrapper integrity | Official JAR checksum PASS | Satisfied; checksum-pinning improvements remain later debt |
| Unit tests | CRLF run FAIL; clean LF baseline 265/265 PASS | Satisfied with documented environment condition |
| Static analysis | FAIL, 5 errors / 79 warnings / 2 hints | Status known; documented upstream debt, not silently converted to PASS |
| Security/dependency/secret baseline | Expanded review with open debt; no immediate confirmed blocker found | Baseline documentation satisfied, security clearance not granted |
| Actual S25 Ultra + Pocket 4P baseline | NOT_TESTED | Mandatory remaining blocker |
| Known-good / implementation / release | null / false / false | Unchanged |

Accepted baseline debt: LF-sensitive golden loader/no repository EOL policy; four app:tint lint errors; existing permissions/security/integrity limitations for measurement only. GATE-1 owns permission review (including coarse/fine), privacy/redaction, credential/cleartext policy, Kotlin advisory/toolchain plan, CI cache trust/pinning and architecture decisions. This document accepts no release risk on the user's behalf.

**Only the target-hardware evidence category remains mandatory for GATE-0 after this software disposition**, provided its results are satisfactory and the documented conditions hold. That category includes install/launch, connect/browse, original and large-file downloads, independent completion/integrity checks and interrupted resume behavior. There is no automatic gate closure: review persisted hardware evidence against the unchanged active-plan criteria first. If a hardware requirement fails, keep the gate open and record the new finding.

| Mandatory gate-report field | Result |
|---|---|
| GATE | GATE-0 |
| IMPLEMENTATION | Governance/evidence only; functional implementation NOT AUTHORIZED |
| UNIT TESTS | PASS in isolated LF baseline; original CRLF FAIL retained |
| REGRESSION | 265 unit tests PASS; hardware/failure-matrix NOT_TESTED |
| SECURITY | Baseline reviewed with deferred findings; no production PASS |
| HARDWARE TEST | NOT_TESTED |
| OPEN FINDINGS | Hardware evidence blocker; documented baseline/GATE-1 debt |
| EVIDENCE | This directory plus original baseline evidence |
| GATE STATUS | BLOCKED |
| NEXT ACTION | Collect and review actual S25 Ultra + Pocket 4P baseline evidence |
