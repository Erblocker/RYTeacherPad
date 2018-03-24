package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class Questions extends CRestDataBase {
    private String acl_groupguid;
    private Integer acl_groupread;
    private Integer acl_groupwrite;
    private Integer acl_otherread;
    private Integer acl_otherwrite;
    private String acl_ownerguid;
    private String acl_ownername;
    private Integer acl_ownerread;
    private Integer acl_ownerwrite;
    private Integer answertime;
    private String author;
    private byte[] content;
    private Integer difficulty;
    private String filename;
    private Integer grade;
    private String guid;
    private Date questiondate;
    private Integer rank;
    private Integer score;
    private String searchtext;
    private String source;
    private Integer subject;
    private Integer subtype;
    private String tags;
    private Integer teachlevel;
    private String title;
    private Integer type;
    private Integer usetime;

    public Questions(String guid) {
        this.guid = guid;
    }

    public Questions(String guid, String title, Date questiondate, Integer type, Integer subtype, Integer grade, Integer subject, Integer score, Integer difficulty, Integer answertime, Integer teachlevel, Integer usetime, Integer rank, String searchtext, String source, String author, String filename, String tags, String acl_ownerguid, String acl_ownername, String acl_groupguid, Integer acl_ownerread, Integer acl_ownerwrite, Integer acl_groupread, Integer acl_groupwrite, Integer acl_otherread, Integer acl_otherwrite, byte[] content) {
        this.guid = guid;
        this.title = title;
        this.questiondate = questiondate;
        this.type = type;
        this.subtype = subtype;
        this.grade = grade;
        this.subject = subject;
        this.score = score;
        this.difficulty = difficulty;
        this.answertime = answertime;
        this.teachlevel = teachlevel;
        this.usetime = usetime;
        this.rank = rank;
        this.searchtext = searchtext;
        this.source = source;
        this.author = author;
        this.filename = filename;
        this.tags = tags;
        this.acl_ownerguid = acl_ownerguid;
        this.acl_ownername = acl_ownername;
        this.acl_groupguid = acl_groupguid;
        this.acl_ownerread = acl_ownerread;
        this.acl_ownerwrite = acl_ownerwrite;
        this.acl_groupread = acl_groupread;
        this.acl_groupwrite = acl_groupwrite;
        this.acl_otherread = acl_otherread;
        this.acl_otherwrite = acl_otherwrite;
        this.content = content;
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

    public Date getQuestiondate() {
        return this.questiondate;
    }

    public void setQuestiondate(Date questiondate) {
        this.questiondate = questiondate;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSubtype() {
        return this.subtype;
    }

    public void setSubtype(Integer subtype) {
        this.subtype = subtype;
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

    public Integer getScore() {
        return this.score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getAnswertime() {
        return this.answertime;
    }

    public void setAnswertime(Integer answertime) {
        this.answertime = answertime;
    }

    public Integer getTeachlevel() {
        return this.teachlevel;
    }

    public void setTeachlevel(Integer teachlevel) {
        this.teachlevel = teachlevel;
    }

    public Integer getUsetime() {
        return this.usetime;
    }

    public void setUsetime(Integer usetime) {
        this.usetime = usetime;
    }

    public Integer getRank() {
        return this.rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getSearchtext() {
        return this.searchtext;
    }

    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAcl_ownerguid() {
        return this.acl_ownerguid;
    }

    public void setAcl_ownerguid(String acl_ownerguid) {
        this.acl_ownerguid = acl_ownerguid;
    }

    public String getAcl_ownername() {
        return this.acl_ownername;
    }

    public void setAcl_ownername(String acl_ownername) {
        this.acl_ownername = acl_ownername;
    }

    public String getAcl_groupguid() {
        return this.acl_groupguid;
    }

    public void setAcl_groupguid(String acl_groupguid) {
        this.acl_groupguid = acl_groupguid;
    }

    public Integer getAcl_ownerread() {
        return this.acl_ownerread;
    }

    public void setAcl_ownerread(Integer acl_ownerread) {
        this.acl_ownerread = acl_ownerread;
    }

    public Integer getAcl_ownerwrite() {
        return this.acl_ownerwrite;
    }

    public void setAcl_ownerwrite(Integer acl_ownerwrite) {
        this.acl_ownerwrite = acl_ownerwrite;
    }

    public Integer getAcl_groupread() {
        return this.acl_groupread;
    }

    public void setAcl_groupread(Integer acl_groupread) {
        this.acl_groupread = acl_groupread;
    }

    public Integer getAcl_groupwrite() {
        return this.acl_groupwrite;
    }

    public void setAcl_groupwrite(Integer acl_groupwrite) {
        this.acl_groupwrite = acl_groupwrite;
    }

    public Integer getAcl_otherread() {
        return this.acl_otherread;
    }

    public void setAcl_otherread(Integer acl_otherread) {
        this.acl_otherread = acl_otherread;
    }

    public Integer getAcl_otherwrite() {
        return this.acl_otherwrite;
    }

    public void setAcl_otherwrite(Integer acl_otherwrite) {
        this.acl_otherwrite = acl_otherwrite;
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
