# ADR0008 — Narrow B1/B2 authorization and GATE-1 closure

Date:2026-09-17. GATE-1 PASS for architecture/security baseline; general product implementation=false, release=false. [Implementation and verification evidence](../evidence/GATE-1/2026-09-17_B1-B2/REPORT.md) and its validation summary supersede ADR0007's B1/B2 blocked disposition without rewriting historical evidence.

Explicit user authorization permitted only launcher/test-intent hardening and stable Kotlin2.4.20 security remediation on `gate-1/security-foundation`, based on bootstrap efb4830e435b1efeef7a6de56fde0ea609806171. That finite scope is now completed, not an open implementation license.

B1 CLOSED: remove externally controllable test commands in all variants; validated saved-camera shortcut is only a hint requiring in-app confirmation. Both Activity entry points use the same policy and clean retained intents. No exported debug endpoint, new signature permission or BuildConfig.DEBUG-only trust boundary. Eight policy tests, actual empty-emulator Activity cold/warm tests and debug/release compilation/merged-manifest audit establish the scoped result. Physical hardware remains paused.

B2 CLOSED: resolved KGP2.4.20, successful debug/release compilation and authoritative LF tests (265 original plus8 security, no failures). Only the plugin version and two explicit nullable-Int result types are changed for B2. Initial new lint type findings were recorded before correction; final lint retains exactly the five original errors/79 warnings/2 hints. No suppression, deletion behavior change, processor or unrelated dependency update.

All other G1 architecture/security models retain their evidence-backed dispositions; future implementation and runtime tests stay in their assigned gates. No new immediate G1 blocker was identified. G1 PASS does not certify all baseline privacy/cleartext implementation, future sync reliability, signing or release safety. Known G0 limitations are preserved. Do not start G2, resume S25/Pocket, merge or release without the corresponding explicit authorization.
