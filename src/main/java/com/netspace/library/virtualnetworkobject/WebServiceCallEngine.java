package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;
import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

public class WebServiceCallEngine extends Engine {
    private static final String TAG = "WebServiceCallEngine";
    protected String mEngineName = TAG;

    public WebServiceCallEngine(Context Context) {
        super(Context);
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public boolean handlePackageRead(final ItemObject OneObject) {
        String szMethodName = OneObject.getObjectURI();
        setItemObjectActivityBusy(OneObject, true);
        LoadExamDataThread3 mWorkThread = new LoadExamDataThread3(this.mContext, szMethodName, new OnSoapCompleteListener() {
            public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                int nParamIndex = 0;
                WebServiceCallEngine.this.setItemObjectActivityBusy(OneObject, false);
                if (arrSoapData != null) {
                    Iterator it = arrSoapData.iterator();
                    while (it.hasNext()) {
                        AttributeContainer Obj = it.next();
                        if (Obj instanceof SoapPrimitive) {
                            SoapPrimitive OneObj = (SoapPrimitive) Obj;
                            OneObject.setParam(String.valueOf(nParamIndex), OneObj.toString());
                            if (nParamIndex == 0) {
                                OneObject.writeTextData(OneObj.toString());
                            }
                        } else if (Obj instanceof SoapObject) {
                            SoapObject Obj1 = (SoapObject) Obj;
                            ArrayList<String> arrResult = new ArrayList();
                            for (int i = 0; i < Obj1.getPropertyCount(); i++) {
                                PropertyInfo OneProperty = Obj1.getProperty(i);
                                boolean bValueSet = false;
                                if (OneProperty instanceof PropertyInfo) {
                                    if (OneProperty.getValue() instanceof SoapPrimitive) {
                                        arrResult.add(Obj1.getPropertyAsString(i));
                                        bValueSet = true;
                                    }
                                } else if (OneProperty instanceof SoapPrimitive) {
                                    arrResult.add(Obj1.getPropertyAsString(i));
                                    bValueSet = true;
                                }
                                if (!bValueSet) {
                                    arrResult.add("");
                                }
                            }
                            OneObject.setParam(String.valueOf(nParamIndex), arrResult);
                        }
                        nParamIndex++;
                    }
                }
                OneObject.callCallbacks(true, nReturnCode);
            }
        });
        mWorkThread.setOnSoapFailListener(new OnSoapFailListener() {
            public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                WebServiceCallEngine.this.setItemObjectActivityBusy(OneObject, false);
                OneObject.mReturnMessage = ThreadObject.getErrorDescription();
                OneObject.callCallbacks(false, nReturnCode);
                return true;
            }
        });
        for (Entry<String, Object> entry : OneObject.mParams.entrySet()) {
            String szKey = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof ArrayList) {
                mWorkThread.addParam(szKey, (ArrayList) value);
            } else {
                mWorkThread.addParam(szKey, value);
            }
        }
        mWorkThread.setAllowReturnCode(1);
        WebServiceCallItemObject WebServiceCallItemObject = (WebServiceCallItemObject) OneObject;
        ArrayList<Integer> arrReturnCodes = WebServiceCallItemObject.getAllowReturnCodes();
        for (int i = 0; i < arrReturnCodes.size(); i++) {
            mWorkThread.setAllowReturnCode(((Integer) arrReturnCodes.get(i)).intValue());
        }
        mWorkThread.setUseSuperAdmin(WebServiceCallItemObject.getUseSuperAdmin());
        if (WebServiceCallItemObject.mReplaceUserGUID != null) {
            mWorkThread.setUserGUID(WebServiceCallItemObject.mReplaceUserGUID);
        }
        mWorkThread.start();
        return true;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        return false;
    }
}
