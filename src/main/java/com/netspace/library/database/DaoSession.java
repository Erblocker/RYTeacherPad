package com.netspace.library.database;

import java.util.Map;
import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

public class DaoSession extends AbstractDaoSession {
    private final AnswerSheetResultDao answerSheetResultDao = new AnswerSheetResultDao(this.answerSheetResultDaoConfig, this);
    private final DaoConfig answerSheetResultDaoConfig;
    private final AnswerSheetStudentAnswerDao answerSheetStudentAnswerDao = new AnswerSheetStudentAnswerDao(this.answerSheetStudentAnswerDaoConfig, this);
    private final DaoConfig answerSheetStudentAnswerDaoConfig;
    private final DataSynchronizeDao dataSynchronizeDao = new DataSynchronizeDao(this.dataSynchronizeDaoConfig, this);
    private final DaoConfig dataSynchronizeDaoConfig;
    private final IMMessagesDao iMMessagesDao = new IMMessagesDao(this.iMMessagesDaoConfig, this);
    private final DaoConfig iMMessagesDaoConfig;
    private final LessonsScheduleDao lessonsScheduleDao = new LessonsScheduleDao(this.lessonsScheduleDaoConfig, this);
    private final DaoConfig lessonsScheduleDaoConfig;
    private final LessonsScheduleResultDao lessonsScheduleResultDao = new LessonsScheduleResultDao(this.lessonsScheduleResultDaoConfig, this);
    private final DaoConfig lessonsScheduleResultDaoConfig;
    private final QuestionsDao questionsDao = new QuestionsDao(this.questionsDaoConfig, this);
    private final DaoConfig questionsDaoConfig;
    private final ResourcesDao resourcesDao = new ResourcesDao(this.resourcesDaoConfig, this);
    private final DaoConfig resourcesDaoConfig;
    private final StudentQuestionBookDao studentQuestionBookDao = new StudentQuestionBookDao(this.studentQuestionBookDaoConfig, this);
    private final DaoConfig studentQuestionBookDaoConfig;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig> daoConfigMap) {
        super(db);
        this.answerSheetStudentAnswerDaoConfig = ((DaoConfig) daoConfigMap.get(AnswerSheetStudentAnswerDao.class)).clone();
        this.answerSheetStudentAnswerDaoConfig.initIdentityScope(type);
        this.answerSheetResultDaoConfig = ((DaoConfig) daoConfigMap.get(AnswerSheetResultDao.class)).clone();
        this.answerSheetResultDaoConfig.initIdentityScope(type);
        this.questionsDaoConfig = ((DaoConfig) daoConfigMap.get(QuestionsDao.class)).clone();
        this.questionsDaoConfig.initIdentityScope(type);
        this.resourcesDaoConfig = ((DaoConfig) daoConfigMap.get(ResourcesDao.class)).clone();
        this.resourcesDaoConfig.initIdentityScope(type);
        this.lessonsScheduleDaoConfig = ((DaoConfig) daoConfigMap.get(LessonsScheduleDao.class)).clone();
        this.lessonsScheduleDaoConfig.initIdentityScope(type);
        this.lessonsScheduleResultDaoConfig = ((DaoConfig) daoConfigMap.get(LessonsScheduleResultDao.class)).clone();
        this.lessonsScheduleResultDaoConfig.initIdentityScope(type);
        this.studentQuestionBookDaoConfig = ((DaoConfig) daoConfigMap.get(StudentQuestionBookDao.class)).clone();
        this.studentQuestionBookDaoConfig.initIdentityScope(type);
        this.dataSynchronizeDaoConfig = ((DaoConfig) daoConfigMap.get(DataSynchronizeDao.class)).clone();
        this.dataSynchronizeDaoConfig.initIdentityScope(type);
        this.iMMessagesDaoConfig = ((DaoConfig) daoConfigMap.get(IMMessagesDao.class)).clone();
        this.iMMessagesDaoConfig.initIdentityScope(type);
        registerDao(AnswerSheetStudentAnswer.class, this.answerSheetStudentAnswerDao);
        registerDao(AnswerSheetResult.class, this.answerSheetResultDao);
        registerDao(Questions.class, this.questionsDao);
        registerDao(Resources.class, this.resourcesDao);
        registerDao(LessonsSchedule.class, this.lessonsScheduleDao);
        registerDao(LessonsScheduleResult.class, this.lessonsScheduleResultDao);
        registerDao(StudentQuestionBook.class, this.studentQuestionBookDao);
        registerDao(DataSynchronize.class, this.dataSynchronizeDao);
        registerDao(IMMessages.class, this.iMMessagesDao);
    }

    public void clear() {
        this.answerSheetStudentAnswerDaoConfig.clearIdentityScope();
        this.answerSheetResultDaoConfig.clearIdentityScope();
        this.questionsDaoConfig.clearIdentityScope();
        this.resourcesDaoConfig.clearIdentityScope();
        this.lessonsScheduleDaoConfig.clearIdentityScope();
        this.lessonsScheduleResultDaoConfig.clearIdentityScope();
        this.studentQuestionBookDaoConfig.clearIdentityScope();
        this.dataSynchronizeDaoConfig.clearIdentityScope();
        this.iMMessagesDaoConfig.clearIdentityScope();
    }

    public AnswerSheetStudentAnswerDao getAnswerSheetStudentAnswerDao() {
        return this.answerSheetStudentAnswerDao;
    }

    public AnswerSheetResultDao getAnswerSheetResultDao() {
        return this.answerSheetResultDao;
    }

    public QuestionsDao getQuestionsDao() {
        return this.questionsDao;
    }

    public ResourcesDao getResourcesDao() {
        return this.resourcesDao;
    }

    public LessonsScheduleDao getLessonsScheduleDao() {
        return this.lessonsScheduleDao;
    }

    public LessonsScheduleResultDao getLessonsScheduleResultDao() {
        return this.lessonsScheduleResultDao;
    }

    public StudentQuestionBookDao getStudentQuestionBookDao() {
        return this.studentQuestionBookDao;
    }

    public DataSynchronizeDao getDataSynchronizeDao() {
        return this.dataSynchronizeDao;
    }

    public IMMessagesDao getIMMessagesDao() {
        return this.iMMessagesDao;
    }
}
