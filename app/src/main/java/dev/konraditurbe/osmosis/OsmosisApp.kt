package dev.konraditurbe.osmosis

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import dev.konraditurbe.osmosis.connection.CameraConnectionService
import dev.konraditurbe.osmosis.backup.ExternalReplicaCoordinator

/**
 * Starts the application-owned services. Pocket Pickup deliberately keeps its restrained brand palette
 * instead of inheriting potentially bright wallpaper colours from Material You. This is visual-only; it does
 * not change camera, transfer, storage, or ledger ownership.
 */
class OsmosisApp : Application() {
    private val externalStorageEvents=object:BroadcastReceiver() {
        override fun onReceive(context:Context,intent:Intent) {
            // A USB/media event is only a hint. The coordinator always re-probes the approved SAF tree.
            ExternalReplicaCoordinator.get(context).refreshAndReplicate()
        }
    }
    override fun onCreate() {
        super.onCreate()
        // Restore the application-owned, fenced session projection before an Activity can observe it.
        CameraConnectionService.runtime(this)
        // A configured SSD may reappear while the Pocket is off; resume only durable verified-phone work.
        ExternalReplicaCoordinator.get(this).refreshAndReplicate()
        val filter=IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED);addAction(Intent.ACTION_MEDIA_UNMOUNTED);addDataScheme("file")
            addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if(Build.VERSION.SDK_INT>=33) registerReceiver(externalStorageEvents,filter,Context.RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(externalStorageEvents,filter)
    }
}
