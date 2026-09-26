# Status truthfulness correction — 2026-09-26

## Triggering S25 observation

With the connected Pocket, the UI first showed every file as requiring assignment and described
seven local files as being checked. The display did not change during one minute of idle time.
After restarting the app and reconnecting, the count became five and five rows showed locally
complete / integrity checked / source still open.

This is a valid product defect report, not evidence of a background verifier finishing work.

## Cause

`AutomaticCameraTransferDispatcher` schedules only trusted `DOWNLOAD` plan items.
`VERIFY_EXISTING` and identity revalidation items remain deliberately fail-closed: the ledger must
not promote an existing local file without a real integrity or source-identity proof. The former
copy rendered those *open* plan items as “werden geprüft”, which implied a worker that does not
exist. The restart caused a fresh durable reconciliation/read, explaining the changed observed
counts; it did not prove that a continuous background check had run.

## Correction

- A real service operation remains the only source of “Synchronisation läuft” and progress.
- Open local integrity proof is rendered as **Synchronisation offen** / “N lokale Dateien brauchen
  eine Integritätsprüfung”.
- Open source identity work is rendered as **Synchronisation offen** / “N Dateien brauchen eine
  sichere Zuordnung”.
- Both states explicitly say that no file is silently promoted as safe.
- The final combined hardware plan now rejects wording that presents open proof work as active.

The correction changes only human-facing state copy. It does not weaken the source, integrity,
redundancy, cleanup or automatic-deletion invariants.

## Follow-up: SSD result projection

The same audit found a separate live-update gap: `ExternalReplicaCoordinator` wrote its completed
phone-to-SSD receipt on a background thread but did not notify `BackupProjectionNotifier`.
Consequently the next status transition could require an Activity refresh or restart even though
the durable result was already correct. The coordinator now publishes exactly once after a real
copy run. It deliberately does not publish after a no-work availability probe, which would turn
the UI observer's refresh into a loop.

## Follow-up: live card and per-video projection

The audit also found that camera transfer liveness was exposed only as a global percentage. The
dispatcher now maintains an in-memory, exact-display-identity overlay for the file it currently
owns: **Synchronisation aufs Telefon · N %**, followed by **Telefon gesichert · Integrität
geprüft** or a clearly fail-closed review state. A small horizontal bar is visible only for the
currently downloading video. Every terminal dispatcher path clears every transient cell overlay
before publishing the durable reread, so a stale live label cannot survive completion, failure or
replacement.

The independent SSD runner now projects its actual count/percentage in the same status area while
it runs and returns to the durable phone/SSD result when it ends. The UI never calls an open
integrity/identity plan item an active transfer.

## Reproducible verification

On 2026-09-26:

```text
:app:testDebugUnitTest :app:assembleDebug --no-daemon
538 tests, 0 failures, 0 errors, BUILD SUCCESSFUL
```

Debug APK SHA-256:

```text
ADB424D961F6566CE0F1C5790C24F3D76229D2EB7A8BC763020673A5B0E453F6
```

## Follow-up: transient Pocket source refusal

The subsequent S25 observation exposed a missing transport scenario in the prior audit: the
automatic strict writer started, but the Pocket returned HTTP 404 before the first byte for every
selected item. The previous automatic path treated that immediately as permanent review, while
the established manual camera client already treats the same observed 404/500 shape as a
short-lived busy response. The display therefore had no meaningful opportunity to show a changing
transfer state.

The automatic source now retries only HTTP 404 or 500, at most three times with bounded 250 ms,
750 ms and 1,500 ms waits. The retry happens before `SingleAssetTransfer` reserves an allocation
or opens a phone destination. Other status codes, cancellation and exhausted retries stay
fail-closed and never create a duplicate/partial media file. During the wait, both the global card
and the exact video row state **Kamera antwortet noch · Übertragung wird erneut versucht**; the
row keeps an indeterminate progress bar because work is genuinely active but no byte progress
exists yet. A successful retry switches to the measured phone percentage; terminal state clears
the live projection and rereads durable evidence.

The prior test gap was real. The corrected focused matrix includes `CameraTransferSourceTest`:
two transient 404 responses then 200, four 404 responses then bounded fail-closed refusal, and a
403 response with no retry. `LiveTransferFileProjectionPolicyTest` covers the distinct
waiting-for-camera state. The allocation ordering was reviewed against `SingleAssetTransfer`:
`source()` completes before `reserveAllocation()`.

Final verification for this correction completed with **541 JVM tests, zero failures/errors** and
debug APK SHA-256 `D788F45892CE67E3B7546E0EFA22D42852AC95F058D5DB3105507DD38310CDEE`.
No test result claims that unresolved historical/source identity evidence was automatically
verified.
