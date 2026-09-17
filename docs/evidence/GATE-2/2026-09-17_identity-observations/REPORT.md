# GATE-2 incomplete identity observation disposition

2026-09-17. Starting commit2a2e0ec56f55689da52d6447e6cfaca76e9daadb, branch gate-2/persistent-ledger-sync-planner. Main remains2fcdbc97e6dbefc875d425368be67cf32b50bb06. Decision: docs/decisions/0012-incomplete-identity-observations.md.

## Reconstructed failure and correction

The old fingerprint includes source, storage, full path, size, remote time, type, handle and version. A missing-size observation consequently created a fourth historical candidate. Its local-candidate claim prevented selection of the original38MB copy. Neither name similarity nor its later absence proves source equivalence or deletion.

Schema5 retains all four historical identities and every candidate relationship. Missing/non-positive-size observations now have their own persistent table and cannot enter an actionable asset/transfer plan. Legacy weak identities are backfilled as unresolved observations, retained physically, and excluded from operational asset queries/local ownership claims. Re-observing the original complete candidate can restore its local pointer from current metadata inventory without claiming integrity. Ambiguous candidate pointers remain historical references under NEEDS_REVALIDATION; missing/unavailable/changed local evidence does not authorize transfer completion.

Exact composite evidence permits operational candidate reuse, never byte verification. A non-destructive observation-resolution link requires exactly one compatible candidate with the same independently trustworthy source-domain immutable version token. Name alone, count agreement and partial-snapshot absence never resolve it. Current Pocket supplies no such token. Existing full-sized ambiguous asset rows are not automatically merged. Unresolved observations/current identity ambiguity block snapshot COMPLETE regardless of coverage flags.

## Verification

- Targeted IdentityEvidenceTest and LocalCandidatePolicyTest: PASS; six new pure evidence tests, existing policy suite; debug and instrumentation compilation PASS (initial checkpoint39s).
- Authoritative LF full unit XML:353 tests,0 failures,0 errors,0 skipped. Release compilation PASS. Final broader build/checkpoint57s, exit1 attributable to unchanged lint baseline.
- lintDebug: FAIL, same5 errors/78 warnings/2 hints; no new lint errors or suppression. No dependency/Gradle changes.
- Guarded emulator suite: PASS. New schema4-to-5 fixture replays3 known assets plus1 weak legacy row, preserves history/candidate references/paths, rejects repeated partial observations as actionable assets, restores both known local references, checks order-independent/idempotent planning, restart and strong-evidence reference linking. Existing migrations, rollback/FKs, stale fencing, provider changes, timestamp/audit privacy/corruption checks also pass.
- Launcher B1 security regression: PASS, normal launch, three hostile warm launches, malformed cold launch, no command authority.
- git diff --check: PASS. Scope limited to ledger implementation/schema, associated tests/audit and governance/evidence. No transfer/network adapter, dependency, Gradle, main, media or credentials changed.

### Reproducible command forms and observed outcomes

Build cwd: isolated LF snapshot referenced by the existing TEMP/osmosis-gate2-lf-path.txt. JDK21.0.12.1+1 and established Android SDK/Gradle8.14.5 environment; no tooling changes. Source files mirrored with LF only, no semantic source changes in the mirror.

`./gradlew.bat :app:testDebugUnitTest --tests '*IdentityEvidenceTest' --tests '*LocalCandidatePolicyTest' :app:assembleDebug :app:assembleDebugAndroidTest --no-build-cache --console=plain --no-daemon`: targeted checkpoint PASS.

Final harness-only command `./gradlew.bat :app:assembleDebugAndroidTest --no-build-cache --console=plain --no-daemon`: exit0,20s. No production changes after full353-test checkpoint.

`adb -s emulator-5580 install -r <LF>/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`: Success.

`adb -s emulator-5580 shell am instrument -w -e ledgerPhase suite dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation`: suite PASS; combined install/test5.57s, shell exit0. Instrumentation stream, not shell exit alone, determines PASS. Stream's legacy label migrations1/2-to-3 is stale; actual assertions and fixture cover schema5.

`adb -s emulator-5580 shell am instrument -w dev.konraditurbe.osmosis.test/dev.konraditurbe.osmosis.security.LauncherSecurityInstrumentation`: PASS,3.02s, exit0.

### Original failures retained

Initial suite failed local-reconciliation. Locator retention was initially too broad and conflicted with missing-file expectations; narrowed to AMBIGUOUS only, retaining candidate history for other states. Mechanical test schema-number replacement also changed two unrelated four-item plan expectations; diff review restored both to4. Those runs reported new-and-removed/activity-recreation assertions, including a stale compiled artifact; completed a fresh test-only build after corrections.

Later run reported GATE2_ASSERTION_local-reconciliation:SYNTHETIC_LOCAL_PROVIDER_MISSING:fixture_line_139. Synthetic MediaStore deletion was checked immediately, unlike the existing bounded changed-file stabilization check. Added the same bounded10x100ms stable-inventory wait for deletion; any immediate incomplete inventory must first leave NEEDS_REVALIDATION. Final MISSING assertion, null selected locator and REVALIDATE_IDENTITY remain mandatory. The resulting whole suite passed. No production retry/policy workaround, suppression or real-file deletion performed. The original generic error did not record the transient enum; provider stabilization is consistent with the successful bounded check, not a proven underlying Android root cause.

## Hardware boundary / stop condition

Prior target evidence remains valid: third persisted DJI_FILENAME2026-09-17T18:40:37, day2026-09-17, UNKNOWN offset/zone;3 original current members,4 historical identities, two original local candidate relationships, protected sizes38447651 and3071380142. These are prior audited facts, not a fresh target check today.

Software replay is sufficient to define and prove the deterministic safe transition. No repeated camera rescan can prove immutable source identity, and none is requested for that purpose. The remaining target integration fact is migration/application on the existing S25 ledger and preservation/reselection of its two real candidate references after ordinary observation. Room opens lazily through LedgerCoordinator.observe; merely launching or using the read-only audit does not exercise migration. Existing hardware integration acceptance therefore stays pending for this new schema.

`adb -d get-state` returned `error: no devices found`. New APK has NOT been installed on S25. No camera/media interaction this step. Minimal next physical action: reconnect the unlocked S25 via authorized USB debugging. Then deploy with install-r, run normal existing integration flow only as needed, and sanitized read-only schema/history/reference/capture-time audit; no downloads or media payload reads. Ask for camera interaction only if actually required at that point.

GATE-2 BLOCKED solely on remaining target integration evidence for the new correction, not on fabricating full snapshot coverage. Snapshot remains INCOMPLETE; all-original trustworthy coverage belongs to G4. G3 dependency not yet satisfied; no speculative next-gate implementation. Implementation authorization stays true within current scope; release false.

## Verified artifacts

Debug APK: app-debug.apk; SHA2560EB27248ED4BA5620753413B3BF2C7616F1CD86C89883440C5C4C5F5A7EAC561.
Test APK: app-debug-androidTest.apk; SHA256F3B05A9676B0172439F2992DE527D4C65DDB93D2F178522B26C93EE48AC7DEBB.
Only synthetic emulator modes ran; real-target helper remains read-only. No phone serial, credentials, GPS, media contents, raw logcat or private DB dump persisted.
