# Intent-to-system audit — 2026-09-17

Scope: requirements/architecture/planning only. Hardware remains paused by explicit user instruction; no ADB/device/network/power actions. implementation_authorized=false; release_allowed=false. This document distinguishes verified facts, user-selected policy, design recommendation and untested assumptions.

## Bootstrap / publication reconciliation

Read AGENTS, PROJECT_STATE, core governance, active/completed plans, decisions/incidents, build/disposition/hardware evidence, workflows and relevant source. Published starting branch `bootstrap/project-initialization` at `0a848e536004f4f03a51254452f6bf431a956d8f`; main and merge-base `2fcdbc97e6dbefc875d425368be67cf32b50bb06`. Live fork parent is KonradIT/osmosis, both mains unchanged; latest upstream release v1.4.4 published 2026-09-07. Fork open issues/PRs none; upstream open items 40/32/24/11/8. Upstream read-only; no main mutation.

Important conflict: local working tree already contained prepared but uncommitted hardware evidence, GATE-0 PASS disposition and GATE-1 planning, while published branch contained only the earlier disposition. Neither local file existence nor a prepared PASS implied remote publication. The latest explicit rebase instruction authorizes ONE coherent planning/reconciliation commit; include that pending governance/evidence as provenance rather than discard it or create a second unsolicited commit. Preserve chronological findings and original failed results. Local handoff material stays untracked/excluded from commit. Hardware pause does not prohibit this newly requested docs task; it still prohibits resumed testing.

GATE-0 PASS is the previously prepared unchanged-baseline disposition with known failures, not certification of the clarified product. Do not finalize camera-sleep causality or the latest foreground-drop manual recovery; both remain open. This rebase does not mark GATE-1 PASS or authorize implementation. Earlier publication-hold narrative is historical; this explicit docs-push request supersedes the hold only for publishing known facts, not for inventing missing observations.

## Reconstructed outcome

Power on the known camera, intentionally open Osmosis, and obtain a complete, truthfully verified local snapshot without manual selection/queue/rescan/retry in normal operation. Recover safely where technically permitted; explain genuine physical/OS/permission blockers. Opening again is idempotent. Phone-local durability precedes independently verified SSD/cloud redundancy. Neither cloud availability nor Activity visibility is part of local success. Source deletion is never an automated operation.

The explicit asset decision includes original video/photo/RAW, separate recording audio and non-reconstructable or processing-relevant sidecars. Verified proxies/previews/thumbnails/temp/cache/regenerable helpers are excluded by default. Unknowns stay unclassified; recording-group membership makes a missing required sidecar prevent recording completion. Redundancy policy remains a user decision until confirmed; SAFE TO CLEAR stays unavailable without it.

## Measured mismatches -> requirements

| Evidence / observed mismatch | Required change, not root-cause claim |
|---|---|
| MainActivity owns transfer Thread/in-memory selected queue | R16-17 single service-hosted coordinator and ledger projections |
| APP_BACKGROUND_SESSION_LOSS observed | UI-independent network/session ownership, HIL lifecycle acceptance |
| AUTO_RECONNECT FAIL after intended Wi-Fi loss | R18 connection FSM with bounded automatic recovery and honest consent states |
| AUTO_RESUME FAIL after restored connection; queue appeared empty | R18 durable intent/partial planner automatically reacts to READY, independent of UI selection |
| Manual range resumes retained 374,356,968-byte partial and completes | Preserve adapter path; R22 adds object/Content-Range validation without rewriting protocol needlessly |
| Local file and MediaStore survive; UI marker absent after restart | R3/4/16 canonical asset/replica identity, observed-state projection; do not declare filename lookup sufficient |
| Awake foreground connection loss, camera playback message persists | R28 foreground-drop observation and state-desync hypothesis remain separate; instrument session health |
| CAMERA_SLEEP_OR_IDLE still uncertain | R36 dedicated future experiment, not fabricated explanation for awake foreground drop |
| newest-page/lazy pagination + generic isVideo includes proxies | R20-21 full snapshot enumeration and recording-member inclusion, not grid scrape or extension-only policy |
| No immutable Pocket object/storage identity established | R19 confidence/version evidence, safe revalidation; no unsafe dedup by path/handle |
| baseline EOF/size fallback, no strong verification ledger | R23-25 journaled staging/verification/publication, explicit assurance and storage failure handling |
| process-wide camera routing | R27 per-Network adapters and independent replica networking |
| background robustness historically after one-tap/cloud | Gate dependency proposal moves G7 before G4; no evidence renumbering |

## Implicit requirements discovered

Durable intent must precede external side effects because DB/MediaStore/socket writes are not one transaction. Exactly one writer requires fencing beyond a mutex. Distinguish Network availability from authenticated/healthy DJI session. A new connection epoch invalidates sockets and stale callbacks. A local hash proves local fingerprint, not camera equality. A completed primary does not complete its recording. A stable snapshot needs complete stores/pages/companions, and changing source must prevent stale complete banners. User cancellation cannot be treated as an invitation for a hidden scheduler to restart. Unknown cause must remain unknown rather than becoming a password prompt. Keeping historical source metadata privately is different from logging it.

## Source inventory and limits

Baseline source: CameraFile exposes storage/path/handle/size/type/duration/resolution, proxy/thumbnail locators, group and sidecar conventions; some mtime/fileIndex fields serve other models. CameraSession newest-page path and fetchNextPage/collectStores are not exhaustive target inventory proof. MediaPreviewActivity probes groups/companions on UI demand. ApJoiner binds the process, HttpClient and raw datalink sockets rely on default routing. Keep upstream docs/protocol code unchanged and adapt boundaries. No source checksum/volume-generation stability was established, no Pocket photos/RAW/sidecar inventory was verified in the two-video baseline.

## Assumptions requiring evidence

1. S25 connectedDevice FGS holds usable camera Network through app switch/lock and reacts correctly to system stops; not proven by the baseline app.
2. Per-Network HTTP/TCP/UDP works through every upstream path, with consent reuse; no concurrent Internet guarantee.
3. Pocket object identifiers, storage mappings, complete pagination and companion rules remain stable across required boundaries; format/name-reuse tests cannot use valuable originals.
4. Camera sleep/AP idle, power-off and BLE handoff need independent observations; foreground drop cause unknown.
5. Bounded wake-lock need/resource budgets and automatic recovery policy constants require HIL, not architectural faith.
6. Room version/toolchain compatibility and security advisory remediation require dependency review before addition; no Gradle changes here.

## Traceability to all request sections

| Request sections | Resulting artifacts |
|---|---|
| 1-4 intent/reliability/evidence/UI ownership | REQUIREMENTS R16-18/28, ARCHITECTURE owner table and A-Q, this audit |
| 5 Android execution | ADR0003 comparison/choice/stop semantics and proof requirements |
| 6,24 network/coexistence | ADR0004, N03/N04 tests; actual S25 support remains unknown |
| 7,12,19,21,25 connection/transfer/action/retries | STATE_MACHINES with explicit transitions/taxonomy/budgets |
| 8,9,13-16,28 ledger/identity/resume/integrity/storage/replicas/crash | LEDGER_AND_INTEGRITY and T/S/I/R test rows |
| 10,11,17 originals/enumeration/completion | ASSET_INCLUSION_POLICY with user clarification and recording dependencies |
| 18 camera idle | Separate baseline CAMERA_SLEEP evidence + C01/C02, R36 |
| 20,26,27 UX/resources/observability | R30/33/34, ADR0003 and STATE_MACHINES event schema |
| 22 security | SECURITY rebase threat/disposition table; open Gate-1 actions retained |
| 23 Android17 | ADR0004 migration and N05 planned target36 simulation; no manifest edits |
| 29,30 test discipline/EOL | TEST_MATRIX + TEST_PLAN, original Windows evidence preserved |
| 31 thin fork | adapter design, source diff validation; no protocol rewrite |
| 32 gate re-audit | REVISED_GATE_DEPENDENCIES graphs/migration; proposal status explicit |
| 33 invariants | AGENTS unchanged, all design promotion/cleanup policies preserve INV-001..008 |
| 34 A-Q | ARCHITECTURE explicit answer table with unverified boundaries |
| 35 deliverables | this audit, DELTAS, two ADRs, three design specs, gates, risk/security/test updates |
| 36 report/publication | final response and explicit branch-only commit/push verification |

## Decision log

Asset scope confirmed explicitly by user during this task; unknown types never automatically disposable. Redundancy choices were requested asynchronously; lack of response does not authorize a default safe-clear policy. Proposed gate ordering needs explicit adoption; selected execution/network recommendation needs review and HIL. No new hardware observations are implied by documentation.
