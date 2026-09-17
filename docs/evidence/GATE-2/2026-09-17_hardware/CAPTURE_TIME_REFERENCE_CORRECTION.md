# Capture-time reference correction and pending new-asset observation

2026-09-17 explicit user correction: Pocket4P did NOT display a recording date/time. The user's note came from an external date/clock reference around creation of the short recording. It is only an approximate real-world plausibility window, not authoritative camera metadata and not an exact-minute oracle. No precise reference window was supplied in this correction; do not invent one. For this new recording, this supersedes any description suggesting the user read capture time from the camera screen. The earlier17:26 connection-test note is a separate historical observation and is not a reference for this new recording. No claim is made about which timestamp source the Pocket exposes.

Read-only audit after correction: `adb -d shell am instrument -w -e hardwareAudit read-only dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation`, exit0,0.70s. It intentionally replaces the app process. Schema3,1 source,4 snapshots,2 assets/recordings/members/replicas. Latest snapshot remains f6b8ec520566d197e89068184ea8177f60f6768f0b324e697ab608955f9dece1 with2 members; both asset IDs are the protected older files. No new asset row or enumeration generation observed. Both old rows retain local references and VERIFY_EXISTING; their remote_time_present=false and SYNC_FALLBACK are not measurements of the new recording. No download, media read, deletion or protocol mutation performed by this audit.

New-recording report at this checkpoint:

- REMOTE_TIMESTAMP: NOT_TESTED — new asset not yet in persisted enumeration.
- REMOTE_TIMESTAMP_SOURCE: UNKNOWN.
- MEDIA_METADATA_TIMESTAMP: NOT_TESTED — no media payload accessed; no download allowed.
- TIMEZONE_OR_OFFSET_PRESENT: UNKNOWN for new asset; phone timezone does not establish camera metadata offset.
- FILENAME_TIMESTAMP_IF_ANY: NOT_OBSERVED for new asset; filename naming semantics remain unverified.
- RESOLVED_CAPTURE_TIMESTAMP: NOT_AVAILABLE for new asset.
- RESOLUTION_RULE_USED: NOT_AVAILABLE for new asset; do not substitute old-row fallback.
- RESOLVED_CAPTURE_DAY: NOT_AVAILABLE for new asset.
- DESTINATION_PATH: NOT_AVAILABLE for new asset; no physical destination created by this test.
- CONFIDENCE: INSUFFICIENT_EVIDENCE; approximate external reference only, no exact-minute comparison.

Next physical action: ordinary fresh rescan/connect with the new short recording present, confirm its grid visibility/count without downloading or selecting Back to Live View. Then inspect actual persisted/protocol evidence, distinguishing raw remote time, parsed media metadata, unverified filename candidate and ledger resolution. Existing audit exposes only timestamp presence/day/source, so exact-time/path diagnostics may require a narrowly sanitized read-only projection extension; do not fabricate fields it does not export. Media-embedded metadata remains NOT_TESTED if unavailable without the prohibited media read/download. Existing connection-time camera clock synchronization is a confounder and never proves intrinsic timestamp timezone semantics. G2 remains BLOCKED.
