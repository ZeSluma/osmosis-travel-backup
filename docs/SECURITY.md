# Security

## Intent rebase threat/disposition review — 2026-09-17

Design review in progress, not SECURITY PASS. Baseline behavior is preserved; all fixes require later authorization. [ADRs](decisions/0003-android-sync-execution.md), [network design](decisions/0004-camera-network-ownership.md) and [test matrix](design/TEST_MATRIX.md) define boundaries.

| Boundary / threat | Required disposition before implementation/release acceptance |
|---|---|
| Camera credentials in ordinary private prefs | Credential-reference abstraction; evaluate Keystore-backed encryption at rest and invalidation/restore semantics; exclude secrets from logs, exports, backups and DB diagnostics; do not invent secure storage already present |
| Local cleartext and hostile camera/LAN input | Evaluate narrow network-security configuration for actual numeric/dynamic endpoints, do not assume domain rules cover CIDR. Adapter endpoint allowlist, no cross-host redirect, bounded parser sizes/timeouts, range validation and per-Network routing; retain compatibility evidence before tightening |
| Non-exported execution host, launcher/debug extras, intents/provider | Inventory exported components and existing test hooks; explicit immutable notification intents, input validation and least privilege; never let arbitrary intent claim verification/trigger delete |
| Logs/crash reports/media metadata | Typed event allowlist, reason confidence, no raw packet/exception/URL/credential/GPS/content; test secrets injected at all error paths. Disable unsolicited telemetry; export only sanitized bundle |
| Ledger and filesystem mutation/crash | Canonical transactional truth, fenced writers, no adopt-by-name, cross-system journal, no destructive migrations; valid replica cannot be overwritten by partial |
| Kotlin advisory / new Room toolchain | Baseline plugin1.9.24 advisory remains applicable/reduced exposure; future compatible fix and Room processor choice need advisory/provenance/license/build review. No upgrade/dependency added now |
| CI/signing | Mutable action/cache trust and artifact verification review, least-privilege tokens, no fork reuse of upstream signing, fork release signing separate; no secrets printed/requested/rotated |
| Cloud/SSD | Separate tokens/SAF grants and replica verification, cloud TLS independent of cleartext camera, no routing leakage; local success independent of remote services |
| Platform permission changes | Consent/denial/revocation distinct from camera authentication; future target37 local-network migration tested, not premature current manifest addition |

Existing no-auto-delete and informational-only safe-clear invariants remain absolute. Unknown media types are not disposable. Rebase documentation does not authorize hashing/copying private media or conducting new hardware tests during pause.

## Accepted safety / GPS / diagnostics reconciliation

ADR0005 adopts default phone plus one independently verified SSD/cloud storage domain, not phone-only safety. Replica availability and proof are revalidated before explicit cleanup. [Cleanup design](design/VERIFIED_SNAPSHOT_CLEANUP.md) requires exact immutable scope, non-forgeable internal confirmation binding, exclusive/fenced writer, strong camera/storage/object identity, known collateral effects and exhaustive post-verification. External intents, replayed UI actions, sync callbacks and retry jobs must not authorize deletion. No format fallback. Existing upstream delete entry points are baseline code, not evidence of policy enforcement; accepted dedicated later GATE-9 must cover every destructive path before real use. ADR0006 binds confirmation to source/replica proof versions and safety generation; new evidence immediately revokes safety, material changes revoke authorization. Narrow same-live-operation continuation is not a background initiation right.

GPS telemetry is explicit opt-in; backup never initiates location collection. Review permission and location-FGS/background implications before deciding cross-session GPS auto-resumption. BLE ownership arbitration must prevent telemetry/offload contention without falsely diagnosing the existing foreground drop as GPS-caused.

[GPS_AND_DIAGNOSTICS](design/GPS_AND_DIAGNOSTICS.md) applies the no-secret/GPS/media/PII rule to normal events, logcat, verbose files and exports. Normal event recording is bounded and independent of verbose mode. Verbose is OFF by default, temporary, app-private and explicitly exported only after sanitization; omit unsafe raw fields, never automatically upload. No GPS-coordinate diagnostic exception is enabled by this review. Baseline FileLog has no central redaction or per-file byte/time bound, and its share path is not a sanitized-export guarantee; privacy remains an open GATE-1 review item.

## Security goal

Protect media, credentials and user privacy while keeping the local camera workflow reliable.

## Baseline observations to re-verify

Observed read-only on 2026-09-16:

- app requests Bluetooth, location, Wi-Fi/network, foreground-service, notification and Internet-related permissions
- `android:allowBackup="false"`
- `MainActivity` exported
- observed service/provider components non-exported
- network security config globally allows cleartext traffic because cameras use local plain HTTP
- existing release workflow relies on signing secrets that must not be assumed to exist in the fork
- current CI actions should be reviewed for supply-chain pinning and permissions

## Required reviews per gate

- Android runtime permissions
- exported activities/services/receivers/providers
- intent handling
- network security
- credential storage
- Android Keystore use
- logging and PII
- dependency provenance
- dependency vulnerabilities
- secret scanning
- GitHub Actions permissions
- third-party action pinning
- build/signing flow
- SBOM before release
- future OneDrive OAuth/authentication
- foreground/background attack surface

## Cleartext traffic finding

Global `cleartextTrafficPermitted="true"` is a review finding, not an automatic defect.

The camera may require HTTP on a local/private address. Determine whether cleartext can be scoped more narrowly without breaking camera compatibility across supported models.

Do not weaken camera reliability merely to satisfy a cosmetic configuration change. Use evidence.

## Dependency policy

Before adding a dependency:

1. verify the package exists
2. verify official project/repository
3. identify maintainer
4. verify current stable version
5. check license
6. check known vulnerabilities
7. justify why it is needed
8. prefer platform/standard-library capability where practical

## Logging policy

Never log:

- media content
- GPS/location traces
- Wi-Fi passwords
- access tokens
- refresh tokens
- cloud credentials
- signing secrets
- unnecessary PII

Diagnostics must redact sensitive fields.

## Release security

Do not reuse or assume upstream signing secrets.

Fork release signing must be explicitly designed, documented and tested before releases are allowed.

Missing fork release signing is a release blocker, not by itself a GATE 0 blocker. GATE 0 observes signing assumptions; GATE 1 reviews the build/signing design.

## GATE-1 verification started

[Initial read-only verification](evidence/GATE-1/2026-09-17_initial-verification/REPORT.md) records G1-01..G1-11: official-platform comparison, observed manifest/intent/credential/cleartext/diagnostic surfaces, refreshed Kotlin advisory disposition and proposed credential/transport controls. Overall gate remains NOT_TESTED; findings are not implementation or runtime proof. No secrets or hardware accessed.
