package net.sqlcipher.database;

import android.util.Log;

class SQLiteCompiledSql {
    private static final String TAG = "SQLiteCompiledSql";
    SQLiteDatabase mDatabase;
    private boolean mInUse = false;
    private String mSqlStmt = null;
    private Throwable mStackTrace = null;
    int nHandle = 0;
    int nStatement = 0;

    private final native void native_compile(String str);

    private final native void native_finalize();

    SQLiteCompiledSql(SQLiteDatabase db, String sql) {
        if (db.isOpen()) {
            this.mDatabase = db;
            this.mSqlStmt = sql;
            this.mStackTrace = new DatabaseObjectNotClosedException().fillInStackTrace();
            this.nHandle = db.mNativeHandle;
            compile(sql, true);
            return;
        }
        throw new IllegalStateException("database " + db.getPath() + " already closed");
    }

    private void compile(String sql, boolean forceCompilation) {
        if (!this.mDatabase.isOpen()) {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        } else if (forceCompilation) {
            this.mDatabase.lock();
            try {
                native_compile(sql);
            } finally {
                this.mDatabase.unlock();
            }
        }
    }

    void releaseSqlStatement() {
        if (this.nStatement != 0) {
            if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                Log.v(TAG, "closed and deallocated DbObj (id#" + this.nStatement + ")");
            }
            try {
                this.mDatabase.lock();
                native_finalize();
                this.nStatement = 0;
            } finally {
                this.mDatabase.unlock();
            }
        }
    }

    synchronized boolean acquire() {
        boolean z = true;
        synchronized (this) {
            if (this.mInUse) {
                z = false;
            } else {
                this.mInUse = true;
                if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                    Log.v(TAG, "Acquired DbObj (id#" + this.nStatement + ") from DB cache");
                }
            }
        }
        return z;
    }

    synchronized void release() {
        if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
            Log.v(TAG, "Released DbObj (id#" + this.nStatement + ") back to DB cache");
        }
        this.mInUse = false;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.nStatement != 0) {
                if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                    Log.v(TAG, "** warning ** Finalized DbObj (id#" + this.nStatement + ")");
                }
                int len = this.mSqlStmt.length();
                String str = TAG;
                StringBuilder append = new StringBuilder().append("Releasing statement in a finalizer. Please ensure that you explicitly call close() on your cursor: ");
                String str2 = this.mSqlStmt;
                if (len > 100) {
                    len = 100;
                }
                Log.w(str, append.append(str2.substring(0, len)).toString(), this.mStackTrace);
                releaseSqlStatement();
                super.finalize();
            }
        } finally {
            super.finalize();
        }
    }
}
