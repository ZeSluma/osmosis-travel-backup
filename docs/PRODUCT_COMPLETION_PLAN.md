# Product completion plan — Osmosis Travel Backup

Status: ACTIVE. Authoritative software-completion checklist for `codex/autonomous-backup-mvp`.

The complete requirement-to-evidence mapping is maintained in
[`REQUIREMENTS_AUDIT_2026-09-26.md`](REQUIREMENTS_AUDIT_2026-09-26.md). It classifies product
decisions, safety-deferred destructive work, software gaps and physical evidence separately.

This checklist is derived from `AGENTS.md`, `PROJECT_STATE.yaml`, the active MVP plan, the
service/ledger/replica implementation, and the persistent hardware queue. `PASS` means
reproducible software evidence exists; it never substitutes for a physical Pocket/S25/SSD claim.

## Software completion items

| ID | Capability and PASS criterion | Evidence | State |
|---|---|---|---|
| PC01 | Known-camera selection respects saved MAC matching, explicit stop and replacement epochs. | **Reopened 2026-09-26 from target evidence:** an in-place update/forced restart produced `CAMERA_UNAVAILABLE` after one scan. A queued service `STOP` had no epoch and could race a replacement launcher epoch. Service STOP now carries and verifies its initiating epoch; stale STOP is ignored before host/backup shutdown. `DurableSessionRuntimeTest` explicitly proves a superseded STOP cannot stop its replacement; full JVM/debug checkpoint **525 tests, 0 failures/errors** PASS. Target automatic-connect recheck remains required. | SOFTWARE_PROVEN_HARDWARE_PENDING |
| PC02 | Transport never implies media readiness; stale/empty/failed enumeration cannot become trusted. | `CameraDatalinkCoordinatorTest`, `DurableSessionRuntimeTest` | PASS |
| PC03 | Empty/incomplete post-connect inventory gets one bounded fresh-session retry then remains untrusted. | `CameraDatalinkCoordinatorTest` | PASS |
| PC04 | Trusted complete inventory reconciles durably; incomplete observations preserve known evidence and block automatic work. | `LedgerEnumeratorTest`, `AutomaticBackupPlanTest` | PASS |
| PC05 | Automatic planner selects only complete, safe DOWNLOAD work and excludes verified/unknown work. | `AutomaticBackupPlanTest`, `SyncPlannerTest` | PASS |
| PC06 | Automatic executor prevents duplicate same-epoch camera/replica writers and fences stale completions. | `AutonomousBackupRuntimeTest`, `EndToEndBackupRecoveryScenarioTest` | PASS |
| PC07 | Interrupted strict transfer remains partial/review-required; failed automatic work cannot falsely complete and a later trusted epoch gets a replacement writer. | `StrictTransferBatchTest`, `AutonomousBackupRuntimeTest` | PASS |
| PC08 | Phone integrity requires exact length and SHA-256/readback evidence; corrupt/truncated/interrupted content cannot become verified. | `IntegrityContractTest`, integrity process tests | PASS |
| PC09 | Capture-day paths derive only from supported source timestamp evidence with explicit uncertainty. | `DjiFilenameTimeTest`, capture-day tests | PASS |
| PC10 | SAF replica allocation/recovery uses durable staging, rejects collisions/corruption, and verifies independent readback. | `ReplicaVerificationTest`, `PhoneToExternalReplicaPolicyTest`, replica migration instrumentation | PASS |
| PC16 | **Hard requirement — autonomous SSD replica:** the app probes the previously user-approved SAF tree itself; a present/usable SSD is recognized independently of camera state (at app start and USB/media availability hints); verified phone receipts are copied after camera disconnect or process recreation; absent/revoked/unprobeable storage is fail-closed and never blocks camera→phone or claims redundancy; the same durable Osmosis relative path is used and absent day directories are created safely. Selecting “SSD” only grants the tree once; it is not a per-sync mode switch. | 2026-09-18: `ExternalReplicaRunPolicyTest`, `SafReplicaPathTest`, `ReplicaVerificationTest`, `ExternalProviderFaultScenarioTest`, integrated fault tests; Android-test source compiles; full `:app:testDebugUnitTest :app:assembleDebug` = **458 tests, 0 failures, BUILD SUCCESSFUL**. Application-owned `ExternalReplicaCoordinator`, durable completed-snapshot rehydration and SAF provider probe. | PASS |
| PC11 | Product status derives Camera Sync, redundancy and informational Safe-to-Clear only from trusted inventory and verified independent evidence. | `ReplicaVerificationTest`, `BackupStatusProjection` | PASS |
| PC12 | UI is an observer: stale session/adapter callbacks cannot repaint a replacement grid; untrusted inventory is not rendered as confirmed empty. | `CameraDatalinkCoordinatorTest`, MainActivity fences | PASS |
| PC13 | Privacy-safe diagnostics expose state/reason/counts without credentials, GPS or media content. | 2026-09-19 source/sink audit removed MAC/name, SSID, IP, serial and raw BLE/R-SDK/thumbnail/media-path logging; `PrivacySafeDiagnosticsTest` and full JVM/debug build PASS. | PASS |
| PC14 | JVM integrated fault checkpoint and debug build pass on the final source state. | 2026-09-18: `:app:testDebugUnitTest :app:assembleDebug` — **461 tests, 0 failures/errors, BUILD SUCCESSFUL**; `ManifestGoldenTest` normalizes CRLF fixture input only | PASS |
| PC15 | Emulator lifecycle harness is either PASS or explicitly bounded with reproducible failure evidence and alternative lifecycle coverage. | `Gate7LifecycleInstrumentation`; hardware-session report records two bounded no-stacktrace harness attempts and passing durable process-restoration coverage | PASS |
| PC17 | A stale BLE scan/GATT, AP-network, or datalink callback from a released or superseded platform client cannot mutate a replacement connection/session or tear down its UI/ledger path. | `CameraSessionResourcesTest`, `CameraDatalinkCoordinatorTest`; service-level callback-generation and epoch fences; full JVM/debug-build checkpoint | PASS |
| PC18 | Automatic camera-transfer dispatch is owned by the service/coordinator rather than the Activity; UI observes service projection plus durable receipt state and cannot be required to remain alive for a trusted automatic plan to execute. | **Audit-repaired 2026-09-26:** the strict Pocket manual action delegates to `AutomaticCameraTransferDispatcher`; `MainActivity` no longer owns a Pocket writer. A replacement epoch waits for the fenced predecessor writer to release, then rechecks session/source/plan before acquiring exactly one new writer. `SessionRecoveryScenarioTest` (3), `DurableSessionRuntimeTest` (7), dispatch/progress policies and full JVM/debug checkpoint **514 tests, 0 failures/errors** PASS. | PASS |
| PC19 | Camera traffic is explicitly pinned to the selected camera `Network`; no process-wide network binding can route future cloud traffic through the camera AP. | 2026-09-19: `ApJoiner` no longer calls `bindProcessToNetwork`; `DumlTransport` binds its UDP socket, TCP poke uses the network socket factory, and `HttpClient`/preview/manual downloader receive the camera network. Targeted datalink/integrity/dispatch tests and full JVM/debug build **471 tests, 0 failures/errors**. | PASS |
| PC20 | The backup summary uses human-readable, privacy-safe German state text. It explains pending camera matching, SSD redundancy, informational safe-to-clear and automatic-work blockers without exposing source metadata or claiming completion. | **Corrected 2026-09-26 from target feedback:** the dedicated status card distinguishes an actual service-owned operation from a durable plan that is merely open. Preparation, transfer and integrity progress appear only while the transfer worker is active. Existing local files that need an integrity proof, or files that need identity revalidation, say `Synchronisation offen` and state what proof is missing; they never claim to be running in the background. A terminal incomplete list says `Kameraliste noch nicht vollständig – deshalb keine Übertragung`. `BackupStatusCopyTest` covers current/historic precedence, phone-only/full outcomes and both non-running proof states; target readability remains consolidated physical evidence. | SOFTWARE_PROVEN_HARDWARE_PENDING |

| PC20-TRANSITION | Every changed visible backup/progress state has deterministic active, progress, successful-terminal, fail-closed-terminal and stale/replacement callback coverage. Progress is shown only during active service-owned work and has a bounded success dismissal. | **Extended 2026-09-26:** `UI_TRANSITION_AUDIT.md` maps global, per-video and external-replica lifecycle rows. `LiveTransferFileProjectionPolicyTest` proves bounded per-file phone progress and terminal overlay removal; `ExternalReplicaRefreshPolicyTest` prevents a terminal external-copy refresh loop; dispatcher clears cell overlays before durable reread. A transient Pocket 404/500 before the first byte now has a dedicated global/per-video indeterminate retry state and `CameraTransferSourceTest` verifies success after bounded retry, exhausted fail-closed retry, and non-retry client refusal. The retry is before allocation. Full JVM/debug checkpoint is required before target installation. | SOFTWARE_PROVEN_HARDWARE_PENDING |

| PC20-FIX | Current-inventory completeness is separated from historical identity ambiguity, so a terminal current camera list cannot be displayed as still loading only because older observations remain unresolved. | 2026-09-26: `LedgerCoordinator` uses `SnapshotCompletenessPolicy` with current unresolved count; `LedgerModelTest` and `BackupStatusCopyTest` PASS. Final target trace remains hardware pending. | SOFTWARE_PROVEN_HARDWARE_PENDING |
| R043 | Pocket Pickup has a stable app label, restrained modern palette and adaptive vector icon without changing application ID, signing/data continuity, backup paths or safety behavior. | 2026-09-26: [Pocket Pickup brand decision](design/POCKET_PICKUP_BRAND.md); full `:app:testDebugUnitTest :app:assembleDebug --no-daemon` = **531 tests, 0 failures/errors**; current debug APK SHA-256 `D232711BCD8022BA1127B0C5221E7C06FD71AAF3135C4254061DE831352E78D4`. Target launcher rendering remains part of the combined physical session. | SOFTWARE_PROVEN_HARDWARE_PENDING |
| R037 | Capture-day resolution and allocation preserve source-local date uncertainty, deterministic paths, sidecar grouping and restart/reconcile stability without filename-only identity merging. | `CaptureDayFaultMatrixTest` (4) makes the deterministic CD01–CD08 policy cases explicit; `LedgerModelTest`, `DjiFilenameTimeTest`, `SafReplicaPathTest` and the synthetic emulator fixture cover resolver, reservation, parent membership and restart/reconcile persistence. Pocket timestamp/MIME/sidecar facts remain physical evidence. | SOFTWARE_PROVEN_HARDWARE_PENDING |
| R040 | GPS recording is a current explicit opt-in, has one BLE owner, and cannot silently restart, collect location, or suppress independent automatic backup after recreation. | 2026-09-26: `GpsModePolicyTest` proves a fresh process enters backup mode, GPS requires current explicit selection, a granted permission response cannot revive GPS after the user turns the mode off, and automatic discovery remains backup unless the GPS owner is actually active. `MainActivity` clears the legacy persisted `gps_mode` toggle and tears down the offload owner before GPS handoff. Android permission/FGS behavior remains physical validation. | SOFTWARE_PROVEN_HARDWARE_PENDING |
| R041 | Typed diagnostics are bounded, app-private and explicitly exportable; ordinary/verbose paths cannot expose protected data or affect backup. | `DiagnosticEventStoreTest`, `PrivacySafeDiagnosticsTest`, `VerboseDiagnosticsPolicyTest` and `GpsDiagnosticsPolicyTest`. The GPS service now has fixed non-identifying copy only: no camera name, location-adjacent timing/fix data, provider names or raw failure text reaches its normal logs. Target share-sheet observation remains physical evidence. | SOFTWARE_PROVEN_HARDWARE_PENDING |
| R042 | Camera liveness/recovery is fenced, bounded and reason-coded; no unproven model-specific keepalive or power claim is made. | `LivenessFaultMatrixTest` proves bounded transient-loss exhaustion, revalidation-before-camera-traffic, permanent-cause escalation and explicit-stop refusal. `SessionRecoveryScenarioTest` covers replacement and process-recreation handoff. KA01-KA08 physical timing/cause observations remain intentionally unclaimed. | SOFTWARE_PROVEN_HARDWARE_PENDING |

## Latest integrated software checkpoint

2026-09-26: target evidence exposed a stale advisory progress area after automatic recovery
reached a terminal no-work plan. The Activity now removes a transient preparation panel on a
terminal no-work service decision, while the durable summary remains the source of truth; a
source-to-job conversion failure is now visibly fail-closed as `SOURCE_CHANGED`. Targeted policy
regression and `:app:testDebugUnitTest :app:assembleDebug --no-daemon` completed successfully with
**524 JVM tests, 0 failures, 0 errors**. Debug APK SHA-256:
`4DA7DD5700BD79F314B59D4629EDDF3BF64513BF2D20569AF043AA1BB4E37C1A`.
This is software evidence only; it does not replace any pending physical validation.

2026-09-26 (follow-up): feedback from the live session confirmed automatic connection and grid
recovery while showing that the old technical status dump did not help the traveller. The summary
now renders one short next-action state; it never treats presentation as backup proof. Full
checkpoint: **526 JVM tests, 0 failures, 0 errors**; debug APK SHA-256:
`A2082D6CC11909230FCBEE863E4E95651B9FE813002F74D2A70343F6CF02A728`.

2026-09-26 (Pocket Pickup): the user selected the final product name and a restrained visual
direction. The label, adaptive vector launcher mark and day/night neutral-plus-petrol palette
were implemented without modifying backup code, app ID, signing, data, paths or safety rules.
Full JVM/debug checkpoint: **531 JVM tests, 0 failures, 0 errors**; debug APK SHA-256:
`D232711BCD8022BA1127B0C5221E7C06FD71AAF3135C4254061DE831352E78D4`.

2026-09-26 (Pocket Pickup refinement): target feedback rejected the initial Petrol treatment. The
app now uses a fixed Graphit-/Nachtblau palette rather than wallpaper-derived Material You colours,
and a bolder pickup-tray launcher mark. This changes no backup behavior, app identity or data.
Full JVM/debug checkpoint: **531 JVM tests, 0 failures, 0 errors**; debug APK SHA-256:
`7B94C1EC072CFD5149E9F5AC001EDB3B92EDA97C634960A36B145042B549679F`.

2026-09-26 (backup-state card): live S25 feedback showed that the safe terminal incomplete-list
text was too small and visually ambiguous. The gallery now renders all backup state in a card that
matches the camera-status card; active service progress is inside it, while the terminal state
clearly says no transfer starts. Full JVM/debug checkpoint: **531 JVM tests, 0 failures, 0
errors**; debug APK SHA-256 `9007D833A1CA9283EE3281B4418F353280606406BC09314029D8CAF5F1E867BE`.

2026-09-26 (current versus historical state): live S25 evidence showed a new clip was safely
transferred while three historical identity records remained unresolved. The prior card then
contradicted the completed transfer by calling the entire list incomplete and saying no transfer
would start. The projection now keeps the global completion block but says: `Frühere Dateien
brauchen Prüfung (3) – neue Dateien werden weiterhin gesichert`. A dedicated regression proves
this historical state cannot overwrite a completed current transfer. Full JVM/debug checkpoint:
**532 JVM tests, 0 failures, 0 errors**; debug APK SHA-256
`B5D856F25976D76050935463CED59A8E312AFE22ED7CF4D94BECD18FA340E66B`.

2026-09-26 (whole-status card): target feedback required the primary line to report the actual
current backup outcome, not implementation detail. The card now has a deterministic hierarchy:
**Synchronisation läuft**, **Synchronisation wird vorbereitet**, **Aktuelle Synchronisation fertig**
with a historical-review note, **Telefon-Synchronisation fertig · SSD-Sicherung ausstehend**, or
**Synchronisation fertig** only after independent phone and SSD evidence. An incomplete current
list remains a fail-closed blocker. `BackupStatusCopyTest` proves historical ambiguity cannot
replace current success, phone-only and complete outcomes, and that open integrity/identity proof
items do not impersonate running work. Full JVM/debug checkpoint: **533 JVM tests, 0 failures,
0 errors**; debug APK SHA-256
`773977BFD0DBA7D48D4858BB1850E674095CB0F052A62052713F90045400CB48`.

## Hardware deferred

| ID | Requirement | State |
|---|---|---|
| HD01 | Pocket automatic connect → trusted non-empty inventory → automatic transfer on S25. | HARDWARE_DEFERRED (`G7-LIFECYCLE-RECOVERY`) |
| HD02 | Physical AP/liveness/reason-state recovery. | HARDWARE_DEFERRED (`G7-REASON-DIAGNOSTICS`) |
| HD03 | S25 SAF grant/reacquisition and USB SSD behavior. | HARDWARE_DEFERRED (`MVP-SSD-SAF-HOST`) |
| HD04 | Physical phone→SSD interruption/readback recovery. | HARDWARE_DEFERRED (`MVP-SSD-REPLICA-RECOVERY`) |
| HD05 | S25: detach camera, attach already-authorized SSD, then confirm automatic phone→SSD catch-up and provider reappearance detection without selecting a new folder. | HARDWARE_DEFERRED (`MVP-SSD-INDEPENDENT-CATCHUP`) |

No camera-original deletion, release, main merge, or destructive media test is authorized.
