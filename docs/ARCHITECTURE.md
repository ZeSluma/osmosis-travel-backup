# Architecture

## Status

This document separates:

- **Observed baseline facts** that must be re-verified against the repository.
- **Proposed project architecture** that is not yet implemented.

No functional implementation is authorized at bootstrap.

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
