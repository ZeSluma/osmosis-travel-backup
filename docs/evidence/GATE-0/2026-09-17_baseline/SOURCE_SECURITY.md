# GATE-0 source and security baseline — 2026-09-17

Scope: unchanged application at `2fcdbc97e6dbefc875d425368be67cf32b50bb06`, inspected from governance-only branch tip `5582f3f71d66ddeb225ee59f96de03f94a19b3e0`. This is source analysis, not target-hardware verification or a security clearance. No findings were fixed.

## Source reconstruction

- `net/HttpClient.kt`: HTTP Range starts at the stored length; a resumed response must be 206. A 200 response becomes RANGE_IGNORED rather than being appended. Timeouts are 5 seconds connect / 20 seconds read; downloads request Connection: close. 404/500 return FAILED to the caller.
- `net/MediaDownloader.kt`: whole originals use MediaStore IS_PENDING=1 and persist the URI in `osmosis_dl` SharedPreferences under `dl_` plus camera path. Reopened file size supplies the resume offset. The retry loop backs off by 750 ms times consecutive barren attempts, stopping at five barren attempts or the MAX_RESUMES=20 guard. HTTP failures are retried. Failed partial downloads remain pending for a later attempt.
- Completion is weaker than project VERIFIED: clean EOF/DONE or local size >= expected remote size can clear IS_PENDING. No source checksum, exact-length equality, or Content-Range start validation was found in this path. The RANGE_IGNORED truncation happens after closing FileOutputStream on the shared descriptor and swallows truncation errors; descriptor/truncation behavior needs a targeted Android regression test. These are source-level concerns, not reproduced hardware failures.
- Completed-copy lookup matches display name, completed flag and a relative-path prefix, without byte-size/hash or camera identity. This supports skipping existing downloads across restarts but is not a robust verified backup ledger and can collide across same-named media.
- `ui/MediaPreviewActivity.kt:427` queries downloadedUri asynchronously and disables requeue of a saved whole file. `ui/MediaGridAdapter.kt` keeps its selection queue in memory. ROADMAP item 22 describes a stateless grid tick; that must not be generalized to all downloaded state. Persistent resume URIs and MediaStore-based completed-copy detection already exist.
- `ui/MainActivity.kt:1773` starts a bare Thread calling MediaDownloader with the Activity context and callbacks. No persistent job queue/download foreground service owns this work. The manifest foreground service is GPS-specific. Process death recovery remains a hardware requirement.
- `ble/CameraModel.kt:79,85` marks Pocket 4 (0x0021) and Pocket 4 Pro (0x0022) verified upstream, with pocket4p name matching before pocket4. README describes upstream hardware verification. Op4ManifestTest decodes a 45-record fixture, not a full Pocket 4P + S25 Ultra transfer/integrity test.
- Existing camera deletion is an explicit long-press action followed by confirmation (`MainActivity.confirmDelete`). No automatic deletion was identified in the reviewed download path. No deletion was exercised.

## Security observations and open findings

| ID | Observation | Disposition |
|---|---|---|
| SEC-01 | Global cleartextTrafficPermitted=true; HttpClient uses camera-local HTTP | GATE-1 scope/threat review; do not break camera access speculatively |
| SEC-02 | MainActivity exported as launcher; MediaPreviewActivity, GpsService and FileProvider non-exported; provider grants URI permissions only for shared_logs external cache path | Source inspected; runtime intent/provider testing not performed |
| SEC-03 | allowBackup=false; Bluetooth scan/connect (legacy Bluetooth capped at API 30), fine location, foreground service/location, notification, Wi-Fi/network, Internet and nearby Wi-Fi permissions | Permission rationale and runtime behavior still need target-device review |
| SEC-04 | Wi-Fi passwords saved in app-private ordinary SharedPreferences; no Keystore encryption in that path | GATE-1 credential-storage design finding; no credential values read or captured |
| SEC-05 | MainActivity logs camera SSID/MAC/name and password length; HttpClient/MediaDownloader log media names/paths; unknown protocol payloads may be hex-logged. FileLog appends supplied strings without central redaction | Privacy gap against INV-005; opt-in logs, five-file retention and comments forbidding credentials do not prove sanitization. No real device logs collected |
| SEC-06 | Actions use mutable tags; test workflow lacks explicit least-privilege permissions; release workflow grants contents:write and is tag-triggered | Supply-chain/permissions review pending. No workflow changes or release triggers |
| SEC-07 | Wrapper JAR is official Gradle 8.8 while distribution is 8.14.5; distributionSha256Sum absent, no dependency lock/verification metadata observed | JAR integrity PASS; distribution runtime launches. Repository-level artifact verification/pinning remains an improvement for later review |
| SEC-08 | OSV reports GHSA-r937-wjx7-w2jp / CVE-2026-53914 for Kotlin Gradle plugin 1.9.24: unsafe build-cache metadata deserialization | Open build-tool vulnerability; isolated fresh cache used, no shared remote build cache configured. Applicability/exploit prerequisites require follow-up; no upgrade authorized in this baseline |

Release signing is conditional on local keystore.properties and CI secrets; its absence does not block a debug baseline. No signing secrets were read, created, requested or assumed available. Fork signing remains a future release prerequisite. The manifest does not declare a download dataSync foreground service.

## Dependencies and scan limits

Declared runtime coordinates: androidx.core:core-ktx:1.9.0; androidx.activity:activity:1.9.3; androidx.appcompat:appcompat:1.6.1; com.google.android.material:material:1.10.0; androidx.recyclerview:recyclerview:1.3.2; org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4. Test: junit:junit:4.13.2. Build plugins: AGP 8.13.2 and Kotlin 1.9.24. Repositories: Google, Maven Central, Gradle Plugin Portal. No dependencies were added or upgraded.

OSV querybatch covered these nine declared coordinates. Eight returned no matching advisory; Kotlin returned SEC-08. This is not a full transitive or native-binary vulnerability scan, SBOM, or proof of safety. Results and advisory are persisted separately.

Selected private-key, AWS key, GitHub token and Google API-key signature scans of tracked text files found no matches; selected reachable-history pickaxe scans also found none. No tracked keystore, .env or packet-capture filenames were found. This limited scan excludes binary contents and is not an exhaustive secret audit. Historical fixture data may contain media metadata and should not be republished in diagnostic reports. No matched secret values were emitted.

Sources: [OSV advisory](https://api.osv.dev/v1/vulns/GHSA-r937-wjx7-w2jp), [JetBrains security issues](https://www.jetbrains.com/privacy-security/issues-fixed), [Gradle checksum catalog](https://gradle.org/release-checksums/). Source references above are relative to `app/src/main/java/dev/konraditurbe/osmosis/` unless otherwise stated.

## Required hardware evidence — NOT_TESTED

On actual Samsung Galaxy S25 Ultra + DJI Osmo Pocket 4P record Android/app/camera firmware versions, install/launch, pairing and camera Wi-Fi connection, complete browse/enumeration, original download and independent size/integrity assessment, large file/batch, interrupted HTTP/Wi-Fi resume, and app/process restart. Record failed/partial state and repeated-run behavior. Keep filenames, MACs, passwords, GPS and media contents out of shared logs. Upstream labels, fixtures and desktop unit tests cannot close this criterion.
