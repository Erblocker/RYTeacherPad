package com.netspace.library.restful.provider.screencapture;

import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import java.util.HashMap;

public class DeviceRootScreenCaptureServiceProvider extends RESTServiceProvider {
    public static final String TARGETFILENAME = "targetfilename";

    public RESTServiceProvider initialize(RESTService instance) {
        super.initialize(instance);
        this.mszName = "DeviceRootScreenCaptureService";
        this.mszURI = ScreenCaptureRESTServiceProvider.URI;
        this.mPriority = 5;
        return this;
    }

    public boolean execute(RESTRequest request, String szURI, HashMap<String, String> arrParams, RESTRequestCallBack callBack) {
        super.execute(request, szURI, arrParams, callBack);
        String szPictureFileName = (String) arrParams.get("targetfilename");
        if (szPictureFileName == null || szPictureFileName == "") {
            szPictureFileName = Utilities.getTempFileName("jpg");
            request.param("targetfilename", szPictureFileName);
        }
        if (Utilities.captureScreen(szPictureFileName)) {
            callBack.onRestSuccess(this, request, RESTEvent.getOKEvent());
        } else {
            callBack.onRestFailure(this, request, RESTEvent.getCancelEvent());
        }
        return true;
    }
}
