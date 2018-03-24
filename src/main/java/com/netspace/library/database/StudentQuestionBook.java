package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class StudentQuestionBook extends CRestDataBase {
    private String booknames;
    private String description;
    private Integer flags;
    private String guid;
    private String kpguid;
    private String kpname;
    private Date modifydate;
    private String objectguid;
    private String packageid;
    private String studentid;
    private String studentname;
    private Integer subject;
    private Integer syn_isdelete;
    private Date syn_timestamp;
    private String tags;
    private String title;
    private Integer type;
    private String userclassguid;
    private String userclassname;

    public StudentQuestionBook(String guid) {
        this.guid = guid;
    }

    public StudentQuestionBook(String guid, String title, Date modifydate, String studentid, String studentname, String userclassguid, String userclassname, String objectguid, String packageid, String kpguid, String kpname, String description, Integer subject, Integer type, String tags, String booknames, Integer flags, Date syn_timestamp, Integer syn_isdelete) {
        this.guid = guid;
        this.title = title;
        this.modifydate = modifydate;
        this.studentid = studentid;
        this.studentname = studentname;
        this.userclassguid = userclassguid;
        this.userclassname = userclassname;
        this.objectguid = objectguid;
        this.packageid = packageid;
        this.kpguid = kpguid;
        this.kpname = kpname;
        this.description = description;
        this.subject = subject;
        this.type = type;
        this.tags = tags;
        this.booknames = booknames;
        this.flags = flags;
        this.syn_timestamp = syn_timestamp;
        this.syn_isdelete = syn_isdelete;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getModifydate() {
        return this.modifydate;
    }

    public void setModifydate(Date modifydate) {
        this.modifydate = modifydate;
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

    public String getPackageid() {
        return this.packageid;
    }

    public void setPackageid(String packageid) {
        this.packageid = packageid;
    }

    public String getKpguid() {
        return this.kpguid;
    }

    public void setKpguid(String kpguid) {
        this.kpguid = kpguid;
    }

    public String getKpname() {
        return this.kpname;
    }

    public void setKpname(String kpname) {
        this.kpname = kpname;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSubject() {
        return this.subject;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getBooknames() {
        return this.booknames;
    }

    public void setBooknames(String booknames) {
        this.booknames = booknames;
    }

    public Integer getFlags() {
        return this.flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
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
