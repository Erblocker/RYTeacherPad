package com.netspace.teacherpad;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.support.v4.internal.view.SupportMenu;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.netspace.library.activity.CommentActivity;
import com.netspace.library.activity.ExecuteCommandActivity;
import com.netspace.library.activity.ExecuteCommandActivity.ExecuteCommandInterface;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.FingerDrawActivity2;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.adapter.SubjectLearnAdapter.SubjectItem;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.components.fragment.ContentDisplayComponentFragment.ContentDisplayExtensionCallBack;
import com.netspace.library.consts.Features;
import com.netspace.library.controls.CustomChatInputView;
import com.netspace.library.controls.CustomDocumentView.OnOpenDocumentListener;
import com.netspace.library.fragment.RESTLibraryFragment;
import com.netspace.library.im.IMCore;
import com.netspace.library.im.IMCore.IMInfo;
import com.netspace.library.im.IMCore.IMMessageInterface;
import com.netspace.library.im.IMService;
import com.netspace.library.interfaces.IMyiApplication;
import com.netspace.library.parser.QuestionParser;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.servers.AudioServer;
import com.netspace.library.servers.MJpegRelayToMulticastThread;
import com.netspace.library.servers.MJpegRelayToMulticastThread.MJpegRelayCallBackInterface;
import com.netspace.library.servers.MJpegServer;
import com.netspace.library.servers.MJpegServerH264;
import com.netspace.library.servers.MP3PlayThread;
import com.netspace.library.servers.MP3PlayThread.MP3PlayThreadCallBackInterface;
import com.netspace.library.servers.MP3RecordThread;
import com.netspace.library.servers.MP3RecordThread.MP3RecordThreadCallBackInterface;
import com.netspace.library.service.ScreenRecorderService;
import com.netspace.library.service.ScreenRecorderService3;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.struct.StudentAnswer;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.ui.StatusBarDisplayer.NotificationPrepareInterface;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.ClassMultiQuestions;
import com.netspace.library.utilities.ClassMultiQuestions.MultiQuestionCallBack;
import com.netspace.library.utilities.HardwareInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.QuestionItemObject;
import com.netspace.library.virtualnetworkobject.RESTEngine;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.library.wrapper.CameraCaptureActivity;
import com.netspace.teacherpad.adapter.MasterControlPagesAdapter;
import com.netspace.teacherpad.config.Config;
import com.netspace.teacherpad.exception.AutoRestartExceptionHandle;
import com.netspace.teacherpad.im.WmIMThread;
import com.netspace.teacherpad.modules.startclass.ReportActivity2;
import com.netspace.teacherpad.structure.MultiScreen;
import com.netspace.teacherpad.theads.ClassOnScreenControlsService;
import com.netspace.teacherpad.theads.StudentsInfoThread;
import com.xsj.crasheye.Crasheye;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TeacherPadApplication extends MyiBaseApplication implements IMMessageInterface, IMyiApplication, MultiQuestionCallBack, MJpegRelayCallBackInterface {
    public static MP3PlayThread AudioPlayThread = null;
    public static AudioServer AudioServer = null;
    public static MJpegServer CameraJpegServer = null;
    public static ClassMultiQuestions ClassMultiQuestions = new ClassMultiQuestions();
    public static WmIMThread IMThread = null;
    public static MasterControlPagesAdapter MasterAdapter = null;
    public static MP3RecordThread RecordServer = null;
    public static ArrayList<String> arrCurrentOnlineStudentIDs = new ArrayList();
    public static final ArrayList<ResourceItemData> arrResourceData = new ArrayList();
    public static final ArrayList<String> arrStudentIDs = new ArrayList();
    public static boolean bAllowCountDownMessage = false;
    public static boolean bAllowQuestionAnswer = false;
    public static boolean bAutoSwitchToInternetClass = false;
    public static final boolean bDebug = false;
    public static boolean bInternetMode = false;
    public static boolean bLockStudentPadAfterAnswer = true;
    public static boolean bOfflinePrompt = false;
    public static boolean bPenButtonSwitch = true;
    public static boolean bSoundAlert = false;
    public static boolean bWhiteBoardOn = false;
    public static int mActiveScreenID = 0;
    public static boolean mAirplayOn = false;
    public static Context mBaseContext = null;
    public static TeacherPadApplication mInstance = null;
    public static MJpegRelayToMulticastThread mMJpegRelayToMulticastThread = null;
    public static HashMap<String, FolderInfo> mMapPersonalFolder = new HashMap();
    public static OnClickListener mOnAirplayClick = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            if (!TeacherPadApplication.mAirplayOn) {
                if (UI.ScreenJpegServer == null) {
                    UI.ScreenJpegServer = MyiBaseApplication.createMJpegServer();
                    UI.ScreenJpegServer.SetSendOnlyDiff(true);
                    UI.ScreenJpegServer.start();
                }
                UI.ScreenJpegServer.CleanImageData();
                TeacherPadApplication.projectToMonitor(new StringBuilder(String.valueOf(Utilities.getWifiIP(TeacherPadApplication.mBaseContext))).append(":8082").toString());
            } else if (UI.ScreenJpegServer != null) {
                UI.ScreenJpegServer.Stop();
                UI.ScreenJpegServer = null;
            }
        }
    };
    public static String mPersonalKPGUID = "";
    public static String mPersonalKPPath = "";
    public static StatusBarDisplayer mRecordStatusBarDisplayer = null;
    public static StudentsInfoThread mStudentsInfoThread = null;
    public static final HashMap<String, Integer> mapAutoSubmitedStudents = new HashMap();
    public static final HashMap<String, String> mapIMCommandsResponse = new HashMap();
    public static final HashMap<String, Integer> mapResourcePlayPos = new HashMap();
    public static final HashMap<String, String> mapResourceTitles = new HashMap();
    public static final HashMap<String, String> mapStatus = new HashMap();
    public static final HashMap<String, Integer> mapStatusUpdateTime = new HashMap();
    public static final HashMap<String, Boolean> mapStudentIDExists = new HashMap();
    public static final HashMap<String, String> mapStudentIP = new HashMap();
    public static final HashMap<String, String> mapStudentName = new HashMap();
    public static final HashMap<String, Boolean> mapStudentQuestionReceived = new HashMap();
    public static final HashMap<String, String> mapStudentWifiSSID = new HashMap();
    public static final HashMap<String, Integer> mapStudentsAnswerTime = new HashMap();
    public static final HashMap<String, String> mapStudentsQuestionAnswers = new HashMap();
    public static ArrayList<MultiScreen> marrMonitors = new ArrayList();
    public static final ArrayList<StudentAnswer> marrRequiredStudentAnswer = new ArrayList();
    public static final ArrayList<StudentAnswer> marrStudentAnswers = new ArrayList();
    public static String mszActiveMonitorLayoutTags = "";
    public static int nOnlineStudentPadCount = -1;
    public static long nQuestionStartTime = 0;
    public static long nQuestionStopTime = 0;
    public static int nRelayServerPort = 0;
    public static int nStudentsCount = 0;
    public static String szActiveAudioStudentID = "";
    public static final String szAppName = "TeacherPad";
    public static String szAppVersionName;
    public static String szCorrectAnswer = "";
    public static String szCurrentClassGUID = "";
    public static String szCurrentClassName = "";
    public static String szCurrentMulticastAddress = "";
    public static String szCurrentPlayingGUID;
    public static String szCurrentQuestionGroupGUID = "";
    public static String szCurrentQuestionIMMessage = "";
    public static String szLastPlayingGUID = "";
    public static String szLastResourceGUID = "";
    public static String szLastStudentAnswerJSON = "";
    public static String szPCIP = "";
    public static String szPCScheduleGUID = "";
    public static String szPCScreenKey = "";
    public static String szPCSessionID = "";
    public static String szPCStatus = "";
    public static String szScheduleGUID;
    public static String szScheduleResourceGUID = "";
    public static String szScheduleResourceTitle = "";
    public static String szWanIP = "";

    /* renamed from: com.netspace.teacherpad.TeacherPadApplication$3 */
    class AnonymousClass3 implements OnClickListener {
        private final /* synthetic */ String val$szAddr;

        AnonymousClass3(String str) {
            this.val$szAddr = str;
        }

        public void onClick(DialogInterface dialog, int which) {
            TeacherPadApplication.IMThread.SendMessage("SwitchToPadScreen " + this.val$szAddr + " " + String.valueOf(which), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            TeacherPadApplication.IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        }
    }

    public static class FolderInfo {
        public String szFolderFullPath = "";
        public String szFolderGUID = "";
    }

    public static String getPCScreenCaptureURL() {
        return "http://" + szPCIP + ":50007/" + szPCScreenKey + ".jpg";
    }

    public static String getMJpegURL() {
        return "http://" + szPCIP + ":50007";
    }

    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new AutoRestartExceptionHandle(Thread.getDefaultUncaughtExceptionHandler()));
        Crasheye.init(this, "002a2a90");
        Crasheye.setLogging(500, "");
        MyiBaseApplication.getCommonVariables().MyiApplication = this;
        szAppVersionName = HardwareInfo.getVersionName(this);
        UI.mSynchronizeIcon = R.drawable.ic_launcher;
        ClassMultiQuestions.setCallBack(this);
        mBaseContext = getBaseContext();
        mInstance = this;
        loadSettings();
        Utilities.logContent("TeacherPadApplication.onCreate");
    }

    public static void loadSettings() {
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(mBaseContext);
        bAutoSwitchToInternetClass = false;
        bSoundAlert = Settings.getBoolean("SoundAlert", false);
        bOfflinePrompt = Settings.getBoolean("OfflinePrompt", false);
        bPenButtonSwitch = Settings.getBoolean("PenButtonSwitch", true);
        bLockStudentPadAfterAnswer = Settings.getBoolean("LockStudentPadAfterAnswer", true);
    }

    public static void connectToIM(IMMessageInterface CallBack) {
        if (IMThread != null) {
            IMThread.Disconnect();
            IMThread = null;
            mapStudentIP.clear();
        }
        if (CallBack == null) {
            CallBack = (IMMessageInterface) mBaseContext.getApplicationContext();
        }
        IMThread = new WmIMThread(mBaseContext, MyiBaseApplication.getCommonVariables().MyiApplication.getClientID(), null, CallBack);
        IMThread.start();
    }

    public static void projectToMonitor() {
        String szAddr = new StringBuilder(String.valueOf(Utilities.getWifiIP(mBaseContext))).append(":8082").toString();
        if (marrMonitors.size() <= 1) {
            IMThread.SendMessage("SwitchToPadScreen " + szAddr + " " + String.valueOf(0), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            return;
        }
        ArrayList<String> arrOptionTexts = new ArrayList();
        String[] arrNames = new String[0];
        Activity activity = UI.getCurrentActivity();
        if (activity != null) {
            for (int i = 0; i < marrMonitors.size(); i++) {
                arrOptionTexts.add("投影到大屏幕" + String.valueOf(i + 1) + "上");
            }
            new Builder(new ContextThemeWrapper(activity, 16974130)).setItems((String[]) arrOptionTexts.toArray(arrNames), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    TeacherPadApplication.IMThread.SendMessage("SwitchToPadScreen " + new StringBuilder(String.valueOf(Utilities.getWifiIP(TeacherPadApplication.mBaseContext))).append(":8082").toString() + " " + String.valueOf(which), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                    TeacherPadApplication.IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                    TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                }
            }).setCancelable(true).setTitle("选择动作").create().show();
        }
    }

    public static void projectToMonitor(String szTargetURL) {
        String szAddr = szTargetURL;
        if (marrMonitors.size() <= 1) {
            IMThread.SendMessage("SwitchToPadScreen " + szAddr + " " + String.valueOf(0), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            return;
        }
        ArrayList<String> arrOptionTexts = new ArrayList();
        String[] arrNames = new String[0];
        Activity activity = UI.getCurrentActivity();
        if (activity != null) {
            for (int i = 0; i < marrMonitors.size(); i++) {
                arrOptionTexts.add("投影到大屏幕" + String.valueOf(i + 1) + "上");
            }
            new Builder(new ContextThemeWrapper(activity, 16974130)).setItems((String[]) arrOptionTexts.toArray(arrNames), new AnonymousClass3(szAddr)).setCancelable(true).setTitle("选择动作").create().show();
        }
    }

    public static void startAudioServer() {
        if (RecordServer == null) {
            String szHost = MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress;
            String szURI = "/" + szScheduleGUID + "/" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + "_teacherpad/Audio";
            if (szHost.indexOf(":") != -1) {
                szHost = szHost.substring(0, szHost.indexOf(":"));
            }
            RecordServer = new MP3RecordThread(mBaseContext, szHost, nRelayServerPort, szURI, new MP3RecordThreadCallBackInterface() {
                public void onRecordStart() {
                }

                public void onRecordError() {
                }

                public void onNewMP3RecordThreadInstance(MP3RecordThread NewThread) {
                    TeacherPadApplication.RecordServer = NewThread;
                }
            });
            RecordServer.setAutoReconnect(true);
            RecordServer.start();
        }
    }

    public static String getStudentRelayBaseURI(String szStudentID) {
        String szHost = MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress;
        if (szHost.indexOf(":") != -1) {
            szHost = szHost.substring(0, szHost.indexOf(":"));
        }
        return "http://" + szHost + ":" + String.valueOf(nRelayServerPort) + "/" + MyiBaseApplication.getCommonVariables().UserInfo.getFirstClassGUID() + "/" + szStudentID + "/";
    }

    public static void switchToStudentAudio(String szStudentID) {
        if (AudioPlayThread != null) {
            AudioPlayThread.stopPlay();
            AudioPlayThread = null;
        }
        if (AudioPlayThread == null) {
            AudioPlayThread = new MP3PlayThread(getStudentRelayBaseURI(szStudentID) + "Audio", new MP3PlayThreadCallBackInterface() {
                public void onPlayStart() {
                }

                public void onPlayError() {
                }

                public boolean onEndOfStream() {
                    return false;
                }

                public void onNewMP3PlayThreadInstance(MP3PlayThread NewThread) {
                    TeacherPadApplication.AudioPlayThread = NewThread;
                }
            });
            AudioPlayThread.start();
            szActiveAudioStudentID = szStudentID;
        }
    }

    public static void stopStudentAudio() {
        if (AudioPlayThread != null) {
            AudioPlayThread.stopPlay();
            AudioPlayThread = null;
        }
        szActiveAudioStudentID = "";
    }

    public void OnIMMessage(int nCode, String szFrom, Object szData, IMCore ThreadObject) {
    }

    public void GetInfo(IMInfo IMInfo) {
    }

    public void OnNewInstance(IMCore NewThreadObject) {
        if (NewThreadObject instanceof WmIMThread) {
            IMThread = (WmIMThread) NewThreadObject;
        }
    }

    public String getClientID() {
        return new StringBuilder(String.valueOf(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)).append("_teacherpad").toString().toLowerCase();
    }

    public String getAppName() {
        return "TeacherPad " + Utilities.getVersionName(MyiBaseApplication.getBaseAppContext());
    }

    public String getAdditionalHardwareInfo() {
        return "AppKey: " + MyiBaseApplication.getBaseAppContext().getString(R.string.app_key);
    }

    public int getRequiredService() {
        return 3;
    }

    public MJpegServer createScreenCaptureServer() {
        if (VERSION.SDK_INT < 21 || (!mCommonVariables.UserInfo.checkPermission(Features.PERMISSION_H264) && !mCommonVariables.UserInfo.checkPermission(Features.PERMISSION_H264_TEACHERPADONLY))) {
            return new MJpegServer(mBaseContext, mCommonVariables.MyiApplication.getMjpegServerPort());
        }
        return new MJpegServerH264(mBaseContext, mCommonVariables.MyiApplication.getMjpegServerPort());
    }

    public int getMjpegServerPort() {
        return Config.TEACHER_SCREEN_JPEG_SERVER_PORT;
    }

    public String[] getBlockedModules() {
        return getResources().getStringArray(R.array.blockedModules);
    }

    public boolean isAppRootRequired() {
        return false;
    }

    public int getMDMFlags() {
        return 0;
    }

    public void startAppMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(335544320);
        startActivity(intent);
    }

    public void startAppLogout() {
        Intent intent = new Intent(this, LoginActivity2.class);
        intent.setFlags(335544320);
        startActivity(intent);
    }

    public void startAppBackgroundService() {
        UI.mOnAirplayButton = mOnAirplayClick;
        UI.mSynchronizeIcon = R.drawable.ic_launcher;
        FingerDrawActivity.setDefaultColor(SupportMenu.CATEGORY_MASK);
        FingerDrawActivity2.setDefaultColor(SupportMenu.CATEGORY_MASK);
        try {
            Crasheye.setUserIdentifier(mCommonVariables.UserInfo.szUserName);
            Crasheye.leaveBreadcrumb("Logged True");
            Crasheye.leaveBreadcrumb("UserName " + mCommonVariables.UserInfo.szUserName);
            Crasheye.leaveBreadcrumb("Password " + mCommonVariables.Session.getPasswordMD5());
            WifiManager wm = (WifiManager) getSystemService("wifi");
            Crasheye.leaveBreadcrumb("WifiMac " + Utilities.getMacAddr());
            Crasheye.leaveBreadcrumb("szClassGUID " + MyiBaseApplication.getCommonVariables().UserInfo.getClassesGUIDs(","));
            Crasheye.leaveBreadcrumb("szClassName " + MyiBaseApplication.getCommonVariables().UserInfo.getClassNames());
            Crasheye.leaveBreadcrumb("szRealName " + MyiBaseApplication.getCommonVariables().UserInfo.szRealName);
            Crasheye.leaveBreadcrumb("szResourceBaseURL " + MyiBaseApplication.getCommonVariables().ServerInfo.szResourceBaseURL);
            Crasheye.leaveBreadcrumb("szSchoolID " + MyiBaseApplication.getCommonVariables().UserInfo.szSchoolGUID);
            Crasheye.leaveBreadcrumb("szSchoolName " + MyiBaseApplication.getCommonVariables().UserInfo.szSchoolName);
            Crasheye.leaveBreadcrumb("szServerAddress " + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress);
            Crasheye.leaveBreadcrumb("szStudentID " + MyiBaseApplication.getCommonVariables().UserInfo.szStudentID);
            Crasheye.leaveBreadcrumb("szUserGUID " + MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
            Crasheye.leaveBreadcrumb("szRealName " + MyiBaseApplication.getCommonVariables().UserInfo.szRealName);
        } catch (IllegalStateException e) {
        }
        connectToIM(null);
        ResourceParser.setFlashOpenListener(new OnOpenDocumentListener() {
            public void OnOpenDocument(String szURL) {
                Intent intent = new Intent(TeacherPadApplication.mBaseContext, WebBrowserActivity.class);
                if (szURL.endsWith(".cache")) {
                    Utilities.copyFile(new File(szURL), new File(new StringBuilder(String.valueOf(szURL)).append(".swf").toString()));
                    szURL = "file://" + szURL + ".swf";
                }
                intent.putExtra(StudentAnswerImageService.LISTURL, szURL);
                intent.putExtra("safeurl", true);
                intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                TeacherPadApplication.mBaseContext.startActivity(intent);
            }
        });
        ResourceDetailActivity.setExtensionCallBack(new ContentDisplayExtensionCallBack() {
            public void onAddToQuestionBook(ResourceItemData item, int nSubject) {
            }

            public void onAddToResourceLibrary(ResourceItemData item) {
                if (item.nType == 0) {
                    QuestionItemObject ResourceObject = new QuestionItemObject(item.szResourceGUID, UI.getCurrentActivity());
                    ResourceObject.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            if (nReturnCode != 0 || ItemObject.readTextData() == null) {
                                Utilities.showAlertMessage(null, "获取资源失败", "无法获得该资源的内容。");
                                return;
                            }
                            QuestionParser Parser = new QuestionParser();
                            FolderInfo folder = (FolderInfo) TeacherPadApplication.mMapPersonalFolder.get("试题库");
                            if (folder == null) {
                                Utilities.showAlertMessage(null, "获取目录失败", "无法获得系统中试题库目录所在的路径。");
                            } else if (Parser.initialize(TeacherPadApplication.mBaseContext, ItemObject.readTextData())) {
                                Parser.setGUID(Utilities.createGUID());
                                Parser.addKnowledgePoints(folder.szFolderFullPath, folder.szFolderGUID);
                                WebServiceCallItemObject WebServiceCallItem = new WebServiceCallItemObject("AddQuestionByXML", null);
                                WebServiceCallItem.setSuccessListener(new OnSuccessListener() {
                                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                        Utilities.showAlertMessage(null, "导入成功", "所选资源已成功加入“我的资源库\\我的平板\\试题库”目录中。");
                                    }
                                });
                                WebServiceCallItem.setFailureListener(new OnFailureListener() {
                                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                        Utilities.showAlertMessage(null, "导入失败", "所选资源无法成功导入，错误信息：" + ItemObject.getErrorText());
                                    }
                                });
                                WebServiceCallItem.setParam("lpszQuestionXML", Parser.getXML());
                                WebServiceCallItem.setAlwaysActiveCallbacks(true);
                                WebServiceCallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                                VirtualNetworkObject.addToQueue(WebServiceCallItem);
                            } else {
                                Utilities.showAlertMessage(null, "资源解析失败", "资源的内容解析失败。");
                            }
                        }
                    });
                    ResourceObject.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            Utilities.showAlertMessage(null, "获取资源失败", "无法获得该试题的内容，错误信息：" + ItemObject.getErrorText());
                        }
                    });
                    VirtualNetworkObject.addToQueue(ResourceObject);
                    return;
                }
                ResourceItemObject ResourceObject2 = new ResourceItemObject(item.szResourceGUID, UI.getCurrentActivity());
                ResourceObject2.setSuccessListener(new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        if (nReturnCode != 0 || ItemObject.readTextData() == null) {
                            Utilities.showAlertMessage(null, "获取资源失败", "无法获得该资源的内容。");
                            return;
                        }
                        ResourceParser Parser = new ResourceParser();
                        FolderInfo folder = (FolderInfo) TeacherPadApplication.mMapPersonalFolder.get("资源库");
                        if (folder == null) {
                            Utilities.showAlertMessage(null, "获取目录失败", "无法获得系统中资源库目录所在的路径。");
                        } else if (Parser.initialize(TeacherPadApplication.mBaseContext, ItemObject.readTextData())) {
                            Parser.setGUID(Utilities.createGUID());
                            Parser.addKnowledgePoints(folder.szFolderFullPath, folder.szFolderGUID);
                            WebServiceCallItemObject WebServiceCallItem = new WebServiceCallItemObject("AddResourceByXML", null);
                            WebServiceCallItem.setSuccessListener(new OnSuccessListener() {
                                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                    Utilities.showAlertMessage(null, "导入成功", "所选资源已成功加入“我的资源库\\我的平板\\资源库”目录中。");
                                }
                            });
                            WebServiceCallItem.setFailureListener(new OnFailureListener() {
                                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                    Utilities.showAlertMessage(null, "导入失败", "所选资源无法成功导入，错误信息：" + ItemObject.getErrorText());
                                }
                            });
                            WebServiceCallItem.setParam("lpszResourceXML", Parser.getXML());
                            WebServiceCallItem.setAlwaysActiveCallbacks(true);
                            WebServiceCallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                            VirtualNetworkObject.addToQueue(WebServiceCallItem);
                        } else {
                            Utilities.showAlertMessage(null, "资源解析失败", "资源的内容解析失败。");
                        }
                    }
                });
                ResourceObject2.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        Utilities.showAlertMessage(null, "获取资源失败", "无法获得该资源的内容，错误信息：" + ItemObject.getErrorText());
                    }
                });
                VirtualNetworkObject.addToQueue(ResourceObject2);
            }
        });
        RESTEngine.getDefault().setAutoSychronizeOnWifiConnect(false);
        RESTEngine.getDefault().getAnswerSheetHelper().setEventSaveToLocalOnly(true);
        RESTEngine.getDefault().getAnswerSheetStudentAnswerHelper().setReadOnly(true);
        String szMessageText = "";
        CustomChatInputView.setAppendString(new StringBuilder(String.valueOf("fields=realname=" + MyiBaseApplication.getCommonVariables().UserInfo.szRealName + ";")).append("userclassname=").append(MyiBaseApplication.getCommonVariables().UserInfo.getFirstClassName()).append(";").toString());
        CommentActivity.setOwnerGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        CommentComponent.setOwnerGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        CommentComponent.setCanDeleteAll(true);
        ChatComponent.setChatUserInfo(this, MyiBaseApplication.getCommonVariables().UserInfo.szRealName, MyiBaseApplication.getCommonVariables().UserInfo.getFirstClassName(), MyiBaseApplication.getCommonVariables().UserInfo.getFirstClassGUID(), null, true, MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        Intent show = new Intent(this, ClassOnScreenControlsService.class);
        show.putExtra("operation", 101);
        stopService(show);
        Utilities.runOnUIThread(this, new Runnable() {
            public void run() {
                TeacherPadApplication.this.getPersonalKP();
            }
        });
        IMService.showChatNotifyBar(this);
    }

    public void stopAppBackgroundService() {
        if (AudioServer != null) {
            AudioServer.StopServer();
            AudioServer = null;
        }
        if (mStudentsInfoThread != null) {
            mStudentsInfoThread.stopThread();
            mStudentsInfoThread = null;
        }
        if (mMJpegRelayToMulticastThread != null) {
            mMJpegRelayToMulticastThread.stopThread();
            mMJpegRelayToMulticastThread = null;
        }
        if (UI.ScreenJpegServer != null) {
            UI.ScreenJpegServer.Stop();
            UI.ScreenJpegServer = null;
        }
        szActiveAudioStudentID = "";
        if (AudioPlayThread != null) {
            AudioPlayThread.stopPlay();
            AudioPlayThread = null;
        }
        if (IMThread != null) {
            IMThread.Disconnect();
            IMThread = null;
            mapStudentIP.clear();
        }
        if (mRecordStatusBarDisplayer != null) {
            mRecordStatusBarDisplayer.hideMessage();
            mRecordStatusBarDisplayer.shutDown();
            mRecordStatusBarDisplayer = null;
            mBaseContext.stopService(new Intent(mBaseContext, ScreenRecorderService.class));
        }
    }

    public void onSwitchToQuestion(String szQuestionGroupGUID, ArrayList<StudentAnswer> arrStudentAnswers) {
        marrStudentAnswers.clear();
        marrStudentAnswers.addAll(arrStudentAnswers);
        mapStudentsAnswerTime.clear();
        mapStudentsQuestionAnswers.clear();
        Iterator it = arrStudentAnswers.iterator();
        while (it.hasNext()) {
            StudentAnswer oneAnswer = (StudentAnswer) it.next();
            mapStudentsQuestionAnswers.put(oneAnswer.szStudentID, oneAnswer.szAnswerOrPictureKey);
            mapStudentsAnswerTime.put(oneAnswer.szStudentID, Integer.valueOf(oneAnswer.nTimeInMS));
        }
        szCurrentQuestionGroupGUID = szQuestionGroupGUID;
        szCorrectAnswer = ClassMultiQuestions.getQuestionCorrectAnswer(szQuestionGroupGUID);
        Activity activity = UI.getCurrentActivity();
        if (activity != null && (activity instanceof ReportActivity2)) {
            ((ReportActivity2) activity).update();
        }
    }

    public void onSuccessUploadStudentAnswerJson(String szQuestionGUID, String szJsonURL) {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("AllowViewOtherAnswer", false)) {
            IMThread.SendIMMessage("StudentAnswersJSON: " + szJsonURL);
        }
    }

    public void onNoMoreQuestionNeedToAnswer() {
    }

    private void displayRecordScreenTools() {
        if (!Utilities.isInArray(getResources().getStringArray(R.array.blockedModules), ScreenRecorderService.class.getName()) && VERSION.SDK_INT >= 18) {
            if (mRecordStatusBarDisplayer == null) {
                mRecordStatusBarDisplayer = new StatusBarDisplayer(this);
                mRecordStatusBarDisplayer.setNotifyID(12);
                mRecordStatusBarDisplayer.setTitle("屏幕录制");
                mRecordStatusBarDisplayer.setText("点击这里开始屏幕录制");
                mRecordStatusBarDisplayer.setIcon(R.drawable.ic_camera);
                mRecordStatusBarDisplayer.setAlwaysHere(true);
                mRecordStatusBarDisplayer.setLayoutID(R.layout.layout_screenrecordernotify);
                mRecordStatusBarDisplayer.setNotifPrepareCallBack(new NotificationPrepareInterface() {
                    public void prepareNotification(Notification notif) {
                        Intent cameraIntent = new Intent(TeacherPadApplication.mBaseContext, CameraCaptureActivity.class);
                        cameraIntent.putExtra("startscreenrecord", true);
                        cameraIntent.putExtra("startfullscreendrawpad", true);
                        notif.contentView.setOnClickPendingIntent(R.id.buttonPictureRecord, PendingIntent.getActivity(TeacherPadApplication.mBaseContext, 0, cameraIntent, 0));
                        cameraIntent = new Intent(TeacherPadApplication.mBaseContext, CameraCaptureActivity.class);
                        cameraIntent.putExtra("startscreenrecord", true);
                        cameraIntent.putExtra("startfullscreendrawpad", true);
                        cameraIntent.putExtra("nocamera", true);
                        notif.contentView.setOnClickPendingIntent(R.id.buttonWhiteBoardRecord, PendingIntent.getActivity(TeacherPadApplication.mBaseContext, 1, cameraIntent, 0));
                        cameraIntent = new Intent(TeacherPadApplication.mBaseContext, CameraCaptureActivity.class);
                        cameraIntent.putExtra("startscreenrecord", true);
                        cameraIntent.putExtra("startfullscreendrawpad", true);
                        cameraIntent.putExtra("nocamera", true);
                        cameraIntent.putExtra("screencapture", true);
                        notif.contentView.setOnClickPendingIntent(R.id.buttonScreenRecord, PendingIntent.getActivity(TeacherPadApplication.mBaseContext, 2, cameraIntent, 0));
                    }
                });
                File Folder = new File(new StringBuilder(String.valueOf(getExternalCacheDir().getAbsolutePath())).append("/upload").toString());
                Folder.mkdir();
                PendingIntent Intent = ScreenRecorderService.getScreenRecordIntent();
                ScreenRecordProcess ScreenRecordProcess = new ScreenRecordProcess(this, Folder.getAbsolutePath());
                ScreenRecordProcess.scanUnUploadFile(Folder.getAbsolutePath());
                if (VERSION.SDK_INT >= 21) {
                    ScreenRecorderService3.setCallBack(ScreenRecordProcess);
                } else {
                    ScreenRecorderService.setCallBack(ScreenRecordProcess);
                }
                mRecordStatusBarDisplayer.setPendingIntent(Intent);
                mRecordStatusBarDisplayer.showAlertBox();
            }
            ExecuteCommandActivity.setCallBack(new ExecuteCommandInterface() {
                public void executeCommand(Intent intent) {
                    if (intent.getAction().equalsIgnoreCase("startscreenrecordupload")) {
                        File Folder = new File(new StringBuilder(String.valueOf(TeacherPadApplication.this.getExternalCacheDir().getAbsolutePath())).append("/upload").toString());
                        Folder.mkdir();
                        new ScreenRecordProcess(TeacherPadApplication.this, Folder.getAbsolutePath()).scanUnUploadFile(Folder.getAbsolutePath());
                    }
                }
            });
        }
    }

    private void getPersonalKP() {
        PrivateDataItemObject ItemObject = new PrivateDataItemObject(szAppName, null);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szXML = ItemObject.readTextData();
                ArrayList<SubjectItem> arrData = new ArrayList();
                try {
                    NodeList arrPadDirectory = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getChildNodes();
                    for (int i = 0; i < arrPadDirectory.getLength(); i++) {
                        Node OneDirectory = arrPadDirectory.item(i);
                        String szKPName = OneDirectory.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                        String szKPPath = OneDirectory.getAttributes().getNamedItem("kpFullPath").getNodeValue();
                        String szKPGUID = OneDirectory.getAttributes().getNamedItem("kpGUID").getNodeValue();
                        if (szKPName.contentEquals("平板录屏")) {
                            TeacherPadApplication.mPersonalKPGUID = szKPGUID;
                            TeacherPadApplication.mPersonalKPPath = szKPPath;
                            TeacherPadApplication.this.displayRecordScreenTools();
                        }
                        FolderInfo info = new FolderInfo();
                        info.szFolderFullPath = szKPPath;
                        info.szFolderGUID = szKPGUID;
                        TeacherPadApplication.mMapPersonalFolder.put(szKPName, info);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        ItemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ItemObject);
    }

    public void OnNewMJpegRelayInstance(MJpegRelayToMulticastThread NewThread) {
        mMJpegRelayToMulticastThread = NewThread;
    }

    public void OnMJpegRelayError(String szMessage) {
        Utilities.showToastMessage("组播中转：" + szMessage, 0);
        Log.e("MJPEGRelay", szMessage);
    }

    public void OnMJpegRelayMessage(String szMessage) {
    }
}
