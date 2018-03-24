package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

public class DataSynchronizeDao extends AbstractDao<DataSynchronize, String> {
    public static final String TABLENAME = "DATA_SYNCHRONIZE";

    public static class Properties {
        public static final Property Clientid = new Property(1, String.class, DeviceOperationRESTServiceProvider.CLIENTID, false, "ClientID");
        public static final Property Contenttype = new Property(5, Integer.class, "contenttype", false, "ContentType");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Packagecontent = new Property(4, String.class, "packagecontent", false, "PackageContent");
        public static final Property Packagedate = new Property(3, Date.class, "packagedate", false, "PackageDate");
        public static final Property Packagedelete = new Property(6, Integer.class, "packagedelete", false, "PackageDelete");
        public static final Property Packageid = new Property(2, String.class, "packageid", false, "PackageID");
    }

    public DataSynchronizeDao(DaoConfig config) {
        super(config);
    }

    public DataSynchronizeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"DATA_SYNCHRONIZE\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"ClientID\" TEXT," + "\"PackageID\" TEXT," + "\"PackageDate\" INTEGER," + "\"PackageContent\" TEXT," + "\"ContentType\" INTEGER," + "\"PackageDelete\" INTEGER);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DATA_SYNCHRONIZE_ClientID ON \"DATA_SYNCHRONIZE\"" + " (\"ClientID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DATA_SYNCHRONIZE_PackageID ON \"DATA_SYNCHRONIZE\"" + " (\"PackageID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DATA_SYNCHRONIZE_PackageDate ON \"DATA_SYNCHRONIZE\"" + " (\"PackageDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DATA_SYNCHRONIZE_PackageContent ON \"DATA_SYNCHRONIZE\"" + " (\"PackageContent\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DATA_SYNCHRONIZE_ContentType ON \"DATA_SYNCHRONIZE\"" + " (\"ContentType\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DATA_SYNCHRONIZE_PackageDelete ON \"DATA_SYNCHRONIZE\"" + " (\"PackageDelete\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DATA_SYNCHRONIZE\"");
    }

    protected final void bindValues(DatabaseStatement stmt, DataSynchronize entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String clientid = entity.getClientid();
        if (clientid != null) {
            stmt.bindString(2, clientid);
        }
        String packageid = entity.getPackageid();
        if (packageid != null) {
            stmt.bindString(3, packageid);
        }
        Date packagedate = entity.getPackagedate();
        if (packagedate != null) {
            stmt.bindLong(4, packagedate.getTime());
        }
        String packagecontent = entity.getPackagecontent();
        if (packagecontent != null) {
            stmt.bindString(5, packagecontent);
        }
        Integer contenttype = entity.getContenttype();
        if (contenttype != null) {
            stmt.bindLong(6, (long) contenttype.intValue());
        }
        Integer packagedelete = entity.getPackagedelete();
        if (packagedelete != null) {
            stmt.bindLong(7, (long) packagedelete.intValue());
        }
    }

    protected final void bindValues(SQLiteStatement stmt, DataSynchronize entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String clientid = entity.getClientid();
        if (clientid != null) {
            stmt.bindString(2, clientid);
        }
        String packageid = entity.getPackageid();
        if (packageid != null) {
            stmt.bindString(3, packageid);
        }
        Date packagedate = entity.getPackagedate();
        if (packagedate != null) {
            stmt.bindLong(4, packagedate.getTime());
        }
        String packagecontent = entity.getPackagecontent();
        if (packagecontent != null) {
            stmt.bindString(5, packagecontent);
        }
        Integer contenttype = entity.getContenttype();
        if (contenttype != null) {
            stmt.bindLong(6, (long) contenttype.intValue());
        }
        Integer packagedelete = entity.getPackagedelete();
        if (packagedelete != null) {
            stmt.bindLong(7, (long) packagedelete.intValue());
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public DataSynchronize readEntity(Cursor cursor, int offset) {
        Integer num = null;
        String string = cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
        String string2 = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
        String string3 = cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2);
        Date date = cursor.isNull(offset + 3) ? null : new Date(cursor.getLong(offset + 3));
        String string4 = cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4);
        Integer valueOf = cursor.isNull(offset + 5) ? null : Integer.valueOf(cursor.getInt(offset + 5));
        if (!cursor.isNull(offset + 6)) {
            num = Integer.valueOf(cursor.getInt(offset + 6));
        }
        return new DataSynchronize(string, string2, string3, date, string4, valueOf, num);
    }

    public void readEntity(Cursor cursor, DataSynchronize entity, int offset) {
        Integer num = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setClientid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPackageid(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setPackagedate(cursor.isNull(offset + 3) ? null : new Date(cursor.getLong(offset + 3)));
        entity.setPackagecontent(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setContenttype(cursor.isNull(offset + 5) ? null : Integer.valueOf(cursor.getInt(offset + 5)));
        if (!cursor.isNull(offset + 6)) {
            num = Integer.valueOf(cursor.getInt(offset + 6));
        }
        entity.setPackagedelete(num);
    }

    protected final String updateKeyAfterInsert(DataSynchronize entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(DataSynchronize entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(DataSynchronize entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
