package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;

public class PrivateDataItemObject extends ItemObject {
    private final String ENGINENAME;

    public PrivateDataItemObject() {
        this(null, null, null);
    }

    public PrivateDataItemObject(String szKey) {
        this(szKey, null, null);
    }

    public PrivateDataItemObject(String szKey, Activity Activity) {
        this(szKey, Activity, null);
    }

    public PrivateDataItemObject(String szKey, Activity Activity, OnSuccessListener SuccessListener) {
        super(szKey, Activity, SuccessListener);
        this.ENGINENAME = "PrivateDataEngine";
        this.mAllowCache = false;
    }

    public String getRequiredEngineName() {
        return "PrivateDataEngine";
    }

    public void setKey(String szKey) {
        this.mObjectURI = szKey;
    }

    public String getKey() {
        return this.mObjectURI;
    }
}
