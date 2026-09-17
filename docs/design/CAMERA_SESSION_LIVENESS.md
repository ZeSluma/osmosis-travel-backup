# Camera/AP/session liveness — GATE-7 requirement

2026-09-17 user clarification, R-042. Architecture/test requirements only during G2; no keepalive implementation, camera power-setting change or new transfer is authorized by this document. All Pocket4P timing/mechanism claims below remain NOT_TESTED until controlled HIL.

## Separate mechanisms and evidence

1. Camera Auto Power Off / device standby / display sleep.
2. Camera Wi-Fi AP idle sleep or network disappearance.
3. BLE control-link idle/disconnection.
4. Datalink/playback session timeout/desynchronization while AP or camera may remain available.

The user reports a Pocket4P Auto Power Off setting. Its exact firmware behavior is unmeasured here. Upstream `docs/01-protocol-map.md` describes AP idle, HTTP/UDP activity and model-specific TCP7001 heartbeat, and separately BLE idle. Its introductory verified-model scope is Nano/Xtra; approximate10s AP,2s ping and5–6s BLE observations are **not Pocket4P timing requirements**.

Current `CameraSession.kt` already has a datalink-keepalive loop with acknowledgments, app-presence and playback maintenance. Therefore the new investigation must not assume the observed loss was caused by absence of all heartbeat traffic. Source comments describe model-dependent behavior and commands that may disrupt playback; adding a generic poll blindly is unsafe. Inventory actual emitted operations/cadence and ownership before choosing a target-specific mechanism. Source code existence does not establish effective Pocket4P keepalive, lifecycle survival or root cause.

Saved-entry failure followed by successful fresh rescan, awake FOREGROUND_SESSION_DROP, Android background loss and provisional camera sleep remain independent observations. "Playback running on DJI Mimo" is camera UI terminology; it does not prove the Mimo app is running or owns the session. Auto Media Transfer/Mimo competition is a hypothesis requiring evidence.

## Owner and recovery contract

CameraConnectionManager (the CameraConnectionController architecture role) owns the active camera-specific Android Network and protocol session under the lifecycle-safe connectedDevice execution host. MainActivity is an observer, never the keepalive lifetime owner. During ACTIVE_SYNCHRONIZATION_SESSION:

`SESSION_ACTIVE -> CAMERA_READY -> KEEPALIVE_ACTIVE -> HEALTH_MONITORED`

Measure Network availability separately from actual protocol readiness. Monitor bounded liveness/deadlines, detect loss promptly against measured tolerances, preserve reason confidence, and recover safely when possible:

`CONNECTION_LOST -> classify/retain UNKNOWN -> BLE rediscovery if needed -> AP wake -> request/join Network -> rebuild datalink/playback -> validate camera/source identity -> return READY to sync engine -> revalidate/continue PARTIAL`

One fenced owner; no competing heartbeat writers or stale callbacks. Automatic recovery must not silently resume user-cancelled work or claim it can bypass Android force-stop/consent. UI backgrounding or screen-off cannot stop keepalive solely because Activity pauses. Bound traffic, retries and resource ownership; pause/stop releases them intentionally. Normal operation must not require repeated Back/Cameras/Rescan/Select/Retry. Genuine unavailable camera, revoked permissions or unavoidable firmware constraints surface concrete USER_ACTION_REQUIRED rather than speculative password/range diagnosis.

Auto Power Off need not be permanently disabled for normal backup. Document changing it only as optional mitigation/controlled experiment. Any truly unavoidable setting must be proven on the exact hardware/firmware and surfaced explicitly; default settings remain the primary acceptance scenario.

## Controlled S25 Ultra + Pocket4P experiment (future G7)

Before each run record build/firmware, exact start/stop monotonic times, original Auto Power Off and Auto Media Transfer settings, display state, phone foreground/screen state and whether a transfer is active. Use sanitized reason-coded BLE/Network/AP/protocol transitions and packet counts/types/timing only; no credentials, identifiers, GPS, media content or raw packet/log dump. Keep clock synchronization effects separate. No existing original or completed phone file may be altered/deleted/re-downloaded for these experiments.

| ID | Controlled condition | Required evidence / oracle |
|---|---|---|
| KA01 / A | Connected, deliberately no keepalive and no transfer in a reviewed test configuration | Time-to-loss for AP, BLE and playback/datalink separately; document any remaining traffic so this is truly a no-keepalive control. Awake/display-only sleep/standby distinguishable; right-censor runs with no observed loss. |
| KA02 / B | Evidence-backed keepalive active, idle, same settings | Repeated sessions significantly longer than measured control timeout, predeclared duration and repetitions; establish reliable cadence/interval range and timing margin with bounded traffic. Compare HTTP/UDP/model-specific TCP only where justified; no borrowed Pocket timeout or arbitrary PASS after a short wait. |
| KA03 / C | Authorized non-critical active transfer | Whether transfer traffic alone maintains AP/session, including gaps/backpressure; evidence of continued transfer and no false complete. No re-download of existing protected originals solely for this test. |
| KA04 / D | Screen off and app background, separately then combined | Keepalive owner survives Activity pause; timing/Network/session and transfer truth remain observable. Distinguish OS process/service stop and screen state from camera-side loss. |
| KA05 / E | Separately agreed temporary Auto Power Off change, matched controls | Restore original value afterward; separate display sleep, actual power/standby, AP and session behavior. Prove any dependency, never silently make disabled auto-off a prerequisite. |
| KA06 / F | Controlled AP/session loss, G7 recovery implemented | Automatic rediscovery/AP wake/Network reacquisition/datalink/source validation followed by safe partial revalidation/resume, without repeated UI interaction or reset/deletion. |
| KA07 | Auto Media Transfer/Mimo inactive/active, one variable at a time | Establish whether competing app/feature actually changes AP ownership, BLE, playback, Wi-Fi or Osmosis session; UI wording alone is not proof. Record/restore settings, avoid unintended automatic transfers; stop before an ambiguous operation. |

All KA01–KA07 NOT_TESTED. If a control cannot be safely isolated, classify BLOCKED/NOT_TESTED rather than invent a cause or baseline. Keep G2 local-copy reconciliation progressing; this requirement does not authorize premature G7 changes.
