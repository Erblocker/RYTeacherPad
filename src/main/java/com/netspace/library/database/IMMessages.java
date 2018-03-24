package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class IMMessages extends CRestDataBase {
    private String content;
    private Date date;
    private Date expiredate;
    private String fromclientid;
    private String guid;
    private Date receivedate;
    private String receiveip;
    private Integer receivestate;
    private String toclientid;

    public IMMessages(String guid) {
        this.guid = guid;
    }

    public IMMessages(String guid, String fromclientid, String toclientid, Date date, Date receivedate, Date expiredate, Integer receivestate, String content, String receiveip) {
        this.guid = guid;
        this.fromclientid = fromclientid;
        this.toclientid = toclientid;
        this.date = date;
        this.receivedate = receivedate;
        this.expiredate = expiredate;
        this.receivestate = receivestate;
        this.content = content;
        this.receiveip = receiveip;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getFromclientid() {
        return this.fromclientid;
    }

    public void setFromclientid(String fromclientid) {
        this.fromclientid = fromclientid;
    }

    public String getToclientid() {
        return this.toclientid;
    }

    public void setToclientid(String toclientid) {
        this.toclientid = toclientid;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getReceivedate() {
        return this.receivedate;
    }

    public void setReceivedate(Date receivedate) {
        this.receivedate = receivedate;
    }

    public Date getExpiredate() {
        return this.expiredate;
    }

    public void setExpiredate(Date expiredate) {
        this.expiredate = expiredate;
    }

    public Integer getReceivestate() {
        return this.receivestate;
    }

    public void setReceivestate(Integer receivestate) {
        this.receivestate = receivestate;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReceiveip() {
        return this.receiveip;
    }

    public void setReceiveip(String receiveip) {
        this.receiveip = receiveip;
    }
}
