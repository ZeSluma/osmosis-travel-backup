# GATE-7 bootstrap — lifecycle-safe camera-session recovery

Branch: `codex/gate-7-lifecycle-recovery`, parent GATE-3 closure `490b7c627eefa8453d33e189f7d3b0cb390f106a`.

## Entry evidence

GATE-3 PASS proves fail-closed transfer, persistence and final target status binding. It does not provide service-owned camera execution, screen-off/background survival, automatic recovery or production append/resume.

The existing implementation keeps `ApJoiner`, `CameraSession`/datalink, BLE keepalive, rejoin counters and transfer-restart flags in `MainActivity`. This is incompatible with the accepted G7 ownership contract: an Activity cannot be the durable owner of a live Pocket session. Existing target observations retain separate causes: saved-entry recovery failure, foreground session drop, background loss, incomplete enumeration, idle/standby-like state and power-cycle-only recovery. None is attributed to a single cause.

## Bounded first implementation target

Implement an application/service-owned, fenced session coordinator interface whose effect adapters preserve the existing strict transfer/ledger rules. The Activity becomes an observer and command surface. Software tests must cover single owner, stale callback fencing, bounded retry, user-action-required reasons and process/lifecycle truth. Do not claim background survival or Pocket recovery before S25/Pocket HIL.

## 2026-09-18 implementation checkpoint

Implemented an application-owned durable session runtime and registered `connectedDevice` foreground
service host. The runtime persists only epoch/recovery/explicit-stop truth (never credentials,
SSID, Network, source locator or media data), rejects callbacks for an older epoch, and does not
permit a user-stopped session to revive. MainActivity now starts the host from an explicit offload
command, fences Wi-Fi callbacks and strict-transfer cancellation with a captured epoch, and only
marks the runtime READY after a fresh complete, nonfailed enumeration. Empty, paged or failed
enumerations remain `INCOMPLETE_UNTRUSTED`; the existing ledger observation continues to retain
historical assets and does not infer deletion.

`DurableSessionRuntimeTest` covers process-restart restoration, stale/replacement epochs, explicit
stop, bounded repeated loss, and incomplete-versus-complete post-reconnect revalidation. It is a
deterministic unit seam: no S25/Pocket conclusion is drawn from it.

The same runtime now owns strict-transfer allocation with an opaque epoch/generation token. A
second Activity click/recreation cannot allocate a second writer; an old worker's `finally` cannot
release a successor; and process replacement discards only that in-memory writer lock while the
ledger's durable PARTIAL evidence remains for later revalidation. This adds deterministic coverage
for duplicate allocation, stale transfer completion and process-death transfer recovery boundaries.

Verification attempt: `gradlew.bat testDebugUnitTest --tests
dev.konraditurbe.osmosis.connection.*` could not configure because this host exposes Java 11 and
Room 2.8.5 requires Java 17+ (the project targets Java 21). This is `NOT_TESTED`, not a test pass;
use the repository's isolated JDK 21 environment for the authoritative checkpoint.

Status: **GATE-7 INTERNAL MVP NOT_YET_PASS.** The service/runtime boundary and transfer fence are
implemented, but the remaining Activity-owned BLE/datalink/AP effect adapters still need to be
moved behind the service owner and verified by the targeted JVM/emulator suite. No background or
hardware-survival claim is made.

## Hardware queue boundary

No G7 hardware loop begins at bootstrap. Later consolidated target work must test foreground/background/screen-off separately, saved-entry recovery, network loss/reacquisition, accurate reason states and no duplicate writers. Source-continuity remains unavailable, so any partial continuation must preserve the G3 refusal unless independently justified.
