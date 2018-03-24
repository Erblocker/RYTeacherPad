package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class AnswerSheetStudentAnswer extends CRestDataBase {
    private String answercamera;
    private String answerchoice;
    private Date answerdate;
    private String answerhandwritedata;
    private String answerhandwritepreview;
    private Integer answerindex;
    private String answersheetresourceguid;
    private String answertext;
    private String clientid;
    private Integer favorite;
    private String guid;
    private String notifyclientid;
    private String questionguid;
    private String scheduleguid;
    private Integer state;
    private String studentname;
    private Integer syn_isdelete;
    private Date syn_timestamp;
    private String userclassguid;
    private String userclassname;
    private String userguid;
    private String username;

    public AnswerSheetStudentAnswer(String guid) {
        this.guid = guid;
    }

    public AnswerSheetStudentAnswer(String guid, String scheduleguid, String answersheetresourceguid, String questionguid, String clientid, String notifyclientid, String username, String userguid, String studentname, String userclassguid, String userclassname, String answerhandwritedata, String answerhandwritepreview, String answerchoice, String answertext, String answercamera, Date answerdate, Integer state, Integer favorite, Integer answerindex, Date syn_timestamp, Integer syn_isdelete) {
        this.guid = guid;
        this.scheduleguid = scheduleguid;
        this.answersheetresourceguid = answersheetresourceguid;
        this.questionguid = questionguid;
        this.clientid = clientid;
        this.notifyclientid = notifyclientid;
        this.username = username;
        this.userguid = userguid;
        this.studentname = studentname;
        this.userclassguid = userclassguid;
        this.userclassname = userclassname;
        this.answerhandwritedata = answerhandwritedata;
        this.answerhandwritepreview = answerhandwritepreview;
        this.answerchoice = answerchoice;
        this.answertext = answertext;
        this.answercamera = answercamera;
        this.answerdate = answerdate;
        this.state = state;
        this.favorite = favorite;
        this.answerindex = answerindex;
        this.syn_timestamp = syn_timestamp;
        this.syn_isdelete = syn_isdelete;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getScheduleguid() {
        return this.scheduleguid;
    }

    public void setScheduleguid(String scheduleguid) {
        this.scheduleguid = scheduleguid;
    }

    public String getAnswersheetresourceguid() {
        return this.answersheetresourceguid;
    }

    public void setAnswersheetresourceguid(String answersheetresourceguid) {
        this.answersheetresourceguid = answersheetresourceguid;
    }

    public String getQuestionguid() {
        return this.questionguid;
    }

    public void setQuestionguid(String questionguid) {
        this.questionguid = questionguid;
    }

    public String getClientid() {
        return this.clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getNotifyclientid() {
        return this.notifyclientid;
    }

    public void setNotifyclientid(String notifyclientid) {
        this.notifyclientid = notifyclientid;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserguid() {
        return this.userguid;
    }

    public void setUserguid(String userguid) {
        this.userguid = userguid;
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

    public String getAnswerhandwritedata() {
        return this.answerhandwritedata;
    }

    public void setAnswerhandwritedata(String answerhandwritedata) {
        this.answerhandwritedata = answerhandwritedata;
    }

    public String getAnswerhandwritepreview() {
        return this.answerhandwritepreview;
    }

    public void setAnswerhandwritepreview(String answerhandwritepreview) {
        this.answerhandwritepreview = answerhandwritepreview;
    }

    public String getAnswerchoice() {
        return this.answerchoice;
    }

    public void setAnswerchoice(String answerchoice) {
        this.answerchoice = answerchoice;
    }

    public String getAnswertext() {
        return this.answertext;
    }

    public void setAnswertext(String answertext) {
        this.answertext = answertext;
    }

    public String getAnswercamera() {
        return this.answercamera;
    }

    public void setAnswercamera(String answercamera) {
        this.answercamera = answercamera;
    }

    public Date getAnswerdate() {
        return this.answerdate;
    }

    public void setAnswerdate(Date answerdate) {
        this.answerdate = answerdate;
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

    public Integer getAnswerindex() {
        return this.answerindex;
    }

    public void setAnswerindex(Integer answerindex) {
        this.answerindex = answerindex;
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
