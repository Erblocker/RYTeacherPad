package net.sqlcipher.database;

import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.foxit.uiextensions.utils.AppSQLite;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import net.sqlcipher.AbstractWindowedCursor;
import net.sqlcipher.CursorWindow;
import net.sqlcipher.SQLException;

public class SQLiteCursor extends AbstractWindowedCursor {
    static final int NO_COUNT = -1;
    static final String TAG = "Cursor";
    private Map<String, Integer> mColumnNameMap;
    private String[] mColumns;
    private int mCount = -1;
    private int mCursorState = 0;
    private SQLiteDatabase mDatabase;
    private SQLiteCursorDriver mDriver;
    private String mEditTable;
    private int mInitialRead = Integer.MAX_VALUE;
    private ReentrantLock mLock = null;
    private int mMaxRead = Integer.MAX_VALUE;
    protected MainThreadNotificationHandler mNotificationHandler;
    private boolean mPendingData = false;
    private SQLiteQuery mQuery;
    private Throwable mStackTrace = new DatabaseObjectNotClosedException().fillInStackTrace();

    protected class MainThreadNotificationHandler extends Handler {
        protected MainThreadNotificationHandler() {
        }

        public void handleMessage(Message msg) {
            SQLiteCursor.this.notifyDataSetChange();
        }
    }

    private final class QueryThread implements Runnable {
        private final int mThreadState;

        QueryThread(int version) {
            this.mThreadState = version;
        }

        private void sendMessage() {
            if (SQLiteCursor.this.mNotificationHandler != null) {
                SQLiteCursor.this.mNotificationHandler.sendEmptyMessage(1);
                SQLiteCursor.this.mPendingData = false;
                return;
            }
            SQLiteCursor.this.mPendingData = true;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            CursorWindow cw = SQLiteCursor.this.mWindow;
            Process.setThreadPriority(Process.myTid(), 10);
            while (true) {
                SQLiteCursor.this.mLock.lock();
                if (SQLiteCursor.this.mCursorState != this.mThreadState) {
                    SQLiteCursor.this.mLock.unlock();
                    return;
                }
                try {
                    int count = SQLiteCursor.this.mQuery.fillWindow(cw, SQLiteCursor.this.mMaxRead, SQLiteCursor.this.mCount);
                    if (count == 0) {
                        SQLiteCursor.this.mLock.unlock();
                        return;
                    } else if (count == -1) {
                        SQLiteCursor.access$512(SQLiteCursor.this, SQLiteCursor.this.mMaxRead);
                        sendMessage();
                        SQLiteCursor.this.mLock.unlock();
                    } else {
                        SQLiteCursor.this.mCount = count;
                        sendMessage();
                        SQLiteCursor.this.mLock.unlock();
                        return;
                    }
                } catch (Exception e) {
                    return;
                } catch (Throwable th) {
                    SQLiteCursor.this.mLock.unlock();
                }
            }
        }
    }

    static /* synthetic */ int access$512(SQLiteCursor x0, int x1) {
        int i = x0.mCount + x1;
        x0.mCount = i;
        return i;
    }

    public void setLoadStyle(int initialRead, int maxRead) {
        this.mMaxRead = maxRead;
        this.mInitialRead = initialRead;
        this.mLock = new ReentrantLock(true);
    }

    private void queryThreadLock() {
        if (this.mLock != null) {
            this.mLock.lock();
        }
    }

    private void queryThreadUnlock() {
        if (this.mLock != null) {
            this.mLock.unlock();
        }
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (!(Integer.MAX_VALUE == this.mMaxRead && Integer.MAX_VALUE == this.mInitialRead) && this.mNotificationHandler == null) {
            queryThreadLock();
            try {
                this.mNotificationHandler = new MainThreadNotificationHandler();
                if (this.mPendingData) {
                    notifyDataSetChange();
                    this.mPendingData = false;
                }
                queryThreadUnlock();
            } catch (Throwable th) {
                queryThreadUnlock();
            }
        }
    }

    public SQLiteCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        this.mDatabase = db;
        this.mDriver = driver;
        this.mEditTable = editTable;
        this.mColumnNameMap = null;
        this.mQuery = query;
        try {
            db.lock();
            int columnCount = this.mQuery.columnCountLocked();
            this.mColumns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String columnName = this.mQuery.columnNameLocked(i);
                this.mColumns[i] = columnName;
                if (AppSQLite.KEY_ID.equals(columnName)) {
                    this.mRowIdColumnIndex = i;
                }
            }
        } finally {
            db.unlock();
        }
    }

    public SQLiteDatabase getDatabase() {
        return this.mDatabase;
    }

    public boolean onMove(int oldPosition, int newPosition) {
        if (this.mWindow == null || newPosition < this.mWindow.getStartPosition() || newPosition >= this.mWindow.getStartPosition() + this.mWindow.getNumRows()) {
            fillWindow(newPosition);
        }
        return true;
    }

    public int getCount() {
        if (this.mCount == -1) {
            fillWindow(0);
        }
        return this.mCount;
    }

    private void fillWindow(int startPos) {
        if (this.mWindow == null) {
            this.mWindow = new CursorWindow(true);
        } else {
            this.mCursorState++;
            queryThreadLock();
            try {
                this.mWindow.clear();
            } finally {
                queryThreadUnlock();
            }
        }
        this.mWindow.setStartPosition(startPos);
        this.mCount = this.mQuery.fillWindow(this.mWindow, this.mInitialRead, 0);
        if (this.mCount == -1) {
            this.mCount = this.mInitialRead + startPos;
            new Thread(new QueryThread(this.mCursorState), "query thread").start();
        }
    }

    public int getColumnIndex(String columnName) {
        if (this.mColumnNameMap == null) {
            String[] columns = this.mColumns;
            int columnCount = columns.length;
            HashMap<String, Integer> map = new HashMap(columnCount, 1.0f);
            for (int i = 0; i < columnCount; i++) {
                map.put(columns[i], Integer.valueOf(i));
            }
            this.mColumnNameMap = map;
        }
        int periodIndex = columnName.lastIndexOf(46);
        if (periodIndex != -1) {
            Log.e(TAG, "requesting column name with table name -- " + columnName, new Exception());
            columnName = columnName.substring(periodIndex + 1);
        }
        Integer i2 = (Integer) this.mColumnNameMap.get(columnName);
        if (i2 != null) {
            return i2.intValue();
        }
        return -1;
    }

    public boolean deleteRow() {
        SQLiteDatabase sQLiteDatabase = null;
        checkPosition();
        if (this.mRowIdColumnIndex == -1 || this.mCurrentRowID == null) {
            Log.e(TAG, "Could not delete row because either the row ID column is not available or ithas not been read.");
            return false;
        }
        boolean success;
        this.mDatabase.lock();
        try {
            this.mDatabase.delete(this.mEditTable, this.mColumns[this.mRowIdColumnIndex] + "=?", new String[]{this.mCurrentRowID.toString()});
            success = true;
        } catch (SQLException e) {
            success = false;
        }
        try {
            int pos = this.mPos;
            requery();
            moveToPosition(pos);
            if (!success) {
                return sQLiteDatabase;
            }
            onChange(true);
            return true;
        } finally {
            sQLiteDatabase = this.mDatabase;
            sQLiteDatabase.unlock();
        }
    }

    public String[] getColumnNames() {
        return this.mColumns;
    }

    public boolean supportsUpdates() {
        return !TextUtils.isEmpty(this.mEditTable);
    }

    public boolean commitUpdates(Map<? extends Long, ? extends Map<String, Object>> additionalValues) {
        if (supportsUpdates()) {
            synchronized (this.mUpdatedRows) {
                if (additionalValues != null) {
                    this.mUpdatedRows.putAll(additionalValues);
                }
                if (this.mUpdatedRows.size() == 0) {
                    return true;
                }
                this.mDatabase.beginTransaction();
                try {
                    StringBuilder sql = new StringBuilder(128);
                    for (Entry<Long, Map<String, Object>> rowEntry : this.mUpdatedRows.entrySet()) {
                        Map<String, Object> values = (Map) rowEntry.getValue();
                        Long rowIdObj = (Long) rowEntry.getKey();
                        if (rowIdObj == null || values == null) {
                            throw new IllegalStateException("null rowId or values found! rowId = " + rowIdObj + ", values = " + values);
                        } else if (values.size() != 0) {
                            long rowId = rowIdObj.longValue();
                            Iterator<Entry<String, Object>> valuesIter = values.entrySet().iterator();
                            sql.setLength(0);
                            sql.append("UPDATE " + this.mEditTable + " SET ");
                            Object[] bindings = new Object[values.size()];
                            int i = 0;
                            while (valuesIter.hasNext()) {
                                Entry<String, Object> entry = (Entry) valuesIter.next();
                                sql.append((String) entry.getKey());
                                sql.append("=?");
                                bindings[i] = entry.getValue();
                                if (valuesIter.hasNext()) {
                                    sql.append(", ");
                                }
                                i++;
                            }
                            sql.append(" WHERE " + this.mColumns[this.mRowIdColumnIndex] + '=' + rowId);
                            sql.append(';');
                            this.mDatabase.execSQL(sql.toString(), bindings);
                            this.mDatabase.rowUpdated(this.mEditTable, rowId);
                        }
                    }
                    this.mDatabase.setTransactionSuccessful();
                    this.mUpdatedRows.clear();
                    onChange(true);
                    return true;
                } finally {
                    this.mDatabase.endTransaction();
                }
            }
        } else {
            Log.e(TAG, "commitUpdates not supported on this cursor, did you include the _id column?");
            return false;
        }
    }

    private void deactivateCommon() {
        this.mCursorState = 0;
        if (this.mWindow != null) {
            this.mWindow.close();
            this.mWindow = null;
        }
    }

    public void deactivate() {
        super.deactivate();
        deactivateCommon();
        this.mDriver.cursorDeactivated();
    }

    public void close() {
        super.close();
        deactivateCommon();
        this.mQuery.close();
        this.mDriver.cursorClosed();
    }

    public boolean requery() {
        if (isClosed()) {
            return false;
        }
        this.mDatabase.lock();
        try {
            if (this.mWindow != null) {
                this.mWindow.clear();
            }
            this.mPos = -1;
            this.mDriver.cursorRequeried(this);
            this.mCount = -1;
            this.mCursorState++;
            queryThreadLock();
            this.mQuery.requery();
            queryThreadUnlock();
            this.mDatabase.unlock();
            return super.requery();
        } catch (Throwable th) {
            this.mDatabase.unlock();
        }
    }

    public void setWindow(CursorWindow window) {
        if (this.mWindow != null) {
            this.mCursorState++;
            queryThreadLock();
            try {
                this.mWindow.close();
                this.mCount = -1;
            } finally {
                queryThreadUnlock();
            }
        }
        this.mWindow = window;
    }

    public void setSelectionArguments(String[] selectionArgs) {
        this.mDriver.setBindArguments(selectionArgs);
    }

    protected void finalize() {
        try {
            if (this.mWindow != null) {
                int len = this.mQuery.mSql.length();
                String str = TAG;
                StringBuilder append = new StringBuilder().append("Finalizing a Cursor that has not been deactivated or closed. database = ").append(this.mDatabase.getPath()).append(", table = ").append(this.mEditTable).append(", query = ");
                String str2 = this.mQuery.mSql;
                if (len > 100) {
                    len = 100;
                }
                Log.e(str, append.append(str2.substring(0, len)).toString(), this.mStackTrace);
                close();
                SQLiteDebug.notifyActiveCursorFinalized();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void fillWindow(int startPos, android.database.CursorWindow window) {
        if (this.mWindow == null) {
            this.mWindow = new CursorWindow(true);
        } else {
            this.mCursorState++;
            queryThreadLock();
            try {
                this.mWindow.clear();
            } finally {
                queryThreadUnlock();
            }
        }
        this.mWindow.setStartPosition(startPos);
        this.mCount = this.mQuery.fillWindow(this.mWindow, this.mInitialRead, 0);
        if (this.mCount == -1) {
            this.mCount = this.mInitialRead + startPos;
            new Thread(new QueryThread(this.mCursorState), "query thread").start();
        }
    }
}
