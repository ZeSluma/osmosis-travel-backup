# Requirements

## 1. Product goal

Build a transactional, self-healing camera synchronization system for:

- DJI Osmo Pocket 4P
- Samsung Galaxy S25 Ultra
- Hagibis/P310 external NVMe SSD
- later OneDrive

Primary target:

Pocket 4P on
→ intentionally open Osmosis to start or attach to a synchronization session
→ connect
→ enumerate all original media
→ identify only new/unverified originals
→ transfer
→ verify integrity/completeness
→ persist a verified local copy
→ trigger downstream SSD/cloud backup
→ clearly report status.

Reliability and data safety outrank convenience and feature count.

Normal operation requires no manual file selection, queue maintenance, rescan, retry or reconnect. Manual recovery remains a fallback only for a demonstrated platform/camera/permission/identity/storage blocker. The user need not understand transfer internals. Activity visibility is not a prerequisite after session start. Force-stop/user cancellation semantics must remain honest; automatic execution is never promised where Android disallows it.

## 2. Functional requirements

- **R-001 Camera connection:** connect automatically to the known available Pocket 4P on intentional Open; bound recovery and request only genuinely required user actions.
- **R-002 Complete enumeration:** independently enumerate all required originals/stores/pages/recording members under ASSET_INCLUSION_POLICY; no complete claim from the visible grid alone.
- **R-003 Persistent ledger:** persist backup state across process death and device/app restart.
- **R-004 Idempotent backup:** repeated backup runs transfer only files that are new or not safely verified.
- **R-005 Integrity:** never mark a transfer VERIFIED before transfer completion and integrity criteria pass.
- **R-006 Recovery:** interrupted work must resume or fail safely without presenting partial data as complete.
- **R-007 Local-first:** a verified local backup must succeed independently of cloud availability.
- **R-008 External storage:** support downstream verified backup to the P310/external NVMe path.
- **R-009 OneDrive:** later add downstream OneDrive backup without making cloud availability a prerequisite for local success.
- **R-010 User feedback:** clearly report current progress, success, retryable failure, fatal failure and unresolved state.
- **R-011 Safe-clear signal:** provide an informational `SAFE TO CLEAR CAMERA` state only when required backup policy has been satisfied.
- **R-012 No automatic camera deletion:** never automatically delete camera originals. Future cleanup requires explicit user initiation and confirmation, safe-to-clear eligibility and exact verified snapshot scope under R-039; no delete implementation/use is authorized now.
- **R-013 Diagnostics:** capture privacy-preserving technical incident information sufficient for debugging.
- **R-014 Large media:** support large files and large batches without assuming Activity lifetime.
- **R-015 Thin fork:** reuse upstream protocol and behavior where suitable; isolate custom backup logic.

## 2a. Intent rebase requirements — 2026-09-17

- **R-016 Session ownership:** one fenced coordinator hosted independently of Activity; repeated Open/Start is idempotent. UI/notifications observe canonical state, never own the engine.
- **R-017 Lifecycle contract:** continue when backgrounded, screen off/locked or Activity recreated where the chosen Android API permits; reconcile after process recreation/upgrade. Explicit force-stop, OS stop, reboot and user cancellation semantics; no guaranteed resurrection.
- **R-018 Automatic recovery:** independently restore connection then evaluate/resume partial jobs, with bounded exponential backoff/jitter and no repeated normal-operation manual interaction.
- **R-019 Identity:** camera + storage generation + strongest proven object-version evidence, never filename/MediaStore-only deduplication. Uncertain identity requires safe revalidation, not unsafe skipping/appending.
- **R-020 Asset/recording scope:** primary full-quality video/photo, RAW, separate recording audio and non-reconstructable or processing-relevant recording sidecars are required. Preserve recording relationships; missing required member blocks recording completion. Verified proxies/thumbnails/temp/cache/regenerable helpers excluded by default; unknown types receive explicit class/disposition under ASSET_INCLUSION_POLICY v2, never silently discarded.
- **R-021 Snapshot completeness:** persist enumeration generation, all page/store/group endings and final stability validation. Partial enumeration and missing/unverified required members prevent completion. Potentially required unknowns require classification or safe preservation; evidence-backed unrelated unknown artifacts do not automatically fail the whole sync. Separate enumeration, recording, local, redundancy and cleanup predicates.
- **R-022 Safe range:** identity/size/partial validation, correct 206/Content-Range for append, no append of HTTP 200, safe non-overwriting restart only when needed. Preserve demonstrated lower-level resume through adapters/tests.
- **R-023 Explicit integrity:** LOCAL_VERIFIED only under recorded identity/size/range/readability/publication criteria; trustworthy source checksum when available. Local hash alone does not prove source equality.
- **R-024 Crash consistency:** persist intent before side effects; reconcile DB with pending/actual bytes/current source at all crash boundaries, without requiring lifecycle callbacks.
- **R-025 Storage safety:** estimate space/reserve margin, handle ENOSPC/provider failures, quarantine orphan/ambiguous partials, never overwrite verified replicas with incomplete ones.
- **R-026 Independent replicas:** phone is first durable required landing zone; separate phone/SSD/cloud states and verification; cloud outage or SSD removal cannot invalidate a good local copy.
- **R-027 Route ownership:** camera-specific Network adapters for all TCP/UDP/HTTP/preview paths, no accidental cloud routing into camera AP; sequential replication default pending coexistence evidence.
- **R-028 Failure taxonomy:** preserve all reason classes in STATE_MACHINES; generic failure cannot assert stale password, sleep or background causality. Foreground loss and state-desync candidate remain distinct.
- **R-029 Honest user actions:** concrete permission/pairing/network-approval/storage/identity/camera-power actions; no endless retry for permanent blockers. Multi-camera ambiguity requires explicit choice.
- **R-030 Background UX:** durable progress/current item/count confidence, reconnection/wait/retry/actions, safe Pause/Cancel, completion from ledger only; respect notification and lockscreen privacy.
- **R-031 Completion vocabulary:** CAMERA SYNC COMPLETE is verified phone snapshot; BACKUP REDUNDANCY COMPLETE covers configured required replica set; SAFE TO CLEAR CAMERA additionally requires current independent redundancy, complete scoped source/identity revalidation and no ambiguity; it never triggers deletion. CAMERA CLEANUP COMPLETE requires a separate confirmed operation and successful post-delete verification.
- **R-032 Target compatibility:** validate target36 Android16 quotas, lifecycle, local-only routing and permission simulation; plan target37 migration without adding premature permission declarations.
- **R-033 Resource discipline:** stream with bounded memory, initially one transfer/one datalink, structured cancellation; measure throughput/CPU/memory/battery/thermal. Any wake lock must be justified, bounded and released.
- **R-034 Structured diagnostics:** safe reason-coded events and export; no raw secrets/PII/media/GPS. Preserve private identity data only where required by ledger, not in logs.
- **R-035 Development reproducibility:** canonical golden-fixture line-ending policy proposal, Windows/Linux and CI validation; preserve original baseline failures, no fixes in this task.
- **R-036 Camera availability:** independently characterize idle/AP timeout/display sleep/standby/power-off/wake/BLE and active-transfer behavior; do not hide reliability problems by altering power settings.

- **R-037 Automatic capture-day organization:** automatically store every synchronized recording asset under its capture-day `YYYY-MM-DD` directory, e.g. `Movies/Osmosis/2026-11-15/`. Create/reuse the folder with the first eligible asset, without manual folder creation. Delayed synchronization uses capture day, not download day. DNG, recording audio and required metadata/sidecars inherit their parent recording's day grouping. Persist the resolved capture timestamp, provenance/uncertainty, day and destination-relative path before destination allocation; retry/resume/restart and repeated sync reuse that destination without duplicate folders/files. Use the same logical layout for phone, later SSD and cloud with independent roots. Time source order: trustworthy camera/media capture timestamp, trustworthy camera-supplied remote-file timestamp, verified filename timestamp, then first synchronization time only as an explicitly uncertain last resort. Preserve intended local capture day; never silently reinterpret timestamps using the phone's current travel timezone. Pocket naming/metadata/timezone semantics require proof. Deterministic fallback, conflicts and acceptance tests are specified in [CAPTURE_DAY_ORGANIZATION](design/CAPTURE_DAY_ORGANIZATION.md).

**R-038 Redundancy and unknown policy:** default safety requires at least two independently verified copies outside the camera on distinct storage domains: phone plus SSD or cloud, including all additionally configured required destinations. Adopt KNOWN_REQUIRED, KNOWN_OPTIONAL, KNOWN_REGENERABLE_EXCLUDED, UNKNOWN_POTENTIALLY_REQUIRED, UNKNOWN_NON_RECORDING and UNSUPPORTED; evaluate exact scoped predicates in ASSET_INCLUSION_POLICY v2. Phone-only never enables safe-to-clear.

**R-039 Explicit snapshot cleanup:** future user-initiated, explicitly confirmed DELETE_VERIFIED_SOURCE_SNAPSHOT only; revalidate source/camera/storage and replicas immediately before execution, retain new/unverified media, journal per-asset results, never blindly retry uncertain handles, verify all intended absent and retained assets present before CAMERA CLEANUP COMPLETE. No format, automatic deletion or early-gate destructive implementation. See VERIFIED_SNAPSHOT_CLEANUP and proposed GATE-9.

**R-040 Optional GPS recording sync:** explicit opt-in independent telemetry feature; backup, verification, reconnect/resume and safe-to-clear fully work with GPS OFF. Persist preference separately from permission/running state, review automatic resumption eligibility; never collect location because backup starts. Embedded telemetry stays in originals, non-regenerable telemetry sidecars are required recording members.

**R-041 Independent diagnostics:** bounded lightweight sanitized events available in normal runtime, separate from OFF-by-default temporary verbose Save logs. No backup/reconnect dependence on verbose logging; app-private bounded storage, explicit sanitized export, no automatic uploads, GPS/secret/media/PII exclusion. All sinks including logcat follow policy; see GPS_AND_DIAGNOSTICS.

Acceptance mapping and evidence status: [TEST_MATRIX](design/TEST_MATRIX.md). These requirements describe the intended system; baseline PASS is not evidence they are implemented.

## 3. Non-functional requirements

- Target-device reliability must be evidenced on Samsung Galaxy S25 Ultra + DJI Osmo Pocket 4P.
- Data-loss risk takes precedence over speed or UI convenience.
- App state must remain consistent after process death.
- New dependencies must be justified, verified and security-reviewed.
- Secrets and private media metadata must not leak into logs.
- Behavior must be reproducible and evidence-backed.
- Changes must remain reviewable and minimize divergence from upstream.

## 4. Invariants

See `AGENTS.md`. INV-001 through INV-008 are non-negotiable unless the user explicitly changes project policy in a versioned decision.

## 5. Scope sequencing

Accepted dependency order: **0 -> 1 -> 2 -> 3 -> 7 -> 4 -> (5,6) -> 8**, per [ADR0005](decisions/0005-gate-safety-and-optional-features.md) and [accepted dependency graph](plans/REVISED_GATE_DEPENDENCIES.md). Lifecycle-safe execution, automatic reconnect and recoverable synchronization must be demonstrated before one-tap acceptance. GATE-9 cleanup is a separate later proposal, not part of early sync gates. The original numbered scope list below is retained as historical identity context, not execution order.

1. GATE 0 — unchanged baseline
2. GATE 1 — security/architecture baseline
3. GATE 2 — persistent ledger + download-all-new
4. GATE 3 — integrity/failure handling
5. GATE 4 — one-tap backup
6. GATE 5 — P310
7. GATE 6 — OneDrive
8. GATE 7 — robust background/foreground behavior
9. GATE 8 — optional zero-touch detection
