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

## Reproducible verification

On 2026-09-26:

```text
:app:testDebugUnitTest :app:assembleDebug --no-daemon
533 tests, 0 failures, 0 errors, BUILD SUCCESSFUL
```

Debug APK SHA-256:

```text
773977BFD0DBA7D48D4858BB1850E674095CB0F052A62052713F90045400CB48
```

The next hardware session must validate readability and live transition behavior with this exact
APK. It must not claim that unresolved historical/source identity evidence was automatically
verified.
