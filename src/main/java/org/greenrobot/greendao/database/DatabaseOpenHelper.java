package org.greenrobot.greendao.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteDatabase;

public abstract class DatabaseOpenHelper extends SQLiteOpenHelper {
    private final Context context;
    private EncryptedHelper encryptedHelper;
    private boolean loadSQLCipherNativeLibs;
    private final String name;
    private final int version;

    private class EncryptedHelper extends net.sqlcipher.database.SQLiteOpenHelper {
        public EncryptedHelper(Context context, String name, int version, boolean loadLibs) {
            super(context, name, null, version);
            if (loadLibs) {
                SQLiteDatabase.loadLibs(context);
            }
        }

        public void onCreate(SQLiteDatabase db) {
            DatabaseOpenHelper.this.onCreate(wrap(db));
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            DatabaseOpenHelper.this.onUpgrade(wrap(db), oldVersion, newVersion);
        }

        public void onOpen(SQLiteDatabase db) {
            DatabaseOpenHelper.this.onOpen(wrap(db));
        }

        protected Database wrap(SQLiteDatabase sqLiteDatabase) {
            return new EncryptedDatabase(sqLiteDatabase);
        }
    }

    public DatabaseOpenHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.loadSQLCipherNativeLibs = true;
        this.context = context;
        this.name = name;
        this.version = version;
    }

    public void setLoadSQLCipherNativeLibs(boolean loadSQLCipherNativeLibs) {
        this.loadSQLCipherNativeLibs = loadSQLCipherNativeLibs;
    }

    public Database getWritableDb() {
        return wrap(getWritableDatabase());
    }

    public Database getReadableDb() {
        return wrap(getReadableDatabase());
    }

    protected Database wrap(android.database.sqlite.SQLiteDatabase sqLiteDatabase) {
        return new StandardDatabase(sqLiteDatabase);
    }

    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        onCreate(wrap(db));
    }

    public void onCreate(Database db) {
    }

    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(wrap(db), oldVersion, newVersion);
    }

    public void onUpgrade(Database db, int oldVersion, int newVersion) {
    }

    public void onOpen(android.database.sqlite.SQLiteDatabase db) {
        onOpen(wrap(db));
    }

    public void onOpen(Database db) {
    }

    private EncryptedHelper checkEncryptedHelper() {
        if (this.encryptedHelper == null) {
            this.encryptedHelper = new EncryptedHelper(this.context, this.name, this.version, this.loadSQLCipherNativeLibs);
        }
        return this.encryptedHelper;
    }

    public Database getEncryptedWritableDb(String password) {
        EncryptedHelper encryptedHelper = checkEncryptedHelper();
        return encryptedHelper.wrap(encryptedHelper.getWritableDatabase(password));
    }

    public Database getEncryptedWritableDb(char[] password) {
        EncryptedHelper encryptedHelper = checkEncryptedHelper();
        return encryptedHelper.wrap(encryptedHelper.getWritableDatabase(password));
    }

    public Database getEncryptedReadableDb(String password) {
        EncryptedHelper encryptedHelper = checkEncryptedHelper();
        return encryptedHelper.wrap(encryptedHelper.getReadableDatabase(password));
    }

    public Database getEncryptedReadableDb(char[] password) {
        EncryptedHelper encryptedHelper = checkEncryptedHelper();
        return encryptedHelper.wrap(encryptedHelper.getReadableDatabase(password));
    }
}
