package dev.konraditurbe.osmosis.connection

/** Pure, fenced recovery policy. Android/BLE/Wi-Fi adapters perform effects outside this reducer. */
enum class ConnectionState { DISCONNECTED, CONNECTING, READY, RECONNECT_WAIT, RECONNECTING, REVALIDATING, USER_ACTION_REQUIRED, STOPPED }
enum class ConnectionReason { NETWORK_LOSS, SESSION_DESYNC, CAMERA_POWER_OFF, CAMERA_UNAVAILABLE, PERMISSION_REVOKED, SOURCE_IDENTITY_CHANGED, UNKNOWN }
enum class ConnectionEvent { START, TRANSPORT_READY, SESSION_READY, LOST, RETRY_TIMER, REVALIDATED, STOP, USER_ACTION_RESOLVED }
data class RecoverySnapshot(val state:ConnectionState=ConnectionState.DISCONNECTED,val attempts:Int=0,val reason:ConnectionReason?=null)

object RecoveryStateMachine {
    const val MAX_TRANSIENT_ATTEMPTS=8
    fun reduce(current:RecoverySnapshot,event:ConnectionEvent,reason:ConnectionReason?=null):RecoverySnapshot = when(event) {
        ConnectionEvent.START, ConnectionEvent.USER_ACTION_RESOLVED -> RecoverySnapshot(ConnectionState.CONNECTING)
        ConnectionEvent.TRANSPORT_READY -> if(current.state in setOf(ConnectionState.CONNECTING,ConnectionState.RECONNECTING)) RecoverySnapshot(ConnectionState.REVALIDATING,current.attempts,current.reason) else current
        ConnectionEvent.SESSION_READY -> if(current.state in setOf(ConnectionState.CONNECTING,ConnectionState.REVALIDATING)) RecoverySnapshot(ConnectionState.READY) else current
        ConnectionEvent.REVALIDATED -> if(current.state==ConnectionState.REVALIDATING) RecoverySnapshot(ConnectionState.READY) else current
        ConnectionEvent.LOST -> when(reason) {
            ConnectionReason.CAMERA_POWER_OFF,ConnectionReason.CAMERA_UNAVAILABLE,ConnectionReason.PERMISSION_REVOKED,ConnectionReason.SOURCE_IDENTITY_CHANGED -> RecoverySnapshot(ConnectionState.USER_ACTION_REQUIRED,current.attempts,reason)
            else -> if(current.attempts>=MAX_TRANSIENT_ATTEMPTS) RecoverySnapshot(ConnectionState.USER_ACTION_REQUIRED,current.attempts,reason?:ConnectionReason.UNKNOWN)
                else RecoverySnapshot(ConnectionState.RECONNECT_WAIT,current.attempts+1,reason?:ConnectionReason.UNKNOWN)
        }
        ConnectionEvent.RETRY_TIMER -> if(current.state==ConnectionState.RECONNECT_WAIT) current.copy(state=ConnectionState.RECONNECTING) else current
        ConnectionEvent.STOP -> current.copy(state=ConnectionState.STOPPED)
    }
}
