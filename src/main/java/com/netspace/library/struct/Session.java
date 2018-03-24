package com.netspace.library.struct;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.google.gson.annotations.Expose;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Features;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.parser.ServerConfigurationParser.PluginModule;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import com.netspace.library.utilities.HardwareInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import io.vov.vitamio.utils.Log;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Session {
    protected static final String TAG = "Session";
    private SessionCallBack mCallBack;
    @Expose
    private int mLimitUserType = -1;
    @Expose
    private String mPassword;
    @Expose
    private String mPasswordMD5;
    @Expose
    private String mSession;
    @Expose
    private String mUserName;
    @Expose
    private boolean mbLoggedIn = false;
    @Expose
    private boolean mbOfflineMode = false;

    public interface SessionCallBack {
        void onLoginFailure(int i, String str);

        void onLoginSuccess(String str);
    }

    public void setOfflineMode(boolean bOfflineMode) {
        this.mbOfflineMode = bOfflineMode;
    }

    public boolean getOfflineMode() {
        return this.mbOfflineMode;
    }

    public void setLimitUserType(int nUserType) {
        this.mLimitUserType = nUserType;
    }

    public void login(String szUserName, String szPassword, SessionCallBack CallBack) {
        if (MyiBaseApplication.getCommonVariables().MyiApplication == null) {
            throw new NullPointerException("CommonVarialbes.MyiApplication is null. Set the MyiApplication instance first.");
        }
        this.mCallBack = CallBack;
        this.mUserName = szUserName;
        this.mPassword = szPassword;
        this.mPasswordMD5 = Utilities.md5(this.mPassword);
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext());
        if (VirtualNetworkObject.getOfflineMode()) {
            parserLoginData(Settings.getString("OfflineLoginJson", ""));
            return;
        }
        MyiBaseApplication.enableSSL();
        doRealLogin();
    }

    private void doRealLogin() {
        LoadExamDataThread3 LoginThread = new LoadExamDataThread3(MyiBaseApplication.getBaseAppContext(), "UsersLoginJson", new OnSoapCompleteListener() {
            public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                String szJsonResult = arrSoapData.get(0).toString();
                Editor edit = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext()).edit();
                edit.putString("OfflineLoginJson", szJsonResult);
                edit.putString("LastSuccessLoginUserName", Session.this.mUserName);
                edit.putString("LastSuccessLoginPasswordMD5", Session.this.mPasswordMD5);
                edit.commit();
                Session.this.parserLoginData(szJsonResult);
            }
        });
        LoginThread.setOnSoapFailListener(new OnSoapFailListener() {
            public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                if (Session.this.mCallBack != null) {
                    Session.this.mCallBack.onLoginFailure(nReturnCode, Utilities.getErrorMessage(ThreadObject.getErrorCode(), ThreadObject.getErrorDescription()));
                }
                return true;
            }
        });
        LoginThread.addParam("lpszUserName", this.mUserName);
        LoginThread.addParam("lpszPasswordMD5", this.mPasswordMD5);
        LoginThread.addParam("lpszClientID", MyiBaseApplication.getCommonVariables().MyiApplication.getClientID());
        Object szHardwareKey = HardwareInfo.getHardwareInfo(MyiBaseApplication.getBaseAppContext());
        if (MyiBaseApplication.getCommonVariables().MyiApplication.getAdditionalHardwareInfo() != null) {
            szHardwareKey = new StringBuilder(String.valueOf(szHardwareKey)).append(MyiBaseApplication.getCommonVariables().MyiApplication.getAdditionalHardwareInfo()).toString();
        }
        LoginThread.addParam("lpszHardwareKey", szHardwareKey);
        LoginThread.start();
    }

    public void setCallBack(SessionCallBack callBack) {
        this.mCallBack = callBack;
    }

    public void relogin() {
        Log.d("Session", "relogin request found.");
        if (MyiBaseApplication.getCommonVariables().MyiApplication == null) {
            throw new NullPointerException("CommonVarialbes.MyiApplication is null. Set the MyiApplication instance first.");
        }
        if (this.mUserName == null || this.mUserName.isEmpty()) {
            SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext());
            this.mUserName = Settings.getString("LastSuccessLoginUserName", "");
            this.mPasswordMD5 = Settings.getString("LastSuccessLoginPasswordMD5", "");
            if (this.mUserName.isEmpty() || this.mPasswordMD5.isEmpty()) {
                Log.e("Session", "No last sucess login info. Relogin ejected.");
                if (this.mCallBack != null) {
                    this.mCallBack.onLoginFailure(ErrorCode.ERROR_ACCOUNT_EXPIRED, "没有找到最后一次成功登录的账号信息，无法自动重新登录。");
                    return;
                }
                return;
            }
        }
        LoadExamDataThread3 LoginThread = new LoadExamDataThread3(MyiBaseApplication.getBaseAppContext(), "UsersLoginJson", new OnSoapCompleteListener() {
            public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                String szJsonResult = arrSoapData.get(0).toString();
                Editor edit = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext()).edit();
                edit.putString("OfflineLoginJson", szJsonResult);
                edit.commit();
                Session.this.parserLoginData(szJsonResult);
                Log.d("Session", "relogin success.");
            }
        });
        LoginThread.setOnSoapFailListener(new OnSoapFailListener() {
            public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                Log.d("Session", "relogin failed. Force logout");
                Session.this.logOut();
                if (Session.this.mCallBack != null) {
                    Session.this.mCallBack.onLoginFailure(nReturnCode, Utilities.getErrorMessage(ThreadObject.getErrorCode(), ThreadObject.getErrorDescription()));
                }
                return true;
            }
        });
        LoginThread.addParam("lpszUserName", this.mUserName);
        LoginThread.addParam("lpszPasswordMD5", this.mPasswordMD5);
        LoginThread.addParam("lpszClientID", MyiBaseApplication.getCommonVariables().MyiApplication.getClientID());
        LoginThread.addParam("lpszHardwareKey", HardwareInfo.getHardwareInfo(MyiBaseApplication.getBaseAppContext()));
        LoginThread.start();
    }

    public String getSessionID() {
        return this.mSession;
    }

    public void parserLoginData(String szJsonResult) {
        if (szJsonResult != null && !szJsonResult.isEmpty()) {
            Log.d("Session", "jsonResult=" + szJsonResult);
            JSONObject json = new JSONObject(szJsonResult);
            this.mSession = json.getString("sessionid");
            if (MyiBaseApplication.isUseSSL()) {
                LoadExamDataThread3.setSessionID(this.mSession);
            }
            MyiBaseApplication.getCommonVariables().UserInfo.szUserName = this.mUserName;
            if (!MyiBaseApplication.getCommonVariables().UserInfo.decodeLoginJson(szJsonResult)) {
                MyiBaseApplication.saveState();
                if (this.mCallBack != null) {
                    this.mCallBack.onLoginFailure(ErrorCode.ERROR_JSON_PARSER_ERROR, "用户信息解析出现错误");
                }
            } else if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0 && MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size() == 0) {
                MyiBaseApplication.saveState();
                MyiBaseApplication.getCommonVariables().UserInfo.deleteSavedAccountConfig();
                if (this.mCallBack != null) {
                    this.mCallBack.onLoginFailure(ErrorCode.ERROR_JSON_PARSER_ERROR, "该学生账户必须分配至少一个班级");
                }
            } else {
                try {
                    if (this.mLimitUserType != -1) {
                        int nLoginUserType = MyiBaseApplication.getCommonVariables().UserInfo.nUserType;
                        if (this.mLimitUserType == 0) {
                            if (nLoginUserType != this.mLimitUserType) {
                                MyiBaseApplication.saveState();
                                MyiBaseApplication.getCommonVariables().UserInfo.deleteSavedAccountConfig();
                                if (this.mCallBack != null) {
                                    this.mCallBack.onLoginFailure(ErrorCode.ERROR_JSON_PARSER_ERROR, "不允许非学生账户登录");
                                    return;
                                }
                                return;
                            }
                        } else if (nLoginUserType == 0) {
                            MyiBaseApplication.saveState();
                            if (this.mCallBack != null) {
                                this.mCallBack.onLoginFailure(ErrorCode.ERROR_JSON_PARSER_ERROR, "不允许学生账户登录");
                                return;
                            }
                            return;
                        }
                    }
                    if (json.has("basicConfiguration")) {
                        MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.clear();
                        MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.initialize(MyiBaseApplication.getBaseAppContext(), json.getString("basicConfiguration"));
                        MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.clearScripts();
                    }
                    if (json.has("configuration")) {
                        if (MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.initialize(MyiBaseApplication.getBaseAppContext(), json.getString("configuration"))) {
                            if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_CAN_SWITCH_ACCOUNTS)) {
                                MyiBaseApplication.getCommonVariables().UserInfo.deleteSavedAccountConfig();
                            }
                            MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.executeScripts();
                        } else {
                            MyiBaseApplication.saveState();
                            if (this.mCallBack != null) {
                                this.mCallBack.onLoginFailure(ErrorCode.ERROR_JSON_PARSER_ERROR, "服务器配置信息解析出现错误");
                                return;
                            }
                            return;
                        }
                    }
                    Iterator it = MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.getPluginModules().iterator();
                    while (it.hasNext()) {
                        PluginModule plugin = (PluginModule) it.next();
                        HttpItemObject HttpObject = new HttpItemObject(plugin.szURL);
                        try {
                            final String szFileName = File.createTempFile("Plugin", ".apk").getAbsolutePath();
                            final String szClassName = plugin.szClassName;
                            HttpObject.setSizeLimit(104857600);
                            HttpObject.setSuccessListener(new OnSuccessListener() {
                                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                    if (MyiBaseApplication.PluginsManager.loadFromExternalAPKOrJar(szFileName, szClassName)) {
                                        new File(szFileName).delete();
                                    }
                                }
                            });
                            HttpObject.setFailureListener(new OnFailureListener() {
                                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                }
                            });
                            HttpObject.setTargetFileName(szFileName);
                            HttpObject.setSaveToFile(true);
                            HttpObject.setAlwaysActiveCallbacks(true);
                            VirtualNetworkObject.addToQueue(HttpObject);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext()).edit();
                    if (!this.mPassword.isEmpty()) {
                        editor.putString("OfflinePassword", this.mPassword);
                        editor.putString("OfflineJson", szJsonResult);
                        editor.commit();
                    }
                    this.mbLoggedIn = true;
                    MyiBaseApplication.startBackgroundService();
                    MyiBaseApplication.saveState();
                    if (this.mCallBack != null) {
                        this.mCallBack.onLoginSuccess(szJsonResult);
                    }
                } catch (JSONException e2) {
                    e2.printStackTrace();
                    if (this.mCallBack != null) {
                        this.mCallBack.onLoginFailure(ErrorCode.ERROR_JSON_PARSER_ERROR, e2.getMessage());
                    }
                }
            }
        } else if (this.mCallBack != null) {
            this.mCallBack.onLoginFailure(ErrorCode.ERROR_JSON_PARSER_ERROR, ErrorCode.getErrorMessage(ErrorCode.ERROR_JSON_PARSER_ERROR));
        }
    }

    public String getPasswordMD5() {
        return this.mPasswordMD5;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public void logOut() {
        WebServiceCallItemObject ItemObject = new WebServiceCallItemObject("UsersLogout", null);
        ItemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            }
        });
        ItemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            }
        });
        ItemObject.setParam("lpszSessionID", this.mSession);
        ItemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ItemObject);
        MyiBaseApplication.cleanState();
        proceslogOut();
    }

    private void proceslogOut() {
        if (this.mbLoggedIn) {
            Utilities.runOnUIThread(MyiBaseApplication.getBaseAppContext(), new Runnable() {
                public void run() {
                    if (Session.this.mbLoggedIn) {
                        Session.this.mSession = "";
                        Session.this.mbLoggedIn = false;
                        LoadExamDataThread3.setSessionID("");
                        MyiBaseApplication.stopBackgroundService();
                        MyiBaseApplication.startLogout();
                    }
                }
            });
        }
    }

    public boolean isLoggedIn() {
        return this.mbLoggedIn;
    }
}
