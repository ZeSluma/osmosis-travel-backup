# Product completion plan — Osmosis Travel Backup

Status: ACTIVE. Authoritative software-completion checklist for `codex/autonomous-backup-mvp`.

This checklist is derived from `AGENTS.md`, `PROJECT_STATE.yaml`, the active MVP plan, the
service/ledger/replica implementation, and the persistent hardware queue. `PASS` means
reproducible software evidence exists; it never substitutes for a physical Pocket/S25/SSD claim.

## Software completion items

| ID | Capability and PASS criterion | Evidence | State |
|---|---|---|---|
| PC01 | Known-camera selection respects saved MAC matching, explicit stop and replacement epochs. | **Repaired 2026-09-25:** launcher-after-camera-off now gets eight bounded, epoch-fenced scan windows. Target then exposed a replacement-session race: selection teardown issued service `STOP` and only later began the new GATT epoch, so a queued stop could render `Camera Session Stopped`. Replacement transport release now preserves the host, begins the new epoch before GATT, and treats early pre-Wi-Fi GATT loss as bounded recovery; only an explicit terminal user path stops the service. `CameraStartupDiscoveryPolicyTest`, `CameraRecoveryScanPolicyTest`, `DurableSessionRuntimeTest`, resource tests and full JVM/debug build **490 tests, 0 failures/errors** PASS. Physical validation remains consolidated in HD01. | PASS |
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
| PC18 | Automatic camera-transfer dispatch is owned by the service/coordinator rather than the Activity; UI observes service projection plus durable receipt state and cannot be required to remain alive for a trusted automatic plan to execute. | **Repaired and internally reverified 2026-09-25:** exact durable asset IDs are now mapped back to the trusted `CameraFile` observation before dispatch, so path-vs-identity comparison cannot falsely reject every planned source. The service publishes a privacy-safe `transfer=<percent>% (<completed>/<total>)` projection on percentage changes and on completion; the Activity only re-reads/projections it, without owning a writer. `AutomaticTransferProgressPolicyTest`, dispatch/integrity tests and full `:app:testDebugUnitTest :app:assembleDebug` **483 tests, 0 failures/errors** PASS. Prior target logs confirm both planned files were strict-transferred with readback/integrity, but the current UI-progress APK still needs one consolidated target observation. | PASS |
| PC19 | Camera traffic is explicitly pinned to the selected camera `Network`; no process-wide network binding can route future cloud traffic through the camera AP. | 2026-09-19: `ApJoiner` no longer calls `bindProcessToNetwork`; `DumlTransport` binds its UDP socket, TCP poke uses the network socket factory, and `HttpClient`/preview/manual downloader receive the camera network. Targeted datalink/integrity/dispatch tests and full JVM/debug build **471 tests, 0 failures/errors**. | PASS |
| PC20 | The backup summary uses human-readable, privacy-safe German state text. It explains pending camera matching, SSD redundancy, informational safe-to-clear and automatic-work blockers without exposing source metadata or claiming completion. | `BackupStatusCopyTest` plus `ProductStateMatrixTest`; the copy boundary accepts only durable booleans/counts and leaves machine diagnostics out of the UI. | PASS |

## Hardware deferred

| ID | Requirement | State |
|---|---|---|
| HD01 | Pocket automatic connect → trusted non-empty inventory → automatic transfer on S25. | HARDWARE_DEFERRED (`G7-LIFECYCLE-RECOVERY`) |
| HD02 | Physical AP/liveness/reason-state recovery. | HARDWARE_DEFERRED (`G7-REASON-DIAGNOSTICS`) |
| HD03 | S25 SAF grant/reacquisition and USB SSD behavior. | HARDWARE_DEFERRED (`MVP-SSD-SAF-HOST`) |
| HD04 | Physical phone→SSD interruption/readback recovery. | HARDWARE_DEFERRED (`MVP-SSD-REPLICA-RECOVERY`) |
| HD05 | S25: detach camera, attach already-authorized SSD, then confirm automatic phone→SSD catch-up and provider reappearance detection without selecting a new folder. | HARDWARE_DEFERRED (`MVP-SSD-INDEPENDENT-CATCHUP`) |

No camera-original deletion, release, main merge, or destructive media test is authorized.
