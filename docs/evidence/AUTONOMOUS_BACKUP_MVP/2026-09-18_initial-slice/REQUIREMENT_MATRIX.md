# Autonomous backup MVP requirement matrix

This is an internal software audit, not a hardware claim. `PROVEN` means only the stated test
scope; Pocket/S25/hub/SSD behavior remains in the consolidated hardware queue.

| Requirement | Current evidence | Status |
|---|---|---|
| Known camera automatic availability; explicit stop terminal | `AutomaticCameraAvailabilityTest`; advertisement-gated wiring | SOFTWARE_PROVEN; hardware pending |
| Complete inventory-only planning; incomplete/unknown fail closed | Ledger planner/reconciliation tests; G7 revalidation tests | SOFTWARE_PROVEN |
| Automatic camera transfer initiation | `AutonomousBackupRuntime`, trusted plan wiring and recovery scenario | SOFTWARE_PROVEN at scheduler boundary |
| Camera effect/session ownership outside Activity | Service layer owns scanner/GATT/AP allocation/start, datalink socket/thread lifecycle, and durable transport/revalidation transitions. Activity receives immutable observations for ledger/UI projection. | SOFTWARE_PROVEN |
| Source continuity and phone integrity | Existing G3 strict transfer/receipt evidence; no source-equivalence overclaim | SOFTWARE_PROVEN / Pocket semantics pending |
| SAF destination, capacity/grant failure, capture-day staging | Policy tests and SAF adapter | SOFTWARE_PROVEN; real provider pending |
| Phone→SSD checksum/readback and no partial promotion | Replica verification/fault tests | SOFTWARE_PROVEN |
| SSD partial/restart/reconciliation/no duplicate allocation | Schema-9 emulator fixture; reconciliation policy and transactional allocation fence | SOFTWARE_EMULATOR_PROVEN |
| Derived camera-sync/redundancy/cleanup status | Ledger-derived projection tests/UI observer | SOFTWARE_PROVEN; cleanup informational only |
| Activity/process lifecycle | G7 recreation/background, durable-session process restoration, and replica restart emulator modes | SOFTWARE_EMULATOR_PROVEN |
| Physical Pocket/S25/hub/SSD behavior | `G7-LIFECYCLE-RECOVERY-BATCH`, `MVP-SSD-BATCH` | HARDWARE_PENDING |

## Internal conclusion

All software-implementable requirements in this matrix are internally proven at the stated scope.
Only the consolidated physical S25/Pocket/hub/SSD validation remains.
