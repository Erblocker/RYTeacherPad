package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import io.vov.vitamio.MediaMetadataRetriever;
import java.util.Date;
import org.apache.http.protocol.HTTP;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

public class IMMessagesDao extends AbstractDao<IMMessages, String> {
    public static final String TABLENAME = "IMMESSAGES";

    public static class Properties {
        public static final Property Content = new Property(7, String.class, "content", false, "Content");
        public static final Property Date = new Property(3, Date.class, MediaMetadataRetriever.METADATA_KEY_DATE, false, HTTP.DATE_HEADER);
        public static final Property Expiredate = new Property(5, Date.class, "expiredate", false, "ExpireDate");
        public static final Property Fromclientid = new Property(1, String.class, "fromclientid", false, "FromClientID");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Receivedate = new Property(4, Date.class, "receivedate", false, "ReceiveDate");
        public static final Property Receiveip = new Property(8, String.class, "receiveip", false, "ReceiveIP");
        public static final Property Receivestate = new Property(6, Integer.class, "receivestate", false, "ReceiveState");
        public static final Property Toclientid = new Property(2, String.class, "toclientid", false, "ToClientID");
    }

    public IMMessagesDao(DaoConfig config) {
        super(config);
    }

    public IMMessagesDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"IMMESSAGES\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"FromClientID\" TEXT," + "\"ToClientID\" TEXT," + "\"Date\" INTEGER," + "\"ReceiveDate\" INTEGER," + "\"ExpireDate\" INTEGER," + "\"ReceiveState\" INTEGER," + "\"Content\" TEXT," + "\"ReceiveIP\" TEXT);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_FromClientID ON \"IMMESSAGES\"" + " (\"FromClientID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_ToClientID ON \"IMMESSAGES\"" + " (\"ToClientID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_Date ON \"IMMESSAGES\"" + " (\"Date\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_ReceiveDate ON \"IMMESSAGES\"" + " (\"ReceiveDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_ExpireDate ON \"IMMESSAGES\"" + " (\"ExpireDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_ReceiveState ON \"IMMESSAGES\"" + " (\"ReceiveState\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_Content ON \"IMMESSAGES\"" + " (\"Content\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_IMMESSAGES_ReceiveIP ON \"IMMESSAGES\"" + " (\"ReceiveIP\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"IMMESSAGES\"");
    }

    protected final void bindValues(DatabaseStatement stmt, IMMessages entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String fromclientid = entity.getFromclientid();
        if (fromclientid != null) {
            stmt.bindString(2, fromclientid);
        }
        String toclientid = entity.getToclientid();
        if (toclientid != null) {
            stmt.bindString(3, toclientid);
        }
        Date date = entity.getDate();
        if (date != null) {
            stmt.bindLong(4, date.getTime());
        }
        Date receivedate = entity.getReceivedate();
        if (receivedate != null) {
            stmt.bindLong(5, receivedate.getTime());
        }
        Date expiredate = entity.getExpiredate();
        if (expiredate != null) {
            stmt.bindLong(6, expiredate.getTime());
        }
        Integer receivestate = entity.getReceivestate();
        if (receivestate != null) {
            stmt.bindLong(7, (long) receivestate.intValue());
        }
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(8, content);
        }
        String receiveip = entity.getReceiveip();
        if (receiveip != null) {
            stmt.bindString(9, receiveip);
        }
    }

    protected final void bindValues(SQLiteStatement stmt, IMMessages entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String fromclientid = entity.getFromclientid();
        if (fromclientid != null) {
            stmt.bindString(2, fromclientid);
        }
        String toclientid = entity.getToclientid();
        if (toclientid != null) {
            stmt.bindString(3, toclientid);
        }
        Date date = entity.getDate();
        if (date != null) {
            stmt.bindLong(4, date.getTime());
        }
        Date receivedate = entity.getReceivedate();
        if (receivedate != null) {
            stmt.bindLong(5, receivedate.getTime());
        }
        Date expiredate = entity.getExpiredate();
        if (expiredate != null) {
            stmt.bindLong(6, expiredate.getTime());
        }
        Integer receivestate = entity.getReceivestate();
        if (receivestate != null) {
            stmt.bindLong(7, (long) receivestate.intValue());
        }
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(8, content);
        }
        String receiveip = entity.getReceiveip();
        if (receiveip != null) {
            stmt.bindString(9, receiveip);
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public IMMessages readEntity(Cursor cursor, int offset) {
        String str = null;
        String string = cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
        String string2 = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
        String string3 = cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2);
        Date date = cursor.isNull(offset + 3) ? null : new Date(cursor.getLong(offset + 3));
        Date date2 = cursor.isNull(offset + 4) ? null : new Date(cursor.getLong(offset + 4));
        Date date3 = cursor.isNull(offset + 5) ? null : new Date(cursor.getLong(offset + 5));
        Integer valueOf = cursor.isNull(offset + 6) ? null : Integer.valueOf(cursor.getInt(offset + 6));
        String string4 = cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7);
        if (!cursor.isNull(offset + 8)) {
            str = cursor.getString(offset + 8);
        }
        return new IMMessages(string, string2, string3, date, date2, date3, valueOf, string4, str);
    }

    public void readEntity(Cursor cursor, IMMessages entity, int offset) {
        String str = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setFromclientid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setToclientid(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setDate(cursor.isNull(offset + 3) ? null : new Date(cursor.getLong(offset + 3)));
        entity.setReceivedate(cursor.isNull(offset + 4) ? null : new Date(cursor.getLong(offset + 4)));
        entity.setExpiredate(cursor.isNull(offset + 5) ? null : new Date(cursor.getLong(offset + 5)));
        entity.setReceivestate(cursor.isNull(offset + 6) ? null : Integer.valueOf(cursor.getInt(offset + 6)));
        entity.setContent(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        if (!cursor.isNull(offset + 8)) {
            str = cursor.getString(offset + 8);
        }
        entity.setReceiveip(str);
    }

    protected final String updateKeyAfterInsert(IMMessages entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(IMMessages entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(IMMessages entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
