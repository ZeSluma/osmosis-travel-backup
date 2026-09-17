# ASSET_INCLUSION_POLICY — version 1

Inclusion policy explicitly confirmed by the user on 2026-09-17: all non-reproducible recording-related source data is required. Primary originals, separate recording audio and processing-relevant/non-reconstructable metadata are included. This inventory distinguishes observed Pocket evidence from generic upstream capability. Actual HIL in GATE-0 transferred two MP4 originals only; no exhaustive Pocket asset census exists. Policy acceptance does not prove target enumeration support.

| Asset class | Current evidence | Confirmed inclusion policy |
|---|---|---|
| Primary full-resolution MP4/MOV videos | Pocket MP4 transfer measured; CameraFile supports MOV too | Required, every finalized original; MOV presence on target to validate |
| Primary photos/JPEG, DNG/RAW pairs | Generic model/sidecar code supports images and JPEG-to-DNG probing; target pairs NOT_TESTED | Every primary image and each available RAW partner required; cannot replace RAW with JPEG |
| Burst/interval and panorama members | CameraFile notes lead-only manifests, group expansion; evidence across other cameras is not Pocket-4P proof | All source originals/member assets required; rendered panorama not assumed to substitute for members |
| Independent recording audio / WAV companion | CameraFile.sidecarCandidate and MediaPreviewActivity probes, not an exhaustive listing guarantee | Required when associated with recording, explicitly user-confirmed |
| Recording metadata/sidecars | No complete target inventory/schema verified | Required if not reliably reconstructable from primary or potentially relevant to later processing; unknowns block recording completeness until classified |
| LRF/LRV proxies, low-resolution preview video | Model exposes proxyPath; isVideo also includes proxy extensions | Exclude from required originals only after positive type/role classification; optional replica policy separate |
| THM/SCR thumbnails and caches | thumbPath and protocol support | Exclude from required originals once positively classified |
| Temporary/cache/regenerable helper files | Actual target roles require verification | Exclude by default only after verified regenerable/temporary role; never classify by unfamiliar extension alone |
| OSV/INSV/XRF or any unknown companion | Generic extensions/type enum do not establish Pocket support/role | UNKNOWN, visible decision item; never silently omit or label required-original merely from isVideo |

A selected preview/trim is not the original asset. Do not substitute stream remuxes or frame captures for originals. Validate supported naming/type relationships without relying on extension alone; preserve relationships privately, diagnostics use opaque IDs. Absence of sidecar on transient 404 is not definitive; retry under bounded busy policy and record discovery completeness.

Every recording has explicit primary/audio/metadata/RAW/member relationships in the ledger. A recording is complete only when required-member discovery is resolved and every required asset is LOCAL_VERIFIED. A verified primary cannot conceal a missing required companion. Unknown asset types remain UNKNOWN/UNCLASSIFIED until purpose is verified; they are never automatically disposable. Recording-group requirements contribute to snapshot completeness and all downstream replica policies.

## Enumeration generation and truthful completion

SyncCoordinator enumerates independent of grid scrolling. Source currently lazily pages through CameraSession.fetchNextPage and uses per-store mapping; new planner must walk all present/eligible stores and pages, expand groups and resolve required companion assets. No single newest-page response, empty exception fallback or stable UI count means complete.

Create OPEN generation with store-presence evidence, cursor chain, raw record counts, unique asset versions and policy version. Validate page endings, repeated/nonadvancing cursors, truncation, overlap and expected counts. A missing store is not empty: establish absent card versus unmounted/unresponsive store. Failed page or unresolved required/unknown asset leaves INCOMPLETE. Treat protocol paging heuristics as candidates needing HIL validation, not immutable truth.

Proposed closure rule without an atomic camera snapshot: after transferring current eligible assets, perform a second complete enumeration and require matching normalized asset/version sets across every store plus completed group/companion discovery, no errors and no known active recording/mode change. Changed/new objects extend a new generation and schedule differences. Two matching scans provide an explicitly bounded stability observation, not proof the camera never changed between reads. Keep per-pass timestamps and source-confidence limits. If target protocol cannot support trustworthy page termination/stability, do not claim global completion; expose incomplete enumeration and require action/review.

Growing/active-recording assets are deferred until finalized identity/size is stable. Continue safe finalized work, but final current-camera completion is withheld while recording/unknown activity persists. Disappearance during transfer marks SOURCE_CHANGED/MISSING and preserves local data; never mirror camera deletion onto phone. Re-enumerate after reconnect before reuse when source continuity is uncertain.

`CAMERA SYNC COMPLETE(g)` requires a SEALED, stable complete generation g, resolved inclusion policy, and every required member having LOCAL_VERIFIED phone replica with matching assetVersion. State is scoped to that completed snapshot/time; new media revokes the live complete banner until the new generation is processed. Enumeration failure halfway blocks completion even if every known downloaded file is verified.

`BACKUP REDUNDANCY COMPLETE(g, policy)` additionally requires all configured required destination replicas VERIFIED/available according to policy. `SAFE TO CLEAR CAMERA` is informational, never an action, and remains unavailable until the user selects a redundancy policy and every required condition is supported. Proposed minimum is phone plus one independently stored verified copy; phone-only is not silently treated as safe. Cloud/SSD unavailability must not revoke valid CAMERA SYNC COMPLETE for phone, but prevents missing required redundancy completion.

Resolved: audio/metadata scope and conservative treatment of unknowns per explicit user clarification. Still open: required redundancy destinations for SAFE TO CLEAR CAMERA. Block broad completeness on unknowns, never discard them. No camera inventory/recording/download was performed for this planning task.
