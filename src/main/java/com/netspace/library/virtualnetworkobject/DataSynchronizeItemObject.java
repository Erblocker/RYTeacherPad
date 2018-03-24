package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;

public class DataSynchronizeItemObject extends ItemObject {
    protected final String ENGINENAME = "DataSynchronizeEngine";
    private boolean mNoSave = false;

    public DataSynchronizeItemObject(String szPackageID, Activity Activity, OnSuccessListener SuccessListener) {
        super(szPackageID, Activity, SuccessListener);
    }

    public DataSynchronizeItemObject(String szPackageID, Activity Activity) {
        super(szPackageID, Activity);
    }

    public DataSynchronizeItemObject(String szPackageID) {
        super(szPackageID);
    }

    public String getRequiredEngineName() {
        return "DataSynchronizeEngine";
    }

    public void setPackageID(String szPackageID) {
        this.mObjectURI = szPackageID;
    }

    public String getPackageID() {
        return this.mObjectURI;
    }

    public void setClientID(String szClientID) {
        setParam("ClientID", szClientID);
    }

    public String getClientID() {
        return (String) getParam("ClientID");
    }

    public boolean isNoSave() {
        return this.mNoSave;
    }

    public void setNoSave(boolean bNoSave) {
        this.mNoSave = bNoSave;
        if (bNoSave) {
            this.mAllowCache = false;
        }
    }
}
