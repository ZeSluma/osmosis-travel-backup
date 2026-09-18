package dev.konraditurbe.osmosis.ui

import org.junit.Assert.*
import org.junit.Test

class LauncherInputPolicyTest {
    private val known = setOf("AA:BB:CC:DD:EE:FF")
    private val view = "android.intent.action.VIEW"

    @Test fun `normal launcher never reads command extras`() {
        assertNull(LauncherInputPolicy.camera("android.intent.action.MAIN", false, emptySet(), known) {
            error("Launcher extras must not be read")
        })
    }

    @Test fun `only bare launcher intent restarts a stopped discovery session`() {
        assertTrue(LauncherInputPolicy.freshLauncher("android.intent.action.MAIN", false,
            setOf("android.intent.category.LAUNCHER"), false))
        assertFalse(LauncherInputPolicy.freshLauncher("android.intent.action.MAIN", false,
            setOf("android.intent.category.LAUNCHER"), true))
        assertFalse(LauncherInputPolicy.freshLauncher(view, false, emptySet(), false))
    }

    @Test fun `unknown and absent actions never read extras`() {
        for (action in listOf(null, "internal.offload", "android.intent.action.SEND")) {
            assertNull(LauncherInputPolicy.camera(action, false, emptySet(), known) { error("Unexpected read") })
        }
    }

    @Test fun `data and categories cannot smuggle a shortcut`() {
        assertNull(LauncherInputPolicy.camera(view, true, emptySet(), known) { error("Unexpected read") })
        assertNull(LauncherInputPolicy.camera(view, false, setOf("android.intent.category.BROWSABLE"), known) { error("Unexpected read") })
    }

    @Test fun `unknown camera cannot become a target`() {
        assertNull(LauncherInputPolicy.camera(view, false, emptySet(), known) { "00:11:22:33:44:55" })
    }

    @Test fun `malformed missing and oversized identifiers are ignored`() {
        for (value in listOf(null, "", "AA:BB:CC:DD:EE:GG", "x".repeat(10000))) {
            assertNull(LauncherInputPolicy.camera(view, false, emptySet(), known) { value })
        }
    }

    @Test fun `malformed bundle fails closed`() {
        assertNull(LauncherInputPolicy.camera(view, false, emptySet(), known) { throw IllegalArgumentException("synthetic invalid bundle") })
    }

    @Test fun `known shortcut returns stored identity only`() {
        assertEquals("AA:BB:CC:DD:EE:FF", LauncherInputPolicy.camera(view, false, emptySet(), known) { "aa:bb:cc:dd:ee:ff" })
    }

    @Test fun `forgotten camera cannot be selected on subsequent launch`() {
        assertNull(LauncherInputPolicy.camera(view, false, emptySet(), emptySet()) { known.first() })
    }
}
