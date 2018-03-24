package com.netspace.library.restful;

import android.util.Log;
import com.netspace.library.utilities.Utilities;
import java.util.HashMap;

public class RESTRequest extends RESTHTTPRequest {
    protected RESTRequestBeforeStart mBeforeStartCallBack;
    protected RESTRequestCallBack mCallBack;
    protected RESTRequest mChainRequest;
    protected RESTRequest mParentRequest;
    protected RESTServiceProvider mProvider;
    protected RESTService mService;
    protected HashMap<String, String> marrParams = new HashMap();
    protected boolean mbCancel = false;
    protected boolean mbFinish = false;
    protected boolean mbSilence = false;
    protected String mszOperationName;
    protected String mszRequestGUID = Utilities.createGUID();
    protected String mszURI;

    public interface RESTRequestBeforeStart {
        void onBeforeStart(RESTServiceProvider rESTServiceProvider, RESTRequest rESTRequest);
    }

    public interface RESTRequestCallBack {
        void onRestFailure(RESTServiceProvider rESTServiceProvider, RESTRequest rESTRequest, RESTEvent rESTEvent);

        void onRestSuccess(RESTServiceProvider rESTServiceProvider, RESTRequest rESTRequest, RESTEvent rESTEvent);
    }

    public interface RESTRequestIntecept {
        boolean onIntecept(RESTRequest rESTRequest, RESTRequest rESTRequest2);
    }

    public RESTRequest(RESTService service, String szURI) {
        super(service, szURI);
        RESTRequestManager.registerRequest(this);
        this.mService = service;
        this.mszURI = szURI;
        splitURIParam();
    }

    public RESTRequest(RESTService service, Class<? extends RESTServiceProvider> providerClass) {
        super(service, null);
        RESTRequestManager.registerRequest(this);
        this.mService = service;
        this.mProvider = service.findProvider((Class) providerClass);
        this.mszURI = this.mProvider.mszURI;
        splitURIParam();
    }

    public RESTRequest(RESTService service, RESTRequest parentRequest, String szURI) {
        super(service, szURI);
        RESTRequestManager.registerRequest(this);
        this.mService = service;
        this.mParentRequest = parentRequest;
        this.mszURI = szURI;
        splitURIParam();
    }

    public RESTRequest(RESTService service, RESTRequest parentRequest, Class<? extends RESTServiceProvider> providerClass) {
        super(service, null);
        RESTRequestManager.registerRequest(this);
        this.mService = service;
        this.mParentRequest = parentRequest;
        this.mProvider = service.findProvider((Class) providerClass);
        this.mszURI = this.mProvider.mszURI;
        splitURIParam();
    }

    public String getRequestGUID() {
        return this.mszRequestGUID;
    }

    public RESTRequest setProvider(RESTServiceProvider provider) {
        this.mProvider = provider;
        return this;
    }

    public RESTRequest setCallBack(RESTRequestCallBack callBack) {
        this.mCallBack = callBack;
        return this;
    }

    public RESTRequest setOnBeforeStart(RESTRequestBeforeStart beforeStartCallBack) {
        this.mBeforeStartCallBack = beforeStartCallBack;
        return this;
    }

    public RESTRequestCallBack getCallBack() {
        return this.mCallBack;
    }

    public void setCancel(boolean bCancelled) {
        this.mbCancel = true;
        if (this.mParentRequest != null) {
            this.mParentRequest.setCancel(bCancelled);
        }
    }

    public void setSilence(boolean bSilence) {
        this.mbSilence = bSilence;
        if (this.mParentRequest != null) {
            this.mParentRequest.setSilence(bSilence);
        }
    }

    public boolean getCancel() {
        return this.mbCancel;
    }

    public RESTRequest getParentRequest() {
        return this.mParentRequest;
    }

    public boolean isFinish() {
        return this.mbFinish;
    }

    public boolean isAllFinish() {
        boolean bFinish = this.mbFinish;
        if (this.mParentRequest != null) {
            return bFinish & this.mParentRequest.isAllFinish();
        }
        return bFinish;
    }

    private void splitURIParam() {
        int nParamPos = this.mszURI.indexOf("?");
        if (nParamPos != -1) {
            String szParams = this.mszURI.substring(nParamPos + 1);
            this.mszURI = this.mszURI.substring(nParamPos);
            String[] arrParams = szParams.split("&");
            for (String szOneParam : arrParams) {
                String[] arrList = szOneParam.split("=");
                if (arrList.length >= 2) {
                    this.marrParams.put(arrList[0], arrList[1]);
                }
            }
        }
    }

    public RESTRequest param(String szParamName, String szValue) {
        this.marrParams.put(szParamName, szValue);
        return this;
    }

    public RESTRequest param(String szParamName, int nValue) {
        this.marrParams.put(szParamName, String.valueOf(nValue));
        return this;
    }

    public String getParam(String szParamName) {
        return (String) this.marrParams.get(szParamName);
    }

    public RESTRequest chainRequest(String szURI) {
        this.mChainRequest = new RESTRequest(this.mService, this, szURI);
        return this.mChainRequest;
    }

    public RESTRequest chainRequest(Class<? extends RESTServiceProvider> providerClass) {
        this.mChainRequest = new RESTRequest(this.mService, this, (Class) providerClass);
        return this.mChainRequest;
    }

    public RESTRequest uniqueRequest(String szOperationName) {
        if (RESTRequestManager.isUniqueRequestFinished(szOperationName)) {
            this.mszOperationName = szOperationName;
            RESTRequestManager.registerUnique(szOperationName, this);
        } else {
            Log.e(getClass().getCanonicalName(), "Previous " + szOperationName + " is not finish. Skip new one.");
            setCancel(true);
            setSilence(true);
        }
        return this;
    }

    private void startOurOwn(final RESTRequestCallBack callBack) {
        if (this.mProvider == null) {
            this.mProvider = this.mService.findProvider(this.mszURI);
        }
        if (this.mbCancel) {
            if (!this.mbSilence) {
                callBack.onRestFailure(this.mProvider, this, RESTEvent.getCancelEvent());
            }
            cancelRequestRegister();
        } else if (this.mProvider != null) {
            if (this.mBeforeStartCallBack != null) {
                this.mBeforeStartCallBack.onBeforeStart(this.mProvider, this);
            }
            RESTRequestCallBack restCallBack = new RESTRequestCallBack() {
                public void onRestSuccess(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                    RESTRequest.this.mbFinish = true;
                    if (RESTRequest.this.mbCancel) {
                        if (!RESTRequest.this.mbSilence) {
                            callBack.onRestFailure(service, request, event);
                        }
                        RESTRequest.this.cancelRequestRegister();
                    } else if (RESTRequest.this.mChainRequest != null) {
                        RESTRequest.this.mChainRequest.marrParams.putAll(RESTRequest.this.marrParams);
                        RESTRequest.this.mChainRequest.startOurOwn(callBack);
                    } else {
                        if (!RESTRequest.this.mbSilence) {
                            callBack.onRestSuccess(service, request, event);
                        }
                        RESTRequest.this.cancelRequestRegister();
                    }
                }

                public void onRestFailure(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                    RESTRequest.this.mbFinish = true;
                    if (!RESTRequest.this.mbCancel) {
                        RESTRequest.this.mProvider = RESTRequest.this.mService.findNextProvider(RESTRequest.this.mProvider);
                        if (RESTRequest.this.mProvider != null) {
                            RESTRequest.this.startOurOwn(callBack);
                            return;
                        }
                    }
                    if (!RESTRequest.this.mbSilence) {
                        callBack.onRestFailure(service, request, event);
                    }
                    RESTRequest.this.cancelRequestRegister();
                }
            };
            if (!this.mProvider.execute(this, this.mszURI, this.marrParams, restCallBack)) {
                restCallBack.onRestFailure(this.mProvider, this, RESTEvent.getEvent(RESTEvent.RESULT_FAILURE));
            }
        } else {
            throw new IllegalArgumentException("No provider can handle " + this.mszURI + " address.");
        }
    }

    public RESTRequest start(RESTRequestCallBack callBack) {
        if (this.mParentRequest != null) {
            this.mParentRequest.start(callBack);
        } else {
            startOurOwn(callBack);
        }
        return this;
    }

    private void cancelRequestRegister() {
        RESTRequestManager.removeRequest(this.mszRequestGUID, this.mszOperationName);
        if (this.mParentRequest != null) {
            this.mParentRequest.cancelRequestRegister();
        }
    }
}
