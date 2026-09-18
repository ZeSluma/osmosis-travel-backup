# Next software session — evidence-driven recovery repair

Start from the final S25 artifact and the 2026-09-18 report. Do not repeat camera/SSD observations
until a targeted build exists. The current hardware findings are reproducible and sufficient to
authorize narrowly scoped software diagnosis.

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
