package net.sqlcipher;

import android.util.Log;
import java.io.File;
import net.sqlcipher.database.SQLiteDatabase;

public final class DefaultDatabaseErrorHandler implements DatabaseErrorHandler {
    private static final String TAG = "DefaultDatabaseErrorHandler";

    public void onCorruption(SQLiteDatabase dbObj) {
        Log.e(TAG, "Corruption reported by sqlite on database, deleting: " + dbObj.getPath());
        if (dbObj.isOpen()) {
            Log.e(TAG, "Database object for corrupted database is already open, closing");
            try {
                dbObj.close();
            } catch (Exception e) {
                Log.e(TAG, "Exception closing Database object for corrupted database, ignored", e);
            }
        }
        deleteDatabaseFile(dbObj.getPath());
    }

    private void deleteDatabaseFile(String fileName) {
        if (!fileName.equalsIgnoreCase(":memory:") && fileName.trim().length() != 0) {
            Log.e(TAG, "deleting the database file: " + fileName);
            try {
                new File(fileName).delete();
            } catch (Exception e) {
                Log.w(TAG, "delete failed: " + e.getMessage());
            }
        }
    }
}
