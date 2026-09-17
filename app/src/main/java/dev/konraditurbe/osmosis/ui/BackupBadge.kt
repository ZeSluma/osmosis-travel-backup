package dev.konraditurbe.osmosis.ui

import android.widget.TextView
import dev.konraditurbe.osmosis.R
import dev.konraditurbe.osmosis.ledger.*

object BackupBadge {
    fun render(view:TextView,display:BackupDisplay) {
        val resource=when(display.state){
            BackupDisplayState.NEW->R.string.backup_new
            BackupDisplayState.PARTIAL_REVIEW->R.string.backup_partial
            BackupDisplayState.TRANSFERRED_UNVERIFIED->R.string.backup_transferred
            BackupDisplayState.EXISTING_UNVERIFIED->R.string.backup_existing
            BackupDisplayState.REVIEW_REQUIRED->R.string.backup_review
        }
        view.text=if(display.state==BackupDisplayState.PARTIAL_REVIEW) view.context.getString(resource,display.percent)
            else view.context.getString(resource)
    }
}
