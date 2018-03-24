package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class LessonsScheduleResult extends CRestDataBase {
    private Date answerdate;
    private Integer answerindex;
    private Integer answerresult;
    private String answers;
    private Float answerscore;
    private String answerslarge;
    private Integer favorite;
    private String guid;
    private String lessonsscheduleguid;
    private String objectguid;
    private Integer objecttype;
    private Integer state;
    private String studentid;
    private String studentname;
    private Integer syn_isdelete;
    private Date syn_timestamp;
    private String userclassguid;
    private String userclassname;
    private Date viewdate;
    private Integer viewresult;
    private Integer vote;

    public LessonsScheduleResult(String guid) {
        this.guid = guid;
    }

    public LessonsScheduleResult(String guid, String lessonsscheduleguid, String studentid, String studentname, String userclassguid, String userclassname, String objectguid, Integer objecttype, String answers, String answerslarge, Date answerdate, Integer answerresult, Integer viewresult, Date viewdate, Integer state, Integer favorite, Integer vote, Integer answerindex, Float answerscore, Date syn_timestamp, Integer syn_isdelete) {
        this.guid = guid;
        this.lessonsscheduleguid = lessonsscheduleguid;
        this.studentid = studentid;
        this.studentname = studentname;
        this.userclassguid = userclassguid;
        this.userclassname = userclassname;
        this.objectguid = objectguid;
        this.objecttype = objecttype;
        this.answers = answers;
        this.answerslarge = answerslarge;
        this.answerdate = answerdate;
        this.answerresult = answerresult;
        this.viewresult = viewresult;
        this.viewdate = viewdate;
        this.state = state;
        this.favorite = favorite;
        this.vote = vote;
        this.answerindex = answerindex;
        this.answerscore = answerscore;
        this.syn_timestamp = syn_timestamp;
        this.syn_isdelete = syn_isdelete;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getLessonsscheduleguid() {
        return this.lessonsscheduleguid;
    }

    public void setLessonsscheduleguid(String lessonsscheduleguid) {
        this.lessonsscheduleguid = lessonsscheduleguid;
    }

    public String getStudentid() {
        return this.studentid;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
    }

    public String getStudentname() {
        return this.studentname;
    }

    public void setStudentname(String studentname) {
        this.studentname = studentname;
    }

    public String getUserclassguid() {
        return this.userclassguid;
    }

    public void setUserclassguid(String userclassguid) {
        this.userclassguid = userclassguid;
    }

    public String getUserclassname() {
        return this.userclassname;
    }

    public void setUserclassname(String userclassname) {
        this.userclassname = userclassname;
    }

    public String getObjectguid() {
        return this.objectguid;
    }

    public void setObjectguid(String objectguid) {
        this.objectguid = objectguid;
    }

    public Integer getObjecttype() {
        return this.objecttype;
    }

    public void setObjecttype(Integer objecttype) {
        this.objecttype = objecttype;
    }

    public String getAnswers() {
        return this.answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getAnswerslarge() {
        return this.answerslarge;
    }

    public void setAnswerslarge(String answerslarge) {
        this.answerslarge = answerslarge;
    }

    public Date getAnswerdate() {
        return this.answerdate;
    }

    public void setAnswerdate(Date answerdate) {
        this.answerdate = answerdate;
    }

    public Integer getAnswerresult() {
        return this.answerresult;
    }

    public void setAnswerresult(Integer answerresult) {
        this.answerresult = answerresult;
    }

    public Integer getViewresult() {
        return this.viewresult;
    }

    public void setViewresult(Integer viewresult) {
        this.viewresult = viewresult;
    }

    public Date getViewdate() {
        return this.viewdate;
    }

    public void setViewdate(Date viewdate) {
        this.viewdate = viewdate;
    }

    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getFavorite() {
        return this.favorite;
    }

    public void setFavorite(Integer favorite) {
        this.favorite = favorite;
    }

    public Integer getVote() {
        return this.vote;
    }

    public void setVote(Integer vote) {
        this.vote = vote;
    }

    public Integer getAnswerindex() {
        return this.answerindex;
    }

    public void setAnswerindex(Integer answerindex) {
        this.answerindex = answerindex;
    }

    public Float getAnswerscore() {
        return this.answerscore;
    }

    public void setAnswerscore(Float answerscore) {
        this.answerscore = answerscore;
    }

    public Date getSyn_timestamp() {
        return this.syn_timestamp;
    }

    public void setSyn_timestamp(Date syn_timestamp) {
        this.syn_timestamp = syn_timestamp;
    }

    public Integer getSyn_isdelete() {
        return this.syn_isdelete;
    }

    public void setSyn_isdelete(Integer syn_isdelete) {
        this.syn_isdelete = syn_isdelete;
    }
}
