package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import java.util.ArrayList;

public class WebServiceCallItemObject extends ItemObject {
    private final String ENGINENAME;
    private ArrayList<Integer> mAllowReturnCodes;
    protected String mReplaceUserGUID;
    private boolean mbUseSuperAdmin;

    public WebServiceCallItemObject() {
        this(null, null, null);
    }

    public WebServiceCallItemObject(String szMethodName) {
        this(szMethodName, null, null);
    }

    public WebServiceCallItemObject(String szMethodName, Activity Activity) {
        this(szMethodName, Activity, null);
    }

    public WebServiceCallItemObject(String szMethodName, Activity Activity, OnSuccessListener SuccessListener) {
        super(szMethodName, Activity, SuccessListener);
        this.ENGINENAME = "WebServiceCallEngine";
        this.mAllowReturnCodes = new ArrayList();
        this.mbUseSuperAdmin = false;
        this.mAllowCache = false;
    }

    public void setUserGUID(String szUserGUID) {
        this.mReplaceUserGUID = szUserGUID;
    }

    public String getRequiredEngineName() {
        return "WebServiceCallEngine";
    }

    public void setMethodName(String szMethodName) {
        this.mObjectURI = szMethodName;
    }

    public String getMethodName() {
        return this.mObjectURI;
    }

    public void setAllowReturnCodes(int nCode) {
        this.mAllowReturnCodes.add(Integer.valueOf(nCode));
    }

    public ArrayList<Integer> getAllowReturnCodes() {
        return this.mAllowReturnCodes;
    }

    public boolean getUseSuperAdmin() {
        return this.mbUseSuperAdmin;
    }

    public void setUseSuperAdmin(boolean bUse) {
        this.mbUseSuperAdmin = bUse;
    }
}
