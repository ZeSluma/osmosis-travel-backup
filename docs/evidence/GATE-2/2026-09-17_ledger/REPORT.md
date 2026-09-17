# GATE-2 verification report

Date: 2026-09-17. Software foundation implemented; target-hardware validation remains NOT_TESTED and the overall gate is BLOCKED pending explicit hardware resume. Continuous execution policy is persisted in AGENTS.md; this stop is a real hardware dependency, not a gate-boundary permission request. No main modification/push, merge, release or camera/media mutation.

## Provenance and scope

Branch gate-2/persistent-ledger-sync-planner was created from freshly fetched/independently queried G1 remote tip a58d858ecfd404579960d9a32960e151071f9fd3. Merge-base equals parent. Main remains 2fcdbc97e6dbefc875d425368be67cf32b50bb06; origin is ZeSluma/osmosis-travel-backup. Upstream KonradIT/osmosis remains read-only. Fork open-issues endpoint returned no open items at bootstrap; workflows remain unchanged. G0/G1 PASS retained. Local handoff and generated .kotlin/build outputs excluded.

Feature commit: 986e71a0cb90a6e780ebb5951fe04f9106099284. It includes Room/KSP configuration, schema1/2, ledger/domain/enumerator/coordinator, credential migration and backup rules, small MainActivity integration, localized secure-storage failure text and accompanying tests. Protocol packet modules, transfer engine, GPS service, network configuration, wrapper and existing test fixtures remain unchanged. Source-versioned schema exports contain structure only, never operational records.

## Implemented truth and limitations

- Room2.8.5/KSP2.3.12, explicit additive1→2 migration, seven tables, unique source/fingerprint/path keys and restrictive FKs. Nullable local locator is reserved for later phone IO; no destructive upgrade/downgrade/reset. Corruption is preserved and blocks access. No migration/restore can manufacture verification.
- Durable per-source owner epoch plus request identity and one application-context writer. Duplicate requests/scans do not duplicate asset/group rows; stale callbacks fail. New snapshots retain historical absent assets. SQL rollback and actual SQLiteFullException are tested with synthetic data, not by filling the PC/phone.
- Composite candidate identity includes source/store/path/size/time/type/handle/version. Pocket immutable volume/version evidence is unproved; equal names/metadata remain ambiguous across observations and require revalidation. No real identity assurance is inferred from synthetic strong-version tests.
- Six classes, preserved unknowns, explicit recording links and missing-required-member placeholders. Similar names never fabricate relationships. Group/source/local completeness are separate predicates.
- Capture-source priority, timezone/offset evidence, explicit uncertain fallback and immutable day/path reservations. Group inputs use parent capture evidence; conflicts are rejected or recorded. No date recalculation on retry/restart. Actual phone folders/media allocation are not implemented by G2; no existing file is moved.
- UI-independent bounded read-only pagination and reconciliation compute missing/new/partial/revalidation/review plans. Repeated/stalled/conflicting pages cannot seal enumeration. Current Pocket adapter cannot prove all sidecars/stores/stable generation; its snapshots stay INCOMPLETE. Full one-open inventory/UX remains later-gate work.
- No production LOCAL_VERIFIED promotion. Transfer success is at most TRANSFERRED_UNVERIFIED; no adoption by filename/size/MediaStore. Planner supports skipping genuinely current verified proof in pure policy tests; G2 production has no G3 verifier and therefore does not claim verified existing phone copies.
- SC01 per-camera Keystore/AES-GCM migration, explicit file/directory fsync, atomic ciphertext write/decrypt verification before retiring legacy plaintext. Off-main serialized credential work and request fencing; unavailable keys/storage fail closed. No credential column or GPS/media payload introduced. Legacy credentials migrate lazily when that camera is selected; unselected legacy values remain excluded from backup. Broader existing all-sink diagnostics and camera/network privacy controls retain their assigned later gates.

## Reproducible software verification

Toolchain is isolated Temurin21.0.12.1, SDK36, Gradle8.14.5, AGP8.13.2 on Windows. Kotlin Gradle plugin actually resolves2.4.20; processor/runtime coordinates are in resolved-dependencies.json. All Gradle commands used --no-build-cache --console=plain --no-daemon and trusted local dependency caches; no shared/untrusted Gradle build cache.

Authoritative checkout: fresh temporary clone with core.autocrlf=false; initial feature commit and final code commit93576f6e53b9f4ee52d84a8cc9c63a66af92df48 tested. Test strengthening commitafb21897e0c0be9a2eb631298839fe2ce853f4cc asserts actual SQLiteFullException. No history rewritten. Main project fixtures were not changed to make tests pass. Command:

```text
./gradlew.bat help :app:assembleDebug :app:assembleDebugAndroidTest :app:compileReleaseKotlin :app:testDebugUnitTest --no-build-cache --console=plain --no-daemon
```

Initial exit0,70s,91 tasks executed; final code regression exit0,39s,18 tasks executed/73 up-to-date. **313 tests,0 failures,0 errors**:265 unchanged baseline +8 B1 +40 G2 (31 domain/planner/capture and9 enumeration/adapter). unit-results.json lists suites. Debug APK from this LF build SHA256: 461C05C4462D0F8F85F776425D2470707C975731C41F5E5FEA4201657ECE89A1. Application ID dev.konraditurbe.osmosis, version1.4.4/code29; debug artifact only, no release produced.

```text
./gradlew.bat :app:lintDebug --no-build-cache --console=plain --no-daemon
```

Initial exit1,38s; final code repeat exit1,34s: exactly five accepted baseline errors (CoarseFineLocation and four UseAppTint),78 warnings/two hints. No new lint error or suppression. lint-errors.json retains rule/file/line. Lint remains FAIL with documented baseline debt; it is not relabeled PASS.

## Android execution (isolated API36 x86_64 emulator only)

Every adb command explicitly selected emulator-5580. No physical-device enumeration. Installed exact LF debug/test artifacts. Commands:

```text
adb -s emulator-5580 install -r <LF>/app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5580 install -r <LF>/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5580 shell am instrument -w -e ledgerPhase suite dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
adb -s emulator-5580 shell am instrument -w -e ledgerPhase persist dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
adb -s emulator-5580 shell am force-stop dev.konraditurbe.osmosis
adb -s emulator-5580 shell am instrument -w -e ledgerPhase restore dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
adb -s emulator-5580 shell am instrument -w dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
adb -s emulator-5580 shell bmgr backupnow dev.konraditurbe.osmosis
```

Install success; all instrumentation phases PASS. Final test-only commit f4d39b9ec8012f5e109cf6293604b48e0a1777e4 adds database-level capture/identity cases; rebuilt test APK passes against the exact final functional LF APK. Actual Room tests cover persisted two-day/parent grouping, travel-zone/partial path stability, ambiguous identical reuse, empty inventory; primary/audio/missing member; photo/RAW/metadata; repeated scans/requests; new/removed/changed files; unknown/excluded records; partial/unverified truth; repo/Activity/process recreation; stable paths/groups/unknown ambiguity; concurrent requests/fencing; FK/transaction rollback; constrained-capacity failure; corruption preservation; additive migration retaining partial/member/path/classification; unsupported version preservation. Backup manager explicitly returned `Backup is not allowed`; actual OEM D2D is still HIL NOT_TESTED. B1 normal/cold/warm hostile-launch assertions remain PASS.

Synthetic Keystore tests cover migration interruption at four boundaries, durable reopen, isolated cameras, fresh nonce, swapped/tampered ciphertext rejection, missing key and explicit re-entry, and scoped synthetic forgetting. No real credential read/export/rotation and no secret test values in reports. Android instrumentation is a separate test APK; no production debug command entry point exists.

## Security and dependency disposition

Both merged variants have seven components: the prior six plus Room MultiInstanceInvalidationService, non-exported. It is an internal dependency service, not the G7 execution host; multi-instance invalidation is not enabled. Exported surfaces remain MainActivity and the DUMP-protected ProfileInstallReceiver; B1 policy unchanged. components.json is the inventory. No permissions or network policy broadened. DB/WAL/journals/blobs remain private/no-backup, with explicit legacy/cloud/D2D exclusions.

Gitleaks8.30.1 redacted scan of a58d858..f4d39b9: exit0,4 commits,106509 bytes, no leaks found. Scope is the new committed diff, not a new blanket full-history claim. New ledger/credential code contains no raw logging sink; operational names/paths exist only where identity/path planning requires them. Baseline raw diagnostics are not certified by this scoped result.

OSV queried226 resolved unique Maven coordinates. No returned advisory for newly introduced coordinates or the Android runtime graph.44 distinct advisory IDs affect11 **unchanged parent build-classpath** coordinates (Netty, Commons Compress, jose4j, Bouncy Castle, JDOM). They concern parser/protocol/resource/crypto behavior in the build toolchain; the affected libraries are not packaged in the Android runtime. Parent resolution independently reproduced at a58d858; advisory-disposition.json proves identical versions and scopes. This expands disclosed existing build-tool debt, not a new G2 runtime regression. No unsafe blanket version forcing: toolchain remediation belongs to reviewed build/CI hardening before release. Current builds use trusted source/artifacts and no remote/untrusted build cache; no release/secret-bearing workflow was run. No claim of exploit-unreachability for every build-tool code path. OSV results are point-in-time evidence, not proof of absence of vulnerabilities.

Room necessarily adds AndroidX SQLite/collection/annotations and raises runtime coroutines1.6.4→1.8.1 through dependency resolution; runtime-dependency-delta.json records the complete change. No direct opportunistic upgrade. Full compilation/unit/B1/emulator regression passes; actual S25 behavior remains pending. Source/operating-system/shared-user compromise is outside the assurance of app-private storage and Keystore.

## Disposition

Original failed attempts/corrections are retained in ITERATIONS.md. Automated software foundation meets the requested G2 boundary; physical integration is NOT_TESTED. Overall **GATE-2 BLOCKED** solely awaiting explicit S25/Pocket return and the checks in HARDWARE_PENDING.md. Hardware pause and unresolved sleep/foreground-drop causes are unchanged. Continuous progression is authorized in principle, but mandatory predecessor/hardware evidence cannot be skipped; no G3 work started. Release remains false.


## Publication evidence

Code/test tip f4d39b9ec8012f5e109cf6293604b48e0a1777e4 was pushed only to the G2 branch and independently matched by Git ls-remote and GitHub branch API. G1 remains a58d858ecfd404579960d9a32960e151071f9fd3 and main remains 2fcdbc97e6dbefc875d425368be67cf32b50bb06. The following governance/evidence commit is published on the same branch and verified after creation; its SHA is reported externally to avoid self-reference. No merge, tag, release or upstream push.
