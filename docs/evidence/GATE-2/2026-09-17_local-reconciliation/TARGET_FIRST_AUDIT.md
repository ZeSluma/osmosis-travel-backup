# First target reconciliation audit

2026-09-17, verified debug artifact D42573232A017F649388327E57171FE59E9659759EE41409582D7BD09D2DB496 on S25 Ultra. User confirms successful connection and grid count2, no download. Audit intentionally replaces the target process; no spontaneous-disconnect inference.

Command: `adb -d shell am instrument -w -e hardwareAudit read-only dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation`. Sanitized output reports database exists, schema3, sources1, snapshots2, assets2, recordings2, members2, replicas2. No error reported. Combined audit/metadata-stat command exit0, duration0.91s.

| Expected bytes | Asset hash | Recording hash | Reserved destination hash | Local locator hash |
|---|---|---|---|---|
|38447651|dd5a40293644c9da5284652ea446d1bcff341260cdd1a7489f5ed5fcac15ce74|9124f6c9f483883c332d2299f133bbb853cd877d27c47fb496a1253860b25b3d|ede3e74dbd1b27e224c7d8c43e7d3d9308528d25e3c3258189bbb74b02adec55|657110c510a53bdca86dd00b1706b0c9bd2144ebac718281cbd56c50e44f36b8|
|3071380142|9e181fcedb7dc2619aaf2aa1d3ae5547764c4e82290692216233c03d2f4575e9|74602f8dc36bcb197c63952bb94fe36574b776971349d577ff239eab1bebbb4e|571702d95c97f3462db30d837e11ab5d60484eb2bd2e83277acaf982abd919a7|baa24bf995e9f34832834ab911d7ed2528407606ce7b23769241df87cd93d659|

Both rows: classification KNOWN_REQUIRED, identity_ambiguous=true, state LOCAL_PRESENT_UNVERIFIED, capture_day2026-09-17, time_source SYNC_FALLBACK, fallback SYNC_TIME_FALLBACK, relationship_uncertain=true, remote_time_present=false, strong_version_present=false, in_latest_source_snapshot=true, local_locator_present=true, committed_bytes0, local_presence PRESENT_UNVERIFIED, candidate_count1, recomputed_planner_action VERIFY_EXISTING. Timestamp is explicitly fallback, not verified capture date. No LOCAL_VERIFIED or source-equivalence claim.

Original snapshot hash9f53fc67e5f2c7897ee60667ef07da44345fbf1937dd0ecf3d9a046a875fadd4 plus distinct new snapshot28267b1a7863dc5258f3baedf1ba9444bf03e703f64d19401d6f733f32f2443f; each INCOMPLETE, all_coverage_proven=false, sealed=true, members2. New persisted enumeration generation is proven; completeness of all camera originals is not. Asset/recording/destination hashes match pre-update observations, no duplicate asset rows.

Planner evidence is PRODUCTION_POLICY_RECOMPUTED_FROM_PERSISTED_ROWS_NOT_LIVE_QUEUE. Both existing local candidates require verification rather than blind download; no actual live queue inspection or byte integrity verification is claimed.

`adb -d shell 'stat -c %s /sdcard/Movies/Osmosis/DJI_20260916143329_0001_D.MP4 /sdcard/Movies/Osmosis/DJI_20260917113913_0002_D.MP4'` returned38447651 and3071380142. Protected files remain present with unchanged sizes. No payload read/hash, download, deletion or media modification.

Target schema upgrade, candidate association and first distinct generation PASS. Next: ordinary app restart/reconnection and new snapshot comparison of local locator hashes, assets/groups/path, counts, states/plans and protected sizes. Overall G2 remains BLOCKED pending required remaining hardware evidence. Repeated saved-entry reconnect failure/rescan recovery is separate future-G7 debt with unconfirmed cause.
