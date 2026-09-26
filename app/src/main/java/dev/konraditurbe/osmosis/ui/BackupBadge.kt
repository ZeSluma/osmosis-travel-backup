package dev.konraditurbe.osmosis.ui

import android.widget.TextView
import android.view.View
import android.widget.ProgressBar
import dev.konraditurbe.osmosis.R
import dev.konraditurbe.osmosis.ledger.*

object BackupBadge {
    fun render(view:TextView,display:BackupDisplay) {
        val resource=when(display.state){
            BackupDisplayState.NEW->R.string.backup_new
            BackupDisplayState.PARTIAL_REVIEW->R.string.backup_partial
            BackupDisplayState.LOCAL_INTEGRITY_CONFIRMED->R.string.backup_transferred
            BackupDisplayState.EXISTING_UNVERIFIED->R.string.backup_existing
            BackupDisplayState.REVIEW_REQUIRED->R.string.backup_review
        }
        view.text=if(display.state==BackupDisplayState.PARTIAL_REVIEW) view.context.getString(resource,display.percent)
            else view.context.getString(resource)
    }
    fun renderLive(view:TextView,bar:ProgressBar,live:dev.konraditurbe.osmosis.connection.LiveTransferFileProjection) {
        when(live.phase) {
            dev.konraditurbe.osmosis.connection.LiveTransferFileProjection.Phase.DOWNLOADING -> {
                view.text=view.context.getString(R.string.backup_live_phone,live.percent ?: 0)
                bar.visibility=View.VISIBLE
                bar.isIndeterminate=false
                bar.progress=live.percent ?: 0
            }
            dev.konraditurbe.osmosis.connection.LiveTransferFileProjection.Phase.INTEGRITY_SAVED -> {
                view.text=view.context.getString(R.string.backup_live_integrity_saved)
                bar.visibility=View.GONE
            }
            dev.konraditurbe.osmosis.connection.LiveTransferFileProjection.Phase.WAITING_FOR_CAMERA -> {
                view.text=view.context.getString(R.string.backup_live_camera_retry)
                // A retry is live camera work, but it has no byte percentage yet.  Keep an
                // indeterminate bar visible rather than leaving the row apparently idle.
                bar.visibility=View.VISIBLE
                bar.isIndeterminate=true
            }
            dev.konraditurbe.osmosis.connection.LiveTransferFileProjection.Phase.REVIEW_REQUIRED -> {
                view.text=view.context.getString(R.string.backup_live_review)
                bar.visibility=View.GONE
            }
        }
    }
}
