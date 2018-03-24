package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import com.netspace.library.components.ContentDisplayComponent;
import com.netspace.library.fragment.UserHonourFragment;
import io.vov.vitamio.provider.MediaStore.Video.VideoColumns;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

public class StudentQuestionBookDao extends AbstractDao<StudentQuestionBook, String> {
    public static final String TABLENAME = "STUDENT_QUESTION_BOOK";

    public static class Properties {
        public static final Property Booknames = new Property(15, String.class, "booknames", false, "BookNames");
        public static final Property Description = new Property(11, String.class, VideoColumns.DESCRIPTION, false, "Description");
        public static final Property Flags = new Property(16, Integer.class, ContentDisplayComponent.FLAGS, false, "Flags");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Kpguid = new Property(9, String.class, "kpguid", false, "KPGUID");
        public static final Property Kpname = new Property(10, String.class, "kpname", false, "KPName");
        public static final Property Modifydate = new Property(2, Date.class, "modifydate", false, "ModifyDate");
        public static final Property Objectguid = new Property(7, String.class, "objectguid", false, "ObjectGUID");
        public static final Property Packageid = new Property(8, String.class, "packageid", false, "PackageID");
        public static final Property Studentid = new Property(3, String.class, "studentid", false, "StudentID");
        public static final Property Studentname = new Property(4, String.class, "studentname", false, "StudentName");
        public static final Property Subject = new Property(12, Integer.class, "subject", false, "Subject");
        public static final Property Syn_isdelete = new Property(18, Integer.class, "syn_isdelete", false, "SYN_IsDelete");
        public static final Property Syn_timestamp = new Property(17, Date.class, "syn_timestamp", false, "SYN_TimeStamp");
        public static final Property Tags = new Property(14, String.class, "tags", false, "Tags");
        public static final Property Title = new Property(1, String.class, "title", false, "Title");
        public static final Property Type = new Property(13, Integer.class, "type", false, "Type");
        public static final Property Userclassguid = new Property(5, String.class, UserHonourFragment.USERCLASSGUID, false, "UserClassGUID");
        public static final Property Userclassname = new Property(6, String.class, "userclassname", false, "UserClassName");
    }

    public StudentQuestionBookDao(DaoConfig config) {
        super(config);
    }

    public StudentQuestionBookDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"STUDENT_QUESTION_BOOK\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"Title\" TEXT," + "\"ModifyDate\" INTEGER," + "\"StudentID\" TEXT," + "\"StudentName\" TEXT," + "\"UserClassGUID\" TEXT," + "\"UserClassName\" TEXT," + "\"ObjectGUID\" TEXT," + "\"PackageID\" TEXT," + "\"KPGUID\" TEXT," + "\"KPName\" TEXT," + "\"Description\" TEXT," + "\"Subject\" INTEGER," + "\"Type\" INTEGER," + "\"Tags\" TEXT," + "\"BookNames\" TEXT," + "\"Flags\" INTEGER," + "\"SYN_TimeStamp\" INTEGER," + "\"SYN_IsDelete\" INTEGER);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_Title ON \"STUDENT_QUESTION_BOOK\"" + " (\"Title\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_ModifyDate ON \"STUDENT_QUESTION_BOOK\"" + " (\"ModifyDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_StudentID ON \"STUDENT_QUESTION_BOOK\"" + " (\"StudentID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_StudentName ON \"STUDENT_QUESTION_BOOK\"" + " (\"StudentName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_UserClassGUID ON \"STUDENT_QUESTION_BOOK\"" + " (\"UserClassGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_UserClassName ON \"STUDENT_QUESTION_BOOK\"" + " (\"UserClassName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_ObjectGUID ON \"STUDENT_QUESTION_BOOK\"" + " (\"ObjectGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_PackageID ON \"STUDENT_QUESTION_BOOK\"" + " (\"PackageID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_KPGUID ON \"STUDENT_QUESTION_BOOK\"" + " (\"KPGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_KPName ON \"STUDENT_QUESTION_BOOK\"" + " (\"KPName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_Description ON \"STUDENT_QUESTION_BOOK\"" + " (\"Description\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_Subject ON \"STUDENT_QUESTION_BOOK\"" + " (\"Subject\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_Type ON \"STUDENT_QUESTION_BOOK\"" + " (\"Type\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_Tags ON \"STUDENT_QUESTION_BOOK\"" + " (\"Tags\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_BookNames ON \"STUDENT_QUESTION_BOOK\"" + " (\"BookNames\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_Flags ON \"STUDENT_QUESTION_BOOK\"" + " (\"Flags\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_SYN_TimeStamp ON \"STUDENT_QUESTION_BOOK\"" + " (\"SYN_TimeStamp\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_STUDENT_QUESTION_BOOK_SYN_IsDelete ON \"STUDENT_QUESTION_BOOK\"" + " (\"SYN_IsDelete\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"STUDENT_QUESTION_BOOK\"");
    }

    protected final void bindValues(DatabaseStatement stmt, StudentQuestionBook entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
        Date modifydate = entity.getModifydate();
        if (modifydate != null) {
            stmt.bindLong(3, modifydate.getTime());
        }
        String studentid = entity.getStudentid();
        if (studentid != null) {
            stmt.bindString(4, studentid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(5, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(6, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(7, userclassname);
        }
        String objectguid = entity.getObjectguid();
        if (objectguid != null) {
            stmt.bindString(8, objectguid);
        }
        String packageid = entity.getPackageid();
        if (packageid != null) {
            stmt.bindString(9, packageid);
        }
        String kpguid = entity.getKpguid();
        if (kpguid != null) {
            stmt.bindString(10, kpguid);
        }
        String kpname = entity.getKpname();
        if (kpname != null) {
            stmt.bindString(11, kpname);
        }
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(12, description);
        }
        Integer subject = entity.getSubject();
        if (subject != null) {
            stmt.bindLong(13, (long) subject.intValue());
        }
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(14, (long) type.intValue());
        }
        String tags = entity.getTags();
        if (tags != null) {
            stmt.bindString(15, tags);
        }
        String booknames = entity.getBooknames();
        if (booknames != null) {
            stmt.bindString(16, booknames);
        }
        Integer flags = entity.getFlags();
        if (flags != null) {
            stmt.bindLong(17, (long) flags.intValue());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(18, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(19, (long) syn_isdelete.intValue());
        }
    }

    protected final void bindValues(SQLiteStatement stmt, StudentQuestionBook entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }
        Date modifydate = entity.getModifydate();
        if (modifydate != null) {
            stmt.bindLong(3, modifydate.getTime());
        }
        String studentid = entity.getStudentid();
        if (studentid != null) {
            stmt.bindString(4, studentid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(5, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(6, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(7, userclassname);
        }
        String objectguid = entity.getObjectguid();
        if (objectguid != null) {
            stmt.bindString(8, objectguid);
        }
        String packageid = entity.getPackageid();
        if (packageid != null) {
            stmt.bindString(9, packageid);
        }
        String kpguid = entity.getKpguid();
        if (kpguid != null) {
            stmt.bindString(10, kpguid);
        }
        String kpname = entity.getKpname();
        if (kpname != null) {
            stmt.bindString(11, kpname);
        }
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(12, description);
        }
        Integer subject = entity.getSubject();
        if (subject != null) {
            stmt.bindLong(13, (long) subject.intValue());
        }
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(14, (long) type.intValue());
        }
        String tags = entity.getTags();
        if (tags != null) {
            stmt.bindString(15, tags);
        }
        String booknames = entity.getBooknames();
        if (booknames != null) {
            stmt.bindString(16, booknames);
        }
        Integer flags = entity.getFlags();
        if (flags != null) {
            stmt.bindLong(17, (long) flags.intValue());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(18, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(19, (long) syn_isdelete.intValue());
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public StudentQuestionBook readEntity(Cursor cursor, int offset) {
        Date date;
        Integer num;
        String string = cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
        String string2 = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
        Date date2 = cursor.isNull(offset + 2) ? null : new Date(cursor.getLong(offset + 2));
        String string3 = cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3);
        String string4 = cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4);
        String string5 = cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5);
        String string6 = cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6);
        String string7 = cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7);
        String string8 = cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8);
        String string9 = cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9);
        String string10 = cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10);
        String string11 = cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11);
        Integer valueOf = cursor.isNull(offset + 12) ? null : Integer.valueOf(cursor.getInt(offset + 12));
        Integer valueOf2 = cursor.isNull(offset + 13) ? null : Integer.valueOf(cursor.getInt(offset + 13));
        String string12 = cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14);
        String string13 = cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15);
        Integer valueOf3 = cursor.isNull(offset + 16) ? null : Integer.valueOf(cursor.getInt(offset + 16));
        if (cursor.isNull(offset + 17)) {
            date = null;
        } else {
            Date date3 = new Date(cursor.getLong(offset + 17));
        }
        if (cursor.isNull(offset + 18)) {
            num = null;
        } else {
            num = Integer.valueOf(cursor.getInt(offset + 18));
        }
        return new StudentQuestionBook(string, string2, date2, string3, string4, string5, string6, string7, string8, string9, string10, string11, valueOf, valueOf2, string12, string13, valueOf3, date, num);
    }

    public void readEntity(Cursor cursor, StudentQuestionBook entity, int offset) {
        Integer num = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setModifydate(cursor.isNull(offset + 2) ? null : new Date(cursor.getLong(offset + 2)));
        entity.setStudentid(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setStudentname(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setUserclassguid(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setUserclassname(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setObjectguid(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setPackageid(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setKpguid(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setKpname(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setDescription(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setSubject(cursor.isNull(offset + 12) ? null : Integer.valueOf(cursor.getInt(offset + 12)));
        entity.setType(cursor.isNull(offset + 13) ? null : Integer.valueOf(cursor.getInt(offset + 13)));
        entity.setTags(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setBooknames(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setFlags(cursor.isNull(offset + 16) ? null : Integer.valueOf(cursor.getInt(offset + 16)));
        entity.setSyn_timestamp(cursor.isNull(offset + 17) ? null : new Date(cursor.getLong(offset + 17)));
        if (!cursor.isNull(offset + 18)) {
            num = Integer.valueOf(cursor.getInt(offset + 18));
        }
        entity.setSyn_isdelete(num);
    }

    protected final String updateKeyAfterInsert(StudentQuestionBook entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(StudentQuestionBook entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(StudentQuestionBook entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
