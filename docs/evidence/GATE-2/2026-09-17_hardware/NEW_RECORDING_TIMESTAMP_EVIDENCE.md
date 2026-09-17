# New recording and timestamp evidence

2026-09-17. Separate user-observed finding: **IDLE_OR_STANDBY_LIKE_STATE**, exact power/session state UNCONFIRMED. Green status light ON, fresh Rescan required, camera found, normal connection succeeded and new short video visible. No media selected/downloaded/deleted/modified. Do not merge with CAMERA_UNREACHABLE_UNTIL_POWER_CYCLE, CONNECTED_BUT_MEDIA_ENUMERATION_EMPTY or saved-entry failure; LED ON is not proof of standby or remote-wake mechanism.

## Three-asset verification

Existing read-only hardware audit exit0,0.90s: [exact output](../2026-09-17_local-reconciliation/new-recording-readonly-audit.txt). Schema3, sources1, snapshots5, assets3, recordings3, members3, replicas3. New snapshot2a30e319da4d5295923613320219406763ee0a2a60a8e6c1d10437003180f308 has3 members, INCOMPLETE/all_coverage_proven=false. This confirms3 currently enumerated media, not exhaustive all-original/sidecar coverage.

[Comparison](../2026-09-17_local-reconciliation/new-recording-comparison.json): exactly one new asset; both prior IDs/group links/path/local-reference identities, sizes, states and policy unchanged, no duplicates. New asset opaque hash ee17e7b7994ca5ef0cd2a73fc0b791dc21b8e34847207a1ea67ff2261300c369,103945077 bytes, KNOWN_REQUIRED, DISCOVERED, local_presence ABSENT, candidate_count0, local_locator absent, committed_bytes0. Production policy recomputes DOWNLOAD for the unsatisfied new original; this is a plan only, not a started transfer or live queue export. Old2 remain LOCAL_PRESENT_UNVERIFIED/PRESENT_UNVERIFIED with VERIFY_EXISTING and one candidate each. Metadata-only stat protected old files38447651 and3071380142 unchanged.

## Exact timestamp fields for the new recording

- REMOTE_FILENAME: `DJI_20260917184037_0003_D.MP4`.
- REMOTE_TIMESTAMP: null in persisted asset.remoteTime; no separate timestamp surfaced by current adapter.
- REMOTE_TIMESTAMP_SOURCE: NONE_EXPOSED_BY_CURRENT_POCKET_PARSER. This is not proof the raw protocol has no timestamp anywhere.
- MEDIA_METADATA_TIMESTAMP: NOT_TESTED; MP4 payload/embedded metadata not accessed, no download.
- TIMEZONE_OR_OFFSET_PRESENT: no offset in filename; camera timestamp timezone UNKNOWN. Persisted resolution_zone Europe/Berlin and timestamp Z belong to phone-derived fallback, not camera metadata.
- FILENAME_TIMESTAMP_IF_ANY: `20260917184037`, syntactically2026-09-17 18:40:37, unverified naming semantics and timezone. Do not promote to VERIFIED_FILENAME.
- RESOLVED_CAPTURE_TIMESTAMP: `2026-09-17T16:46:04.285117Z` — allocation/sync fallback, **not proven capture time**.
- RESOLUTION_RULE_USED: time_source SYNC_FALLBACK, fallback SYNC_TIME_FALLBACK, allocation Instant converted using phone zone Europe/Berlin for day reservation.
- RESOLVED_CAPTURE_DAY: `2026-09-17` (fallback reservation).
- DESTINATION_PATH: persisted relative reservation `2026-09-17/DJI_20260917184037_0003_D-c27205149b442d47f537bea3.MP4`. No physical folder/file created; destination PHONE_LOCAL. Do not claim an actual Movies path exists for this recording.
- CONFIDENCE: UNCERTAIN. Exact capture time, camera offset and filename authority not established.

The approximate user clock/date note is an external plausibility window only, not camera-displayed time, metadata or exact-minute oracle. No numeric bounds were supplied in the current correction/observation, so no quantitative agreement or exact expected timestamp is asserted. Filename and fallback share the same calendar-date string; that alone cannot validate travel/day/offset correctness. Existing on-connect syncTime is a confounder. Source inspection: CameraSession constructs CameraFile without mtimeEpoch (default0); CameraFile.timestamp extracts a14-digit filename substring. CameraLedgerAdapter persists only positive mtimeEpoch as remoteTime and supplies no trusted capture candidates. Thus observed null/fallback can reflect parser/adapter limitations; do not conclude Pocket lacks any usable capture metadata. Old file dated20260916 in its name also remains reserved under sync day2026-09-17, illustrating why fallback is not verified capture-day organization.

## Diagnostic scope and validation

Added opt-in `timestampEvidence=read-only` inside existing `hardwareAudit=read-only` early-return path of the separate androidTest runner. It reads one schema3 SELECT with SQLite OPEN_READONLY and preserving corruption handler; no Room initialization, network, credentials, media or database mutation. Outputs only opaque asset IDs, strict DJI numeric filename/path patterns, numeric/null remote time, bounded ISO-like timestamp, valid zone IDs/offsets and allowlisted confidence. Unexpected names/paths withheld. Default audit unchanged; no production app/dependency/Gradle/config change or production APK reinstall.

LF build command `./gradlew.bat :app:assembleDebugAndroidTest --no-build-cache --console=plain --no-daemon`: PASS,23s, exit0; existing SDK XML/deprecation warnings retained. New separate test APK SHA256 C8BBF13E54515D4FB8ED4C568C344A1398F242516A33555A6301B333CDD89B67. `adb -d install -r <LF>/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk` Success. `adb -d shell am instrument -w -e hardwareAudit read-only -e timestampEvidence read-only dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation` successfully returned [timestamp projection](../2026-09-17_local-reconciliation/new-recording-timestamps.txt). Combined install/audit/stat exit0,3.07s. Controlled instrumentation replaces app process; not a spontaneous connection failure. No synthetic mutation suite run on phone. Actual projection validates query/format against3 rows; prior full335-unit results not represented as rerun for this diagnostic-only change.

New-asset ingestion/planning and preservation subset PASS. G2 remains BLOCKED pending truthful acceptance disposition of capture-time/type/relationship hardware criteria; no complete-original, accurate capture-time, byte-integrity or gate-PASS claim.
