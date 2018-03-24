package com.netspace.library.servers;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Const;
import com.netspace.library.consts.Features;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.threads.MulticastSendThread;
import com.netspace.library.utilities.Utilities;
import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class MJpegRelayToMulticastThread extends Thread {
    private static final String TAG = "MJpegRelayToMulticastThread";
    private Context mContext;
    private boolean mDecodePartImage = false;
    private String mMulticastAddress = "";
    private int mMulticastPort = 0;
    private MulticastSendThread mMulticastSendThread;
    private int mScreenHeight = -1;
    private int mScreenWidth = -1;
    private int mSourceHeight = -1;
    private int mSourceWidth = -1;
    private MJpegRelayCallBackInterface m_CallBack = null;
    private HttpClient m_HttpClient;
    private boolean m_bShortTimeWait = true;
    private volatile Boolean m_bWorking = Boolean.valueOf(true);
    private String m_szTargetIP = "";
    private boolean mbConnected = false;
    private boolean mbServerResponseH264 = false;

    public interface MJpegRelayCallBackInterface {
        void OnMJpegRelayError(String str);

        void OnMJpegRelayMessage(String str);

        void OnNewMJpegRelayInstance(MJpegRelayToMulticastThread mJpegRelayToMulticastThread);
    }

    public MJpegRelayToMulticastThread(Context context, String szTargetIP, String szTargetMulticastAddress, int nTargetPort, MJpegRelayCallBackInterface CallBack) {
        this.m_szTargetIP = szTargetIP;
        this.mMulticastAddress = szTargetMulticastAddress;
        this.mMulticastPort = nTargetPort;
        this.mMulticastSendThread = new MulticastSendThread(context, szTargetMulticastAddress, nTargetPort, Const.MULTICAST_TCP_PORT);
        this.mMulticastSendThread.start();
        this.m_bWorking = Boolean.valueOf(true);
        this.m_CallBack = CallBack;
        this.mContext = context;
    }

    public boolean isH264() {
        return this.mbServerResponseH264;
    }

    public boolean isConnected() {
        return this.mbConnected;
    }

    public int getPort() {
        return this.mMulticastPort;
    }

    public int getWidth() {
        return this.mSourceWidth;
    }

    public int getHeight() {
        return this.mSourceHeight;
    }

    public boolean isReady() {
        if (!this.mbConnected || this.mSourceWidth == -1 || this.mSourceHeight == -1) {
            return false;
        }
        return true;
    }

    public void setMulticastSendThread(MulticastSendThread SendThread) {
        this.mMulticastSendThread = SendThread;
    }

    public MulticastSendThread getMulticastSendThread() {
        return this.mMulticastSendThread;
    }

    public void stopThread() {
        this.m_bWorking = Boolean.valueOf(false);
        if (this.mMulticastSendThread != null) {
            this.mMulticastSendThread.stopThread();
            this.mMulticastSendThread = null;
        }
        if (!(this.m_HttpClient == null || this.m_HttpClient.getConnectionManager() == null)) {
            this.m_HttpClient.getConnectionManager().shutdown();
        }
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.m_CallBack = null;
    }

    public void run() {
        String szError;
        setName(TAG);
        if (this.m_bWorking.booleanValue()) {
            BasicHttpParams httpParams = new BasicHttpParams();
            if (this.m_bShortTimeWait) {
                HttpConnectionParams.setConnectionTimeout(httpParams, DeviceOperationRESTServiceProvider.TIMEOUT);
                HttpConnectionParams.setSoTimeout(httpParams, DeviceOperationRESTServiceProvider.TIMEOUT);
            } else {
                HttpConnectionParams.setConnectionTimeout(httpParams, DeviceOperationRESTServiceProvider.TIMEOUT);
            }
            this.m_HttpClient = new DefaultHttpClient(httpParams);
            HttpParams httpParameters = this.m_HttpClient.getParams();
            if (this.m_bShortTimeWait) {
                HttpConnectionParams.setConnectionTimeout(httpParameters, DeviceOperationRESTServiceProvider.TIMEOUT);
                HttpConnectionParams.setSoTimeout(httpParameters, DeviceOperationRESTServiceProvider.TIMEOUT);
            } else {
                HttpConnectionParams.setConnectionTimeout(httpParameters, DeviceOperationRESTServiceProvider.TIMEOUT);
            }
            Log.i(TAG, "1. Sending http request");
            try {
                HttpGet httpGet = new HttpGet(URI.create(this.m_szTargetIP));
                httpGet.addHeader("PartImage", "support");
                if (VERSION.SDK_INT >= 21 && (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_H264) || MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_H264_RECEIVEONLY))) {
                    httpGet.addHeader("Accept", "video/h264");
                }
                HttpResponse res = this.m_HttpClient.execute(httpGet);
                Log.i(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if (res.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
                    this.mbConnected = true;
                    if (this.m_CallBack != null) {
                        this.m_CallBack.OnMJpegRelayMessage("已连接到服务器");
                    }
                    MjpegInputStream InputStream = new MjpegInputStream(res.getEntity().getContent());
                    InputStream.setDecodePartImage(this.mDecodePartImage);
                    InputStream.setMulticastSendThread(this.mMulticastSendThread);
                    boolean bH264 = InputStream.isH264();
                    if (bH264) {
                        this.mSourceWidth = InputStream.getWidth();
                        this.mSourceHeight = InputStream.getHeight();
                        this.mScreenWidth = InputStream.getRealScreenWidth();
                        this.mScreenHeight = InputStream.getRealScreenHeight();
                        this.mbServerResponseH264 = true;
                        if (this.mSourceWidth <= 0 || this.mSourceHeight <= 0) {
                            Log.e(TAG, "Can not found a vaild height and width.");
                        }
                        while (this.m_bWorking.booleanValue()) {
                            if (bH264) {
                                InputStream.readH264Frame(null, null);
                            }
                        }
                        if (this.m_HttpClient.getConnectionManager() != null) {
                            this.m_HttpClient.getConnectionManager().shutdown();
                        }
                        if (this.mMulticastSendThread != null) {
                            this.mMulticastSendThread.stopThread();
                            this.mMulticastSendThread = null;
                        }
                        this.m_CallBack = null;
                        return;
                    }
                    Log.e(TAG, "Can not relay non-H.264 video to multicast.");
                    if (this.m_CallBack != null) {
                        this.m_CallBack.OnMJpegRelayMessage("当前不是H.264的服务器，不能进行转发。");
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.i(TAG, "Request failed-ClientProtocolException", e);
                if (this.m_CallBack != null) {
                    szError = e.getMessage();
                    if (szError == null || szError.isEmpty()) {
                        szError = "客户端协议异常";
                    }
                    this.m_CallBack.OnMJpegRelayError(szError);
                }
                if (this.m_bWorking.booleanValue()) {
                    createNewThread();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                if (this.m_CallBack != null) {
                    szError = e2.getMessage();
                    if (szError == null || szError.isEmpty()) {
                        szError = "IO异常";
                    }
                    this.m_CallBack.OnMJpegRelayError(szError);
                }
                Log.i(TAG, "Request failed-IOException", e2);
                if (this.m_bWorking.booleanValue()) {
                    createNewThread();
                }
            } catch (IllegalStateException e3) {
                if (this.m_CallBack != null) {
                    this.m_CallBack.OnMJpegRelayError(e3.getMessage());
                }
                e3.printStackTrace();
                if (this.m_bWorking.booleanValue()) {
                    createNewThread();
                }
            }
        }
    }

    private void createNewThread() {
        Log.e(TAG, "createNewThread");
        if (this.mMulticastSendThread != null) {
            this.mMulticastSendThread.stopThread();
            this.mMulticastSendThread = null;
        }
        final MJpegRelayToMulticastThread NewThread = new MJpegRelayToMulticastThread(this.mContext, this.m_szTargetIP, this.mMulticastAddress, this.mMulticastPort, this.m_CallBack);
        NewThread.m_bWorking = this.m_bWorking;
        if (this.m_CallBack != null) {
            this.m_CallBack.OnNewMJpegRelayInstance(NewThread);
        }
        Utilities.runOnUIThreadDelay(this.mContext, new Runnable() {
            public void run() {
                NewThread.start();
            }
        }, 1000);
    }
}
