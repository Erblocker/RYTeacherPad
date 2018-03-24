package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import java.util.Vector;

public class PrivateDataEngine extends Engine {
    private static final String TAG = "PrivateDataEngine";
    protected String mEngineName = TAG;

    public PrivateDataEngine(Context Context) {
        super(Context);
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public boolean handlePackageRead(final ItemObject OneObject) {
        Object szKey = OneObject.getObjectURI();
        setItemObjectActivityBusy(OneObject, true);
        if (VirtualNetworkObject.getOfflineMode() && VirtualNetworkObject.getCache().hasCache(OneObject) && VirtualNetworkObject.getCache().readFromCache(OneObject)) {
            OneObject.callCallbacks(true, 0);
        } else {
            LoadExamDataThread3 LoadThread = new LoadExamDataThread3(this.mContext, "GetPrivateData2", new OnSoapCompleteListener() {
                public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                    PrivateDataEngine.this.setItemObjectActivityBusy(OneObject, false);
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
                    PrivateDataEngine.this.setItemObjectActivityBusy(OneObject, false);
                    OneObject.mReturnMessage = ThreadObject.getErrorDescription();
                    OneObject.callCallbacks(false, nReturnCode);
                    return true;
                }
            });
            LoadThread.addParam("lpszKey", szKey);
            LoadThread.setAllowReturnCode(ErrorCode.ERROR_NO_DATA);
            LoadThread.start();
        }
        return true;
    }

    public boolean handlePackageWrite(final ItemObject OneObject) {
        Object szKey = OneObject.getObjectURI();
        setItemObjectActivityBusy(OneObject, true);
        LoadExamDataThread3 LoadThread = new LoadExamDataThread3(this.mContext, "PutPrivateData2", new OnSoapCompleteListener() {
            public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                PrivateDataEngine.this.setItemObjectActivityBusy(OneObject, false);
                OneObject.callCallbacks(true, nReturnCode);
            }
        });
        LoadThread.setOnSoapFailListener(new OnSoapFailListener() {
            public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                PrivateDataEngine.this.setItemObjectActivityBusy(OneObject, false);
                OneObject.mReturnMessage = ThreadObject.getErrorDescription();
                OneObject.callCallbacks(false, nReturnCode);
                return true;
            }
        });
        LoadThread.addParam("lpszKey", szKey);
        LoadThread.addParam("lpszData", OneObject.readTextData());
        LoadThread.addParam("nRights", Integer.valueOf(-1));
        LoadThread.setAllowReturnCode(ErrorCode.ERROR_NO_DATA);
        LoadThread.start();
        return true;
    }
}
