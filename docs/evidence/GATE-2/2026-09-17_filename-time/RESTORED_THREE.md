# Restored three-member snapshot: timestamp PASS, identity conflict unresolved

2026-09-17. User followed awake-camera, one fresh connect, no settings/power-cycle/media changes and observed3 media. Initial ADB audit was blocked by unauthorized device; after user reconfirmation, `adb -d get-state` returned device. Both read-only instrumentation modes and protected stat succeeded (combined1.11s, exit0). Instrumentation intentionally ends the session; no media payloads or raw logs read. Exact results: restored-three-audit.txt and restored-three-times.txt.

| Check | Result |
|---|---|
| Schema |4|
| Latest snapshot |fc1c488f4b5338481c1b4d4dd15eb5d2b0405ea10cdf872d2b873ee79a19f12a;3 members|
| Current members |Same original3 asset identities, not the size-null variant|
| History |7 snapshots;4 asset/recording/replica rows retained, no further multiplication|
| Third stored source/time |DJI_FILENAME;2026-09-17T18:40:37|
| Third day/zone/confidence |2026-09-17;null zone, offset UNKNOWN;HIGH_LOCAL_DATE_UNKNOWN_INSTANT|
| Third reserved path |2026-09-17/DJI_20260917184037_0003_D-c27205149b442d47f537bea3.MP4; unchanged|
| Third state/plan |DISCOVERED/ABSENT;DOWNLOAD policy only, not executed|
| Large-file state/time |LOCAL_PRESENT_UNVERIFIED/VERIFY_EXISTING;DJI_FILENAME local2026-09-17T11:39:13, null zone; local reference unchanged|
|38MB original |NEEDS_REVALIDATION/AMBIGUOUS; selected replica localLocator now null, one candidate relationship retained; historical reserved path unchanged|
|Size-null variant |Historical, not current-snapshot member;NEEDS_REVALIDATION/AMBIGUOUS; not merged/deleted|
|Protected file sizes |38447651 and3071380142, unchanged|

**Third-asset persisted filename-time hardware check PASS. Conflict resolution NOT achieved. Snapshot remains INCOMPLETE, all_coverage_proven=false.** The three-member result matches expected visible inventory but does not prove all storage/media/required companions/stable generation or source equivalence. Do not mark COMPLETE/trustworthy full snapshot from count agreement.

Focused source review confirms deliberate conservative behavior: reconcileLocal processes current snapshot assets; candidate rows from older identities remain. Cross-claims include unresolved CONFLICT rows and block selecting a local replica, then plan returns REVALIDATE_IDENTITY. Consequently the original38MB replica pointer is cleared as an arbitrary selection while its candidate relationship is retained. This is not local-file loss, a resolved duplicate, or a safe authorization to remove the historical variant. Unknown-size identity fingerprinting remains a G2 modeling issue; latest absence of the variant does not prove it is the same original because current snapshot is not complete. No unsupported filename-only merge or source-deletion inference made.

The physical rescan request has now supplied all obtainable evidence for this checkpoint. Another identical rescan cannot establish byte/source identity by itself. No destructive repair or data rewrite performed; no automatic re-download. Identity reconciliation requires a reviewed non-destructive weak-observation/quarantine design and regression evidence, or stronger source-version evidence; do not silently weaken INV-002/004 to close G2. Keep historical incomplete/partial findings separate from capture-time PASS. Gate BLOCKED, release false. No gate/UTC/integrity PASS fabricated.
