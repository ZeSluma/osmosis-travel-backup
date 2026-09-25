package dev.konraditurbe.osmosis.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerboseDiagnosticsPolicyTest {
    @Test fun `allows data only within the explicit time and size budgets`() {
        assertTrue(VerboseDiagnosticsPolicy.mayAppend(100, 100 + VerboseDiagnosticsPolicy.MAX_DURATION_MILLIS,
            VerboseDiagnosticsPolicy.MAX_BYTES - 1, 1))
        assertFalse(VerboseDiagnosticsPolicy.mayAppend(100, 101 + VerboseDiagnosticsPolicy.MAX_DURATION_MILLIS, 0, 1))
        assertFalse(VerboseDiagnosticsPolicy.mayAppend(100, 101, VerboseDiagnosticsPolicy.MAX_BYTES, 1))
    }
}
