package com.netspace.library.restful.provider.camera;

import android.content.Intent;
import android.util.Log;
import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import java.util.HashMap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CameraRESTServiceProvider extends RESTServiceProvider {
    public static final String TARGETFILENAME = "targetfilename";
    public static final String URI = "pad/camera";

    public static class CameraRESTCaptureEvent extends RESTEvent {
        public String szTargetFileName;
    }

    public static class CameraRESTCaptureResultEvent extends RESTEvent {
        public String szTargetFileName;
    }

    public RESTServiceProvider initialize(RESTService instance) {
        super.initialize(instance);
        this.mszName = "CameraService";
        this.mszURI = URI;
        EventBus.getDefault().register(this);
        return this;
    }

    public boolean execute(RESTRequest request, String szURI, HashMap<String, String> arrParams, RESTRequestCallBack callBack) {
        super.execute(request, szURI, arrParams, callBack);
        CameraRESTCaptureEvent captureRequest = new CameraRESTCaptureEvent();
        captureRequest.szTargetFileName = (String) arrParams.get("targetfilename");
        captureRequest.szRequestGUID = request.getRequestGUID();
        this.mInstance.launchIntent(new Intent(this.mContext, CameraRESTServiceActivity.class), captureRequest);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCameraRESTCaptureResultEvent(CameraRESTCaptureResultEvent event) {
        RESTRequest request = getRequest(event.szRequestGUID);
        if (request == null) {
            Log.e(getClass().getCanonicalName(), "RESTRequest is not found with guid " + event.szRequestGUID + ", futher request can not process.");
        } else if (event.nResult == -1) {
            request.param("targetfilename", event.szTargetFileName);
            request.getCallBack().onRestSuccess(this, request, event);
        } else {
            request.getCallBack().onRestFailure(this, request, event);
        }
    }
}
