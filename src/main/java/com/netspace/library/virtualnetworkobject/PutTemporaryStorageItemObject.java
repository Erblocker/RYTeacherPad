package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;

public class PutTemporaryStorageItemObject extends HttpItemObject {
    private String mOldObjectURI = "";
    private boolean mbCanAppendParam = false;
    private boolean mbServerSaveToBase64 = false;
    private boolean mbUseBinaryMode = false;

    public PutTemporaryStorageItemObject() {
        init();
    }

    public PutTemporaryStorageItemObject(String szURL) {
        super(szURL);
        init();
    }

    public PutTemporaryStorageItemObject(String szURL, Activity Activity) {
        super(szURL, Activity);
        init();
    }

    public PutTemporaryStorageItemObject(String szURL, Activity Activity, OnSuccessListener SuccessListener) {
        super(szURL, Activity, SuccessListener);
        init();
    }

    private void init() {
        this.mOldObjectURI = this.mObjectURI;
        String szURL = this.mObjectURI;
        if (szURL.indexOf("://") == -1) {
            szURL = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/PutTemporaryStorage?filename=" + szURL;
            this.mbCanAppendParam = true;
        }
        this.mObjectURI = szURL;
        setContentType("application/octet-stream");
        setReadOperation(false);
    }

    private void prepareURL() {
        if (this.mbCanAppendParam) {
            String szURL = this.mOldObjectURI;
            if (szURL.indexOf("://") == -1) {
                szURL = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/PutTemporaryStorage?filename=" + szURL;
            }
            this.mObjectURI = szURL;
            if (this.mbUseBinaryMode) {
                this.mObjectURI += "&binary=1";
            }
            if (this.mbUseBinaryMode) {
                this.mObjectURI += "&savetobase64=1";
            }
        }
    }

    public void setSourceFileName(String szFileName) {
        super.setSourceFileName(szFileName);
    }
}
