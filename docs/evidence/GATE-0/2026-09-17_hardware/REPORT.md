# GATE-0 target-hardware verification

Final status: GATE-0 PASS for unchanged-baseline reproducibility with disclosed limitations. TEST A-D and manual range resume passed. TEST F file/MediaStore persistence passed; actual UI recognition failed. Automatic reconnect/resume failures and the separate provisional camera-sleep finding remain future-gate work. Chronological pending/blocked statements below describe their observation times and are superseded by the final disposition at the end. GATE-1 is NOT_TESTED; implementation and release remain unauthorized.

## Artifact and source identity

- Baseline: `2fcdbc97e6dbefc875d425368be67cf32b50bb06`.
- Governance branch: `bootstrap/project-initialization`, preparation tip `0a848e536004f4f03a51254452f6bf431a956d8f`.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, the existing unchanged-baseline build artifact.
- SHA256: `58e5cfde7e8398660db997dd48f69e37eef1611311130480fb98a03762ab1a3b`.
- Application ID: `dev.konraditurbe.osmosis`; versionName `1.4.4`; versionCode `29`.
- Launcher: `dev.konraditurbe.osmosis.ui.MainActivity`.
- Current APK hash matches the original baseline build evidence. Application/build/wrapper/workflow diff from baseline is empty. This establishes provenance through the recorded build and artifact hash, not an embedded Git identity in the APK.
- `aapt dump badging` independently confirms the APK package/version/launcher.
- ADB 1.0.41, platform-tools 37.0.1-15733141, available in the isolated SDK.
- Initial sandbox ADB invocation could not create its Android user directory; normal-user invocation succeeds. Initial `adb devices -l`: zero devices. Serial identifiers are not persisted.
- Live fork and upstream main both remain at the baseline SHA. Fork open work: none; upstream open items: 40, 32, 24, 11, 8.

## Evidence and privacy procedure

No raw logcat, bugreport, packet capture, screenshot, media payload, shared-preference dump or full device dump will be persisted. Use only explicitly selected numeric/status fields and manually confirmed observations. Identify test files as TEST-D and TEST-E; retain only approved non-sensitive filename metadata if necessary, byte sizes, offsets, durations and completion state. Omit device serials, SSIDs, MACs, credentials, tokens and GPS.

Before connecting the camera, ensure Save logs and GPS Sync are OFF. Baseline still writes logcat internally; disabling Save logs does not remove that debt. Do not export raw logs. If needed, inspect narrowly scoped output in memory and emit only allowlisted event labels and numeric counters; never write intermediate raw output to disk. Existing baseline privacy limitations remain documented, not fixed or waived.

Check for an existing installed package before installation. Never uninstall or clear app data to resolve a signing conflict. Stop if installation would require destructive/ambiguous handling. No camera delete, format, trim or existing-original alteration actions are permitted. The user subsequently authorized creating a new non-critical test recording if no sufficiently large existing file is available; retain it after the test.

## Physical sequence and results

| Test | Required observation | Status |
|---|---|---|
| A | Authorized SM-S938B detected; verified APK installed; process alive after 10 seconds; no app crash marker observed; user confirms camera-selection screen visible | PASS |
| B | User observes Osmo Pocket 4 Pro and Connected · WiFi | PASS (user-observed) |
| C | User observes internal storage status and media grid containing one video | PASS (user-observed enumeration; complete inventory not independently checked) |
| D | User confirms saved file corresponds to selected original; repeated ADB filename/size check consistent at 38,447,651 bytes | PASS for normal-download baseline; no checksum-integrity claim |
| E | Retained 374,356,968-byte partial resumes manually to 3,071,380,142 bytes; automatic reconnect/resume fail | Manual range resume PASS; automation FAIL |
| F | Completed file and MediaStore persist; UI does not visibly recognize completed original after restart/manual connection | File/MediaStore PASS; UI recognition FAIL |

For E, keep USB connected. Choose the exact interruption point after observing progress; interrupt phone Wi-Fi, not camera storage/power. Record whether resume is observed directly or inferred. A progress indicator alone does not prove an HTTP Range response. If the unchanged app exposes no safe numeric Range evidence, record that limitation and do not fabricate it.

## TEST A observations — 2026-09-17

One authorized USB device detected. Manufacturer samsung, model SM-S938B (target S25 Ultra), Android 16 / API 36. Device serial omitted. No existing `dev.konraditurbe.osmosis` installation was present.

APK hash rechecked before installation and unchanged. `adb -d install <verified-apk>` returned Success / exit 0 in 3.07 seconds. `adb -d shell am start -W -n dev.konraditurbe.osmosis/dev.konraditurbe.osmosis.ui.MainActivity` returned Status ok / exit 0, cold launch, TotalTime 393 ms. Launch output identified Android permission-controller activity. After 10 seconds `pidof` confirmed the app remained alive; activity inspection found a resumed app activity. Installed version independently confirmed as 1.4.4 / 29. A crash-buffer check at 09:21:38 UTC found no matching app Process crash marker; raw buffer was held only in memory and not persisted. This is a bounded startup observation, not proof of crash-free operation indefinitely.

Window-focus filtering did not identify either an app window or permission prompt, so current visible-screen state is not inferred from that check. Physical confirmation is required to resolve the launch permission flow.

User subsequently confirmed the Osmosis camera-selection screen is visible. TEST A is PASS for the observed install/launch scope.

## TEST B/C observations

User reports Osmo Pocket 4 Pro, Connected · WiFi, internal storage status, and a media grid with one video. This supports connection and enumeration of that item; it does not establish exhaustive camera inventory or successful original transfer. No screenshot, camera identifier, password or raw log collected.

TEST D candidate: existing visible video, approximately 38 MB by user report, alias TEST-D. Exact source byte count not yet established. User authorized normal original download. Use Add to Queue without trim, return to grid and Download (1). No sidecars or camera media mutations are needed.

## Runtime observation before TEST D

After previously confirmed connection and a visible media grid, the user briefly switched away from Osmosis. On return, the app reported a camera connection failure and suggested the saved Wi-Fi password might be outdated. Camera name omitted from evidence. The user confirms the camera/password had not been changed or reset.

Classification: observed background/return connection failure; root cause UNCONFIRMED. Stale credentials are an app suggestion, not a verified diagnosis. No password reset, credential change, source fix or baseline-debt fix was performed. Background transition duration and underlying network callbacks were not captured. Prior TEST B/C success remains valid for its observed scope; this additional failure must not be hidden by a successful retry.

At the time of this observation, recovery and TEST D were pending. TEST D subsequently passed as documented below. No interrupted-transfer or resume result may be inferred from this separate incident.

User disposition: preserve this as a separate background/runtime finding for later architecture work, not TEST E. No active transfer was established during the observed background failure; it motivates a background-transfer lifecycle/network requirement, not a claim of measured background data loss. GATE-1 architecture review must address reliable connection ownership and reconnect after leaving/returning, without falsely diagnosing unchanged credentials as stale. Root cause remains unconfirmed. Gate remains BLOCKED for the remaining hardware tests; this deferred architecture finding is not independently treated as a foreground TEST E failure.

Pre-download metadata-only ADB check: `/sdcard/Movies/Osmosis` did not exist, exit 0; no media names or content collected.

## TEST D metadata verification — 09:35:01 UTC

User reports the download appears to have completed but did not catch the final status message. At the user's explicit request, ADB queried only directory existence, filenames and byte sizes, without copying, opening, hashing or displaying media contents. No additional download was started.

Command: `adb -d shell 'if [ -d /sdcard/Movies/Osmosis ]; then echo DIRECTORY_EXISTS; find /sdcard/Movies/Osmosis -maxdepth 1 -type f -exec stat -c "%n|%s" {} \; ; else echo DIRECTORY_ABSENT; fi'`. Exit 0.

- Directory now exists; it was absent in the pre-download check.
- Exactly one regular file returned: `DJI_20260916143329_0001_D.MP4` (TEST-D; filename metadata explicitly requested by user).
- Byte size: 38,447,651 (38.447651 decimal MB; approximately 36.67 MiB).
- Size is plausible for the approximately 38 MB camera-grid value. Exact remote size was not captured, so equality/integrity is not proven. Final saved/skipped/failed counters were not observed.
- A new destination file is confirmed. Overall TEST D completion remains partially verified; do not infer full success solely from a plausible size. This also does not establish the exact recovery sequence or resolve the preceding background-reconnect finding.

### TEST D confirmation — 09:35:40 UTC

User independently confirms the destination folder, saved file in phone internal storage, and correspondence to the selected camera video. Repeated identical metadata-only ADB command returned exit 0, the same single filename and exactly 38,447,651 bytes. TEST D is PASS for the normal-download baseline on this combined manual/ADB evidence. Missing final counters and absence of exact source-size/hash comparison remain explicit limitations; no cryptographic integrity or project VERIFIED-state claim is made.

## TEST E preparation (not started)

Select a non-critical original substantially larger than TEST-D, preferably at least 1 GB; 2 GB is ideal but not mandatory. Only one approximately 38 MB video has been reported visible so far; no suitable candidate has yet been identified. If no sufficiently large existing file is available, the user authorizes making a new non-critical test recording. Do not modify/delete existing originals or delete the new test recording afterward. Confirm the candidate is visible in Osmosis and record its displayed size before starting any download.

Interruption procedure: after candidate confirmation, start a normal full download and keep Osmosis foreground. Once at least 100 MB is observed transferred, instruct the user to turn phone Wi-Fi OFF using the Quick Settings overlay and immediately dismiss the overlay back to Osmosis, without switching to another app or Settings activity. Keep USB connected and camera powered on. Record interruption, last progress, numeric partial size and pending/completed state where observable. Keep Wi-Fi OFF until explicitly instructed after evidence capture; then reconnect with unchanged credentials and observe automatic or manual resume. Never reset credentials or delete the partial destination. Verify positive offset versus byte-zero restart and whether the partial item is incorrectly presented as complete. Successful positive-offset transfer plus source handling may support resume inference; explicit HTTP 206 is not logged on the successful path, so distinguish observation from inference. Any overlay/lifecycle effect must be described separately from the earlier background incident.

User confirms a large test video is visible in Osmosis with displayed size 3.1 GB; download not yet started at confirmation. This is the accepted TEST-E candidate. Exact source bytes and whether this was existing/newly recorded have not been established. The displayed size is sufficient for the controlled interruption. No recording duration/size guarantee is assumed.

Next physical action: queue this full original without trimming or sidecars, start Download (1), and turn phone Wi-Fi off at the first visible transferred count of at least 100 MB, before completion. The threshold instruction is supplied in advance to avoid chat latency allowing the transfer to finish. Keep USB connected/camera on and return immediately from Quick Settings to Osmosis; leave Wi-Fi off until instructed after inspection. Report observed last progress and app state. If the transfer completes before interruption, record that outcome and do not manufacture an interrupted result.

Implementation authorized: false. Release allowed: false. No evidence/state commit yet: collect actual test results first, then make the single authorized commit.

### TEST E interruption observations

User reported approximately 140 MB transferred of 3.1 GB. Assistant instructed phone Wi-Fi OFF; user confirmed it was off. At 09:48:44 UTC, ADB directory stat returned exit 0 and sizes 374,356,968 bytes (new TEST-E candidate partial) and 38,447,651 bytes (completed TEST-D). Difference from user-observed 140 MB can reflect progress between observation and interruption; exact interruption offset is not independently timestamped. The new file is substantially below the displayed 3.1 GB total.

Initial MediaStore pending-state query had a shell argument error; its nominal exit 0 is not success. A corrected read-only query at 09:49:07 UTC failed with ADB no devices found (exit 1). Pending flag and current completion presentation remain UNVERIFIED. No false-complete PASS or failure is inferred from missing metadata.

Allowlisted log scan returned one DONE saved=1/skipped=0/failed=0 event without an isolated TEST-E time boundary; it cannot be attributed to TEST E and may be TEST D. No raw logs persisted. No resume has yet been observed or instructed. Wi-Fi remains user-reported OFF; subsequent ADB confirmation was unavailable. Restore USB observation before reconnection, keep Wi-Fi off, and record visible app status.

### TEST E interruption confirmed — 09:51–09:52 UTC

User confirms USB reconnected, Wi-Fi still off, camera still on, app neither restarted nor download retried. ADB independently reports wifi_on=0 and app process alive. The candidate partial remains exactly 374,356,968 bytes, unchanged from 09:48:44; completed TEST-D remains 38,447,651 bytes.

Sanitized app events now show Wi-Fi loss, retries at 374 MB (attempts 2–6, delays 750/750/1500/2250/3000 ms), PAUSED at 374 MB, then DONE saved=0 skipped=0 failed=1. This confirms the interrupted job was reported failed/paused rather than saved. The earlier background incident remains separate.

Default MediaStore query and includePending=1 query return only TEST-D with is_pending=0. TEST-E is not returned as completed by these queries. Direct app-identity metadata query is rejected with SecurityException despite shell exit 0; do not interpret as successful. Therefore TEST-E's exact is_pending flag is not directly established. The combination of stable partial size, absence from returned completed media, and failed/paused app counters supports safe interruption behavior at this observation point, with the pending-flag limitation explicit.

Next physical action: turn phone Wi-Fi ON, return to Osmosis and reconnect to Pocket 4P with unchanged credentials if needed. Do not tap Download yet: first observe whether automatic resume starts. Subsequent evidence must establish positive-offset continuation versus restart; interruption alone does not prove resume.

### Separate TEST E runtime findings and manual-retry preparation

User confirms Wi-Fi restored and camera available, but no automatic reconnection; manual interaction and several attempts were needed to restore connection. **AUTO_RECONNECT: FAIL** (user-observed). Requirement: safely and automatically attempt reconnection to the previously known available Pocket 4P after a temporary network interruption without repeated user interaction.

After manual connection restoration, the interrupted active 3.1 GB job did not automatically resume. **AUTO_RESUME: FAIL** (user-observed). Requirement: automatically continue an interrupted active backup job after successful reconnection whenever safe. Both requirements are recorded for later architecture/implementation gates; no GATE-0 functional fix is authorized. These are separate from the earlier brief-background/return observation.

**MANUAL_RANGE_RESUME: NOT_VERIFIED.** No manual retry has yet been instructed. At 09:56:52 UTC, metadata-only ADB stat confirms the partial remains exactly 374,356,968 bytes; TEST-D remains 38,447,651 bytes. Completed-media query returns only TEST-D with is_pending=0; TEST-E is not reported as completed there. Earlier job result was paused/failed, not saved. Exact private pending flag remains inaccessible.

Current job state: interrupted and inactive according to user report; persistent partial retained. Current queue selection/count is NOT_VERIFIED. Attempted in-memory UI inspection returned no usable hierarchy, so no queue state is inferred. Obtain the visible Download/Retry label and enabled state from the user before instructing any retry or requeue. Do not delete, truncate or substitute the existing partial. On retry, sample destination byte sizes and record allowlisted resume offsets, distinguishing partial growth from a byte-zero restart. Gate closure explicitly withheld.

User queue observation: button label is German "Herunterladen", appears enabled (bright/turquoise), no (1) count and no visible selection marker on the 3.1 GB item. Thus the item appears no longer queued after reconnection; this is a user-observed UI state, not introspection of an internal job object. Baseline onDownloadClicked requires selectedEntries and does nothing useful with an empty queue. To exercise the existing lower-level resume mechanism, instruct reselecting the SAME camera original as a full untrimmed job, not a different file/copy. MediaDownloader looks up the persisted partial by the original camera path, so reselecting that same original can reuse the retained partial; actual reuse remains to be measured. User is instructed to add only that original to queue, return to grid and stop before Download. No partial deletion, new download or source change was performed.

### TEST E manual range resume result

User confirmed the same full original requeued as Download (1), then confirmed starting the explicitly instructed manual retry. A bounded 180-second metadata-only monitor was started first. Immediately before the retry it verified 374,356,968 bytes. The first changed sample was 416,920,620 bytes, followed by 486,354,424; 553,657,208; 623,227,648 and continued growth to the final 3,071,380,142 bytes. Samples were approximately 0.9 seconds apart. No sampled truncation or byte-zero restart occurred. Original TEST-D stayed 38,447,651 bytes. See manual-retry-samples.json; samples include the pre-action waiting period, not just transfer time.

Sanitized app evidence at 10:05:51 UTC reports RESUME offset_MB=374 and DONE saved=1 skipped=0 failed=0. No range-ignored/restart marker was returned by that scan. The positive resume log plus growth from the retained partial establishes lower-level continuation. Unchanged HttpClient sets Range for positive offsets and requires HTTP 206 to append; successful 206 is therefore inferred from code and observed successful path, not claimed as a captured response header. No raw HTTP capture or media-content access was performed. Final size agrees with the rounded 3.1 GB display; exact remote-size/hash equality is not asserted.

Classification: **AUTO_RECONNECT: FAIL; AUTO_RESUME: FAIL; MANUAL_RANGE_RESUME: PASS.** The manual success does not erase either automation failure or the separately recorded background/return finding. Before retry the partial had been stable and the app reported paused/failed, not saved; after actual continuation, completion counters reported saved. Exact interrupted MediaStore pending flag was inaccessible as noted above. GATE-0 remains BLOCKED and must not close in this turn per user instruction. TEST F remains NOT_TESTED.

At 10:06:34 UTC, a metadata-only MediaStore query independently returned the completed TEST-E size 3,071,380,142 bytes with is_pending=0, alongside unchanged TEST-D. Exit 0, no query error. See manual-retry-completion.json. This verifies final publication as completed after the resumed transfer, without inspecting its contents.

## TEST F controlled process restart

Precondition at 10:08:26 UTC: completed file `DJI_20260917113913_0002_D.MP4`, size 3,071,380,142 bytes; MediaStore row 18202, is_pending=0. TEST-D unchanged. See restart-before.json. Only file metadata was read.

Executed `adb -d shell am force-stop dev.konraditurbe.osmosis` (exit 0); `pidof` confirmed no remaining app process. Then `adb -d shell am start -W -n dev.konraditurbe.osmosis/dev.konraditurbe.osmosis.ui.MainActivity` returned Status ok, COLD launch, TotalTime 408 ms. No data clear, uninstall, queue selection, transfer or file mutation was performed.

Post-restart at 10:08:55 UTC: identical filenames, byte sizes and MediaStore row identities; TEST-E still row 18202, 3,071,380,142 bytes, is_pending=0. See restart-after.json. Local completed-file persistence PASS. Actual post-reconnection UI recognition and automatic/manual connection observation are awaiting the user's physical inspection; do not infer them from source or filesystem persistence. Gate remains open until that observation is recorded and final criteria reviewed.

### TEST F final UI observation and classification

User confirms that after process restart and manual camera reconnect, the 3.1 GB camera video is visible in the grid but has no visible downloaded/completed marker. User confirms the local file is intact at 3,071,380,142 bytes and MediaStore remains is_pending=0, consistent with the independent before/after ADB records. No retry, queue selection, re-download, deletion or media-content inspection was performed for TEST F.

- FILE_PERSISTENCE: PASS.
- MEDIASTORE_PERSISTENCE: PASS (same row 18202, is_pending=0).
- OSMOSIS_DOWNLOADED_STATE_RECOGNITION_AFTER_RESTART: FAIL for observed grid UI behavior. This is not proof that every internal deduplication lookup fails; no prohibited repeat download was used to test that.
- Manual reconnect after restart is recorded; earlier AUTO_RECONNECT and AUTO_RESUME failures retain their separate evidence.

Camera sleep remains a separate provisional baseline finding in CAMERA_SLEEP.md, with no causal attribution to this result.

## Final GATE-0 disposition

The current user explicitly authorizes baseline-gate PASS when unchanged reproducibility/hardware observations are complete, with runtime limitations carried forward. The original acceptance criteria require known test/lint status, documented security baseline, actual target-hardware evidence, unchanged source and persisted evidence; they do not require implementing future reliability features during GATE-0. No criterion was silently weakened and no individual FAIL/NOT_TESTED is relabeled PASS.

| Acceptance criterion | Evidence / result |
|---|---|
| Exact baseline | 2fcdbc97e6dbefc875d425368be67cf32b50bb06; unchanged app/build source |
| Debug build and wrapper | Original baseline assembleDebug PASS and official wrapper integrity evidence; APK hash rechecked before installation |
| Unit-test status known | Original Windows CRLF 14 failures retained; unchanged LF checkout 265/265 PASS |
| Lint/static status known | Original FAIL retained, 5 errors dispositioned as baseline/GATE-1 debt |
| Security baseline documented | Expanded scan/review and Kotlin reduced-exposure advisory disposition; no security-clearance claim |
| Install/launch, camera connection, browse | Actual SM-S938B Android 16/API 36 + Pocket 4 Pro; TEST A-C evidence above |
| Normal and large original transfer | TEST D manual/metadata confirmation; TEST E 3.1 GB complete after manual range resume |
| Intentional interruption/resume | Stable partial, paused/failed job, manual positive-offset continuation, final MediaStore completion; automatic reconnect/resume fail |
| Restart observation | Persistent file/MediaStore PASS, visible downloaded-state recognition FAIL; all separately recorded |
| No functional changes | Only governance/state/evidence changed; no app/dependency/Gradle/workflow/media mutation |
| Evidence persisted | This directory plus baseline and disposition directories, in one final hardware evidence/state commit |

**GATE-0: PASS with documented baseline debt.** Known-good is set to version 1.4.4 (29) / exact baseline commit solely as a measured reproduction reference with these limitations, not a production-safe backup or integrity certification. Final size plausibility and MediaStore completion are not cryptographic source equality; exact interrupted pending flag and directly captured HTTP 206 remain explicit observation limits. Sanitized event/size evidence establishes manual continuation without claiming those missing measurements.

Carry forward independently: Android background/return connection failure; AUTO_RECONNECT FAIL; AUTO_RESUME FAIL; UI downloaded-state recognition FAIL; unverified CAMERA_SLEEP_BEHAVIOR; all pre-existing security/lint/line-ending/integrity debt. Camera-sleep idle/active-transfer characterization is NOT_TESTED future work, not invented hardware success. GATE-1 planning is activated administratively; no functional review implementation has begun.

| Mandatory gate-report field | Result |
|---|---|
| GATE | GATE-0 |
| IMPLEMENTATION | None; implementation_authorized=false |
| UNIT TESTS | LF baseline PASS; original environment-specific FAIL retained |
| REGRESSION | Recorded unchanged hardware/software baseline with explicit runtime failures |
| SECURITY | Baseline documented, open GATE-1 debt; no release clearance |
| HARDWARE TEST | Required unchanged-baseline observations complete |
| OPEN FINDINGS | Separate runtime, UI, sleep and prior security/dependency findings above |
| EVIDENCE | This report and sanitized JSON records |
| GATE STATUS | PASS for GATE-0; GATE-1 NOT_TESTED |
| NEXT ACTION | GATE-1 security/architecture review; implementation/release remain false |

Publication hold: before the final evidence/state commit, the user reported a further idle connection loss without deliberate connection change. Cause is unconfirmed. CAMERA_SLEEP.md records the distinct event and pending camera-only wake observation. GATE-0 disposition above is prepared locally; publication is held until this observation completes, and camera sleep is not finalized or inferred from the earlier background/TEST E outcomes.

Further user clarification: the camera was awake and Osmosis foreground; camera display still indicated playback running while Osmosis reported connection failure. This occurrence is now separately recorded as FOREGROUND_SESSION_DROP / SESSION_STATE_DESYNC candidate in FOREGROUND_SESSION_DROP.md. Root cause unconfirmed; explicitly not CAMERA_SLEEP_DISCONNECT or APP_BACKGROUND_DISCONNECT. AUTO_RECONNECT remains FAIL. User is performing normal manual rescan/reconnect without credential changes; outcome pending. No additional transfer or media mutation performed.
