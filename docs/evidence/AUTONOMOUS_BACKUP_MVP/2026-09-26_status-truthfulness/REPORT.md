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

The next hardware session must validate readability and live transition behavior with this exact
APK. It must not claim that unresolved historical/source identity evidence was automatically
verified.
