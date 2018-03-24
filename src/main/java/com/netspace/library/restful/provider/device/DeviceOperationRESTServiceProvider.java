package com.netspace.library.restful.provider.device;

import com.google.gson.annotations.Expose;
import com.netspace.library.im.IMService;
import com.netspace.library.im.IMService.OnIMServiceArrivedListener;
import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTRequestManager;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import java.util.HashMap;

public class DeviceOperationRESTServiceProvider extends RESTServiceProvider implements OnIMServiceArrivedListener {
    public static final String CLIENTID = "clientid";
    public static final String REQUESTGUID = "requestguid";
    public static final String REQUESTVERB = "RESTDeviceRequest/v1";
    public static final String TARGETFILENAME = "targetfilename";
    public static final String TARGETPATH_DISPLAY = "/display/";
    public static final int TIMEOUT = 5000;
    public static final String URI = "/device";

    private class TimeoutRunnable implements Runnable {
        private String mszRequestGUID;

        public TimeoutRunnable(String szRequestGUID) {
            this.mszRequestGUID = szRequestGUID;
        }

        public void run() {
            RESTRequest request = RESTRequestManager.getRequest(this.mszRequestGUID);
            DeviceOperationResultEvent event = new DeviceOperationResultEvent();
            event.nResult = RESTEvent.RESULT_TIMEOUT;
            if (request != null && !request.isFinish()) {
                request.getCallBack().onRestFailure(DeviceOperationRESTServiceProvider.this, request, event);
            }
        }
    }

    public static class DeviceOperationResultEvent extends RESTEvent {
        @Expose
        public String szFrom;
        @Expose
        public String szTimeStamp;
    }

    public DeviceOperationRESTServiceProvider initialize(RESTService instance) {
        super.initialize(instance);
        this.mszName = getClass().getSimpleName();
        this.mszURI = URI;
        IMService.getIMService().registerCallBack(this);
        return this;
    }

    public boolean execute(RESTRequest request, String szURI, HashMap<String, String> arrParams, RESTRequestCallBack callBack) {
        super.execute(request, szURI, arrParams, callBack);
        String szIMMessage = "";
        IMService.getIMService().sendMessage(Utilities.getNow() + " " + IMService.getIMUserName() + ": " + new StringBuilder(String.valueOf("RESTDeviceRequest/v1 " + request.getRequestGUID() + " ")).append("GET ").append(szURI).toString(), request.getParam(CLIENTID));
        return true;
    }

    public void OnMessageArrived(String szMessage) {
        if (szMessage.length() >= 25 && szMessage.indexOf("RESTDeviceRequestResult") != -1) {
            String szMessageTime = szMessage.substring(0, 19);
            szMessage = szMessage.substring(20);
            int nFindPos = szMessage.indexOf(": ");
            String szFrom = "";
            if (nFindPos != -1) {
                szFrom = szMessage.substring(0, nFindPos);
                String[] arrData = szMessage.substring(nFindPos + 2).split(" ");
                String szVerb = "";
                if (arrData.length > 0) {
                    szVerb = arrData[0];
                }
                if (szVerb.equalsIgnoreCase("RESTDeviceRequestResult") && arrData.length > 2) {
                    RESTRequest request = RESTRequestManager.getRequest(arrData[1]);
                    DeviceOperationResultEvent event = new DeviceOperationResultEvent();
                    event.szResult = "";
                    event.szFrom = szFrom;
                    event.szTimeStamp = szMessageTime;
                    for (int i = 2; i < arrData.length; i++) {
                        if (!event.szResult.isEmpty()) {
                            event.szResult += " ";
                        }
                        event.szResult += arrData[i];
                    }
                    event.nResult = RESTEvent.RESULT_OK;
                    if (request != null) {
                        request.param("targetfilename", event.szResult);
                        request.getCallBack().onRestSuccess(this, request, event);
                    }
                }
            }
        }
    }
}
