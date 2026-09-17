# GATE-0 unchanged baseline — 2026-09-17

## Identity and scope

Repository: ZeSluma/osmosis-travel-backup. Branch: bootstrap/project-initialization. Measured governance tip: `5582f3f71d66ddeb225ee59f96de03f94a19b3e0`. Baseline, local main, fetched origin/main and merge-base: `2fcdbc97e6dbefc875d425368be67cf32b50bb06`. The application/build/protocol/workflow source is unchanged from that baseline. The two commits above main before this run contain governance only.

`git fetch origin` succeeded. GitHub API reconfirmed parent/source KonradIT/osmosis, unchanged upstream main, latest upstream release v1.4.4 (2026-09-07), and open work #40/#32/#24/#11/#8. No upstream mutation, main modification, merge or release was performed. CODEX_HANDOFF_ALL_IN_ONE.md remains local and untracked.

## Environment and preparation

Windows 11 Pro 25H2 build 26200.9457, x64; Git 2.53.0.windows.3. Windows registry ProductName still reports Windows 10 Pro; OS build and Gradle identify Windows 11. Initial PATH Java was Temurin 11.0.17+8 with no JAVA_HOME. No SDK was detected via SDK environment variables, PATH tools or standard installation directories.

Minimum installed tooling: official Temurin JDK 21 archive; Google Android command-line tools; Android platform 36 revision 2, build-tools 35.0.0 and platform-tools 37.0.1. Installed under `<TEMP>/osmosis-gate0-toolchain`, outside the repository, with process-only environment selection and isolated Gradle cache. No system PATH, repository source/configuration, dependency versions or local.properties changes. JDK and Android tools checksums match their official metadata/download page. Standard SDK licenses were accepted for the authorized tool setup. See tool-installation.json, sdk-tools-integrity.json and sdk-packages.txt.

The unchanged wrapper JAR matches the official Gradle 8.8 JAR checksum. Initial comparisons to the configured distribution version 8.14.5 and historical 7.5.1 did not match; the official catalog resolved its actual origin. A wrapper JAR can launch a different Gradle distribution. No wrapper replacement was made. The configured 8.14.5 runtime started successfully on JDK 21. The repository does not pin distributionSha256Sum; this remains a supply-chain observation, not a claimed distribution-checksum PASS.

## Command outcomes

All build commands ran from the repository root against unchanged source, with JAVA_HOME selecting the temporary JDK 21, ANDROID_HOME selecting the temporary SDK, and GRADLE_USER_HOME selecting the isolated cache. Commands and original failures are preserved in sanitized logs. No rerun with fixes or suppressions was performed.

| Command/check | Exit | Seconds | Result |
|---|---:|---:|---|
| git fetch origin | 0 | 0.662 | PASS |
| official wrapper JAR SHA-256 comparison | 0 | not timed | PASS, official 8.8 JAR |
| sdkmanager.bat --sdk_root=<TEMP>/osmosis-gate0-toolchain/android-sdk "platforms;android-36" "build-tools;35.0.0" "platform-tools" | 0 | 25.437 | PASS |
| .\gradlew.bat --version | 0 | 17.799 | PASS, Gradle 8.14.5 / JDK 21.0.12.1 |
| .\gradlew.bat assembleDebug --console=plain --no-daemon | 0 | 132.232 | PASS |
| .\gradlew.bat testDebugUnitTest --console=plain --no-daemon | 1 | 77.858 | FAIL, 265 tests / 14 failures / 0 skipped / 0 errors |
| .\gradlew.bat lintDebug --console=plain --no-daemon | 1 | 66.705 | FAIL, 5 errors / 79 warnings / 2 hints |

Build artifact: app/build/outputs/apk/debug/app-debug.apk; SHA-256 `58e5cfde7e8398660db997dd48f69e37eef1611311130480fb98a03762ab1a3b`. APK remains an ignored local debug artifact, not a release or committed binary. One successful build demonstrates local build feasibility, not bit-for-bit reproducibility across repeated builds/machines. SDK XML-version and Gradle deprecation warnings are recorded in the logs.

## Original unit-test failure

All 14 failures are ManifestGoldenTest assertions at line 54. `core.autocrlf=true`; Git reports golden resources as index LF / worktree CRLF. The test reads bytes, trims only LF and splits on LF, leaving CR in expected records. For every failed assertion, expected and actual become exactly equal after removing CR from expected, verified in memory without editing source or fixtures. This strongly isolates the observed failure to checkout line-ending sensitivity; it is not a passing rerun. 251 tests passed. Fixture media names/record dumps and raw XML are excluded from committed evidence; the sanitized summary retains test identities and the CR-comparison evidence.

## Original lint failure

- CoarseFineLocation: AndroidManifest.xml:13 lacks ACCESS_COARSE_LOCATION alongside ACCESS_FINE_LOCATION.
- UseAppTint: activity_preview.xml:210,219,229 and item_camera.xml:60 use android:tint where lint requires app:tint.
- 79 warnings and 2 hints include API, resource, dependency-age and other static observations. Full issue IDs, severities and locations are retained in lint-summary.json. No suppression, baseline file, permission edit or resource fix was applied.

## Security, dependencies and source

See SOURCE_SECURITY.md, dependency-advisories.json, kotlin-advisory.json, secret-scan.json and secret-history-scan.json. Review completed with open findings: unsafe Kotlin build-cache deserialization advisory for pinned plugin 1.9.24; global cleartext HTTP; credential storage; identifying metadata/raw protocol logging; mutable CI action tags; absent artifact verification metadata; completion/idempotency weaknesses. Selected secret signatures found no matches, with stated binary/history/entropy limitations. This is not a security PASS or full transitive dependency audit.

Source reconciliation: persistent resume SharedPreferences and MediaStore completed-copy lookup already exist. The stateless grid-tick roadmap entry is not proof that all downloaded state is nonpersistent. Activity-started transfer Thread and lack of a durable backup ledger remain confirmed. Pocket 4/4 Pro upstream labels and fixture coverage do not establish this project's hardware gate.

## Gate report

| Field | Result |
|---|---|
| GATE | GATE-0 |
| IMPLEMENTATION | No functional source changes; NOT AUTHORIZED |
| UNIT TESTS | FAIL, 14/265 |
| REGRESSION | FAIL in unchanged Windows golden tests; other failure matrix NOT_TESTED |
| SECURITY | INITIAL_REVIEW with documented open findings; not security clearance |
| HARDWARE TEST | NOT_TESTED; actual S25 Ultra + Pocket 4P required |
| OPEN FINDINGS | Windows golden line endings, five lint errors, security/dependency findings, hardware and runtime integrity/resume evidence |
| EVIDENCE | This directory; sanitized outputs only |
| GATE STATUS | BLOCKED; hardware missing and observed software failures remain unresolved |
| NEXT ACTION | Review recorded baseline failures and agree their disposition; then perform actual target-hardware verification. No functional fixes in this run |

Known-good version/commit remain null. implementation_authorized=false and release_allowed=false. Fork release signing remains a separate release prerequisite, not by itself a GATE-0 blocker. Evidence commit identity is supplied by Git history; only governance/state/evidence files are eligible for this commit and push.
