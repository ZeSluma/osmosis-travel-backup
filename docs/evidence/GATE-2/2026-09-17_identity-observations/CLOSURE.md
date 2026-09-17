# GATE-2 closure — schema5 target reselection

2026-09-17. User confirms3 videos visible. Read-only audit at the no-transfer checkpoint completed with metadata-only protected file stat (combined0.84s, exit0). Instrumentation replaces the process, does not read media or capture a live queue.

Schema5,1 source,9 snapshots,4 historical assets/recordings/members/replicas. Latest snapshot6aa5a378efb57300fbcabca0e887d147aa41986e2173c4cb759841d13ed9b759 contains exactly the original3 candidates, sealed INCOMPLETE/all_coverage_proven=false. Two unresolved observations remain unlinked. No new identity, filename merge or inferred deletion.

Original38MB candidate dd5a4029... now LOCAL_PRESENT_UNVERIFIED/PRESENT_UNVERIFIED, candidate_count1, selected locator657110c510a53bdca86dd00b1706b0c9bd2144ebac718281cbd56c50e44f36b8, production policy VERIFY_EXISTING. Large candidate9e181fce... retains locatorbaa24bf995e9f34832834ab911d7ed2528407606ce7b23769241df87cd93d659 and VERIFY_EXISTING. Both original sizes38447651 and3071380142 unchanged. This is metadata candidate association, not source-integrity proof. The legacy size-null identity remains UNRESOLVED_HISTORY. Third ee17e7b7... remains DISCOVERED/ABSENT, DJI_FILENAME day2026-09-17, destination identity unchanged; DOWNLOAD is recomputed planning only and was never executed. Exact local time/unknown zone were independently persisted in TARGET_PARTIAL.md.

Target migration and actual38MB reselection PASS. Earlier target NOT_TESTED is now superseded, not erased. Snapshot completeness remains INCOMPLETE, source-version ambiguity remains unresolved. No real transfers or file changes were needed to exercise this state transition.

## Acceptance disposition

| G2 criterion | Evidence / scope |
|---|---|
| Durable identity/state, paths, classification | Exported schema1..5,353 unit tests, guarded emulator migration/transaction/fencing suite; observed target upgrade retains history |
| Idempotence and restart | Prior target repeated generations/restarts and one-new-asset evidence; current3 originals with4 retained historical rows; additive unresolved observations do not multiply actionable assets |
| New/unverified-only planning | Both existing copies VERIFY_EXISTING; new third candidate unsatisfied; no production verification/payload transfer claim |
| Safe incomplete/ambiguous observations | TARGET_PARTIAL plus this re-observation; no deletion/filename merge, references restored without false VERIFIED |
| Capture-time/path persistence | Third DJI_FILENAME2026-09-17T18:40:37, day2026-09-17, UNKNOWN offset, stable reservation; original38MB old fallback path remains frozen with later conflicting-day evidence |
| Security / build | Existing scoped B1/Keystore/backup/migration suite PASS;353 unit tests/debug/release compile PASS; unchanged5-error lint baseline retained; no new dependency/configuration changes |
| Target integration | S25/Pocket normal connect, earlier repeat/restart/new asset, schema5 partial and later sufficient observation with actual local-candidate reselection PASS |

GATE-2 PASS for durable ledger/planner foundations, under accepted dependency scope. No one-tap/full-inventory/integrity/automatic-recovery claim. All-store/sidecar/version evidence remains G4; actual transfer integrity, publication/ENOSPC/process-death boundaries G3; session/reconnect/sleep reliability G7; broader OEM D2D/security scenarios retain their dedicated later proof obligations. No release authorization.

Next: bootstrap GATE-3 safe-transfer/integrity work under continuous execution policy, dedicated branch. Existing media remain protected; no download/delete/hash/copy of real media authorized. Synthetic software work may proceed. Main/upstream untouched.
