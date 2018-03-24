package com.foxit.uiextensions.security.certificate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;
import com.foxit.uiextensions.utils.AppSQLite;
import com.foxit.uiextensions.utils.AppSQLite.FieldInfo;
import java.util.ArrayList;
import java.util.List;

class CertificateDataSupport {
    private static final String DB_TABLE_CERT = "_cert";
    private static final String DB_TABLE_PFX = "_pfx";
    private static final String FILENAME = "file_name";
    private static final String FILEPATH = "file_path";
    public static final int FULLPERMCODE = 3900;
    private static final String ISSUER = "issuer";
    private static final String PASSWORD = "password";
    private static final String PUBLISHER = "publisher";
    private static final String SERIALNUMBER = "serial_number";
    private Context mContext;

    public CertificateDataSupport(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        if (!AppSQLite.getInstance(this.mContext).isTableExist(DB_TABLE_CERT)) {
            ArrayList<FieldInfo> fieldInfos = new ArrayList();
            fieldInfos.add(new FieldInfo(SERIALNUMBER, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(ISSUER, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo("publisher", AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(FILEPATH, AppSQLite.KEY_TYPE_VARCHAR));
            fieldInfos.add(new FieldInfo(FILENAME, AppSQLite.KEY_TYPE_VARCHAR));
            AppSQLite.getInstance(this.mContext).createTable(DB_TABLE_CERT, fieldInfos);
            fieldInfos.add(new FieldInfo(PASSWORD, AppSQLite.KEY_TYPE_VARCHAR));
            AppSQLite.getInstance(this.mContext).createTable(DB_TABLE_PFX, fieldInfos);
        }
    }

    public boolean insertCert(String issuer, String publisher, String serialNumber, String path, String fileName) {
        ContentValues values = new ContentValues();
        values.put(ISSUER, issuer);
        values.put("publisher", publisher);
        values.put(SERIALNUMBER, serialNumber);
        values.put(FILEPATH, path);
        values.put(FILENAME, fileName);
        AppSQLite.getInstance(this.mContext).insert(DB_TABLE_CERT, values);
        return true;
    }

    public boolean removeCert(String filePath) {
        String selection = FILEPATH;
        AppSQLite.getInstance(this.mContext).delete(DB_TABLE_CERT, selection, new String[]{filePath});
        return true;
    }

    public boolean insertPfx(String issuer, String publisher, String serialNumber, String path, String fileName, String password) {
        ContentValues values = new ContentValues();
        values.put(ISSUER, issuer);
        values.put("publisher", publisher);
        values.put(SERIALNUMBER, serialNumber);
        values.put(FILEPATH, path);
        values.put(FILENAME, fileName);
        values.put(PASSWORD, password);
        AppSQLite.getInstance(this.mContext).insert(DB_TABLE_PFX, values);
        return true;
    }

    public boolean removePfx(String filePath) {
        String selection = FILEPATH;
        AppSQLite.getInstance(this.mContext).delete(DB_TABLE_PFX, selection, new String[]{filePath});
        return true;
    }

    public SparseArray<String> getPfx(String issuer, String serialNumber) {
        SparseArray<String> sa = null;
        if (issuer == null || serialNumber == null) {
            return null;
        }
        String[] whereValue = new String[]{issuer, serialNumber};
        Cursor cursor = AppSQLite.getInstance(this.mContext).getSQLiteDatabase().query(DB_TABLE_PFX, new String[]{FILEPATH, FILENAME, PASSWORD}, "issuer=? AND serial_number=?", whereValue, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                sa = new SparseArray();
                sa.put(0, cursor.getString(cursor.getColumnIndex(FILEPATH)));
                sa.put(1, cursor.getString(cursor.getColumnIndex(FILENAME)));
                sa.put(2, cursor.getString(cursor.getColumnIndex(PASSWORD)));
            }
            cursor.close();
        }
        return sa;
    }

    public void setPfxPassword(String issuer, String serialNumber, String password) {
        ContentValues values = new ContentValues();
        values.put(PASSWORD, password);
        String[] whereValue = new String[]{issuer, serialNumber};
        AppSQLite.getInstance(this.mContext).getSQLiteDatabase().update(DB_TABLE_PFX, values, "issuer=? AND serial_number=?", whereValue);
    }

    public void getAllPfxs(List<CertificateFileInfo> infos) {
        Cursor cursor = AppSQLite.getInstance(this.mContext).select(DB_TABLE_PFX, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CertificateFileInfo info = new CertificateFileInfo();
                info.serialNumber = cursor.getString(cursor.getColumnIndex(SERIALNUMBER));
                info.issuer = cursor.getString(cursor.getColumnIndex(ISSUER));
                info.publisher = cursor.getString(cursor.getColumnIndex("publisher"));
                info.filePath = cursor.getString(cursor.getColumnIndex(FILEPATH));
                info.fileName = cursor.getString(cursor.getColumnIndex(FILENAME));
                info.password = cursor.getString(cursor.getColumnIndex(PASSWORD));
                info.isCertFile = false;
                info.permCode = 3900;
                infos.add(info);
            }
            cursor.close();
        }
    }

    public void getAllCerts(List<CertificateFileInfo> infos) {
        Cursor cursor = AppSQLite.getInstance(this.mContext).select(DB_TABLE_CERT, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CertificateFileInfo info = new CertificateFileInfo();
                info.serialNumber = cursor.getString(cursor.getColumnIndex(SERIALNUMBER));
                info.issuer = cursor.getString(cursor.getColumnIndex(ISSUER));
                info.publisher = cursor.getString(cursor.getColumnIndex("publisher"));
                info.filePath = cursor.getString(cursor.getColumnIndex(FILEPATH));
                info.fileName = cursor.getString(cursor.getColumnIndex(FILENAME));
                info.isCertFile = true;
                info.permCode = 3900;
                infos.add(info);
            }
            cursor.close();
        }
    }
}
