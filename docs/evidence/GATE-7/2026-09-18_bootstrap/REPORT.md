# GATE-7 bootstrap — lifecycle-safe camera-session recovery

Branch: `codex/gate-7-lifecycle-recovery`, parent GATE-3 closure `490b7c627eefa8453d33e189f7d3b0cb390f106a`.

## Entry evidence

GATE-3 PASS proves fail-closed transfer, persistence and final target status binding. It does not provide service-owned camera execution, screen-off/background survival, automatic recovery or production append/resume.

The existing implementation keeps `ApJoiner`, `CameraSession`/datalink, BLE keepalive, rejoin counters and transfer-restart flags in `MainActivity`. This is incompatible with the accepted G7 ownership contract: an Activity cannot be the durable owner of a live Pocket session. Existing target observations retain separate causes: saved-entry recovery failure, foreground session drop, background loss, incomplete enumeration, idle/standby-like state and power-cycle-only recovery. None is attributed to a single cause.

## Bounded first implementation target

Implement an application/service-owned, fenced session coordinator interface whose effect adapters preserve the existing strict transfer/ledger rules. The Activity becomes an observer and command surface. Software tests must cover single owner, stale callback fencing, bounded retry, user-action-required reasons and process/lifecycle truth. Do not claim background survival or Pocket recovery before S25/Pocket HIL.

## Hardware queue boundary

No G7 hardware loop begins at bootstrap. Later consolidated target work must test foreground/background/screen-off separately, saved-entry recovery, network loss/reacquisition, accurate reason states and no duplicate writers. Source-continuity remains unavailable, so any partial continuation must preserve the G3 refusal unless independently justified.