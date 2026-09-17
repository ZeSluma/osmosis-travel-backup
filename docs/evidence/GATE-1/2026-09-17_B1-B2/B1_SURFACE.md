# B1 before/after attack surface

Parent/bootstrap `efb4830e435b1efeef7a6de56fde0ea609806171`; main/unchanged functional baseline `2fcdbc97e6dbefc875d425368be67cf32b50bb06`. Dedicated branch `gate-1/security-foundation` was created from that verified parent, not main. User explicitly authorizes only B1/B2 security implementation; general product implementation remains false. This report complements the earlier read-only SURFACES inventory rather than replacing historical evidence.

## Before (source reconstructed prior to edits)

MainActivity is exported for MAIN/LAUNCHER and singleTop; explicit intents from any installed app can bypass intent-filter matching. There was no caller/build guard on these inputs. onCreate did not validate action/category/data before consuming extras; onNewIntent read shortcut_mac regardless of action/category/data. No URI/deep-link command parser was present.

| Input / entry | Reachable behavior |
|---|---|
| pin / onCreate | Overrides model pairing token and interpolates supplied value into ordinary log |
| nojoin / onCreate | Skips Wi-Fi join/process bind and uses current network for datalink |
| nowriterefresh / onCreate | Changes CameraSession write-session refresh policy; affects later user writes, not an immediate delete command |
| pageforce / pagesize / pageauto / onCreate | Changes enumeration paging rules/size and automatically requests more pages |
| autoscan / onCreate | Schedules camera scan |
| wifi + ssid/pass / onCreate | Starts Wi-Fi/session flow with caller input |
| offload + pick / onCreate | Scans and chooses matching camera/name/brand to connect and enumerate |
| shortcut_mac / onCreate | Stores auto-connect target and starts scan |
| shortcut_mac / onNewIntent | Tears down current offload, returns selector, changes target and scans unless GPS service lock is active |

These controls initiate camera/network/session behavior and can influence subsequent writes or fetch thumbnails/metadata. The reviewed launch branches do not call the download/delete button handlers directly, export diagnostics, or toggle GPS preferences. Existing ordinary launch may restore Save Logs/GPS UI preference and scan; that behavior is independent of test extras. No assertion that an extra directly deletes originals or starts a full download is justified by this call graph. The normal grid Download/Delete/GPS controls are outside this narrow fix.

## Selected smallest safe design

Remove launcher test commands entirely in both build variants (option C). No new debug component, signature permission or BuildConfig.DEBUG trust boundary. Existing unit tests and ordinary UI suffice for currently required development/hardware verification; removed ADB diagnostic shortcuts are intentionally unavailable, not secretly relocated. Any future internal test mechanism must be separately justified and isolated.

Keep MainActivity exported for normal launch. Replace its retained Intent with a clean explicit Intent before framework consumers; both entry points route shortcut hints through the same bounded policy. Only ACTION_VIEW, no URI/data/categories, syntactically valid 17-character MAC matching an already saved camera can produce a confirmation target. Malformed/wrong-type/unparcelable bundles fail closed. Other actions/extras confer no command authority; ordinary launch still opens the selector and does its existing scan.

A camera shortcut is forgeable, even when the address is valid. Therefore validated hints prompt an in-app confirmation, and only its positive callback can switch an existing session and connect. The callback rechecks saved membership and the existing GPS lock. Cancellation/unknown target causes no connection or teardown. No camera identifier or supplied PIN is logged by the new entry path. This confirmation is the narrowly necessary B1 shortcut behavior change, not a product feature or GPS implementation change.

Protocol debug fields remain in baseline CameraSession for upstream unit/internal use, with no external launcher setter. Manifest/components, FileProvider paths/grants and immutable GpsService PendingIntent are unchanged. The merged debug/release inventories and tests are recorded in the final report. Eight pure-policy regression tests cover launcher/unknown actions without reading extras, data/categories, unknown/forgotten cameras, invalid/oversized values, malformed reader failure and canonical saved identity. A platform-only androidTest harness exercises real Activity cold/warm entry on an empty emulator; it must never run on an existing user's paired-camera installation.
