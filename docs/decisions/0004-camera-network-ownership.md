# ADR 0004 — Explicit camera Network ownership

Date: 2026-09-17. Status: selected design recommendation; hardware validation outstanding. No functional changes.

## Observed baseline and decision

ApJoiner.onAvailable calls bindProcessToNetwork; HttpClient uses URL.openConnection; DumlTransport creates DatagramSocket and DumlSession creates TCP Socket. Moving only HTTP would leave protocol/control traffic dependent on global routing. Activity-driven callback/session ownership is part of the redesign boundary, not proof of a specific observed failure cause.

Choose a CameraConnectionController owned by SyncCoordinator's service lifetime. It alone owns requestNetwork/callback registration and the current `Network` plus a monotonically increasing networkEpoch. Pass an immutable CameraNetworkContext to all camera traffic adapters. No Activity, replica worker or cached global variable owns the request. Persist camera association and recovery intent, never the Network object/netId as restart authority.

Use [Network APIs](https://developer.android.com/reference/android/net/Network) for each camera route: openConnection for HTTP, socketFactory for TCP, bindSocket on unconnected UDP sockets, and network-specific DNS if required. Recreate sockets on epoch loss; a live socket cannot be migrated by changing a variable. Per-Network transport removes the need for process-wide binding only after every control, thumbnail, preview and media path is audited. Future cloud clients must not receive CameraNetworkContext.

| Alternative | Disposition |
|---|---|
| Keep process-wide bind for everything | Rejected target design: unrelated sockets/DNS and future OAuth/cloud may be captured; stale binding affects the whole process. Preserve unchanged baseline now. |
| Global bind with sequential cloud only | Potential temporary migration fallback if an upstream path cannot yet be adapted. Must prohibit cloud while bound, release callbacks/unbind, verify actual default network before Internet operations; not the desired long-term design. |
| Per-Network camera routing plus sequential replication | Selected smallest robust default; first finish verified local snapshot, release camera request, then replicate when destination connectivity exists. |
| Simultaneous camera + cloud | Optional future optimization. Routing isolation is necessary but cannot create hardware concurrency/Internet availability; not required for local success. |

## Android contract versus target evidence

[WifiNetworkSpecifier guidance](https://developer.android.com/develop/connectivity/wifi/wifi-bootstrap) establishes a local-only request, with user approval; specific previously approved AP requests can reuse approval, while forgotten or pattern requests can require it again. onUnavailable includes denial or connection failure; it is not credential proof. Do not promise a service can display consent silently. Emit WAITING_FOR_ANDROID_WIFI_APPROVAL / USER_ACTION_REQUIRED when needed and bring the user to normal platform flow.

[NetworkCallback](https://developer.android.com/reference/android/net/ConnectivityManager.NetworkCallback) reports availability and loss for its request. onAvailable means a transport exists, not that DJI protocol session is READY. Await relevant capability/link updates, then handshake and verify protocol liveness. onLost invalidates the matching epoch and cancels its IO. Old callbacks cannot tear down a newer connection. onUnavailable, timeouts and protocol errors retain distinct reason codes; do not label all failures stale password.

[AOSP STA/STA concurrency](https://source.android.com/docs/core/connect/wifi-sta-sta-concurrency) is optional and depends on OEM radio/HAL/configuration support. [WifiNetworkSpecifier reference](https://developer.android.com/reference/android/net/wifi/WifiNetworkSpecifier) points to isStaConcurrencyForLocalOnlyConnectionsSupported on supported releases/targets. **S25 Ultra SM-S938B support is NOT_VERIFIED in this session**: there was no runtime capability result or concurrent transport test, and hardware is paused. A product Wi-Fi version claim is insufficient. Future HIL must record that boolean, both Network capabilities, actual camera traffic and a controlled Internet request, with mobile data on/off. Default to sequential replication even if capability reports true until behavior is proven.

Recover by bounded rescan of the associated camera, AP wake where supported, fresh Network request and fresh protocol session; validate asset identity before resuming. The source camera's display state is an observation only. BLE loss can be expected after a Wi-Fi handoff on some models; Pocket-specific liveness rules need measurements, not automatic teardown on every BLE event.

## Android 17 / local-network compatibility

[Official local-network guidance](https://developer.android.com/privacy-and-security/local-network-permission) now documents SDK 37 enforcement; the user's "future" wording is a migration horizon for this target-36 app, not a claim Android 17 is unreleased. Keep ACCESS_LOCAL_NETWORK absent at target 36. For a later target-37 migration, declare/check/request it for direct camera LAN access; a generic NSD picker is not assumed to authorize DJI raw TCP/UDP paths. Permission revocation is separate from bad credentials.

Planned Android 16 test only (NOT RUN during pause): enable `RESTRICT_LOCAL_NETWORK` for the test package, reboot, exercise denied/granted NEARBY_WIFI_DEVICES states against camera TCP and UDP; then disable the compatibility flag and restore recorded settings. Record OEM support/unsupported result rather than disabling protection to pass. Test target-37 permission grant/deny/revoke on Android 17 separately. Network-bound sockets do not bypass permissions. Commands and assertions are in TEST_MATRIX; no manifest edits in this task.

Security constraint: pin camera transport to validated endpoints learned from the paired session, reject cross-host HTTP redirects, exclude credentials from URLs/logs, and enforce cleartext only in the camera adapter plus reviewed platform policy. Per-Network routing does not authenticate an HTTP peer or replace integrity checks. Local backup completion never depends on cloud availability.
