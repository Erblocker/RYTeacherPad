package net.sqlcipher.database;

import android.os.SystemClock;
import android.util.Log;
import net.sqlcipher.CursorWindow;

public class SQLiteQuery extends SQLiteProgram {
    private static final String TAG = "Cursor";
    private String[] mBindArgs;
    private boolean mClosed = false;
    private int mOffsetIndex;

    private final native int native_column_count();

    private final native String native_column_name(int i);

    private final native int native_fill_window(CursorWindow cursorWindow, int i, int i2, int i3, int i4);

    SQLiteQuery(SQLiteDatabase db, String query, int offsetIndex, String[] bindArgs) {
        super(db, query);
        this.mOffsetIndex = offsetIndex;
        this.mBindArgs = bindArgs;
    }

    int fillWindow(CursorWindow window, int maxRead, int lastPos) {
        int numRows;
        long timeStart = SystemClock.uptimeMillis();
        this.mDatabase.lock();
        this.mDatabase.logTimeStat(this.mSql, timeStart, "GETLOCK:");
        try {
            acquireReference();
            window.acquireReference();
            numRows = native_fill_window(window, window.getStartPosition(), this.mOffsetIndex, maxRead, lastPos);
            if (SQLiteDebug.DEBUG_SQL_STATEMENTS) {
                Log.d(TAG, "fillWindow(): " + this.mSql);
            }
            this.mDatabase.logTimeStat(this.mSql, timeStart);
            window.releaseReference();
            releaseReference();
            this.mDatabase.unlock();
        } catch (IllegalStateException e) {
            numRows = 0;
            window.releaseReference();
            releaseReference();
            this.mDatabase.unlock();
        } catch (SQLiteDatabaseCorruptException e2) {
            this.mDatabase.onCorruption();
            throw e2;
        } catch (Throwable th) {
            window.releaseReference();
        }
        return numRows;
    }

    int columnCountLocked() {
        acquireReference();
        try {
            int native_column_count = native_column_count();
            return native_column_count;
        } finally {
            releaseReference();
        }
    }

    String columnNameLocked(int columnIndex) {
        acquireReference();
        try {
            String native_column_name = native_column_name(columnIndex);
            return native_column_name;
        } finally {
            releaseReference();
        }
    }

    public String toString() {
        return "SQLiteQuery: " + this.mSql;
    }

    public void close() {
        super.close();
        this.mClosed = true;
    }

    void requery() {
        if (this.mBindArgs != null) {
            int i = 0;
            while (i < len) {
                try {
                    super.bindString(i + 1, this.mBindArgs[i]);
                    i++;
                } catch (SQLiteMisuseException e) {
                    StringBuilder errMsg = new StringBuilder("mSql " + this.mSql);
                    for (String append : this.mBindArgs) {
                        errMsg.append(" ");
                        errMsg.append(append);
                    }
                    errMsg.append(" ");
                    throw new IllegalStateException(errMsg.toString(), e);
                }
            }
        }
    }

    public void bindNull(int index) {
        this.mBindArgs[index - 1] = null;
        if (!this.mClosed) {
            super.bindNull(index);
        }
    }

    public void bindLong(int index, long value) {
        this.mBindArgs[index - 1] = Long.toString(value);
        if (!this.mClosed) {
            super.bindLong(index, value);
        }
    }

    public void bindDouble(int index, double value) {
        this.mBindArgs[index - 1] = Double.toString(value);
        if (!this.mClosed) {
            super.bindDouble(index, value);
        }
    }

    public void bindString(int index, String value) {
        this.mBindArgs[index - 1] = value;
        if (!this.mClosed) {
            super.bindString(index, value);
        }
    }
}
