package com.foxit.uiextensions.modules.signature;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import com.foxit.uiextensions.utils.AppSQLite;
import com.foxit.uiextensions.utils.AppSQLite.FieldInfo;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

class SignatureDataUtil {
    private static final String BLOB = "_sg_bolb";
    private static final String BOTTOM = "_sg_bottom";
    private static final String[] COL = new String[]{NAME, BLOB};
    private static final String COLOR = "_color";
    private static final String DIAMETER = "_diamter";
    private static final String DSG_PATH = "_sg_dsgPath";
    private static final String LEFT = "_sg_left";
    private static final String NAME = "_sg_name";
    private static final String RIGHT = "_sg_right";
    private static final String TABLE_MODEL = SignatureConstants.getModelTableName();
    private static final String TABLE_RECENT = SignatureConstants.getRecentTableName();
    private static final String TOP = "_sg_top";
    private static boolean mInit = false;

    SignatureDataUtil() {
    }

    public static synchronized List<String> getModelKeys(Context context) {
        List<String> list;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                list = null;
                Cursor cursor = AppSQLite.getInstance(context).select(TABLE_MODEL, COL, null, null, null, null, "_id desc");
                if (cursor == null) {
                    list = null;
                } else {
                    int count = cursor.getCount();
                    if (count > 0) {
                        list = new ArrayList(count);
                        while (cursor.moveToNext()) {
                            list.add(cursor.getString(cursor.getColumnIndex(NAME)));
                        }
                    }
                    cursor.close();
                }
            } else {
                list = null;
            }
        }
        return list;
    }

    public static synchronized Bitmap getScaleBmpByKey(Context context, String key, int w, int h) {
        Bitmap bitmap;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                bitmap = null;
                Cursor cursor = AppSQLite.getInstance(context).select(TABLE_MODEL, null, "_sg_name=?", new String[]{key}, null, null, null);
                if (cursor == null) {
                    bitmap = null;
                } else {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        byte[] in = cursor.getBlob(cursor.getColumnIndex(BLOB));
                        Bitmap bmp = BitmapFactory.decodeByteArray(in, 0, in.length);
                        bitmap = Bitmap.createScaledBitmap(bmp, w, h, true);
                        bmp.recycle();
                    }
                    cursor.close();
                }
            } else {
                bitmap = null;
            }
        }
        return bitmap;
    }

    public static synchronized HashMap<String, Object> getBitmapByKey(Context context, String key) {
        HashMap<String, Object> hashMap;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                hashMap = null;
                Cursor cursor = AppSQLite.getInstance(context).select(TABLE_MODEL, null, "_sg_name=?", new String[]{key}, null, null, null);
                if (cursor == null) {
                    hashMap = null;
                } else {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        hashMap = new HashMap();
                        hashMap.put("key", key);
                        byte[] in = cursor.getBlob(cursor.getColumnIndex(BLOB));
                        Bitmap bmp = BitmapFactory.decodeByteArray(in, 0, in.length);
                        HashMap<String, Object> hashMap2 = hashMap;
                        hashMap2.put("color", Integer.valueOf(cursor.getInt(cursor.getColumnIndex(COLOR))));
                        hashMap.put("bitmap", bmp);
                        hashMap2 = hashMap;
                        hashMap2.put("diameter", Float.valueOf(cursor.getFloat(cursor.getColumnIndex(DIAMETER))));
                        int l = cursor.getInt(cursor.getColumnIndex(LEFT));
                        int t = cursor.getInt(cursor.getColumnIndex(TOP));
                        Rect rect = new Rect(l, t, cursor.getInt(cursor.getColumnIndex(RIGHT)), cursor.getInt(cursor.getColumnIndex(BOTTOM)));
                        hashMap.put("rect", rect);
                        if (cursor.getColumnIndex("_sg_dsgPath") != -1) {
                            try {
                                hashMap.put("dsgPath", cursor.getString(cursor.getColumnIndex("_sg_dsgPath")));
                            } catch (Exception e) {
                                hashMap.put("dsgPath", null);
                            }
                        } else {
                            hashMap.put("dsgPath", null);
                        }
                    }
                    cursor.close();
                }
            } else {
                hashMap = null;
            }
        }
        return hashMap;
    }

    public static synchronized boolean insertData(Context context, Bitmap bmp, Rect rect, int color, float diameter, String dsgPath) {
        boolean z;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                ContentValues values = new ContentValues();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bmp.compress(CompressFormat.PNG, 100, os);
                String key = UUID.randomUUID().toString();
                values.put(NAME, key);
                values.put(DIAMETER, Float.valueOf(diameter));
                values.put(COLOR, Integer.valueOf(color));
                values.put(LEFT, Integer.valueOf(rect.left));
                values.put(TOP, Integer.valueOf(rect.top));
                values.put(RIGHT, Integer.valueOf(rect.right));
                values.put(BOTTOM, Integer.valueOf(rect.bottom));
                values.put(BLOB, os.toByteArray());
                values.put("_sg_dsgPath", dsgPath);
                AppSQLite.getInstance(context).insert(TABLE_MODEL, values);
                insertRecent(context, key);
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public static synchronized boolean updateByKey(Context context, String key, Bitmap bmp, Rect rect, int color, float diameter, String dsgPath) {
        boolean z;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                if (isExistKey(context, TABLE_MODEL, key)) {
                    ContentValues values = new ContentValues();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    bmp.compress(CompressFormat.PNG, 100, os);
                    values.put(BLOB, os.toByteArray());
                    values.put(LEFT, Integer.valueOf(rect.left));
                    values.put(TOP, Integer.valueOf(rect.top));
                    values.put(RIGHT, Integer.valueOf(rect.right));
                    values.put(BOTTOM, Integer.valueOf(rect.bottom));
                    values.put(DIAMETER, Float.valueOf(diameter));
                    values.put(COLOR, Integer.valueOf(color));
                    values.put("_sg_dsgPath", dsgPath);
                    AppSQLite.getInstance(context).update(TABLE_MODEL, values, NAME, new String[]{key});
                    insertRecent(context, key);
                } else {
                    insertData(context, bmp, rect, color, diameter, dsgPath);
                }
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public static synchronized boolean deleteByKey(Context context, String table, String key) {
        boolean z = false;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                if (!table.equals(TABLE_RECENT) && isExistKey(context, TABLE_RECENT, key)) {
                    deleteByKey(context, TABLE_RECENT, key);
                }
                AppSQLite.getInstance(context).delete(table, NAME, new String[]{key});
                z = true;
            }
        }
        return z;
    }

    public static synchronized boolean insertRecent(Context context, String key) {
        boolean z;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                if (isExistKey(context, TABLE_RECENT, key)) {
                    deleteByKey(context, TABLE_RECENT, key);
                }
                ContentValues values = new ContentValues();
                values.put(NAME, key);
                AppSQLite.getInstance(context).insert(TABLE_RECENT, values);
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    public static synchronized List<String> getRecentKeys(Context context) {
        List<String> list;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                list = null;
                Cursor cursor = AppSQLite.getInstance(context).select(TABLE_RECENT, new String[]{NAME}, null, null, null, null, "_id desc");
                if (cursor == null) {
                    list = null;
                } else {
                    if (cursor.getCount() > 0) {
                        list = new ArrayList();
                        List<String> temp = new ArrayList();
                        while (cursor.moveToNext()) {
                            String key = cursor.getString(cursor.getColumnIndex(NAME));
                            if (isExistKey(context, TABLE_MODEL, key)) {
                                list.add(key);
                            } else {
                                temp.add(key);
                            }
                        }
                        if (temp.size() > 0) {
                            for (int i = 0; i < temp.size(); i++) {
                                deleteByKey(context, TABLE_RECENT, (String) temp.get(i));
                            }
                        }
                    }
                    cursor.close();
                }
            } else {
                list = null;
            }
        }
        return list;
    }

    public static synchronized HashMap<String, Object> getRecentData(Context context) {
        HashMap<String, Object> map;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                map = null;
                List<String> list = getRecentKeys(context);
                if (list != null && list.size() > 0) {
                    map = getBitmapByKey(context, (String) list.get(0));
                }
            } else {
                map = null;
            }
        }
        return map;
    }

    public static synchronized HashMap<String, Object> getRecentNormalSignData(Context context) {
        HashMap<String, Object> map;
        synchronized (SignatureDataUtil.class) {
            if (checkInit(context)) {
                List<String> list = getRecentKeys(context);
                if (list != null && list.size() > 0) {
                    for (String key : list) {
                        map = getBitmapByKey(context, key);
                        if (map.get("dsgPath") == null) {
                            insertRecent(context, key);
                            break;
                        }
                    }
                }
                map = null;
            } else {
                map = null;
            }
        }
        return map;
    }

    private static synchronized boolean isExistKey(Context context, String table, String key) {
        boolean isRowExist;
        synchronized (SignatureDataUtil.class) {
            isRowExist = AppSQLite.getInstance(context).isRowExist(table, NAME, new String[]{key});
        }
        return isRowExist;
    }

    private static synchronized boolean checkInit(Context context) {
        boolean z;
        synchronized (SignatureDataUtil.class) {
            if (!AppSQLite.getInstance(context).isDBOpened()) {
                AppSQLite.getInstance(context).openDB();
            }
            if (!mInit) {
                mInit = init(context);
            }
            z = mInit;
        }
        return z;
    }

    private static boolean init(Context context) {
        return createModelTable(context) && createRecentTable(context);
    }

    private static boolean createModelTable(Context context) {
        ArrayList<FieldInfo> tableList = new ArrayList();
        tableList.add(new FieldInfo(NAME, AppSQLite.KEY_TYPE_VARCHAR));
        tableList.add(new FieldInfo(LEFT, AppSQLite.KEY_TYPE_INT));
        tableList.add(new FieldInfo(TOP, AppSQLite.KEY_TYPE_INT));
        tableList.add(new FieldInfo(RIGHT, AppSQLite.KEY_TYPE_INT));
        tableList.add(new FieldInfo(BOTTOM, AppSQLite.KEY_TYPE_INT));
        tableList.add(new FieldInfo(COLOR, AppSQLite.KEY_TYPE_INT));
        tableList.add(new FieldInfo(DIAMETER, AppSQLite.KEY_TYPE_FLOAT));
        tableList.add(new FieldInfo(BLOB, "BLOB"));
        tableList.add(new FieldInfo("_sg_dsgPath", AppSQLite.KEY_TYPE_VARCHAR));
        return AppSQLite.getInstance(context).createTable(TABLE_MODEL, tableList);
    }

    private static boolean createRecentTable(Context context) {
        ArrayList<FieldInfo> tableList = new ArrayList();
        tableList.add(new FieldInfo(NAME, AppSQLite.KEY_TYPE_VARCHAR));
        return AppSQLite.getInstance(context).createTable(TABLE_RECENT, tableList);
    }
}
