# Baseline Reconstruction

## 2026-09-17 reconciliation

Final hardware evidence now completes GATE-0 as PASS for unchanged-baseline reproduction: [hardware report](evidence/GATE-0/2026-09-17_hardware/REPORT.md). Manual range resume, file persistence and MediaStore persistence passed; automatic reconnect, automatic resume and observed UI downloaded-state recognition after restart failed and remain baseline debt. Camera sleep is separate and not verified. The earlier historical hardware-pending statements below are superseded by this measured result; source lookup capability does not establish successful UI recognition.

The [unchanged baseline run](evidence/GATE-0/2026-09-17_baseline/REPORT.md) now records a successful debug build, Windows golden-test failures and lint failures. Source confirms persisted resume URIs and MediaStore-based completed-copy detection; the historical stateless grid-tick roadmap observation below must not be generalized to all downloaded state. No durable project backup ledger exists. Hardware remains NOT_TESTED.

Current fork and upstream main both match the historical candidate. Local governance initialization and refreshed observations are recorded in [the bootstrap report](evidence/GATE-0/2026-09-17_bootstrap/REPORT.md). The historical account below is preserved as history; its missing-governance statement describes the pre-bootstrap state. No build or hardware success is inferred from repository inspection.

## Purpose

This is a handoff of facts observed read-only on 2026-09-16 before project-specific repository initialization.

Every item must be re-verified before it is used as current truth.

## Repository

Observed:

- fork: `ZeSluma/osmosis-travel-backup`
- repository was public
- fork relationship pointed to `KonradIT/osmosis`
- default branch: `main`
- observed main commit:
  `2fcdbc97e6dbefc875d425368be67cf32b50bb06`
- no project-specific bootstrap branch was successfully created during the ChatGPT connector attempts
- no repository mutation was confirmed from those attempts

## Upstream

Observed:

- upstream: `KonradIT/osmosis`
- upstream is not writable by the user in the project workflow
- latest observed release: `v1.4.4`
- observed main was ahead of the latest release commit

## App/build baseline

Observed from the repository:

- compileSdk 36
- minSdk 29
- targetSdk 36
- Java/Kotlin JVM 21
- versionCode 29
- versionName 1.4.4

Observed dependencies included:

- `androidx.core:core-ktx:1.9.0`
- `androidx.activity:activity:1.9.3`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.10.0`
- `androidx.recyclerview:recyclerview:1.3.2`
- `kotlinx-coroutines-android:1.6.4`
- `junit:4.13.2`

Do not assume these versions remain current.

## Existing capability relevant to this project

Observed:

- upstream README identifies Pocket 4 / 4 Pro as hardware-verified
- existing download path has resumable/range capability
- roadmap notes long downloads currently run from an Activity-started bare Thread
- roadmap notes process death can therefore kill a long transfer
- roadmap notes current downloaded-media indication/state is not persistent across restart

These observations support, but do not by themselves prove, the need for a persistent project ledger and lifecycle-independent orchestration.

## Workflows

Observed test workflow:

- runs on branches/PRs
- validates Gradle wrapper
- sets JDK 21
- runs `./gradlew testDebugUnitTest`
- uploads test report

Observed release workflow:

- tag-triggered
- requires signing secrets such as keystore/store/key credentials
- builds release/debug artifacts
- creates a draft release

Do not assume upstream signing secrets are available in the fork.

## Android/security observations

Observed permissions/capabilities included:

- Bluetooth scan/connect
- fine location
- foreground service/location
- notifications
- Wi-Fi state/change
- network state/change
- Internet
- nearby Wi-Fi devices

Observed:

- `android:allowBackup="false"`
- `MainActivity` exported
- observed media preview activity not exported
- observed GPS service not exported
- observed FileProvider not exported

Observed network security configuration globally permitted cleartext traffic to support camera HTTP.

This requires a GATE 1 scope review; it is not automatically classified as a defect.

## Governance files

At the time of reconstruction, these project files were not observed on `main`:

- `AGENTS.md`
- `PROJECT_STATE.yaml`
- `docs/REQUIREMENTS.md`

The upstream `docs/` directory already contained upstream documentation and must not be overwritten blindly.

## Upstream open work

A small set of upstream issues/PRs was observed. None was identified as a direct blocker for Pocket 4P backup at that time.

Always refresh current upstream issues/PRs before relying on that statement.

## Connector incident

Multiple attempts to create `bootstrap/project-initialization` through the normal ChatGPT GitHub connector failed.

Two classes of behavior were observed:

1. GitHub returned `403 Resource not accessible by integration` for branch creation.
2. After reconnect attempts, ChatGPT plugin/session state became inconsistent (`connected` UI vs runtime `installed=false` / missing GitHub namespace).

No confirmed repository mutation occurred.

Operational conclusion for this project:
use a verified write-capable engineering environment for repository mutation; do not burn project time repeatedly reinstalling the normal ChatGPT GitHub connector.
