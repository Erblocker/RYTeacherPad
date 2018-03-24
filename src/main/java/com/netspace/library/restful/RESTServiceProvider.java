package com.netspace.library.restful;

import android.content.Context;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.utilities.Utilities;
import java.util.HashMap;

public class RESTServiceProvider {
    protected Context mContext;
    protected RESTService mInstance;
    protected int mPriority = 0;
    protected String mszName;
    protected String mszURI;

    public RESTServiceProvider initialize(RESTService instance) {
        this.mContext = MyiBaseApplication.getBaseAppContext();
        this.mInstance = instance;
        return this;
    }

    public boolean execute(RESTRequest request, String szURI, HashMap<String, String> hashMap, RESTRequestCallBack callBack) {
        request.setCallBack(callBack);
        return false;
    }

    public int optParam(HashMap<String, String> arrParams, String szParamName, int nDefaultValue) {
        int nRetVal = nDefaultValue;
        if (arrParams.containsKey(szParamName)) {
            return Utilities.toInt((String) arrParams.get(szParamName));
        }
        return nRetVal;
    }

    public String optParam(HashMap<String, String> arrParams, String szParamName, String szDefaultValue) {
        String szRetVal = szDefaultValue;
        if (!arrParams.containsKey(szParamName)) {
            return szRetVal;
        }
        String szValue = (String) arrParams.get(szParamName);
        if (szValue == null || szValue.isEmpty()) {
            return szRetVal;
        }
        return szValue;
    }

    public RESTRequest getRequest(String szRequestGUID) {
        return RESTRequestManager.getRequest(szRequestGUID);
    }
}
