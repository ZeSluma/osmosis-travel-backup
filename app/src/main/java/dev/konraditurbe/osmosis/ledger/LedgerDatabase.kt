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
    IdentityObservationRow::class, TransferIntegrityRow::class, SourceEquivalenceRow::class, TransferAttemptRow::class,
    ResumeEvidenceRow::class, StorageDestinationRow::class, ReplicaIntegrityRow::class, ReplicaOperationRow::class], version = 9, exportSchema = true)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun ledger(): LedgerDao
    abstract fun integrity(): IntegrityDao
    abstract fun attempts(): AttemptDao
    abstract fun resumes(): ResumeDao
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
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS transfer_integrity (id TEXT NOT NULL, assetId TEXT NOT NULL, locator TEXT NOT NULL, localRevision TEXT NOT NULL, expectedBytes INTEGER NOT NULL, result TEXT NOT NULL, method TEXT NOT NULL, ownerEpoch INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(assetId) REFERENCES assets(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transfer_integrity_assetId ON transfer_integrity(assetId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS source_equivalence (id TEXT NOT NULL, transferId TEXT NOT NULL, sourceVersion TEXT NOT NULL, ownerEpoch INTEGER NOT NULL, method TEXT NOT NULL, PRIMARY KEY(id), FOREIGN KEY(transferId) REFERENCES transfer_integrity(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_source_equivalence_transferId ON source_equivalence(transferId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS transfer_attempts (id TEXT NOT NULL, assetId TEXT NOT NULL, ownerEpoch INTEGER NOT NULL, expectedBytes INTEGER NOT NULL, state TEXT NOT NULL, locator TEXT, checkpoint INTEGER NOT NULL, integrityId TEXT, PRIMARY KEY(id), FOREIGN KEY(assetId) REFERENCES assets(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transfer_attempts_assetId ON transfer_attempts(assetId)")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db:SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS resume_evidence (id TEXT NOT NULL, attemptId TEXT NOT NULL, locator TEXT NOT NULL, checkpoint INTEGER NOT NULL, sha256 TEXT NOT NULL, sourceId TEXT NOT NULL, assetId TEXT NOT NULL, sourceVersion TEXT NOT NULL, ownerEpoch INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(attemptId) REFERENCES transfer_attempts(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_resume_evidence_attemptId ON resume_evidence(attemptId)")
                // Historic partials retain data and journal state but acquire no invented prefix/version proof.
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db:SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS storage_destinations (id TEXT NOT NULL, treeUri TEXT NOT NULL, domain TEXT NOT NULL, state TEXT NOT NULL, lastValidatedAt TEXT, failure TEXT, PRIMARY KEY(id))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_storage_destinations_treeUri ON storage_destinations (treeUri)")
                db.execSQL("CREATE TABLE IF NOT EXISTS replica_integrity (id TEXT NOT NULL, assetId TEXT NOT NULL, destinationId TEXT NOT NULL, locator TEXT NOT NULL, bytes INTEGER NOT NULL, sha256 TEXT NOT NULL, state TEXT NOT NULL, ownerEpoch INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(assetId) REFERENCES assets(id) ON UPDATE NO ACTION ON DELETE NO ACTION, FOREIGN KEY(destinationId) REFERENCES storage_destinations(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_replica_integrity_assetId ON replica_integrity (assetId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_replica_integrity_destinationId ON replica_integrity (destinationId)")
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db:SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS replica_operations (id TEXT NOT NULL, assetId TEXT NOT NULL, destinationId TEXT NOT NULL, locator TEXT, expectedBytes INTEGER NOT NULL, phoneSha256 TEXT NOT NULL, state TEXT NOT NULL, checkpoint INTEGER NOT NULL, ownerEpoch INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(assetId) REFERENCES assets(id) ON UPDATE NO ACTION ON DELETE NO ACTION, FOREIGN KEY(destinationId) REFERENCES storage_destinations(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_replica_operations_assetId ON replica_operations (assetId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_replica_operations_destinationId ON replica_operations (destinationId)")
            }
        }
        fun open(context: Context, name: String = "sync-ledger.db"): LedgerDatabase {
            require(name.matches(Regex("[A-Za-z0-9._-]+")))
            return Room.databaseBuilder(context.applicationContext, LedgerDatabase::class.java,
                File(context.noBackupFilesDir, name).absolutePath)
                .openHelperFactory(PreserveCorruptDatabaseFactory())
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
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
