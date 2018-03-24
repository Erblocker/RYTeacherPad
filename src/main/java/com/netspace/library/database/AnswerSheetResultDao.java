package com.netspace.library.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import java.util.Date;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

public class AnswerSheetResultDao extends AbstractDao<AnswerSheetResult, String> {
    public static final String TABLENAME = "ANSWER_SHEET_RESULT";

    public static class Properties {
        public static final Property Answercorrecthandwrite = new Property(12, String.class, "answercorrecthandwrite", false, "AnswerCorrectHandWrite");
        public static final Property Answercorrecthandwritepreview = new Property(13, String.class, "answercorrecthandwritepreview", false, "AnswerCorrectHandWritePreview");
        public static final Property Answerindex = new Property(18, Integer.class, "answerindex", false, "AnswerIndex");
        public static final Property Answerresult = new Property(10, Integer.class, "answerresult", false, "AnswerResult");
        public static final Property Answerscore = new Property(11, Float.class, "answerscore", false, "AnswerScore");
        public static final Property Answersheetresourceguid = new Property(2, String.class, "answersheetresourceguid", false, "AnswerSheetResourceGUID");
        public static final Property Clientid = new Property(4, String.class, DeviceOperationRESTServiceProvider.CLIENTID, false, "ClientID");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Questionguid = new Property(3, String.class, "questionguid", false, "QuestionGUID");
        public static final Property Scheduleguid = new Property(1, String.class, "scheduleguid", false, "ScheduleGUID");
        public static final Property Scoredate = new Property(14, Date.class, "scoredate", false, "ScoreDate");
        public static final Property State = new Property(17, Integer.class, "state", false, "State");
        public static final Property Studentname = new Property(7, String.class, "studentname", false, "StudentName");
        public static final Property Syn_isdelete = new Property(16, Integer.class, "syn_isdelete", false, "SYN_IsDelete");
        public static final Property Syn_timestamp = new Property(15, Date.class, "syn_timestamp", false, "SYN_TimeStamp");
        public static final Property Userclassguid = new Property(8, String.class, UserHonourFragment.USERCLASSGUID, false, "UserClassGUID");
        public static final Property Userclassname = new Property(9, String.class, "userclassname", false, "UserClassName");
        public static final Property Userguid = new Property(6, String.class, "userguid", false, "UserGUID");
        public static final Property Username = new Property(5, String.class, UserHonourFragment.USERNAME, false, "UserName");
    }

    public AnswerSheetResultDao(DaoConfig config) {
        super(config);
    }

    public AnswerSheetResultDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"ANSWER_SHEET_RESULT\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"ScheduleGUID\" TEXT," + "\"AnswerSheetResourceGUID\" TEXT," + "\"QuestionGUID\" TEXT," + "\"ClientID\" TEXT," + "\"UserName\" TEXT," + "\"UserGUID\" TEXT," + "\"StudentName\" TEXT," + "\"UserClassGUID\" TEXT," + "\"UserClassName\" TEXT," + "\"AnswerResult\" INTEGER," + "\"AnswerScore\" REAL," + "\"AnswerCorrectHandWrite\" TEXT," + "\"AnswerCorrectHandWritePreview\" TEXT," + "\"ScoreDate\" INTEGER," + "\"SYN_TimeStamp\" INTEGER," + "\"SYN_IsDelete\" INTEGER," + "\"State\" INTEGER," + "\"AnswerIndex\" INTEGER);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_ScheduleGUID ON \"ANSWER_SHEET_RESULT\"" + " (\"ScheduleGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_AnswerSheetResourceGUID ON \"ANSWER_SHEET_RESULT\"" + " (\"AnswerSheetResourceGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_QuestionGUID ON \"ANSWER_SHEET_RESULT\"" + " (\"QuestionGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_ClientID ON \"ANSWER_SHEET_RESULT\"" + " (\"ClientID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_UserName ON \"ANSWER_SHEET_RESULT\"" + " (\"UserName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_UserGUID ON \"ANSWER_SHEET_RESULT\"" + " (\"UserGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_StudentName ON \"ANSWER_SHEET_RESULT\"" + " (\"StudentName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_UserClassGUID ON \"ANSWER_SHEET_RESULT\"" + " (\"UserClassGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_UserClassName ON \"ANSWER_SHEET_RESULT\"" + " (\"UserClassName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_AnswerResult ON \"ANSWER_SHEET_RESULT\"" + " (\"AnswerResult\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_AnswerScore ON \"ANSWER_SHEET_RESULT\"" + " (\"AnswerScore\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_AnswerCorrectHandWrite ON \"ANSWER_SHEET_RESULT\"" + " (\"AnswerCorrectHandWrite\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_ScoreDate ON \"ANSWER_SHEET_RESULT\"" + " (\"ScoreDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_SYN_TimeStamp ON \"ANSWER_SHEET_RESULT\"" + " (\"SYN_TimeStamp\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_SYN_IsDelete ON \"ANSWER_SHEET_RESULT\"" + " (\"SYN_IsDelete\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_State ON \"ANSWER_SHEET_RESULT\"" + " (\"State\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_RESULT_AnswerIndex ON \"ANSWER_SHEET_RESULT\"" + " (\"AnswerIndex\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ANSWER_SHEET_RESULT\"");
    }

    protected final void bindValues(DatabaseStatement stmt, AnswerSheetResult entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String scheduleguid = entity.getScheduleguid();
        if (scheduleguid != null) {
            stmt.bindString(2, scheduleguid);
        }
        String answersheetresourceguid = entity.getAnswersheetresourceguid();
        if (answersheetresourceguid != null) {
            stmt.bindString(3, answersheetresourceguid);
        }
        String questionguid = entity.getQuestionguid();
        if (questionguid != null) {
            stmt.bindString(4, questionguid);
        }
        String clientid = entity.getClientid();
        if (clientid != null) {
            stmt.bindString(5, clientid);
        }
        String username = entity.getUsername();
        if (username != null) {
            stmt.bindString(6, username);
        }
        String userguid = entity.getUserguid();
        if (userguid != null) {
            stmt.bindString(7, userguid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(8, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(9, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(10, userclassname);
        }
        Integer answerresult = entity.getAnswerresult();
        if (answerresult != null) {
            stmt.bindLong(11, (long) answerresult.intValue());
        }
        Float answerscore = entity.getAnswerscore();
        if (answerscore != null) {
            stmt.bindDouble(12, (double) answerscore.floatValue());
        }
        String answercorrecthandwrite = entity.getAnswercorrecthandwrite();
        if (answercorrecthandwrite != null) {
            stmt.bindString(13, answercorrecthandwrite);
        }
        String answercorrecthandwritepreview = entity.getAnswercorrecthandwritepreview();
        if (answercorrecthandwritepreview != null) {
            stmt.bindString(14, answercorrecthandwritepreview);
        }
        Date scoredate = entity.getScoredate();
        if (scoredate != null) {
            stmt.bindLong(15, scoredate.getTime());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(16, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(17, (long) syn_isdelete.intValue());
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(18, (long) state.intValue());
        }
        Integer answerindex = entity.getAnswerindex();
        if (answerindex != null) {
            stmt.bindLong(19, (long) answerindex.intValue());
        }
    }

    protected final void bindValues(SQLiteStatement stmt, AnswerSheetResult entity) {
        stmt.clearBindings();
        String guid = entity.getGuid();
        if (guid != null) {
            stmt.bindString(1, guid);
        }
        String scheduleguid = entity.getScheduleguid();
        if (scheduleguid != null) {
            stmt.bindString(2, scheduleguid);
        }
        String answersheetresourceguid = entity.getAnswersheetresourceguid();
        if (answersheetresourceguid != null) {
            stmt.bindString(3, answersheetresourceguid);
        }
        String questionguid = entity.getQuestionguid();
        if (questionguid != null) {
            stmt.bindString(4, questionguid);
        }
        String clientid = entity.getClientid();
        if (clientid != null) {
            stmt.bindString(5, clientid);
        }
        String username = entity.getUsername();
        if (username != null) {
            stmt.bindString(6, username);
        }
        String userguid = entity.getUserguid();
        if (userguid != null) {
            stmt.bindString(7, userguid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(8, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(9, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(10, userclassname);
        }
        Integer answerresult = entity.getAnswerresult();
        if (answerresult != null) {
            stmt.bindLong(11, (long) answerresult.intValue());
        }
        Float answerscore = entity.getAnswerscore();
        if (answerscore != null) {
            stmt.bindDouble(12, (double) answerscore.floatValue());
        }
        String answercorrecthandwrite = entity.getAnswercorrecthandwrite();
        if (answercorrecthandwrite != null) {
            stmt.bindString(13, answercorrecthandwrite);
        }
        String answercorrecthandwritepreview = entity.getAnswercorrecthandwritepreview();
        if (answercorrecthandwritepreview != null) {
            stmt.bindString(14, answercorrecthandwritepreview);
        }
        Date scoredate = entity.getScoredate();
        if (scoredate != null) {
            stmt.bindLong(15, scoredate.getTime());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(16, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(17, (long) syn_isdelete.intValue());
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(18, (long) state.intValue());
        }
        Integer answerindex = entity.getAnswerindex();
        if (answerindex != null) {
            stmt.bindLong(19, (long) answerindex.intValue());
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public AnswerSheetResult readEntity(Cursor cursor, int offset) {
        Date date;
        Integer num;
        String string = cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
        String string2 = cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1);
        String string3 = cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2);
        String string4 = cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3);
        String string5 = cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4);
        String string6 = cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5);
        String string7 = cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6);
        String string8 = cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7);
        String string9 = cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8);
        String string10 = cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9);
        Integer valueOf = cursor.isNull(offset + 10) ? null : Integer.valueOf(cursor.getInt(offset + 10));
        Float valueOf2 = cursor.isNull(offset + 11) ? null : Float.valueOf(cursor.getFloat(offset + 11));
        String string11 = cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12);
        String string12 = cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13);
        Date date2 = cursor.isNull(offset + 14) ? null : new Date(cursor.getLong(offset + 14));
        if (cursor.isNull(offset + 15)) {
            date = null;
        } else {
            Date date3 = new Date(cursor.getLong(offset + 15));
        }
        Integer valueOf3 = cursor.isNull(offset + 16) ? null : Integer.valueOf(cursor.getInt(offset + 16));
        Integer valueOf4 = cursor.isNull(offset + 17) ? null : Integer.valueOf(cursor.getInt(offset + 17));
        if (cursor.isNull(offset + 18)) {
            num = null;
        } else {
            num = Integer.valueOf(cursor.getInt(offset + 18));
        }
        return new AnswerSheetResult(string, string2, string3, string4, string5, string6, string7, string8, string9, string10, valueOf, valueOf2, string11, string12, date2, date, valueOf3, valueOf4, num);
    }

    public void readEntity(Cursor cursor, AnswerSheetResult entity, int offset) {
        Integer num = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setScheduleguid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setAnswersheetresourceguid(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setQuestionguid(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setClientid(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setUsername(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setUserguid(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setStudentname(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setUserclassguid(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setUserclassname(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setAnswerresult(cursor.isNull(offset + 10) ? null : Integer.valueOf(cursor.getInt(offset + 10)));
        entity.setAnswerscore(cursor.isNull(offset + 11) ? null : Float.valueOf(cursor.getFloat(offset + 11)));
        entity.setAnswercorrecthandwrite(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setAnswercorrecthandwritepreview(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setScoredate(cursor.isNull(offset + 14) ? null : new Date(cursor.getLong(offset + 14)));
        entity.setSyn_timestamp(cursor.isNull(offset + 15) ? null : new Date(cursor.getLong(offset + 15)));
        entity.setSyn_isdelete(cursor.isNull(offset + 16) ? null : Integer.valueOf(cursor.getInt(offset + 16)));
        entity.setState(cursor.isNull(offset + 17) ? null : Integer.valueOf(cursor.getInt(offset + 17)));
        if (!cursor.isNull(offset + 18)) {
            num = Integer.valueOf(cursor.getInt(offset + 18));
        }
        entity.setAnswerindex(num);
    }

    protected final String updateKeyAfterInsert(AnswerSheetResult entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(AnswerSheetResult entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(AnswerSheetResult entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
