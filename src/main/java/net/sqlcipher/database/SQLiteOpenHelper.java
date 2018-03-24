package net.sqlcipher.database;

import android.content.Context;
import android.util.Log;
import java.io.File;
import net.sqlcipher.DatabaseErrorHandler;
import net.sqlcipher.DefaultDatabaseErrorHandler;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;

public abstract class SQLiteOpenHelper {
    private static final String TAG = SQLiteOpenHelper.class.getSimpleName();
    private final Context mContext;
    private SQLiteDatabase mDatabase;
    private final DatabaseErrorHandler mErrorHandler;
    private final CursorFactory mFactory;
    private final SQLiteDatabaseHook mHook;
    private boolean mIsInitializing;
    private final String mName;
    private final int mNewVersion;

    public abstract void onCreate(SQLiteDatabase sQLiteDatabase);

    public abstract void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

    public SQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        this(context, name, factory, version, null, new DefaultDatabaseErrorHandler());
    }

    public SQLiteOpenHelper(Context context, String name, CursorFactory factory, int version, SQLiteDatabaseHook hook) {
        this(context, name, factory, version, hook, new DefaultDatabaseErrorHandler());
    }

    public SQLiteOpenHelper(Context context, String name, CursorFactory factory, int version, SQLiteDatabaseHook hook, DatabaseErrorHandler errorHandler) {
        this.mDatabase = null;
        this.mIsInitializing = false;
        if (version < 1) {
            throw new IllegalArgumentException("Version must be >= 1, was " + version);
        } else if (errorHandler == null) {
            throw new IllegalArgumentException("DatabaseErrorHandler param value can't be null.");
        } else {
            this.mContext = context;
            this.mName = name;
            this.mFactory = factory;
            this.mNewVersion = version;
            this.mHook = hook;
            this.mErrorHandler = errorHandler;
        }
    }

    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        return getWritableDatabase(password.toCharArray());
    }

    public synchronized SQLiteDatabase getWritableDatabase(char[] password) {
        SQLiteDatabase sQLiteDatabase;
        if (this.mDatabase != null && this.mDatabase.isOpen() && !this.mDatabase.isReadOnly()) {
            sQLiteDatabase = this.mDatabase;
        } else if (this.mIsInitializing) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        } else {
            sQLiteDatabase = null;
            if (this.mDatabase != null) {
                this.mDatabase.lock();
            }
            try {
                this.mIsInitializing = true;
                if (this.mName == null) {
                    sQLiteDatabase = SQLiteDatabase.create(null, password);
                } else {
                    String path = this.mContext.getDatabasePath(this.mName).getPath();
                    File dbPathFile = new File(path);
                    if (!dbPathFile.exists()) {
                        dbPathFile.getParentFile().mkdirs();
                    }
                    sQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(path, password, this.mFactory, this.mHook, this.mErrorHandler);
                }
                int version = sQLiteDatabase.getVersion();
                if (version != this.mNewVersion) {
                    sQLiteDatabase.beginTransaction();
                    if (version == 0) {
                        onCreate(sQLiteDatabase);
                    } else {
                        onUpgrade(sQLiteDatabase, version, this.mNewVersion);
                    }
                    sQLiteDatabase.setVersion(this.mNewVersion);
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                }
                onOpen(sQLiteDatabase);
                this.mIsInitializing = false;
                if (true) {
                    if (this.mDatabase != null) {
                        try {
                            this.mDatabase.close();
                        } catch (Exception e) {
                        }
                        this.mDatabase.unlock();
                    }
                    this.mDatabase = sQLiteDatabase;
                } else {
                    if (this.mDatabase != null) {
                        this.mDatabase.unlock();
                    }
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    }
                }
            } catch (Throwable th) {
                this.mIsInitializing = false;
                if (false) {
                    if (this.mDatabase != null) {
                        try {
                            this.mDatabase.close();
                        } catch (Exception e2) {
                        }
                        this.mDatabase.unlock();
                    }
                    this.mDatabase = sQLiteDatabase;
                } else {
                    if (this.mDatabase != null) {
                        this.mDatabase.unlock();
                    }
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    }
                }
            }
        }
        return sQLiteDatabase;
    }

    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        return getReadableDatabase(password.toCharArray());
    }

    public synchronized SQLiteDatabase getReadableDatabase(char[] password) {
        SQLiteDatabase sQLiteDatabase;
        SQLiteDatabase db;
        if (this.mDatabase != null && this.mDatabase.isOpen()) {
            sQLiteDatabase = this.mDatabase;
        } else if (this.mIsInitializing) {
            throw new IllegalStateException("getReadableDatabase called recursively");
        } else {
            try {
                sQLiteDatabase = getWritableDatabase(password);
            } catch (SQLiteException e) {
                if (this.mName == null) {
                    throw e;
                }
                boolean z = TAG;
                Log.e(z, "Couldn't open " + this.mName + " for writing (will try read-only):", e);
                db = null;
                this.mIsInitializing = z;
                String path = this.mContext.getDatabasePath(this.mName).getPath();
                File databasePath = new File(path);
                File databasesDirectory = new File(this.mContext.getDatabasePath(this.mName).getParent());
                if (!databasesDirectory.exists()) {
                    databasesDirectory.mkdirs();
                }
                z = databasePath.exists();
                if (!z) {
                    this.mIsInitializing = z;
                    db = getWritableDatabase(password);
                    this.mIsInitializing = true;
                    db.close();
                }
                db = SQLiteDatabase.openDatabase(path, password, this.mFactory, 1);
                if (db.getVersion() != this.mNewVersion) {
                    throw new SQLiteException("Can't upgrade read-only database from version " + db.getVersion() + " to " + this.mNewVersion + ": " + path);
                }
                onOpen(db);
                Log.w(TAG, "Opened " + this.mName + " in read-only mode");
                this.mDatabase = db;
                sQLiteDatabase = this.mDatabase;
                this.mIsInitializing = false;
                if (!(db == null || db == this.mDatabase)) {
                    db.close();
                }
            } finally {
                this.mIsInitializing = false;
                if (!(db == null || db == this.mDatabase)) {
                    db.close();
                }
            }
        }
        return sQLiteDatabase;
    }

    public synchronized void close() {
        if (this.mIsInitializing) {
            throw new IllegalStateException("Closed during initialization");
        } else if (this.mDatabase != null && this.mDatabase.isOpen()) {
            this.mDatabase.close();
            this.mDatabase = null;
        }
    }

    public void onOpen(SQLiteDatabase db) {
    }
}
