package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import java.util.Vector;

public class ResourceEngine extends Engine {
    private static final String TAG = "ResourceEngine";
    protected String mEngineName = TAG;

    public ResourceEngine(Context Context) {
        super(Context);
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public boolean handlePackageRead(final ItemObject OneObject) {
        Object szGUID = OneObject.getObjectURI();
        setItemObjectActivityBusy(OneObject, true);
        LoadExamDataThread3 LoadThread = new LoadExamDataThread3(this.mContext, "GetResourceByGUID", new OnSoapCompleteListener() {
            public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                ResourceEngine.this.setItemObjectActivityBusy(OneObject, false);
                String szData = ThreadObject.getStringParam(arrSoapData, 0);
                if (szData == null) {
                    szData = "";
                }
                OneObject.writeTextData(szData);
                OneObject.callCallbacks(true, nReturnCode);
            }
        });
        LoadThread.setOnSoapFailListener(new OnSoapFailListener() {
            public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                ResourceEngine.this.setItemObjectActivityBusy(OneObject, false);
                OneObject.mReturnMessage = ThreadObject.getErrorDescription();
                OneObject.callCallbacks(false, nReturnCode);
                return true;
            }
        });
        LoadThread.addParam("lpszResourceGUID", szGUID);
        LoadThread.setAllowReturnCode(ErrorCode.ERROR_NO_DATA);
        LoadThread.start();
        return true;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        return false;
    }
}
