package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;

public class GetTemporaryStorageItemObject extends HttpItemObject {
    public GetTemporaryStorageItemObject(String szURL) {
        super(szURL);
        init();
    }

    public GetTemporaryStorageItemObject(String szURL, Activity Activity) {
        super(szURL, Activity);
        init();
    }

    public GetTemporaryStorageItemObject(String szURL, Activity Activity, OnSuccessListener SuccessListener) {
        super(szURL, Activity, SuccessListener);
        init();
    }

    private void init() {
        this.mObjectURI = MyiBaseApplication.getProtocol() + "://" + VirtualNetworkObject.getServerAddress() + "/GetTemporaryStorage?filename=" + this.mObjectURI;
        setReadOperation(true);
    }
}
