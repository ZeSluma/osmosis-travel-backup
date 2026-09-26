# Delta analysis — complete non-destructive product scope

Status: ACTIVE. This supplements `DELTA_ANALYSIS_COMPLETE_APP.md` with the current repository
state, not a replacement for hardware evidence.

## Objective and decision rule

The product target is a truthful, autonomous camera → phone → SSD backup: automatic known-camera
connection, trusted source enumeration, durable scheduling and transfer, local integrity, safe
recovery, independent replica verification, understandable state, and privacy-safe diagnostics.
Camera deletion, release, production accounts, and cloud credentials remain deliberately outside
this non-destructive scope.

One hardware session is allowed only after every row marked software-testable below has current
reproducible evidence. A physical test records each observation as PASS, FAIL, INCONCLUSIVE, or
HARDWARE_DEFERRED; it never upgrades an unobserved claim.

## Delta matrix

| Area | Proven now | Remaining software measure | Physical evidence to bundle once |
|---|---|---|---|
| Camera → phone core (PC01–PC19) | Epoch fencing, untrusted-inventory refusal, durable automatic plan/dispatch, strict transfer, receipt/integrity, replica isolation, UI observation, callback and network ownership have focused tests. The 2026-09-26 audit repaired replacement-writer handoff: a fresh trusted epoch waits for the fenced predecessor release rather than becoming `USER_ACTION_REQUIRED`; completion is committed before that release. Pocket manual requests now delegate to the same service dispatcher, so the Activity is no Pocket writer. | Re-run the integrated state matrix after completion-scope work; preserve the user-stop, stale callback and partial-recovery cases. | Known camera automatic discovery, one safe new transfer with visible progress, background, controlled loss/rebuild. |
| PC20 truthful copy | Current terminal-inventory eligibility no longer inherits historical unresolved identity ambiguity. `LedgerModelTest` and `BackupStatusCopyTest` cover the distinction. | Verify observer refresh through the full durable-plan path and keep an incomplete list untrusted. | German summary must explain camera list/identity blocker without claiming a completed sync. |
| Progress and product surface | The service owns progress; the UI renders its privacy-safe percentage and distinct preparing, safe writer-handoff, transfer, receipt-update and review-required states. The launcher label is `Osmosis Travel Backup`; an independent generated camera-to-storage mark is used on Android 26+ without changing package/signature. | Do not make a visual percentage a completion source; retain ledger-derived badges and summary. | Confirm the bar updates during one safe new transfer and that final labels come from the receipt, not the transient bar. |
| R037 capture day | Resolver, deterministic day paths, collision handling, uncertainty and destination-path persistence are model-tested. `CaptureDayFaultMatrixTest` makes CD01–CD08 policy cases explicit, while the existing synthetic emulator fixture covers durable parent/reservation/restart behavior. | Do not infer unobserved Pocket timestamp/MIME/sidecar semantics from the policy matrix. | Capture-time/zone/sidecar facts on Pocket and real provider layout. |
| R040 optional GPS | Fresh launch is backup mode; GPS requires a new explicit choice and cannot suppress backup after recreation. A late Android permission approval also cannot revive GPS after the user has turned the mode off. | Exercise mode ownership and backup-vs-GPS arbitration in deterministic tests. | Optional, separately opt-in GPS behavior only; it must not be required for backup. |
| R041 diagnostics | App-private bounded event store, allowlisted fields, explicit export, verbose opt-in bounds and no restore are implemented. New typed lifecycle codes distinguish start, ready and recovery states. The audit removed the remaining raw throwable from the normal verbose-export failure sink and replaced GPS service strings with fixed non-identifying diagnostics. | Do not regress the source/sink audit; export boundary coverage is deterministic. | Export/share path and actual recovery reason output with no sensitive data. |
| R042 liveness | Recovery state machine, service ownership and stale callback fences are simulated. `LivenessFaultMatrixTest` additionally covers transient-loss exhaustion, revalidation-before-traffic, permanent causes and explicit-stop refusal without inferring a Pocket power cause. | Keep replacement/process-recreation coverage linked to `SessionRecoveryScenarioTest` and do not add an unmeasured keepalive mechanism. | KA01–KA08 timing/cause observations on S25/Pocket; no model-specific keepalive claim before that. |
| SSD/SAF | Safe allocation, staged replica, readback verification, restart/reappearance and catch-up fakes exist. | Regress SSD independence after the overall matrix; do not write without a grant. | Host topology, persisted grant, catch-up, interruption and readback. If the hub is unavailable, record HARDWARE_DEFERRED. |

## Execution order

1. Finish and commit the typed diagnostics/liveness change only after targeted regression passes.
2. Expand R037, R040, R041 and R042 deterministic integration/fault coverage.
3. Run the required visible-state matrix and full requirement/closure audit. Any gap reopens software work.
4. Build one signer-compatible debug APK in place.
5. Execute `docs/hardware/FINAL_COMPLETE_APP_HARDWARE_TEST.md` once, recording every step. The SSD branch is conditional and does not cause a separate camera test.

## Current exclusions

No camera original deletion, no data clear, no protected-media retransfer, no main merge, no release,
and no production cloud access are authorized.

## Audit checkpoint — 2026-09-26

The requirement/code/source-sink audit closed two software-correctable gaps before any hardware
request: a trusted replacement session now waits for a fenced predecessor writer to release and
then repeats all source/plan/identity checks; and the Pocket-specific manual action delegates to
the service-owned durable dispatcher. Raw export exceptions were removed from both the normal
log sink and user-visible error text.

Targeted ownership/recovery, progress and privacy suites passed. The full checkpoint
`:app:testDebugUnitTest :app:assembleDebug --no-daemon` passed with **514 JVM tests, zero failures
and zero errors**. Debug APK SHA-256:
`6CC9F3C551E9ABAB1A87FA3395EEC2555A3CB1DB6200B3D86241DDD4C4749F6B`.

R037, R040, R041 and R042 remain deliberately active: their explicit deterministic matrices and
physical-only facts have not been upgraded by this checkpoint.
