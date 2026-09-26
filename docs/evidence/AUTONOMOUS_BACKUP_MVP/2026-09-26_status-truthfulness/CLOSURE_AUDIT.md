# Closure audit — status truthfulness and durable refresh

Date: 2026-09-26

## Acceptance-chain re-check

| Area | Re-check | Result |
|---|---|---|
| Service ownership | The camera dispatcher alone owns automatic `DOWNLOAD`; the Activity only reads durable state. | PASS |
| Truthful active state | Only an active dispatcher/progress policy can render “Synchronisation läuft”. Open evidence actions render “Synchronisation offen”. The global card and each active video follow the same live source. | PASS |
| Inventory and plan | Incomplete source inventory remains a hard no-transfer boundary. Historical ambiguity remains distinct from the current inventory. | PASS |
| Integrity and identity | Existing local copies and ambiguous identities are not promoted without an actual proof. | PASS |
| Durable completion refresh | Dispatcher clears all transient per-video overlays then publishes terminal state; the external SSD coordinator publishes after real ledger-mutating copy work. | PASS |
| Lifecycle/stale ownership | Observer registration invalidates immediately; session/adapter guards reject stale grid painting; terminal decisions dominate stale progress. | PASS |
| Replica independence | SSD reappearance/catch-up remains camera-independent; no-work availability probe cannot recursively trigger observer refresh. | PASS |
| User-facing state matrix | Active, terminal, fail-closed, open-proof, stale replacement, per-video live overlay and external-replica completion rows are documented in `UI_TRANSITION_AUDIT.md`. | PASS |

## Reproducible evidence

`./gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon` completed with 538 JVM tests,
zero failures and zero errors. Debug APK SHA-256:
`ADB424D961F6566CE0F1C5790C24F3D76229D2EB7A8BC763020673A5B0E453F6`.

`tools/autonomy/control.py --check` could not run because this Windows host has neither `py` nor a
`python` launcher. This is recorded as a local control-tool limitation; it does not convert any
hardware criterion into PASS.

## Remaining boundary

No further safe software change is justified by this observation. The only remaining uncertainty
for this correction is physical rendering/timing on S25 and real SSD topology. Both are explicitly
batched in `docs/hardware/FINAL_COMPLETE_APP_HARDWARE_TEST.md`; no test result is fabricated here.
