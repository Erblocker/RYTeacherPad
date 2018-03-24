package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import com.netspace.library.fragment.UserHonourFragment;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

public class LessonsScheduleResultDao extends AbstractDao<LessonsScheduleResult, String> {
    public static final String TABLENAME = "LESSONS_SCHEDULE_RESULT";

    public static class Properties {
        public static final Property Answerdate = new Property(10, Date.class, "answerdate", false, "AnswerDate");
        public static final Property Answerindex = new Property(17, Integer.class, "answerindex", false, "AnswerIndex");
        public static final Property Answerresult = new Property(11, Integer.class, "answerresult", false, "AnswerResult");
        public static final Property Answers = new Property(8, String.class, "answers", false, "Answers");
        public static final Property Answerscore = new Property(18, Float.class, "answerscore", false, "AnswerScore");
        public static final Property Answerslarge = new Property(9, String.class, "answerslarge", false, "AnswersLarge");
        public static final Property Favorite = new Property(15, Integer.class, "favorite", false, "Favorite");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Lessonsscheduleguid = new Property(1, String.class, "lessonsscheduleguid", false, "LessonsScheduleGUID");
        public static final Property Objectguid = new Property(6, String.class, "objectguid", false, "ObjectGUID");
        public static final Property Objecttype = new Property(7, Integer.class, "objecttype", false, "ObjectType");
        public static final Property State = new Property(14, Integer.class, "state", false, "State");
        public static final Property Studentid = new Property(2, String.class, "studentid", false, "StudentID");
        public static final Property Studentname = new Property(3, String.class, "studentname", false, "StudentName");
        public static final Property Syn_isdelete = new Property(20, Integer.class, "syn_isdelete", false, "SYN_IsDelete");
        public static final Property Syn_timestamp = new Property(19, Date.class, "syn_timestamp", false, "SYN_TimeStamp");
        public static final Property Userclassguid = new Property(4, String.class, UserHonourFragment.USERCLASSGUID, false, "UserClassGUID");
        public static final Property Userclassname = new Property(5, String.class, "userclassname", false, "UserClassName");
        public static final Property Viewdate = new Property(13, Date.class, "viewdate", false, "ViewDate");
        public static final Property Viewresult = new Property(12, Integer.class, "viewresult", false, "ViewResult");
        public static final Property Vote = new Property(16, Integer.class, "vote", false, "Vote");
    }

    public LessonsScheduleResultDao(DaoConfig config) {
        super(config);
    }

    public LessonsScheduleResultDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"LESSONS_SCHEDULE_RESULT\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"LessonsScheduleGUID\" TEXT," + "\"StudentID\" TEXT," + "\"StudentName\" TEXT," + "\"UserClassGUID\" TEXT," + "\"UserClassName\" TEXT," + "\"ObjectGUID\" TEXT," + "\"ObjectType\" INTEGER," + "\"Answers\" TEXT," + "\"AnswersLarge\" TEXT," + "\"AnswerDate\" INTEGER," + "\"AnswerResult\" INTEGER," + "\"ViewResult\" INTEGER," + "\"ViewDate\" INTEGER," + "\"State\" INTEGER," + "\"Favorite\" INTEGER," + "\"Vote\" INTEGER," + "\"AnswerIndex\" INTEGER," + "\"AnswerScore\" REAL," + "\"SYN_TimeStamp\" INTEGER," + "\"SYN_IsDelete\" INTEGER);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_LessonsScheduleGUID ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"LessonsScheduleGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_StudentID ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"StudentID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_StudentName ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"StudentName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_UserClassGUID ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"UserClassGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_UserClassName ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"UserClassName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_ObjectGUID ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"ObjectGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_ObjectType ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"ObjectType\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_Answers ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"Answers\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_AnswersLarge ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"AnswersLarge\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_AnswerDate ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"AnswerDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_AnswerResult ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"AnswerResult\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_ViewResult ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"ViewResult\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_ViewDate ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"ViewDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_State ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"State\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_Favorite ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"Favorite\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_Vote ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"Vote\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_AnswerIndex ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"AnswerIndex\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_AnswerScore ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"AnswerScore\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_SYN_TimeStamp ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"SYN_TimeStamp\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_LESSONS_SCHEDULE_RESULT_SYN_IsDelete ON \"LESSONS_SCHEDULE_RESULT\"" + " (\"SYN_IsDelete\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LESSONS_SCHEDULE_RESULT\"");
    }

    protected final void bindValues(DatabaseStatement stmt, LessonsScheduleResult entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String lessonsscheduleguid = entity.getLessonsscheduleguid();
        if (lessonsscheduleguid != null) {
            stmt.bindString(2, lessonsscheduleguid);
        }
        String studentid = entity.getStudentid();
        if (studentid != null) {
            stmt.bindString(3, studentid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(4, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(5, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(6, userclassname);
        }
        String objectguid = entity.getObjectguid();
        if (objectguid != null) {
            stmt.bindString(7, objectguid);
        }
        Integer objecttype = entity.getObjecttype();
        if (objecttype != null) {
            stmt.bindLong(8, (long) objecttype.intValue());
        }
        String answers = entity.getAnswers();
        if (answers != null) {
            stmt.bindString(9, answers);
        }
        String answerslarge = entity.getAnswerslarge();
        if (answerslarge != null) {
            stmt.bindString(10, answerslarge);
        }
        Date answerdate = entity.getAnswerdate();
        if (answerdate != null) {
            stmt.bindLong(11, answerdate.getTime());
        }
        Integer answerresult = entity.getAnswerresult();
        if (answerresult != null) {
            stmt.bindLong(12, (long) answerresult.intValue());
        }
        Integer viewresult = entity.getViewresult();
        if (viewresult != null) {
            stmt.bindLong(13, (long) viewresult.intValue());
        }
        Date viewdate = entity.getViewdate();
        if (viewdate != null) {
            stmt.bindLong(14, viewdate.getTime());
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(15, (long) state.intValue());
        }
        Integer favorite = entity.getFavorite();
        if (favorite != null) {
            stmt.bindLong(16, (long) favorite.intValue());
        }
        Integer vote = entity.getVote();
        if (vote != null) {
            stmt.bindLong(17, (long) vote.intValue());
        }
        Integer answerindex = entity.getAnswerindex();
        if (answerindex != null) {
            stmt.bindLong(18, (long) answerindex.intValue());
        }
        Float answerscore = entity.getAnswerscore();
        if (answerscore != null) {
            stmt.bindDouble(19, (double) answerscore.floatValue());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(20, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(21, (long) syn_isdelete.intValue());
        }
    }

    protected final void bindValues(SQLiteStatement stmt, LessonsScheduleResult entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String lessonsscheduleguid = entity.getLessonsscheduleguid();
        if (lessonsscheduleguid != null) {
            stmt.bindString(2, lessonsscheduleguid);
        }
        String studentid = entity.getStudentid();
        if (studentid != null) {
            stmt.bindString(3, studentid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(4, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(5, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(6, userclassname);
        }
        String objectguid = entity.getObjectguid();
        if (objectguid != null) {
            stmt.bindString(7, objectguid);
        }
        Integer objecttype = entity.getObjecttype();
        if (objecttype != null) {
            stmt.bindLong(8, (long) objecttype.intValue());
        }
        String answers = entity.getAnswers();
        if (answers != null) {
            stmt.bindString(9, answers);
        }
        String answerslarge = entity.getAnswerslarge();
        if (answerslarge != null) {
            stmt.bindString(10, answerslarge);
        }
        Date answerdate = entity.getAnswerdate();
        if (answerdate != null) {
            stmt.bindLong(11, answerdate.getTime());
        }
        Integer answerresult = entity.getAnswerresult();
        if (answerresult != null) {
            stmt.bindLong(12, (long) answerresult.intValue());
        }
        Integer viewresult = entity.getViewresult();
        if (viewresult != null) {
            stmt.bindLong(13, (long) viewresult.intValue());
        }
        Date viewdate = entity.getViewdate();
        if (viewdate != null) {
            stmt.bindLong(14, viewdate.getTime());
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(15, (long) state.intValue());
        }
        Integer favorite = entity.getFavorite();
        if (favorite != null) {
            stmt.bindLong(16, (long) favorite.intValue());
        }
        Integer vote = entity.getVote();
        if (vote != null) {
            stmt.bindLong(17, (long) vote.intValue());
        }
        Integer answerindex = entity.getAnswerindex();
        if (answerindex != null) {
            stmt.bindLong(18, (long) answerindex.intValue());
        }
        Float answerscore = entity.getAnswerscore();
        if (answerscore != null) {
            stmt.bindDouble(19, (double) answerscore.floatValue());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(20, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(21, (long) syn_isdelete.intValue());
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public LessonsScheduleResult readEntity(Cursor cursor, int offset) {
        Date date;
        Date date2;
        Integer num;
        String string = cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
        String string2 = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
        String string3 = cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2);
        String string4 = cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3);
        String string5 = cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4);
        String string6 = cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5);
        String string7 = cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6);
        Integer valueOf = cursor.isNull(offset + 7) ? null : Integer.valueOf(cursor.getInt(offset + 7));
        String string8 = cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8);
        String string9 = cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9);
        Date date3 = cursor.isNull(offset + 10) ? null : new Date(cursor.getLong(offset + 10));
        Integer valueOf2 = cursor.isNull(offset + 11) ? null : Integer.valueOf(cursor.getInt(offset + 11));
        Integer valueOf3 = cursor.isNull(offset + 12) ? null : Integer.valueOf(cursor.getInt(offset + 12));
        if (cursor.isNull(offset + 13)) {
            date = null;
        } else {
            Date date4 = new Date(cursor.getLong(offset + 13));
        }
        Integer valueOf4 = cursor.isNull(offset + 14) ? null : Integer.valueOf(cursor.getInt(offset + 14));
        Integer valueOf5 = cursor.isNull(offset + 15) ? null : Integer.valueOf(cursor.getInt(offset + 15));
        Integer valueOf6 = cursor.isNull(offset + 16) ? null : Integer.valueOf(cursor.getInt(offset + 16));
        Integer valueOf7 = cursor.isNull(offset + 17) ? null : Integer.valueOf(cursor.getInt(offset + 17));
        Float valueOf8 = cursor.isNull(offset + 18) ? null : Float.valueOf(cursor.getFloat(offset + 18));
        if (cursor.isNull(offset + 19)) {
            date2 = null;
        } else {
            date4 = new Date(cursor.getLong(offset + 19));
        }
        if (cursor.isNull(offset + 20)) {
            num = null;
        } else {
            num = Integer.valueOf(cursor.getInt(offset + 20));
        }
        return new LessonsScheduleResult(string, string2, string3, string4, string5, string6, string7, valueOf, string8, string9, date3, valueOf2, valueOf3, date, valueOf4, valueOf5, valueOf6, valueOf7, valueOf8, date2, num);
    }

    public void readEntity(Cursor cursor, LessonsScheduleResult entity, int offset) {
        Integer num = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setLessonsscheduleguid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setStudentid(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setStudentname(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setUserclassguid(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setUserclassname(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setObjectguid(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setObjecttype(cursor.isNull(offset + 7) ? null : Integer.valueOf(cursor.getInt(offset + 7)));
        entity.setAnswers(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setAnswerslarge(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setAnswerdate(cursor.isNull(offset + 10) ? null : new Date(cursor.getLong(offset + 10)));
        entity.setAnswerresult(cursor.isNull(offset + 11) ? null : Integer.valueOf(cursor.getInt(offset + 11)));
        entity.setViewresult(cursor.isNull(offset + 12) ? null : Integer.valueOf(cursor.getInt(offset + 12)));
        entity.setViewdate(cursor.isNull(offset + 13) ? null : new Date(cursor.getLong(offset + 13)));
        entity.setState(cursor.isNull(offset + 14) ? null : Integer.valueOf(cursor.getInt(offset + 14)));
        entity.setFavorite(cursor.isNull(offset + 15) ? null : Integer.valueOf(cursor.getInt(offset + 15)));
        entity.setVote(cursor.isNull(offset + 16) ? null : Integer.valueOf(cursor.getInt(offset + 16)));
        entity.setAnswerindex(cursor.isNull(offset + 17) ? null : Integer.valueOf(cursor.getInt(offset + 17)));
        entity.setAnswerscore(cursor.isNull(offset + 18) ? null : Float.valueOf(cursor.getFloat(offset + 18)));
        entity.setSyn_timestamp(cursor.isNull(offset + 19) ? null : new Date(cursor.getLong(offset + 19)));
        if (!cursor.isNull(offset + 20)) {
            num = Integer.valueOf(cursor.getInt(offset + 20));
        }
        entity.setSyn_isdelete(num);
    }

    protected final String updateKeyAfterInsert(LessonsScheduleResult entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(LessonsScheduleResult entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(LessonsScheduleResult entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
