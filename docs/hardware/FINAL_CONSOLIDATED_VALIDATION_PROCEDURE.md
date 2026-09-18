# Final consolidated hardware validation procedure

Status: **READY FOR ONE NON-DESTRUCTIVE SESSION**. This is the only remaining hardware boundary for the current software artifact. It exercises the maximum useful product surface in one session, without authorizing camera cleanup, app-data clearing, SSD formatting, or overwriting existing media.

## Prepared artifact and guardrails

- Install only `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `27C877C412EFAF673AEE3788CE1874A1FD644293DB9FA04936267D20177CD600`.
- Keep every Pocket original, current phone partial/local copy, and existing SSD file unchanged. A single new non-sensitive test asset may be used for a complete transfer and a second only if an interruption test is safe. Never delete from camera or format/clear storage.
- Capture only state/reason/count outcomes. Do not export credentials, GPS, paths, media names, or SSD contents. A missing fact is **INCONCLUSIVE**, never inferred as success or failure.
- Do not clear app data, reset the Pocket, or repeat scans. One Rescan is allowed only when the camera was restarted after the initial scan and the app shows the saved camera out of range.

## A. Known-camera connection and trusted inventory

1. Install in place; do not uninstall. Launch Osmosis normally with the Pocket powered on.
2. Observe whether saved `Slumas 4P`/Osmo Pocket 4 Pro connects without tapping Rescan, the camera row, Media, or Download.
3. If it is genuinely out of range after a camera restart, use the one permitted Rescan and wait for `Connecting` then `Revalidating`. Do not repeat it.
4. Confirm the gallery is non-empty and retains known prior states rather than treating prior evidence as deleted or complete.
5. Read the one privacy-safe `Auto:` plan summary. Record only counts: complete, download, verify, revalidate, review. It diagnoses the prior “no download candidates” observation without exposing an asset identity.

PASS: automatic or one bounded revalidation reaches a truthful non-empty source view; incomplete/empty results remain untrusted. FAIL: known data becomes empty/deleted, or UI claims completion from incomplete evidence. Otherwise record INCONCLUSIVE and continue to SSD steps that do not need Pocket traffic.

## B. Automatic phone backup, integrity and duplicate-writer fence

1. With a trusted inventory and one new disposable test asset, wait for automatic scheduling. Do not tap Download first.
2. Confirm the UI reports a queued/active automatic transfer at most once. If it does not queue, record `Auto:` counts and do not force a duplicate writer.
3. Let one small test transfer finish if scheduled. Confirm it becomes local verified/confirmed only after local integrity; it must not become verified merely from bytes written.
4. Background the app, return, then separately turn screen off/on once while idle or safely transferring. Confirm state is re-observed rather than reset or duplicated.

PASS: one writer, no false completion, truthful persistent status across lifecycle. FAIL: duplicate transfer, lost durable state, or false VERIFIED. INCONCLUSIVE: no safe candidate/scheduling opportunity; retain diagnostics.

## C. Bounded recovery, replacement session and diagnostics

1. Only after B is idle or a retained partial is visible, induce one controlled non-destructive camera/AP loss by powering off the Pocket once.
2. Observe the reason state. It may become `USER_ACTION_REQUIRED`, `REVALIDATING`, or another bounded truthful state; no speculative credential/source claim is acceptable.
3. Restore the Pocket once. Confirm reconnect rebuilds the session and performs fresh source revalidation before automatic continuation. A retained partial stays partial/review-required unless safety criteria are met.
4. Confirm no stale callback from the old session repaints the replacement gallery or starts a second writer. Capture sanitized logs only if failure diagnosis needs them.

PASS: bounded recovery/revalidation with prior evidence retained and no stale/duplicate mutation. FAIL: recovery bypasses revalidation, stale UI/transfer mutation, sensitive diagnostic output, or false completion. INCONCLUSIVE: liveness cannot safely be induced.

## D. SSD detection and independent phone-to-SSD catch-up

Run this whenever S25/dongle/SSD topology is usable, even if A--C are INCONCLUSIVE.

1. Use an empty dedicated SSD folder with the `SSD` button **once** to grant a SAF tree. This authorizes future automatic use; it is not a per-sync mode.
2. Turn Pocket off or leave it disconnected. With a known verified phone receipt, attach/re-attach SSD and open/reopen Osmosis. Confirm it is detected/probed without a camera session and phone→SSD catch-up starts only for verified phone work.
3. Confirm SSD copy uses the same date/relative Osmosis folder and safely creates a missing date folder. Do not alter unrelated SSD content.
4. Let one small copy finish. Confirm independent readback gates redundancy; Camera Sync must not regress merely because SSD is absent.
5. For a second disposable copy only, disconnect SSD once mid-copy, restart/reopen app, reconnect SSD, and inspect result. The old item remains partial/review-required; no duplicate final allocation or redundancy claim is allowed.

PASS: valid grant is re-acquired only when provider is usable; offline Pocket does not block catch-up; readback gates redundancy; interruption stays fail-closed. FAIL: stale storage is writable, unrelated data is overwritten, partial becomes verified, or camera absence blocks SSD detection. INCONCLUSIVE: host/hub/provider cannot safely expose SSD.

## Session close-out

Record each section as PASS, FAIL, or INCONCLUSIVE with only state/reason/count information. Do not retry failed physical conditions repeatedly. The record updates `G7-LIFECYCLE-RECOVERY`, `G7-REASON-DIAGNOSTICS`, `MVP-SSD-SAF-HOST`, and `MVP-SSD-REPLICA-RECOVERY` in one pass.
