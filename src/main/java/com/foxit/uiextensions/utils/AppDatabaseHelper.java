package com.foxit.uiextensions.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.foxit.uiextensions.modules.signature.SignatureConstants;

class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Foxit_Rdk.db";
    private static final int DATABASE_VERSION = 3;

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        if (tableIsExist(db, SignatureConstants.getModelTableName()) && !isExistColumn(db, SignatureConstants.getModelTableName(), SignatureConstants.SG_DSG_PATH_FIELD)) {
            db.execSQL("ALTER TABLE " + SignatureConstants.getModelTableName() + " ADD COLUMN " + SignatureConstants.SG_DSG_PATH_FIELD + " VARCHAR");
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public synchronized boolean tableIsExist(SQLiteDatabase db, String tableName) {
        boolean z;
        if (db == null) {
            z = false;
        } else {
            z = false;
            if (tableName == null) {
                z = false;
            } else {
                try {
                    Cursor cursor = db.rawQuery("select count(*) as CNT from sqlite_master where type ='table' and name ='" + tableName.trim() + "'", null);
                    if (cursor.moveToNext() && cursor.getInt(0) > 0) {
                        z = true;
                    }
                } catch (Exception e) {
                }
            }
        }
        return z;
    }

    public synchronized boolean isExistColumn(SQLiteDatabase db, String tableName, String columnName) {
        boolean z;
        if (db == null) {
            z = false;
        } else {
            z = true;
            Cursor cursor = db.rawQuery("select * from " + tableName, null);
            if (cursor.getColumnIndex(columnName) == -1) {
                z = false;
            }
            cursor.close();
        }
        return z;
    }
}
