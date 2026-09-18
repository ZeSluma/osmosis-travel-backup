package dev.konraditurbe.osmosis

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.google.android.material.color.DynamicColors
import dev.konraditurbe.osmosis.connection.CameraConnectionService
import dev.konraditurbe.osmosis.backup.ExternalReplicaCoordinator

/**
 * Applies Material You **dynamic color** to every Activity on Android 12+ (API 31), so the app's accent,
 * surfaces and background follow the user's system/wallpaper palette. It's a no-op below API 31, where the
 * static [dev.konraditurbe.osmosis.R.style] theme (teal accent, cream light / neutral dark) is used instead
 * — keeping everything working back to Android 10 (minSdk 29).
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
        DynamicColors.applyToActivitiesIfAvailable(this)
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
