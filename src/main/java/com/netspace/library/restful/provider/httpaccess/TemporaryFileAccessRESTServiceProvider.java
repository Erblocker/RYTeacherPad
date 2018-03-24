package com.netspace.library.restful.provider.httpaccess;

import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import java.util.HashMap;

public class TemporaryFileAccessRESTServiceProvider extends RESTServiceProvider {
    public static final String TARGETFILENAME = "targetfilename";
    public static final String URI = "/temporaryfile";

    public TemporaryFileAccessRESTServiceProvider initialize(RESTService instance) {
        super.initialize(instance);
        this.mszName = getClass().getSimpleName();
        this.mszURI = URI;
        return this;
    }

    public boolean execute(RESTRequest request, String szURI, HashMap<String, String> arrParams, RESTRequestCallBack callBack) {
        super.execute(request, szURI, arrParams, callBack);
        String szFileName = optParam((HashMap) arrParams, "targetfilename", "");
        if (szFileName.isEmpty()) {
            return false;
        }
        RESTServiceProvider httpFileAccessProvider = this.mInstance.findProvider(HttpFileAccessRESTServiceProvider.class);
        if (httpFileAccessProvider == null) {
            return false;
        }
        arrParams.remove("targetfilename");
        return httpFileAccessProvider.execute(request, new StringBuilder(String.valueOf(MyiBaseApplication.getCommonVariables().ServerInfo.getServerURL())).append("/GetTemporaryStorage?filename=").append(szFileName).toString(), arrParams, callBack);
    }
}
