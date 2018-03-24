package com.netspace.library.virtualnetworkobject;

import android.content.Context;

public class ResourceOfflineEngine extends Engine {
    private static final String TAG = "ResourceOfflineEngine";
    protected String mEngineName = TAG;

    public ResourceOfflineEngine(Context Context) {
        super(Context);
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public boolean handlePackageRead(ItemObject OneObject) {
        String szGUID = OneObject.getObjectURI();
        setItemObjectActivityBusy(OneObject, true);
        String szLocalFileContent = VirtualNetworkObject.getOfflineObjectContent(szGUID);
        if (szLocalFileContent != null) {
            setItemObjectActivityBusy(OneObject, false);
            String szData = szLocalFileContent;
            if (szData == null) {
                szData = "";
            }
            OneObject.writeTextData(szData);
            OneObject.callCallbacks(true, 0);
        } else {
            setItemObjectActivityBusy(OneObject, false);
            OneObject.mReturnMessage = "所需资源没有缓存，无法访问。";
            OneObject.callCallbacks(false, -2);
        }
        return true;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        return false;
    }
}
