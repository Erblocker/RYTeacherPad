package net.sqlcipher.database;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;

public class SQLiteDirectCursorDriver implements SQLiteCursorDriver {
    private Cursor mCursor;
    private SQLiteDatabase mDatabase;
    private String mEditTable;
    private SQLiteQuery mQuery;
    private String mSql;

    public SQLiteDirectCursorDriver(SQLiteDatabase db, String sql, String editTable) {
        this.mDatabase = db;
        this.mEditTable = editTable;
        this.mSql = sql;
    }

    public Cursor query(CursorFactory factory, String[] selectionArgs) {
        int numArgs = 0;
        SQLiteQuery query = new SQLiteQuery(this.mDatabase, this.mSql, 0, selectionArgs);
        if (selectionArgs != null) {
            numArgs = selectionArgs.length;
        }
        int i = 0;
        while (i < numArgs) {
            try {
                query.bindString(i + 1, selectionArgs[i]);
                i++;
            } catch (Throwable th) {
                if (query != null) {
                    query.close();
                }
            }
        }
        if (factory == null) {
            this.mCursor = new SQLiteCursor(this.mDatabase, this, this.mEditTable, query);
        } else {
            this.mCursor = factory.newCursor(this.mDatabase, this, this.mEditTable, query);
        }
        this.mQuery = query;
        query = null;
        Cursor cursor = this.mCursor;
        if (query != null) {
            query.close();
        }
        return cursor;
    }

    public void cursorClosed() {
        this.mCursor = null;
    }

    public void setBindArguments(String[] bindArgs) {
        int numArgs = bindArgs.length;
        for (int i = 0; i < numArgs; i++) {
            this.mQuery.bindString(i + 1, bindArgs[i]);
        }
    }

    public void cursorDeactivated() {
    }

    public void cursorRequeried(android.database.Cursor cursor) {
    }

    public String toString() {
        return "SQLiteDirectCursorDriver: " + this.mSql;
    }
}
