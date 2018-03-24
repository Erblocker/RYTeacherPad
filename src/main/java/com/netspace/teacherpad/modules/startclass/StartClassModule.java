package com.netspace.teacherpad.modules.startclass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.adapter.InlineViewPageAdapter;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.CustomScheduleView;
import com.netspace.library.controls.CustomViewPager;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.receiver.WifiReceiver.WifiConnect;
import com.netspace.library.receiver.WifiReceiver.WifiDisconnect;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.teacherpad.MasterControlActivity;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.adapter.ClassesAdapter;
import com.netspace.teacherpad.fragments.SelectPCFragment;
import com.netspace.teacherpad.fragments.SelectPCFragment.PCData;
import com.netspace.teacherpad.modules.TeacherModuleBase;
import com.netspace.teacherpad.structure.ClassInfo;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;
import org.apache.http.protocol.HTTP;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StartClassModule extends TeacherModuleBase implements OnItemClickListener, IXListViewListener, OnClickListener {
    protected View mContentView;
    private TextView mLabel;
    private XListView mListView;
    private HashMap<String, String> mMapSSIDClassroom;
    private InlineViewPageAdapter mModeSelectAdapter;
    private CustomScheduleView mScheduleView;
    private CustomViewPager mViewPager;
    private ClassesAdapter m_Adapter;
    private ArrayList<ClassInfo> m_arrClasses;

    public StartClassModule(Activity Activity, LinearLayout RootLayout) {
        super(Activity, RootLayout);
        this.m_arrClasses = new ArrayList();
        this.mMapSSIDClassroom = new HashMap();
        this.mModuleName = "上今天的课";
        this.mCategoryName = "上课";
        this.mIconID = R.drawable.ic_classroom_record_light;
    }

    public void startModule() {
        super.startModule();
        if (this.mContentView == null) {
            this.mContentView = this.mLayoutInflater.inflate(R.layout.layout_startclassmodule, null);
            this.mRootLayout.addView(this.mContentView);
            this.mLabel = (TextView) this.mRootLayout.findViewById(R.id.textView2);
            this.mScheduleView = (CustomScheduleView) this.mRootLayout.findViewById(R.id.calendarView1);
            this.mScheduleView.setGravity(1);
            this.mViewPager = (CustomViewPager) this.mRootLayout.findViewById(R.id.customViewPager1);
            this.mModeSelectAdapter = new InlineViewPageAdapter((Context) this.mActivity.get());
            this.mModeSelectAdapter.addPage(this.mRootLayout.findViewById(R.id.listView1), "列表模式");
            this.mModeSelectAdapter.addPage(this.mRootLayout.findViewById(R.id.calendarView1), "课表模式");
            this.mViewPager.setAdapter(this.mModeSelectAdapter);
            this.mRootLayout.findViewById(R.id.buttonListMode).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    StartClassModule.this.mViewPager.setCurrentItem(0);
                }
            });
            this.mRootLayout.findViewById(R.id.buttonSchedule).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    StartClassModule.this.mViewPager.setCurrentItem(1);
                }
            });
            this.mViewPager.setCurrentItem(0);
            ((LockableScrollView) this.mRootLayout.getParent()).setScrollingEnabled(false);
            LayoutParams LayoutParam = (LayoutParams) this.mContentView.getLayoutParams();
            LayoutParam.height = -1;
            this.mContentView.setLayoutParams(LayoutParam);
            this.mListView = (XListView) this.mContentView.findViewById(R.id.listView1);
            this.mListView.setPullLoadEnable(false);
            this.mListView.setPullRefreshEnable(true);
            this.mListView.setXListViewListener(this);
            this.mListView.setOnItemClickListener(this);
            EventBus.getDefault().register(this);
            onRefresh();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiConnect(WifiConnect event) {
        String szSSID = Utilities.getWifiSSID((Context) this.mActivity.get());
        String szClassroomName = "";
        if (this.mMapSSIDClassroom.containsKey(szSSID)) {
            this.mLabel.setText("准备上课 - 当前位于" + ((String) this.mMapSSIDClassroom.get(szSSID)));
            return;
        }
        this.mLabel.setText("准备上课 - 当前位置未知");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWifiDisconnect(WifiDisconnect event) {
        this.mLabel.setText("准备上课 - 当前Wifi断开，无法上课");
    }

    private void displayScheduleData() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int nWeekNumber = c.get(7) - 1;
        if (nWeekNumber == 0) {
            nWeekNumber = 7;
        }
        this.mScheduleView.SetHighlightWeek(nWeekNumber);
        HttpItemObject ItemObject = new HttpItemObject(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/restfuldatasource/schedule/", (Activity) this.mActivity.get());
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    StartClassModule.this.mMapSSIDClassroom.clear();
                    JSONObject jSONObject = new JSONObject(ItemObject.readTextData());
                    JSONArray arrWeekNames = jSONObject.names();
                    for (int i = 0; i < arrWeekNames.length(); i++) {
                        String szOneDay = arrWeekNames.getString(i);
                        JSONObject oneDay = jSONObject.getJSONObject(szOneDay);
                        JSONArray arrOneDaySchedule = oneDay.names();
                        for (int j = 0; j < arrOneDaySchedule.length(); j++) {
                            String szOneLessonIndex = arrOneDaySchedule.getString(j);
                            String szLessonName = "";
                            JSONObject oneLesson = oneDay.getJSONObject(szOneLessonIndex);
                            oneLesson.put("weeknumber", Utilities.toInt(szOneDay));
                            oneLesson.put("lessonindex", Utilities.toInt(szOneLessonIndex));
                            JSONObject classNode = oneLesson.getJSONObject("class");
                            JSONObject classroomNode = oneLesson.getJSONObject("classroom");
                            String szTeacherGUID = oneLesson.getJSONObject("teacher").optString("guid");
                            String szUserClassGUID = classNode.optString("guid");
                            if (szTeacherGUID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID)) {
                                ClassInfo existClassInfo = StartClassModule.this.checkLessonScheduleForToday(szUserClassGUID, Utilities.toInt(szOneLessonIndex));
                                int nSubject = classNode.optInt("subject", -1);
                                String szSubjectName = "全部科目";
                                if (nSubject != -1) {
                                    szSubjectName = Utilities.getSubjectName(nSubject);
                                }
                                if (existClassInfo != null) {
                                    szSubjectName = existClassInfo.szResourceTitle;
                                }
                                String szWifiSSID1 = classroomNode.optString("wifi1ssid");
                                String szWifiSSID2 = classroomNode.optString("wifi2ssid");
                                if (!szWifiSSID1.isEmpty()) {
                                    StartClassModule.this.mMapSSIDClassroom.put(szWifiSSID1, classroomNode.optString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX));
                                }
                                if (!szWifiSSID2.isEmpty()) {
                                    StartClassModule.this.mMapSSIDClassroom.put(szWifiSSID2, classroomNode.optString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX));
                                }
                                szLessonName = classroomNode.optString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX) + "<br>" + classNode.optString("grade") + classNode.optString(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX) + "<br>" + szSubjectName;
                                if (Utilities.toInt(szOneDay) == StartClassModule.this.mScheduleView.getHighlightWeekNumber()) {
                                    TextView textViewScheduleNode = StartClassModule.this.mScheduleView.AddLesson(Utilities.toInt(szOneDay), Utilities.toInt(szOneLessonIndex), szLessonName, oneLesson, StartClassModule.this);
                                    if (existClassInfo == null) {
                                        textViewScheduleNode.setTextColor(-8355712);
                                    }
                                } else {
                                    try {
                                        StartClassModule.this.mScheduleView.AddLesson(Utilities.toInt(szOneDay), Utilities.toInt(szOneLessonIndex), szLessonName, null, StartClassModule.this).setTextColor(-8355712);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    if (Utilities.isNetworkConnected()) {
                        StartClassModule.this.onWifiConnect(null);
                    } else {
                        StartClassModule.this.onWifiDisconnect(null);
                    }
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        ItemObject.setNeedAuthenticate(true);
        ItemObject.setReadOperation(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public void stopModule() {
        super.stopModule();
        EventBus.getDefault().unregister(this);
        if (this.mContentView != null) {
            this.mRootLayout.removeView(this.mContentView);
            this.mContentView = null;
        }
        this.m_arrClasses.clear();
        this.m_Adapter = null;
    }

    public void onItemClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
        startClass((ClassInfo) this.m_arrClasses.get(arg2 - 1));
    }

    private void startClass(ClassInfo classInfo) {
        if (!(TeacherPadApplication.szScheduleGUID == null || TeacherPadApplication.szScheduleGUID.isEmpty() || TeacherPadApplication.szScheduleGUID.equalsIgnoreCase(classInfo.szGUID))) {
            MasterControlActivity.clearLessonsData();
        }
        TeacherPadApplication.szCurrentClassGUID = classInfo.szClassGUID;
        TeacherPadApplication.szCurrentClassName = classInfo.szClassName;
        TeacherPadApplication.szScheduleResourceGUID = classInfo.szResourceGUID;
        TeacherPadApplication.szScheduleGUID = classInfo.szGUID;
        Intent intent = new Intent((Context) this.mActivity.get(), MasterControlActivity.class);
        intent.setFlags(67108864);
        ((Activity) this.mActivity.get()).startActivity(intent);
    }

    public void onRefresh() {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("LessonsScheduleGetTeacherLessonsToday", (Activity) this.mActivity.get());
        ItemObject.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                try {
                    NodeList arrList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(ItemObject.readTextData().getBytes(HTTP.UTF_8))).getDocumentElement().getElementsByTagName("List");
                    StartClassModule.this.m_arrClasses.clear();
                    for (int i = 0; i < arrList.getLength(); i++) {
                        Node OneData = arrList.item(i);
                        ClassInfo OneInfo = new ClassInfo();
                        String szTitle = "";
                        OneInfo.szGUID = OneData.getAttributes().getNamedItem("GUID").getNodeValue();
                        OneInfo.szClassGUID = OneData.getAttributes().getNamedItem("UserClassGUID").getNodeValue();
                        OneInfo.szClassName = OneData.getAttributes().getNamedItem("UserClassName").getNodeValue();
                        OneInfo.szUserSchoolGUID = OneData.getAttributes().getNamedItem("UserSchoolGUID").getNodeValue();
                        OneInfo.szResourceGUID = OneData.getAttributes().getNamedItem("ResourceGUID").getNodeValue();
                        OneInfo.szLessonIndex = OneData.getAttributes().getNamedItem("LessonIndex").getNodeValue();
                        OneInfo.szSubject = OneData.getAttributes().getNamedItem("Subject").getNodeValue();
                        if (OneData.getAttributes().getNamedItem("ResourceTitle") != null) {
                            szTitle = OneData.getAttributes().getNamedItem("ResourceTitle").getNodeValue();
                        }
                        int nLessonIndex = Integer.valueOf(OneInfo.szLessonIndex).intValue();
                        if (nLessonIndex > 4) {
                            OneInfo.szLessonIndex = "下午第" + String.valueOf(nLessonIndex - 4) + "节";
                        } else {
                            OneInfo.szLessonIndex = "上午第" + String.valueOf(nLessonIndex) + "节";
                        }
                        OneInfo.nLessonIndex = nLessonIndex;
                        OneInfo.szResourceTitle = szTitle;
                        OneInfo.szLessonIndex += OneInfo.szSubject + " " + szTitle;
                        if (MyiBaseApplication.getCommonVariables().UserInfo.szSchoolGUID.isEmpty() || OneInfo.szUserSchoolGUID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szSchoolGUID)) {
                            StartClassModule.this.m_arrClasses.add(OneInfo);
                        }
                    }
                    StartClassModule.this.mListView.stopRefresh();
                    StartClassModule.this.mListView.stopLoadMore();
                    StartClassModule.this.mListView.setRefreshTime("刚刚");
                    StartClassModule.this.m_Adapter = new ClassesAdapter((Context) StartClassModule.this.mActivity.get(), StartClassModule.this.m_arrClasses);
                    StartClassModule.this.mListView.setAdapter(StartClassModule.this.m_Adapter);
                    if (StartClassModule.this.m_arrClasses.size() == 0) {
                        ((BaseActivity) StartClassModule.this.mActivity.get()).reportMessage("没有数据", "当前系统中没有发现您今天可以上的课。");
                    } else {
                        ((BaseActivity) StartClassModule.this.mActivity.get()).hideMessage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                StartClassModule.this.mListView.stopRefresh();
                StartClassModule.this.mListView.stopLoadMore();
                StartClassModule.this.mListView.setRefreshTime("刚刚");
            }
        });
        ItemObject.setParam("lpszUserName", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        VirtualNetworkObject.addToQueue(ItemObject);
        displayScheduleData();
    }

    private ClassInfo checkLessonScheduleForToday(String szUserClassGUID, int nLessonIndex) {
        for (int i = 0; i < this.m_arrClasses.size(); i++) {
            ClassInfo oneClass = (ClassInfo) this.m_arrClasses.get(i);
            if (oneClass.szClassGUID.equalsIgnoreCase(szUserClassGUID) && oneClass.nLessonIndex == nLessonIndex) {
                return oneClass;
            }
        }
        return null;
    }

    public void onLoadMore() {
    }

    public void refreshModule() {
        if (this.m_Adapter != null) {
            this.m_Adapter.notifyDataSetChanged();
        }
        displayScheduleData();
    }

    public boolean startClass(String szScheduleGUID) {
        for (int i = 0; i < this.m_arrClasses.size(); i++) {
            ClassInfo OneData = (ClassInfo) this.m_arrClasses.get(i);
            if (OneData.szGUID.equalsIgnoreCase(szScheduleGUID)) {
                startClass(OneData);
                return true;
            }
        }
        return false;
    }

    private void startClass2(final ClassInfo classInfo, String szClassroomGUID) {
        final ProgressDialog progressDialog = new ProgressDialog((Context) this.mActivity.get());
        Runnable checkOnlineUserRunnable = new Runnable() {
            public void run() {
                if (progressDialog.isShowing()) {
                    StartClassModule.this.loadPCData(this, progressDialog, classInfo);
                }
            }
        };
        progressDialog.setMessage("正在等待教室里的睿易通完成账号切换...");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(0);
        progressDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                progressDialog.cancel();
            }
        });
        progressDialog.show();
        TeacherPadApplication.IMThread.SendMessage("LoginAs " + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + " " + MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID, "classroom_" + szClassroomGUID);
        TeacherPadApplication.szScheduleGUID = classInfo.szGUID;
        TeacherPadApplication.szPCSessionID = "classroomsession_" + szClassroomGUID;
        Utilities.runOnUIThreadDelay((Context) this.mActivity.get(), checkOnlineUserRunnable, 1000);
    }

    private void loadPCData(final Runnable runnable, final ProgressDialog progressDialog, final ClassInfo classInfo) {
        HttpItemObject itemObject = new HttpItemObject(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/clients.json?filter=" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ";", (Activity) this.mActivity.get());
        itemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() == null) {
                    return;
                }
                if (!StartClassModule.this.findSession(ItemObject.readTextData(), TeacherPadApplication.szPCSessionID)) {
                    Utilities.showAlertMessage((Context) StartClassModule.this.mActivity.get(), "无法开始上课", "所选的教室PC上没有启动运行在上课模式下的睿易通程序，无法自动切换账号并开始上课。\n\n请先在教室里使用上课模式登录睿易通。");
                    progressDialog.dismiss();
                } else if (StartClassModule.this.analysisJsonData(ItemObject.readTextData())) {
                    progressDialog.dismiss();
                    StartClassModule.this.startClass(classInfo);
                } else {
                    Utilities.runOnUIThreadDelay((Context) StartClassModule.this.mActivity.get(), runnable, 1000);
                }
            }
        });
        itemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.runOnUIThreadDelay((Context) StartClassModule.this.mActivity.get(), runnable, 1000);
            }
        });
        itemObject.setReadOperation(true);
        itemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(itemObject);
    }

    private boolean findSession(String szJsonData, String szTargetSessionID) {
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
                        if (szTargetSessionID.equalsIgnoreCase(szSessionID)) {
                            return true;
                        }
                    }
                    continue;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
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

    private void checkAndStartClass(JSONObject oneLesson, ClassInfo classInfo) {
        try {
            JSONObject classNode = oneLesson.getJSONObject("class");
            JSONObject classroomNode = oneLesson.getJSONObject("classroom");
            JSONObject teacherNode = oneLesson.getJSONObject("teacher");
            String szWifiSSID1 = classroomNode.optString("wifi1ssid");
            String szWifiSSID2 = classroomNode.optString("wifi2ssid");
            String szCurrentWifiSSID = Utilities.getWifiSSID((Context) this.mActivity.get());
            final String szClassroomGUID = classroomNode.optString("guid");
            String szWifiText = "";
            boolean bWifiBad = true;
            if (!(1 == null || szWifiSSID1.isEmpty())) {
                if (szCurrentWifiSSID.equalsIgnoreCase(szWifiSSID1)) {
                    bWifiBad = false;
                } else {
                    szWifiText = new StringBuilder(String.valueOf(szWifiText)).append(szWifiSSID1).toString();
                }
            }
            if (bWifiBad && !szWifiSSID2.isEmpty()) {
                if (szCurrentWifiSSID.equalsIgnoreCase(szWifiSSID2)) {
                    bWifiBad = false;
                } else {
                    if (!szWifiText.isEmpty()) {
                        szWifiText = new StringBuilder(String.valueOf(szWifiText)).append("或").toString();
                    }
                    szWifiText = new StringBuilder(String.valueOf(szWifiText)).append(szWifiSSID1).toString();
                }
            }
            if (bWifiBad) {
                final ClassInfo classInfo2 = classInfo;
                Utilities.showAlertMessage(null, "WIFI错误", new StringBuilder(String.valueOf("当前连接的Wifi不正确，请连接教室的" + szWifiText)).append("，是否继续上课？").toString(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        StartClassModule.this.startClass2(classInfo2, szClassroomGUID);
                    }
                }, null);
                return;
            }
            startClass2(classInfo, szClassroomGUID);
        } catch (JSONException e) {
        }
    }

    public void onClick(View v) {
        if (v.getTag() != null) {
            try {
                JSONObject oneLesson = (JSONObject) v.getTag();
                JSONObject classNode = oneLesson.getJSONObject("class");
                JSONObject classroomNode = oneLesson.getJSONObject("classroom");
                JSONObject teacherNode = oneLesson.getJSONObject("teacher");
                ClassInfo oneClass = checkLessonScheduleForToday(classNode.optString("guid"), oneLesson.getInt("lessonindex"));
                if (oneClass != null) {
                    checkAndStartClass(oneLesson, oneClass);
                    return;
                }
            } catch (JSONException e) {
            }
            Utilities.showAlertMessage(null, "无法开始上课", "当前的课程安排中没有发现这个班级对应的备课。");
            return;
        }
        Utilities.showAlertMessage(null, "不能开始上课", "您所点击的课程不是今天的，不能开始上课。");
    }
}
