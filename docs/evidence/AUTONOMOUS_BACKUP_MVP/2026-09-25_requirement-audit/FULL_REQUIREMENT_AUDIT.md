# Full software requirement audit — 2026-09-25

## Scope and decision rule

This audit evaluates every software-testable completion item in
`docs/PRODUCT_COMPLETION_PLAN.md` against implementation and reproducible tests. A PASS below
means software evidence only. Physical radio, firmware and USB/SAF-provider claims remain in the
single consolidated queue and are never inferred from simulation.

## Requirement matrix

| Items | Software verification | Result |
|---|---|---|
| PC01, PC17 | Saved-camera selection; explicit-stop, epoch and platform-callback fences; launcher-after-camera-off bounded discovery; replacement ownership. | `CameraStartupDiscoveryPolicyTest`, `CameraRecoveryScanPolicyTest`, `CameraSessionResourcesTest`, datalink fence tests — PASS |
| PC02–PC04 | Media readiness distinct from transport; bounded empty retry; incomplete inventory preserves historical evidence. | `CameraDatalinkCoordinatorTest`, `DurableSessionRuntimeTest`, `LedgerEnumeratorTest` — PASS |
| PC05–PC07, PC18 | Trusted plan selection; exact asset-ID-to-live-source mapping; duplicate writer refusal; stale/lost writer refusal; partial/review outcome; service-owned dispatch/progress. | `AutomaticBackupPlanTest`, `AutomaticTransferDispatchPolicyTest`, `AutonomousBackupRuntimeTest`, `StrictTransferBatchTest`, end-to-end recovery scenarios — PASS |
| PC08–PC09 | Exact length, SHA-256, readback and no false verified result; capture-day uncertainty. | integrity contract/process tests and `DjiFilenameTimeTest` — PASS |
| PC10, PC16 | Durable external staging, collision/corruption refusal, readback checksum, independent rehydration and unavailable SAF fail-closed behavior. | `ReplicaVerificationTest`, `PhoneToExternalReplicaPolicyTest`, `ExternalReplicaRunPolicyTest`, `ExternalProviderFaultScenarioTest` — PASS |
| PC11–PC12 | Backend-derived Camera Sync/redundancy/Safe-to-Clear; UI observer and stale-grid fences. | `ReplicaVerificationTest`, `ProductStateMatrixTest`, `BackupDisplayTest`, datalink/UI fence tests — PASS |
| PC13 | Diagnostics omit credentials, paths, GPS and media content. | `PrivacySafeDiagnosticsTest` and source/sink audit — PASS |
| PC14–PC15, PC19 | Integrated JVM/debug checkpoint; bounded lifecycle harness/alternative restoration coverage; selected-network camera traffic. | full JVM/debug checkpoint; `Gate7LifecycleInstrumentation`; network/datalink tests — PASS |

## Mandatory visible-state matrix

`ProductStateMatrixTest` covers every supported grid state: new, partial, locally complete with
integrity proof but source identity open, existing local copy without proof, and ambiguous/missing
binding. It also proves that source ambiguity keeps Camera Sync, redundancy and Safe-to-Clear false
even when phone and external proof objects are otherwise present.

The user-visible German label for the proven-local/open-source row is now:
`Lokal vollständig · Integrität geprüft; Quelle offen`.
It does not mean Camera Sync Complete or redundancy complete.

## Combined hardware boundary

`docs/hardware/VALIDATION_QUEUE.json#G7-LIFECYCLE-RECOVERY-BATCH` is the only active Pocket/S25
session. It combines launcher-before-camera discovery, trusted inventory, visible state labels,
automatic service transfer/progress if safe work exists, lifecycle/background retention, controlled
power-cycle recovery and privacy-safe reason diagnostics. SSD criteria remain a separately marked
hardware-topology-deferred batch and must be appended only when that topology is available.

No safe software-only requirement is left unexamined by this audit. The next physical session is
permitted only after the final all-JVM/debug checkpoint and a successful autonomy-control check.
