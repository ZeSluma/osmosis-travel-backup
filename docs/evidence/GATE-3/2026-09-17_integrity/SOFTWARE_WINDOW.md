# Autonomous software window — live engineering record

Started2026-09-17T20:40:37Z; planned approximately two hours. No routine physical interaction or real-device scans. Hardware absence is queued, not an engineering stop. G3 remains NOT_TESTED; implementation authorized, release false.

## Work and evidence so far

- Guarded resume kernel rejects unknown/different source versions, invalid200/206/416 framing, modified prefix, destination race, short/excess body, cancellation and write/sync/journal/close/readback faults. Eight targeted tests PASS6s. Synthetic immutable-version trust is not a Pocket capability claim.
- Publication recovery finishes only a journaled PUBLISH_PENDING gap whose immutable integrity receipt matches measured local bytes/revision/URI. Current lease and exact replica binding fence old writers; old evidence epochs remain intact. No media byte writing or network request. Ten synthetic fault/epoch/reopen scenarios and real emulator MediaStore pending-to-public recovery PASS. Protected real files untouched.
- Strict fixed-length response-header parser rejects duplicate/case-variant contradictory headers, transfer-coding ambiguity, malformed/overflow length and control characters. Six tests added. Initial compile FAIL5s due Kotlin Map key variance in a test loop; corrected parser input covariance;14 targeted header/resume tests and builds PASS4s.
- Schema7 adds only immutable resume-prefix evidence. Migration must preserve all existing data and invent no proof for old partials. Prefix digests originate from received/synced bytes under independently established immutable source version; old Pocket partial B has no such proof and remains blocked. Repeated-resume Room integration, migration and restart tests are in progress. Camera adapter still supplies no trusted source version; it cannot activate this path.

Original schema7 emulator run reported `GATE2_ASSERTION_local-reconciliation:SYNTHETIC_LOCAL_AUDIT_QUERY:fixture_line_56` despite shell exit0. Classified FAIL, not PASS. The read-only audit still rejected schema>6; updated explicit supported bound to7. No database migration/data-loss cause inferred; rerun pending.

## Queue policy

The prior A/B UI recheck is folded into the final consolidated build/session. Do not request it separately. Candidate future checks include additive schema7 preservation/no invented B proof, independent A/B UI labels, publication-gap handling only on newly owned authorized fixtures, strict transport compatibility and any independently justified source-version evidence. Exact final PASS/FAIL/INCONCLUSIVE criteria will be consolidated after software preparation. No additional real transfer, mutation or cleanup is authorized by this software window.
