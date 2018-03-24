package com.netspace.library.threads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.utilities.MarshalDouble;
import com.netspace.library.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.cookie.SM;
import org.apache.http.protocol.HTTP;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.MarshalFloat;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.KeepAliveHttpsTransportSE;
import org.ksoap2.transport.Transport;
import org.w3c.dom.Element;

public class LoadExamDataThread3 extends Thread {
    protected static final String NAMESPACE_MESSAGE = "http://webservice.myi.cn/wmstudyservice/wsdl/";
    private static final String TAG = "LoadExamDataThread3";
    protected static int UI_MSG_FAILURE = -1;
    protected static int UI_MSG_SUCCESS = 0;
    protected static String URL = "http://webservice.myi.cn:8089/wmexam/wmstudyservice.WSDL";
    protected static Integer mCurrentRunningThreadCount = Integer.valueOf(0);
    protected static String mServiceAddress = "";
    protected static OnSessionFailListener mSessionFailureListener;
    protected static String mSessionID = "";
    protected static String mUserGUID = "ffffffffffffffffffffffffffffffff";
    protected static String mUserName = "paduser";
    protected static ArrayList<WeakReference<LoadExamDataThread3>> mWorkingThreads = new ArrayList();
    protected final int MAX_CONCURRENT_COUNT;
    protected boolean mBusy;
    protected boolean mCancelled;
    protected WeakReference<Context> mContext;
    protected int mErrorCode;
    protected String mErrorDescription;
    protected String mMethodName;
    protected OnSoapCompleteListener mOnSoapCompleteListener;
    protected OnSoapFailListener mOnSoapFailListener;
    protected Object mPrivateData;
    protected String mProgressText;
    protected String mReplaceUserGUID;
    protected SoapObject mSoapObject;
    protected ArrayList<Integer> mSuccessReturnCodes;
    protected UIThreadMessageHandler mUIHandler;
    protected boolean mUseSuperAdmin;

    public interface OnSessionFailListener {
        void OnSessionFail();
    }

    public interface OnSoapCompleteListener {
        void OnDataOK(Vector vector, LoadExamDataThread3 loadExamDataThread3, int i);
    }

    public interface OnSoapFailListener {
        boolean OnDataFail(LoadExamDataThread3 loadExamDataThread3, int i);
    }

    private class UIThreadMessageHandler extends Handler {
        private UIThreadMessageHandler() {
        }

        public void handleMessage(Message msg) {
            if (!LoadExamDataThread3.this.mCancelled) {
                if (msg.what == LoadExamDataThread3.UI_MSG_SUCCESS) {
                    if (LoadExamDataThread3.this.mOnSoapCompleteListener != null) {
                        if (msg.obj instanceof Vector) {
                            LoadExamDataThread3.this.mOnSoapCompleteListener.OnDataOK((Vector) msg.obj, LoadExamDataThread3.this, LoadExamDataThread3.this.getSoapReturnCode(msg.obj));
                        } else {
                            LoadExamDataThread3.this.mOnSoapCompleteListener.OnDataOK(null, LoadExamDataThread3.this, LoadExamDataThread3.this.getSoapReturnCode(msg.obj));
                        }
                    }
                    LoadExamDataThread3.this.mSoapObject = null;
                    msg.obj = null;
                } else if (msg.what == LoadExamDataThread3.UI_MSG_FAILURE) {
                    if (LoadExamDataThread3.this.mOnSoapFailListener != null) {
                        LoadExamDataThread3.this.mOnSoapFailListener.OnDataFail(LoadExamDataThread3.this, LoadExamDataThread3.this.mErrorCode);
                    }
                    LoadExamDataThread3.this.mSoapObject = null;
                    msg.obj = null;
                }
            }
            LoadExamDataThread3.this.mOnSoapCompleteListener = null;
            LoadExamDataThread3.this.mOnSoapFailListener = null;
        }
    }

    public LoadExamDataThread3(Context Context, String szMethodName, OnSoapCompleteListener OnSoapCompleteListener) {
        this(Context, szMethodName, OnSoapCompleteListener, null);
    }

    public LoadExamDataThread3(Context Context, String szMethodName, OnSoapCompleteListener OnSoapCompleteListener, SoapObject SoapObject) {
        this.MAX_CONCURRENT_COUNT = 10;
        this.mProgressText = "正在获取数据，请稍候...";
        this.mErrorCode = 0;
        this.mErrorDescription = "未指定错误原因。";
        this.mCancelled = false;
        this.mBusy = false;
        this.mUIHandler = null;
        this.mUseSuperAdmin = false;
        this.mContext = new WeakReference(Context);
        this.mOnSoapCompleteListener = OnSoapCompleteListener;
        this.mMethodName = szMethodName;
        if (SoapObject != null) {
            this.mSoapObject = SoapObject;
        } else {
            this.mSoapObject = new SoapObject("http://webservice.myi.cn/wmstudyservice/wsdl/", szMethodName);
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            this.mUIHandler = new UIThreadMessageHandler();
        }
        this.mSuccessReturnCodes = new ArrayList();
        this.mSuccessReturnCodes.add(Integer.valueOf(0));
        synchronized (mWorkingThreads) {
            mWorkingThreads.add(new WeakReference(this));
        }
    }

    private void removeFromWorkingThread() {
        synchronized (mWorkingThreads) {
            boolean bModified = true;
            while (bModified) {
                bModified = false;
                Iterator it = mWorkingThreads.iterator();
                while (it.hasNext()) {
                    WeakReference<LoadExamDataThread3> OneThread = (WeakReference) it.next();
                    LoadExamDataThread3 OneObject = (LoadExamDataThread3) OneThread.get();
                    if (OneObject != null) {
                        if (OneObject.equals(this)) {
                            mWorkingThreads.remove(OneThread);
                            bModified = true;
                            break;
                        }
                    }
                    mWorkingThreads.remove(OneThread);
                    bModified = true;
                    break;
                }
            }
        }
    }

    public static void cancelAll() {
        synchronized (mWorkingThreads) {
            Iterator it = mWorkingThreads.iterator();
            while (it.hasNext()) {
                LoadExamDataThread3 OneObject = (LoadExamDataThread3) ((WeakReference) it.next()).get();
                if (OneObject != null) {
                    OneObject.setCancel(true);
                }
            }
        }
    }

    public static void cancelAndWaitAll() {
        synchronized (mWorkingThreads) {
            Iterator it = mWorkingThreads.iterator();
            while (it.hasNext()) {
                LoadExamDataThread3 OneObject = (LoadExamDataThread3) ((WeakReference) it.next()).get();
                if (OneObject != null) {
                    OneObject.setCancel(true);
                }
            }
        }
        boolean z = false;
        while (!z) {
            WeakReference<LoadExamDataThread3> OneThread = null;
            synchronized (mWorkingThreads) {
                if (mWorkingThreads.size() > 0) {
                    OneThread = (WeakReference) mWorkingThreads.get(0);
                } else {
                    z = true;
                }
            }
            if (OneThread != null) {
                try {
                    ((LoadExamDataThread3) OneThread.get()).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setUseSuperAdmin(boolean bUse) {
        this.mUseSuperAdmin = bUse;
    }

    public void setProgressText(String szProgressText) {
        this.mProgressText = szProgressText;
    }

    public static void setUserInfo(String szUserName, String szUserGUID) {
        mUserName = szUserName;
        if (!szUserGUID.isEmpty()) {
            mUserGUID = szUserGUID;
        }
    }

    public static void setupServerAddress(String szServiceAddress) {
        URL = "http://" + szServiceAddress + "/wmexam/wmstudyservice.WSDL";
        mServiceAddress = szServiceAddress;
    }

    public static String getServerAddress() {
        return mServiceAddress;
    }

    public static void setSessionID(String szSessionID) {
        mSessionID = szSessionID;
    }

    public static void setSessionFailListener(OnSessionFailListener SessionFailureListener) {
        mSessionFailureListener = SessionFailureListener;
    }

    public void addSoapObject(SoapObject obj) {
        this.mSoapObject.addSoapObject(obj);
    }

    public void addParam(String szParamName, Object obj) {
        this.mSoapObject.addProperty(szParamName, obj);
    }

    public void addParam(String szParamName, ArrayList arrValues) {
        SoapObject soapParams = new SoapObject("http://webservice.myi.cn/wmstudyservice/wsdl/", szParamName);
        for (int i = 0; i < arrValues.size(); i++) {
            soapParams.addProperty("anyType", arrValues.get(i));
        }
        addSoapObject(soapParams);
    }

    public Object getParam(String szParamName) {
        return this.mSoapObject.getProperty(szParamName);
    }

    public Object getPrivateData() {
        return this.mPrivateData;
    }

    public void setPrivateData(Object PrivateData) {
        this.mPrivateData = PrivateData;
    }

    public String getErrorDescription() {
        return this.mErrorDescription;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public void setCancel(boolean bCancel) {
        this.mCancelled = bCancel;
        if (this.mCancelled) {
            this.mOnSoapCompleteListener = null;
            this.mOnSoapFailListener = null;
        }
    }

    public String getStringParam(Vector vector, int nParamIndex) {
        if (vector.size() > nParamIndex) {
            Object OneObject = vector.get(nParamIndex);
            if (OneObject instanceof SoapPrimitive) {
                return OneObject.toString();
            }
        }
        return null;
    }

    public ArrayList<String> getStringArrayParam(Vector vector, int nParamIndex) {
        ArrayList<String> arrResult = new ArrayList();
        if (!(vector.get(nParamIndex) instanceof SoapObject)) {
            return null;
        }
        SoapObject Obj1 = (SoapObject) vector.get(nParamIndex);
        for (int i = 0; i < Obj1.getPropertyCount(); i++) {
            arrResult.add(Obj1.getPropertyAsString(i));
        }
        return arrResult;
    }

    public ArrayList<Integer> getIntegerArrayParam(Vector vector, int nParamIndex) {
        ArrayList<Integer> arrResult = new ArrayList();
        if (!(vector.get(nParamIndex) instanceof SoapObject)) {
            return null;
        }
        SoapObject Obj1 = (SoapObject) vector.get(nParamIndex);
        for (int i = 0; i < Obj1.getPropertyCount(); i++) {
            arrResult.add(Integer.valueOf(Obj1.getPropertyAsString(i)));
        }
        return arrResult;
    }

    public void setOnSoapFailListener(OnSoapFailListener Listener) {
        this.mOnSoapFailListener = Listener;
    }

    public void setAllowReturnCode(int nReturnCode) {
        this.mSuccessReturnCodes.add(Integer.valueOf(nReturnCode));
    }

    public void setUserGUID(String szUserGUID) {
        this.mReplaceUserGUID = szUserGUID;
    }

    public Element getXMLDocument(String szXML) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getSoapReturnCode(Object SoapObject) {
        if (SoapObject instanceof Vector) {
            Vector arrResult = (Vector) SoapObject;
            return Integer.parseInt(arrResult.get(arrResult.size() - 1).toString());
        } else if (SoapObject instanceof SoapPrimitive) {
            return Integer.parseInt(SoapObject.toString());
        } else {
            return -1;
        }
    }

    private Object getSoapServiceData(String szMethodName, SoapObject request) {
        Transport transport;
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        new MarshalDate().register(envelope);
        new MarshalFloat().register(envelope);
        new MarshalDouble().register(envelope);
        URL targetURL = null;
        try {
            targetURL = new URL(URL);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        if (MyiBaseApplication.isUseSSL()) {
            transport = new KeepAliveHttpsTransportSE(targetURL.getHost(), targetURL.getPort(), targetURL.getPath(), 30000);
        } else {
            transport = new HttpTransportSE2(URL, (int) DeviceOperationRESTServiceProvider.TIMEOUT);
        }
        try {
            HeaderProperty CookieHeader;
            ArrayList<HeaderProperty> reqHeaders = new ArrayList();
            String szUserGUID = mUserGUID;
            if (this.mUseSuperAdmin) {
                szUserGUID = "ffffffffffffffffffffffffffffffff";
            }
            if (this.mReplaceUserGUID != null) {
                szUserGUID = this.mReplaceUserGUID;
            }
            if (mSessionID.isEmpty()) {
                CookieHeader = new HeaderProperty(SM.COOKIE, "userguid=" + szUserGUID + ";username=" + mUserName + ";usergroupguid=" + szUserGUID + ";");
            } else if (this.mReplaceUserGUID == null) {
                CookieHeader = new HeaderProperty(SM.COOKIE, "sessionid=" + mSessionID + ";userguid=ffffffffffffffffffffffffffffffff");
            } else {
                CookieHeader = new HeaderProperty(SM.COOKIE, "sessionid=" + mSessionID + ";userguid=" + szUserGUID + ";");
            }
            reqHeaders.add(CookieHeader);
            transport.call("http://webservice.myi.cn/wmstudyservice/wsdl/" + szMethodName, envelope, reqHeaders);
            return envelope.getResponse();
        } catch (Exception e) {
            this.mErrorCode = -100;
            this.mErrorDescription = e.getMessage();
            if (this.mErrorDescription != null && (this.mErrorDescription.equalsIgnoreCase("Error -4063") || this.mErrorDescription.equalsIgnoreCase("Error -4066"))) {
                this.mErrorCode = -4063;
                this.mErrorDescription = "当前会话已无效，请重新登录";
                if (!(szMethodName.equalsIgnoreCase("UsersLogout") || mSessionFailureListener == null)) {
                    mSessionFailureListener.OnSessionFail();
                    mSessionFailureListener = null;
                }
            }
            Log.e(TAG, "Error while process method " + szMethodName);
            e.printStackTrace();
            return null;
        }
    }

    public void run() {
        setName("LoadExamDataThread3(" + String.valueOf(Thread.currentThread().getId()) + ") exec " + this.mMethodName + " thread waiting");
        int nCurrentRunningCount = mCurrentRunningThreadCount.intValue();
        while (nCurrentRunningCount > 10) {
            try {
                Thread.sleep(50);
                synchronized (mCurrentRunningThreadCount) {
                    nCurrentRunningCount = mCurrentRunningThreadCount.intValue();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        setName("LoadExamDataThread3(" + String.valueOf(Thread.currentThread().getId()) + ") exec " + this.mMethodName + " thread");
        boolean bError = false;
        Context Context = (Context) this.mContext.get();
        long nStartTime = System.currentTimeMillis();
        long nNetworkTimeCost = 0;
        if (Context == null) {
            Log.d(TAG, "context invalid. Skip running for method " + this.mMethodName);
            removeFromWorkingThread();
            this.mSoapObject = null;
            this.mOnSoapCompleteListener = null;
            this.mOnSoapFailListener = null;
            return;
        }
        Object objResult;
        this.mBusy = true;
        if (Utilities.isNetworkConnected(Context)) {
            long nNetworkStartTime = System.currentTimeMillis();
            synchronized (mCurrentRunningThreadCount) {
                mCurrentRunningThreadCount = Integer.valueOf(mCurrentRunningThreadCount.intValue() + 1);
            }
            objResult = getSoapServiceData(this.mMethodName, this.mSoapObject);
            synchronized (mCurrentRunningThreadCount) {
                mCurrentRunningThreadCount = Integer.valueOf(mCurrentRunningThreadCount.intValue() - 1);
            }
            nNetworkTimeCost = System.currentTimeMillis() - nNetworkStartTime;
            if (objResult == null) {
                bError = true;
            } else {
                int nRetCode = getSoapReturnCode(objResult);
                boolean bFound = false;
                Iterator it = this.mSuccessReturnCodes.iterator();
                while (it.hasNext()) {
                    if (((Integer) it.next()).intValue() == nRetCode) {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    this.mErrorCode = nRetCode;
                    this.mErrorDescription = ErrorCode.getErrorMessage(this.mErrorCode);
                    bError = true;
                }
            }
        } else {
            Log.d(TAG, "network is not connected.");
            this.mErrorCode = ErrorCode.ERROR_NO_INTERNET_CONNECTION;
            this.mErrorDescription = ErrorCode.getErrorMessage(this.mErrorCode);
            objResult = null;
            bError = true;
        }
        if (this.mCancelled) {
            this.mSoapObject = null;
            this.mOnSoapCompleteListener = null;
            this.mOnSoapFailListener = null;
        } else if (this.mUIHandler == null) {
            if (bError) {
                if (this.mOnSoapFailListener != null) {
                    this.mOnSoapFailListener.OnDataFail(this, this.mErrorCode);
                }
                this.mSoapObject = null;
            } else {
                if (this.mOnSoapCompleteListener != null) {
                    if (objResult instanceof Vector) {
                        this.mOnSoapCompleteListener.OnDataOK((Vector) objResult, this, getSoapReturnCode(objResult));
                    } else {
                        this.mOnSoapCompleteListener.OnDataOK(null, this, getSoapReturnCode(objResult));
                    }
                }
                this.mSoapObject = null;
            }
            this.mOnSoapCompleteListener = null;
            this.mOnSoapFailListener = null;
        } else if (bError) {
            this.mUIHandler.obtainMessage(UI_MSG_FAILURE).sendToTarget();
        } else {
            this.mUIHandler.obtainMessage(UI_MSG_SUCCESS, objResult).sendToTarget();
        }
        this.mBusy = false;
        removeFromWorkingThread();
        Log.i(TAG, this.mMethodName + " run complete. Timecost " + String.valueOf(System.currentTimeMillis() - nStartTime) + " (" + String.valueOf(nNetworkTimeCost) + ") ms.");
    }

    public static String getCurrentUserGUID() {
        return mUserGUID;
    }
}
