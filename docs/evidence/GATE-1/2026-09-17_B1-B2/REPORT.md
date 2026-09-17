# GATE-1 B1/B2 narrowly authorized security implementation

Date:2026-09-17. PRE-FLIGHT passed before mutation: local and remote bootstrap `efb4830e435b1efeef7a6de56fde0ea609806171`, main `2fcdbc97e6dbefc875d425368be67cf32b50bb06`; G0 PASS and G1 blocked only by B1/B2. No functional delta from baseline and no unrelated changes; local untracked handoff excluded. Branch `gate-1/security-foundation` created from verified bootstrap, merge-base with bootstrap equals that parent. Main/upstream untouched. Current explicit authorization supersedes the earlier documentation-only restriction for B1/B2 only; general implementation and release remain false.

## B1 implementation and proof

[Before/after reconstruction](B1_SURFACE.md) covers every previously consumed launcher input. Commit `7ed2a4afbb1dc1585b48f1693914076e792a4fed` removes external test commands from both variants, strips retained input, shares a fail-closed shortcut policy across cold/warm entry, and requires in-app confirmation before a validated saved-camera hint can initiate connection. No debug command component or signature permission added. Unneeded ADB test controls are removed; ordinary UI and tests remain available. A legitimate shortcut still offers its known camera, with explicit confirmation because the launch input can be forged.

Eight focused policy tests pass. Platform-only instrumentation (no additional dependency) covers normal launch, three hostile warm launches and malformed cold launch; assertions check clean retained intent, unchanged pairing token, no auto-connect target/connection, no confirmation for invalid inputs and unchanged protocol debug controls. Tests use an empty isolated API36 x86_64 emulator, not S25/Pocket. Debug/release share this source with no build guard, and both compile; the release manifest contains no debug interface. This is release-like source/manifest evidence plus actual debug-artifact Android execution, not a signed-release or target-hardware test.

Fresh merged manifests in both variants contain the same six components: exported MainActivity (MAIN/LAUNCHER, no permission); non-exported MediaPreviewActivity, location GpsService, FileProvider and AndroidX InitializationProvider; exported AndroidX ProfileInstallReceiver protected by android.permission.DUMP with its four profile/benchmark actions. No new exported component. FileProvider remains limited to shared_logs/ with temporary read sharing; existing GpsService STOP PendingIntent remains explicit and immutable. No change to GPS, provider, protocol commands, transport, downloads or deletion behavior.

Exact emulator-only commands (tool root abbreviated as `<SDK>`):

```text
<SDK>/emulator/emulator -avd Gate1Security -port 5580 -no-window -no-audio -no-snapshot -no-boot-anim -gpu swiftshader_indirect
<SDK>/platform-tools/adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk
<SDK>/platform-tools/adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
<SDK>/platform-tools/adb -s emulator-5580 shell am instrument -w dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
<SDK>/platform-tools/adb -s emulator-5580 shell am start -W -n dev.konraditurbe.osmosis/.ui.MainActivity -a untrusted.internal.COMMAND --es pin SYNTHETIC_TEST_ONLY --ez wifi true --ez offload true --ei shortcut_mac 12345
<SDK>/platform-tools/adb -s emulator-5580 shell am start -W -n dev.konraditurbe.osmosis/.ui.MainActivity -a android.intent.action.VIEW --ez pageforce true --ei pagesize 2147483647 --ei shortcut_mac 12345
```

Initial B1 instrumentation: PASS, installations exit0. Separate shell-UID cold/warm explicit starts: status ok, process remains alive. Post-Kotlin repeat encountered the emulator's outstanding Bluetooth permission UI and stalled; it was explicitly force-stopped (the runner reported Process crashed because of that operator stop). No AndroidRuntime crash was observed. Granting BLUETOOTH_SCAN/BLUETOOTH_CONNECT to this empty emulator installation and dismissing the dialog allowed the same harness to PASS. No GPS grant or physical-device setting changed. Do not report the interrupted run as PASS or as a reproduced app crash. Emulator installation initially needed its temporary image-path corrected; repository SDK/source settings were not changed.

## B2 implementation and resolved graph

Commit `d2bbb3bbd7ea4ec584722fa012799db7b57a55e3`: Kotlin Android plugin1.9.24 -> stable2.4.20, comment updated; two explicit existing nullable-Int type annotations described below. AGP8.13.2, Gradle8.14.5, JDK21.0.12.1, compile/target36, JVM21 and every other direct dependency remain unchanged. No KAPT/KSP/annotation processor introduced. No new product architecture or dependency cascade.

[GHSA-r937-wjx7-w2jp / CVE-2026-53914](https://github.com/advisories/GHSA-r937-wjx7-w2jp) identifies the affected plugin range and first fixed beta; stable2.4.20 is the approved candidate. Before editing, Maven Central's exact plugin POM returned HTTP200/artifact kotlin-gradle-plugin/version2.4.20. [Kotlin compatibility](https://kotlinlang.org/docs/gradle-configure-project.html) encompasses current Gradle/AGP. Actual root `buildEnvironment` resolves `org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20`; the Android plugin marker points to it. `:app:dependencies --configuration debugRuntimeClasspath` resolves runtime dependencies including Kotlin stdlib. Closure uses actual resolution/build evidence, not just build-file text. Gradle --version reports embedded Kotlin2.0.21; that is Gradle's own Kotlin, not the application's resolved KGP.

Cache assumptions: isolated locally populated Gradle home, no KAPT or remote task-output cache configured, verification explicitly uses --no-build-cache. Dependency caches still exist; no cache-free claim. Fixed plugin does not establish CI cache provenance or eliminate mutable action/signing risks. Those remain reviewed later hardening/release prerequisites.

## Original failures retained and compatibility adjustment

First B2 LF run at preliminary unpushed commit aa1e08cd9dd818ed55cf6c685fb5a7fe21d5f0a1: all273 unit tests pass, but lint15 errors/79 warnings/2 hints. Ten new StringFormatMatches findings (two call sites times five locales) classified the inferred nullable result of `runCatching { dl.deleteFiles(...) }.getOrNull()` as Object when formatting existing `%x` error text. The interface already declares `deleteFiles(...): Int?`; the existing when handles null separately. No camera delete was executed.

Strictly necessary B2 compatibility adjustment: explicitly declare those two local variables `val status: Int?`. No command, handle, branch, string, exception policy or operation behavior changes; no lint suppression/baseline rewrite. Lint then returns to the original five errors. The unpublished B2 commit was amended to contain that minimal adjustment; original failure is preserved here. No unrelated library upgrade or cleanup implementation occurred.

## Reproducible verification commands

Process environment only: JAVA_HOME=<TEMP>/osmosis-gate0-toolchain/jdk-21.0.12.1+1, ANDROID_HOME=<TEMP>/osmosis-gate0-toolchain/android-sdk, GRADLE_USER_HOME=<TEMP>/osmosis-gate0-toolchain/gradle-cache. No global or project toolchain configuration change.

```text
./gradlew.bat :app:testDebugUnitTest --tests '*LauncherInputPolicyTest' :app:assembleDebug :app:processReleaseMainManifest --console=plain --no-daemon
./gradlew.bat :app:assembleDebugAndroidTest :app:compileReleaseKotlin --console=plain --no-daemon
./gradlew.bat --version
./gradlew.bat :app:buildEnvironment :app:assembleDebug :app:assembleDebugAndroidTest :app:compileReleaseKotlin --no-build-cache --console=plain --no-daemon
git clone --no-hardlinks --no-checkout -c core.autocrlf=false -- <PROJECT_ROOT> <TEMP_LF_CHECKOUT>
git -C <TEMP_LF_CHECKOUT> checkout --detach <B2_COMMIT>
git -C <TEMP_LF_CHECKOUT> ls-files --eol app/src/test/resources/manifests/golden
# In exact B2 LF checkout:
./gradlew.bat assembleDebug testDebugUnitTest lintDebug buildEnvironment :app:dependencies --configuration debugRuntimeClasspath --continue --no-build-cache --console=plain --no-daemon
```

B1 targeted build/tests exit0 in67s; harness/release compile exit0 in47s. First B2 compile/package exit0 in98s. First combined LF unit/lint/dependency run exits1 in85s solely for recorded lint15; typed working-tree lint exits1 in56s with the five baseline errors. Final exact-candidate results and sanitized summaries are recorded in VALIDATION.json. No raw media, passwords, GPS, tokens or host-identifying test XML is persisted. LF resources verified index/worktree LF; original project Git EOL configuration/fixtures untouched.

## Full GATE-1 re-check disposition

| Area | Current disposition |
|---|---|
| Credentials | Reviewed per-camera Keystore/migration model retained; implementation SC01/SC02 deferred, no credential read/change |
| Cleartext | Reviewed finite-IP plus per-Network boundary retained; existing implementation debt, SC03 deferred |
| Network/execution | Official-API-backed service/Network ownership retained, no new service/transport; runtime G7 NOT_TESTED |
| Components/intents | B1 surface removed; shared validation/confirmation and merged variants/security tests reviewed |
| Diagnostics | Supplied-PIN launcher log removed; broader typed bounded diagnostics remain later implementation, no raw capture |
| GPS | Optional/separate model unchanged; no provider/service/permission change, known lint debt retained |
| Dependencies | Actual fixed KGP resolution/build/tests verified; B2 compatibility scope limited to version and two types |
| CI/supply chain | No workflow changes; action pins/cache trust/fork signing remain documented later prerequisites |
| Android17 | target37 plan unchanged, no premature permission addition |
| Ledger | Minimized/private/no-backup model unchanged; no Room, DB or migrations implemented |

The limited B1/B2 authorization is exhausted after these security changes. No automatic G2 start. Target S25/Pocket lifecycle/network/identity/sidecar/capture-day/sleep/replica/cleanup tests remain assigned later and NOT_TESTED; hardware remains paused. G0 baseline evidence and known-good baseline identity stay historical and unchanged. Only the dedicated security branch is published, never main or bootstrap; no merge/release.

## Final acceptance and scope audit

Final exact B2 commit: debug assembly PASS;265/265 original LF unit tests plus8/8 security tests PASS, zero skipped/errors/failures. Combined command exits1 in63s because lint retains CoarseFineLocation once and UseAppTint four times;79 warnings/two hints, zero new errors. No original finding relabeled PASS. Final release compilation/test-APK packaging exit0 in41s. The exact final LF debug APK passed the emulator harness again; the isolated emulator was then stopped. [VALIDATION.json](VALIDATION.json), [resolved coordinate evidence](resolved-kotlin.txt) and [both merged component inventories](components.json) persist sanitized facts.

Scope audit: B1 changes only MainActivity entry/test-hook consumers, shared input policy, shortcut documentation, two confirmation strings in five existing locales, eight regression tests, platform-only instrumentation and its runner declaration. B2 changes only root Kotlin version/comment and two existing nullable result type declarations. No protocol module, GPS service, transfer engine, manifest permissions, network configuration, dependency other than Kotlin, workflow, wrapper, main or upstream source outside this allowlist changed. No camera original/completed phone file accessed or altered. Handoff remains local/untracked. No unrelated working changes or generated artifacts staged.

Governance relative links, state assertions, unique YAML root keys and whitespace checks pass (structural YAML validation, not a full parser). Historical evidence remains intact. B1 CLOSED; B2 CLOSED; no additional immediate G1 blocker identified. **GATE-1 PASS**, general product implementation_authorized=false and release_allowed=false. NEXT ACTION: await explicit GATE-2 authorization; no new work or physical hardware testing starts automatically. Publication result is independently checked and returned after the evidence commit; no self-referential commit SHA is embedded here.
