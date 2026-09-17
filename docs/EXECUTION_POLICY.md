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

Repeated camera display states (including Playback running via DJI Mimo with Back to Live View) are recurrence evidence, not new standalone tests. Request the same observation again only when a new software change/build creates a specific hypothesis, it distinguishes concrete competing explanations, AND the answer changes the next engineering action. Otherwise reuse existing evidence and continue software work.

Stop only when hardware evidence is the actual remaining blocker; a product decision cannot be derived safely; credentials/authentication or Windows/Android/GitHub approval is required; an actual destructive action is required; a source-of-truth conflict remains unresolved; safety/security/data integrity requires human judgment; or a blocker has not converged after approximately three substantively different evidence-based attempts. Continue unaffected useful work first. At a hardware stop, provide one consolidated validation session, not an isolated micro-test unless necessary for safety/evidence isolation.

## Boundaries and cost

Never weaken VERIFIED, infer source deletion/completeness from incomplete observations, merge ambiguous identities without proof, present interrupted transfers as complete, or erase ledger/history. No automatic camera deletion, destructive testing, merge, release or modification of main without explicit authorization. Existing no-download/no-media-mutation restrictions remain in force.

Use targeted tests while iterating; broaden at meaningful checkpoints, gate closure or justified scope changes. Avoid repeated corpus reads, redundant probes, evidence micro-commits and unrelated refactoring. Exhaust useful preparation, not speculative perfection of category C work.
