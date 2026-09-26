# Closure audit — status truthfulness and durable refresh

Date: 2026-09-26

## Acceptance-chain re-check

| Area | Re-check | Result |
|---|---|---|
| Service ownership | The camera dispatcher alone owns automatic `DOWNLOAD`; the Activity only reads durable state. | PASS |
| Truthful active state | Only an active dispatcher/progress policy can render “Synchronisation läuft”. Open evidence actions render “Synchronisation offen”. The global card and each active video follow the same live source. | PASS |
| Transient camera refusal | A 404/500 before the first byte is retried only three bounded times before allocation; retry is live/indeterminate in both global and per-video projection, then becomes measured bytes or durable review. | PASS (software simulation; target behavior pending) |
| Inventory and plan | Incomplete source inventory remains a hard no-transfer boundary. Historical ambiguity remains distinct from the current inventory. | PASS |
| Integrity and identity | Existing local copies and ambiguous identities are not promoted without an actual proof. | PASS |
| Durable completion refresh | Dispatcher clears all transient per-video overlays then publishes terminal state; the external SSD coordinator publishes after real ledger-mutating copy work. | PASS |
| Lifecycle/stale ownership | Observer registration invalidates immediately; session/adapter guards reject stale grid painting; terminal decisions dominate stale progress. | PASS |
| Replica independence | SSD reappearance/catch-up remains camera-independent; no-work availability probe cannot recursively trigger observer refresh. | PASS |
| User-facing state matrix | Active, terminal, fail-closed, open-proof, stale replacement, per-video live overlay and external-replica completion rows are documented in `UI_TRANSITION_AUDIT.md`. | PASS |
| Idempotent grid rendering | Identical notifier events redraw no video; a live/durable delta redraws only the exact video owner, preserving headers, filters and unrelated thumbnails. | PASS (software simulation; target visual confirmation pending) |

## Reproducible evidence

`./gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon` completed with **544 JVM
tests**, zero failures and zero errors. Debug APK SHA-256:
`188D877671CD8D4331E129DFE29A8469C95647D13CBBC78289A24E8A11E5E388`.

`tools/autonomy/control.py --check` could not run because this Windows host has neither `py` nor a
`python` launcher. This is recorded as a local control-tool limitation; it does not convert any
hardware criterion into PASS.

## Remaining boundary

The final target observation revoked the former presentation-closure claim: an open local-proof
state was fail-closed but not sufficiently actionable to answer whether work was active, whether
the phone was protected, and whether the overall/SSD backup was complete. This is software work,
not a hardware boundary. The physical retry/render criteria remain batched only after that repair.
