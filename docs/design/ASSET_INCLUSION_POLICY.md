# ASSET_INCLUSION_POLICY — version 2

Version 2 reconciles the explicit six-class and two-independent-copy decisions in ADR0005. Inclusion policy explicitly confirmed by the user on 2026-09-17: all non-reproducible recording-related source data is required. Primary originals, separate recording audio and processing-relevant/non-reconstructable metadata are included. This inventory distinguishes observed Pocket evidence from generic upstream capability. Actual HIL in GATE-0 transferred two MP4 originals only; no exhaustive Pocket asset census exists. Policy acceptance does not prove target enumeration support.

| Asset class | Current evidence | Confirmed inclusion policy |
|---|---|---|
| Primary full-resolution MP4/MOV videos | Pocket MP4 transfer measured; CameraFile supports MOV too | Required, every finalized original; MOV presence on target to validate |
| Primary photos/JPEG, DNG/RAW pairs | Generic model/sidecar code supports images and JPEG-to-DNG probing; target pairs NOT_TESTED | Every primary image and each available RAW partner required; cannot replace RAW with JPEG |
| Burst/interval and panorama members | CameraFile notes lead-only manifests, group expansion; evidence across other cameras is not Pocket-4P proof | All source originals/member assets required; rendered panorama not assumed to substitute for members |
| Independent recording audio / WAV companion | CameraFile.sidecarCandidate and MediaPreviewActivity probes, not an exhaustive listing guarantee | Required when associated with recording, explicitly user-confirmed |
| Recording metadata/sidecars | No complete target inventory/schema verified | Required if not reliably reconstructable from primary or potentially relevant to later processing; potentially required unknowns require disposition or verified precautionary preservation |
| LRF/LRV proxies, low-resolution preview video | Model exposes proxyPath; isVideo also includes proxy extensions | Exclude from required originals only after positive type/role classification; optional replica policy separate |
| THM/SCR thumbnails and caches | thumbPath and protocol support | Exclude from required originals once positively classified |
| Temporary/cache/regenerable helper files | Actual target roles require verification | Exclude by default only after verified regenerable/temporary role; never classify by unfamiliar extension alone |
| OSV/INSV/XRF or any unknown companion | Generic extensions/type enum do not establish Pocket support/role | UNKNOWN_POTENTIALLY_REQUIRED unless non-recording role is evidenced; visible disposition, never classification merely from isVideo |

A selected preview/trim is not the original asset. Do not substitute stream remuxes or frame captures for originals. Validate supported naming/type relationships without relying on extension alone; preserve relationships privately, diagnostics use opaque IDs. Absence of sidecar on transient 404 is not definitive; retry under bounded busy policy and record discovery completeness.

## Classification and recording semantics

| Class | Evidence / completion effect |
|---|---|
| KNOWN_REQUIRED | Proven primary original/RAW/source audio/non-regenerable or processing-relevant metadata; required member missing or unverified blocks its recording and local snapshot completion |
| KNOWN_OPTIONAL | Known nonessential derivative/accessory; retain inventory/reason, optional transfer unless policy explicitly makes it required |
| KNOWN_REGENERABLE_EXCLUDED | Positively classified thumbnail/proxy/preview/cache; excluded by default, not inferred from unfamiliar extension |
| UNKNOWN_POTENTIALLY_REQUIRED | Unknown recording-related or insufficiently understood role; retain discovery/identity, associate only with evidence, obtain disposition or verified precautionary backup; never discard |
| UNKNOWN_NON_RECORDING | Purpose still unknown but positive evidence establishes no recording dependency and independent deletion effect; retain inventory/evidence and do not auto-delete; does not automatically block otherwise complete recording/local sync |
| UNSUPPORTED | Adapter cannot enumerate/transfer/verify/classify sufficiently; also persist requiredness as REQUIRED/POTENTIAL/NON_RECORDING. Required/potential unsupported data blocks affected completeness/safety; unrelated unsupported data stays visible without blanket whole-sync failure |

Unknown extension alone cannot establish non-recording or regenerable status. Classification/disposition stores evidence, policy version and reason, not an undocumented ignore flag. Reclassify newly proven recording-related data and invalidate affected derived completion. Unsupported enumeration of a relevant store is always incomplete inventory, irrespective of individual-file class.

Every recording has primary/RAW/audio/metadata/member relationships. All KNOWN_REQUIRED members must be LOCAL_VERIFIED. Potentially required unknowns join a precautionary required set until classified: known full bytes and exact identity may be safely preserved even without semantic decoding, but membership discovery must be complete. Record disposition `PRESERVED_OPAQUE` plus proof references and unresolved semantic type; a copied preview, unknown-length transfer or uncertain identity is not safe preservation. This resolves the backup obligation, not arbitrary delete effects. Unresolved association/discovery or a required missing asset still blocks the recording. KNOWN_OPTIONAL and evidenced unrelated unknowns do not fail the whole snapshot solely by existing.

## Enumeration generation and truthful completion

R-037 [capture-day organization](CAPTURE_DAY_ORGANIZATION.md) groups every required member under its parent recording's resolved capture day, including DNG, separate audio and metadata whose own file dates differ. Unknown membership/time provenance must remain explicit. Destination roots may differ across phone/SSD/cloud, but the persisted logical grouping is reused.

SyncCoordinator enumerates independent of grid scrolling. Source currently lazily pages through CameraSession.fetchNextPage and uses per-store mapping; new planner must walk all present/eligible stores and pages, expand groups and resolve required companion assets. No single newest-page response, empty exception fallback or stable UI count means complete.

Create OPEN generation with store-presence evidence, cursor chain, raw record counts, unique asset versions and policy version. Validate page endings, repeated/nonadvancing cursors, truncation, overlap and expected counts. A missing store is not empty: establish absent card versus unmounted/unresponsive store. Failed page leaves enumeration INCOMPLETE. Unknown classification is recorded separately from whether all inventory pages were obtained; unresolved required/potential preservation leaves recording/local/safety predicates incomplete as applicable. Treat protocol paging heuristics as candidates needing HIL validation, not immutable truth.

Proposed closure rule without an atomic camera snapshot: after transferring current eligible assets, perform a second complete enumeration and require matching normalized asset/version sets across every store plus completed group/companion discovery, no errors and no known active recording/mode change. Changed/new objects extend a new generation and schedule differences. Two matching scans provide an explicitly bounded stability observation, not proof the camera never changed between reads. Keep per-pass timestamps and source-confidence limits. If target protocol cannot support trustworthy page termination/stability, do not claim global completion; expose incomplete enumeration and require action/review.

Growing/active-recording assets are deferred until finalized identity/size is stable. Continue safe finalized work, but final current-camera completion is withheld while recording/unknown activity persists. Disappearance during transfer marks SOURCE_CHANGED/MISSING and preserves local data; never mirror camera deletion onto phone. Re-enumerate after reconnect before reuse when source continuity is uncertain.

## Distinct completion predicates

| Predicate | Exact scope and necessary evidence |
|---|---|
| Source enumeration complete | All relevant stores/pages/endings and stable inventory obtained; absent store distinguished from unavailable; unknown classes may be listed without making enumeration fail |
| Recording complete | Membership discovery resolved; every KNOWN_REQUIRED member and unresolved potential member accepted as PRESERVED_OPAQUE is verified at the evaluated destination; no unaccounted required member |
| CAMERA SYNC COMPLETE(g) | Stable complete source snapshot g, all required recordings/assets LOCAL_VERIFIED on phone including precautionary required set. Unrelated evidenced UNKNOWN_NON_RECORDING does not alone block. Retained/excluded counts and time-organization uncertainty stay visible |
| BACKUP REDUNDANCY COMPLETE(g, policy) | Required assets/recordings have independently verified replicas satisfying configured storage-domain policy; default phone plus SSD OR cloud, plus any explicitly mandatory extra destination |
| SAFE TO CLEAR CAMERA(g) | Local and redundancy criteria plus fresh source/camera/storage revalidation, required/unknown safety disposition and exact unambiguous effects; revocable, scoped informational status only, never triggers deletion |
| CAMERA CLEANUP COMPLETE(operation) | Separate explicitly confirmed deletion of exact eligible snapshot executed and exhaustive post-enumeration proves intended assets absent, retained assets present and no unintended loss |

Live CAMERA SYNC COMPLETE is scoped to a sealed snapshot/time; new media requires a successor generation for a current-camera complete claim. An older eligible cleanup snapshot can exclude newly created recordings, but only after revalidation, explicit retained counts and renewed confirmation of scope; never display whole-camera clear eligibility when only a subset is safe.

Default safety requires **at least two independently verified copies outside the camera on distinct storage domains**, normally phone plus external SSD or phone plus cloud; all three also qualify. Two directories or partitions on one physical device do not qualify. Source camera is not a copy. Replica verification records identity, assurance and availability; unavailable/missing/stale required proof invalidates safe-to-clear until revalidated or another independent eligible set satisfies policy. It does not retroactively erase a valid phone copy. Cloud is not required when phone+SSD satisfies policy. Later policy configurability must not silently weaken the accepted default.

UNKNOWN_POTENTIALLY_REQUIRED blocks safety until disposition or safe preservation on the required independent domains; delete identity and collateral behavior must additionally be unambiguous. Unknown types are never automatically disposable. Evidence-backed unrelated unknown assets can remain on camera and do not automatically fail the backup. [Cleanup design](VERIFIED_SNAPSHOT_CLEANUP.md) controls exact retained scope, preconditions and operations.

GPS opt-in does not change backup eligibility. Existing embedded telemetry is preserved as part of original bytes; non-regenerable telemetry sidecars are KNOWN_REQUIRED and share their parent capture-day grouping. No coordinate extraction into logs is allowed.

Resolved user decisions: audio/metadata inclusion, six-class unknown disposition, default two-domain safety and mandatory capture-day layout. Target enumeration, metadata and destructive protocol capabilities remain NOT_TESTED; no new camera inventory/download/cleanup performed in this reconciliation.
