package com.netspace.teacherpad;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.struct.Session.SessionCallBack;
import com.netspace.library.threads.CheckNewVersionThread;
import com.netspace.library.utilities.HardwareInfo;
import com.netspace.library.utilities.Utilities;
import java.io.File;

public class LoginActivity2 extends Activity implements OnClickListener {
    public static final int MSG_AUTHFAIL = 258;
    public static final int MSG_AUTHSUCCESS = 257;
    public static final int MSG_DOWNLOADFAIL = 260;
    public static final int MSG_DOWNLOADPROGRESS = 256;
    public static final int MSG_DOWNLOADSUCCESS = 261;
    public static final int MSG_NONEWVERSION = 259;
    public static final int MSG_VERSIONCHECKFAIL = 262;
    private static CheckNewVersionThread m_CheckThread;
    private static boolean m_bLoggedIn = false;
    private final Runnable UserImageGetRunnable = new Runnable() {
        public void run() {
            Log.d("UserImageGetRunnable", "UserImageGetRunnable.run");
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(2000);
            if (LoginActivity2.this.getExternalCacheDir() == null || LoginActivity2.this.getExternalCacheDir().getAbsolutePath() == null) {
                LoginActivity2.this.m_Handler.postDelayed(LoginActivity2.this.UserImageGetRunnable, 500);
                return;
            }
            LoginActivity2.this.m_szAvatarFileName = new StringBuilder(String.valueOf(LoginActivity2.this.getExternalCacheDir().getAbsolutePath())).append("/").append(LoginActivity2.this.m_szUserName).append("_avatar.jpg").toString();
            ImageView ImageView = (ImageView) LoginActivity2.this.findViewById(R.id.imageView1);
            if (new File(LoginActivity2.this.m_szAvatarFileName).exists()) {
                ImageView.setImageURI(null);
                ImageView.setImageURI(Uri.fromFile(new File(LoginActivity2.this.m_szAvatarFileName)));
            }
            View imageViewLayout = LoginActivity2.this.findViewById(R.id.imageViewLayout);
            imageViewLayout.setVisibility(4);
            imageViewLayout.startAnimation(fadeIn);
            imageViewLayout.setVisibility(0);
        }
    };
    private Runnable m_AutologinRunnable = new Runnable() {
        public void run() {
            if (Utilities.isNetworkConnected(LoginActivity2.this.m_Context)) {
                LoginActivity2.this.m_LoginButton.performClick();
            } else {
                LoginActivity2.this.m_ThreadMessageHandler.postDelayed(LoginActivity2.this.m_AutologinRunnable, 300);
            }
        }
    };
    private Context m_Context = null;
    private Handler m_Handler;
    private Button m_LoginButton;
    private SharedPreferences m_Settings = null;
    private EditText m_TextPassword;
    private TextView m_TextViewVersionCheck;
    private Handler m_ThreadMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 256:
                    LoginActivity2.this.m_TextViewVersionCheck.setText("正在下载新版本，" + Integer.valueOf((int) ((Float) msg.obj).floatValue()) + "% 已完成...");
                    return;
                case 257:
                    Editor editor;
                    if (LoginActivity2.this.m_bSaveLoginInfo) {
                        editor = LoginActivity2.this.m_Settings.edit();
                        editor.putString("password", LoginActivity2.this.m_szPassword);
                        editor.commit();
                    }
                    editor = LoginActivity2.this.m_Settings.edit();
                    editor.putString("RealName", MyiBaseApplication.getCommonVariables().UserInfo.szRealName);
                    editor.commit();
                    MyiBaseApplication.getCommonVariables().Session.setCallBack(null);
                    LoginActivity2.m_bLoggedIn = true;
                    Intent TestIntent = new Intent(LoginActivity2.this.m_Context, MainActivity.class);
                    TestIntent.setFlags(67108864);
                    LoginActivity2.this.startActivity(TestIntent);
                    LoginActivity2.this.finish();
                    return;
                case 258:
                    String szPromptText = "登录出现错误。";
                    if (msg.obj instanceof String) {
                        szPromptText = msg.obj;
                    }
                    new Builder(LoginActivity2.this.m_Context).setTitle("登录错误").setIcon(17301543).setMessage(szPromptText).setPositiveButton("确定", null).show();
                    LoginActivity2.this.m_TextPassword.setEnabled(true);
                    LoginActivity2.this.m_LoginButton.setEnabled(true);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean m_bDataCenterInfoComplete = false;
    private boolean m_bSaveLoginInfo = false;
    private boolean m_bSimpleLoginComplete = false;
    private String m_szAvatarFileName;
    private String m_szPassword = "";
    private String m_szUserName = "";
    private LoginActivity2 m_this = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        Utilities.trimCache(this);
        this.m_this = this;
        this.m_Handler = new Handler();
        setContentView(R.layout.activity_login2);
        this.m_Context = this;
        this.m_LoginButton = (Button) findViewById(R.id.buttonLogin);
        this.m_LoginButton.setOnClickListener(this);
        this.m_TextPassword = (EditText) findViewById(R.id.editTextPassword);
        this.m_TextPassword.setImeOptions(2);
        this.m_TextPassword.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != 2) {
                    return false;
                }
                LoginActivity2.this.m_LoginButton.performClick();
                return true;
            }
        });
        this.m_TextViewVersionCheck = (TextView) findViewById(R.id.textViewNewVersion);
        ((TextView) findViewById(R.id.textViewVersion)).setText("当前版本号: " + TeacherPadApplication.szAppVersionName);
        this.m_Settings = PreferenceManager.getDefaultSharedPreferences(this);
        this.m_bSaveLoginInfo = this.m_Settings.getBoolean("RememberPassword", false);
        this.m_szUserName = this.m_Settings.getString(UserHonourFragment.USERNAME, "");
        if (this.m_bSaveLoginInfo) {
            this.m_TextPassword.setText(this.m_Settings.getString("password", ""));
        }
        ((TextView) findViewById(R.id.textViewRealName)).setText(this.m_Settings.getString("RealName", this.m_szUserName));
        String szVersionCheckURL = "http://updates.myi.cn/release/updates/teacherpad.asp";
        String szBaseAddress = this.m_Settings.getString("BaseAddress", "");
        boolean bUseOnlyFileName = false;
        if (!szBaseAddress.isEmpty() && szBaseAddress.indexOf("webservice.myi.cn") == -1) {
            if (!szBaseAddress.endsWith("/")) {
                szBaseAddress = new StringBuilder(String.valueOf(szBaseAddress)).append("/").toString();
            }
            szVersionCheckURL = new StringBuilder(String.valueOf(szBaseAddress)).append("updates/release/updates/teacherpad.asp").toString();
            bUseOnlyFileName = true;
        }
        if (m_CheckThread == null) {
            m_CheckThread = new CheckNewVersionThread(getApplicationContext(), TeacherPadApplication.szAppVersionName, szVersionCheckURL, this.m_TextViewVersionCheck, this.m_ThreadMessageHandler, bUseOnlyFileName);
            m_CheckThread.start();
        } else {
            m_CheckThread.SetContext(this);
            m_CheckThread.SetMessageHandler(this.m_ThreadMessageHandler);
            m_CheckThread.SetViewProgress(this.m_TextViewVersionCheck);
        }
        if (!m_bLoggedIn && this.m_Settings.getBoolean("AutoLogin", false) && !this.m_TextPassword.getText().toString().isEmpty()) {
            this.m_ThreadMessageHandler.postDelayed(this.m_AutologinRunnable, 1000);
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.m_szAvatarFileName == null) {
            this.m_Handler.postDelayed(this.UserImageGetRunnable, 10);
        }
        this.m_LoginButton.setEnabled(true);
        this.m_TextPassword.setEnabled(true);
    }

    public void onClick(final View v) {
        Utilities.logClick(v);
        this.m_ThreadMessageHandler.removeCallbacks(this.m_AutologinRunnable);
        if (Utilities.isNetworkConnected(this)) {
            String szHardwareInfo = HardwareInfo.getHardwareInfo(this);
            String szUserName = this.m_szUserName;
            String szPassword = this.m_TextPassword.getText().toString();
            this.m_szPassword = szPassword;
            if (szPassword.isEmpty()) {
                new Builder(this.m_Context).setTitle("登录错误").setMessage("请输入您的密码").setIcon(17301543).setPositiveButton("确定", null).show();
                return;
            }
            MyiBaseApplication.getCommonVariables().Session.setLimitUserType(1);
            MyiBaseApplication.getCommonVariables().Session.login(this.m_szUserName, szPassword, new SessionCallBack() {
                public void onLoginSuccess(String szJsonResult) {
                    Utilities.logClick(v, "login success");
                    LoginActivity2.this.m_ThreadMessageHandler.obtainMessage(257).sendToTarget();
                }

                public void onLoginFailure(int nReturnCode, String szReason) {
                    Utilities.logClick(v, szReason);
                    LoginActivity2.this.m_ThreadMessageHandler.obtainMessage(258, szReason).sendToTarget();
                }
            });
            this.m_TextPassword.setEnabled(false);
            this.m_LoginButton.setEnabled(false);
            return;
        }
        Utilities.logClick(v, "no network");
        new Builder(this.m_Context).setTitle("登录错误").setMessage("当前没有检测到任何网络连接，无法访问服务器。\n请检查无线连接是否正常。").setIcon(17301543).setPositiveButton("确定", null).show();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 3:
                event.isCanceled();
                return true;
            case 4:
                this.m_ThreadMessageHandler.removeCallbacks(this.m_AutologinRunnable);
                event.isCanceled();
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}
