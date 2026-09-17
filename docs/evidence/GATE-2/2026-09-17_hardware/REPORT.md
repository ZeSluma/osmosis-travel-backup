# GATE-2 target-hardware validation — active session

## Latest checkpoint (supersedes pending statements in historical sections below)

[New recording evidence](NEW_RECORDING_TIMESTAMP_EVIDENCE.md): snapshot5 has3 assets, exactly one new DISCOVERED/ABSENT asset with DOWNLOAD plan only; existing2 unchanged. Timestamp audit reports null remoteTime, unverified filename digits, explicit sync-time fallback and reserved relative path. No camera-offset/embedded-metadata evidence. Separate green-light-ON IDLE_OR_STANDBY_LIKE_STATE remains unclassified. This supersedes new-asset checks described as pending below; capture-time correctness/coverage limits remain unresolved.

Capture-time attribution correction: [external reference only](CAPTURE_TIME_REFERENCE_CORRECTION.md). Pocket did not display recording date/time for the new test recording. The historical17:26 connection-test note is not its capture-time reference. User reference is approximate external context, not camera metadata or an exact-minute oracle. New short recording is user-reported but not yet present in the latest ledger audit; new-asset timestamp fields remain unobserved.

Verified schema3 reconciliation APK installed; target legacy-local adoption, repeated enumeration without duplicate assets, and persisted asset/group/capture-path/local-reference/state stability across app process restart PASS for the2 known videos. Both retain one local candidate and VERIFY_EXISTING, never LOCAL_VERIFIED. Protected sizes38447651 and3071380142 unchanged. [Recovery/persistence evidence](CAMERA_UNREACHABLE_UNTIL_POWER_CYCLE.md) links exact audit and comparisons. New-media planning and capture-time/type/relationship hardware disposition remain open; overall G2 BLOCKED. Saved-entry reconnect, connected-but-empty enumeration and camera-unreachable-until-power-cycle are three separate findings; causes unconfirmed, future G7 investigation. Earlier missing-local-association findings below are historical, superseded by schema3 evidence; do not erase original failures.

The user explicitly resumed non-destructive S25 Ultra + Pocket 4P validation on 2026-09-17. Prior pause is lifted for this scope. No real-media re-download, deletion, overwrite or content inspection is needed for the initial ledger checks. GATE-2 remains BLOCKED pending required observations; hardware is NOT_TESTED except the preflight facts below. Historical sleep/background/foreground root-cause uncertainty is unchanged.

## Preflight

- Branch: `gate-2/persistent-ledger-sync-planner`.
- Local/published branch: `6afddd7e292f3bdd27b652c6ec94cc5dcf6ddd15`, verified by `git ls-remote origin refs/heads/main refs/heads/gate-2/persistent-ledger-sync-planner` (exit 0).
- Origin: `https://github.com/ZeSluma/osmosis-travel-backup.git`.
- Remote main unchanged: `2fcdbc97e6dbefc875d425368be67cf32b50bb06`.
- Production source identical to final functional commit `93576f6e53b9f4ee52d84a8cc9c63a66af92df48`; subsequent commits contain tests/governance.
- APK: `C:\Users\Selim\AppData\Local\Temp\osmosis-gate2-lf-fafddfa5573640d6a5d8d4a2b9bfb981\app\build\outputs\apk\debug\app-debug.apk`.
- SHA256 rechecked: `461C05C4462D0F8F85F776425D2470707C975731C41F5E5FEA4201657ECE89A1`.
- Application ID `dev.konraditurbe.osmosis`, versionName `1.4.4`, versionCode `29`, from artifact output metadata. Installation/signature compatibility remains to be verified.
- ADB 1.0.41 / platform-tools 37.0.1-15733141; one authorized USB target, SM-S938B, Android 16/API36. Device serial omitted. Existing package is debuggable, version1.4.4/code29; `run-as` works. Version numbers alone do not identify the installed source.
- Device clock read-only observation: `2026-09-17T17:19:24+0200`. Camera clock and current connection/transfer status requested before connection, since existing connection code can synchronize camera time.
- Metadata-only `stat -c %s` on the two previously identified protected phone files: TEST-D **38,447,651**, TEST-E **3,071,380,142** bytes, both match G0 completion evidence. No media content copied, opened, hashed or displayed. Current MediaStore flags not yet queried.
- No device `sqlite3` command found. A reviewed read-only projection mechanism is required before ledger observations; never export the raw database or use the synthetic destructive/emulator-only test harness on this phone.
- Fork open-issues endpoint returned no open items. Upstream read-only open work: issues40/32, PRs24/11/8; no mutation. Workflows unchanged.

## Initial preflight checkpoint (historical; superseded below)

Confirm no active download; report whether already connected and camera date/time without changing settings. No installation, force-stop, launch or camera interaction has been executed in this resumed session yet. Preserve originals and completed phone files throughout. No gate PASS inferred from preflight.

## Evidence privacy

Persist only allowlisted model/platform/package/version facts, counts/classes/state, opaque ledger/path identities, uncertainty and test outcomes. No raw database/preferences/logcat/UI screenshots, Wi-Fi credentials, GPS, media content or unrelated logs. Existing credentials must be exercised only through the ordinary app flow; never inspect values through tools.

## Installation and preconnection checkpoint

User confirmed app not started and camera off, establishing no active app transfer. Installed APK code only was pulled for signing comparison (not app data/media): its SHA256 was `58E5CFDE7E8398660DB997DD48F69E37EEF1611311130480FB98A03762AB1A3B`. Installed and candidate APK certificates both SHA256 `9c51946929452911a8554b4f83a24bcbf71172af60d8991488c3d2bc2ede35bc`.

`adb -d install -r <verified-LF-APK>`: Success, exit0. No uninstall/data clear. Ordinary `adb -d shell am start -W -n dev.konraditurbe.osmosis/dev.konraditurbe.osmosis.ui.MainActivity`: Status ok, COLD,286ms. Protected TEST-D/TEST-E sizes unchanged after installation. User subsequently reported camera time **17:26**; calendar date remains unconfirmed. Phone read-only time at17:27:57 was2026-09-17+0200. This non-simultaneous observation does not establish clock equality, timestamp semantics or timezone trust. No camera setting was changed by tools.

### Read-only instrumentation preparation and original failures

No device sqlite3 available. An initial standalone platform-Java projection compiled with the existing SDK, but the attempted cache deployment first failed due shell quoting (exit1, no write); corrected private-cache deployment succeeded and the run-as app_process invocation returned `Aborted` (exit1). Cause is not inferred; no raw logcat read. This diagnostic never connected to a camera or accessed media. Only diagnostic code was placed in app cache.

A test-only instrumentation entry was attempted next; AGP's configured runner did not register that additional manifest entry. Invocation failed with instrumentation-info-not-found (exit1), preserved in preconnection-audit.txt. No test executed. The unused manifest/runner were removed locally. Final mechanism is the explicitly selected `hardwareAudit=read-only` early-return path of the configured runner; it cannot fall through into the synthetic emulator suite. Production source, dependencies and Gradle configuration are unchanged. Java projector opens the existing DB with OPEN_READONLY, verifies schema2, uses one SELECT for a consistent statement snapshot, hashes paths/IDs on-device and allowlists all exported strings. It neither initializes Room nor reads credentials/preferences/media; arbitrary exception text is suppressed.

Build command `./gradlew.bat :app:assembleDebugAndroidTest --no-build-cache --console=plain --no-daemon`: first build PASS23s, corrected runner build PASS18s. Final separate test APK SHA256 `B6ED1D4CF84C36823E5BA5FD3ABFC9FF6396F4DB397254299D74EA6F22683176`; installation Success. Only the following audit mode was invoked on the phone:

```text
adb -d shell am instrument -w -e hardwareAudit read-only dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
```

Result exit0: `{"database_exists":false}`, persisted in preconnection-readonly-audit.txt. This is expected before the first G2 inventory, not a failed persistence test. Instrumentation starts a fresh target process; use it only at explicitly recorded no-transfer checkpoints, never claim it is a passive live-session observation. Ordinary relaunch returned COLD/Status ok,221ms. No camera connection/download was instructed yet. Next physical action: normal camera selection/connection and grid observation, without queueing or downloading anything.

## Saved-entry connection attempt

User reports the existing saved Pocket 4 Pro entry failed with a not-in-range/switch-on message despite the camera being powered on. Recorded separately as [SAVED_ENTRY_RECONNECT_FAILURE](SAVED_ENTRY_RECONNECT.md), root cause UNCONFIRMED. A fresh rescan was initiated; the submitted result fields were placeholders and cannot establish success, availability or media count. Await actual observations without restarting the app or disturbing the potential live connection. No transfer or repeat download requested.

## First enumeration and actual planner-policy observation

User subsequently confirms rediscovery YES, connection SUCCESS, grid visible and no download started. **Saved-entry direct reconnect FAIL; fresh-rescan connection PASS**, distinct events, root cause unconfirmed. Visible UI item count was not supplied; persisted enumeration count below is independently read from the ledger.

The user explicitly requested non-destructive ledger/planner inspection and protection of the two existing copies. Audit updated only in androidTest to include current-snapshot membership, local-locator presence, committed length and calls to the actual production `SyncPlanner.action` with the same default verification argument as `LedgerRepository.plan`. This is a recomputation from persisted state, not a capture of the previous process's volatile `latestPlan` or legacy UI download queue. No policy copied/reimplemented in the test. One SQL SELECT supplies a consistent snapshot; all DB access remains read-only.

Build of separate audit APK: same assembleDebugAndroidTest command, exit0,18s. SHA256 `BC69B58DE1F29807D06BE959CCE14A027C56F34EDC3164D85D0835B65C2B53A5`. Install Success. Explicit hardwareAudit read-only command exit0; complete sanitized output in [first-enumeration-readonly-audit.txt](first-enumeration-readonly-audit.txt). Instrumentation ran at a confirmed no-transfer checkpoint, replaces the target process and may end its camera session. No synthetic suite or production implementation change.

Actual schema2 DB: **1 source,1 snapshot,2 assets,2 recording rows,2 members,2 phone replica reservations**. Snapshot sealed **INCOMPLETE**,2 members; complete store/member/generation coverage is not proven. Two separate uncertain recording rows do not prove complete real recording membership.

| Asset observation | Asset class | Persisted transfer state | Production planner action | Local locator | Committed bytes |
|---|---|---|---|---|---:|
| 38,447,651 bytes; opaque ID dd5a4029… | KNOWN_REQUIRED | DISCOVERED | REVALIDATE_IDENTITY | absent | 0 |
| 3,071,380,142 bytes; opaque ID 9e181fce… | KNOWN_REQUIRED | DISCOVERED | REVALIDATE_IDENTITY | absent | 0 |

Both belong to the latest source snapshot; both identityAmbiguous=true, strongVersion absent, remote timestamp absent, recording relationships uncertain. Neither is TRANSFERRED_UNVERIFIED, LOCAL_VERIFIED or persisted NEEDS_REVALIDATION at this first observation. NEW/KNOWN are not TransferState enum labels here; DISCOVERED means first ledger discovery, not proof the phone lacks a physical copy. KNOWN_REQUIRED is an asset inclusion class, not a verified-copy assertion.

Both capture-day reservations are2026-09-17 with explicit SYNC_FALLBACK/SYNC_TIME_FALLBACK. This does **not** establish actual capture dates, intended local capture-day correctness or physical date folders. No trusted filename/timezone contract is inferred.

Metadata-only repeat stat returns38,447,651 and3,071,380,142 bytes for the protected pre-existing TEST-D/TEST-E files, unchanged. Size correspondence is not independent cryptographic identity/integrity proof. **Existing local downloads are not recognized as transferred/verified replicas by the G2 ledger.** No locator/adoption exists for either. The current planner requests identity revalidation, not DOWNLOAD, but this cannot certify eventual no-retransfer behavior: G2 executes no new transfer engine, G3 reconciliation/integrity is not implemented, and the legacy UI downloader is separate. Safe legacy-copy reconciliation without re-download must be evidenced before asserting that acceptance. Do not fake VERIFIED or redownload to hide the gap.

Observed ledger persistence across the audit's controlled process replacement PASS for these rows; unchanged counts/IDs/paths across another enumeration remain NOT_TESTED. Overall G2 remains BLOCKED pending remaining mandatory HIL/disposition. No media content opened/hashed/copied, no transfer/deletion/overwrite, no main/dependency/Gradle/production-source changes.
