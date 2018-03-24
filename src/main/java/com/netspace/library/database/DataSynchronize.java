package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class DataSynchronize extends CRestDataBase {
    private String clientid;
    private Integer contenttype;
    private String guid;
    private String packagecontent;
    private Date packagedate;
    private Integer packagedelete;
    private String packageid;

    public DataSynchronize(String guid) {
        this.guid = guid;
    }

    public DataSynchronize(String guid, String clientid, String packageid, Date packagedate, String packagecontent, Integer contenttype, Integer packagedelete) {
        this.guid = guid;
        this.clientid = clientid;
        this.packageid = packageid;
        this.packagedate = packagedate;
        this.packagecontent = packagecontent;
        this.contenttype = contenttype;
        this.packagedelete = packagedelete;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getClientid() {
        return this.clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getPackageid() {
        return this.packageid;
    }

    public void setPackageid(String packageid) {
        this.packageid = packageid;
    }

    public Date getPackagedate() {
        return this.packagedate;
    }

    public void setPackagedate(Date packagedate) {
        this.packagedate = packagedate;
    }

    public String getPackagecontent() {
        return this.packagecontent;
    }

    public void setPackagecontent(String packagecontent) {
        this.packagecontent = packagecontent;
    }

    public Integer getContenttype() {
        return this.contenttype;
    }

    public void setContenttype(Integer contenttype) {
        this.contenttype = contenttype;
    }

    public Integer getPackagedelete() {
        return this.packagedelete;
    }

    public void setPackagedelete(Integer packagedelete) {
        this.packagedelete = packagedelete;
    }
}
