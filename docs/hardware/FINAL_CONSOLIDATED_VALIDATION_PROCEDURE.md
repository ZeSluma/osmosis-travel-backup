# Final consolidated hardware validation procedure

Status: **HARDWARE_DEFERRED**. Execute only after the final software artifact is available and
with the non-destructive limits below. This procedure consolidates the pending items in
`VALIDATION_QUEUE.json`; it does not authorize camera cleanup, app-data clearing, SSD formatting,
or overwriting existing media.

## Artifact and preparation

- Verify the S25-install artifact SHA-256 is
  `544D36B98B308E8DF0A9A6A356D6107FAD55B34FD981E1A2847392909E2FB915` for
  `app/build/outputs/apk/debug/app-debug.apk`.
- Preserve all current Pocket originals, phone partials and SSD contents. Use one new,
  non-sensitive test asset and an empty dedicated SSD directory only if the SSD topology is usable.
- Do not clear app storage, rescan repeatedly, delete media, reset the Pocket, or request a
  destructive recovery action. Capture only sanitized state/reason/count evidence.

## One S25/Pocket session

1. Install in place, open Osmosis once, and observe automatic reuse of the known Pocket without
   tapping Rescan or media controls. Confirm a non-empty trusted inventory contains retained known
   evidence and the new test asset.
2. Allow the automatic plan to run for the new test asset. Confirm exactly one transfer writer,
   no duplicate allocation, and verified phone completion only after local integrity evidence.
3. Background the app and separately screen it off/on. Confirm the service-owned state remains
   truthful and the UI re-observes it after recreation.
4. Induce one controlled, non-destructive AP/session loss. Confirm recovery is bounded, fresh
   source revalidation occurs before continuation, and any incomplete/empty enumeration remains
   untrusted without removing prior asset knowledge or promoting completion.
5. Record sanitized reason transitions. A reason that cannot be established must remain UNKNOWN or
   USER_ACTION_REQUIRED; credentials, GPS, paths and media names must not appear in diagnostics.

For every step: **PASS** requires truthful fenced state with no duplicate writer, false completion,
or unsafe resume. **FAIL** is any such promotion or state corruption. **INCONCLUSIVE** preserves
the safe state and records only the reason/count outcome.

## Conditional SSD extension — only if the host topology is usable

1. Select the empty dedicated SSD directory through SAF and restart/reconnect once; verify access
   is reacquired only with a valid grant.
2. Replicate the verified phone test asset and check independent SSD readback checksum.
3. During a second copy, perform one controlled SSD disconnect, restart once and reconnect. The
   interrupted copy must remain partial/review-required, avoid a duplicate final allocation, and
   never count toward redundancy.

If the dongle/hub/SSD path is unavailable, mark `MVP-SSD-SAF-HOST` and
`MVP-SSD-REPLICA-RECOVERY` **INCONCLUSIVE / HARDWARE_DEFERRED**. Do not infer an Osmosis or SSD
failure, and do not promote Backup Redundancy Complete or Safe-to-Clear.
