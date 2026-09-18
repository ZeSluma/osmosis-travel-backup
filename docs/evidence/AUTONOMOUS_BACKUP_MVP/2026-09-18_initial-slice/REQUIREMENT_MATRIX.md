# Autonomous backup MVP requirement matrix

This is an internal software audit, not a hardware claim. `PROVEN` means only the stated test
scope; Pocket/S25/hub/SSD behavior remains in the consolidated hardware queue.

| Requirement | Current evidence | Status |
|---|---|---|
| Known camera automatic availability; explicit stop terminal | `AutomaticCameraAvailabilityTest`; advertisement-gated wiring | SOFTWARE_PROVEN; hardware pending |
| Complete inventory-only planning; incomplete/unknown fail closed | Ledger planner/reconciliation tests; G7 revalidation tests | SOFTWARE_PROVEN |
| Automatic camera transfer initiation | `AutonomousBackupRuntime`, trusted plan wiring and recovery scenario | SOFTWARE_PROVEN at scheduler boundary |
| Camera effect/session ownership outside Activity | Service owns durable truth/resources, but MainActivity still drives scanner/GATT/AP/datalink effects | **GAP: extraction required** |
| Source continuity and phone integrity | Existing G3 strict transfer/receipt evidence; no source-equivalence overclaim | SOFTWARE_PROVEN / Pocket semantics pending |
| SAF destination, capacity/grant failure, capture-day staging | Policy tests and SAF adapter | SOFTWARE_PROVEN; real provider pending |
| Phone→SSD checksum/readback and no partial promotion | Replica verification/fault tests | SOFTWARE_PROVEN |
| SSD partial/restart/reconciliation/no duplicate allocation | Schema-9 emulator fixture; reconciliation policy | SOFTWARE_EMULATOR_PROVEN |
| Derived camera-sync/redundancy/cleanup status | Ledger-derived projection tests/UI observer | SOFTWARE_PROVEN; cleanup informational only |
| Activity/process lifecycle | G7 recreation/background and replica restart emulator modes | SOFTWARE_EMULATOR_PROVEN |
| Physical Pocket/S25/hub/SSD behavior | `G7-LIFECYCLE-RECOVERY-BATCH`, `MVP-SSD-BATCH` | HARDWARE_PENDING |

## Required next software work

Move automatic availability and connection effect initiation behind the application/service
connection owner, preserving the existing epoch fence and keeping UI as an observer/command
surface. Then repeat targeted lifecycle and fault tests before any completion audit.
