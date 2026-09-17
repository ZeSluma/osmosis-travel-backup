package dev.konraditurbe.osmosis.ledger

import android.app.Instrumentation
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import dev.konraditurbe.osmosis.R
import dev.konraditurbe.osmosis.ui.BackupBadge
import java.util.Locale

/** Synthetic empty grid-cell layout only; no image loader, network, or media. */
object BackupDisplayInstrumentation {
    fun verify(instrumentation:Instrumentation) {
        var problem=false
        instrumentation.runOnMainSync {
            try {
                val config=Configuration(instrumentation.targetContext.resources.configuration).apply { setLocale(Locale.GERMAN) }
                val context=instrumentation.targetContext.createConfigurationContext(config)
                val cell=LayoutInflater.from(context).inflate(R.layout.item_media,null)
                val label=cell.findViewById<TextView>(R.id.backupState)
                label.visibility=View.VISIBLE
                val displayA=BackupDisplayPolicy.resolve("TRANSFERRED_UNVERIFIED","PRESENT_UNVERIFIED",true,100,100,true)
                BackupBadge.render(label,displayA)
                check(label.text.contains("unbestätigt"))
                val textA=label.text.toString()
                BackupBadge.render(label,BackupDisplayPolicy.resolve("PARTIAL","CHANGED",true,330769591,1164588515))
                check(label.text.contains("28%") && label.text.contains("Prüfung") && label.text.toString()!=textA)
                val density=context.resources.displayMetrics.density
                cell.measure(View.MeasureSpec.makeMeasureSpec((120*density).toInt(),View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec((140*density).toInt(),View.MeasureSpec.EXACTLY))
                cell.layout(0,0,cell.measuredWidth,cell.measuredHeight)
                check(label.height>0 && label.top>=36*density && label.bottom<=cell.height-19*density)
                BackupBadge.render(label,BackupDisplayPolicy.resolve("TRANSFERRED_UNVERIFIED","NOT_SCANNED",true,100,100,false))
                check(label.text.toString()==context.getString(R.string.backup_review))
            }catch(_:Throwable){problem=true}
        }
        check(!problem){"GATE3_BADGE_RENDER"}
    }
}
