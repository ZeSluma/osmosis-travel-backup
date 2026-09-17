# ADR 0003 — Execution owner for an intentional camera sync

2026-09-17 closure: architecture evidence reviewed under [ADR0007](0007-security-closure-disposition.md); [execution contract](../evidence/GATE-1/2026-09-17_security-closure/PLATFORM.md) resolves permissions/start/stop/durable ownership. ADR0008 closes B1/B2 with GATE-1 PASS, independent of deferred G7 runtime proof.
Date: 2026-09-17. Status: selected evidence-backed design; target-device validation NOT_TESTED. No implementation/dependency/manifest change authorized. Supersedes any implicit dataSync-first assumption, not verified baseline facts.

## Decision

Use one application-scoped SyncCoordinator hosted by one `connectedDevice` foreground service for the user-opened Pocket session. Start while the Activity is visible, after necessary permissions; promptly foreground it with a persistent notification. The service owns the coordinator scope, not the Activity. Room-backed intent and progress survive the host. Start/Open coalesces into the same active session; Activity recreation merely reattaches observers. A service is an execution opportunity, not a durable truth store or immortality guarantee.

This is our inference from the external-device use case and [Android's transfer API guidance](https://developer.android.com/develop/background-work/background-tasks/data-transfer-options), which specifically offers connectedDevice for local-device synchronization. It avoids splitting one live BLE/Wi-Fi protocol session across a job and a separate connection host. Adopt only after HIL proves foreground-service network ownership and screen-off behavior on SM-S938B/Android 16. Failure of that proof reopens this ADR.

## Alternatives compared

| Model | Fit for this system | Decision |
|---|---|---|
| UIDT / JobScheduler, API 34+ | Long user-started transfer, notification, system scheduling/restart hooks. Can express network constraints without blindly requiring Internet. Does not itself pair/wake the camera or maintain a local Network request; loss of a constraint can stop the job. A BLE/AP owner would still be needed, and job handover risks duplicate ownership. | Credible alternative; reject as initial camera-session host for complexity, not alleged Internet-only restriction. Revisit for future explicitly user-started cloud replication. |
| connectedDevice FGS | Cohesive external-device session lifecycle, immediate visible-user start, survives Activity loss while process/service lives. Owns BLE, Wi-Fi callback and protocol adapters together. | Selected, conditional on HIL and security review. |
| dataSync FGS | General backup/transfer type, but weaker match to a live external device and bounded background duration. | Reject for camera owner; no automatic fallback switching service types to evade restrictions. |
| WorkManager / long-running Worker | Durable deferrable work, useful for bounded reconciliation or future downstream replication; not a guarantee of immediate camera-session work. Wrapping long work in its FGS does not remove job constraints. | Not the live camera engine. Later use only for bounded work whose latency is acceptable. |
| FGS connection host + UIDT transfer | Separates platform execution roles but adds two cancellation/lifetime systems for one source session. | Defer unless measured FGS limitations justify extra complexity; coordinator fencing remains mandatory. |
| Companion Device APIs | Association/presence can aid later background eligibility; not the DJI application pairing or media protocol. Continuous Pocket advertising/presence stability across Wi-Fi/sleep states is unproven. | Optional future adapter after association/presence testing; not a prerequisite or automatic blanket permission exemption. |

[UIDT documentation](https://developer.android.com/develop/background-work/background-tasks/uidt): jobs require visible-user initiation or an allowed condition, RUN_USER_INITIATED_JOBS, a JobService notification and appropriate network constraints. They avoid standby-bucket quotas but can stop for thermal/memory/constraint reasons. Task Manager stop can terminate the process without onStopJob; persistence must precede callbacks. These properties make UIDT useful but not self-healing by itself.

[FGS types](https://developer.android.com/develop/background-work/services/fgs/service-types) require FOREGROUND_SERVICE_CONNECTED_DEVICE plus a listed network/Bluetooth prerequisite for that type; real protocol permissions still apply. Do not use the `camera` type merely because the remote peripheral is a camera. [Launch requirements](https://developer.android.com/develop/background-work/services/fgs/launch) and runtime permission checks must be implemented later with non-exported service and explicit notification actions. [Background start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start) still apply after process loss: app-open is our safe initial start opportunity, not permission for arbitrary later launches.

## Android 16 resource and stop semantics

[Android 16 quotas](https://developer.android.com/about/versions/16/behavior-changes-all#job-scheduler) apply to regular/expedited jobs, including jobs concurrent with an FGS and jobs started while visible. No hard-coded universal runtime allowance is assumed. Direct service work is not a JobScheduler quota workaround: it must genuinely be connected-device work and stop when no useful session remains.

[dataSync timeouts](https://developer.android.com/develop/background-work/services/fgs/timeout) budget six background hours per 24 hours for target 35+, shared by that service type; timeout must stop promptly. The documented budget is not imposed on connectedDevice, but absence of this budget is not a promise of endless execution. Do not restart services to conceal user/system cancellation.

| Event | Required design behavior, not yet implemented |
|---|---|
| Activity background/recreation | Same coordinator, callback registrations and single writer; observer detaches only. No second engine. |
| Screen off / lock | Notification and transfer persist if permitted; no screen-on dependency. FGS alone does not guarantee CPU wakefulness. |
| OS low-memory process kill | Treat persisted intent/partial as recoverable. If Android restarts the service, acquire a new fenced owner and reconcile; timing is not guaranteed. If ineligible, wait for user open and report USER_ACTION_REQUIRED then. No live Network/BLE object survives process death. |
| Phone reboot | Durable records survive credential unlock; first baseline design requires user open to start camera work. Boot is not a promised silent sync trigger. |
| Settings force-stop | Do not self-resurrect. Reconcile only after user reopens. [Stopped-state rules](https://developer.android.com/about/versions/15/behavior-changes-all#stopped-state) require user action and can cancel PendingIntents. |
| Task Manager stop | No callback guarantee. [FGS stop behavior](https://developer.android.com/develop/background-work/services/fgs/handle-user-stopping) removes the app while scheduled jobs may still run. Our policy prevents downstream maintenance from resurrecting a user-stopped camera session; inspect exit reason on next start. |
| In-app Pause / Cancel | Commit intent first, stop streams, flush/checkpoint, preserve partial; Cancel stops scheduling but does not delete originals or valid replicas. Next intentional Open may offer resume of a user-cancelled session rather than silently overriding cancel. |
| Permission revoke / Android approval / prolonged camera absence | Persist reason/action, release unneeded resources, stop bounded recovery. Activity handles permission dialogs only when user returns; notification supplies action. |

[Power guidance](https://developer.android.com/develop/background-work/background-tasks/awake) says an FGS is not itself a wake lock. For screen-off transfer, evaluate a timeout-bounded partial wake lock only if required, no covering API already holds one, and HIL confirms need. If selected, hold solely during active IO/checkpoint/session maintenance that cannot tolerate suspend, release on every stop/error, measure drain and never hold through long retry wait. No wake-lock permission or code is added now.

[Companion guidance](https://developer.android.com/develop/connectivity/bluetooth/companion-device-pairing) offers association/presence, with newer Android 16 presence APIs; it does not remove runtime network consent or establish stable camera identity. Do not require an unsupported camera profile.

Android 17 does not change our selected owner by assumption. Local-network authorization is a separately gated transport precondition; see ADR 0004. No SDK bump now.

## Validation before execution-model capability acceptance

TEST_MATRIX IDs X01-X07/N01-N05 must test visible start, immediate background, screen-off, process recreation, explicit stop, notification denial, and local-only network ownership. Run multi-GB and long-duration batches on the actual S25 Ultra; measure callbacks, stop reasons, memory, CPU, thermal/battery, bytes and single-owner fencing. Paused hardware session remains paused. Neither this ADR nor source review earns those PASS results.
