# Component, intent, diagnostics and GPS review

Source manifest and existing merged debug manifest at `app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml` reviewed; APK hash matches preserved baseline. No new release build or binary manifest extraction. All six merged components are listed below; no app-declared receiver or runtime registerReceiver call was found in own source. Earlier source-only wording about launcher-only export did not cover merged libraries.

| Component | Export / filters / permission | Necessity and reachable behavior / disposition |
|---|---|---|
| dev.konraditurbe.osmosis.ui.MainActivity | true; MAIN + LAUNCHER; no component permission; singleTop | Required launcher and camera shortcuts. Also accepts unnecessary test-control extras; **B1 blocker**, described below |
| dev.konraditurbe.osmosis.ui.MediaPreviewActivity | false; no filter/permission | Internal media preview/group selection, network paths/host passed in extras. No direct external entry; target must validate paths, sizes, parent membership and camera context |
| dev.konraditurbe.osmosis.rsdk.GpsService | false; no filter/permission; type location | Internal opted-in GPS/camera telemetry, STOP action. Runtime location/FGS prerequisites still apply |
| androidx.core.content.FileProvider | false; no filter/component permission; grantUriPermissions=true | Explicit log-sharing URI capability. Authority dev.konraditurbe.osmosis.fileprovider; only external-cache shared_logs/ path, no broad root. Temporary read grant; content is not automatically sanitized |
| androidx.startup.InitializationProvider | false; no filter/permission | Merged AndroidX EmojiCompat/Lifecycle/ProfileInstaller initialization; internal only |
| androidx.profileinstaller.ProfileInstallReceiver | true; android.permission.DUMP; INSTALL_PROFILE, SKIP_FILE, SAVE_PROFILE, BENCHMARK_OPERATION (all androidx.profileinstaller.action namespace) | Merged AndroidX profiling/benchmark support. Privileged permission protects externally invoked operations; not an unprotected app endpoint. Accept for reviewed baseline, verify dependency/release merge on future updates |

Merged manifest also declares application-specific DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION with signature protection; it does not protect MainActivity. [AndroidX ProfileInstallReceiver](https://developer.android.com/reference/androidx/profileinstaller/ProfileInstallReceiver) describes the profiling receiver's external tooling role. Library export must be audited, not silently counted as non-exported.

## Incoming intents and B1

`MainActivity.kt:364-403` reads pin, nojoin, nowriterefresh, pageforce, pagesize, pageauto, autoscan, wifi/ssid/pass and offload/pick. The PIN value is interpolated into a log. pagesize accepts positive values without a visible upper bound in this branch. These branches have no visible caller/build-type restriction. MAIN/LAUNCHER filters do not prohibit an explicit external intent to an exported Activity. This establishes a sensitive attack surface, not a tested exploit, credential theft or remote-delete exploit.

Camera shortcut extras are also read onCreate and onNewIntent (428 onward); a trusted shortcut producer does not make arbitrary external extras trusted. Target: keep normal launch, bounded validated known-camera selection and internal commands; remove or isolate restricted diagnostic controls, never let intents create verification/cleanup authority or supply camera credentials/endpoints. B1 cannot close solely with a documentation promise. Security-only code authorization and SC09 tests are needed before G1 PASS.

Only own-source PendingIntent use found is GpsService261-263: explicit getService STOP, FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT. No mutable PendingIntent found. Future service actions require explicit target, immutable flags, operation identity and safe replay handling. FileProvider grants must remain exact URI/read-only, chooser initiated by user, no retained broad grants; sanitize before creating export file and remove expired bundles. No exported future coordinator/service/provider is justified by this review.

## Logging data-flow audit

| Field | Observed source behavior | Target disposition at every sink |
|---|---|---|
| Wi-Fi password | MainActivity credential getter logs length, not the normal returned secret. No ordinary literal password log established; raw/unknown protocol logging prevents a global no-leak assurance | Omit value and unnecessary length; no dump of credential frames/errors |
| Pairing PIN / token | External pin extra explicitly logged; protocol pairing responses can be hex-dumped | Never log supplied PIN, auth tokens, signing secrets or bearer material; omit unsafe packet classes |
| Bluetooth identifiers / camera names | GattClient logs device address; pairing/discovery and saved-camera UI use identifiers | Per-session opaque event IDs; no raw address/name in diagnostics |
| IP / network | ApJoiner/Duml and HTTP paths report network/session data, SSID at MainActivity1909 | Transport category/capabilities/reason code only; no SSID, MAC or unnecessary local address |
| GPS | GpsService standard messages show health/age/satellite counts rather than direct coordinates; raw unhandled protocol payloads preclude exhaustive absence claim | No coordinates/location traces or location-bearing raw data in any mode |
| Filename / path / URI | HttpClient and transfer messages expose paths; media manifest diagnostics contain filenames | Opaque asset ID and coarse counts/byte progress; no filenames/URIs/URLs in logs |
| Media metadata / content | CameraSession manifest hex/ASCII diagnostics expose serialized metadata; preview/content paths exist | No raw manifest, thumbnails, media bytes or metadata dump; size-limiting a dump does not sanitize it |
| Unhandled payload / exception | MainActivity1873/1888/1955 and RsdkController malformed/unhandled branches log payload material; raw exception messages can echo inputs | Typed reason codes, reviewed stack class/site IDs, no arbitrary exception message/byte array |

`core/FileLog.kt` accepts raw strings without central redaction, stores app-specific external files under logs/, retains newest five files by count without per-file byte/time limit. Save Logs is default OFF but preference restored on launch; disabling it does not disable ordinary Logcat from MainActivity/GpsService. Gzip share chooser adds no sanitization. No actual device logs, credentials or private payloads were read during this review.

Two-layer target (design decisions):

- Always-on structured events: typed field allowlist before Logcat/file/export; app-private ring, maximum 10 MiB and seven days (earliest limit wins). Keep reason confidence, state transition, anonymous session/asset IDs and bounded progress. No unsolicited upload. Logging failures/drop counts never change transfer truth; essential ledger transactions are separate from diagnostics.
- Temporary verbose: explicit opt-in, default OFF, auto-expire at session end or 30 minutes; maximum 10 MiB with rotation. App-private internal files, do not restore enabled state after restart. Same forbidden fields in every mode; unknown raw protocol payloads omitted. Debug verbosity is not permission to record secrets/GPS/media. Explicit preview of sanitized export categories and user share action; sanitized bundle only, bounded expiry/removal.

Backup must succeed with verbose OFF. SC05/GD tests inject synthetic canary secrets/coordinates/paths into all error paths and inspect every sink/export; never real user credentials. Baseline sinks are not silently declared compliant.

## Optional GPS on Android16

Current manifest has ACCESS_FINE_LOCATION but not COARSE or BACKGROUND_LOCATION; GpsService is location FGS with FOREGROUND_SERVICE/FOREGROUND_SERVICE_LOCATION and POST_NOTIFICATIONS. MainActivity640 starts optional GPS flow with fine permission and notification request; modern precise permission flow must request FINE and COARSE together and handle approximate selection. This dispositions the existing CoarseFineLocation lint finding as a future permission-correctness fix; original lint failure remains.

GPS_PERMISSION_MODEL: explicit GPS opt-in and foreground permission UI, coarse/fine disclosure, location-type FGS eligibility while visible plus actual Bluetooth permissions. If precision is insufficient for the optional feature, explain that limitation without blocking backup. BACKGROUND_LOCATION_REQUIRED: **no for continued location FGS started while eligible/visible**; arbitrary background creation is restricted and is not promised. No silent cross-session GPS restart or new background-location request justified here. [Runtime location guidance](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime) and [FGS start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).

Source location subscriptions are in GpsService (around165), reached via the GPS mode flow; backup alone does not call the location provider on target Android16. Legacy <=30 BLE scan separately requires location permission, not GPS telemetry intent. Current GpsService can outlive the Activity and returns START_NOT_STICKY; actual background continuation on this hardware was not tested. GpsSyncState mediates offload/BLE ownership, which is not proof of contention-free concurrency.

BACKUP_DEPENDENCY: none in target architecture; no GPS permission precondition for target36 backup. PRIVACY_IMPACT: opted-in phone location is transmitted to camera and may become recording metadata; diagnostics must never retain coordinates. Persist preference separately from current consent/permission/active-session state; a stored preference is not a permission grant. GPS OFF must release providers and telemetry without cancelling backup. SC06/GD01-GD02 test opt-in/out, precise/approximate/denied/revoked, Activity background and shared-BLE arbitration. Do not attribute foreground session loss to GPS without evidence.
