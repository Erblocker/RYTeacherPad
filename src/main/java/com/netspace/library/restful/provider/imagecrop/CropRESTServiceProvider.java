package com.netspace.library.restful.provider.imagecrop;

import android.content.Intent;
import android.util.Log;
import com.google.gson.annotations.Expose;
import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import java.util.HashMap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CropRESTServiceProvider extends RESTServiceProvider {
    public static final String TARGETFILENAME = "targetfilename";
    public static final String URI = "pad/image/crop";

    public static class CropRESTCaptureEvent extends RESTEvent {
        @Expose
        public String szTargetFileName;
    }

    public static class CropRESTCaptureResultEvent extends RESTEvent {
        @Expose
        public String szTargetFileName;
    }

    public CropRESTServiceProvider initialize(RESTService instance) {
        super.initialize(instance);
        this.mszName = "CropService";
        this.mszURI = URI;
        EventBus.getDefault().register(this);
        return this;
    }

    public boolean execute(RESTRequest request, String szURI, HashMap<String, String> arrParams, RESTRequestCallBack callBack) {
        super.execute(request, szURI, arrParams, callBack);
        CropRESTCaptureEvent cropRequest = new CropRESTCaptureEvent();
        cropRequest.szTargetFileName = (String) arrParams.get("targetfilename");
        cropRequest.szRequestGUID = request.getRequestGUID();
        this.mInstance.launchIntent(new Intent(this.mContext, CropRESTServiceActivity.class), cropRequest);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCropRESTCaptureResultEvent(CropRESTCaptureResultEvent event) {
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
