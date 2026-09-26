package dev.konraditurbe.osmosis.connection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.net.LinkProperties
import android.net.Network
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.konraditurbe.osmosis.R
import dev.konraditurbe.osmosis.backup.AutonomousBackupRuntime
import dev.konraditurbe.osmosis.ble.GattClient
import dev.konraditurbe.osmosis.ble.OsmoScanner
import dev.konraditurbe.osmosis.net.ApJoiner
import dev.konraditurbe.osmosis.core.DiagnosticEventStore

/**
 * Lifecycle-safe host for future camera-only BLE/AP/datalink effect adapters. It owns persistent
 * session truth; MainActivity may observe it but cannot make a late callback current. No cloud
 * traffic is bound here.
 */
class CameraConnectionService : Service() {
    override fun onCreate() {
        super.onCreate()
        runtime(this) // Restore durable truth before any platform callback can be accepted.
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            // The explicit UI command has already allocated the epoch before requesting this host.
            // Starting it again here would invalidate callbacks that were queued in that same command.
            ACTION_START -> {
                ensureChannel(); startForeground(NOTIFICATION_ID, notification())
                DiagnosticEventStore.open(this).record(DiagnosticEventStore.Type.SESSION_STARTED, newState = "HOST_ACTIVE")
            }
            ACTION_STOP -> { runtime(this).stop(); backupRuntime(this).stop(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
        }
        return START_NOT_STICKY // process restart restores state but never silently resumes user work.
    }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun notification() = NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(getString(R.string.app_name)).setContentText("Camera session active").setOngoing(true).build()
    private fun ensureChannel() {
        (getSystemService(NotificationManager::class.java)).createNotificationChannel(
            NotificationChannel(CHANNEL, "Camera connection", NotificationManager.IMPORTANCE_LOW))
    }
    companion object {
        private const val CHANNEL = "camera_connection"; private const val NOTIFICATION_ID = 7001
        const val ACTION_START = "dev.konraditurbe.osmosis.connection.START"
        const val ACTION_STOP = "dev.konraditurbe.osmosis.connection.STOP"
        @Volatile private var instance: DurableSessionRuntime? = null
        @Volatile private var coordinatorInstance: CameraSessionEffectCoordinator? = null
        @Volatile private var datalinkCoordinatorInstance: CameraDatalinkCoordinator? = null
        @Volatile private var resourcesInstance: CameraSessionResources? = null
        @Volatile private var backupRuntimeInstance: AutonomousBackupRuntime? = null
        @Volatile private var automaticTransferDispatcherInstance: AutomaticCameraTransferDispatcher? = null
        @Volatile private var backupPlanCoordinatorInstance: CameraBackupPlanCoordinator? = null
        @Volatile private var backupProjectionNotifierInstance: BackupProjectionNotifier? = null
        fun runtime(context: Context): DurableSessionRuntime = instance ?: synchronized(this) {
            instance ?: DurableSessionRuntime(PreferenceSessionStore(context)) { lease ->
                val diagnostics = DiagnosticEventStore.open(context)
                val type = when (lease.recovery.state) {
                    ConnectionState.READY -> DiagnosticEventStore.Type.CAMERA_SESSION_READY
                    ConnectionState.RECONNECT_WAIT -> DiagnosticEventStore.Type.RECONNECT_SCHEDULED
                    ConnectionState.RECONNECTING -> DiagnosticEventStore.Type.RECONNECT_ATTEMPT
                    ConnectionState.USER_ACTION_REQUIRED -> DiagnosticEventStore.Type.RECONNECT_FAILED
                    else -> DiagnosticEventStore.Type.SESSION_STATE
                }
                diagnostics.record(
                    type,
                    newState = lease.recovery.state.name,
                    reason = lease.recovery.reason?.name,
                    retryCount = lease.recovery.attempts,
                )
                if (lease.recovery.state == ConnectionState.USER_ACTION_REQUIRED) diagnostics.record(
                    DiagnosticEventStore.Type.USER_ACTION_REQUIRED,
                    reason = lease.recovery.reason?.name,
                    retryCount = lease.recovery.attempts,
                )
            }.also { instance = it }
        }
        fun coordinator(context: Context): CameraSessionEffectCoordinator = coordinatorInstance ?: synchronized(this) {
            coordinatorInstance ?: CameraSessionEffectCoordinator(runtime(context)).also { coordinatorInstance = it }
        }
        fun datalinkCoordinator(context: Context): CameraDatalinkCoordinator = datalinkCoordinatorInstance ?: synchronized(this) {
            datalinkCoordinatorInstance ?: CameraDatalinkCoordinator(resources(context), coordinator(context)) { files, complete, trusted, started ->
                val owned = resources(context)
                owned.sourceAssociation?.let { association ->
                    backupPlanCoordinator(context).publish(association, files, complete, trusted, started,
                        backupProjectionNotifier(context)::publish)
                }
            }.also { datalinkCoordinatorInstance = it }
        }
        fun resources(context: Context): CameraSessionResources = resourcesInstance ?: synchronized(this) {
            resourcesInstance ?: CameraSessionResources().also { resourcesInstance = it }
        }

        /**
         * Bind an explicit new camera selection to the freshly released transport.  The service
         * owns this bridge because trusted inventory publication must not depend on an Activity
         * retaining transient selection state.
         */
        fun configureSelectedCamera(context: Context, association: String, strictTransferSupported: Boolean) {
            resources(context).configureSelectedCamera(association, strictTransferSupported)
        }
        /** Camera and local-replica scheduling belongs to the application owner, never an Activity. */
        fun backupRuntime(context: Context): AutonomousBackupRuntime = backupRuntimeInstance ?: synchronized(this) {
            backupRuntimeInstance ?: AutonomousBackupRuntime().also { backupRuntimeInstance = it }
        }
        fun automaticTransferDispatcher(context: Context): AutomaticCameraTransferDispatcher = automaticTransferDispatcherInstance ?: synchronized(this) {
            automaticTransferDispatcherInstance ?: AutomaticCameraTransferDispatcher(context.applicationContext, resources(context), runtime(context), backupRuntime(context), dev.konraditurbe.osmosis.ledger.LedgerCoordinator.get(context)).also { automaticTransferDispatcherInstance = it }
        }
        fun backupPlanCoordinator(context: Context): CameraBackupPlanCoordinator = backupPlanCoordinatorInstance ?: synchronized(this) {
            backupPlanCoordinatorInstance ?: CameraBackupPlanCoordinator(resources(context), dev.konraditurbe.osmosis.ledger.LedgerCoordinator.get(context), automaticTransferDispatcher(context)).also { backupPlanCoordinatorInstance = it }
        }
        /** UI may observe this signal, but must derive its display from the durable ledger. */
        fun backupProjectionNotifier(context: Context): BackupProjectionNotifier = backupProjectionNotifierInstance ?: synchronized(this) {
            backupProjectionNotifierInstance ?: BackupProjectionNotifier().also { backupProjectionNotifierInstance = it }
        }

        /**
         * Camera transport effects are allocated and started by the application/service owner,
         * rather than by an Activity instance. The listener is deliberately supplied by the
         * caller: it is an observer only and must still fence every asynchronous result.
         */
        fun startCameraScan(
            context: Context,
            adapter: BluetoothAdapter,
            listener: OsmoScanner.Listener,
        ): OsmoScanner {
            val owned = resources(context)
            owned.releaseScanner()
            val callbackGeneration = owned.nextScannerCallbackGeneration()
            lateinit var scanner: OsmoScanner
            val fencedListener = object : OsmoScanner.Listener {
                private fun current() = owned.acceptsScannerCallback(callbackGeneration) && owned.scanner === scanner
                override fun onLog(s: String) { if (current()) listener.onLog(s) }
                override fun onHit(device: BluetoothDevice, rssi: Int, name: String?, modelGuess: String?, modelId: Int?) {
                    if (current()) listener.onHit(device, rssi, name, modelGuess, modelId)
                }
            }
            scanner = OsmoScanner(adapter, fencedListener)
            owned.scanner = scanner
            scanner.start()
            return scanner
        }

        fun stopCameraScan(context: Context, scanner: OsmoScanner) {
            val owned = resources(context)
            if (owned.scanner === scanner) owned.releaseScanner()
            else scanner.stop()
        }

        fun connectCameraGatt(
            context: Context,
            device: BluetoothDevice,
            listener: GattClient.Listener,
        ): GattClient {
            val owned = resources(context)
            owned.releaseGatt()
            val callbackGeneration = owned.nextGattCallbackGeneration()
            lateinit var client: GattClient
            val fencedListener = object : GattClient.Listener {
                private fun current() = owned.acceptsGattCallback(callbackGeneration) && owned.gattClient === client
                override fun onLog(s: String) { if (current()) listener.onLog(s) }
                override fun onReady(gatt: GattClient) { if (current()) listener.onReady(gatt) }
                override fun onNotification(sourceChar: java.util.UUID, raw: ByteArray, parsed: dev.konraditurbe.osmosis.duml.DjiMessage?) {
                    if (current()) listener.onNotification(sourceChar, raw, parsed)
                }
                override fun onDisconnected() { if (current()) listener.onDisconnected() }
            }
            client = GattClient(context.applicationContext, fencedListener)
            owned.gattClient = client
            client.connect(device)
            return client
        }

        fun newApJoiner(context: Context, listener: ApJoiner.Listener): ApJoiner {
            val owned = resources(context)
            owned.releaseApJoiner()
            val callbackGeneration = owned.nextApJoinerCallbackGeneration()
            lateinit var joiner: ApJoiner
            val fencedListener = object : ApJoiner.Listener {
                private fun current() = owned.acceptsApJoinerCallback(callbackGeneration) && owned.apJoiner === joiner
                override fun onLog(s: String) { if (current()) listener.onLog(s) }
                override fun onNetwork(network: Network, link: LinkProperties?) { if (current()) listener.onNetwork(network, link) }
                override fun onFailed(reason: String) { if (current()) listener.onFailed(reason) }
                override fun onLost() { if (current()) listener.onLost() }
            }
            joiner = ApJoiner(context.applicationContext, fencedListener)
            owned.apJoiner = joiner
            return joiner
        }

        /** Saved cameras are hints only; durable explicit stop always wins over auto-connect. */
        fun automaticCameraSelection(
            context: Context,
            recentSavedMacs: List<String>,
            advertisedMacs: Set<String>,
        ): String? {
            val session = runtime(context).snapshot()
            return AutomaticCameraAvailability.select(
                recentSavedMacs,
                advertisedMacs,
                userStopped = session.userStopped,
                alreadyConnecting = resources(context).connecting,
            )
        }

        fun host(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, CameraConnectionService::class.java).setAction(ACTION_START))
        }
        fun stopHost(context: Context) {
            context.startService(Intent(context, CameraConnectionService::class.java).setAction(ACTION_STOP))
        }
    }
}
