package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import org.achartengine.chart.TimeChart;

public class ResourceItemObject extends ItemObject {
    private final String ENGINENAME;

    public ResourceItemObject() {
        this(null, null, null);
    }

    public ResourceItemObject(String szObjectGUID) {
        this(szObjectGUID, null, null);
    }

    public ResourceItemObject(String szObjectGUID, Activity Activity) {
        this(szObjectGUID, Activity, null);
    }

    public ResourceItemObject(String szObjectGUID, Activity Activity, OnSuccessListener SuccessListener) {
        super(szObjectGUID, Activity, SuccessListener);
        this.ENGINENAME = "ResourceEngine";
        this.mAllowCache = false;
        this.mExpireTimeInMS = TimeChart.DAY;
    }

    public String getRequiredEngineName() {
        return "ResourceEngine";
    }

    public void setResourceGUID(String szGUID) {
        this.mObjectURI = szGUID;
    }

    public String getResourceGUID() {
        return this.mObjectURI;
    }
}
