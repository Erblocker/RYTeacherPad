package com.netspace.library.database;

import com.netspace.library.struct.CRestDataBase;
import java.util.Date;

public class Resources extends CRestDataBase {
    private String acl_groupguid;
    private Integer acl_groupread;
    private Integer acl_groupwrite;
    private Integer acl_otherread;
    private Integer acl_otherwrite;
    private String acl_ownerguid;
    private String acl_ownername;
    private Integer acl_ownerread;
    private Integer acl_ownerwrite;
    private String author;
    private byte[] content;
    private String filename;
    private String guid;
    private Integer haspreview;
    private Integer hasthumbnail;
    private String mainfileextname;
    private String mainfileuri;
    private String previewfileuri;
    private Date resourcedate;
    private String resourcetype;
    private String searchtext;
    private String source;
    private Integer status;
    private String summery;
    private String title;
    private Integer type;

    public Resources(String guid) {
        this.guid = guid;
    }

    public Resources(String guid, String title, Date resourcedate, String resourcetype, Integer type, String source, String author, String summery, String searchtext, String filename, String mainfileextname, String mainfileuri, String previewfileuri, Integer hasthumbnail, Integer haspreview, Integer status, String acl_ownerguid, String acl_ownername, String acl_groupguid, Integer acl_ownerread, Integer acl_ownerwrite, Integer acl_groupread, Integer acl_groupwrite, Integer acl_otherread, Integer acl_otherwrite, byte[] content) {
        this.guid = guid;
        this.title = title;
        this.resourcedate = resourcedate;
        this.resourcetype = resourcetype;
        this.type = type;
        this.source = source;
        this.author = author;
        this.summery = summery;
        this.searchtext = searchtext;
        this.filename = filename;
        this.mainfileextname = mainfileextname;
        this.mainfileuri = mainfileuri;
        this.previewfileuri = previewfileuri;
        this.hasthumbnail = hasthumbnail;
        this.haspreview = haspreview;
        this.status = status;
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

    public Date getResourcedate() {
        return this.resourcedate;
    }

    public void setResourcedate(Date resourcedate) {
        this.resourcedate = resourcedate;
    }

    public String getResourcetype() {
        return this.resourcetype;
    }

    public void setResourcetype(String resourcetype) {
        this.resourcetype = resourcetype;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public String getSummery() {
        return this.summery;
    }

    public void setSummery(String summery) {
        this.summery = summery;
    }

    public String getSearchtext() {
        return this.searchtext;
    }

    public void setSearchtext(String searchtext) {
        this.searchtext = searchtext;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMainfileextname() {
        return this.mainfileextname;
    }

    public void setMainfileextname(String mainfileextname) {
        this.mainfileextname = mainfileextname;
    }

    public String getMainfileuri() {
        return this.mainfileuri;
    }

    public void setMainfileuri(String mainfileuri) {
        this.mainfileuri = mainfileuri;
    }

    public String getPreviewfileuri() {
        return this.previewfileuri;
    }

    public void setPreviewfileuri(String previewfileuri) {
        this.previewfileuri = previewfileuri;
    }

    public Integer getHasthumbnail() {
        return this.hasthumbnail;
    }

    public void setHasthumbnail(Integer hasthumbnail) {
        this.hasthumbnail = hasthumbnail;
    }

    public Integer getHaspreview() {
        return this.haspreview;
    }

    public void setHaspreview(Integer haspreview) {
        this.haspreview = haspreview;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
