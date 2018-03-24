package com.netspace.teacherpad.theads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMService;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.teacherpad.MasterControlActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.fragments.SelectPCFragment;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudentsInfoThread extends Thread {
    private Context mContext;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            HttpItemObject itemObject = new HttpItemObject(StudentsInfoThread.this.mszURL, null);
            itemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    if (ItemObject.readTextData() != null) {
                        StudentsInfoThread.this.analysisJsonData(ItemObject.readTextData());
                        Activity activity = UI.getCurrentActivity();
                        if (activity != null && (activity instanceof MasterControlActivity)) {
                            ((MasterControlActivity) activity).updatePadStatusDisplay();
                        }
                    }
                    if (StudentsInfoThread.this.mWorkingThreadHandler != null) {
                        StudentsInfoThread.this.mWorkingThreadHandler.postDelayed(StudentsInfoThread.this.mRunnable, 5000);
                    }
                }
            });
            itemObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    if (StudentsInfoThread.this.mWorkingThreadHandler != null) {
                        StudentsInfoThread.this.mWorkingThreadHandler.postDelayed(StudentsInfoThread.this.mRunnable, 5000);
                    }
                }
            });
            itemObject.setReadOperation(true);
            VirtualNetworkObject.executeNow(itemObject);
        }
    };
    private Handler mWorkingThreadHandler;
    private String mszFilter;
    private String mszURL;

    public StudentsInfoThread(Context context, String szFilter) {
        this.mContext = context;
        this.mszFilter = szFilter;
        this.mszURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/clients.json?filter=" + this.mszFilter;
    }

    private void analysisJsonData(String szJsonData) {
        boolean bNeedIPUpdate = false;
        boolean bPCSessionIDFound = false;
        boolean bPCStatusBad = false;
        JSONObject json = new JSONObject(szJsonData);
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            String szJID = (String) iter.next();
            JSONArray oneStudent = json.getJSONArray(szJID);
            String szUserName = szJID.replace("myipad_", "").replace("teacherpad_", "");
            for (int i = 0; i < oneStudent.length(); i++) {
                JSONObject data = oneStudent.getJSONObject(i);
                String szIP = data.getString("ip");
                String szStatus = data.getString("status");
                String szSessionID = data.getString("sessionid");
                String szSSID = data.getString("ssid");
                boolean bUpdateIP = true;
                if (szSSID == null) {
                    szSSID = "";
                }
                if (!szUserName.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName) || !bPCSessionIDFound) {
                    if (szSessionID.equalsIgnoreCase(TeacherPadApplication.szPCSessionID)) {
                        bPCSessionIDFound = true;
                        if ((TeacherPadApplication.szPCIP == null || TeacherPadApplication.szPCIP.isEmpty()) && !szIP.isEmpty()) {
                            TeacherPadApplication.szPCIP = szIP;
                        }
                    }
                    if (szIP.isEmpty()) {
                        bNeedIPUpdate = true;
                    } else {
                        if (szUserName.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                            bUpdateIP = false;
                            if (szSessionID.equalsIgnoreCase(TeacherPadApplication.szPCSessionID)) {
                                TeacherPadApplication.szPCStatus = szStatus;
                                if (!SelectPCFragment.checkinClassStatus(szStatus, TeacherPadApplication.szScheduleGUID)) {
                                    bPCStatusBad = true;
                                }
                                StartClassControlUnit.processPCStatus();
                            }
                        }
                        if (bUpdateIP) {
                            synchronized (TeacherPadApplication.mapStudentIP) {
                                TeacherPadApplication.mapStudentIP.put(szUserName, szIP);
                                TeacherPadApplication.mapStudentWifiSSID.put(szUserName, szSSID);
                            }
                        }
                        if (!(TeacherPadApplication.szPCSessionID == null || TeacherPadApplication.szPCSessionID.isEmpty())) {
                            String szMessage = Utilities.getNow() + " " + szJID + ": ClientIPResult: " + szIP;
                            IMService.getIMService().sendMessage(szMessage, MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                        }
                    }
                    if (!szStatus.isEmpty()) {
                        synchronized (TeacherPadApplication.mapStatus) {
                            TeacherPadApplication.mapStatus.put(szUserName, szStatus);
                        }
                        try {
                            synchronized (TeacherPadApplication.mapStatusUpdateTime) {
                                TeacherPadApplication.mapStatusUpdateTime.put(szUserName, new Integer((int) System.currentTimeMillis()));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (szJID.indexOf("myipad_") != -1) {
                        TeacherPadApplication.IMThread.SendMessage("status " + szUserName, szJID);
                    }
                }
            }
        }
        if (bNeedIPUpdate) {
            TeacherPadApplication.IMThread.SendIPRequest();
        }
        if (!IMService.isDirectIMClientConnected(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
            if ((!bPCSessionIDFound || bPCStatusBad) && !TeacherPadApplication.szPCSessionID.isEmpty()) {
                TeacherPadApplication.szPCSessionID = "";
                Utilities.runOnUIThread(this.mContext, new Runnable() {
                    public void run() {
                        StartClassControlUnit.showSelectPCDialog();
                    }
                });
            }
        }
    }

    public void stopThread() {
        if (this.mWorkingThreadHandler != null) {
            this.mWorkingThreadHandler.removeCallbacks(null);
            this.mWorkingThreadHandler.post(new Runnable() {
                public void run() {
                    Looper.myLooper().quit();
                }
            });
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.mWorkingThreadHandler = null;
    }

    public void run() {
        super.run();
        setName("StudentsInfoThread Working Thread");
        Looper.prepare();
        this.mWorkingThreadHandler = new Handler();
        this.mWorkingThreadHandler.postDelayed(this.mRunnable, 2000);
        Looper.loop();
    }
}
