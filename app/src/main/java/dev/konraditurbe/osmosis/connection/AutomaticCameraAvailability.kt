package dev.konraditurbe.osmosis.connection

/** Pure selection policy: a saved camera is a hint, not authority to override a user stop. */
object AutomaticCameraAvailability {
    fun select(recentSavedMacs:List<String>,advertisedMacs:Set<String>,userStopped:Boolean,alreadyConnecting:Boolean):String? {
        if(userStopped || alreadyConnecting)return null
        return recentSavedMacs.firstOrNull { it in advertisedMacs }
    }
}
