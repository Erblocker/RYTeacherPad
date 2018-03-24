package com.netspace.teacherpad;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Const;
import com.netspace.library.consts.Features;
import com.netspace.library.dialog.GiveHonourDialog;
import com.netspace.library.dialog.GiveHonourDialog.GiveHonourDialogCallBack;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.im.IMService;
import com.netspace.library.servers.MP3RecordThread;
import com.netspace.library.service.ScreenRecorderService;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.struct.UserHonourData;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.teacherpad.adapter.MasterControlPagesAdapter;
import com.netspace.teacherpad.adapter.MasterControlPagesAdapter.StudentInfo;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.fragments.SelectPCFragment;
import com.netspace.teacherpad.fragments.SelectPCFragment.PCData;
import com.netspace.teacherpad.theads.ClassOnScreenControlsService;
import com.netspace.teacherpad.theads.StudentsInfoThread;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MasterControlActivity extends BaseActivity {
    private final Runnable GetPlayPosRunable = new Runnable() {
        public void run() {
            if (TeacherPadApplication.IMThread != null) {
                TeacherPadApplication.IMThread.SendMessage("PlayStats", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            }
            MasterControlActivity.this.m_Handler.postDelayed(this, 1000);
        }
    };
    private final Runnable PadStatusUpdateToScreenRunnable = new Runnable() {
        public void run() {
            Log.d("MasterControlActivity", "PadStatusUpdateRunnable.run");
            synchronized (TeacherPadApplication.mapStatus) {
                MasterControlActivity.this.m_Adapter.UpdatePadSatus(TeacherPadApplication.mapStatus);
            }
        }
    };
    private Runnable mEnterScreenRunnable = new Runnable() {
        public void run() {
            if (MasterControlActivity.this.mbReceivePlayStackMessage) {
                if (MasterControlActivity.this.mWaitProgressDialog != null) {
                    if (MasterControlActivity.this.mWaitProgressDialog.isShowing()) {
                        MasterControlActivity.this.mWaitProgressDialog.dismiss();
                    }
                    MasterControlActivity.this.mWaitProgressDialog = null;
                }
                String szHostPCMjpegServer = "http://" + TeacherPadApplication.szPCIP + ":50007";
                Intent intent = new Intent(MasterControlActivity.this, ScreenDisplayActivity.class);
                intent.putExtra("MJpegServer", szHostPCMjpegServer);
                MasterControlActivity.this.startActivity(intent);
                return;
            }
            TeacherPadApplication.IMThread.SendMessage("StartMonitor", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            MasterControlActivity.this.m_Handler.postDelayed(this, 1000);
        }
    };
    private ProgressDialog mWaitProgressDialog;
    private MasterControlPagesAdapter m_Adapter;
    private Handler m_Handler;
    private ArrayList<Integer> marrRandStudentIDs = new ArrayList();
    private boolean mbReceivePlayStackMessage = false;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_master_control);
        getWindow().setFlags(128, 128);
        this.m_Handler = new Handler();
        if (TeacherPadApplication.szCurrentClassGUID.isEmpty()) {
            TeacherPadApplication.mInstance.startAppMainActivity();
            finish();
            return;
        }
        TeacherPadApplication.IMThread.JoinChatRoom(TeacherPadApplication.szCurrentClassGUID, "");
        ImageView Monitor = (ImageView) findViewById(R.id.imageMonitor);
        final OnClickListener MonitorOnClick = new OnClickListener() {
            public void onClick(DialogInterface dialog2, int which) {
                if (TeacherPadApplication.szPCScreenKey.isEmpty()) {
                    Utilities.showAlertMessage(MasterControlActivity.this, "请稍候", "目前程序正在和睿易通同步关键数据，请等到平板状态图标刷新后再点击大屏幕。这个过程大约需要4秒钟。");
                } else if (!TeacherPadApplication.szPCIP.isEmpty()) {
                    TeacherPadApplication.IMThread.SendMessage("StartMonitor", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                    MasterControlActivity.this.mWaitProgressDialog = new ProgressDialog(MasterControlActivity.this);
                    MasterControlActivity.this.mWaitProgressDialog.setMessage("正在等待睿易通进入上课模式，请稍候...");
                    MasterControlActivity.this.mWaitProgressDialog.setCancelable(true);
                    MasterControlActivity.this.mWaitProgressDialog.setIndeterminate(true);
                    MasterControlActivity.this.mWaitProgressDialog.setProgressStyle(0);
                    MasterControlActivity.this.mWaitProgressDialog.setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            MasterControlActivity.this.m_Handler.removeCallbacks(MasterControlActivity.this.mEnterScreenRunnable);
                        }
                    });
                    MasterControlActivity.this.mWaitProgressDialog.show();
                    MasterControlActivity.this.m_Handler.post(MasterControlActivity.this.mEnterScreenRunnable);
                }
            }
        };
        Monitor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TeacherPadApplication.szPCIP.isEmpty()) {
                    new Builder(MasterControlActivity.this).setTitle("没有找到运行睿易通的服务器").setMessage("目前尚未收到运行睿易通程序的机器发来的IP地址，请确定睿易通已启动并在正确的班级上进入了上课模式。\n\n如果程序已启动，请稍等几秒钟再点击。").setPositiveButton("继续", MonitorOnClick).setNegativeButton("取消", null).show();
                    return;
                }
                String szHostPCMjpegServer = TeacherPadApplication.szPCIP;
                String szCurrentIP = Utilities.getWifiIP(MasterControlActivity.this);
                String[] arrCurrentIP = szCurrentIP.split("\\.");
                String[] arrHostIP = szHostPCMjpegServer.split("\\.");
                if (arrCurrentIP.length <= 0 || arrHostIP.length <= 0) {
                    new Builder(MasterControlActivity.this).setTitle("警告").setMessage("发现睿易通程序的机器IP为" + szHostPCMjpegServer + "，而平板的IP为" + szCurrentIP + "，存在异常。").setPositiveButton("确定", null).show();
                } else if (arrCurrentIP.length == arrHostIP.length && arrHostIP[0].equalsIgnoreCase(arrCurrentIP[0])) {
                    MonitorOnClick.onClick(null, 0);
                } else {
                    new Builder(MasterControlActivity.this).setTitle("警告").setMessage("发现睿易通程序的机器IP为" + szHostPCMjpegServer + "，而平板的IP为" + szCurrentIP + "，可能不在一个网段，部分功能可能无法正常工作。").setPositiveButton("继续", MonitorOnClick).setNegativeButton("取消", null).show();
                }
            }
        });
        this.m_Adapter = new MasterControlPagesAdapter();
        TeacherPadApplication.MasterAdapter = this.m_Adapter;
        loadStudentSeats();
        Button LockScreenButton = (Button) findViewById(R.id.buttonLockScreen);
        if (LockScreenButton != null) {
            LockScreenButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TeacherPadApplication.IMThread.SendLockRequest(null);
                    synchronized (TeacherPadApplication.mapStatusUpdateTime) {
                        TeacherPadApplication.mapStatusUpdateTime.clear();
                    }
                }
            });
        }
        Button UnLockScreenButton = (Button) findViewById(R.id.buttonUnLockScreen);
        if (UnLockScreenButton != null) {
            UnLockScreenButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TeacherPadApplication.IMThread.SendUnLockRequest(null);
                    synchronized (TeacherPadApplication.mapStatusUpdateTime) {
                        TeacherPadApplication.mapStatusUpdateTime.clear();
                    }
                }
            });
        }
        Button DisplayToPadButton = (Button) findViewById(R.id.buttonDisplayToPad);
        if (DisplayToPadButton != null) {
            DisplayToPadButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (TeacherPadApplication.mapStudentIP.containsKey(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                        TeacherPadApplication.IMThread.SendMessage("DisplayMJpegServer http://" + ((String) TeacherPadApplication.mapStudentIP.get(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) + ":50007/");
                    }
                }
            });
        }
        ((Button) findViewById(R.id.buttonChart)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MasterControlActivity.this.getRandomStudentName();
            }
        });
        ((Button) findViewById(R.id.buttonHonour)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GiveHonourDialog giveHonourDialog = new GiveHonourDialog();
                FragmentTransaction ft = MasterControlActivity.this.getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                giveHonourDialog.setCancelable(true);
                giveHonourDialog.setCallBack(new GiveHonourDialogCallBack() {
                    public void onGiveHonour(ArrayList<UserHonourData> arrHonourData, ArrayList<String> arrSelectedUserJIDs) {
                        Iterator it = arrHonourData.iterator();
                        while (it.hasNext()) {
                            UserHonourData data = (UserHonourData) it.next();
                            for (int i = 0; i < arrSelectedUserJIDs.size(); i++) {
                                String szJID = (String) arrSelectedUserJIDs.get(i);
                                String szUserName = szJID.replace("myipad_", "");
                                IMService.getIMService().sendMessage(IMService.buildMessage(String.format("GiveHonour %s %s %s %s %s", new Object[]{data.szTitle, data.szDescription, data.szImageDataGUID, MyiBaseApplication.getCommonVariables().UserInfo.szRealName, szUserName}), Utilities.createGUID(), MyiBaseApplication.getCommonVariables().MyiApplication.getClientID(), szJID, 0), szJID);
                            }
                        }
                        Utilities.showAlertMessage(null, "所选奖励已发出", "您所选择的" + String.valueOf(arrHonourData.size()) + "个奖励已成功发送给所选的" + String.valueOf(arrSelectedUserJIDs.size() + "个学生。"));
                    }
                });
                giveHonourDialog.setUserClassGUID(TeacherPadApplication.szCurrentClassGUID);
                giveHonourDialog.show(ft, "giveHonourDialog");
            }
        });
        if (TeacherPadApplication.mStudentsInfoThread == null) {
            TeacherPadApplication.mStudentsInfoThread = new StudentsInfoThread(this, "*_" + TeacherPadApplication.szCurrentClassGUID + "_*;" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ";" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + "_teacherpad;");
            TeacherPadApplication.mStudentsInfoThread.start();
        }
        ScreenRecorderService.setAllowRecord(false);
        TeacherPadApplication.startAudioServer();
        Intent show = new Intent(this, ClassOnScreenControlsService.class);
        show.putExtra("operation", 100);
        startService(show);
    }

    private void loadPCData() {
        HttpItemObject itemObject = new HttpItemObject(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/clients.json?filter=" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ";", this);
        itemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() == null) {
                    return;
                }
                if (MasterControlActivity.this.analysisJsonData(ItemObject.readTextData())) {
                    StartClassControlUnit.launchPC();
                } else {
                    StartClassControlUnit.showSelectPCDialog();
                }
            }
        });
        itemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Builder builder = new Builder(MasterControlActivity.this);
                ItemObject.buildErrorDialog(builder);
                builder.setNegativeButton("确定", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MasterControlActivity.this.shutdownClass();
                    }
                }).show();
            }
        });
        itemObject.setReadOperation(true);
        itemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(itemObject);
    }

    private boolean analysisJsonData(String szJsonData) {
        if (szJsonData.startsWith("null")) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(szJsonData);
            ArrayList<PCData> arrNewData = new ArrayList();
            Iterator<String> iter = json.keys();
            while (iter.hasNext()) {
                String szJID = (String) iter.next();
                JSONArray oneStudent = json.getJSONArray(szJID);
                if (szJID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                    for (int i = 0; i < oneStudent.length(); i++) {
                        JSONObject data = oneStudent.getJSONObject(i);
                        String szIP = data.getString("ip");
                        String szStatus = data.getString("status");
                        String szVersion = data.getString("client");
                        String szSessionID = data.getString("sessionid");
                        PCData oneData = new PCData();
                        oneData.szIP = szIP;
                        oneData.szSessionID = szSessionID;
                        oneData.szVersion = szVersion;
                        oneData.szStatus = szStatus;
                        if (TeacherPadApplication.szPCSessionID.isEmpty()) {
                            arrNewData.add(oneData);
                        } else if (TeacherPadApplication.szPCSessionID.equalsIgnoreCase(szSessionID)) {
                            arrNewData.add(oneData);
                        }
                    }
                }
            }
            if (arrNewData.size() != 1 || !SelectPCFragment.checkStatus(((PCData) arrNewData.get(0)).szStatus, TeacherPadApplication.szScheduleGUID)) {
                return false;
            }
            TeacherPadApplication.szPCIP = ((PCData) arrNewData.get(0)).szIP;
            TeacherPadApplication.szPCSessionID = ((PCData) arrNewData.get(0)).szSessionID;
            TeacherPadApplication.szPCStatus = ((PCData) arrNewData.get(0)).szStatus;
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onIMMessage(String szFrom, String szMessage) {
        boolean bFromPC = false;
        if (szFrom.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
            bFromPC = true;
        }
        if (szMessage.indexOf("GetScreenPlayStackResult") != -1 && bFromPC) {
            String[] arrData = szMessage.split(" ");
            String szVerb = arrData[0].replaceAll(":", "");
            String[] arrScreenData = arrData[1].split(";", -1);
            for (String split : arrScreenData) {
                if (split.split(",").length >= 2) {
                    this.mbReceivePlayStackMessage = true;
                    return;
                }
            }
        }
    }

    private void loadMulticastAddress() {
        if (!MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MULTICAST_PCSCREEN_BROADCAST)) {
            loadPCData();
        } else if (TeacherPadApplication.szCurrentMulticastAddress.isEmpty()) {
            WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("BroadcastGetMulticastAddress", this);
            ItemObject.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    if (!(ItemObject.readTextData() == null || ItemObject.readTextData().isEmpty())) {
                        TeacherPadApplication.szCurrentMulticastAddress = ItemObject.readTextData();
                    }
                    MasterControlActivity.this.loadPCData();
                }
            });
            ItemObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    MasterControlActivity.this.loadPCData();
                }
            });
            VirtualNetworkObject.addToQueue(ItemObject);
        }
    }

    private void getRandomStudentName() {
        int nStudentID = -1;
        if (this.marrRandStudentIDs.size() >= TeacherPadApplication.arrStudentIDs.size()) {
            this.marrRandStudentIDs.clear();
        }
        while (true) {
            if (nStudentID == -1 || Utilities.isInArray(this.marrRandStudentIDs, nStudentID)) {
                nStudentID = ThreadLocalRandom.current().nextInt(0, TeacherPadApplication.arrStudentIDs.size());
            } else {
                this.marrRandStudentIDs.add(Integer.valueOf(nStudentID));
                new Builder(this).setTitle("抽查学生").setMessage("抽查到下列学生：\n\n\t" + ((String) TeacherPadApplication.mapStudentName.get((String) TeacherPadApplication.arrStudentIDs.get(nStudentID))) + "\n\n").setPositiveButton("关闭", null).setNeutralButton("抽查下一个学生", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MasterControlActivity.this.getRandomStudentName();
                    }
                }).show();
                return;
            }
        }
    }

    public static void clearLessonsData() {
        TeacherPadApplication.szCurrentQuestionIMMessage = "";
        TeacherPadApplication.szLastStudentAnswerJSON = "";
        IMService.setMessageWaitTimeout(Const.IM_DEFAULT_TIMEOUT);
        IMService.shutdownDirectIMClients();
        TeacherPadApplication.mapStatus.clear();
        TeacherPadApplication.mapStatusUpdateTime.clear();
        TeacherPadApplication.mapIMCommandsResponse.clear();
        TeacherPadApplication.mapStudentName.clear();
        TeacherPadApplication.mapStudentsQuestionAnswers.clear();
        TeacherPadApplication.mapStudentsAnswerTime.clear();
        TeacherPadApplication.marrStudentAnswers.clear();
        TeacherPadApplication.mapResourcePlayPos.clear();
        TeacherPadApplication.nStudentsCount = 0;
        TeacherPadApplication.arrResourceData.clear();
        TeacherPadApplication.arrStudentIDs.clear();
        TeacherPadApplication.szPCScheduleGUID = "";
        TeacherPadApplication.szCurrentClassGUID = "";
        TeacherPadApplication.szScheduleGUID = "";
        TeacherPadApplication.nOnlineStudentPadCount = -1;
        TeacherPadApplication.arrCurrentOnlineStudentIDs.clear();
        TeacherPadApplication.mapStudentIDExists.clear();
        TeacherPadApplication.mapStudentQuestionReceived.clear();
        TeacherPadApplication.marrMonitors.clear();
        TeacherPadApplication.szCurrentMulticastAddress = "";
        TeacherPadApplication.ClassMultiQuestions.clear();
        ScreenRecorderService.setAllowRecord(true);
        if (TeacherPadApplication.mStudentsInfoThread != null) {
            TeacherPadApplication.mStudentsInfoThread.stopThread();
            TeacherPadApplication.mStudentsInfoThread = null;
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onPause() {
        this.m_Handler.removeCallbacks(this.PadStatusUpdateToScreenRunnable);
        this.m_Handler.removeCallbacks(this.GetPlayPosRunable);
        super.onPause();
    }

    protected void onResume() {
        TeacherPadApplication.szPCScheduleGUID = "";
        this.m_Handler.postDelayed(this.GetPlayPosRunable, 1000);
        super.onResume();
    }

    public void updatePadStatusDisplay() {
        this.m_Handler.post(this.PadStatusUpdateToScreenRunnable);
    }

    private void HandleResourceXML(String szXMLContent) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXMLContent.getBytes(HTTP.UTF_8)));
            doc.getDocumentElement();
            NodeList NodeList = doc.getElementsByTagName("Resource");
            NodeList rootNode = doc.getElementsByTagName("LessonsPrepare");
            if (rootNode.getLength() > 0) {
                TeacherPadApplication.szScheduleResourceTitle = rootNode.item(0).getAttributes().getNamedItem("title").getTextContent();
                if (TeacherPadApplication.szScheduleResourceTitle == null) {
                    TeacherPadApplication.szScheduleResourceTitle = "";
                }
            }
            TeacherPadApplication.arrResourceData.clear();
            for (int i = 0; i < NodeList.getLength(); i++) {
                Node OneResource = NodeList.item(i);
                ResourceItemData NewItem = new ResourceItemData();
                NewItem.szGUID = OneResource.getAttributes().getNamedItem("guid").getTextContent();
                NewItem.szTitle = OneResource.getAttributes().getNamedItem("title").getTextContent();
                NewItem.szResourceType = OneResource.getAttributes().getNamedItem("resourceType").getTextContent();
                NewItem.nUsageType = Integer.valueOf(OneResource.getAttributes().getNamedItem("usageType").getTextContent()).intValue();
                NewItem.nType = Integer.valueOf(OneResource.getAttributes().getNamedItem("type").getTextContent()).intValue();
                if (NewItem.nUsageType == 1) {
                    TeacherPadApplication.arrResourceData.add(NewItem);
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e2) {
            e2.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
    }

    public static void stopClass() {
        if (TeacherPadApplication.mStudentsInfoThread != null) {
            TeacherPadApplication.mStudentsInfoThread.stopThread();
            TeacherPadApplication.mStudentsInfoThread = null;
        }
        if (TeacherPadApplication.IMThread != null) {
            TeacherPadApplication.IMThread.SendMessage("UnLockScreen");
            TeacherPadApplication.IMThread.SendMessage("EndClass");
            TeacherPadApplication.IMThread.SendMessage("StopMJpegServer");
            TeacherPadApplication.IMThread.SendMessage("StopVoice");
        }
        if (TeacherPadApplication.mMJpegRelayToMulticastThread != null) {
            TeacherPadApplication.mMJpegRelayToMulticastThread.stopThread();
            TeacherPadApplication.mMJpegRelayToMulticastThread = null;
        }
        Intent show = new Intent(MyiBaseApplication.getBaseAppContext(), ClassOnScreenControlsService.class);
        show.putExtra("operation", 101);
        MyiBaseApplication.getBaseAppContext().stopService(show);
        if (TeacherPadApplication.RecordServer != null) {
            TeacherPadApplication.RecordServer.stopRecord();
            TeacherPadApplication.RecordServer = null;
        }
        TeacherPadApplication.szScheduleGUID = "";
        TeacherPadApplication.szPCSessionID = "";
        MP3RecordThread.setMute(false);
        TeacherPadApplication.stopStudentAudio();
        clearLessonsData();
    }

    private void shutdownClass() {
        stopClass();
        finish();
    }

    public void onBackPressed() {
        new Builder(this).setTitle("结束上课").setCancelable(true).setMessage("确实结束上课吗？如果您选择了“结束上课并解锁平板”，那么所有的学生平板都将被解锁。如果您选择了“仅退出上课界面”，那么学生平板将维持当前状态。").setPositiveButton("结束上课并解锁平板", new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MasterControlActivity.this.shutdownClass();
            }
        }).setNeutralButton("仅退出上课界面", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MasterControlActivity.this.finish();
            }
        }).setNegativeButton("取消", null).show();
    }

    private void loadMainResource(String szResourceGUID) {
        ResourceItemObject resourceItem = new ResourceItemObject(szResourceGUID, this);
        resourceItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                MasterControlActivity.this.HandleResourceXML(ItemObject.readTextData());
                if (TeacherPadApplication.arrResourceData.size() == 0) {
                    new Builder(MasterControlActivity.this).setTitle("备课资源不正确").setCancelable(false).setMessage("这堂课所使用的备课资源中，没有一个资源被标记为在课上使用，是否继续上课？").setNegativeButton("否", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MasterControlActivity.this.shutdownClass();
                        }
                    }).setPositiveButton("是", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            MasterControlActivity.this.loadMulticastAddress();
                        }
                    }).show();
                } else {
                    MasterControlActivity.this.loadMulticastAddress();
                }
            }
        });
        resourceItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Builder builder = new Builder(MasterControlActivity.this);
                ItemObject.buildErrorDialog(builder);
                builder.setNegativeButton("确定", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MasterControlActivity.this.shutdownClass();
                    }
                }).show();
            }
        });
        VirtualNetworkObject.addToQueue(resourceItem);
    }

    private void loadStudentSeats() {
        PrivateDataItemObject ItemObject = new PrivateDataItemObject("StudentSeats_" + TeacherPadApplication.szCurrentClassGUID, this);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                int i;
                TeacherPadApplication.mapStudentName.clear();
                ArrayList<StudentInfo> arrStudents = new ArrayList();
                float fScale = Utilities.getDisplayScale(MasterControlActivity.this);
                try {
                    String szXML = ItemObject.readTextData();
                    NodeList arrSeats = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getElementsByTagName("Seat");
                    for (i = 0; i < arrSeats.getLength(); i++) {
                        Node OneSeat = arrSeats.item(i);
                        Rect rect = new Rect();
                        rect.left = Integer.valueOf(OneSeat.getAttributes().getNamedItem("left").getNodeValue()).intValue();
                        rect.right = Integer.valueOf(OneSeat.getAttributes().getNamedItem("right").getNodeValue()).intValue();
                        rect.top = Integer.valueOf(OneSeat.getAttributes().getNamedItem("top").getNodeValue()).intValue();
                        rect.bottom = Integer.valueOf(OneSeat.getAttributes().getNamedItem("bottom").getNodeValue()).intValue();
                        rect.left = (int) (((double) rect.left) * 1.2d);
                        rect.top = (int) (((double) rect.top) * 1.3d);
                        rect.left = (int) (((float) rect.left) * fScale);
                        rect.top = (int) (((float) rect.top) * fScale);
                        rect.left -= 100;
                        String szStudentID = OneSeat.getAttributes().getNamedItem("studentId").getNodeValue();
                        String szStudentName = OneSeat.getAttributes().getNamedItem("studentName").getNodeValue();
                        if (!szStudentID.isEmpty()) {
                            TeacherPadApplication.mapStudentName.put(szStudentID, szStudentName);
                            TeacherPadApplication.arrStudentIDs.add(szStudentID);
                            arrStudents.add(MasterControlActivity.this.m_Adapter.NewStudentInfo(Integer.valueOf(OneSeat.getAttributes().getNamedItem("col").getNodeValue()).intValue(), Integer.valueOf(OneSeat.getAttributes().getNamedItem("row").getNodeValue()).intValue(), szStudentID, szStudentName, rect));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ArrayList<Integer> arrPages = new ArrayList();
                arrPages.add(Integer.valueOf(R.layout.page_studentseats));
                MasterControlActivity.this.m_Adapter.Init(MasterControlActivity.this, arrPages, arrStudents);
                ViewPager m_myPager = (ViewPager) MasterControlActivity.this.findViewById(R.id.pageSelector);
                m_myPager.setAdapter(MasterControlActivity.this.m_Adapter);
                m_myPager.setCurrentItem(0);
                ArrayList<UserInfo> arrAllStudents = MyiBaseApplication.getCommonVariables().UserInfo.getClassStudents(TeacherPadApplication.szCurrentClassGUID);
                if (arrAllStudents != null) {
                    TeacherPadApplication.mapStudentName.clear();
                    TeacherPadApplication.arrStudentIDs.clear();
                    for (i = 0; i < arrAllStudents.size(); i++) {
                        UserInfo oneStudent = (UserInfo) arrAllStudents.get(i);
                        TeacherPadApplication.mapStudentName.put(oneStudent.szUserName, oneStudent.szRealName);
                        TeacherPadApplication.arrStudentIDs.add(oneStudent.szUserName);
                    }
                }
                TeacherPadApplication.nStudentsCount = arrStudents.size();
                if (TeacherPadApplication.nStudentsCount == 0) {
                    new Builder(MasterControlActivity.this).setTitle("座次分配不正确").setCancelable(false).setMessage("您分配的座次中尚未给至少一个座位指定学生。").setPositiveButton("确定", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MasterControlActivity.this.shutdownClass();
                        }
                    }).show();
                    return;
                }
                MasterControlActivity.this.loadMainResource(TeacherPadApplication.szScheduleResourceGUID);
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                if (nReturnCode == ErrorCode.ERROR_NO_DATA) {
                    new Builder(MasterControlActivity.this).setTitle("座次尚未分配").setCancelable(true).setMessage("您尚未针对所选择的班级分配好座次，请在睿易通的备课中先进行好座次分配。").setPositiveButton("确定", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MasterControlActivity.this.shutdownClass();
                        }
                    }).show();
                    return;
                }
                Builder builder = new Builder(MasterControlActivity.this);
                ItemObject.buildErrorDialog(builder);
                builder.setNegativeButton("确定", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MasterControlActivity.this.shutdownClass();
                    }
                }).show();
            }
        });
        ItemObject.setReadOperation(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public void playSound() {
        final MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.jump);
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mPlayer.release();
            }
        });
        mPlayer.start();
    }
}
