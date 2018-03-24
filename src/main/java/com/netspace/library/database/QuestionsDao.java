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

public class QuestionsDao extends AbstractDao<Questions, String> {
    public static final String TABLENAME = "QUESTIONS";

    public static class Properties {
        public static final Property Acl_groupguid = new Property(20, String.class, "acl_groupguid", false, "ACL_GroupGUID");
        public static final Property Acl_groupread = new Property(23, Integer.class, "acl_groupread", false, "ACL_GroupRead");
        public static final Property Acl_groupwrite = new Property(24, Integer.class, "acl_groupwrite", false, "ACL_GroupWrite");
        public static final Property Acl_otherread = new Property(25, Integer.class, "acl_otherread", false, "ACL_OtherRead");
        public static final Property Acl_otherwrite = new Property(26, Integer.class, "acl_otherwrite", false, "ACL_OtherWrite");
        public static final Property Acl_ownerguid = new Property(18, String.class, "acl_ownerguid", false, "ACL_OwnerGUID");
        public static final Property Acl_ownername = new Property(19, String.class, "acl_ownername", false, "ACL_OwnerName");
        public static final Property Acl_ownerread = new Property(21, Integer.class, "acl_ownerread", false, "ACL_OwnerRead");
        public static final Property Acl_ownerwrite = new Property(22, Integer.class, "acl_ownerwrite", false, "ACL_OwnerWrite");
        public static final Property Answertime = new Property(9, Integer.class, "answertime", false, "AnswerTime");
        public static final Property Author = new Property(15, String.class, MediaMetadataRetriever.METADATA_KEY_AUTHOR, false, "Author");
        public static final Property Content = new Property(27, byte[].class, "content", false, "Content");
        public static final Property Difficulty = new Property(8, Integer.class, "difficulty", false, "Difficulty");
        public static final Property Filename = new Property(16, String.class, MediaMetadataRetriever.METADATA_KEY_FILENAME, false, "FileName");
        public static final Property Grade = new Property(5, Integer.class, "grade", false, "Grade");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Questiondate = new Property(2, Date.class, "questiondate", false, "QuestionDate");
        public static final Property Rank = new Property(12, Integer.class, "rank", false, "Rank");
        public static final Property Score = new Property(7, Integer.class, "score", false, "Score");
        public static final Property Searchtext = new Property(13, String.class, "searchtext", false, "SearchText");
        public static final Property Source = new Property(14, String.class, "source", false, "Source");
        public static final Property Subject = new Property(6, Integer.class, "subject", false, "Subject");
        public static final Property Subtype = new Property(4, Integer.class, "subtype", false, "SubType");
        public static final Property Tags = new Property(17, String.class, "tags", false, "Tags");
        public static final Property Teachlevel = new Property(10, Integer.class, "teachlevel", false, "TeachLevel");
        public static final Property Title = new Property(1, String.class, "title", false, "Title");
        public static final Property Type = new Property(3, Integer.class, "type", false, "Type");
        public static final Property Usetime = new Property(11, Integer.class, "usetime", false, "UseTime");
    }

    public QuestionsDao(DaoConfig config) {
        super(config);
    }

    public QuestionsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"QUESTIONS\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"Title\" TEXT," + "\"QuestionDate\" INTEGER," + "\"Type\" INTEGER," + "\"SubType\" INTEGER," + "\"Grade\" INTEGER," + "\"Subject\" INTEGER," + "\"Score\" INTEGER," + "\"Difficulty\" INTEGER," + "\"AnswerTime\" INTEGER," + "\"TeachLevel\" INTEGER," + "\"UseTime\" INTEGER," + "\"Rank\" INTEGER," + "\"SearchText\" TEXT," + "\"Source\" TEXT," + "\"Author\" TEXT," + "\"FileName\" TEXT," + "\"Tags\" TEXT," + "\"ACL_OwnerGUID\" TEXT," + "\"ACL_OwnerName\" TEXT," + "\"ACL_GroupGUID\" TEXT," + "\"ACL_OwnerRead\" INTEGER," + "\"ACL_OwnerWrite\" INTEGER," + "\"ACL_GroupRead\" INTEGER," + "\"ACL_GroupWrite\" INTEGER," + "\"ACL_OtherRead\" INTEGER," + "\"ACL_OtherWrite\" INTEGER," + "\"Content\" BLOB);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Title ON \"QUESTIONS\"" + " (\"Title\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_QuestionDate ON \"QUESTIONS\"" + " (\"QuestionDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Type ON \"QUESTIONS\"" + " (\"Type\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_SubType ON \"QUESTIONS\"" + " (\"SubType\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Grade ON \"QUESTIONS\"" + " (\"Grade\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Subject ON \"QUESTIONS\"" + " (\"Subject\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Score ON \"QUESTIONS\"" + " (\"Score\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Difficulty ON \"QUESTIONS\"" + " (\"Difficulty\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_AnswerTime ON \"QUESTIONS\"" + " (\"AnswerTime\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_TeachLevel ON \"QUESTIONS\"" + " (\"TeachLevel\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_UseTime ON \"QUESTIONS\"" + " (\"UseTime\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Rank ON \"QUESTIONS\"" + " (\"Rank\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Source ON \"QUESTIONS\"" + " (\"Source\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Author ON \"QUESTIONS\"" + " (\"Author\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_FileName ON \"QUESTIONS\"" + " (\"FileName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_Tags ON \"QUESTIONS\"" + " (\"Tags\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_OwnerGUID ON \"QUESTIONS\"" + " (\"ACL_OwnerGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_OwnerName ON \"QUESTIONS\"" + " (\"ACL_OwnerName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_GroupGUID ON \"QUESTIONS\"" + " (\"ACL_GroupGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_OwnerRead ON \"QUESTIONS\"" + " (\"ACL_OwnerRead\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_OwnerWrite ON \"QUESTIONS\"" + " (\"ACL_OwnerWrite\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_GroupRead ON \"QUESTIONS\"" + " (\"ACL_GroupRead\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_GroupWrite ON \"QUESTIONS\"" + " (\"ACL_GroupWrite\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_OtherRead ON \"QUESTIONS\"" + " (\"ACL_OtherRead\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_QUESTIONS_ACL_OtherWrite ON \"QUESTIONS\"" + " (\"ACL_OtherWrite\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"QUESTIONS\"");
    }

    protected final void bindValues(DatabaseStatement stmt, Questions entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
        Date questiondate = entity.getQuestiondate();
        if (questiondate != null) {
            stmt.bindLong(3, questiondate.getTime());
        }
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(4, (long) type.intValue());
        }
        Integer subtype = entity.getSubtype();
        if (subtype != null) {
            stmt.bindLong(5, (long) subtype.intValue());
        }
        Integer grade = entity.getGrade();
        if (grade != null) {
            stmt.bindLong(6, (long) grade.intValue());
        }
        Integer subject = entity.getSubject();
        if (subject != null) {
            stmt.bindLong(7, (long) subject.intValue());
        }
        Integer score = entity.getScore();
        if (score != null) {
            stmt.bindLong(8, (long) score.intValue());
        }
        Integer difficulty = entity.getDifficulty();
        if (difficulty != null) {
            stmt.bindLong(9, (long) difficulty.intValue());
        }
        Integer answertime = entity.getAnswertime();
        if (answertime != null) {
            stmt.bindLong(10, (long) answertime.intValue());
        }
        Integer teachlevel = entity.getTeachlevel();
        if (teachlevel != null) {
            stmt.bindLong(11, (long) teachlevel.intValue());
        }
        Integer usetime = entity.getUsetime();
        if (usetime != null) {
            stmt.bindLong(12, (long) usetime.intValue());
        }
        Integer rank = entity.getRank();
        if (rank != null) {
            stmt.bindLong(13, (long) rank.intValue());
        }
        String searchtext = entity.getSearchtext();
        if (searchtext != null) {
            stmt.bindString(14, searchtext);
        }
        String source = entity.getSource();
        if (source != null) {
            stmt.bindString(15, source);
        }
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(16, author);
        }
        String filename = entity.getFilename();
        if (filename != null) {
            stmt.bindString(17, filename);
        }
        String tags = entity.getTags();
        if (tags != null) {
            stmt.bindString(18, tags);
        }
        String acl_ownerguid = entity.getAcl_ownerguid();
        if (acl_ownerguid != null) {
            stmt.bindString(19, acl_ownerguid);
        }
        String acl_ownername = entity.getAcl_ownername();
        if (acl_ownername != null) {
            stmt.bindString(20, acl_ownername);
        }
        String acl_groupguid = entity.getAcl_groupguid();
        if (acl_groupguid != null) {
            stmt.bindString(21, acl_groupguid);
        }
        Integer acl_ownerread = entity.getAcl_ownerread();
        if (acl_ownerread != null) {
            stmt.bindLong(22, (long) acl_ownerread.intValue());
        }
        Integer acl_ownerwrite = entity.getAcl_ownerwrite();
        if (acl_ownerwrite != null) {
            stmt.bindLong(23, (long) acl_ownerwrite.intValue());
        }
        Integer acl_groupread = entity.getAcl_groupread();
        if (acl_groupread != null) {
            stmt.bindLong(24, (long) acl_groupread.intValue());
        }
        Integer acl_groupwrite = entity.getAcl_groupwrite();
        if (acl_groupwrite != null) {
            stmt.bindLong(25, (long) acl_groupwrite.intValue());
        }
        Integer acl_otherread = entity.getAcl_otherread();
        if (acl_otherread != null) {
            stmt.bindLong(26, (long) acl_otherread.intValue());
        }
        Integer acl_otherwrite = entity.getAcl_otherwrite();
        if (acl_otherwrite != null) {
            stmt.bindLong(27, (long) acl_otherwrite.intValue());
        }
        byte[] content = entity.getContent();
        if (content != null) {
            stmt.bindBlob(28, content);
        }
    }

    protected final void bindValues(SQLiteStatement stmt, Questions entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
        Date questiondate = entity.getQuestiondate();
        if (questiondate != null) {
            stmt.bindLong(3, questiondate.getTime());
        }
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(4, (long) type.intValue());
        }
        Integer subtype = entity.getSubtype();
        if (subtype != null) {
            stmt.bindLong(5, (long) subtype.intValue());
        }
        Integer grade = entity.getGrade();
        if (grade != null) {
            stmt.bindLong(6, (long) grade.intValue());
        }
        Integer subject = entity.getSubject();
        if (subject != null) {
            stmt.bindLong(7, (long) subject.intValue());
        }
        Integer score = entity.getScore();
        if (score != null) {
            stmt.bindLong(8, (long) score.intValue());
        }
        Integer difficulty = entity.getDifficulty();
        if (difficulty != null) {
            stmt.bindLong(9, (long) difficulty.intValue());
        }
        Integer answertime = entity.getAnswertime();
        if (answertime != null) {
            stmt.bindLong(10, (long) answertime.intValue());
        }
        Integer teachlevel = entity.getTeachlevel();
        if (teachlevel != null) {
            stmt.bindLong(11, (long) teachlevel.intValue());
        }
        Integer usetime = entity.getUsetime();
        if (usetime != null) {
            stmt.bindLong(12, (long) usetime.intValue());
        }
        Integer rank = entity.getRank();
        if (rank != null) {
            stmt.bindLong(13, (long) rank.intValue());
        }
        String searchtext = entity.getSearchtext();
        if (searchtext != null) {
            stmt.bindString(14, searchtext);
        }
        String source = entity.getSource();
        if (source != null) {
            stmt.bindString(15, source);
        }
        String author = entity.getAuthor();
        if (author != null) {
            stmt.bindString(16, author);
        }
        String filename = entity.getFilename();
        if (filename != null) {
            stmt.bindString(17, filename);
        }
        String tags = entity.getTags();
        if (tags != null) {
            stmt.bindString(18, tags);
        }
        String acl_ownerguid = entity.getAcl_ownerguid();
        if (acl_ownerguid != null) {
            stmt.bindString(19, acl_ownerguid);
        }
        String acl_ownername = entity.getAcl_ownername();
        if (acl_ownername != null) {
            stmt.bindString(20, acl_ownername);
        }
        String acl_groupguid = entity.getAcl_groupguid();
        if (acl_groupguid != null) {
            stmt.bindString(21, acl_groupguid);
        }
        Integer acl_ownerread = entity.getAcl_ownerread();
        if (acl_ownerread != null) {
            stmt.bindLong(22, (long) acl_ownerread.intValue());
        }
        Integer acl_ownerwrite = entity.getAcl_ownerwrite();
        if (acl_ownerwrite != null) {
            stmt.bindLong(23, (long) acl_ownerwrite.intValue());
        }
        Integer acl_groupread = entity.getAcl_groupread();
        if (acl_groupread != null) {
            stmt.bindLong(24, (long) acl_groupread.intValue());
        }
        Integer acl_groupwrite = entity.getAcl_groupwrite();
        if (acl_groupwrite != null) {
            stmt.bindLong(25, (long) acl_groupwrite.intValue());
        }
        Integer acl_otherread = entity.getAcl_otherread();
        if (acl_otherread != null) {
            stmt.bindLong(26, (long) acl_otherread.intValue());
        }
        Integer acl_otherwrite = entity.getAcl_otherwrite();
        if (acl_otherwrite != null) {
            stmt.bindLong(27, (long) acl_otherwrite.intValue());
        }
        byte[] content = entity.getContent();
        if (content != null) {
            stmt.bindBlob(28, content);
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public Questions readEntity(Cursor cursor, int offset) {
        byte[] bArr;
        String string = cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
        String string2 = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
        Date date = cursor.isNull(offset + 2) ? null : new Date(cursor.getLong(offset + 2));
        Integer valueOf = cursor.isNull(offset + 3) ? null : Integer.valueOf(cursor.getInt(offset + 3));
        Integer valueOf2 = cursor.isNull(offset + 4) ? null : Integer.valueOf(cursor.getInt(offset + 4));
        Integer valueOf3 = cursor.isNull(offset + 5) ? null : Integer.valueOf(cursor.getInt(offset + 5));
        Integer valueOf4 = cursor.isNull(offset + 6) ? null : Integer.valueOf(cursor.getInt(offset + 6));
        Integer valueOf5 = cursor.isNull(offset + 7) ? null : Integer.valueOf(cursor.getInt(offset + 7));
        Integer valueOf6 = cursor.isNull(offset + 8) ? null : Integer.valueOf(cursor.getInt(offset + 8));
        Integer valueOf7 = cursor.isNull(offset + 9) ? null : Integer.valueOf(cursor.getInt(offset + 9));
        Integer valueOf8 = cursor.isNull(offset + 10) ? null : Integer.valueOf(cursor.getInt(offset + 10));
        Integer valueOf9 = cursor.isNull(offset + 11) ? null : Integer.valueOf(cursor.getInt(offset + 11));
        Integer valueOf10 = cursor.isNull(offset + 12) ? null : Integer.valueOf(cursor.getInt(offset + 12));
        String string3 = cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13);
        String string4 = cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14);
        String string5 = cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15);
        String string6 = cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16);
        String string7 = cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17);
        String string8 = cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18);
        String string9 = cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19);
        String string10 = cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20);
        Integer valueOf11 = cursor.isNull(offset + 21) ? null : Integer.valueOf(cursor.getInt(offset + 21));
        Integer valueOf12 = cursor.isNull(offset + 22) ? null : Integer.valueOf(cursor.getInt(offset + 22));
        Integer valueOf13 = cursor.isNull(offset + 23) ? null : Integer.valueOf(cursor.getInt(offset + 23));
        Integer valueOf14 = cursor.isNull(offset + 24) ? null : Integer.valueOf(cursor.getInt(offset + 24));
        Integer valueOf15 = cursor.isNull(offset + 25) ? null : Integer.valueOf(cursor.getInt(offset + 25));
        Integer valueOf16 = cursor.isNull(offset + 26) ? null : Integer.valueOf(cursor.getInt(offset + 26));
        if (cursor.isNull(offset + 27)) {
            bArr = null;
        } else {
            bArr = cursor.getBlob(offset + 27);
        }
        return new Questions(string, string2, date, valueOf, valueOf2, valueOf3, valueOf4, valueOf5, valueOf6, valueOf7, valueOf8, valueOf9, valueOf10, string3, string4, string5, string6, string7, string8, string9, string10, valueOf11, valueOf12, valueOf13, valueOf14, valueOf15, valueOf16, bArr);
    }

    public void readEntity(Cursor cursor, Questions entity, int offset) {
        byte[] bArr = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setQuestiondate(cursor.isNull(offset + 2) ? null : new Date(cursor.getLong(offset + 2)));
        entity.setType(cursor.isNull(offset + 3) ? null : Integer.valueOf(cursor.getInt(offset + 3)));
        entity.setSubtype(cursor.isNull(offset + 4) ? null : Integer.valueOf(cursor.getInt(offset + 4)));
        entity.setGrade(cursor.isNull(offset + 5) ? null : Integer.valueOf(cursor.getInt(offset + 5)));
        entity.setSubject(cursor.isNull(offset + 6) ? null : Integer.valueOf(cursor.getInt(offset + 6)));
        entity.setScore(cursor.isNull(offset + 7) ? null : Integer.valueOf(cursor.getInt(offset + 7)));
        entity.setDifficulty(cursor.isNull(offset + 8) ? null : Integer.valueOf(cursor.getInt(offset + 8)));
        entity.setAnswertime(cursor.isNull(offset + 9) ? null : Integer.valueOf(cursor.getInt(offset + 9)));
        entity.setTeachlevel(cursor.isNull(offset + 10) ? null : Integer.valueOf(cursor.getInt(offset + 10)));
        entity.setUsetime(cursor.isNull(offset + 11) ? null : Integer.valueOf(cursor.getInt(offset + 11)));
        entity.setRank(cursor.isNull(offset + 12) ? null : Integer.valueOf(cursor.getInt(offset + 12)));
        entity.setSearchtext(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setSource(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setAuthor(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setFilename(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setTags(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setAcl_ownerguid(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setAcl_ownername(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
        entity.setAcl_groupguid(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setAcl_ownerread(cursor.isNull(offset + 21) ? null : Integer.valueOf(cursor.getInt(offset + 21)));
        entity.setAcl_ownerwrite(cursor.isNull(offset + 22) ? null : Integer.valueOf(cursor.getInt(offset + 22)));
        entity.setAcl_groupread(cursor.isNull(offset + 23) ? null : Integer.valueOf(cursor.getInt(offset + 23)));
        entity.setAcl_groupwrite(cursor.isNull(offset + 24) ? null : Integer.valueOf(cursor.getInt(offset + 24)));
        entity.setAcl_otherread(cursor.isNull(offset + 25) ? null : Integer.valueOf(cursor.getInt(offset + 25)));
        entity.setAcl_otherwrite(cursor.isNull(offset + 26) ? null : Integer.valueOf(cursor.getInt(offset + 26)));
        if (!cursor.isNull(offset + 27)) {
            bArr = cursor.getBlob(offset + 27);
        }
        entity.setContent(bArr);
    }

    protected final String updateKeyAfterInsert(Questions entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(Questions entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(Questions entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
