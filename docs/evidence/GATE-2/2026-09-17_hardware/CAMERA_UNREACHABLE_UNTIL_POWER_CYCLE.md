# CAMERA_UNREACHABLE_UNTIL_POWER_CYCLE

2026-09-17, S25 Ultra / Pocket4P, user-observed following the separately recorded CONNECTED_BUT_MEDIA_ENUMERATION_EMPTY event. Green status light was off; fresh Osmosis Rescan could not establish a usable connection. User could not recover through Rescan alone, then fully powered camera off and on. Fresh Rescan subsequently succeeded; exactly2 videos visible. Camera displayed "Playback running via DJI Mimo" and offered "Back to Live View". No download, deletion or overwrite reported. No tool pressed Back to Live View or commanded the camera power cycle.

ROOT_CAUSE: UNCONFIRMED. Full power-off, deep standby/sleep, AP/BLE shutdown or another camera-side state are unresolved alternatives. LED/display observations do not distinguish them. Do not classify as pure Wi-Fi reconnect failure, confirmed sleep, Android background failure or proof that the DJI Mimo app owns the session. No controlled timing, BLE advertisement/wake-command or firmware-state trace was captured.

## Recovery and G2 persistence evidence

After explicit user confirmation of the restored two-video grid and no transfer, ran `adb -d shell am instrument -w -e hardwareAudit read-only dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation`. It intentionally replaces the app process and ends its current session; this is not another spontaneous failure. Sanitized output: [recovery audit](../2026-09-17_local-reconciliation/power-cycle-recovery-readonly-audit.txt). Combined audit and metadata-only stat exit0,0.73s. No media content opened/read/hashed/copied.

- Schema3;1 source,4 snapshots,2 assets,2 recording groups,2 members,2 replicas. No duplicates.
- New snapshot f6b8ec520566d197e89068184ea8177f60f6768f0b324e697ab608955f9dece1 has2 members, INCOMPLETE/all_coverage_proven=false. The older zero-member snapshot remains INCOMPLETE and historical; all previous snapshots retained.
- Same two asset IDs now in the latest snapshot. Asset/group/destination/local-locator hashes, candidate counts1 each, capture-day reservations and state/policy match the prior audit. Both remain LOCAL_PRESENT_UNVERIFIED, PRESENT_UNVERIFIED, committed_bytes0, VERIFY_EXISTING; neither is independently integrity-verified.
- Metadata-only stat of protected TEST-D/TEST-E paths returned38447651 and3071380142 bytes, unchanged.
- [Machine-readable comparison](../2026-09-17_local-reconciliation/power-cycle-recovery-comparison.json) records exact equality checks between empty and recovered-generation rows. Prior TARGET_FIRST_AUDIT.md documents the same identities before the empty generation.

G2 repeated enumeration/idempotence and local-reference/state/path persistence across prior controlled process restart, empty generation and camera power-cycle recovery: PASS for these2 observed assets. This does not prove reliable reconnect or complete all-original coverage. Per-row policy is recomputed from persisted state, not a captured live queue. Candidate/source byte equivalence remains unverified. SYNC_FALLBACK remains explicit, not trusted camera capture time.

The return of the same two ledger identities and user-visible videos supports the inference that the earlier zero-media result was not evidence of an actually empty source. Preserve CONNECTED_BUT_MEDIA_ENUMERATION_EMPTY as a separate finding. Expected observed source inventory for this checkpoint is2 videos; unknown types/companions/all-store coverage remain unproven.

## G7 investigation requirements — not implemented

1. Measure whether this exact Pocket4P state can be remotely woken via BLE; distinguish unavailable advertisements from a powered-off or sleeping camera rather than assuming equivalence.
2. Inspect the wake command Osmosis actually sends, its model-specific correctness, acknowledgement, timing and subsequent AP/media readiness; current correctness/effectiveness is NOT_TESTED for this occurrence.
3. Determine what observable evidence distinguishes deep standby from full power-off. If the protocol cannot distinguish them reliably, preserve UNKNOWN.
4. Establish when automatic recovery is possible and when `USER_ACTION_REQUIRED: POWER_ON_CAMERA` is justified. Do not promise remote wake from actual full power-off without evidence or mislabel a transient session error as power-off.
5. Keep playback/session readiness and trustworthy media enumeration separate from network connection. Do not press Back to Live View merely to recover a test unless the test explicitly requires that state change.

G2 remains BLOCKED for remaining new-recording and capture-time/type/relationship hardware disposition; existing-media persistence subset now PASS. No code/dependency/Gradle change, release or main mutation.
