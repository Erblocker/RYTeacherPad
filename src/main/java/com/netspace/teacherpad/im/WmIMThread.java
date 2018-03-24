package com.netspace.teacherpad.im;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMCore;
import com.netspace.library.im.IMCore.IMInfo;
import com.netspace.library.im.IMCore.IMMessageInterface;
import com.netspace.library.im.IMCore2;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.TeacherPadApplication;
import com.xsj.crasheye.CrasheyeFileFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class WmIMThread extends IMCore2 {
    private static MessageHandlerForFIFO m_MessageHandlerForFIFO = new MessageHandlerForFIFO();
    private IMMessageInterface m_IMDefaultCallBack;
    private IMMessageProcess m_MessageProcess = new IMMessageProcess(this.m_Context);

    private class SendWorkThread extends Thread {
        private String m_szMessage;

        public SendWorkThread(String szMessage) {
            this.m_szMessage = szMessage;
        }

        public void run() {
            for (int i = 0; i < TeacherPadApplication.arrStudentIDs.size(); i++) {
                String szStudentID = (String) TeacherPadApplication.arrStudentIDs.get(i);
                WmIMThread.this.SendMessage(this.m_szMessage + " " + szStudentID, szStudentID);
                if (i % 5 == 0) {
                    try {
                        sleep(300);
                    } catch (Exception e) {
                    }
                }
            }
            super.run();
        }
    }

    private static class MessageHandlerForFIFO implements IMMessageInterface {
        private Context m_Context;
        private Handler m_Handler;
        private WmIMThread m_WmIMThread;

        private MessageHandlerForFIFO() {
        }

        public void Initialize(Context Context, WmIMThread IMThread, Handler Handler) {
            this.m_Context = Context;
            this.m_WmIMThread = IMThread;
            this.m_Handler = Handler;
        }

        public void OnIMMessage(int nCode, String szFrom, Object szData, IMCore ThreadObject) {
            if (this.m_WmIMThread.m_IMDefaultCallBack != null) {
                this.m_WmIMThread.m_IMDefaultCallBack.OnIMMessage(nCode, szFrom, szData, ThreadObject);
            }
            if (nCode == 3) {
                return;
            }
            if (nCode == -4) {
                WmIMThread WmIMThread = this.m_WmIMThread.Clone();
                this.m_WmIMThread.m_IMDefaultCallBack.OnNewInstance(WmIMThread);
                WmIMThread.start();
            } else if (nCode == 2) {
                this.m_WmIMThread.m_MessageProcess.ProcessIMGroupChatMessage((String) szData, szFrom);
            } else if (nCode == 4) {
                String szMessage = (String) szData;
                int nPos = szMessage.indexOf(": ");
                if (nPos != -1) {
                    szFrom = szMessage.substring(0, nPos);
                    szMessage = szMessage.substring(nPos + 2);
                    this.m_WmIMThread.m_MessageProcess.ProcessIMGroupChatMessage(szMessage, szFrom);
                    if (szMessage.indexOf("HandWrite") == 0) {
                        this.m_WmIMThread.SendIMMessage(szMessage);
                    }
                }
            }
        }

        public void GetInfo(IMInfo IMInfo) {
            IMInfo.szServerForConnect = MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress;
            IMInfo.szAppName = TeacherPadApplication.szAppName;
            IMInfo.szUserName = MyiBaseApplication.getCommonVariables().UserInfo.szUserName;
            IMInfo.szUserNameInIM = IMInfo.szUserName + "_teacherpad";
            if (this.m_WmIMThread.m_IMDefaultCallBack != null) {
                this.m_WmIMThread.m_IMDefaultCallBack.GetInfo(IMInfo);
            }
        }

        public void OnNewInstance(IMCore NewThreadObject) {
            if (this.m_WmIMThread.m_IMDefaultCallBack != null) {
                this.m_WmIMThread.m_IMDefaultCallBack.OnNewInstance(NewThreadObject);
            }
        }
    }

    public WmIMThread(Context context, String szUserName, String szPassword, IMMessageInterface CallBack) {
        super(context, szUserName, szPassword, m_MessageHandlerForFIFO);
        this.m_IMDefaultCallBack = CallBack;
        m_MessageHandlerForFIFO.Initialize(this.m_Context, this, this.m_ThreadMessageHandler);
    }

    public WmIMThread(Context context, String szUserName, String szPassword, IMMessageInterface CallBack, IMInfo IMInfo) {
        super(context, szUserName, szPassword, m_MessageHandlerForFIFO, IMInfo);
        m_MessageHandlerForFIFO.Initialize(this.m_Context, this, this.m_ThreadMessageHandler);
    }

    public void forgetData() {
        this.m_MessageProcess.forgetData();
    }

    public void SendMessage(String szMessage) {
        super.SendMessage(szMessage);
    }

    public void SendIMMessage(String szMessage) {
        super.SendMessage(szMessage);
    }

    public WmIMThread Clone() {
        WmIMThread WmIMThread = new WmIMThread(this.m_Context, this.m_szIMLoginUserName, this.m_szIMLoginPassword, m_MessageHandlerForFIFO, this.m_IMInfo);
        WmIMThread.SetCallBack(this.m_IMDefaultCallBack);
        return WmIMThread;
    }

    public void SetCallBack(IMMessageInterface CallBack) {
        this.m_IMDefaultCallBack = CallBack;
    }

    public void Shutdown() {
    }

    public void SendHandsUp() {
        SendMessage("StudentHandsUp");
    }

    public void SendLockRequest(String szStudentID) {
        if (szStudentID != null) {
            SendMessage("LockScreen " + szStudentID, szStudentID);
        } else {
            SendMessage("LockScreen");
        }
    }

    public void SendAskQuestionRequest(String szStudentID) {
        if (szStudentID != null) {
            SendMessage("Camera " + szStudentID);
        } else {
            SendMessage("Camera reset");
        }
    }

    public void SendUnLockRequest(String szStudentID) {
        if (szStudentID != null) {
            SendMessage("UnLockScreen " + szStudentID);
        } else {
            SendMessage("UnLockScreen");
        }
    }

    public boolean SendProjectRequest(String szStudentID, String szStudentAnswer) {
        if (szStudentAnswer != null && szStudentAnswer.length() > 10) {
            SendMessage("HandWriteNoUpload " + szStudentAnswer + " " + szStudentID, szStudentID);
        }
        String szIP = (String) TeacherPadApplication.mapStudentIP.get(szStudentID);
        if (szIP != null) {
            TeacherPadApplication.projectToMonitor(new StringBuilder(String.valueOf(szIP)).append(":8081").toString());
            return true;
        }
        Utilities.showAlertMessage(UI.getCurrentActivity(), "无法发还", "当前没有找到该学生的IP信息，请在座次表界面多停留几秒钟让程序收集完整所有在线学生的IP信息。");
        return false;
    }

    public void sendMessageToMonitor(final String szIMMessage, boolean bCanCancel) {
        ArrayList<String> arrOptionTexts = new ArrayList();
        String[] arrNames = new String[0];
        Activity activity = UI.getCurrentActivity();
        if (TeacherPadApplication.marrMonitors.size() <= 1) {
            TeacherPadApplication.IMThread.SendMessage(new StringBuilder(String.valueOf(szIMMessage)).append(" ").append(String.valueOf(0)).toString(), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        } else if (activity != null) {
            for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
                arrOptionTexts.add("投影到大屏幕" + String.valueOf(i + 1) + "上");
            }
            new Builder(new ContextThemeWrapper(activity, 16974130)).setItems((String[]) arrOptionTexts.toArray(arrNames), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    TeacherPadApplication.IMThread.SendMessage(szIMMessage + " " + String.valueOf(which), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                }
            }).setCancelable(bCanCancel).setTitle("选择动作").create().show();
        }
    }

    public void SendVoteData(String szData) {
        SendMessage("VResult: " + szData);
    }

    public void SendScreenCaptureRequest(String szStudentID) {
        if (szStudentID == null) {
            SendMessage("ScreenCopy");
        } else {
            SendMessage("ScreenCopy " + szStudentID);
        }
    }

    public void SendStatusRequest(String szStudentID) {
        if (szStudentID == null) {
            SendMessage("status");
        } else {
            SendMessage("status " + szStudentID, szStudentID);
        }
    }

    public void SendScreenCaptureKeyRequest() {
        SendMessage("ScreenCaptureKey");
    }

    public void SendIPRequest() {
        SendMessage("ReportIP");
    }

    public void SendIPRequest(String szTargetID) {
        SendMessage("ReportIP " + szTargetID);
    }

    public void SendToEveryone(String szMessage) {
        SendMessage(szMessage);
    }

    public void SendScreenCaptureGUID(String szGUID) {
        SendMessage("ScreenCaptureResult: " + szGUID);
    }

    public void SendAllSubmit() {
        Activity activity = UI.getCurrentActivity();
        TeacherPadApplication.mapAutoSubmitedStudents.clear();
        if (activity != null) {
            Runnable runnable = new Runnable() {
                public void run() {
                    int i;
                    HashMap<String, Integer> arrSubmitedWifiCount = new HashMap();
                    ArrayList<String> arrUnAnsweredStudents = new ArrayList();
                    ArrayList<String> arrWifiSSID = new ArrayList();
                    for (i = 0; i < TeacherPadApplication.arrStudentIDs.size(); i++) {
                        String szOneStudentID = (String) TeacherPadApplication.arrStudentIDs.get(i);
                        if (!(TeacherPadApplication.mapStudentsAnswerTime.containsKey(szOneStudentID) || TeacherPadApplication.mapAutoSubmitedStudents.containsKey(szOneStudentID) || TeacherPadApplication.mapStudentWifiSSID.get(szOneStudentID) == null)) {
                            arrUnAnsweredStudents.add(szOneStudentID);
                            arrWifiSSID.add((String) TeacherPadApplication.mapStudentWifiSSID.get(szOneStudentID));
                        }
                    }
                    int nNeedSubmitCount = arrUnAnsweredStudents.size();
                    for (i = 0; i < arrUnAnsweredStudents.size(); i++) {
                        String szWifiSSID = (String) arrWifiSSID.get(i);
                        String szStudentID = (String) arrUnAnsweredStudents.get(i);
                        Integer nCount = (Integer) arrSubmitedWifiCount.get(szWifiSSID);
                        if (nCount == null) {
                            nCount = Integer.valueOf(0);
                        }
                        if (nCount.intValue() < 12) {
                            WmIMThread.this.SendMessage("allsubmit", "myipad_" + szStudentID + ";");
                            TeacherPadApplication.mapAutoSubmitedStudents.put(szStudentID, Integer.valueOf(1));
                            arrSubmitedWifiCount.put(szWifiSSID, Integer.valueOf(nCount.intValue() + 1));
                            nNeedSubmitCount--;
                        }
                    }
                    if (nNeedSubmitCount > 0) {
                        Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), this, 1000);
                    } else if (PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext()).getBoolean("AllowViewOtherAnswer", false)) {
                        for (i = 0; i < TeacherPadApplication.ClassMultiQuestions.getSize(); i++) {
                            TeacherPadApplication.IMThread.SendIMMessage("StudentAnswersJSON: " + ("http://" + TeacherPadApplication.szPCIP + ":50007/StudentAnswer_" + TeacherPadApplication.ClassMultiQuestions.getQuestionGUID(i) + CrasheyeFileFilter.POSTFIX));
                        }
                    }
                }
            };
            Utilities.clearRunnable(MyiBaseApplication.getBaseAppContext(), runnable);
            Utilities.runOnUIThreadDelay(MyiBaseApplication.getBaseAppContext(), runnable, 1000);
        }
    }
}
