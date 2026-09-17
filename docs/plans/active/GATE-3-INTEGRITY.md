# GATE-3 — transfer integrity entry review

Predecessor G2 PASS at136b02b3951b18def39ace3062ca164584d1f2cd, supported by G2 identity-observations/CLOSURE.md. Dedicated branch codex/gate-3-transfer-integrity. No G3 implementation or real transfer has started; release false. Continuous authorization covers unambiguous safe work but does not authorize weakening identity/integrity claims.

Reviewed QUALITY_GATES, accepted dependency graph, TEST_PLAN SC08/T01-T04/S01-S03 and LEDGER_AND_INTEGRITY range/completion contract. G3 must fence/journal attempt and owned staging URI, validate exact64-bit200/206/range/length semantics, retain safe partial checkpoints, handle ENOSPC/flush/close/restart/publication gaps and never falsely verify. Tests use synthetic content; current camera/phone media cannot be downloaded, overwritten, hashed or deleted.

## Safety decision before production verification authority

Current Pocket adapter provides no proven immutable source version/checksum. G2 exact candidate matching intentionally remains identityAmbiguous, including both existing local copies. The accepted design requires validated source version, but also describes an assurance-labelled identity_size_range_readable verification method without a remote checksum. It does not define evidence sufficient to waive immutable-version proof for this Pocket under cross-session replacement risk. A local hash alone cannot supply it. G2 filename-time evidence is calendar evidence, not independent object identity.

These constraints permit conservative transfer/revalidation blocking; they do not by themselves authorize a weaker LOCAL_VERIFIED claim. Need explicit product/security disposition before enabling that promotion for this target:

1. Strict: retain TRANSFERRED_UNVERIFIED/NEEDS_REVALIDATION whenever source-version equality cannot be demonstrated; develop strict engine with synthetic source-version proof. No promise of actual Pocket LOCAL_VERIFIED until stronger evidence is obtained.
2. Separate lower-assurance result: allow measured size/range/readability completion under an explicitly named state that is NOT LOCAL_VERIFIED and never satisfies SAFE TO CLEAR CAMERA; exact limits remain visible. Whether/when such a result counts as successful local backup requires explicit acceptance.

Never silently interpret either option as camera-byte equality or cleanup authority. No need for another identical hardware rescan to resolve this policy. No source deletion, merge, release, main changes or real media operations. Current Human Stop Condition is safety/product assurance semantics, not gate boundary or temporary device absence. This review does not revoke existing scoped implementation authorization; it blocks the ambiguous verification-authority choice.

After disposition, implement minimal response/identity validation and targeted fake failure matrix first, then durable journal/publication boundaries, broad checkpoint tests and separately authorized non-critical-media HIL. Preserve all baseline failure evidence.
