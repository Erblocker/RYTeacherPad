package com.netspace.library.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import org.greenrobot.greendao.AbstractDaoMaster;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;
import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 3;

    public static abstract class OpenHelper extends DatabaseOpenHelper {
        public OpenHelper(Context context, String name) {
            super(context, name, 3);
        }

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, 3);
        }

        public void onCreate(Database db) {
            Log.i("greenDAO", "Creating tables for schema version 3");
            DaoMaster.createAllTables(db, false);
        }
    }

    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name) {
            super(context, name);
        }

        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            DaoMaster.dropAllTables(db, true);
            onCreate(db);
        }
    }

    public static void createAllTables(Database db, boolean ifNotExists) {
        AnswerSheetStudentAnswerDao.createTable(db, ifNotExists);
        AnswerSheetResultDao.createTable(db, ifNotExists);
        QuestionsDao.createTable(db, ifNotExists);
        ResourcesDao.createTable(db, ifNotExists);
        LessonsScheduleDao.createTable(db, ifNotExists);
        LessonsScheduleResultDao.createTable(db, ifNotExists);
        StudentQuestionBookDao.createTable(db, ifNotExists);
        DataSynchronizeDao.createTable(db, ifNotExists);
        IMMessagesDao.createTable(db, ifNotExists);
    }

    public static void dropAllTables(Database db, boolean ifExists) {
        AnswerSheetStudentAnswerDao.dropTable(db, ifExists);
        AnswerSheetResultDao.dropTable(db, ifExists);
        QuestionsDao.dropTable(db, ifExists);
        ResourcesDao.dropTable(db, ifExists);
        LessonsScheduleDao.dropTable(db, ifExists);
        LessonsScheduleResultDao.dropTable(db, ifExists);
        StudentQuestionBookDao.dropTable(db, ifExists);
        DataSynchronizeDao.dropTable(db, ifExists);
        IMMessagesDao.dropTable(db, ifExists);
    }

    public static DaoSession newDevSession(Context context, String name) {
        return new DaoMaster(new DevOpenHelper(context, name).getWritableDb()).newSession();
    }

    public DaoMaster(SQLiteDatabase db) {
        this(new StandardDatabase(db));
    }

    public DaoMaster(Database db) {
        super(db, 3);
        registerDaoClass(AnswerSheetStudentAnswerDao.class);
        registerDaoClass(AnswerSheetResultDao.class);
        registerDaoClass(QuestionsDao.class);
        registerDaoClass(ResourcesDao.class);
        registerDaoClass(LessonsScheduleDao.class);
        registerDaoClass(LessonsScheduleResultDao.class);
        registerDaoClass(StudentQuestionBookDao.class);
        registerDaoClass(DataSynchronizeDao.class);
        registerDaoClass(IMMessagesDao.class);
    }

    public DaoSession newSession() {
        return new DaoSession(this.db, IdentityScopeType.Session, this.daoConfigMap);
    }

    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(this.db, type, this.daoConfigMap);
    }
}
