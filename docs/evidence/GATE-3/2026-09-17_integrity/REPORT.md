# GATE-3 software checkpoint

2026-09-17. Parent3c3a06ab8ee4e7a16c0666ce04841c6465dc8612; working branch codex/gate-3-transfer-integrity. G2 predecessor136b02b3951b18def39ace3062ca164584d1f2cd. Main remains2fcdbc97e6dbefc875d425368be67cf32b50bb06. This report belongs to its enclosing commit; no self-referential commit hash is fabricated.

## Claims and limits

- SOFTWARE_PROVEN: strict full/tail response validation, exact length/EOF/flush/close/readback contract, independent evidence dimensions, fail-closed completion predicates, checked-copy failure paths, recovery disposition and explicit UI batch results.373 unit tests, zero failures/errors/skips at final LF checkpoint;20 are current integrity/copy/recovery/batch tests.
- EMULATOR_PROVEN: guarded synthetic schema migrations through6, Room persistence/fencing, nine injected transfer failure/success cases, preservation on unsafe retry, new-owned MediaStore staging/publication, separate-process journal/evidence restoration after force-stop, launcher security regression.
- HARDWARE_PROVEN: no new G3 claim. Earlier G0/G2 evidence retains its own scope. S25 remains on G2/schema5; no S25 installation or real media operations during this software checkpoint.
- UNPROVEN: actual Pocket strict HTTP compatibility, independently trustworthy source-version continuity, successful production resume, S25 staging/publication behavior and target process-death boundaries. Overall G3 NOT_TESTED. No VERIFIED promotion or cleanup authority.

The scoped Pocket4Pro full-video UI routes through the serialized ledger writer and explicit camera Network. It does not fall back to the legacy downloader, automatically retry an ambiguous partial, or adopt/overwrite an existing same-name copy. Existing unverified copies are counted separately and preserved. Completed new files report independent file integrity and overall UNVERIFIED. Unsupported types and trimmed requests are retained for review in this scoped path. Other cameras retain legacy behavior without receiving new verification authority.

Schema6 adds append-only transfer/source evidence and durable attempts. INTENT precedes allocation; ALLOCATING fences duplicate allocation; journaled URI precedes writes; checkpoints follow sync; publication follows exact transfer and readback; publication alone never supplies source identity. A process gap may leave an orphan or review-required partial; it cannot authorize deletion, silent replacement or duplicate allocation. Recovery is conservative. Safe resume blocking is proven; successful production resume is not claimed.

## Commands and results

Environment: existing isolated LF checkout, JDK21.0.12.1+1, Android SDK36, Gradle wrapper8.14.5; build cache disabled. Host Windows. Emulator serial emulator-5580, guarded ranchu/sdk and empty camera preferences. No new dependencies or Gradle configuration changes.

Candidate debug APK: isolated mirror app/build/outputs/apk/debug/app-debug.apk, SHA2560435e8d85a461275db358bf9e5585cca41c53474ab6b06319b2f4f6d005563c2. Normalized Kotlin/Java/XML source comparison against the working repository found zero differences after testing. APK is local only and not installed on S25. No release artifact was produced or published.

Commands run from the isolated source mirror:

```text
./gradlew.bat :app:testDebugUnitTest --tests '*integrity*' :app:assembleDebug :app:assembleDebugAndroidTest --no-build-cache --console=plain --no-daemon
```

Exit0,37s for integrated UI/adapter checkpoint (16 integrity tests then present). Prior contract iterations:7 tests PASS; expanded16 PASS; builds approximately22–38s. Final four batch tests are included in373 below.

```text
./gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:compileReleaseKotlin :app:lintDebug --no-build-cache --console=plain --no-daemon
```

Final exit1,36s, exclusively lint failure:373 unit tests PASS; debug APK, instrumentation APK and release Kotlin compilation PASS. Lint5 unchanged errors,79 warnings,2 hints. The additional warning is Recycle on PhonePendingVideo query line30; the cursor is enclosed in use and closed on success/failure. Retained as nonblocking tooling review, not suppressed. Baseline errors remain CoarseFineLocation and four UseAppTint. No lint PASS claim or baseline suppression/configuration change.

Before the final rerun, the mirror copy accidentally restored CRLF golden text resources:373 tests/14 golden failures, exit1,44s. Failure differences contain CR characters; only isolated mirror golden text line endings were restored to LF. Repository test fixtures and configuration were unchanged. The earlier broad checkpoint had369 tests PASS,5 errors78 warnings2 hints,72s, before integration additions.

```text
adb -s emulator-5580 install -r <LF>/app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5580 install -r <LF>/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5580 shell am instrument -w -e ledgerPhase suite dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
adb -s emulator-5580 shell am instrument -w -e ledgerPhase integrityPersist dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
adb -s emulator-5580 shell am force-stop dev.konraditurbe.osmosis
adb -s emulator-5580 shell am instrument -w -e ledgerPhase integrityRestore dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
adb -s emulator-5580 shell am instrument -w dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation
```

Both installs Success; all four instrumentation outputs explicitly PASS (not inferred from shell exit). Final suite observed provider indexed-size lag=true, which did not become byte-integrity evidence. Process restore confirms partial40/100 retained and publication-gap100/100 retains CONFIRMED transfer, UNCONFIRMED source, UNVERIFIED overall. Launcher normal/three hostile warm/malformed cold PASS. Synthetic artifacts only; fixture cleanup touches only its newly allocated emulator URI.

## Original failures preserved

Initial migration assertion still expected schema5 after additive schema6; fixed version assertions, then suite PASS. No migration-data failure was inferred.

Initial real-MediaStore fixture failed at initial inspect. Fixed-label narrowing established provider-row insertion had not materialized a readable file. The adapter now opens only its newly inserted owned empty file, verifies zero length, syncs and closes it before inspection. Regression PASS; no existing-file overwrite/adoption introduced. Diagnostic iterations were approximately17–22s; latest pre-integration suite6.39s PASS. No raw device logs, secrets, GPS, credentials, media payloads or unrelated logs persisted.

## Remaining work disposition

A: unknown source/prefix continuity must continue blocking append/overall verification. No camera proof is invented from filename, size, local hash or a successful range response.

B: actual target adapter/source-continuity and storage/lifecycle facts are queued in HARDWARE_QUEUE.md; successful resume remains open. Do not mark G3 PASS or cross its dependent gates on software-only evidence.

C: conservative repeated digest scans/checkpoint overhead; unsupported asset-type/sidecar presentation assigned G4; automatic session/background ownership assigned G7; existing lint debt and cursor-analysis warning. Do not perfect these before resolving the target capability facts.

Implementation authorized within G3; release false. No main/upstream modification, merge, release, camera scan or media download/delete/read/hash occurred. User execution policy persisted in docs/EXECUTION_POLICY.md and linked from AGENTS/state.
