# Next software session — evidence-driven recovery repair

Start from the final S25 artifact and the 2026-09-18 report. Do not repeat camera/SSD observations
until a targeted build exists. The current hardware findings are reproducible and sufficient to
authorize narrowly scoped software diagnosis.

## Integrated completion contract

This is one product objective, not three tickets. Do not return after an individual fix, test,
build, commit, or partial device result. Continue until automatic power-cycle recovery, Rescan
auto-selection, trusted durable planning, and one safe automatic scheduling path work together on
the S25, or a genuine external hardware blocker prevents the integrated retest. Preserve every
existing identity, integrity, no-duplicate-writer, explicit-stop, and fail-closed invariant.

## Implementation checkpoint — 2026-09-18

The recovery implementation is now present but not hardware-PASS: an unexpected live-grid GATT
disconnect no longer invokes the explicit-stop selector path. It retains the connection host and
executes at most three epoch-fenced BLE recovery scans. A saved in-range hit marks connection
selection before asynchronous credential lookup, preventing scan-timeout/user-action races.
`CameraRecoveryScanPolicyTest` proves bounded retries and explicit-stop refusal.

The plan display now adds only privacy-safe diagnostics: snapshot completeness reason plus current
and historical unresolved-observation counts. It does not alter planning, identity resolution, or
verification semantics. The targeted regression suite and the full JVM/debug checkpoint passed:
460 tests, zero failures/errors. The resulting debug artifact SHA-256 is
`C5DA2E5C6A72FA111BA1A50F8715950B43CE1EC145B3D84820C72A95D00D5B66`.

Next required action is the single focused in-place S25 retest of the integrated completion
contract. Do not claim PASS from this software checkpoint alone.

## Priority order

1. Trace why controlled Pocket power-off leaves no `CameraConnectionService`, despite a foreground
   connection before the loss. Repair only the service-owner/recovery transition required to retain
   bounded reconnect behavior; explicit user stop must remain terminal.
2. Trace saved-camera selection after a Rescan. When a saved MAC becomes `in range`, the normal
   automatic path must select it exactly once; stale scan callbacks must still be fenced.
3. Add aggregate, privacy-safe source-completeness diagnosis for the durable plan. Determine why a
   non-empty six-item real grid with `review=0` reports `complete=false`. Preserve unresolved
   identity/history fail-closed semantics unless a generation-scoping defect is proven.
4. Add focused regressions for each confirmed root cause, then build/install in place and retest
   only: normal automatic start, one power-cycle recovery, one Rescan auto-select, and plan counts.

## Non-negotiable safety limits

- Do not clear app data, delete camera media, force a Download, or loosen VERIFIED/completeness.
- Do not retry physical SSD topology until a verified local test receipt exists and the S25/dongle
  path is available; SSD remains deferred rather than failed.
- Keep diagnostics to state/reason/counts; never extract media names, database rows, credentials,
  GPS, or storage paths from the S25.
