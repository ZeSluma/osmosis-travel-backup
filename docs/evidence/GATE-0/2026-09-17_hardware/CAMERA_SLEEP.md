# Separate baseline finding: CAMERA_SLEEP_BEHAVIOR

Status: observation pending corroboration. User reports Pocket 4P appears to enter sleep/standby after inactivity. This is separate from AUTO_RECONNECT, AUTO_RESUME, and Android background/return behavior. Screen dimming/off alone does not establish camera sleep or transport loss.

## Additional idle connection failure — observation still in progress

User reports another camera connection failure while simply waiting, with no deliberate connection change. Camera-side sleep/standby is a hypothesis, not an established cause; no app-background transition is established for this event. Do not merge this event into the earlier Android-background finding or attribute it to stale credentials.

The user will inspect whether the Pocket 4P is actually sleeping/standing by, wake only the camera, and observe Osmosis without app interaction. No result is available yet. Preserve the pre-wake camera indicators and app connection state, wake action and approximate wait duration, and whether the session returns automatically. Connection loss is now user-observed during idle waiting, but its relationship to sleep, individual Wi-Fi/BLE transport states and active-transfer risk remain unverified.

CAMERA_SLEEP_BEHAVIOR must not be finalized until this observation is complete. The pending final hardware evidence/state commit is held to include the outcome, without changing app or camera settings or starting any transfer.

Subsequent clarification of this occurrence: user confirms the camera was awake, Osmosis foreground and no intentional Wi-Fi/app switch. Camera showed playback running while the app reported failure. This occurrence is explicitly excluded from CAMERA_SLEEP_DISCONNECT and recorded separately in FOREGROUND_SESSION_DROP.md. It does not establish any actual sleep/wake behavior. The earlier apparent idle sleep hypothesis remains provisional, with active-transfer sleep risk and wake recovery NOT_TESTED.

| Classification | Current result |
|---|---|
| CAMERA_SLEEP_TRIGGER | Suspected inactivity trigger, user-reported; actual standby versus display-only timeout, duration and power state NOT_VERIFIED |
| CAMERA_SLEEP_CONNECTION_EFFECT | Wi-Fi, BLE and application-session effects independently NOT_VERIFIED; no timestamp-correlated sleep/transport observation |
| CAMERA_WAKE_RECONNECT | NOT_TESTED; no verified sleep/wake cycle with recorded automatic versus manual session recovery |
| TRANSFER_AT_RISK_DURING_SLEEP | UNKNOWN / NOT_TESTED; possible risk, not a demonstrated active-transfer failure and not proven idle-only |
| CURRENT_CONFIDENCE | LOW for the sleep mechanism/causality; user report supports an apparent idle state transition only |

Osmosis detection of the suspected transition: NOT_VERIFIED. Prior TEST E intentionally disabled phone Wi-Fi; it cannot establish camera sleep behavior. Its completed manual resume does not prove immunity to camera sleep. Prior Android background failure likewise cannot establish camera sleep causality.

## Controlled observation needed

Keep Osmosis foreground, USB connected and phone Wi-Fi/Bluetooth unchanged. No active transfer or queued retry; retain all existing files. First record camera visibly awake, app connection status, media-grid presence and time. Let camera become idle naturally without changing power settings. At the visible transition, record elapsed idle time, whether only display went dark, visible camera indicators, and app connection/error state. Capture numeric/status-only diagnostics when available; do not treat intentionally ended BLE pairing transport as a sleep-induced failure.

Wake the camera using its normal non-recording, non-destructive wake interaction, without altering power settings or starting a recording. Observe app/session restoration before any manual reconnect. If automatic restoration does not occur, record observation duration and normal manual steps needed. Do not attribute generic reconnect failure to sleep without confirming the state transition.

Active-transfer sleep behavior remains NOT_TESTED until separately authorized/arranged using a safe test job. Current TEST F prohibition on re-download remains in force; no transfer will be initiated just to test this hypothesis. No existing original/completed phone file is to be modified or deleted.

## Future architecture requirement

One-tap backup must account for camera-side availability/power transitions independently of Android activity/process lifecycle and phone-network loss. GATE-1 must define observable camera states, distinguish display timeout from transport/session loss, and specify safe wait/wake/reconnect/job-continuation behavior when supported. Unknown camera availability must never be interpreted as successful completion or justify deleting/resetting data. Validate idle and active-transfer cases at appropriate later hardware gates. No keepalive, wake command, power-setting change or functional fix is implemented here.

Cross-reference: RISK-016 in docs/RISK_REGISTER.md. This provisional finding does not overwrite measured AUTO_RECONNECT/AUTO_RESUME failures or TEST F's observed UI recognition failure. Final GATE-0 disposition is in REPORT.md; sleep characterization remains separate NOT_TESTED future-gate work, not a condition silently treated as passing.
