# Explicit verified-source-snapshot cleanup

Status: requirements and proposed architecture only, NOT_TESTED. R-038/R-039; ADR0005. No delete/format command, test or camera interaction is authorized by this document.

## Eligibility and scope

`DELETE_VERIFIED_SOURCE_SNAPSHOT(snapshot_id)` is the only proposed primary cleanup operation. It never means delete everything currently on camera. An immutable plan contains exact camera/storage-generation references, recording/member asset versions, proven command addresses, approved collateral-effect closure, replica proof references, policy version and retained/excluded inventory. Neither timestamp folder nor handle/name alone is a safe source identity.

`SAFE TO CLEAR CAMERA(snapshot_id)` is a scoped, revocable informational predicate. Require complete relevant-store enumeration with all pages and no unresolved errors; all required members accounted for; all required/precautionarily preserved assets LOCAL_VERIFIED; no covered PARTIAL/VERIFYING/FAILED/RETRY_PENDING item; two independently verified outside-camera storage domains (phone plus SSD or cloud), and any additional configured mandatory replicas; fresh source/camera/storage identity revalidation; and no destructive ambiguity. Safely preserved unknowns need explicit safety disposition and exact identity/effect mapping, not an unexplained checkbox. Unrelated retained assets are not implicitly eligible.

Replica proof must be current at preparation and execution: revalidate identity, availability and recorded integrity evidence of the required copy set. Offline SSD, inaccessible cloud, revoked grants, missing/changed files or unknown verification invalidate eligibility until sufficient independent copies are revalidated. Historical VERIFIED remains history and may still support local completion; it is not current delete authorization. Verification assurance for destructive use must be reviewed more strictly than baseline size/EOF checks; identity ambiguity or unknown remote equality limitations must be dispositioned in GATE-9, never waived by a green UI.

Re-enumerate before enabling the action and again after confirmation immediately before commands. Compare all relevant stores with the approved versioned plan: new, changed, renamed, absent or replaced objects, reused handles, SD swap/reformat and missing pages invalidate the old execution permit. Recompute and present changed scope for fresh confirmation. New/unverified media are excluded, even if created between backup and cleanup. Required unknowns that might belong to the approved recording block it until resolved or safely preserved; provably distinct new recordings remain intact and are shown as retained.

No infinite freshness promise: before each command revalidate the addressed asset and exclusive session/storage epoch. Pause sync writes, GPS session ownership and other camera-mutating controls during execution. If the target protocol cannot safely bind a command to the intended object/version or prevent/address handle reuse during a concurrent recording race, that case is unsupported and deletion stays disabled. Never infer safety from a client-side mutex alone. Any new camera activity or identity change revokes execution eligibility. A plan may only be narrowed/reconfirmed, never automatically expanded to new assets.

Group effects must be known: if deleting a primary implicitly deletes companions/derivatives, every affected asset must be inventoried and explicitly covered by the approved plan, with required backup and classification satisfied. Unknown collateral scope blocks deletion. Known regenerable derivatives may be included explicitly with documented exclusion rationale; they are not silently counted as verified originals. Unsupported sidecar semantics cannot be worked around with storage format.

## Confirmation and continuation

Normal UX: informational safe status -> `Delete safely backed-up camera media` -> confirmation summary -> explicit destructive confirmation. Show recordings and assets, approximate bytes, verified storage destinations, retained new/unverified counts and latest verification time. No routine manual selection of hundreds of assets. Never use generic OK as the authorization. Cancel emits zero delete commands and preserves all media.

Bind persisted confirmation to snapshot/plan digest, policy, camera/storage identity, exact approved set and operation UUID. App intents, callbacks, scheduled jobs, sync completion, availability events and generic retry timers cannot mint or renew confirmation. Recreated UI observes the operation without a duplicate command. Process death/connection loss stops destructive dispatch: read-only reconnect/enumeration may reconcile the existing operation, but continuation requires an explicit `Resume confirmed cleanup` action after renewed preconditions, with unchanged remaining scope summarized. This conservative continuation policy prevents surprise background deletion and automatic resend of uncertain commands.

Optional enhanced confirmation: evaluate press-and-hold as a later preference, with accessible explicit confirmation as fallback. Swipe/biometric authentication may add friction or accessibility/platform complexity and does not replace snapshot safety. None is required by the default policy; exact optional UX can be decided in GATE-9.

## Separate durable state machine

| State | Condition / next transition |
|---|---|
| DELETE_NOT_ELIGIBLE | Any prerequisite missing; read-only assessment may produce DELETE_ELIGIBLE |
| DELETE_ELIGIBLE | Informational scoped proof; user cleanup action -> DELETE_REVALIDATING |
| DELETE_REVALIDATING | Full identity/source/replica comparison; valid plan -> DELETE_CONFIRMATION_REQUIRED; ambiguity -> DELETE_USER_ACTION_REQUIRED |
| DELETE_CONFIRMATION_REQUIRED | Persist exact proposed scope; explicit confirmation -> final pre-command revalidation -> DELETE_IN_PROGRESS; cancel -> DELETE_NOT_ELIGIBLE |
| DELETE_IN_PROGRESS | One fenced operation; journal per-asset intent before each command, never blind retry; loss/stop -> DELETE_PARTIAL; attempted set processed -> DELETE_VERIFYING |
| DELETE_PARTIAL | Preserve confirmed/unknown/not-attempted sets; reconnect only reads; explicit resume after reconciliation/confirmation of remainder may continue |
| DELETE_VERIFYING | Complete post-delete inventory of intended AND retained assets; all criteria pass -> DELETE_COMPLETE |
| DELETE_COMPLETE | All intended absent, all excluded/new/unverified retained, no unintended disappearance, storage enumerable; only here CAMERA CLEANUP COMPLETE |
| DELETE_FAILED | Known non-recoverable rejection; record actual state and partial results, never full success |
| DELETE_UNVERIFIED | Reply/absence ambiguous or post-enumeration incomplete; no success; read-only reconciliation then explicit action |
| DELETE_USER_ACTION_REQUIRED | Wrong camera/store, unsafe identity, unavailable replica or unsupported capability; no destructive retries |

Example 100 approved / 37 confirmed / connection lost: record exactly 37 confirmed, remaining statuses separately (an in-flight request may be UNKNOWN, not automatically failed). Reconnect, fully enumerate and match strong identities. Already absent is `ABSENT_CONFIRMED` only under complete enumeration of the same storage/version context; distinguish absence observed from proof our command caused it. Do not reissue stale handles. Only still-present original approved members can enter a new explicitly resumed remainder. Treat protocol status as evidence, never as a substitute for inventory verification.

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

Proposed GATE-9 requires passed G2/G3/G7/G4 plus policy-required G5/G6 replica capability, reviewed source/identity/safe-clear predicates, all CL tests and S25 Ultra + Pocket 4P HIL on explicitly disposable test recordings. No real destructive use before separate authorization. GATE-8 zero-touch is independent and never authorizes deletion. Historical gates remain unchanged.
