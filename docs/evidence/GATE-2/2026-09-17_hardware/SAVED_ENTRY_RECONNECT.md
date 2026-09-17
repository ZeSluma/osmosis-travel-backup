# Saved-entry reconnect failure — separate runtime observation

2026-09-17, user-observed during GATE-2 target validation on S25 Ultra/Pocket 4P with the verified G2 debug APK. Exact event time not captured.

- Action: direct connection through the existing saved Pocket 4 Pro entry.
- Result: **FAIL** for this direct connection attempt.
- UI reported that the camera was not in range and should be switched on. Custom camera name omitted from evidence.
- User confirms the camera was powered on.
- The user then initiated a fresh camera search/rescan. Rediscovery, connection result, grid visibility and media count were submitted as template placeholders, not actual results; all remain **UNCONFIRMED** pending clarification.

Follow-up confirmation: **rediscovery YES, connection SUCCESS, media grid visible**. No download started. Visible UI count was not supplied. Thus saved-entry attempt FAIL and fresh-rescan recovery PASS are independently established by the user's observation. This proves camera availability for the later successful connection, not the exact radio/session state or cause at the earlier failure. The two existing local downloads were confirmed present before this test; no ledger recognition or integrity claim follows from that alone.

Classification: **SAVED_ENTRY_RECONNECT_FAILURE**. Confirmed observation is the unsuccessful saved-entry attempt and misleadingly definitive availability wording. Camera power is user-confirmed; BLE advertisement/discoverability, Wi-Fi/AP readiness, protocol-session readiness and effective radio reachability at the failed attempt are not independently established. **ROOT_CAUSE: UNCONFIRMED.** The UI message is not proof of out-of-range, camera-off, sleep, stale credentials or background failure. Do not infer a stale saved identity or G2 credential-migration regression without evidence.

Keep this occurrence separate from AUTO_RECONNECT, AUTO_RESUME, FOREGROUND_SESSION_DROP and CAMERA_SLEEP_BEHAVIOR. Saved-entry selection is a manual action, not an automatic-reconnect test. A successful subsequent rescan, if confirmed, establishes recovery at that later observation; it does not retrospectively prove the failed attempt's exact radio/session state or root cause.

Requirement/risk mapping: R-028/R-029 require evidence-based failure reasons and appropriate recovery actions; RISK-017/RISK-019 are related reliability work, not causal attribution. Carry this distinct observation into G7 connection-controller validation. No repair, credential reset, camera setting change, transfer or deletion performed to investigate it. G2 ledger hardware checks remain pending; no gate PASS/FAIL is inferred solely from the unconfirmed rescan outcome.
