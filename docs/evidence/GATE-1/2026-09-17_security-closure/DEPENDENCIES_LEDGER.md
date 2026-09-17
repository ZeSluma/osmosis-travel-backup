# Build security and ledger classification

## Kotlin advisory disposition

Current build.gradle pins kotlin-gradle-plugin1.9.24; AGP8.13.2, wrapper distribution8.14.5, app JVM21/target36. [GHSA-r937-wjx7-w2jp / CVE-2026-53914](https://github.com/advisories/GHSA-r937-wjx7-w2jp) marks versions below2.4.20-Beta1 affected. The [JetBrains fix](https://github.com/JetBrains/kotlin/commit/bf51df665b458fda7c3eaf436c4d88dc119d7ec6) constrains KAPT cache deserialization. Classification remains APPLICABLE_REDUCED_EXPOSURE, not NOT_APPLICABLE; no exploitation shown.

No KAPT/plugin processor or remote buildCache/org.gradle.caching setting found in repository. Historical isolated GATE-0 execution reported task cache disabled and no user/init overrides. Fresh default user Gradle directory check found no directory; a guessed temporary gradle-home path also did not exist, so it does not independently attest the actual historical isolated cache. No new build or live daemon attestation; future invocation/global overrides cannot be ruled out. Ordinary dependency downloads/Kotlin incremental data are not the same as enabled Gradle task-output cache. CI explicitly restores Gradle caches through setup-java; no remote task-output cache endpoint configured. Cache poisoning still deserves trust review; absence of KAPT reduces the particular reviewed path in current single-user baseline, not the package's vulnerability status.

Decision: **MUST_FIX_BEFORE_IMPLEMENTATION (B2)**. Before new ledger/processor development, use a patched compatible compiler/plugin and trusted caches. Do not introduce KAPT, shared untrusted build metadata or privileged secret-bearing builds under this temporary baseline. No dependency change authorized now.

Fixed candidate: stable **2.4.20** (released September7,2026 in [Kotlin releases](https://kotlinlang.org/docs/releases.html)); the advisory's first fixed beta is not the selected production candidate. [KGP compatibility](https://kotlinlang.org/docs/gradle-configure-project.html) lists2.4.20 with Gradle7.6.3-9.7.0 and AGP8.5.2-9.3.1, encompassing current8.14.5/8.13.2. Current1.9.24's fully supported ranges stop earlier than this baseline's Gradle/AGP. Table compatibility is not a passing build: authorize a separate upgrade, check K2 language/compiler behavior, JVM21 bytecode, old coroutines/AndroidX, unit/lint regressions and later Room/KSP choice. No build or processor change tested here.

## Direct coordinates and fresh lookup

On2026-09-17 a read-only POST to `https://api.osv.dev/v1/querybatch` sent only Maven package names/versions, no repo/private data. Query returned the Kotlin advisory only among these nine coordinates. Blank means no matched record in this lookup, not vulnerability-free or transitive coverage.

| Coordinate | Matched advisory |
|---|---|
| androidx.core:core-ktx:1.9.0 | none returned |
| androidx.activity:activity:1.9.3 | none returned |
| androidx.appcompat:appcompat:1.6.1 | none returned |
| com.google.android.material:material:1.10.0 | none returned |
| androidx.recyclerview:recyclerview:1.3.2 | none returned |
| org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4 | none returned |
| junit:junit:4.13.2 (test) | none returned |
| com.android.tools.build:gradle:8.13.2 | none returned |
| org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24 | GHSA-r937-wjx7-w2jp |

Direct versions are fixed literals; repositories are Google/Maven Central/Gradle Plugin Portal with FAIL_ON_PROJECT_REPOS. A resolved transitive SBOM, license review and artifact-verification policy are still required before release/new dependencies; this lookup is not a full dependency scan. No Room/encryption library added.

## CI, wrapper, signing and secrets

- Both workflows use mutable major action tags: checkout@v4, setup-java@v4, wrapper-validation@v3 and upload-artifact@v4; release additionally softprops/action-gh-release@v2. Target policy: reviewed full commit-SHA pins, tracked update review, minimal permissions and no untrusted cache promotion into privileged builds. No workflow edit in this task.
- Unit workflow runs push/pull_request, no explicit permissions block (effective default cannot be inferred from YAML alone). Release is tag-triggered contents:write, decodes signing secrets and creates draft release. No pull_request_target found. Target separate read-only validation from protected signing, read-only PR cache policy, trusted namespace/key provenance, no signing material in artifacts/caches/logs. setup-java dependency caching must not be described as no-cache CI.
- Current wrapper JAR SHA256 `cb0da6751c2b753a16ac168bb354870ebb1e162e9083f116729cec9c781156b8` freshly matches [official Gradle8.8 wrapper checksum](https://services.gradle.org/distributions/gradle-8.8-wrapper.jar.sha256). The JAR version and distribution8.14.5 are distinct. HTTPS distribution URL/validation exist; distributionSha256Sum is absent. Before release/build hardening add a verified distribution checksum and reviewed dependency verification under separate authorization. Hash match does not authenticate all downloaded dependencies.
- Fork signing is not established and remains release-blocking only. Never reuse/assume upstream key, never print/create/request signing secrets. Current source can emit unsigned release when no keystore.properties; a successful build would not establish release identity.
- Historical GATE-0 redacted Gitleaks8.30.1 reachable-history scan found zero leaks, scoped to then-scanned commits. No new full-history scan ran. Subsequent governance commits and this text contain no intentional secrets; this does not certify unreachable Git objects, ignored files, private device data or external account settings. Public protocol constants are not evidence of a leaked personal credential. No secret rotation/access performed.

## Ledger data classification and necessity

| Data | Sensitivity / minimum persisted use |
|---|---|
| Camera identifier | Private persistent identifier; opaque internal ID plus minimum strong source identity/provenance; raw address only where reconnection actually requires it, not logs |
| Remote identifiers / recording-member relations | Private inventory; version/storage epoch and required companion relationships for correct matching/completeness; no serialized full protocol dump |
| Filenames | Potentially identifying/user content; exact bounded name only for source matching/destination, no diagnostics/export by default |
| Capture timestamp/date | Private travel/activity metadata; resolved timestamp, zone/offset or uncertainty, provenance and conflict state required for R037; minimize unrelated EXIF |
| Byte sizes | Operational metadata; exact transfer/integrity bounds, not proof of equality alone |
| Local paths/URIs | Private capability/location metadata; stable relative path and minimum persisted destination URI/reference; not credentials and not automatically valid permission after restore |
| Hashes | Sensitive content fingerprints; algorithm/assurance/source version needed for integrity, never claim local hash equals source without source proof; exclude ordinary logs |
| Replica state | Private operational records; independent storage domain, versioned verification proof, policy state and validity generation |
| Retry/error | Attempts/budget/next eligibility and allowlisted reason/confidence; no raw exceptions, URLs, payloads or credential strings |
| Deletion audit | High-integrity private operation record; exact scope/proof generation/authorization lifetime, per-asset intended/confirmed/unknown result and minimal times; never stores authority that survives expiry/process loss contrary to ADR0006 |

Must not store Wi-Fi/cloud tokens, GPS coordinates, media bytes, thumbnails, raw packets or arbitrary diagnostic strings in ledger. Associated required sidecars can themselves be backup assets outside the ledger; their private contents never become diagnostics. Preserve asset relationships and frozen capture-day path so a recording is not complete with missing required audio/DNG/metadata. Unknown potentially required assets remain unresolved or conservatively preserved under six-class policy; no automatic discard.

DB encryption decision: **no additional DB-encryption dependency selected**. Threat model is other ordinary apps/off-device unintended backup; app sandbox, OS file encryption, locked device and explicit no-backup isolation address that boundary without a new crypto stack. Unlocked rooted/process compromise and forensic threat beyond OS protection remain residual risks; app-held DB key would not eliminate them. If a stronger offline extraction threat is adopted, revisit encrypted DB with measured WAL/journal/restore behavior before choosing a library. Secrets already require separate Keystore-backed store. Public phone media is intentionally outside this protection claim.

Keep DB and auxiliary WAL/SHM/journals out of cloud and device transfer via appropriate storage/exclusion rules. A restored stale ledger cannot attest current source/replica state or carry deletion authority. Keep required operational/audit records while their referenced assets/operations need reconciliation; prune superseded diagnostic/retry detail by bounded retention after completion, retain minimal proof lineage, allow intentional local history removal with explicit loss-of-recognition warning. No automatic media deletion follows ledger pruning.

Migration/integrity: versioned Room schema proposal, uniqueness/foreign keys, transactions, single fenced writer, explicit migrations and exported synthetic schema fixtures; prohibit destructive fallback. Test every supported migration chain, disk-full/corruption/process interruption, and DB/MediaStore publication journal. Corrupt/untrusted state becomes RECOVERY_REQUIRED with verification/cleanup unavailable; never silently reconstruct VERIFIED from filename or filesize. No DB implemented, migrated or runtime-tested here. [Room migration guidance](https://developer.android.com/training/data-storage/room/migrating-db-versions) informs SC08/G2/G3 requirements.
