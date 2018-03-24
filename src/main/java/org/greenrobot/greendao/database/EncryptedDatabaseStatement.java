package org.greenrobot.greendao.database;

import net.sqlcipher.database.SQLiteStatement;

public class EncryptedDatabaseStatement implements DatabaseStatement {
    private final SQLiteStatement delegate;

    public EncryptedDatabaseStatement(SQLiteStatement delegate) {
        this.delegate = delegate;
    }

    public void execute() {
        this.delegate.execute();
    }

    public long simpleQueryForLong() {
        return this.delegate.simpleQueryForLong();
    }

    public void bindNull(int index) {
        this.delegate.bindNull(index);
    }

    public long executeInsert() {
        return this.delegate.executeInsert();
    }

    public void bindString(int index, String value) {
        this.delegate.bindString(index, value);
    }

    public void bindBlob(int index, byte[] value) {
        this.delegate.bindBlob(index, value);
    }

    public void bindLong(int index, long value) {
        this.delegate.bindLong(index, value);
    }

    public void clearBindings() {
        this.delegate.clearBindings();
    }

    public void bindDouble(int index, double value) {
        this.delegate.bindDouble(index, value);
    }

    public void close() {
        this.delegate.close();
    }

    public Object getRawStatement() {
        return this.delegate;
    }
}
