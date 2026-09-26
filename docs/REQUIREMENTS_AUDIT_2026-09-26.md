# Requirements audit — 2026-09-26

Status: ACTIVE. This is the requirement-to-evidence audit required before the single consolidated
hardware session. It does not turn an unobserved device fact into a PASS.

| Requirement set | Implementation/evidence | Audit result |
|---|---|---|
| R001–R008 | PC01–PC10, PC16–PC19: service-owned known-camera discovery, complete-inventory fence, durable ledger, idempotent strict transfer/readback, local-first behavior and independent SAF replica orchestration. | SOFTWARE_PROVEN; Pocket/S25/SSD behavior remains HARDWARE_DEFERRED. |
| R009 OneDrive | Local-first routing deliberately has no production cloud dependency or credentials. No user-authorized OneDrive tenant/product decision exists. | PRODUCT_DECISION_DEFERRED; not silently substituted by camera/phone behavior. |
| R010–R011 | PC11/PC20 plus `AutomaticTransferUiStatePolicy`: German summary, visible preparing/writer-handoff/transfer/review states and ledger-derived completion vocabulary. The durable state and active progress share one card matching camera status, so an incomplete terminal source cannot look like a running transfer. The primary copy reports the current outcome; historic review is a lower-priority explanation. Crucially, an unexecuted `VERIFY_EXISTING`/identity-revalidation plan item is displayed as **open / proof required**, never as a running background check. A transient Pocket refusal before bytes is current visible work: global card and exact video show bounded indeterminate retry, then measured progress or durable review. | SOFTWARE_PROVEN: `BackupStatusCopyTest` current/historical, phone-only, complete-outcome and non-running-proof-state regressions; `CameraTransferSourceTest` retries 404/500 only before allocation and refuses exhausted/unrelated responses; `LiveTransferFileProjectionPolicyTest` covers the distinct retry row. Visual target observation remains HARDWARE_DEFERRED. |
| R012 and R039 | Cleanup policy remains fail-closed; no automatic deletion, destructive test or cleanup execution is authorized. | SAFETY_DEFERRED_BY_AUTHORIZATION. |
| R013, R034, R041 | Typed app-private event store, bounded explicit verbose session/export, source/sink audit and sanitized fixed export failure UI/log text. GPS normal diagnostics now use fixed non-identifying vocabulary and cannot carry camera name, location-adjacent timing/fix data, provider names or raw failure text. | SOFTWARE_PROVEN; target share sheet/recovery trace remains HARDWARE_DEFERRED. |
| R014, R022–R025, R033 | Strict streaming transfer, range/integrity/publication/reconciliation tests, one writer, bounded cancellation and provider fault simulations. | SOFTWARE_PROVEN; target throughput/thermal/provider behavior remains HARDWARE_DEFERRED. |
| R015 | Custom lifecycle/ledger/replica code is isolated from upstream protocol adapters; upstream is unchanged. | SOFTWARE_PROVEN. |
| R016–R019, R027–R030, R042 | Fenced service session runtime, stale callback guards, bounded recovery, route pinning, current user stop, replacement-writer handoff and observer-only UI. `LivenessFaultMatrixTest` covers recovery exhaustion, revalidation-before-traffic, permanent causes and explicit stop; `SessionRecoveryScenarioTest` covers replacement/process recreation. | SOFTWARE_PROVEN for deterministic matrix; liveness timings/causes remain HARDWARE_DEFERRED. |
| R020–R021, R038 | Asset classification, complete snapshot predicates, unknown/required fail-closed planning and independent replica predicates. | SOFTWARE_PROVEN; target manifest/member facts remain HARDWARE_DEFERRED. |
| R026, R031 | Phone/SSD independent proof and camera-sync/redundancy/safe-to-clear derivation; safe-to-clear remains informational. | SOFTWARE_PROVEN; physical SSD evidence remains HARDWARE_DEFERRED. |
| R032 | Current Android target build and permission boundaries compile; no target-37 migration is claimed. | SOFTWARE_PROVEN_WITH_TARGET_VALIDATION_PENDING. |
| R035 | Canonical fixture normalization and current JVM/debug build checkpoint exist; baseline lint debt remains separately documented. | SOFTWARE_PROVEN_WITH_BASELINE_DEBT_RETAINED. |
| R036 | Recovery makes no unproven model-specific power or keepalive claim. | HARDWARE_DEFERRED by definition of the required physical measurement. |
| R037 | Date resolver, uncertainty, deterministic paths and persistence have unit/fixture coverage. `CaptureDayFaultMatrixTest` makes CD01–CD08 policy outcomes explicit; the synthetic emulator fixture proves reservation/group/restart persistence. | SOFTWARE_PROVEN for deterministic behavior; target source-time/MIME/sidecar facts remain HARDWARE_DEFERRED. |
| R040 | Current opt-in, fresh-process backup default, one BLE owner and late permission-result fence have deterministic tests; the handoff tears down media offload before the GPS owner starts. | SOFTWARE_PROVEN for policy/ownership; Android permission/foreground-service lifecycle remains HARDWARE_DEFERRED. |
| R043 | Pocket Pickup label, adaptive icon and restrained Graphit-/Nachtblau day/night palette are implemented without changing application ID, signing/data continuity, backup paths or safety behavior. Wallpaper-derived dynamic colours are deliberately disabled so the approved brand contrast is stable. | SOFTWARE_PROVEN: full JVM/debug build = 531 tests, 0 failures/errors, SHA-256 `7B94C1EC072CFD5149E9F5AC001EDB3B92EDA97C634960A36B145042B549679F`; target launcher rendering remains HARDWARE_DEFERRED. Application ID, final signing, distribution/OAuth and third-party identity review require a separate product/release decision. |

## Audit findings closed in this cycle

- Replacement writers now wait for the fenced predecessor and revalidate before automatic
  continuation; no healthy handoff is converted to a user-action error.
- Pocket transfer requests no longer make `MainActivity` a writer.
- Progress communicates a safe writer handoff instead of appearing stalled.
- Verbose-export failures no longer surface raw exception data in either logcat or UI.
- GPS permission completion remains subject to current explicit opt-in.

The remaining open rows are deliberate and specific. They keep the software phase open only for
their deterministic matrices; physical observations stay bundled in
`docs/hardware/FINAL_COMPLETE_APP_HARDWARE_TEST.md` when that phase is actually ready.
