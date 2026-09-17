# Capture-day organization — R-037

Date: 2026-09-17. Product requirement accepted from explicit user instruction; resolver/storage design proposed, implementation and hardware validation NOT_TESTED. Hardware remains paused. This supplements the original-asset policy without re-scoring GATE-0.

## Product and destination contract

Every synchronized recording asset uses `<destination-root>/YYYY-MM-DD/<persisted-leaf-name>`, automatically created or reused for the first eligible asset of the day. Phone example: `Movies/Osmosis/2026-11-15/`; the following day's recordings use `Movies/Osmosis/2026-11-16/`. Synchronizing on a later day does not change this grouping. No manual day-folder creation is required. SSD/cloud roots may differ, but reuse the ledger's logical day and asset-relative path rather than recalculate dates from upload time.

Resolve one parent recording capture day; associated RAW/DNG, separate recording audio and required metadata/sidecars inherit it even if their own creation/modification dates differ. A recording spanning midnight groups under its start capture day; proven members of one split recording follow that parent, independent recordings resolve independently. Ambiguous parent membership remains unresolved under the existing asset policy; never invent a relationship from similar filenames. A late sidecar joins the already reserved parent group, not today's folder.

Use a strict calendar date and ASCII `YYYY-MM-DD` (calendar year, not week-based year), fixed digits and no locale-dependent formatting. Capture-date grouping is organization, not asset identity. Distinct same-name originals on the same day must not overwrite each other: reserve a stable suffix derived from the internal asset-version ID when necessary, persist the chosen name, and reuse it on every destination. Never rely on a provider's auto-generated `(1)` name. Preserve original source name privately. Unsupported destination constraints surface a storage action; do not silently split companions among MIME-specific roots.

Phone storage API feasibility is an explicit GATE-1 review item: baseline uses separate Movies/Pictures/Download collections. A common recording folder for mixed media/sidecars must be proven on S25 using a supported adapter; evaluate a SAF tree if MediaStore restrictions prevent the common root. One-time root access consent may be required by Android, but manual daily-folder management is not. Do not assume arbitrary MIME files can be inserted into Movies through the video collection. No storage API, permission or source change is made now.

## Time-source priority and trust

Evaluate available sources in this order, retaining provenance and disagreement:

Current explicit user decision2026-09-17 supersedes the earlier requirement to prove filename timezone before using its local date:

1. Trustworthy explicit camera/media capture timestamp with independently established zone/offset.
2. Trustworthy explicit camera/media capture timestamp without zone/offset (local time stays local).
3. Strict supported DJI filename timestamp belonging to the enumerated remote asset: exact `DJI_<14 digits>_<4 digits>_D.<supported extension>`, valid calendar/clock. Source `DJI_FILENAME`; high local-calendar confidence, absolute instant/offset UNKNOWN. User accepts the observed Pocket4P naming evidence as sufficient for local date organization, not UTC precision.
4. Trustworthy remote-file timestamp with verified semantics; generic mtime/HTTP Last-Modified is not automatically capture time. No stronger remote-file evidence currently justifies overriding the user's priority.
5. First durable sync allocation time only as last resort, explicitly `SYNC_TIME_FALLBACK`.

The strict implementation initially supports uppercase MP4/MOV/JPG/JPEG/DNG/RAW/HEIC/WAV and `_D` names only; other names/types remain unclassified for time and are not discarded. Never infer offset from the phone zone. For the observed third file the local time is2026-09-17T18:40:37, day2026-09-17, no absolute Instant. Clock correctness/travel/DST semantics remain independently unproven.

Trust includes verified timestamp encoding, sane calendar/range, source role and known clock errors. A plausible but wrong camera clock cannot be detected reliably from syntax alone. Preserve source values and mark known/suspected clock faults; do not apply today's phone-camera offset retroactively. Differences within known source precision are recorded accordingly; material disagreement, especially a different day, raises `CAPTURE_TIME_CONFLICT`. Use the highest-ranked still-trustworthy source deterministically and record alternatives; if its trust is disproven, move to the next usable source. Equal-priority unresolved day conflicts are not arbitrarily tie-broken: retain candidates and use an explicitly uncertain provisional last-resort allocation pending reconciliation. No conflict is silently reported as verified organization.

## Deterministic timezone fallback (design policy v1)

- Capture-local wall time with trustworthy capture offset: use its local date directly; retain offset and optional IANA zone. An instant plus proven capture-zone evidence is converted using the rules at capture time, not today's offset. Never replace a recorded offset with the phone's current zone.
- Local wall time with no offset/zone: preserve the literal calendar date, record `CAMERA_LOCAL_ZONE_UNKNOWN`, and leave UTC instant/offset null. Do not assume UTC or the phone's zone. This preserves the camera-reported day, with explicit uncertainty about the true local capture day.
- Trustworthy instant with no capture-local zone: prefer trustworthy local-date evidence from another capture source if available; otherwise use UTC date as a deterministic `UTC_DAY_FALLBACK`, retaining the original instant and `intendedLocalDayVerified=false`. UTC here is an explicit fallback, not a claim the recording happened in UTC.
- No usable capture source: persist first allocation instant, the phone zone/offset and local date at that instant once as `SYNC_TIME_FALLBACK`. Subsequent travel, restart, retry, DST or clock changes cannot recalculate it.
- DST overlaps: preserve the local day; do not fabricate a unique instant without offset evidence. DST gaps/invalid local times under an asserted zone are conflicts, not silently normalized into another time. Midnight and date-line travel use the capture-context day, never the later synchronization context.

UI separates transfer integrity from organization confidence, e.g. a date could not be verified. Uncertainty does not erase a valid backup or block safe byte preservation, but prevents a claim of fully verified capture-day organization. Existing CAMERA SYNC COMPLETE retains its byte/inventory meaning and must display outstanding organization warnings; no R-037 acceptance PASS while relevant cases remain unverified. No GPS collection or location lookup is required for date resolution.

## Ledger and crash/retry rules

G2 schema4 implementation adds asset-linked `capture_evidence` separate from frozen recording/day/path reservations. Filename local timestamp, DJI_FILENAME source, null zoneEvidence, CAMERA_LOCAL_ZONE_UNKNOWN and HIGH_LOCAL_DATE_UNKNOWN_INSTANT persist without UTC conversion. The latter combines high local-date confidence and unknown absolute instant; timezone confidence is UNKNOWN/LOCAL_CAMERA_TIME. A same-day fallback reservation can gain stronger time provenance without path changes. Different-day evidence is retained with reservationDayConflict=true; old reservation/local URI is unchanged pending explicit later reconciliation. This is not permission to move/re-download existing files. New assets reserve the filename-derived day from first allocation. Historical fallback rows without re-enumeration are not silently invented as verified timestamps.

Persist `CaptureResolution` linked to RecordingGroup and referenced by each member: original candidate values/source types, trust/rejection/conflict reasons, resolved local timestamp and precision, nullable UTC instant/zone/offset, `captureDay`, fallback/confidence, `intendedLocalDayVerified`, resolver-policy version and resolution revision. A date-only source stays date-only; never invent midnight as a measured capture time.

Persist each replica's destination/root identity, logical relative directory, stable leaf name, complete relative path, allocation phase and actual URI/object ID. Reserve unique `(destination, relativePath)` and `(assetVersion, destination)` identities transactionally before IO; the coordinator performs idempotent ensure-directory and journals the returned directory identity. Parallel starts/crashes cannot create duplicate day folders or files. Root plus date is not a sufficient dedup key for assets.

Freeze resolution/path before first final destination allocation. If trustworthy metadata is only available after transfer, use ledger-owned staging outside the completed date layout, then resolve and publish once; resume keeps the same staging URI. Do not fetch an original twice merely to organize it. Required metadata discovery precedes the decision to declare a source unavailable and use fallback. Metadata extraction must be bounded, read-only and confined to required timestamp fields; no raw metadata/GPS/media logs.

Retry, reconnection, process restart and downstream replication load persisted paths; they do not invoke a fresh current-date resolver. Stronger evidence arriving after allocation is a pending resolution revision, not permission to create a second file or silently move an active partial/verified replica. Any future correction requires a separately designed crash-safe reconciliation transaction across recording members and replicas, preserving the original mapping until successful. Existing flat baseline files remain intact; migration/import must identify existing replicas without redownloading or silently moving them.

## Investigation and proof limits

Read-only source at baseline `2fcdbc97e6dbefc875d425368be67cf32b50bb06`:

- `CameraFile.kt:135,151,167`: regex extracts 14 filename digits; UI grouping prioritizes them, and mtime formatting uses the runtime default timezone. Neither validates Pocket capture semantics or establishes capture-day storage.
- `CameraFile.kt:59`: mtime field is documented for indexed/drone manifests; target Pocket availability/trust remains unknown.
- `CameraSession.kt:237,2234`: connection attempts `syncTime()`, sending phone epoch seconds, current offset and zone ID. This is a source-observed camera clock write, not proof Pocket accepts it or embeds offset/zone into old/new recordings. It is an important confounder for travel/incorrect-clock experiments; record clock state before connecting in an explicitly authorized future test window.
- `MediaDownloader.kt:256-270,334-342`: flat MIME-dependent roots and lookup, with no capture-day directory. Both allocation and existing-copy discovery require future coordinated changes; no changes made here.

A targeted official DJI search for Pocket 4 timestamps/timezones on 2026-09-17 did not establish the Pocket 4P field/offset/naming contract. The surfaced [Pocket support URL](https://www.dji.com/au/support/product/osmo-pocket-4) redirected to general support; it is not target proof. General [Android shared-media documentation](https://developer.android.com/training/data-storage/shared/media) covers collection/path handling, not Pocket timestamp correctness. No conclusion is drawn from other models or third-party reports.

Outstanding target questions: which capture/remote fields exist; whether they are UTC, local or offset-tagged; filename relationship and rollover; effect of phone/camera timezone changes and connection-time clock write; midnight/DST behavior; incorrect camera clock; conflicting source values; mixed-media shared-folder support. All are NOT_TESTED. Tests CD01-CD08 are specified in TEST_MATRIX; no ADB, camera setting change or access to existing media occurred in this requirement task.
