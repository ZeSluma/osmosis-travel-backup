# ADR0012 — Separate incomplete observations from actionable asset candidates

2026-09-17, G2 user-directed correction starting2a2e0ec. Prior identity is a fingerprint of source association, storage, full remote path, size, remote time, media type, handle and strongVersion. Null size changed the fingerprint and created a fourth candidate; historical local-candidate cross-claims then prevented selection of the known38MB copy. No source deletion was proven. Snapshot membership is an observation, never proof of absence outside it.

## Evidence rules

| Decision | Required evidence and limit |
|---|---|
| Reuse existing operational candidate | Exact composite fingerprint and positive known length. This is deterministic candidate matching, not proof of unchanged bytes; absent strongVersion retains identityAmbiguous and cannot establish completeness/VERIFIED. |
| Keep unresolved observation | Null/non-positive length; persist all supplied identity fields and source/snapshot provenance, no actionable asset/replica or transfer plan. Same basename alone never matches. |
| Different records | Different full source/storage/locator identifies different addressed candidates; differing metadata creates potentially different versions, not automatically distinct immutable originals. Distinct trustworthy immutable object/version tokens in their verified source domain can prove distinct versions. Source association continuity must itself be established before treating tokens as authoritative. |
| Link/collapse a redundant observation | Exactly one candidate in the same source/storage/full path with the same non-null trustworthy strongVersion; every supplied size/type/time/handle field compatible. Persist RESOLVED_SAME_VERSION and target ID; retain observation/history, never physically delete/merge assets or move replicas. Multiple matches or absent/contradictory token remains unresolved. Current Pocket adapter never supplies such a token. |
| Absence | Never deletes, marks gone or resolves a conflict on an INCOMPLETE snapshot. No automatic source-deletion feature added. |

The strongVersion field is an input contract for independently trustworthy immutable version evidence, not permission to fill it from filename/size/hash-of-metadata. No current Pocket observation is promoted to this proof. Multiple pre-existing full-sized candidates are not merged by this change; safely collapsing their files/recording links requires stronger reviewed evidence and remains prohibited. The required observation collapse is an auditable reference link, not destructive database cleanup.

## Minimal implementation

Additive schema4→5 adds identity_observations. It backfills incomplete legacy identities as UNRESOLVED, retaining every old asset/recording/membership/replica/local-candidate/capture-evidence row. Operational asset queries require positive known size; raw historical rows remain queryable. New incomplete observations are keyed deterministically by snapshot plus full evidence fingerprint. Repeated ingestion is idempotent; later observations never overwrite an earlier payload. Linking to a proven candidate preserves the original row.

Incomplete legacy rows have no authority to compete for a selected local replica. Local matching still requires current scoped metadata/name/positive size/published state and remains LOCAL_PRESENT_UNVERIFIED, not integrity proof. Historical candidate references are retained even during ambiguity; stale references cannot authorize completion because state/plan requires revalidation. After exact known re-observation, current inventory can reselect the known candidate and recover a pointer previously cleared by schema4. No media reads/writes/download/deletion or SQL hard-coded target repair.

Any unresolved source observation or identityAmbiguous current asset blocks snapshot COMPLETE even if all coverage flags are true. Historical ambiguity therefore remains explicit and may require future stronger evidence; removing a false operational cross-claim is not resolving source identity. G2 validates truthful durable behavior; complete all-original source proof remains G4 scope. No fictional trustworthy snapshot is needed to pass a software transition test.

## Verification boundary

Targeted pure tests cover evidence sufficiency/contradiction. Guarded emulator replay includes the observed3-known+1-null-size legacy condition, schema4 migration, partial/repeated enumeration, exact later re-observation, both local references, unchanged paths, restart, order-independent plan, historical retention, unresolved completeness block and later explicit strong-version observation linking. Target deployment/evidence recorded in the associated identity-observations report. No new hardware rescan solely to re-demonstrate these deterministic database rules; physical evidence is required only for an unproven target integration fact.
