# Separate baseline finding: FOREGROUND_SESSION_DROP

## User-observed conditions

- Pocket 4P was awake; no sleep/standby for this occurrence.
- Osmosis remained foreground.
- No deliberate phone Wi-Fi change or app switch.
- Camera display still showed "Playback running on DJI Mimo".
- Osmosis nevertheless reported camera connection failure.

FOREGROUND_SESSION_DROP: OBSERVED (user-reported).
SESSION_STATE_DESYNC: CANDIDATE, not confirmed.
ROOT_CAUSE: UNCONFIRMED.
AUTO_RECONNECT: FAIL remains the recorded result.

The camera's display text and Osmosis failure indicate differing visible states; neither establishes that a usable network/session remained alive or that DJI Mimo was actually running on the phone. No transport capture or synchronized callback evidence establishes root cause.

Explicit exclusions for this occurrence: do not classify as CAMERA_SLEEP_DISCONNECT or APP_BACKGROUND_DISCONNECT. Keep separate from the earlier background-return failure, intentional Wi-Fi interruption, automatic job-resume failure and restart UI recognition failure. Camera-sleep behavior remains independently unverified; this occurrence is not sleep evidence.

Recovery pending: user will return to camera list, rescan and reconnect with unchanged credentials. No credential change, download, requeue, media mutation or functional fix is authorized by this recovery observation. Preserve outcome and number of attempts if reported; successful manual recovery must not erase the initial session drop.

Future architecture work: distinguish transport reachability, protocol-session health, camera display state and application connection state. Define measured failure detection and safe recovery for an awake camera with the app foreground. Do not prescribe a protocol rewrite or attribute this to credentials without evidence. Cross-reference RISK-019.
