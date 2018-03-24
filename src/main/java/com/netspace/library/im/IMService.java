package com.netspace.library.im;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.consts.Const;
import com.netspace.library.threads.DirectIMClientThread;
import com.netspace.library.threads.DirectIMClientThread.DirectIMDataReceiveCallBack;
import com.netspace.library.threads.MessagePostThread2;
import com.netspace.library.threads.MessageWaitThread.OnMessageArrivedListener;
import com.netspace.library.threads.MessageWaitThread2;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.library.window.ChatWindow;
import com.netspace.pad.library.R;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import wei.mark.standout.StandOutWindow;

public class IMService extends Service implements OnMessageArrivedListener, DirectIMDataReceiveCallBack {
    private static String SERVER_CLIENTID = "server";
    private static String TAG = "IMService";
    private static StatusBarDisplayer mCharStatusBarDisplayer;
    private static String mFromClientID;
    private static IMService mIMService;
    private static String mLastMessage = "";
    private static Date mLastMessageTime = new Date();
    private static MessagePostThread2 mMessagePostThread;
    private static int mMessageTimeout = Const.IM_DEFAULT_TIMEOUT;
    private static MessageWaitThread2 mMessageWaitThread;
    private static String mPostURL;
    private static String mURL;
    private static ArrayList<DirectIMClientThread> marrDirectIMClients = new ArrayList();
    private static ArrayList<OnIMServiceArrivedListener> marrListener = new ArrayList();
    private Handler mHandler = new Handler();
    private Runnable mMonitorRunnable = new Runnable() {
        public void run() {
            if (MyiBaseApplication.UseThreadModeForIM) {
                if (IMService.isDirectIMClientConnected(IMService.SERVER_CLIENTID)) {
                    IMService.this.mbMonitorRunning = false;
                    return;
                }
                IMService.this.mbMonitorRunning = true;
                IMService.this.mHandler.removeCallbacks(IMService.this.mReconnectRunnable);
                IMService.this.mHandler.post(IMService.this.mReconnectRunnable);
                IMService.this.mHandler.postDelayed(IMService.this.mMonitorRunnable, 2000);
            } else if (IMService.mMessageWaitThread == null) {
            } else {
                if (IMService.mMessageWaitThread.isConnected()) {
                    IMService.this.mbMonitorRunning = false;
                    return;
                }
                IMService.this.mbMonitorRunning = true;
                IMService.this.mHandler.removeCallbacks(IMService.this.mReconnectRunnable);
                IMService.this.mHandler.post(IMService.this.mReconnectRunnable);
                IMService.this.mHandler.postDelayed(IMService.this.mMonitorRunnable, 2000);
            }
        }
    };
    private Runnable mReconnectRunnable = new Runnable() {
        public void run() {
            if (IMService.mMessageWaitThread != null) {
                IMService.mMessageWaitThread.stopThread();
                IMService.mMessageWaitThread = null;
            }
            if (!MyiBaseApplication.UseThreadModeForIM) {
                IMService.mMessageWaitThread = new MessageWaitThread2(IMService.this, IMService.mURL, IMService.this);
                IMService.mMessageWaitThread.start();
            }
            if (MyiBaseApplication.UseThreadModeForIM && !IMService.isDirectIMClientConnected(IMService.SERVER_CLIENTID)) {
                try {
                    URL url = new URL(IMService.mURL);
                    IMService.addDirectIMClient(url.getHost(), url.getPort(), IMService.SERVER_CLIENTID, "GET " + url.getFile() + "&thread=on HTTP/1.1\r\n\r\n");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private boolean mbMonitorRunning = false;

    public interface OnIMServiceArrivedListener {
        void OnMessageArrived(String str);
    }

    public void registerCallBack(OnIMServiceArrivedListener MessageReceiveCallBack) {
        marrListener.add(MessageReceiveCallBack);
    }

    public boolean unregisterCallBack(OnIMServiceArrivedListener MessageReceiveCallBack) {
        for (int i = 0; i < marrListener.size(); i++) {
            if (((OnIMServiceArrivedListener) marrListener.get(i)).equals(MessageReceiveCallBack)) {
                marrListener.remove(i);
                return true;
            }
        }
        return false;
    }

    public static boolean isDirectIMClientConnected(String szClientID) {
        boolean bResult = false;
        synchronized (marrDirectIMClients) {
            for (int i = 0; i < marrDirectIMClients.size(); i++) {
                DirectIMClientThread oneClient = (DirectIMClientThread) marrDirectIMClients.get(i);
                if (oneClient.getClientID().equalsIgnoreCase(szClientID) && oneClient.isReady()) {
                    bResult = true;
                    break;
                }
            }
        }
        return bResult;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void addDirectIMClient(String szIP, String szClientID) {
        synchronized (marrDirectIMClients) {
            DirectIMClientThread oneClient;
            int i = 0;
            while (marrDirectIMClients.size() > 0) {
                oneClient = (DirectIMClientThread) marrDirectIMClients.get(i);
                if (oneClient.getClientID().equalsIgnoreCase(szClientID) && oneClient.getIP().equalsIgnoreCase(szIP) && oneClient.isReady()) {
                    return;
                } else if (oneClient.isReady() || oneClient.getActiveTime() <= 4000) {
                } else {
                    oneClient.shutdown();
                    marrDirectIMClients.remove(i);
                    i = (i - 1) + 1;
                }
            }
            oneClient = new DirectIMClientThread();
            oneClient.setAutoReconnect(true);
            oneClient.setCallBack(getIMService());
            oneClient.connect(szIP, szClientID);
            oneClient.start();
            marrDirectIMClients.add(oneClient);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void addDirectIMClient(String szIP, int nPort, String szClientID, String szHead) {
        synchronized (marrDirectIMClients) {
            DirectIMClientThread oneClient;
            int i = 0;
            while (marrDirectIMClients.size() > 0) {
                oneClient = (DirectIMClientThread) marrDirectIMClients.get(i);
                if (!(oneClient.getClientID().equalsIgnoreCase(szClientID) && oneClient.getIP().equalsIgnoreCase(szIP) && (oneClient.isReady() || oneClient.isConnecting()))) {
                    if (oneClient.isReady() || oneClient.getActiveTime() <= 4000) {
                    } else {
                        oneClient.shutdown();
                        marrDirectIMClients.remove(i);
                        i = (i - 1) + 1;
                    }
                }
            }
            oneClient = new DirectIMClientThread();
            oneClient.setAutoReconnect(true);
            oneClient.setConnectHead(szHead);
            oneClient.setCallBack(getIMService());
            oneClient.connect(szIP, nPort, szClientID);
            oneClient.start();
            marrDirectIMClients.add(oneClient);
        }
    }

    public boolean isConnected() {
        if (MyiBaseApplication.UseThreadModeForIM) {
            return isDirectIMClientConnected(SERVER_CLIENTID);
        }
        if (mMessageWaitThread != null) {
            return mMessageWaitThread.isConnected();
        }
        return false;
    }

    public static IMService getIMService() {
        if (mIMService == null) {
            mIMService = new IMService();
        }
        return mIMService;
    }

    public static String getIMUserName() {
        return mFromClientID;
    }

    public static void showChatNotifyBar(Context context) {
        if (!VirtualNetworkObject.getOfflineMode()) {
            mCharStatusBarDisplayer = new StatusBarDisplayer(context);
            mCharStatusBarDisplayer.setNotifyID(13);
            mCharStatusBarDisplayer.setTitle("在线答疑");
            mCharStatusBarDisplayer.setText("点击这里打开在线答疑对话框");
            mCharStatusBarDisplayer.setIcon(R.drawable.ic_chat_white);
            mCharStatusBarDisplayer.setAlwaysHere(true);
            mCharStatusBarDisplayer.setPendingIntent(PendingIntent.getService(context, 0, StandOutWindow.getShowIntent(context, ChatWindow.class, 1000), 134217728));
            mCharStatusBarDisplayer.showAlertBox();
        }
    }

    public static void hideChatNotifyBar() {
        if (mCharStatusBarDisplayer != null) {
            mCharStatusBarDisplayer.hideMessage();
            mCharStatusBarDisplayer.shutDown();
            mCharStatusBarDisplayer = null;
        }
    }

    public static String buildMessage(String szMessage, String szGUID, int nTimeout) {
        JSONObject JSON = new JSONObject();
        String szResult = null;
        try {
            JSON.put("guid", szGUID);
            JSON.put("expire", nTimeout);
            JSON.put("content", szMessage);
            JSON.put("from", mFromClientID);
            szResult = JSON.toString().trim();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return szResult;
    }

    public static String buildMessage(String szMessage, String szGUID, String szFrom, String szTo, int nTimeout) {
        JSONObject JSON = new JSONObject();
        String szResult = null;
        try {
            JSON.put("guid", szGUID);
            JSON.put("expire", nTimeout);
            JSON.put("content", szMessage);
            JSON.put("from", szFrom);
            JSON.put("to", szTo);
            szResult = JSON.toString().trim();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return szResult;
    }

    public static void resendMissingMessage() {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("IMResendMessage", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("lpszToClientID", mFromClientID);
        CallItem.setParam("nCount", Integer.valueOf(100));
        CallItem.setParam("nMessageIndex", Integer.valueOf(0));
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public static void resendMissingMessage(String szClientID, int nCount) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("IMResendMessage", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("lpszToClientID", szClientID);
        CallItem.setParam("nCount", Integer.valueOf(nCount));
        CallItem.setParam("nMessageIndex", Integer.valueOf(0));
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public static void resendMissingMessage(int nMessageIndex) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("IMResendMessage", null);
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("lpszToClientID", mFromClientID);
        CallItem.setParam("nCount", Integer.valueOf(1));
        CallItem.setParam("nMessageIndex", Integer.valueOf(nMessageIndex));
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void sendMessage(String szMessage, String szClientID) {
        String szURL = mPostURL;
        String[] arrClients = szClientID.split(";");
        String szNewClientID = "";
        for (String szOneClient : arrClients) {
            boolean bSkipThis = false;
            if (szOneClient.indexOf("*") == -1) {
                synchronized (marrDirectIMClients) {
                    for (int j = 0; j < marrDirectIMClients.size(); j++) {
                        DirectIMClientThread oneClient = (DirectIMClientThread) marrDirectIMClients.get(j);
                        if (oneClient.getClientID().equalsIgnoreCase(szOneClient) && oneClient.isReady() && oneClient.sendData(szMessage)) {
                            bSkipThis = true;
                            break;
                        }
                    }
                }
            }
            if (!bSkipThis) {
                if (!szNewClientID.isEmpty()) {
                    szNewClientID = new StringBuilder(String.valueOf(szNewClientID)).append(";").toString();
                }
                szNewClientID = new StringBuilder(String.valueOf(szNewClientID)).append(szOneClient).toString();
            }
        }
        szURL = new StringBuilder(String.valueOf(szURL)).append(szNewClientID).toString();
        if (!szNewClientID.isEmpty()) {
            if (isDirectIMClientConnected(SERVER_CLIENTID)) {
                sendMessage("SendMessage " + szClientID + " " + szMessage, SERVER_CLIENTID);
            } else {
                MessagePostThread2.setPostData(szMessage, szURL);
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        mIMService = this;
    }

    public static void setMessageWaitTimeout(int nTimeoutMS) {
        mMessageTimeout = nTimeoutMS;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getAction().contentEquals("init")) {
            mURL = intent.getStringExtra("listenUrl");
            mPostURL = intent.getStringExtra("postUrl");
            mFromClientID = intent.getStringExtra("from");
            if (mMessageWaitThread != null) {
                mMessageWaitThread.stopThread();
                mMessageWaitThread = null;
            }
            if (MyiBaseApplication.UseThreadModeForIM) {
                try {
                    URL url = new URL(mURL);
                    addDirectIMClient(url.getHost(), url.getPort(), SERVER_CLIENTID, "GET " + url.getFile() + "&thread=on HTTP/1.1\r\n\r\n");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                mMessageWaitThread = new MessageWaitThread2(this, mURL, this);
                mMessageWaitThread.start();
            }
            if (mMessagePostThread != null) {
                mMessagePostThread.stopThread();
                mMessagePostThread = null;
            }
            mMessagePostThread = new MessagePostThread2(mPostURL, null);
            mMessagePostThread.start();
        }
        return 1;
    }

    public static void shutdownDirectIMClients() {
        synchronized (marrDirectIMClients) {
            for (int i = 0; i < marrDirectIMClients.size(); i++) {
                DirectIMClientThread oneClient = (DirectIMClientThread) marrDirectIMClients.get(i);
                if (oneClient.isReady()) {
                    oneClient.shutdown();
                }
            }
            marrDirectIMClients.clear();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandler.removeCallbacks(this.mReconnectRunnable);
        this.mHandler.removeCallbacks(this.mMonitorRunnable);
        marrListener.clear();
        if (mMessageWaitThread != null) {
            mMessageWaitThread.stopThread();
            mMessageWaitThread = null;
        }
        if (mMessagePostThread != null) {
            mMessagePostThread.stopThread();
            mMessagePostThread = null;
        }
        shutdownDirectIMClients();
        ChatComponent.shutdown();
        hideChatNotifyBar();
    }

    public void OnMessageArrived(String szMessage) {
        this.mHandler.removeCallbacks(this.mReconnectRunnable);
        this.mHandler.postDelayed(this.mReconnectRunnable, (long) mMessageTimeout);
        if (!szMessage.isEmpty()) {
            Date messageDate = new Date();
            long nTimeDiff = messageDate.getTime() - mLastMessageTime.getTime();
            if (!szMessage.contentEquals(mLastMessage) || nTimeDiff >= 3000) {
                for (int i = 0; i < marrListener.size(); i++) {
                    ((OnIMServiceArrivedListener) marrListener.get(i)).OnMessageArrived(szMessage);
                }
            } else {
                Log.i("IMService", "Ignore message. Content is same as before.");
            }
            mLastMessage = szMessage;
            mLastMessageTime = messageDate;
        }
    }

    public void OnMessageError() {
        if (!this.mbMonitorRunning) {
            this.mHandler.removeCallbacks(this.mReconnectRunnable);
            this.mHandler.post(this.mReconnectRunnable);
            this.mHandler.postDelayed(this.mMonitorRunnable, 2000);
            this.mbMonitorRunning = true;
        }
    }

    public void onDataAvailable(String szOneline) {
        if (!szOneline.equalsIgnoreCase("ping")) {
            OnMessageArrived(szOneline);
        }
    }

    public void onConnectError() {
        Log.e(TAG, "DirectIMClient meet error.");
        OnMessageError();
    }

    public void OnConnected() {
        reportStatus("FirmwareVersion", Build.DISPLAY);
    }

    public void reportStatus(String szField, String szValue) {
        sendMessage(String.format(Utilities.getNow() + " " + getIMUserName() + ": StatusFieldsResult: %s %s", new Object[]{szField, szValue}), "System");
    }
}
