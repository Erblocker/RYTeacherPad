package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import io.vov.vitamio.MediaMetadataRetriever;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

public class ResourcesDao extends AbstractDao<Resources, String> {
    public static final String TABLENAME = "RESOURCES";

    public static class Properties {
        public static final Property Acl_groupguid = new Property(18, String.class, "acl_groupguid", false, "ACL_GroupGUID");
        public static final Property Acl_groupread = new Property(21, Integer.class, "acl_groupread", false, "ACL_GroupRead");
        public static final Property Acl_groupwrite = new Property(22, Integer.class, "acl_groupwrite", false, "ACL_GroupWrite");
        public static final Property Acl_otherread = new Property(23, Integer.class, "acl_otherread", false, "ACL_OtherRead");
        public static final Property Acl_otherwrite = new Property(24, Integer.class, "acl_otherwrite", false, "ACL_OtherWrite");
        public static final Property Acl_ownerguid = new Property(16, String.class, "acl_ownerguid", false, "ACL_OwnerGUID");
        public static final Property Acl_ownername = new Property(17, String.class, "acl_ownername", false, "ACL_OwnerName");
        public static final Property Acl_ownerread = new Property(19, Integer.class, "acl_ownerread", false, "ACL_OwnerRead");
        public static final Property Acl_ownerwrite = new Property(20, Integer.class, "acl_ownerwrite", false, "ACL_OwnerWrite");
        public static final Property Author = new Property(6, String.class, MediaMetadataRetriever.METADATA_KEY_AUTHOR, false, "Author");
        public static final Property Content = new Property(25, byte[].class, "content", false, "Content");
        public static final Property Filename = new Property(9, String.class, MediaMetadataRetriever.METADATA_KEY_FILENAME, false, "FileName");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Haspreview = new Property(14, Integer.class, "haspreview", false, "HasPreview");
        public static final Property Hasthumbnail = new Property(13, Integer.class, "hasthumbnail", false, "HasThumbnail");
        public static final Property Mainfileextname = new Property(10, String.class, "mainfileextname", false, "MainFileExtName");
        public static final Property Mainfileuri = new Property(11, String.class, "mainfileuri", false, "MainFileURI");
        public static final Property Previewfileuri = new Property(12, String.class, "previewfileuri", false, "PreviewFileURI");
        public static final Property Resourcedate = new Property(2, Date.class, "resourcedate", false, "ResourceDate");
        public static final Property Resourcetype = new Property(3, String.class, "resourcetype", false, "ResourceType");
        public static final Property Searchtext = new Property(8, String.class, "searchtext", false, "SearchText");
        public static final Property Source = new Property(5, String.class, "source", false, "Source");
        public static final Property Status = new Property(15, Integer.class, "status", false, "Status");
        public static final Property Summery = new Property(7, String.class, "summery", false, "Summery");
        public static final Property Title = new Property(1, String.class, "title", false, "Title");
        public static final Property Type = new Property(4, Integer.class, "type", false, "Type");
    }

    public ResourcesDao(DaoConfig config) {
        super(config);
    }

    public ResourcesDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"RESOURCES\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"Title\" TEXT," + "\"ResourceDate\" INTEGER," + "\"ResourceType\" TEXT," + "\"Type\" INTEGER," + "\"Source\" TEXT," + "\"Author\" TEXT," + "\"Summery\" TEXT," + "\"SearchText\" TEXT," + "\"FileName\" TEXT," + "\"MainFileExtName\" TEXT," + "\"MainFileURI\" TEXT," + "\"PreviewFileURI\" TEXT," + "\"HasThumbnail\" INTEGER," + "\"HasPreview\" INTEGER," + "\"Status\" INTEGER," + "\"ACL_OwnerGUID\" TEXT," + "\"ACL_OwnerName\" TEXT," + "\"ACL_GroupGUID\" TEXT," + "\"ACL_OwnerRead\" INTEGER," + "\"ACL_OwnerWrite\" INTEGER," + "\"ACL_GroupRead\" INTEGER," + "\"ACL_GroupWrite\" INTEGER," + "\"ACL_OtherRead\" INTEGER," + "\"ACL_OtherWrite\" INTEGER," + "\"Content\" BLOB);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_Title ON \"RESOURCES\"" + " (\"Title\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ResourceDate ON \"RESOURCES\"" + " (\"ResourceDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ResourceType ON \"RESOURCES\"" + " (\"ResourceType\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_Type ON \"RESOURCES\"" + " (\"Type\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_Source ON \"RESOURCES\"" + " (\"Source\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_Author ON \"RESOURCES\"" + " (\"Author\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_Summery ON \"RESOURCES\"" + " (\"Summery\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_FileName ON \"RESOURCES\"" + " (\"FileName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_MainFileExtName ON \"RESOURCES\"" + " (\"MainFileExtName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_MainFileURI ON \"RESOURCES\"" + " (\"MainFileURI\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_PreviewFileURI ON \"RESOURCES\"" + " (\"PreviewFileURI\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_HasThumbnail ON \"RESOURCES\"" + " (\"HasThumbnail\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_HasPreview ON \"RESOURCES\"" + " (\"HasPreview\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_Status ON \"RESOURCES\"" + " (\"Status\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_OwnerGUID ON \"RESOURCES\"" + " (\"ACL_OwnerGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_OwnerName ON \"RESOURCES\"" + " (\"ACL_OwnerName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_GroupGUID ON \"RESOURCES\"" + " (\"ACL_GroupGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_OwnerRead ON \"RESOURCES\"" + " (\"ACL_OwnerRead\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_OwnerWrite ON \"RESOURCES\"" + " (\"ACL_OwnerWrite\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_GroupRead ON \"RESOURCES\"" + " (\"ACL_GroupRead\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_GroupWrite ON \"RESOURCES\"" + " (\"ACL_GroupWrite\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_OtherRead ON \"RESOURCES\"" + " (\"ACL_OtherRead\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_RESOURCES_ACL_OtherWrite ON \"RESOURCES\"" + " (\"ACL_OtherWrite\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"RESOURCES\"");
    }

    protected final void bindValues(DatabaseStatement stmt, Resources entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
        Date resourcedate = entity.getResourcedate();
        if (resourcedate != null) {
            stmt.bindLong(3, resourcedate.getTime());
        }
        String resourcetype = entity.getResourcetype();
        if (resourcetype != null) {
            stmt.bindString(4, resourcetype);
        }
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(5, (long) type.intValue());
        }
        String source = entity.getSource();
        if (source != null) {
            stmt.bindString(6, source);
        }
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(7, author);
        }
        String summery = entity.getSummery();
        if (summery != null) {
            stmt.bindString(8, summery);
        }
        String searchtext = entity.getSearchtext();
        if (searchtext != null) {
            stmt.bindString(9, searchtext);
        }
        String filename = entity.getFilename();
        if (filename != null) {
            stmt.bindString(10, filename);
        }
        String mainfileextname = entity.getMainfileextname();
        if (mainfileextname != null) {
            stmt.bindString(11, mainfileextname);
        }
        String mainfileuri = entity.getMainfileuri();
        if (mainfileuri != null) {
            stmt.bindString(12, mainfileuri);
        }
        String previewfileuri = entity.getPreviewfileuri();
        if (previewfileuri != null) {
            stmt.bindString(13, previewfileuri);
        }
        Integer hasthumbnail = entity.getHasthumbnail();
        if (hasthumbnail != null) {
            stmt.bindLong(14, (long) hasthumbnail.intValue());
        }
        Integer haspreview = entity.getHaspreview();
        if (haspreview != null) {
            stmt.bindLong(15, (long) haspreview.intValue());
        }
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(16, (long) status.intValue());
        }
        String acl_ownerguid = entity.getAcl_ownerguid();
        if (acl_ownerguid != null) {
            stmt.bindString(17, acl_ownerguid);
        }
        String acl_ownername = entity.getAcl_ownername();
        if (acl_ownername != null) {
            stmt.bindString(18, acl_ownername);
        }
        String acl_groupguid = entity.getAcl_groupguid();
        if (acl_groupguid != null) {
            stmt.bindString(19, acl_groupguid);
        }
        Integer acl_ownerread = entity.getAcl_ownerread();
        if (acl_ownerread != null) {
            stmt.bindLong(20, (long) acl_ownerread.intValue());
        }
        Integer acl_ownerwrite = entity.getAcl_ownerwrite();
        if (acl_ownerwrite != null) {
            stmt.bindLong(21, (long) acl_ownerwrite.intValue());
        }
        Integer acl_groupread = entity.getAcl_groupread();
        if (acl_groupread != null) {
            stmt.bindLong(22, (long) acl_groupread.intValue());
        }
        Integer acl_groupwrite = entity.getAcl_groupwrite();
        if (acl_groupwrite != null) {
            stmt.bindLong(23, (long) acl_groupwrite.intValue());
        }
        Integer acl_otherread = entity.getAcl_otherread();
        if (acl_otherread != null) {
            stmt.bindLong(24, (long) acl_otherread.intValue());
        }
        Integer acl_otherwrite = entity.getAcl_otherwrite();
        if (acl_otherwrite != null) {
            stmt.bindLong(25, (long) acl_otherwrite.intValue());
        }
        byte[] content = entity.getContent();
        if (content != null) {
            stmt.bindBlob(26, content);
        }
    }

    protected final void bindValues(SQLiteStatement stmt, Resources entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
        Date resourcedate = entity.getResourcedate();
        if (resourcedate != null) {
            stmt.bindLong(3, resourcedate.getTime());
        }
        String resourcetype = entity.getResourcetype();
        if (resourcetype != null) {
            stmt.bindString(4, resourcetype);
        }
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(5, (long) type.intValue());
        }
        String source = entity.getSource();
        if (source != null) {
            stmt.bindString(6, source);
        }
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(7, author);
        }
        String summery = entity.getSummery();
        if (summery != null) {
            stmt.bindString(8, summery);
        }
        String searchtext = entity.getSearchtext();
        if (searchtext != null) {
            stmt.bindString(9, searchtext);
        }
        String filename = entity.getFilename();
        if (filename != null) {
            stmt.bindString(10, filename);
        }
        String mainfileextname = entity.getMainfileextname();
        if (mainfileextname != null) {
            stmt.bindString(11, mainfileextname);
        }
        String mainfileuri = entity.getMainfileuri();
        if (mainfileuri != null) {
            stmt.bindString(12, mainfileuri);
        }
        String previewfileuri = entity.getPreviewfileuri();
        if (previewfileuri != null) {
            stmt.bindString(13, previewfileuri);
        }
        Integer hasthumbnail = entity.getHasthumbnail();
        if (hasthumbnail != null) {
            stmt.bindLong(14, (long) hasthumbnail.intValue());
        }
        Integer haspreview = entity.getHaspreview();
        if (haspreview != null) {
            stmt.bindLong(15, (long) haspreview.intValue());
        }
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(16, (long) status.intValue());
        }
        String acl_ownerguid = entity.getAcl_ownerguid();
        if (acl_ownerguid != null) {
            stmt.bindString(17, acl_ownerguid);
        }
        String acl_ownername = entity.getAcl_ownername();
        if (acl_ownername != null) {
            stmt.bindString(18, acl_ownername);
        }
        String acl_groupguid = entity.getAcl_groupguid();
        if (acl_groupguid != null) {
            stmt.bindString(19, acl_groupguid);
        }
        Integer acl_ownerread = entity.getAcl_ownerread();
        if (acl_ownerread != null) {
            stmt.bindLong(20, (long) acl_ownerread.intValue());
        }
        Integer acl_ownerwrite = entity.getAcl_ownerwrite();
        if (acl_ownerwrite != null) {
            stmt.bindLong(21, (long) acl_ownerwrite.intValue());
        }
        Integer acl_groupread = entity.getAcl_groupread();
        if (acl_groupread != null) {
            stmt.bindLong(22, (long) acl_groupread.intValue());
        }
        Integer acl_groupwrite = entity.getAcl_groupwrite();
        if (acl_groupwrite != null) {
            stmt.bindLong(23, (long) acl_groupwrite.intValue());
        }
        Integer acl_otherread = entity.getAcl_otherread();
        if (acl_otherread != null) {
            stmt.bindLong(24, (long) acl_otherread.intValue());
        }
        Integer acl_otherwrite = entity.getAcl_otherwrite();
        if (acl_otherwrite != null) {
            stmt.bindLong(25, (long) acl_otherwrite.intValue());
        }
        byte[] content = entity.getContent();
        if (content != null) {
            stmt.bindBlob(26, content);
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public Resources readEntity(Cursor cursor, int offset) {
        byte[] bArr;
        String string = cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
        String string2 = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
        Date date = cursor.isNull(offset + 2) ? null : new Date(cursor.getLong(offset + 2));
        String string3 = cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3);
        Integer valueOf = cursor.isNull(offset + 4) ? null : Integer.valueOf(cursor.getInt(offset + 4));
        String string4 = cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5);
        String string5 = cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6);
        String string6 = cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7);
        String string7 = cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8);
        String string8 = cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9);
        String string9 = cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10);
        String string10 = cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11);
        String string11 = cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12);
        Integer valueOf2 = cursor.isNull(offset + 13) ? null : Integer.valueOf(cursor.getInt(offset + 13));
        Integer valueOf3 = cursor.isNull(offset + 14) ? null : Integer.valueOf(cursor.getInt(offset + 14));
        Integer valueOf4 = cursor.isNull(offset + 15) ? null : Integer.valueOf(cursor.getInt(offset + 15));
        String string12 = cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16);
        String string13 = cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17);
        String string14 = cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18);
        Integer valueOf5 = cursor.isNull(offset + 19) ? null : Integer.valueOf(cursor.getInt(offset + 19));
        Integer valueOf6 = cursor.isNull(offset + 20) ? null : Integer.valueOf(cursor.getInt(offset + 20));
        Integer valueOf7 = cursor.isNull(offset + 21) ? null : Integer.valueOf(cursor.getInt(offset + 21));
        Integer valueOf8 = cursor.isNull(offset + 22) ? null : Integer.valueOf(cursor.getInt(offset + 22));
        Integer valueOf9 = cursor.isNull(offset + 23) ? null : Integer.valueOf(cursor.getInt(offset + 23));
        Integer valueOf10 = cursor.isNull(offset + 24) ? null : Integer.valueOf(cursor.getInt(offset + 24));
        if (cursor.isNull(offset + 25)) {
            bArr = null;
        } else {
            bArr = cursor.getBlob(offset + 25);
        }
        return new Resources(string, string2, date, string3, valueOf, string4, string5, string6, string7, string8, string9, string10, string11, valueOf2, valueOf3, valueOf4, string12, string13, string14, valueOf5, valueOf6, valueOf7, valueOf8, valueOf9, valueOf10, bArr);
    }

    public void readEntity(Cursor cursor, Resources entity, int offset) {
        byte[] bArr = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setResourcedate(cursor.isNull(offset + 2) ? null : new Date(cursor.getLong(offset + 2)));
        entity.setResourcetype(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setType(cursor.isNull(offset + 4) ? null : Integer.valueOf(cursor.getInt(offset + 4)));
        entity.setSource(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAuthor(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSummery(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setSearchtext(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setFilename(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setMainfileextname(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setMainfileuri(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setPreviewfileuri(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setHasthumbnail(cursor.isNull(offset + 13) ? null : Integer.valueOf(cursor.getInt(offset + 13)));
        entity.setHaspreview(cursor.isNull(offset + 14) ? null : Integer.valueOf(cursor.getInt(offset + 14)));
        entity.setStatus(cursor.isNull(offset + 15) ? null : Integer.valueOf(cursor.getInt(offset + 15)));
        entity.setAcl_ownerguid(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setAcl_ownername(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setAcl_groupguid(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setAcl_ownerread(cursor.isNull(offset + 19) ? null : Integer.valueOf(cursor.getInt(offset + 19)));
        entity.setAcl_ownerwrite(cursor.isNull(offset + 20) ? null : Integer.valueOf(cursor.getInt(offset + 20)));
        entity.setAcl_groupread(cursor.isNull(offset + 21) ? null : Integer.valueOf(cursor.getInt(offset + 21)));
        entity.setAcl_groupwrite(cursor.isNull(offset + 22) ? null : Integer.valueOf(cursor.getInt(offset + 22)));
        entity.setAcl_otherread(cursor.isNull(offset + 23) ? null : Integer.valueOf(cursor.getInt(offset + 23)));
        entity.setAcl_otherwrite(cursor.isNull(offset + 24) ? null : Integer.valueOf(cursor.getInt(offset + 24)));
        if (!cursor.isNull(offset + 25)) {
            bArr = cursor.getBlob(offset + 25);
        }
        entity.setContent(bArr);
    }

    protected final String updateKeyAfterInsert(Resources entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(Resources entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(Resources entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
