# Schema5 migration PASS under incomplete target enumeration

2026-09-17. User reports powered-on/awake Pocket4P, one fresh search/connect, grid with1 visible item, no selection/download/delete/modification. Subsequent user absence explicitly pauses further physical testing; sleep/disconnection during absence is not source-state evidence.

Before the pause, the existing allowlisted read-only instrumentation modes and protected metadata stat completed, combined1.10s, exit0. Commands:

- adb -d get-state -> device
- adb -d shell am instrument -w -e hardwareAudit read-only dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
- Same command with -e timestampEvidence read-only
- adb -d shell stat -c '%s' <the two previously recorded protected Movies/Osmosis paths>

Instrumentation replaces the app process at this no-transfer checkpoint; it does not passively inspect a live camera session. No further device calls after the user's absence instruction. No raw logs, DB export, credentials or media contents accessed.

## Measured state compared with pre-install checkpoint

| Field | Result |
|---|---|
| Database schema |5; migration PASS|
| Source/snapshots |1 source;8 snapshots, previously7|
| Historical assets/recordings/members/replicas |4 each, unchanged|
| Original3 candidate IDs |dd5a4029...,9e181fce...,ee17e7b7..., all retained|
| Legacy weak identity |00cbbe23... retained, UNRESOLVED_HISTORY, not an actionable candidate|
| Latest snapshot |06e704a87df9cb58afff47d699e70c2305beaecf7edf8bb59fe53aeebe3e3e0a|
| Latest status |sealed INCOMPLETE; all_coverage_proven=false;0 actionable memberships|
| Identity observations |2 UNRESOLVED; no resolved_asset link|
| Observation IDs |ae78d38b6cb4ebb7ac1a29fcd129556d63a63549bb3c15f2c45222a5784b093b;00a368b6e75c2162b608211981e53f7424d8ea5c7cd0eafdf9d72d6a6f6d4c67|
| Original38MB local association |candidate_count1 retained; selected locator absent/AMBIGUOUS/NEEDS_REVALIDATION, unchanged from before upgrade|
| Large-file local association |candidate_count1; selected locator hash baa24bf995e9f34832834ab911d7ed2528407606ce7b23769241df87cd93d659 unchanged;LOCAL_PRESENT_UNVERIFIED/VERIFY_EXISTING|
| Weak-row candidate relationship |candidate_count1 retained; selected locator absent, unchanged|
| Third candidate |DISCOVERED/ABSENT; no local candidate; not in current snapshot|
| Protected local sizes |38447651 and3071380142, unchanged|
| Third persisted time |DJI_FILENAME;2026-09-17T18:40:37;day2026-09-17;null zone/offset UNKNOWN|
| Third reserved destination |2026-09-17/DJI_20260917184037_0003_D-c27205149b442d47f537bea3.MP4, unchanged|

The projection recomputes policy for historical rows; it is not a live queue or instruction to download the third asset. All original candidates are outside the newest snapshot. No row is LOCAL_VERIFIED. The38MB pointer was already absent before installation; this check does not demonstrate new reference loss or successful reselection.

## Software-only disposition

Focused source review: MainActivity sends the same fixed manifest list to LedgerCoordinator.observe and showGrid. The UI may display a CameraFile whose positive length is unavailable. CameraLedgerAdapter maps non-positive size to null; schema5 reconciliation stores that as an unresolved observation instead of an actionable candidate. Migration backfills1 legacy weak observation; the new total2 is consistent with1 new insufficient-length observation. The sanitized projection does not expose its payload, so the exact remote cause/response is not asserted. A visible item and0 actionable members are therefore not contradictory and do not prove an empty source.

The already-passing IdentityObservationInstrumentation fixture explicitly covers this transition: schema4-to-5, four retained historical assets, partial null-size observation, zero actionable members, two unresolved observations, no false completeness. It separately proves deterministic later positive-evidence re-observation restores both known local pointers while preserving paths/history; repeat order and database restart preserve the result. No code change or redundant full rerun is justified by this matching observation. Existing353-unit/emulator/security results remain applicable.

Migration, retained history and incomplete-observation isolation on target: PASS. Source coverage: INCOMPLETE/UNTRUSTED, never PASS. Root cause of partial manifest/session remains UNCONFIRMED, separate G7/G4 work. Do not merge identities by name, treat absent members as deleted, or rewrite history to force progress.

Remaining G2 target integration assertion: actual reselection of the existing38MB local candidate when the normal adapter receives sufficient positive-length evidence for that original. The newest observation supplies no actionable candidate, so this assertion was not exercised; it remains NOT_TESTED despite the software fixture PASS. No additional identical rescan requested now. G2 remains BLOCKED for this narrow hardware assertion; migration is no longer a blocker. Broad complete-store/sidecar/immutable-source proof belongs to G4, not a new G2 requirement.

Current physical hardware pause is a Human Stop Condition. Useful software disposition is complete; G3 predecessor remains unmet. Wait for explicit return before any target interaction, and review then whether a distinct justified observation is available. No repeated connection polls/scans while absent. Release false; no media mutation/main/merge/release.
