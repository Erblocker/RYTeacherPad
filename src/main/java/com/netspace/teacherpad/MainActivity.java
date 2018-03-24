package com.netspace.teacherpad;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.netspace.library.activity.SubjectLearnActivity;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.parser.ServerConfigurationParser.Module;
import com.netspace.library.restful.RESTEvent;
import com.netspace.library.restful.RESTRequest;
import com.netspace.library.restful.RESTRequest.RESTRequestCallBack;
import com.netspace.library.restful.RESTService;
import com.netspace.library.restful.RESTServiceProvider;
import com.netspace.library.restful.provider.imagecrop.CropRESTServiceProvider;
import com.netspace.library.restful.provider.screencapture.ScreenCaptureRESTServiceProvider;
import com.netspace.library.threads.CheckNewVersionThread2;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.teacherpad.modules.TeacherModuleBase;
import com.netspace.teacherpad.modules.homework.HomeworkCorrectModule;
import com.netspace.teacherpad.modules.paper.PaperModule;
import com.netspace.teacherpad.modules.startclass.StartClassModule;
import com.netspace.teacherpad.modules.webbrowser.WebBrowserModule;
import com.netspace.teacherpad.theads.ClassOnScreenControlsService;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.http.HttpStatus;

public class MainActivity extends BaseActivity implements OnClickListener {
    private static boolean mInternetStateChecked = false;
    protected Button mFirstButton;
    private FrameLayout mFrameLayout;
    protected Button mLastButton;
    protected TeacherModuleBase mLastModule;
    protected LinearLayout mMenuLayout;
    protected ArrayList<TeacherModuleBase> mModules = new ArrayList();
    protected LinearLayout mRootLayout;
    protected LockableScrollView mScrollView;
    protected StartClassModule mStartClassModule;
    protected TextView mTextViewIP;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MainActivity", "Available Memory size:" + ((ActivityManager) getSystemService("activity")).getLargeMemoryClass() + "MB");
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (!MyiBaseApplication.isConfigured()) {
            MyiBaseApplication.startConfigActivity();
            finish();
        } else if (MyiBaseApplication.isLoggedIn()) {
            MyiBaseApplication.getCommonVariables().UserInfo.szUserName = Settings.getString(UserHonourFragment.USERNAME, "");
            this.mScrollView = (LockableScrollView) findViewById(R.id.scrollView1);
            this.mMenuLayout = (LinearLayout) findViewById(R.id.MenuLayout);
            this.mRootLayout = (LinearLayout) findViewById(R.id.ContentLayout);
            this.mTextViewIP = (TextView) findViewById(R.id.textViewIP);
            this.mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
            String[] arrBlockedModules = getResources().getStringArray(R.array.blockedModules);
            if (!Utilities.isInArray(arrBlockedModules, StartClassModule.class.getName())) {
                this.mStartClassModule = new StartClassModule(this, this.mRootLayout);
                this.mModules.add(this.mStartClassModule);
            }
            if (!Utilities.isInArray(arrBlockedModules, HomeworkCorrectModule.class.getName())) {
                this.mModules.add(new HomeworkCorrectModule(this, this.mRootLayout));
            }
            if (!Utilities.isInArray(arrBlockedModules, PaperModule.class.getName())) {
                this.mModules.add(new PaperModule(this, this.mRootLayout));
            }
            Iterator it = MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.getModules().iterator();
            while (it.hasNext()) {
                Module oneModule = (Module) it.next();
                if (oneModule.bEnable && oneModule.szType.equalsIgnoreCase("teacherpadWebModule")) {
                    WebBrowserModule webBrowserModule = new WebBrowserModule(this, this.mFrameLayout);
                    webBrowserModule.setModuleCategory(oneModule.szGroupName);
                    webBrowserModule.setModuleName(oneModule.szName);
                    webBrowserModule.setParam(oneModule.szParams);
                    this.mModules.add(webBrowserModule);
                }
            }
            ArrayList<TeacherModuleBase> TempModules = new ArrayList();
            String szCategoryName = "";
            String szLastCategoryName = "";
            TempModules.addAll(this.mModules);
            while (TempModules.size() > 0) {
                int i = 0;
                while (i < TempModules.size()) {
                    LayoutParams Params;
                    TeacherModuleBase OneModule = (TeacherModuleBase) TempModules.get(i);
                    szCategoryName = OneModule.getCategoryName();
                    if (szLastCategoryName.isEmpty()) {
                        View textView = new TextView(this);
                        textView.setText(szCategoryName);
                        textView.setTextAppearance(this, 16842817);
                        textView.setTextSize(18.0f);
                        textView.setTypeface(textView.getTypeface(), 1);
                        textView.setTextColor(-8355712);
                        this.mMenuLayout.addView(textView);
                        Params = (LayoutParams) textView.getLayoutParams();
                        Params.leftMargin = Utilities.pixelToDp(15, (Context) this);
                        Params.topMargin = Utilities.pixelToDp(15, (Context) this);
                        textView.setLayoutParams(Params);
                        szLastCategoryName = szCategoryName;
                    }
                    if (szCategoryName.contentEquals(szLastCategoryName)) {
                        Button NewButton = new Button(this);
                        NewButton.setText(OneModule.getModuleName());
                        NewButton.setOnClickListener(this);
                        NewButton.setTag(OneModule);
                        NewButton.setBackgroundResource(R.drawable.button_leftmenu);
                        NewButton.setGravity(16);
                        if (this.mFirstButton == null) {
                            this.mFirstButton = NewButton;
                        }
                        this.mMenuLayout.addView(NewButton);
                        Utilities.sliderFromLeftToRight(NewButton, HttpStatus.SC_MULTIPLE_CHOICES);
                        Params = (LayoutParams) NewButton.getLayoutParams();
                        Params.width = -1;
                        Params.height = -2;
                        if (OneModule.getModuleIcon() != 0) {
                            NewButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(OneModule.getModuleIcon()), null, null, null);
                            NewButton.setCompoundDrawablePadding(Utilities.dpToPixel(5, (Context) this));
                        }
                        NewButton.setLayoutParams(Params);
                        TempModules.remove(i);
                        i--;
                    }
                    i++;
                }
                szLastCategoryName = "";
            }
            Button LogoutButton = (Button) findViewById(R.id.buttonLogout);
            if (LogoutButton != null) {
                LogoutButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent show = new Intent(MainActivity.this, ClassOnScreenControlsService.class);
                        show.putExtra("operation", 100);
                        MainActivity.this.stopService(show);
                        MainActivity.this.finishAllActivity();
                        TeacherPadApplication.mapIMCommandsResponse.clear();
                        TeacherPadApplication.mapStatus.clear();
                        TeacherPadApplication.mapStatusUpdateTime.clear();
                        TeacherPadApplication.mapStudentIP.clear();
                        TeacherPadApplication.mapStudentName.clear();
                        TeacherPadApplication.mapStudentsAnswerTime.clear();
                        TeacherPadApplication.marrStudentAnswers.clear();
                        TeacherPadApplication.mapStudentsQuestionAnswers.clear();
                        TeacherPadApplication.mapResourcePlayPos.clear();
                        TeacherPadApplication.arrResourceData.clear();
                        TeacherPadApplication.arrStudentIDs.clear();
                        TeacherPadApplication.szPCScheduleGUID = "";
                        MyiBaseApplication.getCommonVariables().Session.logOut();
                    }
                });
            }
            Button CheckNewVersionButton = (Button) findViewById(R.id.buttonCheckNewVersion);
            if (CheckNewVersionButton != null) {
                CheckNewVersionButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (MyiBaseApplication.DEBUG) {
                            RESTService.getDefault().execute(ScreenCaptureRESTServiceProvider.class).chainRequest(CropRESTServiceProvider.URI).uniqueRequest("testCapture").start(new RESTRequestCallBack() {
                                public void onRestSuccess(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                                    Utilities.showAlertMessage(MainActivity.this, "success", event.toString());
                                }

                                public void onRestFailure(RESTServiceProvider service, RESTRequest request, RESTEvent event) {
                                    Utilities.showAlertMessage(MainActivity.this, "failure", event.toString());
                                }
                            });
                            return;
                        }
                        String szVersionCheckURL = "http://updates.myi.cn/release/updates/teacherpad.asp";
                        String szBaseAddress = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("BaseAddress", "");
                        boolean bUseOnlyFileName = false;
                        if (!szBaseAddress.isEmpty() && szBaseAddress.indexOf("webservice.myi.cn") == -1) {
                            if (!szBaseAddress.endsWith("/")) {
                                szBaseAddress = new StringBuilder(String.valueOf(szBaseAddress)).append("/").toString();
                            }
                            szVersionCheckURL = new StringBuilder(String.valueOf(szBaseAddress)).append("updates/release/updates/teacherpad.asp").toString();
                            bUseOnlyFileName = true;
                        }
                        new CheckNewVersionThread2(MainActivity.this, MainActivity.this, TeacherPadApplication.szAppVersionName, szVersionCheckURL, bUseOnlyFileName).start();
                    }
                });
            }
            ((Button) findViewById(R.id.buttonSubjectLearn)).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MainActivity.this.startActivity(new Intent(MainActivity.this, SubjectLearnActivity.class));
                }
            });
            Button SetupButton = (Button) findViewById(R.id.buttonSetup);
            if (SetupButton != null) {
                SetupButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        MainActivity.this.startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), 0);
                    }
                });
            }
            ((TextView) findViewById(R.id.textViewVersion)).setText(TeacherPadApplication.szAppVersionName);
            ((TextView) findViewById(R.id.textViewServerAddress)).setText("地址：" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
            this.mTextViewIP.setText("内网IP：" + Utilities.getWifiIP(this));
            if (this.mFirstButton != null) {
                this.mFirstButton.post(new Runnable() {
                    public void run() {
                        MainActivity.this.mFirstButton.performClick();
                    }
                });
            }
        } else {
            startActivity(new Intent(this, LoginActivity2.class));
            finish();
        }
    }

    private void getRelayServerPort() {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("GetRelayServerInfo", this);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    TeacherPadApplication.nRelayServerPort = Integer.valueOf(ItemObject.readTextData()).intValue();
                    if (!TeacherPadApplication.bInternetMode || !TeacherPadApplication.bAutoSwitchToInternetClass) {
                        return;
                    }
                    if (TeacherPadApplication.nRelayServerPort != 0) {
                        new Builder(MainActivity.this).setTitle("公网环境").setMessage("当前检测到您在公网环境下，上课过程将根据需要通过服务器进行数据中转。").setPositiveButton("确定", null).show();
                    } else {
                        new Builder(MainActivity.this).setTitle("警告").setMessage("当前检测到您在公网环境下，但服务器端没有打开中转服务，上课过程将无法正常使用。").setPositiveButton("确定", null).show();
                    }
                }
            }
        });
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public void onBackPressed() {
    }

    public void onClick(View v) {
        Button Button = (Button) v;
        TeacherModuleBase TargetModule = (TeacherModuleBase) Button.getTag();
        Utilities.logClick(Button, Button.getText().toString());
        if (Button != this.mLastButton) {
            if (this.mLastButton != null) {
                this.mLastButton.setSelected(false);
            }
            if (this.mLastModule != null) {
                this.mLastModule.stopModule();
            }
            hideMessage();
            Button.setSelected(true);
            this.mScrollView.setScrollingEnabled(true);
            this.mScrollView.scrollTo(0, 0);
            TargetModule.startModule();
            this.mLastButton = Button;
            this.mLastModule = TargetModule;
        }
    }

    protected void onResume() {
        if (TeacherPadApplication.szWanIP.isEmpty()) {
            this.mTextViewIP.setText("内网IP：" + Utilities.getWifiIP(this));
        } else {
            this.mTextViewIP.setText("内网IP：" + Utilities.getWifiIP(this) + "\n公网IP：" + TeacherPadApplication.szWanIP);
        }
        if (this.mLastModule != null) {
            this.mLastModule.refreshModule();
        }
        super.onResume();
    }

    public void onIMMessage(String szFrom, String szMessage) {
        boolean bFromPC = false;
        if (szFrom.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
            bFromPC = true;
        }
        String[] arrData = szMessage.replace("\r", "").replace("\n", "").split(" ");
        if (arrData[0].replaceAll(":", "").equalsIgnoreCase("StartClass") && bFromPC) {
            if (!this.mStartClassModule.startClass(arrData[1])) {
                Utilities.showAlertMessage(this, "无法进入上课", "无法找到您在睿易通里选择的课程安排，请确认您在睿易通里选择的课程安排确实是今天的一个上课而不是自主学习。");
            }
        }
        super.onIMMessage(szFrom, szMessage);
    }

    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        TeacherPadApplication.loadSettings();
    }
}
