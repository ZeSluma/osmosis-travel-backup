# Architecture

## Status

This document separates:

- **Observed baseline facts** that must be re-verified against the repository.
- **Proposed project architecture** that is not yet implemented.

No functional implementation is authorized at bootstrap.

## Intent rebase design — 2026-09-17

Planning recommendation, not installed architecture. The app-open contract intentionally starts/attaches to a transactional sync session; manual grid/queue operations are a recovery fallback. [Execution ADR](decisions/0003-android-sync-execution.md), [Network ADR](decisions/0004-camera-network-ownership.md), [ledger design](design/LEDGER_AND_INTEGRITY.md), [state machines](design/STATE_MACHINES.md) and [asset policy](design/ASSET_INCLUSION_POLICY.md) specify the target. Existing observed behavior below remains unchanged.

| Boundary | Owner / responsibility |
|---|---|
| Presentation | Activity/ViewModel observes immutable ledger projections; permission/consent UI and intentional commands only |
| SyncCoordinator | One fenced single writer per active camera/session; structured coroutine scope, planner, cancellation and truth |
| Execution host | connectedDevice FGS recommendation, conditional HIL; owns coordinator lifetime independently of Activity |
| CameraConnectionController | BLE discovery, pairing/AP wake, Network request and epoch, bounded recovery; emits typed state, owns no transfer truth |
| Protocol adapter | Upstream packet/handshake knowledge; one live datalink; explicit network context and session health |
| MediaRepository / enumerator | Walk every store/page/group, classify originals/companions, produce versioned completeness evidence |
| SyncPlanner | Compare current asset versions with verified/partial replicas, schedule missing work and automatically reevaluate after READY |
| TransferEngine | Bounded streams/range validation/checkpoints; fenced IO children, no UI queue dependency |
| Ledger | Room design, durable intents/identity/recordings/replicas/evidence and migrations; canonical reconciled truth |
| IntegrityVerifier | Checks explicit assurance policy; never equates EOF or local hash with source equality |
| Destination adapters | Pending phone storage first; later SAF SSD and independent cloud replication |
| Diagnostics | Typed redacted events and bounded export, not raw protocol/file logging |

```mermaid
flowchart TD
    UI[Activity and notification actions] --> C[SyncCoordinator - fenced writer]
    H[connectedDevice FGS host] --> C
    C <--> L[(Room ledger)]
    C --> N[Connection controller]
    N --> P[Upstream protocol adapter]
    P --> E[Complete enumerator]
    E --> C
    C --> T[Transfer engine]
    N --> T
    T --> V[Integrity verifier]
    V --> L
    T --> D[Pending phone destination]
    L --> R[Independent SSD or cloud replicas]
    L --> O[UI and notification projections]
```

## Required questions A-Q — concrete answers and proof limits

| Question | Selected design answer / unresolved proof |
|---|---|
| A. Session owner | SyncCoordinator under execution host; persisted session UUID/lease/epoch, never MainActivity |
| B. Network owner | CameraConnectionController owns callback/current Network in memory; epoch-fenced camera adapters |
| C. Reconnect without Activity | Controller drives association scan, proven AP wake, specific Network request and protocol handshake; platform consent uses USER_ACTION_REQUIRED, not hidden UI |
| D. Execution API | connectedDevice FGS for external-device session; ADR 0003 compares UIDT/dataSync/WorkManager/CDM and requires S25 proof |
| E. Screen off | Same service-owned state, bounded IO; CPU wake behavior explicitly tested and narrowly justified lock only if needed; no guaranteed immunity to OS limits |
| F. Background | Observer detaches; service/connection ownership remains; no Activity.onStop disconnect or Activity-created transfer job in target design |
| G. Android kills process | Lease fenced on allowed restart, reconcile bytes/ledger/source; no callback-dependent safety or promised automatic restart timing; force-stop requires user reopen |
| H. Camera unreachable | Preserve PARTIAL, classify reason, bounded backoff; power/approval/permanent conditions become specific user action |
| I. Unique asset | Internal immutable assetVersion + evidence-based camera/storage/object fingerprint; cross-epoch stability not proven, uncertain matches revalidate |
| J. LOCAL_VERIFIED | Exact size/version, correct range/body completion, readable flushed URI, finalized publication, recorded verification method; optional source checksum only if real |
| K. Partial reconciliation | Persisted URI plus actual length/checkpoint/source validation; no append of changed object or full 200 response; journal cross-system publication |
| L. Replica independence | Separate destination/replica records and required policy; recording-member completeness per destination; phone verified first |
| M. Cloud isolation | Per-Network camera sockets and separate Internet client; sequential camera then cloud default, concurrent Internet NOT_VERIFIED |
| N. CAMERA SYNC COMPLETE | Sealed stable full source generation, no unknown required member, every required phone assetVersion LOCAL_VERIFIED |
| O. SAFE TO CLEAR CAMERA | Local complete plus all explicitly configured required independent replicas verified; policy unresolved => no signal; never deletes |
| P. Genuine user cases | Pairing, Android approval, revoked permission, proven credential change, persistent camera absence, ambiguity/identity uncertainty, storage/grant failure, explicit user stop |
| Q. Proof | TEST_MATRIX automated fault boundaries plus real SM-S938B/Pocket4P lifecycle, permissions, routing, inventory and recovery; none newly run during pause |

Single writer serializes transitions; structured IO child failures become typed events, not unhandled scope cancellation that fabricates completion. Cancel/stop closes streams and releases network resources under finally blocks; durable truth must already be safe if finally never runs. Protocol adapters remain thin so upstream updates remain reviewable.

## Observed baseline facts to re-verify

Observed read-only on 2026-09-16:

- Android/Kotlin application.
- compileSdk 36, targetSdk 36, minSdk 29.
- JVM/JDK 21.
- versionName observed as 1.4.4, versionCode 29.
- Existing camera protocol implementation should be preserved where possible.
- Existing resumable/range download capability exists in the current download path.
- Roadmap notes that long downloads are started from an Activity-owned bare Thread, so process death can interrupt them.
- Roadmap describes a stateless grid tick. The 2026-09-17 source review confirms persisted resume URIs and MediaStore completed-copy lookup across restarts; this is not a durable verified backup ledger. See the GATE-0 baseline evidence.
- Current baseline writes media through existing app behavior; no project-specific persistent backup ledger exists yet.
- No project-specific OneDrive pipeline exists yet.

## Proposed component model

The completed GATE-0 hardware run observed persistent completed files/MediaStore rows but failed UI downloaded-state recognition after restart and manual reconnect. Keep these states distinct in GATE-1 design; source presence of a completed-copy lookup does not supersede observed UI behavior. Automatic reconnect and job resume also failed, while manual range continuation from the retained partial passed. All remain measured baseline limitations; no behavior was changed.

Preserve upstream protocol layer.

Add project-specific capabilities as isolated modules where practical:

- `backup/`
  - orchestration
  - run state
  - policy
- `ledger/`
  - persistent media identity/state
  - idempotency
  - recovery
- `integrity/`
  - completion checks
  - size/checksum policy where source capabilities allow
- `storage/`
  - local verified store/staging
  - P310/external storage adapter
  - later OneDrive adapter
- `diagnostics/`
  - privacy-preserving incident pipeline

## Proposed high-level flow

Camera protocol
→ media enumeration
→ backup orchestrator
→ persistent ledger
→ transfer/resume
→ integrity verification
→ verified local state
→ downstream storage adapters
→ status/diagnostics

## Network considerations

Camera access may require binding traffic to the camera Wi-Fi/AP while later cloud sync requires ordinary Internet connectivity.

The design must explicitly handle network ownership/routing transitions. Do not assume camera-network binding and OneDrive Internet access can occur simultaneously without evidence.

Camera-side sleep/standby must be modeled separately from Android background/process lifecycle and generic reconnect failures. The 2026-09-17 user-reported apparent idle transition is provisional: actual sleep, Wi-Fi/BLE/session effects, wake recovery and active-transfer exposure are not yet established. See [separate evidence](evidence/GATE-0/2026-09-17_hardware/CAMERA_SLEEP.md) and RISK-016. GATE 1 must specify safe state detection and recovery for one-tap backup using measured camera capabilities, without assuming a dark display means lost connectivity or modifying camera power settings during baseline verification.

A separate awake-camera/foreground-app connection failure is recorded as [FOREGROUND_SESSION_DROP](evidence/GATE-0/2026-09-17_hardware/FOREGROUND_SESSION_DROP.md), RISK-019. A camera playback message concurrent with the app's failure message suggests a possible session-state mismatch but does not prove the cause or live connectivity. GATE 1 must distinguish transport, protocol session and UI state; this occurrence is neither a sleep nor an app-background disconnect.

## Storage considerations

External storage is expected to require Android storage APIs such as SAF or an equivalent supported mechanism. Verify target-device behavior before architecture is finalized.

## Security boundaries

Trust boundaries include:

- camera local network
- Android app process
- local storage
- external SSD
- future OneDrive auth/API
- diagnostics
- GitHub / build / release pipeline

## Thin-fork rule

Do not rewrite upstream protocol implementation unless evidence shows it is necessary. Prefer additive, isolated modules and upstream-compatible changes.
