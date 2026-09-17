# Risk Register

| ID | Risk | Likelihood | Impact | Mitigation / Gate |
|---|---|---:|---:|---|
| RISK-001 | Upstream camera protocol changes/regressions | Medium | High | Thin fork, upstream watch, hardware gates |
| RISK-002 | Android process death / network binding interrupts large transfers | High | High | Persistent orchestration/ledger, GATE 2/3/7 |
| RISK-003 | Partial transfer is marked VERIFIED | Medium | Critical | Explicit state machine + integrity gate |
| RISK-004 | Duplicate/repeated runs re-transfer data or corrupt state | Medium | High | Persistent identity/idempotency tests |
| RISK-005 | P310/SAF disconnect causes inconsistent backup state | Medium | High | GATE 5 disconnect/reconnect tests |
| RISK-006 | Cloud auth/network transition interferes with local camera workflow | Medium | High | Local-first design, GATE 6 |
| RISK-007 | Sensitive data leaks into logs/diagnostics | Medium | High | Redaction + log policy + security tests |
| RISK-008 | Dependency/supply-chain compromise | Medium | High | Dependency due diligence, action pinning, scanning |
| RISK-009 | Fork drifts too far from upstream | Medium | Medium | Thin-fork rule, periodic upstream review |
| RISK-010 | Fork signing/release pipeline missing or misconfigured | Medium | High | Future release prerequisite; signing review in GATE 1; absence alone does not block GATE 0 |
| RISK-011 | Device storage exhaustion during travel backup | Medium | High | Space checks, safe failure, user status |
| RISK-012 | User clears camera after a false-success signal | Low-Medium | Critical | Strict SAFE TO CLEAR policy, no auto-delete |
| RISK-013 | Global cleartext policy is broader than required | Medium | Medium-High | Scope review in GATE 1 without breaking camera HTTP |
| RISK-014 | OneDrive becomes accidental prerequisite for local success | Medium | High | Enforce INV-007 |
| RISK-015 | AI/tooling acts on stale state after interruption | Medium | High | Mandatory bootstrap + PROJECT_STATE + evidence |
