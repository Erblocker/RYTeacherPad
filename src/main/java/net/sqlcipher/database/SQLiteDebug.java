package net.sqlcipher.database;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import java.util.ArrayList;

public final class SQLiteDebug {
    public static final boolean DEBUG_ACTIVE_CURSOR_FINALIZATION = Log.isLoggable("SQLiteCursorClosing", 2);
    public static final boolean DEBUG_LOCK_TIME_TRACKING = Log.isLoggable("SQLiteLockTime", 2);
    public static final boolean DEBUG_LOCK_TIME_TRACKING_STACK_TRACE = Log.isLoggable("SQLiteLockStackTrace", 2);
    public static final boolean DEBUG_SQL_CACHE = Log.isLoggable("SQLiteCompiledSql", 2);
    public static final boolean DEBUG_SQL_STATEMENTS = Log.isLoggable("SQLiteStatements", 2);
    public static final boolean DEBUG_SQL_TIME = Log.isLoggable("SQLiteTime", 2);
    private static int sNumActiveCursorsFinalized = 0;

    public static class DbStats {
        public String dbName;
        public long dbSize;
        public int lookaside;
        public long pageSize;

        public DbStats(String dbName, long pageCount, long pageSize, int lookaside) {
            this.dbName = dbName;
            this.pageSize = pageSize;
            this.dbSize = (pageCount * pageSize) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
            this.lookaside = lookaside;
        }
    }

    public static class PagerStats {
        @Deprecated
        public long databaseBytes;
        public ArrayList<DbStats> dbStats;
        public int largestMemAlloc;
        public int memoryUsed;
        @Deprecated
        public int numPagers;
        public int pageCacheOverflo;
        @Deprecated
        public long referencedBytes;
        @Deprecated
        public long totalBytes;
    }

    public static native long getHeapAllocatedSize();

    public static native void getHeapDirtyPages(int[] iArr);

    public static native long getHeapFreeSize();

    public static native long getHeapSize();

    public static native void getPagerStats(PagerStats pagerStats);

    public static PagerStats getDatabaseInfo() {
        PagerStats stats = new PagerStats();
        getPagerStats(stats);
        stats.dbStats = SQLiteDatabase.getDbStats();
        return stats;
    }

    public static int getNumActiveCursorsFinalized() {
        return sNumActiveCursorsFinalized;
    }

    static synchronized void notifyActiveCursorFinalized() {
        synchronized (SQLiteDebug.class) {
            sNumActiveCursorsFinalized++;
        }
    }
}
