package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class LessonsSchedule extends CRestDataBase {
    private Integer grade;
    private String guid;
    private Integer lessonindex;
    private String resourceguid;
    private Date scheduledate;
    private Date scheduleenddate;
    private Integer state;
    private Integer subject;
    private Integer syn_isdelete;
    private Date syn_timestamp;
    private String userclassguid;
    private String userclassname;
    private String userschoolguid;

    public LessonsSchedule(String guid) {
        this.guid = guid;
    }

    public LessonsSchedule(String guid, Integer grade, Integer subject, Integer lessonindex, Date scheduledate, String resourceguid, String userschoolguid, String userclassguid, String userclassname, Integer state, Date scheduleenddate, Date syn_timestamp, Integer syn_isdelete) {
        this.guid = guid;
        this.grade = grade;
        this.subject = subject;
        this.lessonindex = lessonindex;
        this.scheduledate = scheduledate;
        this.resourceguid = resourceguid;
        this.userschoolguid = userschoolguid;
        this.userclassguid = userclassguid;
        this.userclassname = userclassname;
        this.state = state;
        this.scheduleenddate = scheduleenddate;
        this.syn_timestamp = syn_timestamp;
        this.syn_isdelete = syn_isdelete;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Integer getGrade() {
        return this.grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getSubject() {
        return this.subject;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public Integer getLessonindex() {
        return this.lessonindex;
    }

    public void setLessonindex(Integer lessonindex) {
        this.lessonindex = lessonindex;
    }

    public Date getScheduledate() {
        return this.scheduledate;
    }

    public void setScheduledate(Date scheduledate) {
        this.scheduledate = scheduledate;
    }

    public String getResourceguid() {
        return this.resourceguid;
    }

    public void setResourceguid(String resourceguid) {
        this.resourceguid = resourceguid;
    }

    public String getUserschoolguid() {
        return this.userschoolguid;
    }

    public void setUserschoolguid(String userschoolguid) {
        this.userschoolguid = userschoolguid;
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

    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getScheduleenddate() {
        return this.scheduleenddate;
    }

    public void setScheduleenddate(Date scheduleenddate) {
        this.scheduleenddate = scheduleenddate;
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
