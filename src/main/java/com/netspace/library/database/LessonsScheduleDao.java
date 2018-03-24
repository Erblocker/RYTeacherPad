package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.fragment.UserHonourFragment;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

public class LessonsScheduleDao extends AbstractDao<LessonsSchedule, String> {
    public static final String TABLENAME = "LESSONS_SCHEDULE";

    public static class Properties {
        public static final Property Grade = new Property(1, Integer.class, "grade", false, "Grade");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Lessonindex = new Property(3, Integer.class, "lessonindex", false, "LessonIndex");
        public static final Property Resourceguid = new Property(5, String.class, CommentComponent.RESOURCEGUID, false, "ResourceGUID");
        public static final Property Scheduledate = new Property(4, Date.class, "scheduledate", false, "ScheduleDate");
        public static final Property Scheduleenddate = new Property(10, Date.class, "scheduleenddate", false, "ScheduleEndDate");
        public static final Property State = new Property(9, Integer.class, "state", false, "State");
        public static final Property Subject = new Property(2, Integer.class, "subject", false, "Subject");
        public static final Property Syn_isdelete = new Property(12, Integer.class, "syn_isdelete", false, "SYN_IsDelete");
        public static final Property Syn_timestamp = new Property(11, Date.class, "syn_timestamp", false, "SYN_TimeStamp");
        public static final Property Userclassguid = new Property(7, String.class, UserHonourFragment.USERCLASSGUID, false, "UserClassGUID");
        public static final Property Userclassname = new Property(8, String.class, "userclassname", false, "UserClassName");
        public static final Property Userschoolguid = new Property(6, String.class, "userschoolguid", false, "UserSchoolGUID");
    }

    public LessonsScheduleDao(DaoConfig config) {
        super(config);
    }

    public LessonsScheduleDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"LESSONS_SCHEDULE\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"Grade\" INTEGER," + "\"Subject\" INTEGER," + "\"LessonIndex\" INTEGER," + "\"ScheduleDate\" INTEGER," + "\"ResourceGUID\" TEXT," + "\"UserSchoolGUID\" TEXT," + "\"UserClassGUID\" TEXT," + "\"UserClassName\" TEXT," + "\"State\" INTEGER," + "\"ScheduleEndDate\" INTEGER," + "\"SYN_TimeStamp\" INTEGER," + "\"SYN_IsDelete\" INTEGER);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_Grade ON \"LESSONS_SCHEDULE\"" + " (\"Grade\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_Subject ON \"LESSONS_SCHEDULE\"" + " (\"Subject\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_LessonIndex ON \"LESSONS_SCHEDULE\"" + " (\"LessonIndex\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_ScheduleDate ON \"LESSONS_SCHEDULE\"" + " (\"ScheduleDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_ResourceGUID ON \"LESSONS_SCHEDULE\"" + " (\"ResourceGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_UserSchoolGUID ON \"LESSONS_SCHEDULE\"" + " (\"UserSchoolGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_UserClassGUID ON \"LESSONS_SCHEDULE\"" + " (\"UserClassGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_UserClassName ON \"LESSONS_SCHEDULE\"" + " (\"UserClassName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_State ON \"LESSONS_SCHEDULE\"" + " (\"State\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_ScheduleEndDate ON \"LESSONS_SCHEDULE\"" + " (\"ScheduleEndDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_SYN_TimeStamp ON \"LESSONS_SCHEDULE\"" + " (\"SYN_TimeStamp\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_SYN_IsDelete ON \"LESSONS_SCHEDULE\"" + " (\"SYN_IsDelete\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LESSONS_SCHEDULE\"");
    }

    protected final void bindValues(DatabaseStatement stmt, LessonsSchedule entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        Integer grade = entity.getGrade();
        if (grade != null) {
            stmt.bindLong(2, (long) grade.intValue());
        }
        Integer subject = entity.getSubject();
        if (subject != null) {
            stmt.bindLong(3, (long) subject.intValue());
        }
        Integer lessonindex = entity.getLessonindex();
        if (lessonindex != null) {
            stmt.bindLong(4, (long) lessonindex.intValue());
        }
        Date scheduledate = entity.getScheduledate();
        if (scheduledate != null) {
            stmt.bindLong(5, scheduledate.getTime());
        }
        String resourceguid = entity.getResourceguid();
        if (resourceguid != null) {
            stmt.bindString(6, resourceguid);
        }
        String userschoolguid = entity.getUserschoolguid();
        if (userschoolguid != null) {
            stmt.bindString(7, userschoolguid);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(8, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(9, userclassname);
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(10, (long) state.intValue());
        }
        Date scheduleenddate = entity.getScheduleenddate();
        if (scheduleenddate != null) {
            stmt.bindLong(11, scheduleenddate.getTime());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(12, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(13, (long) syn_isdelete.intValue());
        }
    }

    protected final void bindValues(SQLiteStatement stmt, LessonsSchedule entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        Integer grade = entity.getGrade();
        if (grade != null) {
            stmt.bindLong(2, (long) grade.intValue());
        }
        Integer subject = entity.getSubject();
        if (subject != null) {
            stmt.bindLong(3, (long) subject.intValue());
        }
        Integer lessonindex = entity.getLessonindex();
        if (lessonindex != null) {
            stmt.bindLong(4, (long) lessonindex.intValue());
        }
        Date scheduledate = entity.getScheduledate();
        if (scheduledate != null) {
            stmt.bindLong(5, scheduledate.getTime());
        }
        String resourceguid = entity.getResourceguid();
        if (resourceguid != null) {
            stmt.bindString(6, resourceguid);
        }
        String userschoolguid = entity.getUserschoolguid();
        if (userschoolguid != null) {
            stmt.bindString(7, userschoolguid);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(8, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(9, userclassname);
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(10, (long) state.intValue());
        }
        Date scheduleenddate = entity.getScheduleenddate();
        if (scheduleenddate != null) {
            stmt.bindLong(11, scheduleenddate.getTime());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(12, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(13, (long) syn_isdelete.intValue());
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public LessonsSchedule readEntity(Cursor cursor, int offset) {
        String str;
        Integer num;
        Integer num2;
        Integer num3;
        Date date;
        String str2;
        String str3;
        String str4;
        String str5;
        Integer num4;
        Date date2;
        Date date3;
        Integer num5;
        if (cursor.isNull(offset + 0)) {
            str = null;
        } else {
            str = cursor.getString(offset + 0);
        }
        if (cursor.isNull(offset + 1)) {
            num = null;
        } else {
            num = Integer.valueOf(cursor.getInt(offset + 1));
        }
        if (cursor.isNull(offset + 2)) {
            num2 = null;
        } else {
            num2 = Integer.valueOf(cursor.getInt(offset + 2));
        }
        if (cursor.isNull(offset + 3)) {
            num3 = null;
        } else {
            num3 = Integer.valueOf(cursor.getInt(offset + 3));
        }
        if (cursor.isNull(offset + 4)) {
            date = null;
        } else {
            date = new Date(cursor.getLong(offset + 4));
        }
        if (cursor.isNull(offset + 5)) {
            str2 = null;
        } else {
            str2 = cursor.getString(offset + 5);
        }
        if (cursor.isNull(offset + 6)) {
            str3 = null;
        } else {
            str3 = cursor.getString(offset + 6);
        }
        if (cursor.isNull(offset + 7)) {
            str4 = null;
        } else {
            str4 = cursor.getString(offset + 7);
        }
        if (cursor.isNull(offset + 8)) {
            str5 = null;
        } else {
            str5 = cursor.getString(offset + 8);
        }
        if (cursor.isNull(offset + 9)) {
            num4 = null;
        } else {
            num4 = Integer.valueOf(cursor.getInt(offset + 9));
        }
        if (cursor.isNull(offset + 10)) {
            date2 = null;
        } else {
            date2 = new Date(cursor.getLong(offset + 10));
        }
        if (cursor.isNull(offset + 11)) {
            date3 = null;
        } else {
            date3 = new Date(cursor.getLong(offset + 11));
        }
        if (cursor.isNull(offset + 12)) {
            num5 = null;
        } else {
            num5 = Integer.valueOf(cursor.getInt(offset + 12));
        }
        return new LessonsSchedule(str, num, num2, num3, date, str2, str3, str4, str5, num4, date2, date3, num5);
    }

    public void readEntity(Cursor cursor, LessonsSchedule entity, int offset) {
        Integer num = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setGrade(cursor.isNull(offset + 1) ? null : Integer.valueOf(cursor.getInt(offset + 1)));
        entity.setSubject(cursor.isNull(offset + 2) ? null : Integer.valueOf(cursor.getInt(offset + 2)));
        entity.setLessonindex(cursor.isNull(offset + 3) ? null : Integer.valueOf(cursor.getInt(offset + 3)));
        entity.setScheduledate(cursor.isNull(offset + 4) ? null : new Date(cursor.getLong(offset + 4)));
        entity.setResourceguid(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setUserschoolguid(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setUserclassguid(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setUserclassname(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setState(cursor.isNull(offset + 9) ? null : Integer.valueOf(cursor.getInt(offset + 9)));
        entity.setScheduleenddate(cursor.isNull(offset + 10) ? null : new Date(cursor.getLong(offset + 10)));
        entity.setSyn_timestamp(cursor.isNull(offset + 11) ? null : new Date(cursor.getLong(offset + 11)));
        if (!cursor.isNull(offset + 12)) {
            num = Integer.valueOf(cursor.getInt(offset + 12));
        }
        entity.setSyn_isdelete(num);
    }

    protected final String updateKeyAfterInsert(LessonsSchedule entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(LessonsSchedule entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(LessonsSchedule entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
