package org.greenrobot.greendao.database;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class StandardDatabase implements Database {
    private final SQLiteDatabase delegate;

    public StandardDatabase(SQLiteDatabase delegate) {
        this.delegate = delegate;
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return this.delegate.rawQuery(sql, selectionArgs);
    }

    public void execSQL(String sql) throws SQLException {
        this.delegate.execSQL(sql);
    }

    public void beginTransaction() {
        this.delegate.beginTransaction();
    }

    public void endTransaction() {
        this.delegate.endTransaction();
    }

    public boolean inTransaction() {
        return this.delegate.inTransaction();
    }

    public void setTransactionSuccessful() {
        this.delegate.setTransactionSuccessful();
    }

    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        this.delegate.execSQL(sql, bindArgs);
    }

    public DatabaseStatement compileStatement(String sql) {
        return new StandardDatabaseStatement(this.delegate.compileStatement(sql));
    }

    public boolean isDbLockedByCurrentThread() {
        return this.delegate.isDbLockedByCurrentThread();
    }

    public void close() {
        this.delegate.close();
    }

    public Object getRawDatabase() {
        return this.delegate;
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return this.delegate;
    }
}
