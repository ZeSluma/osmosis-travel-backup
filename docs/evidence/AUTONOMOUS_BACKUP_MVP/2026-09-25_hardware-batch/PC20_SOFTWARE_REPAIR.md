# PC20 software repair — durable summary projection

## Defect

On the S25, a trusted seven-member inventory rendered correct item labels but retained the earlier
transient automatic-work copy `Kameraliste wird noch geprüft`.

## Repair

`LedgerCoordinator.backupSummaryProjection` derives the product status and automatic-plan
diagnostic in one serialized, active-session/lease-fenced ledger read. `MainActivity` renders only
that projection, rather than independently racing status and diagnostic callbacks. The UI never
uses earlier automatic text as input to its next render.

## Regression evidence

- `BackupStatusCopyTest.completeProjectionNeverRetainsAnEarlierInventoryPendingMessage` asserts
  that a complete inventory with four historical unresolved identities displays the specific
  review explanation, never the transient inventory-pending text.
- `ProductStateMatrixTest.visibleProductStateMatrixRequiresEveryDurableCompletionPrerequisite`
  covers trusted/incomplete inventory plus Camera Sync, SSD redundancy and Safe-to-Clear failure
  boundaries. Asset-state coverage remains in the same matrix class.
- Targeted regression command and full JVM/debug checkpoint completed successfully on 2026-09-25.

## Remaining physical proof

HD01 must install the final signer-compatible APK in place and observe the durable German summary
after a trusted non-empty Pocket inventory. This is a target UI observation only; no media deletion,
data reset, protected-media overwrite or unsafe resume is authorized.
