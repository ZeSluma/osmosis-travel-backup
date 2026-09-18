package dev.konraditurbe.osmosis.connection

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.konraditurbe.osmosis.R
import dev.konraditurbe.osmosis.backup.AutonomousBackupRuntime

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
            ACTION_START -> { ensureChannel(); startForeground(NOTIFICATION_ID, notification()) }
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
        @Volatile private var resourcesInstance: CameraSessionResources? = null
        @Volatile private var backupRuntimeInstance: AutonomousBackupRuntime? = null
        fun runtime(context: Context): DurableSessionRuntime = instance ?: synchronized(this) {
            instance ?: DurableSessionRuntime(PreferenceSessionStore(context)).also { instance = it }
        }
        fun resources(context: Context): CameraSessionResources = resourcesInstance ?: synchronized(this) {
            resourcesInstance ?: CameraSessionResources().also { resourcesInstance = it }
        }
        /** Camera and local-replica scheduling belongs to the application owner, never an Activity. */
        fun backupRuntime(context: Context): AutonomousBackupRuntime = backupRuntimeInstance ?: synchronized(this) {
            backupRuntimeInstance ?: AutonomousBackupRuntime().also { backupRuntimeInstance = it }
        }
        fun host(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, CameraConnectionService::class.java).setAction(ACTION_START))
        }
        fun stopHost(context: Context) {
            context.startService(Intent(context, CameraConnectionService::class.java).setAction(ACTION_STOP))
        }
    }
}
