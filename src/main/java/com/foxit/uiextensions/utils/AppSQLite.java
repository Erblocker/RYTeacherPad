package com.foxit.uiextensions.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Map.Entry;

public class AppSQLite {
    public static final String KEY_ID = "_id";
    public static final String KEY_TYPE_DATE = "DATE";
    public static final String KEY_TYPE_DOUBLE = "DOUBLE";
    public static final String KEY_TYPE_FLOAT = "FLOAT";
    public static final String KEY_TYPE_INT = "INTEGER";
    public static final String KEY_TYPE_VARCHAR = "VARCHAR";
    private static AppSQLite mAppSQLite = null;
    private Context mContext = null;
    private AppDatabaseHelper mDatabaseHelper = null;
    private int mRefCount = 0;
    private SQLiteDatabase mSQLiteDatabase = null;

    public static class FieldInfo {
        private String fieldName;
        private String fieldType;

        public FieldInfo(String name, String value) {
            this.fieldName = name;
            this.fieldType = value;
        }

        public String getFieldName() {
            return this.fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldType() {
            return this.fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }
    }

    public static AppSQLite getInstance(Context context) {
        if (mAppSQLite == null) {
            mAppSQLite = new AppSQLite(context);
        }
        return mAppSQLite;
    }

    public AppSQLite(Context context) {
        this.mContext = context;
        this.mDatabaseHelper = new AppDatabaseHelper(this.mContext);
    }

    public synchronized void openDB() throws SQLException {
        if (this.mSQLiteDatabase == null) {
            this.mSQLiteDatabase = this.mDatabaseHelper.getWritableDatabase();
        }
        this.mRefCount++;
    }

    public synchronized void closeDB() {
        if (this.mSQLiteDatabase != null) {
            this.mRefCount--;
            if (this.mRefCount == 0) {
                this.mDatabaseHelper.close();
                this.mSQLiteDatabase = null;
            }
        }
    }

    public synchronized boolean isDBOpened() {
        return this.mSQLiteDatabase != null;
    }

    public synchronized SQLiteDatabase getSQLiteDatabase() {
        if (this.mSQLiteDatabase == null || !this.mSQLiteDatabase.isOpen()) {
            openDB();
        }
        return this.mSQLiteDatabase;
    }

    public synchronized boolean createTable(String tableName, ArrayList<FieldInfo> fieldInfo) {
        boolean z;
        if (this.mSQLiteDatabase == null) {
            z = false;
        } else {
            int count = fieldInfo.size();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < count; i++) {
                String resultString;
                String name = ((FieldInfo) fieldInfo.get(i)).getFieldName();
                String type = ((FieldInfo) fieldInfo.get(i)).getFieldType();
                if (i != count - 1) {
                    resultString = new StringBuilder(String.valueOf(name)).append(" ").append(type).append(",").toString();
                } else {
                    resultString = new StringBuilder(String.valueOf(name)).append(" ").append(type).toString();
                }
                buffer.append(resultString);
            }
            this.mSQLiteDatabase.execSQL("CREATE TABLE  IF NOT EXISTS " + tableName + "(_id INTEGER PRIMARY KEY," + buffer + ")");
            z = true;
        }
        return z;
    }

    public synchronized boolean isTableExist(String tableName) {
        boolean z;
        if (this.mSQLiteDatabase == null) {
            z = false;
        } else {
            z = false;
            if (tableName == null) {
                z = false;
            } else {
                Cursor cursor = null;
                try {
                    cursor = this.mSQLiteDatabase.rawQuery("select count(*) as CNT from sqlite_master where type ='table' and name ='" + tableName.trim() + "'", null);
                    if (cursor.moveToNext() && cursor.getInt(0) > 0) {
                        cursor.close();
                        z = true;
                    }
                } catch (Exception e) {
                }
                cursor.close();
            }
        }
        return z;
    }

    public synchronized boolean isRowExist(String tableName, String fieldName, String[] matchValues) {
        boolean z;
        if (this.mSQLiteDatabase == null) {
            z = false;
        } else {
            z = true;
            Cursor cursor = this.mSQLiteDatabase.rawQuery("select " + fieldName + " from " + tableName + " where " + fieldName + " in(?)", matchValues);
            if (cursor.getCount() == 0) {
                z = false;
            }
            cursor.close();
        }
        return z;
    }

    public synchronized void insert(String tableName, ContentValues values) {
        if (this.mSQLiteDatabase != null) {
            this.mSQLiteDatabase.insert(tableName, KEY_ID, values);
        }
    }

    public synchronized void replace(String tableName, ContentValues values) {
        if (this.mSQLiteDatabase != null) {
            this.mSQLiteDatabase.replace(tableName, KEY_ID, values);
        }
    }

    public synchronized void delete(String tableName, String columnName, String[] matchValues) {
        if (this.mSQLiteDatabase != null) {
            this.mSQLiteDatabase.delete(tableName, new StringBuilder(String.valueOf(columnName)).append(" = ?").toString(), matchValues);
        }
    }

    public synchronized void delete(String tableName, ContentValues values) {
        if (this.mSQLiteDatabase != null) {
            for (Entry<String, Object> entry : values.valueSet()) {
                this.mSQLiteDatabase.delete(tableName, new StringBuilder(String.valueOf((String) entry.getKey())).append("= ?").toString(), new String[]{values.getAsString((String) entry.getKey())});
            }
        }
    }

    public synchronized Cursor select(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        Cursor cursor;
        if (this.mSQLiteDatabase == null) {
            cursor = null;
        } else {
            cursor = this.mSQLiteDatabase.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy);
        }
        return cursor;
    }

    public synchronized void update(String tableName, ContentValues values, String fieldName, String[] matchValues) {
        if (this.mSQLiteDatabase != null) {
            this.mSQLiteDatabase.update(tableName, values, new StringBuilder(String.valueOf(fieldName)).append(" = ? ").toString(), matchValues);
        }
    }
}
