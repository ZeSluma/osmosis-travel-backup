# Security

Current execution policy (2026-09-17): GATE-2 implementation is authorized; AGENTS.md now permits continuous progression through sufficiently defined non-destructive gates after dependency/evidence checks. Earlier statements requiring a new permission solely at a gate boundary are historical and superseded. Hardware remains paused; no main mutation, merge, release or real deletion is authorized. Current implementation/evidence status is PROJECT_STATE.yaml and the active GATE-2 plan.

## Intent rebase threat/disposition review — 2026-09-17

GATE-1 PASS after the narrowly authorized B1/B2 fixes and verification in [ADR0008](decisions/0008-b1-b2-security-remediation.md) and [implementation evidence](evidence/GATE-1/2026-09-17_B1-B2/REPORT.md). External launcher test commands are removed; saved-camera shortcut hints require validation and user confirmation. Kotlin2.4.20 is actually resolved and tested. Broader credential/cleartext/logging architecture remains deferred to its assigned gates; no blanket production-security or release approval.

| Boundary / threat | Required disposition before implementation/release acceptance |
|---|---|
| Camera credentials in ordinary private prefs | G2 implements per-camera Keystore AES-GCM blobs, crash-safe lazy migration and explicit backup/D2D exclusions; scoped synthetic SC01/SC02 checks pass. Actual S25 key/OEM behavior remains NOT_TESTED; see ADR0009. |
| Local cleartext and hostile camera/LAN input | Selected default-deny with finite exact-IP exception plus per-Network host/port/path and redirect guards; XML cannot enforce interface/CIDR/port. AOSP supports exact literal matching; target-client enforcement remains SC03/N tests |
| Non-exported execution host, launcher/debug extras, intents/provider | Inventory exported components and existing test hooks; explicit immutable notification intents, input validation and least privilege; never let arbitrary intent claim verification/trigger delete |
| Logs/crash reports/media metadata | Typed event allowlist, reason confidence, no raw packet/exception/URL/credential/GPS/content; test secrets injected at all error paths. Disable unsolicited telemetry; export only sanitized bundle |
| Ledger and filesystem mutation/crash | Canonical transactional truth, fenced writers, no adopt-by-name, cross-system journal, no destructive migrations; valid replica cannot be overwritten by partial |
| Kotlin advisory / new Room toolchain | B2 CLOSED: actual KGP2.4.20 resolution, successful compilation and273 LF unit tests; five baseline lint errors remain, no new errors. G2 adds reviewed Room2.8.5/KSP2.3.12 with resolved Kotlin2.4.20 and313 LF tests; expanded build-tool advisory debt is documented separately |
| CI/signing | Mutable action/cache trust and artifact verification review, least-privilege tokens, no fork reuse of upstream signing, fork release signing separate; no secrets printed/requested/rotated |
| Cloud/SSD | Separate tokens/SAF grants and replica verification, cloud TLS independent of cleartext camera, no routing leakage; local success independent of remote services |
| Platform permission changes | Consent/denial/revocation distinct from camera authentication; future target37 local-network migration tested, not premature current manifest addition |

Existing no-auto-delete and informational-only safe-clear invariants remain absolute. Unknown media types are not disposable. Rebase documentation does not authorize hashing/copying private media or conducting new hardware tests during pause.

## Accepted safety / GPS / diagnostics reconciliation

ADR0005 adopts default phone plus one independently verified SSD/cloud storage domain, not phone-only safety. Replica availability and proof are revalidated before explicit cleanup. [Cleanup design](design/VERIFIED_SNAPSHOT_CLEANUP.md) requires exact immutable scope, non-forgeable internal confirmation binding, exclusive/fenced writer, strong camera/storage/object identity, known collateral effects and exhaustive post-verification. External intents, replayed UI actions, sync callbacks and retry jobs must not authorize deletion. No format fallback. Existing upstream delete entry points are baseline code, not evidence of policy enforcement; accepted dedicated later GATE-9 must cover every destructive path before real use. ADR0006 binds confirmation to source/replica proof versions and safety generation; new evidence immediately revokes safety, material changes revoke authorization. Narrow same-live-operation continuation is not a background initiation right.

GPS telemetry is explicit opt-in; backup never initiates location collection. Review permission and location-FGS/background implications before deciding cross-session GPS auto-resumption. BLE ownership arbitration must prevent telemetry/offload contention without falsely diagnosing the existing foreground drop as GPS-caused.

[GPS_AND_DIAGNOSTICS](design/GPS_AND_DIAGNOSTICS.md) applies the no-secret/GPS/media/PII rule to normal events, logcat, verbose files and exports. Normal event recording is bounded and independent of verbose mode. Verbose is OFF by default, temporary, app-private and explicitly exported only after sanitization; omit unsafe raw fields, never automatically upload. No GPS-coordinate diagnostic exception is enabled by this review. Baseline FileLog has no central redaction or per-file byte/time bound, and its share path is not a sanitized-export guarantee; the completed closure review selects the all-sink controls but their enforcement remains untested (SC05).

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

[Initial read-only verification](evidence/GATE-1/2026-09-17_initial-verification/REPORT.md) and [closure review](evidence/GATE-1/2026-09-17_security-closure/REPORT.md) are historical; [B1/B2 evidence](evidence/GATE-1/2026-09-17_B1-B2/REPORT.md) closes their blockers. Both current merged variants contain six components, with the AndroidX profiling receiver protected by DUMP. Scoped emulator checks do not constitute S25/Pocket runtime proof. Broader bounded diagnostics, GPS separation, ledger classification and future controls retain their reviewed designs and deferred tests.

## GATE-2 scoped verification

[ADR0009](decisions/0009-gate2-foundation-and-continuous-execution.md) implements the ledger/credential/backup foundation and records scoped emulator tests. B1/B2 remain verified. Expanded resolved-coordinate review identifies44 advisories on11 unchanged G1 build-only coordinates; see the G2 report for scope and deferred CI/toolchain remediation. This is not a blanket all-history/runtime security certificate. Real hardware/Keystore/OEM D2D and broader all-sink logging/network controls remain unverified at their assigned boundaries.
