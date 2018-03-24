package com.netspace.library.virtualnetworkobject;

import android.app.Activity;
import android.content.Context;
import com.netspace.library.ui.BaseActivity;

public class Engine {
    protected Context mContext;
    protected String mEngineName = "NullEngine";

    public Engine(Context Context) {
        this.mContext = Context;
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public boolean handlePackageRead(ItemObject OneObject) {
        return false;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        return false;
    }

    public void setItemObjectActivityBusy(ItemObject OneObject, boolean bBusy) {
        Activity OwnerActivity = OneObject.getActivity();
        if (OwnerActivity != null && (OwnerActivity instanceof BaseActivity)) {
            ((BaseActivity) OwnerActivity).setBusy(bBusy);
        }
    }

    public void startEngine() {
    }

    public void stopEngine() {
        this.mContext = null;
    }
}
