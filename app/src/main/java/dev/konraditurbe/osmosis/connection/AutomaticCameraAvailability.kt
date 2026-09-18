package dev.konraditurbe.osmosis.connection

/** Pure selection policy: a saved camera is a hint, not authority to override a user stop. */
object AutomaticCameraAvailability {
    fun select(recentSavedMacs:List<String>,advertisedMacs:Set<String>,userStopped:Boolean,alreadyConnecting:Boolean):String? {
        if(userStopped || alreadyConnecting)return null
        // Android adapters and persisted records do not promise MAC letter casing. Return the
        // advertised value so the caller always connects through the live BluetoothDevice.
        return recentSavedMacs.firstNotNullOfOrNull { saved ->
            advertisedMacs.firstOrNull { advertised -> advertised.equals(saved, ignoreCase = true) }
        }
    }
}
