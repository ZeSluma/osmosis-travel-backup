# Optional GPS recording sync and independent diagnostics

Accepted product boundaries, implementation NOT_TESTED. R-040/R-041; no functional changes. Hardware remains paused; controlled diagnosis is planned, not run.

## Independent product features

CAMERA BACKUP must fully discover/connect/enumerate/sync/verify/reconnect/resume and evaluate safe-to-clear with GPS Sync OFF and Save logs OFF. GPS RECORDING SYNC is explicit opt-in telemetry sent through the existing R-SDK GPS-controller path to a compatible camera. Never collect location merely because backup starts. Persist the preference separately from actual running/permission state; a remembered opt-in is not permission to restart a location service silently. Gate-1 reviews Android location/background/foreground-service eligibility before any automatic cross-session resumption design. Location permissions required by a particular Android discovery API, if any, must be explained separately from actively collecting fixes for GPS recording telemetry.

GPS service owns its telemetry lifecycle; SyncCoordinator owns backup. A shared connection arbiter prevents both claiming the same BLE/session resources. Baseline GpsSyncState locks media while GPS runs, so simultaneous operation is NOT_VERIFIED and must not be promised. Starting backup must not secretly enable GPS or silently stop an intentional recording session; show a clear mode conflict and offer explicit stop/switch if required. GPS being disabled/denied must never prevent the independent backup workflow.

Embedded GPS telemetry stays inside the original byte-for-byte backup; no extraction into diagnostics. Non-regenerable telemetry sidecars become KNOWN_REQUIRED recording members, using the parent capture day. Exact Pocket telemetry output/support still needs hardware evidence. Backing up an existing original containing GPS is not new phone-location collection.

## Always-available sanitized event channel

Separate lightweight typed events from optional verbose logging and critical ledger/audit. Required event vocabulary: SESSION_STARTED, CAMERA_DISCOVERED, BLE_CONNECTED, WIFI_AVAILABLE, CAMERA_SESSION_READY, NETWORK_LOST, SESSION_LOST, PROTOCOL_TIMEOUT, RECONNECT_SCHEDULED, RECONNECT_ATTEMPT, RECONNECT_SUCCESS, RECONNECT_FAILED, TRANSFER_STARTED, TRANSFER_INTERRUPTED, RANGE_RESUME, TRANSFER_VERIFIED, PROCESS_RECOVERY, CAMERA_SLEEP_DETECTED, USER_ACTION_REQUIRED. Additional schema-versioned events may describe enumeration, stop and cleanup. CAMERA_SLEEP_DETECTED requires affirmative evidence; otherwise emit observation/UNKNOWN cause, not an inferred root cause.

Persist UTC timestamp plus monotonic ordering, opaque session/transfer IDs, safe old/new state and reason/confidence codes, retry count and bounded numeric technical context (bytes, offset, validated HTTP status, platform stop reason). Correlate connectivity callbacks, BLE, Wi-Fi Network epoch, protocol session, foreground/background and camera observations without SSID/MAC, raw URLs, filenames or personal device names. Never persist credentials/tokens, Wi-Fi passwords, GPS coordinates, media contents or unnecessary PII. No raw exceptions/packets to an ordinary sink.

Proposed bounded implementation budget: app-private rotating event store, max 7 days or 10 MiB, whichever reached first. Nonblocking bounded ingestion, safe dropped-event count, rate limiting; diagnostic storage failure cannot make backup depend on logging. Ledger recovery truth and cleanup audit remain separately durable; failure to persist critical transaction/authorization state blocks the corresponding operation. Validate observability overhead and diagnostic failure paths with verbose both off/on.

## Advanced temporary verbose mode

OFF by default; deliberately enabled and visibly labeled Diagnostics, independent of backup/GPS toggles. Proposed limit: stop at diagnostic-session end, 30 minutes or 10 MiB, whichever first; permission/explicit stop also ends it. Do not automatically restore enabled verbose mode after process restart. Parameters may be reviewed without weakening the requirement for bounded duration/size. App-private storage, schema/field sanitization before persistence, explicit sanitized export only, never automatic upload. Exclude opaque raw packet bytes that may carry GPS/secrets even if a caller labels them debug; where safe redaction is not possible, omit the field/event payload. Export receives another allowlist check and temporary sharing permission.

No coordinate logging exception is enabled. The user's general mention of a possible deliberately enabled GPS diagnostic session does not override the explicit never-persist list or INV-005 in this design; any future exception requires separate policy review. Verbose logging is not a workaround or prerequisite for stable reconnect.

## Read-only baseline findings

At baseline `2fcdbc97e6dbefc875d425368be67cf32b50bb06`:

- MainActivity lines 325-355 persists `save_logs` and `gps_mode`, both default false; saved log preference can start logging on launch. GPS mode selects the separate service path.
- GpsService uses a location foreground service, R-SDK and phone fixes; START_NOT_STICKY and explicit stop are visible. GpsSyncState documents BLE ownership contention and locks media flows. This is not proof GPS caused the observed awake foreground drop.
- FileLog writes caller-provided strings to app-specific external files, keeps five files, flushes every write; it has no central typed redaction and no per-file byte/time cap. A comment forbidding coordinates/credentials is not enforcement.
- MainActivity.logLine sends text to logcat even when Save logs is OFF. Its share action gzips an existing log through a chooser, not a proven sanitized exporter. Debug hooks and camera/delete messages can carry sensitive or personal values. No raw runtime logs were accessed/exported here.

These are design gaps for Gate-1 disposition, not fixed now. Target requirements apply to all diagnostic sinks including logcat, file logging and exports; simply turning off FileLog is not sufficient proof of privacy.

## Planned foreground-drop reproduction

After explicit hardware resume, review upstream log producers for secrets/GPS first. Use baseline Save logs only temporarily if needed and only if the exercised paths meet sanitization constraints; otherwise collect allowlisted event summaries without persisting raw packets. Record known toggle state and end the temporary session explicitly. Never ask the user to keep logging enabled for reliability.

Correlate timestamps for Android network availability/loss, BLE, protocol ready/timeout, Network epoch, Activity visibility and user-confirmed camera awake/sleep state. Preserve distinct candidate causes: Wi-Fi loss, BLE/session loss, camera protocol termination, app state handling, camera AP behavior, or unconfirmed. Run paired logging-off/on cases when safely authorized, keep GPS off unless separately testing telemetry, and never attribute root cause from a camera display alone. Existing FOREGROUND_SESSION_DROP remains unconfirmed; no diagnosis or resumed hardware work in this task.
