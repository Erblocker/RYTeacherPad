package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import java.util.Vector;

public class QuestionEngine extends Engine {
    private static final String TAG = "QuestionEngine";
    protected String mEngineName = TAG;

    public QuestionEngine(Context Context) {
        super(Context);
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public boolean handlePackageRead(final ItemObject OneObject) {
        Object szGUID = OneObject.getObjectURI();
        setItemObjectActivityBusy(OneObject, true);
        LoadExamDataThread3 LoadThread = new LoadExamDataThread3(this.mContext, "GetQuestionByGUID", new OnSoapCompleteListener() {
            public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                QuestionEngine.this.setItemObjectActivityBusy(OneObject, false);
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
                QuestionEngine.this.setItemObjectActivityBusy(OneObject, false);
                OneObject.mReturnMessage = ThreadObject.getErrorDescription();
                OneObject.callCallbacks(false, nReturnCode);
                return true;
            }
        });
        LoadThread.addParam("lpszQuestionGUID", szGUID);
        LoadThread.setAllowReturnCode(ErrorCode.ERROR_NO_DATA);
        LoadThread.start();
        return true;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        return false;
    }
}
