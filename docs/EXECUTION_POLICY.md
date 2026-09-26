# Autonomous execution and bundled hardware validation

Permanent hard cost priority: Terra/low for primary engineering; Luna/low for narrowly justified subagents. No automatic escalation. Sol requires explicit user approval after a reasonable evidenced Terra attempt; Astra and extra-high/max/ultra require explicit approval. Report a more expensive active root and recommend switching. Defaults never override client selections. Use targeted tests and narrow cached evidence; batch full verification at meaningful checkpoints. Autonomy and safety remain mandatory. Stop non-converging exploration after about three substantively different attempts rather than burning credits.

Permanent across the entire project: ADR0014, root AGENTS autonomy contract and PROJECT_STATE.execution are the operational control layer. Hardware is a last-resort global validation boundary only after useful software preparation and reachable non-destructive work are exhausted. The single current queue is docs/hardware/VALIDATION_QUEUE.json; gate-local queues are historical session records. Run tools/autonomy/control.py --check before proposing user interaction. No root/gate/feature/task boundary alone warrants a stop.

## Current software window

Explicit user instruction starts approximately two hours of autonomous software engineering at2026-09-17T20:40:37Z (target end22:40:37Z). ADB/device absence alone is not a Human Stop Condition. No routine USB, camera rescan, UI observation, recording, transfer, restart or Wi-Fi requests during this window. Queue hardware facts and continue useful implementation, unit/integration/emulator/migration/process-death, malformed protocol, identity, fault-injection and recovery tests. Prepare one consolidated high-information session after the window and useful software preparation, not after each small fix. Safety/data-integrity emergencies, destructive approval, credentials, unavoidable external approval and unresolved source-of-truth conflicts remain real boundaries. Never invent hardware PASS or waive gate dependencies.

Accepted user policy, 2026-09-17. Applies to the current and subsequent defined non-destructive gates. Maximize verified engineering progress per credit and autonomous progress per human interaction without weakening project invariants.

## Default cycle

Implement → targeted test → simulate/emulate → analyze → fix → regression test → continue. Persist important evidence and determine the next unambiguous step from AGENTS, PROJECT_STATE, the active plan and ADRs. Tasks, commits, tests and gate boundaries are not stop conditions. Continue into the next defined non-destructive gate when its prerequisites actually pass.

Prioritize the vertical slice: Pocket available → source observed → missing/incomplete assets detected → safe transfer/resume → independent local integrity evidence → restart/process-death persistence → correct verified/unverified distinction.

Classify work:

- A: safety/data-integrity blocker; resolve before proceeding with affected work.
- B: current end-to-end capability blocker; resolve to advance the slice.
- C: non-blocking hardening; document and defer unless it becomes A or B.

## Evidence and hardware queue

Hardware is a validation resource, not the default next step. Label each claim SOFTWARE_PROVEN, EMULATOR_PROVEN, HARDWARE_PROVEN or UNPROVEN, including its exact scope. Simulation never substitutes for camera/device evidence it cannot establish. Hardware observations use PASS/FAIL/INCONCLUSIVE; INCONCLUSIVE does not become gate PASS. Gate states remain PASS/FAIL/BLOCKED/NOT_TESTED.

Maintain pending hardware-dependent facts in the active gate plan. Continue useful independent software work while they remain pending. Before requesting a session, finish reasonably useful unit/integration/emulator, lifecycle/restart/process-death, migration, deterministic state-machine, fault-injected camera, incomplete enumeration, transfer/resume, identity/reconciliation, static/regression, architecture and persistent-state checks.

Batch all currently useful hardware facts into one session with separate PASS/FAIL/INCONCLUSIVE criteria. Order checks to minimize installs, reconnects, rescans, power cycles, contamination and user effort. Keep evidence independent; split only where safety or attribution requires it. Use all returned results, investigate failures in software, and request isolated retests only for a specific genuinely necessary new hardware fact. Never repeat identical rescan micro-loops.

## Human stop conditions

### Mandatory closure audit — corrective rule (2026-09-18)

A `PASS` table, a successful build, an empty superficial TODO search, an ADB/device absence, or a
queued hardware batch is **not** proof that software preparation is exhausted. Before proposing
hardware or a human stop, the active agent must record a fresh closure audit covering the
end-to-end acceptance chain and all of the following: service/resource ownership; callback and
generation fencing; recovery/replacement and explicit-stop paths; durable inventory/plan/transfer
state; process recreation; replica independence; integrity and derived-status safety; and focused
fault tests. The audit must actively look for a missing integration test or an unfenced platform
callback, not merely reread PASS labels.

If the audit finds a safe software-testable gap, it is an open implementation item. Implement and
test it, update the completion plan and repeat the audit. Hardware is permissible only when this
fresh audit records `full_software_scope_complete: true` with evidence in `PROJECT_STATE.yaml` and
`tools/autonomy/control.py --check` independently permits the boundary. This rule exists because a premature 2026-09-18 stop was disproved immediately by an
unfenced stale-GATT-callback path; it is a process control, not evidence that hardware behavior was
already proven.

### Mandatory visible-state matrix — corrective rule (2026-09-25)

Before any hardware request, execute and record a deterministic matrix covering every visible
asset and product state: new, partial, locally integrity-confirmed with source identity still open,
existing-but-unconfirmed local copy, ambiguous/missing binding, trusted/incomplete inventory,
Camera Sync, redundancy and Safe-to-Clear. Each row must state its required durable evidence and
prove that no stronger completion claim appears. `PROJECT_STATE.execution.closure_audit` must set
`state_matrix_complete: true` and cite `state_matrix_evidence`; `tools/autonomy/control.py --check`
rejects a hardware boundary otherwise. This is a hard control, not a documentation convention.

### Mandatory visible-transition and terminal-state matrix — corrective rule (2026-09-26)

For every user-visible status, banner or progress control changed during a software cycle, tests
must cover: (1) the active entry state, (2) each meaningful progress/update state, (3) successful
terminal dismissal, (4) fail-closed/review terminal dismissal, and (5) a replacement or stale
callback arriving after a newer state. A visible grid with an incomplete durable source inventory
is an explicit required row: it must explain that automatic backup is waiting for a complete list,
not claim ongoing work. A progress bar may be shown only for an active service-owned operation;
completed acknowledgement must have a deterministic bounded dismissal and review/no-work/source-
changed states must clear it. Each row needs an assertion against both false completion and false
activity. Hardware may validate timing and rendering, but cannot replace this deterministic matrix.

### Mandatory full-requirement and combined-hardware audit — corrective rule (2026-09-25)

Before hardware is requested, audit every requirement in `docs/PRODUCT_COMPLETION_PLAN.md` against
the implementation and its reproducible software evidence, including all transfer, recovery,
process-death, integrity, replica, status, privacy and UI-observation paths. A successful test
subset, a build, or an individual fixed bug does not satisfy this audit. Any software-testable
gap reopens the software phase until it is implemented and tested.

At the same boundary, consolidate every remaining hardware-only criterion from
`docs/hardware/VALIDATION_QUEUE.json` into the smallest safe session. The plan must specify a
single ordered procedure and individual PASS/FAIL/INCONCLUSIVE outcomes; do not request a
one-function retest when another currently-open physical criterion can be tested safely in the
same session. `PROJECT_STATE.execution.closure_audit` must record both requirement-audit and
combined-hardware-plan evidence. `tools/autonomy/control.py --check` blocks hardware readiness
without all four explicit fields. This rule is permanent for Osmosis work.

Repeated camera display states (including Playback running via DJI Mimo with Back to Live View) are recurrence evidence, not new standalone tests. Request the same observation again only when a new software change/build creates a specific hypothesis, it distinguishes concrete competing explanations, AND the answer changes the next engineering action. Otherwise reuse existing evidence and continue software work.

Stop only when hardware evidence is the actual remaining blocker; a product decision cannot be derived safely; credentials/authentication or Windows/Android/GitHub approval is required; an actual destructive action is required; a source-of-truth conflict remains unresolved; safety/security/data integrity requires human judgment; or a blocker has not converged after approximately three substantively different evidence-based attempts. Continue unaffected useful work first. At a hardware stop, provide one consolidated validation session, not an isolated micro-test unless necessary for safety/evidence isolation.

## Boundaries and cost

Never weaken VERIFIED, infer source deletion/completeness from incomplete observations, merge ambiguous identities without proof, present interrupted transfers as complete, or erase ledger/history. No automatic camera deletion, destructive testing, merge, release or modification of main without explicit authorization. Existing no-download/no-media-mutation restrictions remain in force.

Use targeted tests while iterating; broaden at meaningful checkpoints, gate closure or justified scope changes. Avoid repeated corpus reads, redundant probes, evidence micro-commits and unrelated refactoring. Exhaust useful preparation, not speculative perfection of category C work.
