# Explicit verified-source-snapshot cleanup

Status: requirements and proposed architecture only, NOT_TESTED. R-038/R-039; ADR0005/0006. GATE-9 is accepted, NOT_TESTED. No delete/format command, test or camera interaction is authorized by this document.

## Eligibility and scope

`DELETE_VERIFIED_SOURCE_SNAPSHOT(snapshot_id)` is the only proposed primary cleanup operation. It never means delete everything currently on camera. An immutable plan contains exact camera/storage-generation references, recording/member asset versions, proven command addresses, approved collateral-effect closure, replica proof references, policy version and retained/excluded inventory. Neither timestamp folder nor handle/name alone is a safe source identity.

`SAFE TO CLEAR CAMERA(snapshot_id)` is a scoped, revocable informational predicate. Require complete relevant-store enumeration with all pages and no unresolved errors; all required members accounted for; all required/precautionarily preserved assets LOCAL_VERIFIED; no covered PARTIAL/VERIFYING/FAILED/RETRY_PENDING item; two independently verified outside-camera storage domains (phone plus SSD or cloud), and any additional configured mandatory replicas; fresh source/camera/storage identity revalidation; and no destructive ambiguity. Safely preserved unknowns need explicit safety disposition and exact identity/effect mapping, not an unexplained checkbox. Unrelated retained assets are not implicitly eligible.

Replica proof must be current at preparation and execution: revalidate identity, availability and recorded integrity evidence of the required copy set. Offline SSD, inaccessible cloud, revoked grants, missing/changed files or unknown verification invalidate eligibility until sufficient independent copies are revalidated. Historical VERIFIED remains history and may still support local completion; it is not current delete authorization. Verification assurance for destructive use must be reviewed more strictly than baseline size/EOF checks; identity ambiguity or unknown remote equality limitations must be dispositioned in GATE-9, never waived by a green UI.

Re-enumerate before enabling the action and again after confirmation immediately before commands. Compare all relevant stores with the approved versioned plan: new, changed, renamed, absent or replaced objects, reused handles, SD swap/reformat and missing pages invalidate the old execution permit. Recompute and present changed scope for fresh confirmation. New/unverified media are excluded, even if created between backup and cleanup. Required unknowns that might belong to the approved recording block it until resolved or safely preserved; provably distinct new recordings remain intact and are shown as retained.

No infinite freshness promise: before each command revalidate the addressed asset and exclusive session/storage epoch. Pause sync writes, GPS session ownership and other camera-mutating controls during execution. If the target protocol cannot safely bind a command to the intended object/version or prevent/address handle reuse during a concurrent recording race, that case is unsupported and deletion stays disabled. Never infer safety from a client-side mutex alone. Any new camera activity or identity change revokes execution eligibility. A plan may only be narrowed/reconfirmed, never automatically expanded to new assets.

Group effects must be known: if deleting a primary implicitly deletes companions/derivatives, every affected asset must be inventoried and explicitly covered by the approved plan, with required backup and classification satisfied. Unknown collateral scope blocks deletion. Known regenerable derivatives may be included explicitly with documented exclusion rationale; they are not silently counted as verified originals. Unsupported sidecar semantics cannot be worked around with storage format.

## Confirmation and continuation

Normal UX: informational safe status -> `Delete safely backed-up camera media` -> confirmation summary -> explicit destructive confirmation. Show recordings and assets, approximate bytes, verified storage destinations, retained new/unverified counts and latest verification time. No routine manual selection of hundreds of assets. Never use generic OK as the authorization. Cancel emits zero delete commands and preserves all media.

Bind authorization to operation UUID, cleanup snapshot ID, plan digest/exact asset-version and collateral set, camera/storage identity and generation, current redundancy identities/proof versions, policy and safety-validation generation. A prior displayed SAFE label is not authorization. New assets, stale/invalid proofs, relevant SSD/cloud loss, identity changes, required unknowns, partial enumeration or ledger inconsistency immediately set safety false and stop dispatch; material changes invalidate approval. Revalidate immediately before execution. No silent substitution of a different replica proof set under old approval.

Default: one explicit destructive confirmation, no typed phrase/per-file selection/repetitive dialogs. Show latest source AND redundancy verification, exact scope and retained counts. Enhanced biometric/hold/swipe is optional and OFF unless evidence supports a reviewed stronger default.

[ADR0006](../decisions/0006-cleanup-gate-and-authorization.md) defines bounded continuation: transient disconnect may resume under the same still-live, unexpired operation authorization only after exhaustive read-only reconciliation proves the same remaining approved asset versions, camera/storage, replica evidence and safety generation. Expected confirmed removals shrink the work set without changing scope. No user pause/cancel, process death, service termination, camera restart, new files or material proof change is allowed on this path; no-progress expiry initially 10 minutes, to be validated. On any exception, revoke and request one fresh confirmation of the still-present remaining intended set after revalidation. Completed items are not reconfirmed or resent. UI recreation alone attaches to the same live owner. No generic job/retry/sync callback can initiate cleanup or mint approval.

## Separate durable state machine

| State | Condition / next transition |
|---|---|
| DELETE_NOT_ELIGIBLE | Any prerequisite missing; read-only assessment may produce DELETE_ELIGIBLE |
| DELETE_ELIGIBLE | Informational scoped proof; user cleanup action -> DELETE_REVALIDATING |
| DELETE_REVALIDATING | Full identity/source/replica comparison; valid plan -> DELETE_CONFIRMATION_REQUIRED; ambiguity -> DELETE_USER_ACTION_REQUIRED |
| DELETE_CONFIRMATION_REQUIRED | Persist exact proposed scope; explicit confirmation -> final pre-command revalidation -> DELETE_IN_PROGRESS; cancel -> DELETE_NOT_ELIGIBLE |
| DELETE_IN_PROGRESS | One fenced operation; journal per-asset intent before each command, never blind retry; loss/stop -> DELETE_PARTIAL; attempted set processed -> DELETE_VERIFYING |
| DELETE_PARTIAL | Preserve confirmed/unknown/not-attempted sets; reconnect first reads; unchanged active authorization may continue under ADR0006, otherwise fresh remaining-scope confirmation |
| DELETE_VERIFYING | Complete post-delete inventory of intended AND retained assets; all criteria pass -> DELETE_COMPLETE |
| DELETE_COMPLETE | All intended absent, all excluded/new/unverified retained, no unintended disappearance, storage enumerable; only here CAMERA CLEANUP COMPLETE |
| DELETE_FAILED | Known non-recoverable rejection; record actual state and partial results, never full success |
| DELETE_UNVERIFIED | Reply/absence ambiguous or post-enumeration incomplete; no success; read-only reconciliation then explicit action |
| DELETE_USER_ACTION_REQUIRED | Wrong camera/store, unsafe identity, unavailable replica or unsupported capability; no destructive retries |

Example 100 approved / 37 confirmed / connection lost: record exactly 37 confirmed, remaining statuses separately (an in-flight request may be UNKNOWN, not automatically failed). Reconnect, fully enumerate and match strong identities. Already absent is `ABSENT_CONFIRMED` only under complete enumeration of the same storage/version context; distinguish absence observed from proof our command caused it. Do not reissue stale handles. Only still-present original approved members can continue under the strictly bounded original authorization or a freshly confirmed remaining plan per ADR0006. Treat protocol status as evidence, never as a substitute for inventory verification.

## Ledger and sanitized audit

`CleanupSnapshot`: immutable scope, camera/storage identities and generation confidence, source inventory fingerprint, approved recording/asset versions, required/excluded/retained set, policy and proof references.

`CleanupOperation`: operation ID, snapshot/plan revision and digest, user initiation and confirmation timestamps/status, eligibility version, state, fenced owner, current step, counts intended/confirmed-deleted/retained/failed/unknown, verification result and reason. `CleanupItem`: assetVersion, intended command/effect scope, durable intent/dispatch/response evidence, actual post-enumeration presence and last validated epoch. Record confirmed/sent/unknown independently. DB commit cannot be atomic with camera IO; crash after send is UNKNOWN until reconciled.

Private ledger retains minimum exact identity needed for recovery. Exported audit contains opaque snapshot/camera/item references, UTC/elapsed times, safe reasons and counts, user-confirmed boolean and post-verification result. No credentials, GPS, media contents, raw protocol or personal filenames. Retain correctness/audit records independently of diagnostic ring rotation; pruning verbose logs must not erase confirmation or partial cleanup state. Replica records persist after source removal; cleanup never cascades deletion to phone/SSD/cloud.

## Current read-only capability evidence and limitations

Baseline source `2fcdbc97e6dbefc875d425368be67cf32b50bb06`, inspected 2026-09-17; no command executed:

| Question | Evidence / disposition |
|---|---|
| Individual remote delete | CameraSession.deleteFiles at line 1142 sends DUML 0x00/0x28 using manifest handles. MainActivity.confirmDelete has a user dialog. Pocket 4P destructive behavior NOT_TESTED |
| Address safety | CameraFile.deletable/opHandle lines 118-131 rejects missing/shared handles and fitted favourite-only handles. Source comments document handle reuse/collisions on other models; not stable target object identity |
| Bulk | MainActivity.runBulkDelete passes a handle list; deletePayload line 1235 serializes it. Source comment says n=1 validated against Nano capture, multi-handle inferred. UI exists but safe target batching NOT_VERIFIED; prefer single-target commands only after proof |
| Reply authority / interrupted operation | Source avoids automatic reissue on no reply and re-lists. Current verifyDeleted (line 1202) checks newest page only, insufficient for complete absence proof. Success/failure response must be checked against exhaustive target inventory |
| Playback/session | Code refreshes session and asserts playback before inline delete. Comments cite Nano/Xtra observations, not Pocket proof; target active playback/recording behavior remains unverified |
| Groups/sidecars | No complete target evidence for independent/implicit deletion effects; required closure and ordering NOT_VERIFIED |
| Index refresh / idempotency | Re-list path exists; full pagination and stable-version idempotency NOT_VERIFIED; never resend by reused handle |
| Internal vs removable storage | Target generation and deletion address namespace/effects on both stores NOT_VERIFIED |

Existing upstream delete UI is baseline functionality, not the future safety-gated feature; no source change hides or endorses it here. GATE-9 must route/disable every destructive entry point through the safety policy before enabling project cleanup. If robust remote deletion is unsupported, retain backups, explain that Osmosis cannot safely clean the camera, provide a non-destructive verified-snapshot report, and leave any later camera-native deletion to separate explicit user control. Never substitute format or pretend external manual deletion was verified by the app.

## Gate placement and evidence

Accepted GATE-9 requires passed G2/G3/G7/G4 plus a passed G5 or G6 secondary replica capability sufficient for policy (both only when required), reviewed source/identity/safe-clear predicates, CL01-CL12 and S25 Ultra + Pocket 4P HIL on explicitly disposable test recordings. No real destructive use before separate authorization. GATE-8 zero-touch is independent and never authorizes deletion. Historical gates remain unchanged.
