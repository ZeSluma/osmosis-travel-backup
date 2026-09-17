package dev.konraditurbe.osmosis.ledger

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import java.io.File

@Database(entities = [SourceRow::class, SnapshotRow::class, RecordingRow::class, AssetRow::class,
    MemberRow::class, MembershipRow::class, ReplicaRow::class], version = 2, exportSchema = true)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun ledger(): LedgerDao
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE replicas ADD COLUMN localLocator TEXT")
            }
        }
        fun open(context: Context, name: String = "sync-ledger.db"): LedgerDatabase {
            require(name.matches(Regex("[A-Za-z0-9._-]+")))
            return Room.databaseBuilder(context.applicationContext, LedgerDatabase::class.java,
                File(context.noBackupFilesDir, name).absolutePath)
                .openHelperFactory(PreserveCorruptDatabaseFactory())
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}

/** Default SQLite corruption recovery deletes files. Preserve evidence and fail closed instead. */
internal class PreserveCorruptDatabaseFactory : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        val delegate = configuration.callback
        val guarded = object : SupportSQLiteOpenHelper.Callback(delegate.version) {
            override fun onCreate(db: SupportSQLiteDatabase) = delegate.onCreate(db)
            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = delegate.onUpgrade(db, oldVersion, newVersion)
            override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = delegate.onDowngrade(db, oldVersion, newVersion)
            override fun onConfigure(db: SupportSQLiteDatabase) = delegate.onConfigure(db)
            override fun onOpen(db: SupportSQLiteDatabase) = delegate.onOpen(db)
            override fun onCorruption(db: SupportSQLiteDatabase) {
                runCatching { db.close() }
                throw IllegalStateException("LEDGER_CORRUPTION_PRESERVED")
            }
        }
        return FrameworkSQLiteOpenHelperFactory().create(SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
            .name(configuration.name).callback(guarded).build())
    }
}
