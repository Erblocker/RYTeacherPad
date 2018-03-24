package com.netspace.library.interfaces;

import java.util.Date;

public class RESTBasicData {
    private String guid;
    protected int mMode = 0;
    private Integer syn_isdelete = Integer.valueOf(0);
    private Date syn_timestamp;
    private String username;

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String szGUID) {
        this.guid = szGUID;
    }

    public void setMode(int nMode) {
        this.mMode = nMode;
    }

    public int getMode() {
        return this.mMode;
    }

    public void setSyn_timestamp(Date dtTime) {
        this.syn_timestamp = dtTime;
    }

    public Date getSyn_timestamp() {
        return this.syn_timestamp;
    }

    public void setSyn_isdelete(Integer nValue) {
        this.syn_isdelete = nValue;
    }

    public Integer getSyn_isdelete() {
        return this.syn_isdelete;
    }

    public void setUsername(String szUserName) {
        this.username = szUserName;
    }

    public String getUsername() {
        return this.username;
    }
}
