package net.sqlcipher.database;

import android.os.SystemClock;

public class SQLiteStatement extends SQLiteProgram {
    private final native long native_1x1_long();

    private final native String native_1x1_string();

    private final native void native_execute();

    SQLiteStatement(SQLiteDatabase db, String sql) {
        super(db, sql);
    }

    public void execute() {
        if (this.mDatabase.isOpen()) {
            long timeStart = SystemClock.uptimeMillis();
            this.mDatabase.lock();
            acquireReference();
            try {
                native_execute();
                this.mDatabase.logTimeStat(this.mSql, timeStart);
            } finally {
                releaseReference();
                this.mDatabase.unlock();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public long executeInsert() {
        if (this.mDatabase.isOpen()) {
            long timeStart = SystemClock.uptimeMillis();
            this.mDatabase.lock();
            acquireReference();
            try {
                native_execute();
                this.mDatabase.logTimeStat(this.mSql, timeStart);
                long lastInsertRow = this.mDatabase.lastChangeCount() > 0 ? this.mDatabase.lastInsertRow() : -1;
                releaseReference();
                this.mDatabase.unlock();
                return lastInsertRow;
            } catch (Throwable th) {
                releaseReference();
                this.mDatabase.unlock();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public int executeUpdateDelete() {
        if (this.mDatabase.isOpen()) {
            long timeStart = SystemClock.uptimeMillis();
            this.mDatabase.lock();
            acquireReference();
            try {
                native_execute();
                this.mDatabase.logTimeStat(this.mSql, timeStart);
                int lastChangeCount = this.mDatabase.lastChangeCount();
                return lastChangeCount;
            } finally {
                releaseReference();
                this.mDatabase.unlock();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public long simpleQueryForLong() {
        if (this.mDatabase.isOpen()) {
            long timeStart = SystemClock.uptimeMillis();
            this.mDatabase.lock();
            acquireReference();
            try {
                long retValue = native_1x1_long();
                this.mDatabase.logTimeStat(this.mSql, timeStart);
                return retValue;
            } finally {
                releaseReference();
                this.mDatabase.unlock();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public String simpleQueryForString() {
        if (this.mDatabase.isOpen()) {
            long timeStart = SystemClock.uptimeMillis();
            this.mDatabase.lock();
            acquireReference();
            try {
                String retValue = native_1x1_string();
                this.mDatabase.logTimeStat(this.mSql, timeStart);
                return retValue;
            } finally {
                releaseReference();
                this.mDatabase.unlock();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }
}
