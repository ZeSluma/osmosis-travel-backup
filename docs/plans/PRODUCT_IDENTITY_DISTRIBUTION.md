# Future Product Identity / Distribution phase

Status: NOT_TESTED / future requirement, R-043. Explicit user instruction2026-09-17. No GATE-2 branding, applicationId, package, signing, icon, theme or UI changes authorized by this plan.

## Sequence

Begin the dedicated phase after the core autonomous synchronization workflow is evidenced (G2/G3/G7/G4 prerequisites PASS), before public release and before final production cloud OAuth/deep-link/signing configuration. Preserve existing gate identities; this phase is an additional distribution prerequisite, not a retroactive failure of completed gates. G6 research and isolated non-production experiments need not wait, but production auth/redirect/signing finalization depends on identity finalization. Local backup remains independent of cloud.

## Required decisions and deliverables

- Independent product name, launcher icon, visual identity and color system; no invented final name in this planning checkpoint.
- UI centered on autonomous backup state, completeness, uncertainty, recovery and necessary user action rather than a manual download queue.
- Final applicationId/package identity and independent release/signing identity before production OAuth registrations, redirect/deep-link association and signing-bound configuration.
- Explicit migration strategy from development Osmosis installations where required. A different applicationId is a different installed Android application; never assume an in-place update or automatic access to the old app's ledger, Keystore, permissions, MediaStore ownership or URI grants. Design and test coexistence, safe metadata/ledger adoption, permission/re-authentication needs and rollback without deleting originals or completed copies, silently re-downloading or falsely marking them verified. Do not export credentials/keys as a migration shortcut.
- About / Credits acknowledging upstream Osmosis and all required open-source attribution; retain MIT license/copyright obligations and applicable third-party notices. Review actual license inventory before distribution.
- Clear independent third-party application statement: not affiliated with or endorsed by DJI. Product naming/iconography and DJI/Osmo references must not imply official affiliation.

## Acceptance before distribution/final production auth

Approved identity decision and assets; verified applicationId/package/signing consistency; documented and tested development-install migration/coexistence and data-preservation behavior; production OAuth/deep-link ownership/redirect/signing checks against final identity; attribution/license inventory and independent-affiliation disclosure review; UI acceptance against proven autonomous workflow. All pending/NOT_TESTED. Do not create secrets, sign releases, register production auth or publish under this planning request. release_allowed remains false.
