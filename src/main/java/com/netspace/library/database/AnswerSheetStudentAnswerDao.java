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

public class AnswerSheetStudentAnswerDao extends AbstractDao<AnswerSheetStudentAnswer, String> {
    public static final String TABLENAME = "ANSWER_SHEET_STUDENT_ANSWER";

    public static class Properties {
        public static final Property Answercamera = new Property(15, String.class, "answercamera", false, "AnswerCamera");
        public static final Property Answerchoice = new Property(13, String.class, "answerchoice", false, "AnswerChoice");
        public static final Property Answerdate = new Property(16, Date.class, "answerdate", false, "AnswerDate");
        public static final Property Answerhandwritedata = new Property(11, String.class, "answerhandwritedata", false, "AnswerHandWriteData");
        public static final Property Answerhandwritepreview = new Property(12, String.class, "answerhandwritepreview", false, "AnswerHandWritePreview");
        public static final Property Answerindex = new Property(19, Integer.class, "answerindex", false, "AnswerIndex");
        public static final Property Answersheetresourceguid = new Property(2, String.class, "answersheetresourceguid", false, "AnswerSheetResourceGUID");
        public static final Property Answertext = new Property(14, String.class, "answertext", false, "AnswerText");
        public static final Property Clientid = new Property(4, String.class, DeviceOperationRESTServiceProvider.CLIENTID, false, "ClientID");
        public static final Property Favorite = new Property(18, Integer.class, "favorite", false, "Favorite");
        public static final Property Guid = new Property(0, String.class, "guid", true, "GUID");
        public static final Property Notifyclientid = new Property(5, String.class, "notifyclientid", false, "NotifyClientID");
        public static final Property Questionguid = new Property(3, String.class, "questionguid", false, "QuestionGUID");
        public static final Property Scheduleguid = new Property(1, String.class, "scheduleguid", false, "ScheduleGUID");
        public static final Property State = new Property(17, Integer.class, "state", false, "State");
        public static final Property Studentname = new Property(8, String.class, "studentname", false, "StudentName");
        public static final Property Syn_isdelete = new Property(21, Integer.class, "syn_isdelete", false, "SYN_IsDelete");
        public static final Property Syn_timestamp = new Property(20, Date.class, "syn_timestamp", false, "SYN_TimeStamp");
        public static final Property Userclassguid = new Property(9, String.class, UserHonourFragment.USERCLASSGUID, false, "UserClassGUID");
        public static final Property Userclassname = new Property(10, String.class, "userclassname", false, "UserClassName");
        public static final Property Userguid = new Property(7, String.class, "userguid", false, "UserGUID");
        public static final Property Username = new Property(6, String.class, UserHonourFragment.USERNAME, false, "UserName");
    }

    public AnswerSheetStudentAnswerDao(DaoConfig config) {
        super(config);
    }

    public AnswerSheetStudentAnswerDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"ANSWER_SHEET_STUDENT_ANSWER\" (" + "\"GUID\" TEXT PRIMARY KEY NOT NULL ," + "\"ScheduleGUID\" TEXT," + "\"AnswerSheetResourceGUID\" TEXT," + "\"QuestionGUID\" TEXT," + "\"ClientID\" TEXT," + "\"NotifyClientID\" TEXT," + "\"UserName\" TEXT," + "\"UserGUID\" TEXT," + "\"StudentName\" TEXT," + "\"UserClassGUID\" TEXT," + "\"UserClassName\" TEXT," + "\"AnswerHandWriteData\" TEXT," + "\"AnswerHandWritePreview\" TEXT," + "\"AnswerChoice\" TEXT," + "\"AnswerText\" TEXT," + "\"AnswerCamera\" TEXT," + "\"AnswerDate\" INTEGER," + "\"State\" INTEGER," + "\"Favorite\" INTEGER," + "\"AnswerIndex\" INTEGER," + "\"SYN_TimeStamp\" INTEGER," + "\"SYN_IsDelete\" INTEGER);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_ScheduleGUID ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"ScheduleGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_AnswerSheetResourceGUID ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"AnswerSheetResourceGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_QuestionGUID ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"QuestionGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_ClientID ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"ClientID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_NotifyClientID ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"NotifyClientID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_UserName ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"UserName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_UserGUID ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"UserGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_StudentName ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"StudentName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_UserClassGUID ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"UserClassGUID\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_UserClassName ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"UserClassName\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_AnswerDate ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"AnswerDate\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_State ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"State\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_Favorite ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"Favorite\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_AnswerIndex ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"AnswerIndex\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_SYN_TimeStamp ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"SYN_TimeStamp\");");
        db.execSQL("CREATE INDEX " + constraint + "IDX_ANSWER_SHEET_STUDENT_ANSWER_SYN_IsDelete ON \"ANSWER_SHEET_STUDENT_ANSWER\"" + " (\"SYN_IsDelete\");");
    }

    public static void dropTable(Database db, boolean ifExists) {
        db.execSQL("DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ANSWER_SHEET_STUDENT_ANSWER\"");
    }

    protected final void bindValues(DatabaseStatement stmt, AnswerSheetStudentAnswer entity) {
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
        String notifyclientid = entity.getNotifyclientid();
        if (notifyclientid != null) {
            stmt.bindString(6, notifyclientid);
        }
        String username = entity.getUsername();
        if (username != null) {
            stmt.bindString(7, username);
        }
        String userguid = entity.getUserguid();
        if (userguid != null) {
            stmt.bindString(8, userguid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(9, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(10, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(11, userclassname);
        }
        String answerhandwritedata = entity.getAnswerhandwritedata();
        if (answerhandwritedata != null) {
            stmt.bindString(12, answerhandwritedata);
        }
        String answerhandwritepreview = entity.getAnswerhandwritepreview();
        if (answerhandwritepreview != null) {
            stmt.bindString(13, answerhandwritepreview);
        }
        String answerchoice = entity.getAnswerchoice();
        if (answerchoice != null) {
            stmt.bindString(14, answerchoice);
        }
        String answertext = entity.getAnswertext();
        if (answertext != null) {
            stmt.bindString(15, answertext);
        }
        String answercamera = entity.getAnswercamera();
        if (answercamera != null) {
            stmt.bindString(16, answercamera);
        }
        Date answerdate = entity.getAnswerdate();
        if (answerdate != null) {
            stmt.bindLong(17, answerdate.getTime());
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(18, (long) state.intValue());
        }
        Integer favorite = entity.getFavorite();
        if (favorite != null) {
            stmt.bindLong(19, (long) favorite.intValue());
        }
        Integer answerindex = entity.getAnswerindex();
        if (answerindex != null) {
            stmt.bindLong(20, (long) answerindex.intValue());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(21, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(22, (long) syn_isdelete.intValue());
        }
    }

    protected final void bindValues(SQLiteStatement stmt, AnswerSheetStudentAnswer entity) {
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
        String notifyclientid = entity.getNotifyclientid();
        if (notifyclientid != null) {
            stmt.bindString(6, notifyclientid);
        }
        String username = entity.getUsername();
        if (username != null) {
            stmt.bindString(7, username);
        }
        String userguid = entity.getUserguid();
        if (userguid != null) {
            stmt.bindString(8, userguid);
        }
        String studentname = entity.getStudentname();
        if (studentname != null) {
            stmt.bindString(9, studentname);
        }
        String userclassguid = entity.getUserclassguid();
        if (userclassguid != null) {
            stmt.bindString(10, userclassguid);
        }
        String userclassname = entity.getUserclassname();
        if (userclassname != null) {
            stmt.bindString(11, userclassname);
        }
        String answerhandwritedata = entity.getAnswerhandwritedata();
        if (answerhandwritedata != null) {
            stmt.bindString(12, answerhandwritedata);
        }
        String answerhandwritepreview = entity.getAnswerhandwritepreview();
        if (answerhandwritepreview != null) {
            stmt.bindString(13, answerhandwritepreview);
        }
        String answerchoice = entity.getAnswerchoice();
        if (answerchoice != null) {
            stmt.bindString(14, answerchoice);
        }
        String answertext = entity.getAnswertext();
        if (answertext != null) {
            stmt.bindString(15, answertext);
        }
        String answercamera = entity.getAnswercamera();
        if (answercamera != null) {
            stmt.bindString(16, answercamera);
        }
        Date answerdate = entity.getAnswerdate();
        if (answerdate != null) {
            stmt.bindLong(17, answerdate.getTime());
        }
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(18, (long) state.intValue());
        }
        Integer favorite = entity.getFavorite();
        if (favorite != null) {
            stmt.bindLong(19, (long) favorite.intValue());
        }
        Integer answerindex = entity.getAnswerindex();
        if (answerindex != null) {
            stmt.bindLong(20, (long) answerindex.intValue());
        }
        Date syn_timestamp = entity.getSyn_timestamp();
        if (syn_timestamp != null) {
            stmt.bindLong(21, syn_timestamp.getTime());
        }
        Integer syn_isdelete = entity.getSyn_isdelete();
        if (syn_isdelete != null) {
            stmt.bindLong(22, (long) syn_isdelete.intValue());
        }
    }

    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    public AnswerSheetStudentAnswer readEntity(Cursor cursor, int offset) {
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
        String string11 = cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10);
        String string12 = cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11);
        String string13 = cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12);
        String string14 = cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13);
        String string15 = cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14);
        String string16 = cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15);
        Date date = cursor.isNull(offset + 16) ? null : new Date(cursor.getLong(offset + 16));
        Integer valueOf = cursor.isNull(offset + 17) ? null : Integer.valueOf(cursor.getInt(offset + 17));
        Integer valueOf2 = cursor.isNull(offset + 18) ? null : Integer.valueOf(cursor.getInt(offset + 18));
        Integer valueOf3 = cursor.isNull(offset + 19) ? null : Integer.valueOf(cursor.getInt(offset + 19));
        Date date2 = cursor.isNull(offset + 20) ? null : new Date(cursor.getLong(offset + 20));
        if (cursor.isNull(offset + 21)) {
            num = null;
        } else {
            num = Integer.valueOf(cursor.getInt(offset + 21));
        }
        return new AnswerSheetStudentAnswer(string, string2, string3, string4, string5, string6, string7, string8, string9, string10, string11, string12, string13, string14, string15, string16, date, valueOf, valueOf2, valueOf3, date2, num);
    }

    public void readEntity(Cursor cursor, AnswerSheetStudentAnswer entity, int offset) {
        Integer num = null;
        entity.setGuid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setScheduleguid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setAnswersheetresourceguid(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setQuestionguid(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setClientid(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setNotifyclientid(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setUsername(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setUserguid(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setStudentname(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setUserclassguid(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setUserclassname(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setAnswerhandwritedata(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setAnswerhandwritepreview(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setAnswerchoice(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setAnswertext(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setAnswercamera(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setAnswerdate(cursor.isNull(offset + 16) ? null : new Date(cursor.getLong(offset + 16)));
        entity.setState(cursor.isNull(offset + 17) ? null : Integer.valueOf(cursor.getInt(offset + 17)));
        entity.setFavorite(cursor.isNull(offset + 18) ? null : Integer.valueOf(cursor.getInt(offset + 18)));
        entity.setAnswerindex(cursor.isNull(offset + 19) ? null : Integer.valueOf(cursor.getInt(offset + 19)));
        entity.setSyn_timestamp(cursor.isNull(offset + 20) ? null : new Date(cursor.getLong(offset + 20)));
        if (!cursor.isNull(offset + 21)) {
            num = Integer.valueOf(cursor.getInt(offset + 21));
        }
        entity.setSyn_isdelete(num);
    }

    protected final String updateKeyAfterInsert(AnswerSheetStudentAnswer entity, long rowId) {
        return entity.getGuid();
    }

    public String getKey(AnswerSheetStudentAnswer entity) {
        if (entity != null) {
            return entity.getGuid();
        }
        return null;
    }

    public boolean hasKey(AnswerSheetStudentAnswer entity) {
        return entity.getGuid() != null;
    }

    protected final boolean isEntityUpdateable() {
        return true;
    }
}
