# Requirements

## 1. Product goal

Build a highly reliable travel-backup workflow for:

- DJI Osmo Pocket 4P
- Samsung Galaxy S25 Ultra
- Hagibis/P310 external NVMe SSD
- later OneDrive

Primary target:

Pocket 4P on
→ ideally one action on S25 Ultra
→ connect
→ enumerate all original media
→ identify only new/unverified originals
→ transfer
→ verify integrity/completeness
→ persist a verified local copy
→ trigger downstream SSD/cloud backup
→ clearly report status.

Reliability and data safety outrank convenience and feature count.

## 2. Functional requirements

- **R-001 Camera connection:** connect to the Pocket 4P using the minimum reliable interaction flow.
- **R-002 Complete enumeration:** enumerate all relevant original media required for backup decisions.
- **R-003 Persistent ledger:** persist backup state across process death and device/app restart.
- **R-004 Idempotent backup:** repeated backup runs transfer only files that are new or not safely verified.
- **R-005 Integrity:** never mark a transfer VERIFIED before transfer completion and integrity criteria pass.
- **R-006 Recovery:** interrupted work must resume or fail safely without presenting partial data as complete.
- **R-007 Local-first:** a verified local backup must succeed independently of cloud availability.
- **R-008 External storage:** support downstream verified backup to the P310/external NVMe path.
- **R-009 OneDrive:** later add downstream OneDrive backup without making cloud availability a prerequisite for local success.
- **R-010 User feedback:** clearly report current progress, success, retryable failure, fatal failure and unresolved state.
- **R-011 Safe-clear signal:** provide an informational `SAFE TO CLEAR CAMERA` state only when required backup policy has been satisfied.
- **R-012 No camera deletion:** do not automatically delete camera originals.
- **R-013 Diagnostics:** capture privacy-preserving technical incident information sufficient for debugging.
- **R-014 Large media:** support large files and large batches without assuming Activity lifetime.
- **R-015 Thin fork:** reuse upstream protocol and behavior where suitable; isolate custom backup logic.

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

1. GATE 0 — unchanged baseline
2. GATE 1 — security/architecture baseline
3. GATE 2 — persistent ledger + download-all-new
4. GATE 3 — integrity/failure handling
5. GATE 4 — one-tap backup
6. GATE 5 — P310
7. GATE 6 — OneDrive
8. GATE 7 — robust background/foreground behavior
9. GATE 8 — optional zero-touch detection
