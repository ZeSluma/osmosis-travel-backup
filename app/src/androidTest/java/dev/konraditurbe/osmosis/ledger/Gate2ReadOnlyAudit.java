package dev.konraditurbe.osmosis.ledger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.json.JSONArray;
import org.json.JSONObject;

/** Separate test-APK diagnostic, never packaged in the production application.
 * Opens only the existing ledger read-only. Does not initialize Room, connect to
 * a camera, read media/preferences/credentials, or change app lifecycle/state.
 * Only this allowlisted projection leaves the device; paths are hashed locally.
 */
public final class Gate2ReadOnlyAudit {
    private static final String ROOT = "/data/user/0/dev.konraditurbe.osmosis/no_backup/";
    private static String opaque(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b : digest) out.append(String.format("%02x", b & 255));
        return out.toString();
    }
    // Never echo arbitrary stored strings, even if unexpected/corrupted.
    private static String token(String value, String allowed) {
        if (value != null) for (String item : allowed.split(","))
            if (value.equals(item)) return value;
        return "UNRECOGNIZED";
    }
    public static String project() {
        try {
            File file = new File(ROOT + "sync-ledger.db");
            JSONObject result = new JSONObject();
            result.put("database_exists", file.isFile());
            if (!file.isFile()) return result.toString();
            try (SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null,
                    SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS)) {
                if (!db.isReadOnly() || db.getVersion() != 2) throw new IllegalStateException();
                result.put("schema", 2);
                // One SELECT provides a consistent SQLite statement snapshot of
                // every projection; no multi-query race and no write transaction.
                String sql = "SELECT 'counts', "
                    + "(SELECT COUNT(*) FROM sources),(SELECT COUNT(*) FROM snapshots),"
                    + "(SELECT COUNT(*) FROM assets),(SELECT COUNT(*) FROM recordings),"
                    + "(SELECT COUNT(*) FROM members),(SELECT COUNT(*) FROM replicas),NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL "
                    + "UNION ALL SELECT 'asset',a.id,a.recordingId,p.relativePath,a.classification,"
                    + "a.identityAmbiguous,p.state,r.captureDay,r.timeSource,r.fallback,"
                    + "r.relationshipUncertain,a.size,a.remoteTime IS NOT NULL,a.strongVersion IS NOT NULL,"
                    + "EXISTS(SELECT 1 FROM membership m JOIN snapshots s ON m.snapshotId=s.id "
                    + "JOIN sources src ON s.sourceId=src.id WHERE m.assetId=a.id AND s.ownerEpoch=src.ownerEpoch),"
                    + "p.localLocator IS NOT NULL,p.committedLength "
                    + "FROM assets a JOIN recordings r ON a.recordingId=r.id "
                    + "JOIN replicas p ON p.assetId=a.id AND p.destination='PHONE_LOCAL' "
                    + "UNION ALL SELECT 'snapshot',id,status,scope,endedAt IS NOT NULL,"
                    + "(SELECT COUNT(*) FROM membership WHERE snapshotId=snapshots.id),"
                    + "NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL FROM snapshots";
                JSONArray assets = new JSONArray(), snapshots = new JSONArray();
                try (Cursor c = db.rawQuery(sql, null)) {
                    while (c.moveToNext()) {
                        JSONObject row = new JSONObject();
                        if (c.getString(0).equals("counts")) {
                            String[] names = {"sources","snapshots","assets","recordings","members","replicas"};
                            for (int i=0;i<names.length;i++) row.put(names[i],c.getLong(i+1));
                            result.put("counts",row);
                        } else if (c.getString(0).equals("asset")) {
                            row.put("asset",opaque(c.getString(1)));
                            row.put("recording",opaque(c.getString(2)));
                            row.put("destination_identity",opaque(c.getString(3)));
                            row.put("classification",token(c.getString(4),"KNOWN_REQUIRED,KNOWN_OPTIONAL,KNOWN_REGENERABLE_EXCLUDED,UNKNOWN_POTENTIALLY_REQUIRED,UNKNOWN_NON_RECORDING,UNSUPPORTED"));
                            row.put("identity_ambiguous",c.getInt(5)!=0);
                            row.put("state",token(c.getString(6),"DISCOVERED,PLANNED,PARTIAL,TRANSFERRED_UNVERIFIED,LOCAL_VERIFIED,FAILED,RETRY_PENDING,NEEDS_REVALIDATION"));
                            String day=c.getString(7);
                            row.put("capture_day",day!=null && day.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")?day:"UNRECOGNIZED");
                            row.put("time_source",token(c.getString(8),"SYNC_FALLBACK,CAMERA_CAPTURE,REMOTE_FILE,VERIFIED_FILENAME"));
                            row.put("fallback",token(c.getString(9),"SYNC_TIME_FALLBACK,NONE,SECONDARY_LOCAL_DAY,CAMERA_LOCAL_ZONE_UNKNOWN,UTC_DAY_FALLBACK"));
                            row.put("relationship_uncertain",c.getInt(10)!=0);
                            row.put("bytes",c.isNull(11)?JSONObject.NULL:c.getLong(11));
                            row.put("remote_time_present",c.getInt(12)!=0);
                            row.put("strong_version_present",c.getInt(13)!=0);
                            row.put("in_latest_source_snapshot",c.getInt(14)!=0);
                            row.put("local_locator_present",c.getInt(15)!=0);
                            row.put("committed_bytes",c.getLong(16));
                            // Call the actual production planner, using the same
                            // no-new-verification default as LedgerRepository.plan.
                            PlanAction action = SyncPlanner.INSTANCE.action(
                                AssetClass.valueOf(c.getString(4)),
                                TransferState.valueOf(c.getString(6)),c.getInt(5)!=0,false);
                            row.put("recomputed_planner_action",action==null?"NO_ACTION":action.name());
                            assets.put(row);
                        } else {
                            row.put("snapshot",opaque(c.getString(1)));
                            row.put("status",token(c.getString(2),"INCOMPLETE,COMPLETE"));
                            row.put("all_coverage_proven","pages=true;stores=true;members=true;stable=true".equals(c.getString(3)));
                            row.put("sealed",c.getInt(4)!=0);
                            row.put("members",c.getLong(5));
                            snapshots.put(row);
                        }
                    }
                }
                result.put("assets",assets).put("snapshots",snapshots);
                result.put("planner_evidence","PRODUCTION_POLICY_RECOMPUTED_FROM_PERSISTED_ROWS_NOT_LIVE_QUEUE");
            }
            return result.toString();
        } catch (Throwable ignored) {
            // Never print exception text, stack, SQL, filenames or stored values.
            return "{\"audit_status\":\"BLOCKED_READ_ONLY_PROJECTION\"}";
        }
    }
}
