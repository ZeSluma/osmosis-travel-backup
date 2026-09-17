# Security

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
