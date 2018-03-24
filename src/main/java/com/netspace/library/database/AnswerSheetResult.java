package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class AnswerSheetResult extends CRestDataBase {
    private String answercorrecthandwrite;
    private String answercorrecthandwritepreview;
    private Integer answerindex;
    private Integer answerresult;
    private Float answerscore;
    private String answersheetresourceguid;
    private String clientid;
    private String guid;
    private String questionguid;
    private String scheduleguid;
    private Date scoredate;
    private Integer state;
    private String studentname;
    private Integer syn_isdelete;
    private Date syn_timestamp;
    private String userclassguid;
    private String userclassname;
    private String userguid;
    private String username;

    public AnswerSheetResult(String guid) {
        this.guid = guid;
    }

    public AnswerSheetResult(String guid, String scheduleguid, String answersheetresourceguid, String questionguid, String clientid, String username, String userguid, String studentname, String userclassguid, String userclassname, Integer answerresult, Float answerscore, String answercorrecthandwrite, String answercorrecthandwritepreview, Date scoredate, Date syn_timestamp, Integer syn_isdelete, Integer state, Integer answerindex) {
        this.guid = guid;
        this.scheduleguid = scheduleguid;
        this.answersheetresourceguid = answersheetresourceguid;
        this.questionguid = questionguid;
        this.clientid = clientid;
        this.username = username;
        this.userguid = userguid;
        this.studentname = studentname;
        this.userclassguid = userclassguid;
        this.userclassname = userclassname;
        this.answerresult = answerresult;
        this.answerscore = answerscore;
        this.answercorrecthandwrite = answercorrecthandwrite;
        this.answercorrecthandwritepreview = answercorrecthandwritepreview;
        this.scoredate = scoredate;
        this.syn_timestamp = syn_timestamp;
        this.syn_isdelete = syn_isdelete;
        this.state = state;
        this.answerindex = answerindex;
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

    public Integer getAnswerresult() {
        return this.answerresult;
    }

    public void setAnswerresult(Integer answerresult) {
        this.answerresult = answerresult;
    }

    public Float getAnswerscore() {
        return this.answerscore;
    }

    public void setAnswerscore(Float answerscore) {
        this.answerscore = answerscore;
    }

    public String getAnswercorrecthandwrite() {
        return this.answercorrecthandwrite;
    }

    public void setAnswercorrecthandwrite(String answercorrecthandwrite) {
        this.answercorrecthandwrite = answercorrecthandwrite;
    }

    public String getAnswercorrecthandwritepreview() {
        return this.answercorrecthandwritepreview;
    }

    public void setAnswercorrecthandwritepreview(String answercorrecthandwritepreview) {
        this.answercorrecthandwritepreview = answercorrecthandwritepreview;
    }

    public Date getScoredate() {
        return this.scoredate;
    }

    public void setScoredate(Date scoredate) {
        this.scoredate = scoredate;
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

    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getAnswerindex() {
        return this.answerindex;
    }

    public void setAnswerindex(Integer answerindex) {
        this.answerindex = answerindex;
    }
}
