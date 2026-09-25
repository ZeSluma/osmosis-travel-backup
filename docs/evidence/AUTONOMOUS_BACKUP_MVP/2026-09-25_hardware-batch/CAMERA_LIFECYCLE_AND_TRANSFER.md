# S25 / Pocket camera lifecycle and automatic-transfer observation — 2026-09-25

## Preconditions

- Working branch build: `64dbd640ee5e7d4eeec7b948bb9c8bc8b2aa39f6`, debug APK SHA-256
  `0BCB0B0A410A9F1E2E0968BD867CF0724C48178B881C7D58D20E758B2B6CDB1E`.
- Installed in place; app data and protected camera originals were retained.
- No deletion, reset, overwrite, manual download selection or SSD operation occurred.

## Observed results

| Check | Result | Evidence |
|---|---|---|
| Saved camera discoverable after app starts while camera is off | PASS | Camera was turned on after launch; app automatically connected and rebuilt the grid. |
| Trusted non-empty inventory | PASS | Grid progressed from 6 to 7 items after a new short test recording. Sanitized log reports `MANIFEST: 7 files`. |
| Automatic service-owned transfer | PASS | New 101 MB / 9 s test recording initially showed not downloaded, then `Lokal vollständig · Integrität geprüft; Quelle offen` without pressing Download. Sanitized target log shows HTTP 200, durable checkpoints through 100,665,952 bytes, then `TRANSFER result=TRANSFERRED_UNVERIFIED`. |
| No false global completion with historic ambiguity | PASS | After completion: Camera Sync pending, Redundancy pending, Safe to Clear no; history unresolved remained 3. |
| Background/screen lock preservation | PASS | After ~15 s background/screen lock, connected grid and conservative summary were retained. |
| Controlled camera power cycle | PASS | App rebuilt/revalidated automatically and restored the grid. Sanitized log records AP loss, bounded BLE rediscovery, new manifest and grid readiness. |
| Progress projection visible during transfer | INCONCLUSIVE | The recording was already complete by the next user-visible observation, so intermediate percentage copy was not observed. Transfer itself and durable result were observed. |
| SSD / SAF target behavior | HARDWARE_DEFERRED | Existing dongle/topology remains unavailable; not an app failure. |

## Safety interpretation

`TRANSFERRED_UNVERIFIED` is intentionally not a claim that the Pocket source identity is immutable.
The UI therefore presents local integrity as confirmed while leaving source matching, redundancy and
camera cleanup pending. This is expected fail-closed behavior, not a download failure.

## Follow-up

The next APK replaces technical English summary text with German action-oriented wording. Any future
physical session should batch only the still-unobserved intermediate progress and the deferred SSD
path with another independently useful check; it must not delete or re-download protected media.
