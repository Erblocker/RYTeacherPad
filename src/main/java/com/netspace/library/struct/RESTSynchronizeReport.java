package com.netspace.library.struct;

import java.util.ArrayList;

public class RESTSynchronizeReport {
    public ArrayList<String> arrDownloadGUIDs = new ArrayList();
    public ArrayList<String> arrUploadGUIDs = new ArrayList();
    public int nDownloadCount = 0;
    public int nModifyCount = 0;
    public int nUploadCount = 0;
    public String szFieldName;

    public RESTSynchronizeReport(String szFieldName, int nDownloadCount, int nUploadCount, int nModifyCount) {
        this.szFieldName = szFieldName;
        this.nDownloadCount = nDownloadCount;
        this.nModifyCount = nModifyCount;
        this.nUploadCount = nUploadCount;
    }
}
