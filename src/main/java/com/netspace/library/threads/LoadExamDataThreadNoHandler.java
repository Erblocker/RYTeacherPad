package com.netspace.library.threads;

import android.content.Context;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.cookie.SM;
import org.apache.http.protocol.HTTP;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalDate;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.KeepAliveHttpsTransportSE;
import org.w3c.dom.Element;

public class LoadExamDataThreadNoHandler extends Thread {
    public static final String NAMESPACE_MESSAGE = "http://webservice.myi.cn/wmstudyservice/wsdl/";
    private static String URL = "http://webservice.myi.cn:8089/wmexam/wmstudyservice.WSDL";
    private static String m_szUserGUID = "ffffffffffffffffffffffffffffffff";
    private static String m_szUserName = "paduser";
    private Context m_Context;
    private SoapDataReadyInterface m_InterfaceSoapReady;
    private SoapObject m_SoapObject;
    private boolean m_bCancelled = false;
    private int m_nErrorCode = 0;
    private String m_szErrorDescription = "未指定错误原因。";
    private String m_szMethodName;
    private String m_szPrivateData;
    private LoadExamDataThreadNoHandler m_this = this;

    public interface SoapDataReadyInterface {
        void OnSoapDataReady(String str, Object obj, int i, String str2, LoadExamDataThreadNoHandler loadExamDataThreadNoHandler, int i2);
    }

    public LoadExamDataThreadNoHandler(Context Context, String szMethodName, SoapDataReadyInterface DataReadyHandler) {
        this.m_szMethodName = szMethodName;
        this.m_InterfaceSoapReady = DataReadyHandler;
        this.m_Context = Context;
        this.m_SoapObject = new SoapObject(NAMESPACE_MESSAGE, szMethodName);
    }

    public LoadExamDataThreadNoHandler(Context Context, String szMethodName, SoapDataReadyInterface DataReadyHandler, SoapObject SoapObject) {
        this.m_szMethodName = szMethodName;
        this.m_InterfaceSoapReady = DataReadyHandler;
        this.m_Context = Context;
        this.m_SoapObject = SoapObject;
    }

    public static void setUserInfo(String szUserName, String szUserGUID) {
        m_szUserName = szUserName;
        m_szUserGUID = szUserGUID;
    }

    public void SetupServerAddress(String szServiceAddress) {
        URL = "http://" + szServiceAddress + "/wmexam/wmstudyservice.WSDL";
    }

    public SoapObject GetNewSoapObject(String szParamName) {
        return new SoapObject(NAMESPACE_MESSAGE, szParamName);
    }

    public void AddSoapObject(SoapObject obj) {
        this.m_SoapObject.addSoapObject(obj);
    }

    public void AddParam(String szParamName, Object obj) {
        this.m_SoapObject.addProperty(szParamName, obj);
    }

    public Object GetParam(String szParamName) {
        return this.m_SoapObject.getProperty(szParamName);
    }

    public String getPrivateData() {
        return this.m_szPrivateData;
    }

    public void setPrivateData(String szPrivateData) {
        this.m_szPrivateData = szPrivateData;
    }

    public String getErrorDescription() {
        return this.m_szErrorDescription;
    }

    public int getErrorCode() {
        return this.m_nErrorCode;
    }

    public void setCancel(boolean bCancel) {
        this.m_bCancelled = bCancel;
    }

    public Element GetXMLDocument(String szXML) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement();
        } catch (Exception e) {
            this.m_nErrorCode = -2;
            this.m_szErrorDescription = e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    public void Retry() {
        LoadExamDataThreadNoHandler NewThread = new LoadExamDataThreadNoHandler(this.m_Context, this.m_szMethodName, this.m_InterfaceSoapReady, this.m_SoapObject);
        NewThread.setPrivateData(this.m_szPrivateData);
        NewThread.start();
    }

    private int GetSoapReturnCode(Object SoapObject) {
        if (SoapObject instanceof Vector) {
            Vector arrResult = (Vector) SoapObject;
            return Integer.parseInt(arrResult.get(arrResult.size() - 1).toString());
        } else if (SoapObject instanceof SoapPrimitive) {
            return Integer.parseInt(SoapObject.toString());
        } else {
            return -1;
        }
    }

    private Object GetSoapServiceData(String szMethodName, SoapObject request) {
        HttpTransportSE transport;
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        new MarshalDate().register(envelope);
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
        transport.debug = true;
        try {
            ArrayList reqHeaders = new ArrayList();
            reqHeaders.add(new HeaderProperty(SM.COOKIE, "userguid=" + m_szUserGUID + ";username=" + m_szUserName + ";usergroupguid=" + m_szUserGUID + ";"));
            transport.call(new StringBuilder(NAMESPACE_MESSAGE).append(szMethodName).toString(), envelope, reqHeaders);
            return envelope.getResponse();
        } catch (Exception e) {
            this.m_nErrorCode = -1;
            this.m_szErrorDescription = e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    public void run() {
        Object objResult;
        Log.d("LoadExamDataThreadNoHandler", "run for method " + this.m_szMethodName);
        if (Utilities.isNetworkConnected(this.m_Context)) {
            objResult = GetSoapServiceData(this.m_szMethodName, this.m_SoapObject);
        } else {
            this.m_nErrorCode = -1;
            this.m_szErrorDescription = "没有检测到有效的网络连接。";
            objResult = null;
        }
        if (!(this.m_InterfaceSoapReady == null || this.m_bCancelled)) {
            this.m_InterfaceSoapReady.OnSoapDataReady(this.m_szMethodName, objResult, this.m_nErrorCode, this.m_szErrorDescription, this.m_this, GetSoapReturnCode(objResult));
        }
        this.m_SoapObject = null;
    }
}
