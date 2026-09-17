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
    MemberRow::class, MembershipRow::class, ReplicaRow::class, LocalCandidateRow::class, CaptureEvidenceRow::class,
    IdentityObservationRow::class], version = 5, exportSchema = true)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun ledger(): LedgerDao
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE replicas ADD COLUMN localLocator TEXT")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE replicas ADD COLUMN localPresence TEXT NOT NULL DEFAULT 'NOT_SCANNED'")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_candidates (assetId TEXT NOT NULL, locator TEXT NOT NULL, displayName TEXT NOT NULL, directory TEXT NOT NULL, bytes INTEGER, pending INTEGER NOT NULL, metadataVersion TEXT NOT NULL, evidence TEXT NOT NULL, confidence TEXT NOT NULL, status TEXT NOT NULL, observedAt TEXT NOT NULL, ownerEpoch INTEGER NOT NULL, PRIMARY KEY(assetId, locator), FOREIGN KEY(assetId) REFERENCES assets(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_candidates_locator ON local_candidates (locator)")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS capture_evidence (assetId TEXT NOT NULL, timestamp TEXT NOT NULL, source TEXT NOT NULL, zoneEvidence TEXT, captureDay TEXT NOT NULL, confidence TEXT NOT NULL, fallback TEXT NOT NULL, reservationDayConflict INTEGER NOT NULL, PRIMARY KEY(assetId), FOREIGN KEY(assetId) REFERENCES assets(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS identity_observations (id TEXT NOT NULL, sourceId TEXT NOT NULL, snapshotId TEXT, remotePath TEXT NOT NULL, storage TEXT NOT NULL, size INTEGER, remoteTime TEXT, mediaType TEXT NOT NULL, handleEvidence TEXT, strongVersion TEXT, status TEXT NOT NULL, resolvedAssetId TEXT, PRIMARY KEY(id), FOREIGN KEY(sourceId) REFERENCES sources(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_identity_observations_sourceId ON identity_observations(sourceId)")
                db.execSQL("INSERT INTO identity_observations SELECT 'legacy:' || id,sourceId,NULL,remotePath,storage,size,remoteTime,mediaType,handleEvidence,strongVersion,'UNRESOLVED',NULL FROM assets WHERE size IS NULL OR size<=0")
                // Retain every legacy asset, membership, replica and candidate row as history.
            }
        }
        fun open(context: Context, name: String = "sync-ledger.db"): LedgerDatabase {
            require(name.matches(Regex("[A-Za-z0-9._-]+")))
            return Room.databaseBuilder(context.applicationContext, LedgerDatabase::class.java,
                File(context.noBackupFilesDir, name).absolutePath)
                .openHelperFactory(PreserveCorruptDatabaseFactory())
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
