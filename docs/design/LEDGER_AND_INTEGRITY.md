# Ledger, identity, replica and integrity design

Status: proposed schema/algorithms, not implemented. R-003 through R-006 and R-019 through R-026. No new dependency. Canonical truth is a transactional ledger reconciled against bytes and source evidence; UI/MediaStore alone cannot establish synchronization.

## Persistence choice and ownership

Select Room over hand-written SQLite for typed access, schema export and migration testing; raw SQLite offers no demonstrated benefit here. [Room documentation](https://developer.android.com/training/data-storage/room) describes checked queries and structured persistence. This is a dependency proposal, not permission to add it; choose a compatible maintained version, verify provenance/license/advisories and compiler/toolchain interaction under SECURITY before implementation. Never copy a website's sample version unreviewed.

One SyncCoordinator serializes decisions; IO children report events. A database transaction acquires a unique active-session lease for camera + policy, increments ownerEpoch and fences every later update. An in-process mutex is insufficient across restart. A startup transaction supersedes the dead process's lease, then reconciles; late completion from an old owner is rejected. Only one writer/stream may own an asset replica. Immutable events and monotonically increasing transitionVersion support UI/notification observers.

## Logical tables

| Entity | Key / essential fields and constraints |
|---|---|
| Camera | internal camera UUID, protected association/protocol identifiers, identity evidence/confidence, credential reference (never plaintext in diagnostics) |
| StorageEpoch | UUID, camera FK, logical store, observed mount mapping, stable volume ID if actually available, generation confidence, last inventory fingerprint; mount 0/1 is not a permanent card identity |
| SyncSession | UUID, camera FK, sourceGeneration, policyVersion, user intent RUN/PAUSE/CANCEL, state, ownerEpoch, retry budget, timestamps; unique active-session key |
| EnumerationGeneration | UUID, storage scope, cursors/pages, counts, end evidence, exclusion/unknown counts, pre/post inventories, stability status; partial scans never SEALED |
| RemoteAssetVersion | internal UUID, camera/storageEpoch FK, raw private locator, observed handle/type, size, timestamp with provenance, candidate fingerprint, confidence, immutable source-version evidence; unknown size is null, not zero |
| AssetMembership | generation + assetVersion unique, classification, required/optional/excluded with policy reason, companion/group relationship |
| RecordingGroup / RecordingMember | recording UUID + source generation, membership-discovery status; each member has role primary/RAW/audio/metadata/group-frame, required flag, provenance and assetVersion FK; required companions cannot be silently detached to obtain completion |
| TransferAttempt | UUID, assetVersion + phoneReplica, expected identity/version, offset/checkpoint, HTTP validation summary, state/reason, retryAt/count, ownerEpoch, attempt count |
| Replica | UUID, assetVersion FK, destination FK, URI/object locator, state, committedLength, expectedLength, local fingerprint, verification method/version, pending/publication phase, last revalidated; unique logical replica key |
| Destination | PHONE / SSD / CLOUD, protected locator and permission/token references, availability and configured requirement; camera source is not a verified destination replica |
| Event | monotonic sequence, safe IDs, UTC + monotonic elapsed, state transition, reason, numeric counters; bounded retention, no raw URLs/names/frames |

Replica states are independent: ABSENT, WRITING, PARTIAL, VERIFYING, VERIFIED, UNAVAILABLE, INVALID, USER_ACTION_REQUIRED. Transfer states: DISCOVERED, QUEUED, TRANSFERRING, PARTIAL, VERIFYING, LOCAL_VERIFIED, RETRY_PENDING, FAILED, USER_ACTION_REQUIRED. A verified phone copy is not demoted solely because SSD is removed/cloud offline; its availability may change independently of its historical verification. Source deletion never cascades into replica deletion.

Recording completeness is a derived transactional query over resolved RecordingMember requirements: every required primary/RAW/audio/non-regenerable or processing-relevant metadata member must be verified at the destination. An absent required sidecar or unknown member role blocks recording completeness even if the primary is verified. Policy versions prevent an older primary-only success being reinterpreted as complete under the expanded user-confirmed asset scope.

## Capture-time and destination persistence extension — R-037

[Capture-day design](CAPTURE_DAY_ORGANIZATION.md) adds a versioned CaptureResolution for each recording: candidate timestamps and provenance, resolved local timestamp/precision, nullable instant/zone/offset, captureDay, confidence/fallback/conflicts and policy revision. Members inherit the parent resolution. Replica records additionally persist destination root, relative directory, stable leaf name, full relative path and allocation phase; directory identity is journaled. Unique path and logical-replica reservations prevent duplicates. Freeze allocation before IO; resume/retry/restart and SSD/cloud use the same logical mapping. Capture grouping is not remote identity, and unknown time is not fabricated precision. Organization confidence remains separate from byte verification. Historical flat files require safe import, not unnecessary retransfer.

## Remote identity evidence and limits

Observed baseline CameraFile exposes path, HTTP storage mapping, handle, size, type, duration/resolution, filename-derived timestamp; fileIndex/mtime support some other models. No trustworthy Pocket checksum or immutable volume/source generation ID was established. handleShared and absent handles in upstream model demonstrate that handle alone is unsafe; filename-derived timestamp is not independent evidence from filename.

Candidate match key: associated camera UUID + storageEpoch + canonical path + known exact size + available source timestamp/type + confirmed stable handle if validated. Store component provenance, not only a hash. Do not equate this tuple with proven global uniqueness: same-name/same-size replacement can collide. Session enumeration generation does not prove physical storage identity.

Before cross-session dedup/resume, revalidate available components and storage mapping. If removable-storage continuity or object version cannot be established, invalidate the candidate match for automatic reuse and request safe revalidation; never skip on filename alone. Full re-fetch to a separate staging replica may be a later authorized revalidation strategy; do not overwrite a previously verified copy. Verified identified assets are not unnecessarily retransferred; uncertainty is surfaced, not silently treated as identity. No current file re-download is authorized by this design.

Evidence matrix required: unchanged repeated enumeration/reconnect; camera restart; card reinsertion; intentional source replacement/name reuse; rollover; format/volume change. Destructive cases use fakes or a separate explicitly authorized disposable card, never current originals. Until hardware is resumed and these checks exist, stability across those boundaries is NOT_VERIFIED. Store invalidation epochs and unsupported checks explicitly.

## Range and completion protocol

1. In a transaction, claim the replica, record expected source identity/size and attempt intent before IO. Create an app-owned pending/staging destination with a unique non-overwriting identity, then journal its URI. A crash in this small cross-system gap is handled by orphan reconciliation.
2. Revalidate source version and exact size; open partial and get actual length. Compare actual length with durable checkpoint. A checkpoint cannot justify bytes absent from disk; uncommitted trailing bytes are untrusted until revalidated (or truncate only that owned partial to a known safe boundary, never a verified file).
3. For offset >0 request Range bytes=offset-. Require 206 with syntactically valid Content-Range whose start equals requested offset, total equals the expected size, and body length/end are consistent. Reject overlaps, gaps, overflow, changed totals and invalid encodings; use 64-bit arithmetic. Never append HTTP 200. A 416 is not completion proof; independently validate identity/length. Restart into safe staging only if identity/resume cannot be trusted and policy permits it, preserving any good replica.
4. Stream bounded buffers (initial one transfer). Persist progress after flush/checkpoint at measured intervals, not per UI tick. Cancel closes IO and records partial intent; crash recovery never depends on onDestroy/onStop callbacks.
5. EOF alone is insufficient. Require validated source version, exact expected local byte count, correct HTTP/range termination, successful flush/close and readable local URI. A short/zero response for a nonempty source stays PARTIAL/FAILED. Unknown expected size prevents LOCAL_VERIFIED until independently resolved. An actually confirmed zero-byte eligible asset is distinct from missing size.
6. In transaction record verification evidence and PUBLISH_PENDING while media remains pending. Finalize MediaStore then confirm readable URI, size and is_pending=0; finally commit LOCAL_VERIFIED/Replica VERIFIED. DB and MediaStore are not a shared transaction: on crash between these operations, recover idempotently and never infer verified merely from publication. If a crash leaves published bytes before the last DB commit, revalidate the recorded intent/evidence before ledger promotion.

LOCAL_VERIFIED means these explicit checks at a recorded assurance level, not an unqualified remote-content checksum guarantee. If a trustworthy remote checksum becomes available, compare it. A local SHA-256 fingerprint can support later replica equality and corruption checks but alone cannot prove camera-source equality; design may compute it after authorization, not during the paused baseline. Store `verificationMethod=identity_size_range_readable` or stronger method/version and its limits. Only the verifier can propose promotion to the coordinator.

## Reconciliation and storage safety

At every authorized restart/reacquisition: fence old work, load session intent, reopen private URI metadata, classify missing/partial/published destinations, revalidate camera identity when available, and plan incomplete work. Missing file invalidates current availability; MediaStore record alone never synthesizes bytes. Orphans are inventoried privately and quarantined from completion; no blanket deletion. Existing baseline files are LEGACY_UNVERIFIED candidates requiring evidence-based import, not adopted merely by name/size.

Preflight required bytes per snapshot, actual usable space and configurable safety margin (initial proposal max(1 GiB, 5% of available volume capacity), validate on device; unknown estimate uses incremental reservations). Disk-free checks are advisory; enforce ENOSPC/write/close errors during transfer. Save truthful partial state even if verification/hash or publication fails. Never reuse an unrelated duplicate-name URI. Local space pressure cannot trigger original/verified-copy deletion. SSD uses persisted SAF grants, availability checks and provider-specific publication/verification; loss of grant prompts user action. Cloud uses separate credential and upload-session records, never camera network ownership.

[Room migration guidance](https://developer.android.com/training/data-storage/room/migrating-db-versions) supports migration testing; export schemas, test every supported upgrade and crash boundary. No destructive-migration fallback or downgrade wipe. An unsupported migration blocks sync while retaining data; schema/policy versions gate interpretation. Checkpoint verification before migration and recovery tests after upgrade are required.
