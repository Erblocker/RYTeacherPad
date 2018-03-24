package com.netspace.library.im;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMCore.IMInfo;
import com.netspace.library.im.IMCore.IMMessageInterface;
import com.netspace.library.im.IMService.OnIMServiceArrivedListener;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class IMCore2 extends IMCore implements OnIMServiceArrivedListener {
    private String mClassRoom;
    private String mTeacherUserName = "";

    public IMCore2(Context context, String szUserName, String szPassword, IMMessageInterface CallBack, IMInfo IMInfo) {
        super(context, szUserName, szPassword, CallBack, IMInfo);
        IMService.getIMService().registerCallBack(this);
    }

    public IMCore2(Context context, String szUserName, String szPassword, IMMessageInterface CallBack) {
        super(context, szUserName, szPassword, CallBack);
        IMService.getIMService().registerCallBack(this);
    }

    public boolean IsConnected() {
        return IMService.getIMService().isConnected();
    }

    public boolean IsConnecting() {
        if (IMService.getIMService() == null) {
            return true;
        }
        return false;
    }

    public void run() {
    }

    protected boolean IsPingReceived() {
        return true;
    }

    protected void SetPingReceived(boolean bReceived) {
    }

    protected void ResetPingState() {
    }

    protected void SendPing() {
    }

    public String getCurrentIMUserName() {
        return super.getCurrentIMUserName();
    }

    public void Disconnect() {
    }

    public boolean checkRosterHasUser(String szUserName) {
        return super.checkRosterHasUser(szUserName);
    }

    public void subscribe(String friendID, String friendName) {
        super.subscribe(friendID, friendName);
    }

    public void subscribe(String friendID, String friendName, String szGroupName) {
        super.subscribe(friendID, friendName, szGroupName);
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

    public boolean JoinNotifyRoom() {
        return true;
    }

    public void SendPresence() {
    }

    public void SendMessage(String szMessage, String szRoomReceptionName) {
        if (!szMessage.startsWith("{")) {
            szMessage = Utilities.getNow() + " " + this.m_szIMLoginUserName + ": " + szMessage;
        }
        if (!(szMessage.indexOf("\r") == -1 && szMessage.indexOf("\n") == -1)) {
            szMessage = szMessage.replace("\r", "\\r").replace("\n", "\\n");
        }
        this.m_szIMLoginUserName.indexOf(szRoomReceptionName);
        if (szRoomReceptionName.indexOf("*") == -1 && szRoomReceptionName.indexOf(";") == -1 && !szRoomReceptionName.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
            szRoomReceptionName = "*" + szRoomReceptionName + "*";
        }
        IMService.getIMService().sendMessage(szMessage, szRoomReceptionName);
    }

    public void SendMessage(String szMessage) {
        String szSendTo = "*_" + this.mClassRoom + "_*;" + this.m_szIMLoginUserName + ";";
        if (this.m_szIMLoginUserName.indexOf("_teacherpad") != -1) {
            szSendTo = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szSendTo)).append(this.m_szIMLoginUserName.substring(0, this.m_szIMLoginUserName.indexOf("_teacherpad"))).toString())).append(";").toString();
        }
        if (!this.mTeacherUserName.isEmpty()) {
            szSendTo = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szSendTo)).append(this.mTeacherUserName).append(";").toString())).append(this.mTeacherUserName).append("_teacherpad;").toString();
        }
        SendMessage(szMessage, szSendTo);
    }

    public boolean RegisterUser(String szUserName, String szPassword) {
        return true;
    }

    public boolean Login(String szUserName, String szPassword) {
        return true;
    }

    public boolean JoinChatRoom(String szRoomName, String szUserName) {
        if (szRoomName == null || szRoomName.isEmpty()) {
            throw new InvalidParameterException("szRoomName can not be null or empty. Or all data will be broadcast to everyone");
        }
        this.mClassRoom = szRoomName;
        this.mClassRoom = this.mClassRoom.replace("class_", "");
        this.mClassRoom = this.mClassRoom.replace("_nodifyroom", "");
        return true;
    }

    public String getTeacherUserName() {
        return this.mTeacherUserName;
    }

    public void OnMessageArrived(String szMessage) {
        if (szMessage.length() >= 20) {
            String szFrom = "";
            String szGUID = null;
            if (szMessage.startsWith("{")) {
                szMessage = szMessage.replaceAll("\\n", "\n");
                try {
                    JSONObject JSON = new JSONObject(szMessage);
                    szGUID = JSON.getString("guid");
                    szFrom = JSON.getString("from");
                    szMessage = JSON.getString("content");
                    if (szMessage.startsWith("CHAT")) {
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                String szMessageTime = szMessage.substring(0, 19);
                szMessage = szMessage.substring(20);
                int nFindPos = szMessage.indexOf(": ");
                if (nFindPos != -1) {
                    szFrom = szMessage.substring(0, nFindPos);
                    szMessage = szMessage.substring(nFindPos + 2);
                }
            }
            if (!(MyiBaseApplication.getCommonVariables().UserInfo.nUserType != 0 || szFrom.equalsIgnoreCase(this.m_szIMLoginUserName) || szFrom.indexOf("_teacherpad") == -1)) {
                this.mTeacherUserName = szFrom.substring(0, szFrom.indexOf("_teacherpad"));
            }
            final String szFinalFrom = szFrom;
            final String szFinalMessage = szMessage;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    IMCore2.this.m_CallBack.OnIMMessage(2, szFinalFrom, szFinalMessage, IMCore2.this);
                }
            });
            if (szGUID != null) {
                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("IMSetMessageReceived", null);
                CallItem.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    }
                });
                ArrayList<String> arrParam = new ArrayList();
                arrParam.add(szGUID);
                CallItem.setParam("arrMessageGUIDs", arrParam);
                CallItem.setParam("lpszIP", "");
                CallItem.setParam("bDeleteFromDB", Integer.valueOf(1));
                CallItem.setAlwaysActiveCallbacks(true);
                VirtualNetworkObject.addToQueue(CallItem);
            }
        }
    }
}
