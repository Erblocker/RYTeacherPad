package com.netspace.library.im;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.netspace.teacherpad.TeacherPadApplication;
import java.util.Collection;
import java.util.Locale;

public class IMCore extends Thread {
    public static final int IMMSG_CONNECTION_FAILED = -2;
    public static final int IMMSG_LOGIN_FAILED = -1;
    public static final int IMMSG_LOGIN_SUCCESS = 1;
    public static final int IMMSG_MESSAGE_ARRIVAL = 2;
    public static final int IMMSG_MESSAGE_CONNECTION_LOST = -3;
    public static final int IMMSG_MESSAGE_CONNECTION_SUCCESS = 3;
    public static final int IMMSG_MESSAGE_RECONNECT = -4;
    public static final int IMMSG_SINGLE_LINE_MESSAGE = 4;
    private static final String TAG = "IMCore";
    protected IMMessageInterface m_CallBack;
    protected Context m_Context;
    protected IMInfo m_IMInfo = new IMInfo();
    private IMKeepAliveThread m_KeepAliveThread = new IMKeepAliveThread(this);
    protected IMThreadMessageHandler m_ThreadMessageHandler = new IMThreadMessageHandler(this);
    private Boolean m_bConnected = Boolean.TRUE;
    private Boolean m_bConnecting = Boolean.FALSE;
    private Boolean m_bPingReceived = Boolean.TRUE;
    protected String m_szIMLoginPassword;
    protected String m_szIMLoginUserName;

    public class IMInfo {
        public int nPort = 5222;
        public String szAppName = TeacherPadApplication.szAppName;
        public String szServerForConnect = "webservice.myi.cn";
        public String szServiceName = "webservice.myi.cn";
        public String szUserClassGUID;
        public String szUserName;
        public String szUserNameInIM;
    }

    private class IMKeepAliveThread extends Thread {
        private IMCore m_IMCore;
        private boolean m_bClosed = false;

        public IMKeepAliveThread(IMCore IMCore) {
            this.m_IMCore = IMCore;
        }

        public void run() {
            Log.d(IMCore.TAG, "IMKeepAliveThread start");
            setName("IMKeepAliveThread");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!this.m_bClosed) {
                Log.d(IMCore.TAG, "IMKeepAliveThread check");
                NetworkInfo activeNetwork = ((ConnectivityManager) IMCore.this.m_Context.getSystemService("connectivity")).getActiveNetworkInfo();
                boolean isConnected = false;
                if (activeNetwork != null) {
                    isConnected = activeNetwork.isConnectedOrConnecting();
                }
                if (!isConnected) {
                    Log.d("IM", "no active network. Check later.");
                } else if (this.m_IMCore.IsConnected()) {
                    if (this.m_IMCore.IsPingReceived()) {
                        this.m_IMCore.ResetPingState();
                        this.m_IMCore.SendPresence();
                    } else {
                        IMCore.this.m_ThreadMessageHandler.obtainMessage(-3, "没有收到服务器的应答，自动重连中").sendToTarget();
                        IMCore.this.m_ThreadMessageHandler.obtainMessage(-4).sendToTarget();
                        return;
                    }
                } else if (this.m_IMCore.IsConnecting()) {
                    Log.d("IM", "IM Connection lost. Connecting...");
                } else {
                    this.m_IMCore.m_ThreadMessageHandler.obtainMessage(-3, "和服务器的连接出现异常，自动重连中").sendToTarget();
                    this.m_IMCore.m_ThreadMessageHandler.obtainMessage(-4).sendToTarget();
                    return;
                }
                try {
                    sleep(5000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public interface IMMessageInterface {
        void GetInfo(IMInfo iMInfo);

        void OnIMMessage(int i, String str, Object obj, IMCore iMCore);

        void OnNewInstance(IMCore iMCore);
    }

    private static class IMThreadMessageHandler extends Handler {
        private IMCore m_IMCore;

        public IMThreadMessageHandler(IMCore IMCore) {
            this.m_IMCore = IMCore;
        }

        public void handleMessage(Message msg) {
            if (msg.what == 2) {
                MessageContent Content = msg.obj;
                this.m_IMCore.m_CallBack.OnIMMessage(msg.what, Content.szFrom, Content.szData, this.m_IMCore);
            } else if (msg.what == -3) {
                Toast.makeText(this.m_IMCore.m_Context, (String) msg.obj, 0).show();
            } else if (msg.what == 3) {
                Toast.makeText(this.m_IMCore.m_Context, (String) msg.obj, 0).show();
                this.m_IMCore.m_CallBack.OnIMMessage(msg.what, null, msg.obj, this.m_IMCore);
            } else if (msg.what == -4) {
                Log.d(IMCore.TAG, "Received Reconnect message. Respan a new IMCore thread.");
                if (this.m_IMCore.IsConnected()) {
                    Log.d(IMCore.TAG, "Disconnecting current instance.");
                    this.m_IMCore.Disconnect();
                    Log.d(IMCore.TAG, "Disconnected. Respan a new one.");
                }
                this.m_IMCore.m_CallBack.OnIMMessage(msg.what, null, msg.obj, this.m_IMCore);
            } else {
                this.m_IMCore.m_CallBack.OnIMMessage(msg.what, null, msg.obj, this.m_IMCore);
            }
        }
    }

    private class MessageContent {
        Object szData;
        String szFrom;

        private MessageContent() {
        }
    }

    public boolean IsConnected() {
        return false;
    }

    public boolean IsConnecting() {
        boolean booleanValue;
        synchronized (this.m_bConnecting) {
            booleanValue = this.m_bConnecting.booleanValue();
        }
        return booleanValue;
    }

    protected boolean IsPingReceived() {
        boolean booleanValue;
        synchronized (this.m_bPingReceived) {
            booleanValue = this.m_bPingReceived.booleanValue();
        }
        return booleanValue;
    }

    protected void SetPingReceived(boolean bReceived) {
        synchronized (this.m_bPingReceived) {
            if (bReceived) {
                this.m_bPingReceived = Boolean.TRUE;
            } else {
                this.m_bPingReceived = Boolean.FALSE;
            }
        }
    }

    protected void ResetPingState() {
        this.m_bPingReceived = Boolean.valueOf(false);
    }

    private void SetConnected(boolean bConnected) {
        synchronized (this.m_bConnected) {
            if (bConnected) {
                this.m_bConnected = Boolean.TRUE;
            } else {
                this.m_bConnected = Boolean.FALSE;
            }
        }
    }

    private void SetConnecting(boolean bConnecting) {
        synchronized (this.m_bConnecting) {
            if (bConnecting) {
                this.m_bConnecting = Boolean.TRUE;
            } else {
                this.m_bConnecting = Boolean.FALSE;
            }
        }
    }

    public IMCore(Context context, String szUserName, String szPassword, IMMessageInterface CallBack) {
        this.m_Context = context;
        this.m_szIMLoginUserName = szUserName.toLowerCase(Locale.US);
        this.m_szIMLoginPassword = this.m_szIMLoginUserName.toLowerCase(Locale.US);
        this.m_CallBack = CallBack;
    }

    public IMCore(Context context, String szUserName, String szPassword, IMMessageInterface CallBack, IMInfo IMInfo) {
        this.m_Context = context;
        this.m_szIMLoginUserName = szUserName;
        this.m_szIMLoginPassword = szPassword;
        this.m_CallBack = CallBack;
        this.m_IMInfo = IMInfo;
    }

    public String getCurrentIMUserName() {
        return this.m_szIMLoginUserName + "@" + this.m_IMInfo.szServiceName.toString();
    }

    private boolean DoConnect() {
        this.m_CallBack.GetInfo(this.m_IMInfo);
        if (this.m_IMInfo.szServiceName.equalsIgnoreCase("MYI-WEBSERVICE")) {
            this.m_IMInfo.szServiceName = "webservice.myi.cn";
        }
        if (this.m_IMInfo.szServerForConnect.indexOf(":") != -1) {
            this.m_IMInfo.szServerForConnect = this.m_IMInfo.szServerForConnect.substring(0, this.m_IMInfo.szServerForConnect.indexOf(":"));
        }
        SetConnecting(true);
        SetConnected(false);
        Log.d("IM", "Connecting");
        Log.d("IM", "Connected Logging in...");
        if (Login(this.m_szIMLoginUserName, this.m_szIMLoginPassword)) {
            this.m_ThreadMessageHandler.obtainMessage(1, null).sendToTarget();
        } else if (!RegisterUser(this.m_szIMLoginUserName, this.m_szIMLoginPassword)) {
            this.m_ThreadMessageHandler.obtainMessage(-1, null).sendToTarget();
            SetConnecting(false);
            return false;
        } else if (Login(this.m_szIMLoginUserName, this.m_szIMLoginPassword)) {
            this.m_ThreadMessageHandler.obtainMessage(1, null).sendToTarget();
        } else {
            this.m_ThreadMessageHandler.obtainMessage(-1, null).sendToTarget();
            SetConnecting(false);
            return false;
        }
        if (JoinNotifyRoom()) {
            Log.d("IM", "Connected successfully.");
            this.m_bPingReceived = Boolean.valueOf(true);
            SetConnecting(false);
            SetConnected(true);
            this.m_ThreadMessageHandler.obtainMessage(3, "成功和服务器建立连接").sendToTarget();
            return true;
        }
        Log.d("IM", "Connected successfully. Failed to join Room");
        SetConnected(false);
        SetConnecting(false);
        return false;
    }

    public void Disconnect() {
    }

    public void run() {
        this.m_KeepAliveThread.start();
        DoConnect();
    }

    public Collection<String> getRoter() {
        return null;
    }

    public boolean checkRosterHasUser(String szUserName) {
        return false;
    }

    public void subscribe(String friendID, String friendName) {
    }

    public void subscribe(String friendID, String friendName, String szGroupName) {
    }

    public boolean addRoster(String szUserName, String szDisplayName) {
        return false;
    }

    public boolean addRoster(String szUserName, String szDisplayName, String szGroupName) {
        return false;
    }

    public boolean sendChatMessage(String szMessage, String szUserName, String szDisplayName) {
        return false;
    }

    public static String getChatUserName(String szMessageText) {
        String szResult = "";
        if (szMessageText == null) {
            return szResult;
        }
        int nPos = szMessageText.lastIndexOf("realname=");
        if (nPos != -1) {
            int nEndPos = szMessageText.indexOf(";", nPos);
            if (nEndPos == -1) {
                nEndPos = szMessageText.length();
            }
            szResult = szMessageText.substring(nPos + 9, nEndPos);
        }
        return szResult;
    }

    public static String getChatUserChassName(String szMessageText) {
        String szResult = "";
        if (szMessageText == null) {
            return szResult;
        }
        int nPos = szMessageText.indexOf("userclassname=");
        if (nPos != -1) {
            int nEndPos = szMessageText.indexOf(";", nPos);
            if (nEndPos == -1) {
                nEndPos = szMessageText.length();
            }
            szResult = szMessageText.substring(nPos + 14, nEndPos);
        }
        return szResult;
    }

    public static String trimMessageText(String szMessageText) {
        int nPos = szMessageText.indexOf("fields=");
        if (nPos != -1) {
            return szMessageText.substring(0, nPos);
        }
        return szMessageText;
    }

    public boolean JoinNotifyRoom() {
        return JoinChatRoom("class_" + this.m_IMInfo.szUserClassGUID + "_notifyroom@conference." + this.m_IMInfo.szServiceName, this.m_IMInfo.szUserNameInIM);
    }

    public void SendPresence() {
    }

    public void SendMessage(String szMessage, String szRoomReceptionName) {
    }

    public void SendMessage(String szMessage) {
    }

    public boolean RegisterUser(String szUserName, String szPassword) {
        return false;
    }

    public boolean Login(String szUserName, String szPassword) {
        return false;
    }

    public boolean JoinChatRoom(String szRoomName, String szUserName) {
        return false;
    }
}
