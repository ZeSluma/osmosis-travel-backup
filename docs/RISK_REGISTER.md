# Risk Register

Current execution policy (2026-09-17): GATE-2 implementation is authorized; AGENTS.md now permits continuous progression through sufficiently defined non-destructive gates after dependency/evidence checks. Earlier statements requiring a new permission solely at a gate boundary are historical and superseded. Hardware explicitly resumed for non-destructive GATE-2 validation on 2026-09-17; earlier pause statements below are historical. No main mutation, merge, release or real deletion is authorized. Current implementation/evidence status is PROJECT_STATE.yaml, the active GATE-2 plan and docs/evidence/GATE-2/2026-09-17_hardware/REPORT.md.

## GATE-1 closure additions

[Closure evidence](evidence/GATE-1/2026-09-17_security-closure/REPORT.md) and ADR0007 supersede pending-review wording for design choices; existing runtime uncertainties remain.

| ID | Risk | Evidence / impact | Disposition |
|---|---|---|---|
| RISK-041 | Exported launcher exposes sensitive test controls and logs supplied PIN | Source-confirmed surface; exploitation NOT_TESTED; high | B1 CLOSED under ADR0008; hooks removed, validated shortcut confirmation and SC09 evidence; preserve regression coverage |
| RISK-042 | Affected Kotlin cache deserialization persists into new compiler/ledger work | Historical1.9.24 applicable; fixed2.4.20 now resolved, no KAPT introduced; supply-chain residual risk remains | B2 CLOSED under ADR0008; actually resolved2.4.20, build/LF tests and no new lint errors; CI/cache trust remains separate |
| RISK-043 | Plaintext prefs or OEM transfer expose camera credentials / stale ledger authority | Plaintext and missing explicit backup rules observed; high | Keystore blob migration, noBackup plus explicit rules, SC01/SC02; design resolved, enforcement untested |

RISK-013/024 resolved design: exact-IP policy plus scoped transport, not global cleartext or process binding; SC03 remains open. RISK-007/040 resolved design: typed bounded all-sink sanitization; baseline remains unsafe to assume sanitized. RISK-039 permission design separates GPS, fixes coarse/fine flow later and requires SC06. RISK-008 mutable actions/caches and signing remain pre-release hardening; no blanket security PASS. RISK-016/019 sleep and awake foreground drops remain separate, root causes unconfirmed.

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
| RISK-016 | Camera-side sleep/standby may drop Wi-Fi/BLE/session or impede backup continuation, independently of Android background behavior | Unknown; apparent idle transition reported, causality unverified | High if active transfer affected; not demonstrated | Separate CAMERA_SLEEP_BEHAVIOR observation; distinguish idle/display-only/active-transfer states, safe wake/recovery requirements in GATE 1 and later hardware validation; no power-setting changes during baseline |
| RISK-017 | Connection/job recovery requires manual intervention after temporary network loss | Observed in GATE-0 | High | AUTO_RECONNECT FAIL and AUTO_RESUME FAIL; design separate recovery responsibilities in GATE 1; manual range resume PASS does not resolve automation debt |
| RISK-018 | Completed local file is not visibly recognized as downloaded after restart/reconnection | Observed in GATE-0 UI | High | Separate file/MediaStore persistence from UI recognition; GATE-1 architecture and later idempotency tests; do not redownload merely to establish the observation |
| RISK-019 | Foreground session drops with camera awake; camera/app may show differing session states | Observed drop; state-desynchronization cause unconfirmed | High | Separate FOREGROUND_SESSION_DROP from sleep/background findings; GATE-1 transport/session health and safe recovery review; no credential reset without evidence |
| RISK-020 | Activity/execution lifetime ends with unjournaled work or duplicate engine after recreation | Baseline owner coupling observed; new design untested | Critical | R16/17/24; fenced single writer, durable intents, X01-X05 crash/lifecycle tests before one-tap acceptance |
| RISK-021 | Remote identity collision after storage change/name reuse causes unsafe skip or append | Unknown target stability | Critical | R19, evidence confidence/storage epochs, I01; no filename/handle-only match |
| RISK-022 | Partial pagination, missing store or required audio/metadata member produces false all-originals completion | Source lazy paging/companions observed; target completeness unverified | Critical | R20/21, sealed generation plus RecordingGroup requirements, I02-I04 |
| RISK-023 | DB/MediaStore publication race, ENOSPC or migration loses canonical truth | Plausible; untested | Critical | R23-25 journal/reconciliation, T/S crash matrix, no destructive migration/overwrite |
| RISK-024 | Camera process-wide binding leaks cloud/OAuth traffic or assumes unsupported dual Wi-Fi | Binding observed; concurrency unknown | High | ADR0004 per-Network audit and sequential default; N03/N04 |
| RISK-025 | Chosen Android execution model cannot meet screen-off/stop/thermal constraints | NOT_TESTED on selected host design | High | ADR0003 conditional recommendation; X02-X07; honest user-action fallback, no restart evasion |
| RISK-026 | Retry loops drain battery or silently misdiagnose network failure as stale credentials | Misleading credential suggestion observed; budgets unimplemented | High | R18/28, bounded jitter/persisted budgets, reason-confidence model, N01/N02/P01 |
| RISK-027 | Android17 local-network enforcement blocks camera TCP/UDP after target migration | Platform migration requirement confirmed | High | N05 target36 simulation and target37 permission tests; no premature permission addition |
| RISK-028 | Local hash or coarse size is misrepresented as remote equality; unsafe safe-clear policy | Baseline assurance limited; two-domain policy accepted, enforcement unverified | Critical | Explicit method/version, source-checksum limits, required independent replica policy; no safe-clear until policy/evidence satisfied |
| RISK-029 | One-tap capability claimed before lifecycle/recovery/enum integrity | Historical ordering mismatch confirmed | Critical | Accepted gate dependencies; G7 before G4, no renumbering or automatic implementation authorization |
| RISK-030 | Cross-platform fixture transformations obscure baseline regressions | CRLF-only 14 failures measured | Medium | Preserve original evidence; future canonical EOL/CI policy with Windows/Linux D01, no silent suppression |

## Capture-day organization risks — R-037

| ID | Risk | Likelihood | Impact | Mitigation / Gate |
|---|---|---|---|---|
| RISK-031 | Filename/default-phone timezone or wrong camera clock misfiles a delayed travel recording | Target timestamp semantics unknown | High | Source-priority resolver, provenance, deterministic uncertain fallback, conflict warnings; CD03/CD06/CD07, G1/G4 |
| RISK-032 | Retry/date change or same-name collision creates duplicate folders/files or splits required sidecars | New capability not implemented | High | Frozen ledger mapping, unique reservation, parent grouping, journaled directory/URI allocation; CD01/CD04/CD05/CD08, G2/G3/G5/G6/G7 |
| RISK-033 | Mixed-MIME phone collection rules prevent one recording directory; baseline clock-sync-on-connect distorts timestamp experiment | Baseline root split and clock command observed; target effects unverified | High | G1 supported storage-adapter review and pre-connect clock observations, no unsupported MIME placement or unapproved clock changes; CD05-CD07 |

## Explicit cleanup / optional-feature risks

| ID | Risk | Likelihood | Impact | Mitigation / Gate |
|---|---|---|---|---|
| RISK-034 | New/replaced source or reused handle deleted under stale approval | Target semantics unverified | Critical | Immutable snapshot, camera/storage/version checks before each command, fresh confirmation on changed plan, no bulk/format shortcut; CL01/CL04, accepted G9 |
| RISK-035 | False delete success from partial newest-page listing, lost reply or implicit group effects | Baseline listing limit observed; Pocket effects unverified | Critical | Full pre/post inventory and known collateral closure; no blind resend, per-item unknown journal; CL02/CL05/CL08/CL09 |
| RISK-036 | Stale/unavailable or non-independent replica enables destructive action | Design not implemented | Critical | Two current verified independent storage domains, invalidate eligibility on proof change, policy version binding; CL07 and AS tests |
| RISK-037 | Background job/replayed confirmation or process recovery silently initiates deletion | New workflow not implemented | Critical | Separate operation and UI-bound plan confirmation, bounded same-operation continuation or fresh confirmation per ADR0006, all entry points gated; CL03/CL06/CL10 |
| RISK-038 | Unknown-class policy either hides required assets or permanently fails unrelated auxiliary inventory | Target classification unverified | High | Six classes, provenance, precautionary preservation and distinct completeness predicates; AS01-AS03 |
| RISK-039 | GPS mode contends for BLE or backup implicitly collects location | Baseline mutual exclusion observed | High | Separate opt-in lifecycle/permission and connection arbitration; backup GPS OFF tests; GD01/GD02; not a proven foreground-drop cause |
| RISK-040 | Verbose/logcat/share leaks secrets or unbounded logging becomes reliability dependency | Baseline raw-string sinks and file-only count bound observed | High | All-sink allowlist, bounded normal/verbose channels, fail-safe diagnostics and sanitized explicit export; GD03-GD05, G1/G7 |

## Finding-to-requirement separation

APP_BACKGROUND_SESSION_LOSS maps to R17/R28; AUTO_RECONNECT and AUTO_RESUME map separately to R18; observed FILE/MEDIASTORE persistence does not resolve UI recognition (R3/R4/R16). Foreground drop maps to R28/RISK-019, with SESSION_STATE_DESYNC only a candidate. CAMERA_SLEEP_OR_IDLE remains unverified R36/RISK-016. Intentional WIFI_NETWORK_LOSS in TEST E is not camera sleep. CAMERA_POWER_OFF, BLE_SESSION_LOSS, ANDROID_PROCESS_DEATH, ANDROID_JOB_OR_SERVICE_STOP, PERMISSION_REVOKED, CREDENTIAL_CHANGED, CAMERA_REQUIRES_USER_CONFIRMATION and STORAGE_FAILURE each retain separate prospective reason/test entries in STATE_MACHINES/TEST_MATRIX; their presence there is not a claim they were observed. No root cause finalized during hardware pause.

## GATE-2 disposition

**RISK-044 — AP/session idle loss conflated with camera power-off or handled by an Activity-owned heartbeat.** Likelihood unknown for Pocket4P; impact high for unattended synchronization. R-042 assigns measured model-specific liveness and recovery to the lifecycle-safe G7 connection layer, with default camera settings and KA01–KA07. Existing heartbeat code is not proof of prevention; no timeout copied from Nano/Xtra. Auto Media Transfer/Mimo interaction remains unconfirmed; camera UI wording is not ownership evidence. Keep RISK-016/019 and saved-entry failure distinct. [Design/experiments](design/CAMERA_SESSION_LIVENESS.md), all NOT_TESTED.

Actual target observation: [first G2 hardware audit](evidence/GATE-2/2026-09-17_hardware/REPORT.md) confirms two pre-existing phone copies remain physically intact but have no linked local locator or transferred/verified state in the new ledger. Planner returns REVALIDATE_IDENTITY, not an established verified-copy skip. Carry safe legacy-copy reconciliation/no-retransfer proof into G3/G4 and retain G2 acceptance review; do not adopt by name/size or redownload to fabricate success. Separately, saved-entry reconnect failed while fresh rescan succeeded; [SAVED_ENTRY_RECONNECT_FAILURE](evidence/GATE-2/2026-09-17_hardware/SAVED_ENTRY_RECONNECT.md) has unconfirmed cause and is not proof of actual out-of-range, sleep or stale credentials.

RISK-020/021/022/023/031/032 now have software foundation evidence under GATE-2 (transactional fencing, preserved uncertainty, group/path persistence, rollback/migration/corruption tests). Target hardware remains NOT_TESTED; no lifecycle, complete-inventory, integrity or physical folder claim. RISK-008 expanded review found44 advisory IDs on11 unchanged parent build-tool coordinates; no returned advisory on new coordinates/runtime graph. Retain this debt for reviewed CI/toolchain remediation before release; do not mistake the limited G1 coordinate scan for a full transitive audit.
