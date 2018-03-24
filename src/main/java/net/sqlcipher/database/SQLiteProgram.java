package net.sqlcipher.database;

import android.util.Log;
import org.apache.http.client.methods.HttpDelete;

public abstract class SQLiteProgram extends SQLiteClosable {
    private static final String TAG = "SQLiteProgram";
    private SQLiteCompiledSql mCompiledSql;
    @Deprecated
    protected SQLiteDatabase mDatabase;
    final String mSql;
    @Deprecated
    protected int nHandle = 0;
    @Deprecated
    protected int nStatement = 0;

    private final native void native_clear_bindings();

    protected final native void native_bind_blob(int i, byte[] bArr);

    protected final native void native_bind_double(int i, double d);

    protected final native void native_bind_long(int i, long j);

    protected final native void native_bind_null(int i);

    protected final native void native_bind_string(int i, String str);

    @Deprecated
    protected final native void native_compile(String str);

    @Deprecated
    protected final native void native_finalize();

    SQLiteProgram(SQLiteDatabase db, String sql) {
        this.mDatabase = db;
        this.mSql = sql.trim();
        db.acquireReference();
        db.addSQLiteClosable(this);
        this.nHandle = db.mNativeHandle;
        String prefixSql = this.mSql.substring(0, 6);
        if (prefixSql.equalsIgnoreCase("INSERT") || prefixSql.equalsIgnoreCase("UPDATE") || prefixSql.equalsIgnoreCase("REPLAC") || prefixSql.equalsIgnoreCase(HttpDelete.METHOD_NAME) || prefixSql.equalsIgnoreCase("SELECT")) {
            this.mCompiledSql = db.getCompiledStatementForSql(sql);
            if (this.mCompiledSql == null) {
                this.mCompiledSql = new SQLiteCompiledSql(db, sql);
                this.mCompiledSql.acquire();
                db.addToCompiledQueries(sql, this.mCompiledSql);
                if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                    Log.v(TAG, "Created DbObj (id#" + this.mCompiledSql.nStatement + ") for sql: " + sql);
                }
            } else if (!this.mCompiledSql.acquire()) {
                int last = this.mCompiledSql.nStatement;
                this.mCompiledSql = new SQLiteCompiledSql(db, sql);
                if (SQLiteDebug.DEBUG_ACTIVE_CURSOR_FINALIZATION) {
                    Log.v(TAG, "** possible bug ** Created NEW DbObj (id#" + this.mCompiledSql.nStatement + ") because the previously created DbObj (id#" + last + ") was not released for sql:" + sql);
                }
            }
            this.nStatement = this.mCompiledSql.nStatement;
            return;
        }
        this.mCompiledSql = new SQLiteCompiledSql(db, sql);
        this.nStatement = this.mCompiledSql.nStatement;
    }

    protected void onAllReferencesReleased() {
        releaseCompiledSqlIfNotInCache();
        this.mDatabase.releaseReference();
        this.mDatabase.removeSQLiteClosable(this);
    }

    protected void onAllReferencesReleasedFromContainer() {
        releaseCompiledSqlIfNotInCache();
        this.mDatabase.releaseReference();
    }

    private void releaseCompiledSqlIfNotInCache() {
        if (this.mCompiledSql != null) {
            synchronized (this.mDatabase.mCompiledQueries) {
                if (this.mDatabase.mCompiledQueries.containsValue(this.mCompiledSql)) {
                    this.mCompiledSql.release();
                } else {
                    this.mCompiledSql.releaseSqlStatement();
                    this.mCompiledSql = null;
                    this.nStatement = 0;
                }
            }
        }
    }

    public final int getUniqueId() {
        return this.nStatement;
    }

    String getSqlString() {
        return this.mSql;
    }

    @Deprecated
    protected void compile(String sql, boolean forceCompilation) {
    }

    public void bindNull(int index) {
        if (this.mDatabase.isOpen()) {
            acquireReference();
            try {
                native_bind_null(index);
            } finally {
                releaseReference();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public void bindLong(int index, long value) {
        if (this.mDatabase.isOpen()) {
            acquireReference();
            try {
                native_bind_long(index, value);
            } finally {
                releaseReference();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public void bindDouble(int index, double value) {
        if (this.mDatabase.isOpen()) {
            acquireReference();
            try {
                native_bind_double(index, value);
            } finally {
                releaseReference();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public void bindString(int index, String value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        } else if (this.mDatabase.isOpen()) {
            acquireReference();
            try {
                native_bind_string(index, value);
            } finally {
                releaseReference();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public void bindBlob(int index, byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        } else if (this.mDatabase.isOpen()) {
            acquireReference();
            try {
                native_bind_blob(index, value);
            } finally {
                releaseReference();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public void clearBindings() {
        if (this.mDatabase.isOpen()) {
            acquireReference();
            try {
                native_clear_bindings();
            } finally {
                releaseReference();
            }
        } else {
            throw new IllegalStateException("database " + this.mDatabase.getPath() + " already closed");
        }
    }

    public void close() {
        if (this.mDatabase.isOpen()) {
            this.mDatabase.lock();
            try {
                releaseReference();
            } finally {
                this.mDatabase.unlock();
            }
        }
    }
}
