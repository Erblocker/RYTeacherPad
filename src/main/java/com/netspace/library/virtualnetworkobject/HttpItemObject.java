package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.servers.NanoHTTPD;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;

public class HttpItemObject extends ItemObject {
    private final String ENGINENAME;
    private String mContentType;
    private int mLimitSize;
    private boolean mSaveToFile;
    private String mSourceFileName;
    private String mTargetFileName;
    private boolean mbNeedAuth;
    private boolean mbRandomWait;
    private int mnBufferSize;
    private int mnTimeout;
    private String mszEncoding;

    public HttpItemObject() {
        this(null, null, null);
    }

    public HttpItemObject(String szURL) {
        this(szURL, null, null);
    }

    public HttpItemObject(String szURL, Activity Activity) {
        this(szURL, Activity, null);
    }

    public HttpItemObject(String szURL, Activity Activity, OnSuccessListener SuccessListener) {
        super(szURL, Activity, SuccessListener);
        this.ENGINENAME = "HttpCallEngine";
        this.mContentType = NanoHTTPD.MIME_HTML;
        this.mTargetFileName = "";
        this.mSourceFileName = "";
        this.mLimitSize = -1;
        this.mSaveToFile = false;
        this.mnTimeout = 30000;
        this.mnBufferSize = 524288;
        this.mbRandomWait = false;
        this.mbNeedAuth = false;
        this.mszEncoding = "GBK";
        this.mAllowCache = false;
    }

    public void setTimeout(int timeoutMillis) {
        this.mnTimeout = timeoutMillis;
    }

    public int getTimeout() {
        return this.mnTimeout;
    }

    public void setEncoding(String szEncoding) {
        this.mszEncoding = szEncoding;
    }

    public String getEncoding() {
        return this.mszEncoding;
    }

    public void setNeedAuthenticate(boolean bEnable) {
        this.mbNeedAuth = bEnable;
    }

    public boolean getNeedAuthenticate() {
        return this.mbNeedAuth;
    }

    public void setEnableRandomWait(boolean bEnable) {
        this.mbRandomWait = bEnable;
    }

    public boolean getEnableRandomWait() {
        return this.mbRandomWait;
    }

    public void setBufferSize(int nBufferSize) {
        if (nBufferSize > 0) {
            this.mnBufferSize = nBufferSize;
        }
    }

    public int getBufferSize() {
        return this.mnBufferSize;
    }

    public String getRequiredEngineName() {
        return "HttpCallEngine";
    }

    public void setURL(String szURL) {
        this.mObjectURI = szURL;
    }

    public void setSourceFileName(String szFileName) {
        this.mSourceFileName = szFileName;
    }

    public String getSourceFileName() {
        return this.mSourceFileName;
    }

    public void setSizeLimit(int nLimit) {
        this.mLimitSize = nLimit;
    }

    public int getSizeLimit() {
        return this.mLimitSize;
    }

    public String getMethodName() {
        return this.mObjectURI;
    }

    public void setContentType(String szContentType) {
        this.mContentType = szContentType;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public String getTargetFileName() {
        return this.mTargetFileName;
    }

    public void setTargetFileName(String szFileName) {
        this.mTargetFileName = szFileName;
    }

    public boolean getSaveToFile() {
        return this.mSaveToFile;
    }

    public void setSaveToFile(boolean bEnable) {
        this.mSaveToFile = bEnable;
    }
}
