# ADR0007 — GATE-1 security closure disposition

Date:2026-09-17. Status: evidence-backed architecture dispositions recorded; GATE-1 **BLOCKED**, implementation_authorized=false, release_allowed=false. This review does not authorize security fixes, dependency edits, functional extensions or hardware resumption.

The [closure report](../evidence/GATE-1/2026-09-17_security-closure/REPORT.md) and its three detailed audits resolve the previously open design choices: per-camera Keystore credential store with crash-safe migration; default-deny plus exact camera-IP exception and per-Network guards; connectedDevice service-owned coordinator and callback/transport manager; bounded sanitized diagnostics; optional GPS; target37 migration; minimized app-private ledger without an unjustified encryption dependency.

CameraConnectionManager and CameraConnectionController denote the same ADR0004 owner. Target network binding is removable only after every HTTP/native preview/TCP/UDP route is covered. FGS does not guarantee process survival. Runtime assumptions are assigned to G2/G3/G7/G4/G5/G6/G9 and remain NOT_TESTED; architecture closure does not require the later implementation to already exist.

B1 is the unnecessary sensitive test-extra surface on the otherwise necessary exported MainActivity. It requires actual scoped hardening and external-intent regression evidence before GATE-1 PASS. The merged AndroidX ProfileInstallReceiver is separately permission-protected; do not conflate it with an unprotected launcher hook.

B2 is Kotlin1.9.24's applicable advisory, disposition MUST_FIX_BEFORE_IMPLEMENTATION. Stable2.4.20 is the verified fixed candidate with documented toolchain range compatibility, not a tested upgrade. Explicit narrow security/dependency remediation authorization is the next action; current instruction allows documentation only. Until remediated or superseded by a new explicit evidence-backed policy decision, G1 remains blocked.

Existing credential/logging/cleartext/permission implementation gaps must meet SC tests before their target capability is accepted. They are not falsely claimed fixed by a resolved design. Fork signing and full resolved dependency verification remain release prerequisites. Historical GATE-0 PASS and baseline runtime failures retain their meaning. Accepted capture-day, six-class asset, redundancy, GPS and G9 confirmation/continuation decisions in ADR0005/0006 are unchanged.
