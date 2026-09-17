# ADR0014 — permanent project-wide autonomy control

Accepted by explicit user instruction. Applies to every gate, feature, defect, migration, refactoring, investigation and validation. A local hardware dependency does not exhaust the project. Safety and evidence requirements remain unchanged.

Root AGENTS defines the short contract. PROJECT_STATE.execution is the durable operational state; its JSON flow mapping is valid YAML and parsed with Python's standard JSON decoder, not an incomplete custom YAML parser. docs/hardware/VALIDATION_QUEUE.json is the single current queue; old gate-local files remain historical evidence. tools/autonomy/control.py evaluates global stops and emits documented hook JSON. It does no network, shell execution, state mutation or arbitrary context replay.

Hardware stop requires exhausted software and reachable follow-on work, a consolidated queue covering every pending item exactly once, meaningful prerequisites/criteria, and a new discriminating hypothesis for repeated observations. Safety, credentials, destructive authorization, external approval, source-of-truth conflict and explicit user pause permit stopping. Invalid state does not force execution. A Stop continuation is issued at most once through stop_hook_active; the loop guard prevents runaway continuation and is not a claim that a task/gate is complete.

SessionStart covers startup/resume/clear/compact and injects under1000 characters. PostCompact is deliberately omitted: its text output is ignored by current documented behavior; SessionStart(source=compact) is the supported developer-context path. Current-runtime hook activation/trust must be proven separately from synthetic script tests. Prepared configuration never disables sandboxing or bypasses hook trust.

Before any physical request: review existing evidence, physical necessity, urgency, remaining work, batching and repetition. Prefer one final build/session validating compatible facts. Software and emulator results never establish DJI hardware semantics. Gate prerequisites cannot be waived to manufacture follow-on work; useful prerequisite-independent planning/tests may continue within authorization.

Deterministic harness plan: reuse actual camera-to-ledger adapter, existing fault fixtures and transport parser; combine scenarios through a guarded synthetic integration runner. Prioritize crash boundaries and cancellation/source-change races identified by read-only review. Do not create an independent fake model that merely restates desired behavior.
