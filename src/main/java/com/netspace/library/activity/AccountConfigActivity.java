package com.netspace.library.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.dm.zbar.android.scanner.CameraPreview;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.consts.Const;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.parser.ServerConfigurationParser;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapCompleteListener;
import com.netspace.library.threads.LoadExamDataThread3.OnSoapFailListener;
import com.netspace.library.utilities.HardwareInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapPrimitive;

public class AccountConfigActivity extends Activity implements PreviewCallback, ZBarConstants, OnClickListener {
    private static AccountQRCodeCallBack mCallBack;
    private static int mLabelResID = R.id.textViewServerAddress;
    private static int mLayoutResID = R.layout.activity_accountconfig;
    private final int MSG_CHECKFAIL = -1;
    private final int MSG_CHECKSUCCESS = 1;
    private final String PATTERN = "(\\w+)\\@(\\S+)\\:([0-9]+)";
    private String TAG = "AccountConfigActivity";
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            AccountConfigActivity.this.mAutoFocusHandler.postDelayed(AccountConfigActivity.this.doAutoFocus, 1000);
        }
    };
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (AccountConfigActivity.this.mCamera != null && AccountConfigActivity.this.mPreviewing) {
                AccountConfigActivity.this.mCamera.autoFocus(AccountConfigActivity.this.autoFocusCB);
            }
        }
    };
    private EditText mAccountText;
    private Handler mAutoFocusHandler;
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean mPreviewing = true;
    private ImageScanner mScanner;
    private TextView mTextViewPrompt;
    private TextView mTextViewServerAddress;
    private LoadExamDataThread3 m_GetDataCenterInfoThread;
    private Handler m_ThreadMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    AccountConfigActivity.this.findViewById(R.id.layoutWorking).setVisibility(4);
                    AccountConfigActivity.this.findViewById(R.id.buttonFinish).setEnabled(true);
                    if (MyiBaseApplication.canExecuteIPTableScript()) {
                        ServerConfigurationParser ServerConfigurationParser = new ServerConfigurationParser();
                        ServerConfigurationParser.generateHostLimitScript();
                        ServerConfigurationParser.executeScripts(true);
                    }
                    if (msg.obj != null && (msg.obj instanceof String)) {
                        Utilities.showAlertMessage(AccountConfigActivity.this, "服务器连接错误", (String) msg.obj);
                        return;
                    }
                    return;
                case 1:
                    AccountConfigActivity.this.findViewById(R.id.layoutWorking).setVisibility(4);
                    AccountConfigActivity.this.findViewById(R.id.buttonFinish).setEnabled(true);
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(AccountConfigActivity.this).edit();
                    editor.putString("FullAccount", AccountConfigActivity.this.mszFullAcccount);
                    editor.putString("ServerAddress", AccountConfigActivity.this.mszServerAddress);
                    editor.putString("BaseAddress", AccountConfigActivity.this.mszBaseAddress);
                    editor.putString(UserHonourFragment.USERNAME, AccountConfigActivity.this.mszUserName);
                    if (AccountConfigActivity.this.marrUserNames.size() > 0 && AccountConfigActivity.this.marrUserClassNames.size() > 0) {
                        for (int i = 0; i < AccountConfigActivity.this.marrUserNames.size(); i++) {
                            AccountConfigActivity.this.marrUserNames.set(i, new StringBuilder(String.valueOf((String) AccountConfigActivity.this.marrUserNames.get(i))).append("|").append((String) AccountConfigActivity.this.marrUserClassNames.get(i)).toString());
                        }
                        editor.putStringSet("UserNames", new LinkedHashSet(AccountConfigActivity.this.marrUserNames));
                    }
                    editor.commit();
                    MyiBaseApplication.startMainActivity();
                    AccountConfigActivity.this.finish();
                    return;
                default:
                    return;
            }
        }
    };
    private ArrayList<String> marrUserClassNames;
    private ArrayList<String> marrUserNames;
    private boolean mbLockAccount = false;
    private boolean mbLockServerAddress = false;
    private String mszBaseAddress = "";
    private String mszFullAcccount = "";
    private String mszOldFullAccount;
    private String mszOldSchoolGUID;
    private String mszOldServerAddress;
    private String mszOldUserName;
    private String mszPassword = "";
    private String mszServerAddress = "";
    private String mszUserName = "";

    public interface AccountQRCodeCallBack {
        boolean onBootloader();

        boolean onWipe();
    }

    private class ResourceTestThreads extends Thread {
        private ResourceTestThreads() {
        }

        public void run() {
            super.run();
            if (Utilities.isURLAccessable(AccountConfigActivity.this.mszBaseAddress)) {
                AccountConfigActivity.this.m_ThreadMessageHandler.obtainMessage(1).sendToTarget();
            } else {
                AccountConfigActivity.this.m_ThreadMessageHandler.obtainMessage(-1, "已成功连接到数据服务器，但无法连接到资源服务器。").sendToTarget();
            }
        }
    }

    static {
        System.loadLibrary("iconv");
    }

    public void setAccountLock(boolean bLockFullAccount, boolean bLockServerAddress) {
        this.mbLockAccount = bLockFullAccount;
        this.mbLockServerAddress = bLockServerAddress;
        updateDisplay();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCameraAvailable();
        requestWindowFeature(1);
        getWindow().addFlags(1024);
        this.mAutoFocusHandler = new Handler();
        setupScanner();
        this.mPreview = new CameraPreview(this, this, this.autoFocusCB);
        setContentView(mLayoutResID);
        this.mTextViewServerAddress = (TextView) findViewById(mLabelResID);
        this.mAccountText = (EditText) findViewById(R.id.editAccount);
        this.mTextViewPrompt = (TextView) findViewById(R.id.textViewVoiceMessage);
        if (this.mTextViewServerAddress != null) {
            this.mTextViewServerAddress.setVisibility(8);
            File externalFile = getExternalCacheDir();
            if (MyiBaseApplication.ReleaseBuild && externalFile != null) {
                String szContent = Utilities.readTextFile(externalFile.getAbsolutePath() + "/../../." + getPackageName() + ".config");
                if (szContent == null || szContent.isEmpty()) {
                    this.mTextViewServerAddress.setVisibility(8);
                } else {
                    if (!szContent.startsWith("{")) {
                        szContent = Utilities.decryptString(szContent, Const.PASSWORDFORLOGINCONFIG);
                    }
                    try {
                        JSONObject json = new JSONObject(szContent);
                        this.mszOldServerAddress = json.getString("serveraddress");
                        this.mszOldFullAccount = json.getString("fullaccount");
                        this.mszOldUserName = json.getString(UserHonourFragment.USERNAME);
                        this.mszOldSchoolGUID = json.getString("schoolguid");
                        this.mbLockAccount = true;
                        this.mbLockServerAddress = true;
                        updateDisplay();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        getWindow().setSoftInputMode(2);
        LinearLayout Layout = (LinearLayout) findViewById(R.id.LinearLayout1);
        if (Layout != null) {
            LayoutParams Params = new LayoutParams(Utilities.dpToPixel(180, (Context) this), Utilities.dpToPixel(150, (Context) this));
            Params.topMargin = Utilities.dpToPixel(10, (Context) this);
            Params.gravity = 1;
            Layout.addView(this.mPreview, Params);
            Layout.invalidate();
        }
        findViewById(R.id.buttonFinish).setOnClickListener(this);
        ((ViewGroup) findViewById(16908290)).getChildAt(0).invalidate();
    }

    private void updateDisplay() {
        if (this.mbLockAccount) {
            this.mTextViewServerAddress.setVisibility(8);
            this.mAccountText.setText(this.mszOldFullAccount);
            this.mAccountText.setEnabled(false);
            this.mTextViewPrompt.setText("此平板已被绑定到此账号，如需更换，请联系学校负责人。");
        } else if (this.mbLockServerAddress) {
            this.mTextViewServerAddress.setVisibility(0);
            this.mTextViewServerAddress.setText("@" + this.mszOldServerAddress);
            this.mAccountText.setText(this.mszOldUserName);
            this.mAccountText.setEnabled(true);
            this.mTextViewPrompt.setText("此平板已被绑定到此服务器，如需更换，请联系学校负责人。");
        } else {
            this.mTextViewServerAddress.setVisibility(8);
            this.mAccountText.setText(this.mszOldFullAccount);
            this.mAccountText.setEnabled(true);
        }
    }

    public void setupScanner() {
        this.mScanner = new ImageScanner();
        this.mScanner.setConfig(0, 256, 3);
        this.mScanner.setConfig(0, 257, 3);
        int[] symbols = getIntent().getIntArrayExtra(ZBarConstants.SCAN_MODES);
        if (symbols != null) {
            this.mScanner.setConfig(0, 0, 0);
            for (int symbol : symbols) {
                this.mScanner.setConfig(symbol, 0, 1);
            }
        }
    }

    public void restartPreview() {
        this.mPreview.setCamera(null);
        this.mCamera.cancelAutoFocus();
        this.mCamera.setPreviewCallback(null);
        this.mCamera.stopPreview();
        this.mCamera.release();
        this.mPreview.hideSurfaceView();
        this.mPreviewing = false;
        this.mCamera = null;
        this.mCamera = Camera.open();
        if (this.mCamera != null) {
            this.mPreview.setCamera(this.mCamera);
            this.mPreview.showSurfaceView();
            this.mPreviewing = true;
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.m_GetDataCenterInfoThread != null) {
            this.m_GetDataCenterInfoThread.setCancel(false);
            this.m_GetDataCenterInfoThread = null;
            findViewById(R.id.layoutWorking).setVisibility(4);
            findViewById(R.id.buttonFinish).setEnabled(true);
        }
        this.mCamera = Camera.open();
        if (this.mCamera != null) {
            this.mPreview.setCamera(this.mCamera);
            this.mPreview.showSurfaceView();
            this.mPreviewing = true;
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.m_GetDataCenterInfoThread != null) {
            this.m_GetDataCenterInfoThread.setCancel(true);
            this.m_GetDataCenterInfoThread = null;
            findViewById(R.id.layoutWorking).setVisibility(4);
            findViewById(R.id.buttonFinish).setEnabled(true);
        }
        if (this.mCamera != null) {
            this.mPreview.setCamera(null);
            this.mCamera.cancelAutoFocus();
            this.mCamera.setPreviewCallback(null);
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mPreview.hideSurfaceView();
            this.mPreviewing = false;
            this.mCamera = null;
        }
    }

    public boolean isCameraAvailable() {
        return getPackageManager().hasSystemFeature("android.hardware.camera");
    }

    public void cancelRequest() {
        Intent dataIntent = new Intent();
        dataIntent.putExtra(ZBarConstants.ERROR_INFO, "Camera unavailable");
        setResult(0, dataIntent);
        finish();
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        Size size = camera.getParameters().getPreviewSize();
        Image barcode = new Image(size.width, size.height, "Y800");
        barcode.setData(data);
        if (this.mScanner.scanImage(barcode) != 0) {
            Iterator it = this.mScanner.getResults().iterator();
            while (it.hasNext()) {
                String symData = ((Symbol) it.next()).getData();
                if (!TextUtils.isEmpty(symData)) {
                    if (symData.startsWith("cmd://")) {
                        this.mCamera.cancelAutoFocus();
                        this.mCamera.setPreviewCallback(null);
                        this.mCamera.stopPreview();
                        this.mPreviewing = false;
                        if (processQRCommands(symData)) {
                            return;
                        }
                    }
                    if (Pattern.compile("(\\w+)\\@(\\S+)\\:([0-9]+)").matcher(symData).find()) {
                        EditText Result = (EditText) findViewById(R.id.editAccount);
                        Result.setText(symData);
                        Result.setEnabled(false);
                        this.mCamera.cancelAutoFocus();
                        this.mCamera.setPreviewCallback(null);
                        this.mCamera.stopPreview();
                        this.mPreviewing = false;
                        return;
                    }
                    return;
                }
            }
        }
    }

    private boolean processQRCommands(String symData) {
        final String szCommand = symData.substring(6);
        final EditText txtUrl = new EditText(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.addView(txtUrl);
        LayoutParams params = (LayoutParams) txtUrl.getLayoutParams();
        params.leftMargin = Utilities.dpToPixel(20, (Context) this);
        params.rightMargin = Utilities.dpToPixel(20, (Context) this);
        params.width = Utilities.dpToPixel(200, (Context) this);
        txtUrl.setInputType(18);
        linearLayout.setGravity(17);
        txtUrl.setHint("请输入密码");
        new Builder(this).setTitle("请输入密码").setMessage("请输入密码后使用此二维码的功能").setView(linearLayout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (txtUrl.getText().toString().isEmpty()) {
                    Utilities.showAlertMessage(AccountConfigActivity.this, "密码无效", "您必须输入密码才能使用此二维码。");
                    return;
                }
                String szNow = new SimpleDateFormat("yyyy-MM-dd HH", Locale.ENGLISH).format(Calendar.getInstance().getTime());
                String szWipeData = Utilities.md5(String.format("%s-%s-%s-%s", new Object[]{AccountConfigActivity.this.mszOldSchoolGUID, "Wipe", szNow, szPassword}));
                String szBootloaderData = Utilities.md5(String.format("%s-%s-%s-%s", new Object[]{AccountConfigActivity.this.mszOldSchoolGUID, "Bootloader", szNow, szPassword}));
                String szUnlockAccount = Utilities.md5(String.format("%s-%s-%s-%s", new Object[]{AccountConfigActivity.this.mszOldSchoolGUID, "UnlockAccount", szNow, szPassword}));
                String szUnlockAddress = Utilities.md5(String.format("%s-%s-%s-%s", new Object[]{AccountConfigActivity.this.mszOldSchoolGUID, "UnlockServerAddress", szNow, szPassword}));
                String szSuperWipeData = Utilities.md5(String.format("%s-%s-%s-%s", new Object[]{"d9ad958bc9a3ff412ed29e2e768b0ce9", "Wipe", "2015-01-01 00", "987654321"}));
                if (szUnlockAddress.contentEquals(szCommand)) {
                    AccountConfigActivity.this.mbLockAccount = false;
                    AccountConfigActivity.this.mbLockServerAddress = false;
                    AccountConfigActivity.this.updateDisplay();
                    Utilities.showAlertMessage(AccountConfigActivity.this, "执行结果", "已解锁服务器地址和账号更改", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AccountConfigActivity.this.restartPreview();
                        }
                    });
                } else if (szUnlockAccount.contentEquals(szCommand)) {
                    AccountConfigActivity.this.mbLockServerAddress = true;
                    AccountConfigActivity.this.mbLockAccount = false;
                    AccountConfigActivity.this.updateDisplay();
                    Utilities.showAlertMessage(AccountConfigActivity.this, "执行结果", "已解锁账户更改", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AccountConfigActivity.this.restartPreview();
                        }
                    });
                } else if (szBootloaderData.contentEquals(szCommand)) {
                    Utilities.showAlertMessage(AccountConfigActivity.this, "执行确认", "点击是后将要重启进入刷机模式", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (AccountConfigActivity.mCallBack != null && !AccountConfigActivity.mCallBack.onBootloader()) {
                                Utilities.showAlertMessage(AccountConfigActivity.this, "执行错误", "进入刷机模式失败，当前平板可能尚未正确激活ELM权限。");
                            }
                        }
                    }, null);
                } else if (szWipeData.contentEquals(szCommand) || szSuperWipeData.equalsIgnoreCase(szCommand)) {
                    Utilities.showAlertMessage(AccountConfigActivity.this, "执行确认", "点击是后将要重启并恢复为出厂设置", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (AccountConfigActivity.mCallBack != null && !AccountConfigActivity.mCallBack.onWipe()) {
                                Utilities.showAlertMessage(AccountConfigActivity.this, "执行错误", "进入恢复出厂模式失败，当前平板可能尚未正确激活ELM权限。");
                            }
                        }
                    }, null);
                } else {
                    Utilities.showAlertMessage(AccountConfigActivity.this, "无法执行", "命令无法执行，请检查您输入的密码是否正确。", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AccountConfigActivity.this.restartPreview();
                        }
                    });
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
        return false;
    }

    public void onClick(View v) {
        if (v.getId() != R.id.buttonFinish) {
            return;
        }
        if (Utilities.isCurrentUserOwner()) {
            String szAccount = ((EditText) findViewById(R.id.editAccount)).getEditableText().toString();
            boolean bAccountCorrect = false;
            if (this.mCamera != null || !szAccount.startsWith("cmd://") || !processQRCommands(szAccount)) {
                if (this.mbLockAccount) {
                    szAccount = this.mszOldFullAccount;
                } else if (this.mbLockServerAddress) {
                    szAccount = new StringBuilder(String.valueOf(szAccount)).append("@").append(this.mszOldServerAddress).toString();
                }
                szAccount = szAccount.replace(" ", "");
                if (szAccount.indexOf("@") == -1) {
                    szAccount = new StringBuilder(String.valueOf(szAccount)).append("@webservice.myi.cn:8089").toString();
                }
                if (Pattern.compile("(\\w+)\\@(\\S+)\\:([0-9]+)").matcher(szAccount).find()) {
                    bAccountCorrect = true;
                }
                if (bAccountCorrect) {
                    int nPos;
                    String[] arrAccounts = szAccount.split("\\|");
                    this.marrUserNames = new ArrayList();
                    this.marrUserClassNames = new ArrayList();
                    if (arrAccounts.length > 1) {
                        szAccount = arrAccounts[0];
                        for (String szOneAccount : arrAccounts) {
                            nPos = szOneAccount.indexOf("@");
                            if (nPos != -1) {
                                this.marrUserNames.add(szOneAccount.substring(0, nPos));
                                this.marrUserClassNames.add("");
                            }
                        }
                    }
                    nPos = szAccount.indexOf("@");
                    if (nPos != -1) {
                        this.mszUserName = szAccount.substring(0, nPos);
                    }
                    this.mszFullAcccount = szAccount;
                    Builder builder = new Builder(this, 3);
                    View view = ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.dialog_inputpassword, null);
                    builder.setView(view);
                    builder.setTitle("请输入账户的密码");
                    builder.setCancelable(false);
                    final EditText input = (EditText) view.findViewById(R.id.editPassword);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AccountConfigActivity.this.mszPassword = input.getText().toString();
                            if (AccountConfigActivity.this.mszPassword.isEmpty()) {
                                Utilities.showAlertMessage(AccountConfigActivity.this, "密码错误", "请输入账号的密码");
                                return;
                            }
                            MyiBaseApplication.disableSSL();
                            AccountConfigActivity.this.ServerTest(AccountConfigActivity.this.mszFullAcccount);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                    return;
                }
                Utilities.showAlertMessage(this, "帐号错误", "您输入的账户格式不正确，请输入或扫描您的登录账户。");
                return;
            }
            return;
        }
        Utilities.showAlertMessage(this, "登录错误", "当前安装的用户不是这个平板的所有者，请重新在所有者账户下安装。");
    }

    private boolean ServerTest(final String szAccount) {
        int nPos = szAccount.indexOf("@");
        String szServerAddress = "";
        boolean bResult = false;
        if (nPos != -1) {
            findViewById(R.id.layoutWorking).setVisibility(0);
            findViewById(R.id.buttonFinish).setEnabled(false);
            szServerAddress = szAccount.substring(nPos + 1);
            nPos = szServerAddress.indexOf("|");
            if (nPos != -1) {
                szServerAddress = szAccount.substring(0, nPos);
            }
            this.mszServerAddress = szServerAddress;
            LoadExamDataThread3.setupServerAddress(szServerAddress);
            ServerConfigurationParser ServerConfigurationParser = new ServerConfigurationParser();
            ServerConfigurationParser.generateRemoveLimitScript();
            Log.i(this.TAG, "MyiBaseApplication.isRootRequired() " + MyiBaseApplication.isRootRequired());
            Log.i(this.TAG, "MyiBaseApplication.canExecuteIPTableScript() " + MyiBaseApplication.canExecuteIPTableScript());
            if (MyiBaseApplication.isRootRequired() && !ServerConfigurationParser.executeScripts(true)) {
                Utilities.showAlertMessage(this, "无法获得管理员权限", "当前无法获得管理员权限，请允许获得，否则您将无法使用。");
                findViewById(R.id.buttonFinish).setEnabled(true);
                return false;
            } else if (MyiBaseApplication.canExecuteIPTableScript() && !ServerConfigurationParser.executeScripts(true)) {
                Utilities.showAlertMessage(this, "无法执行", "当前无法重新设置网络，请重启后再试。");
                findViewById(R.id.buttonFinish).setEnabled(true);
                return false;
            } else if (true) {
                this.m_GetDataCenterInfoThread = new LoadExamDataThread3(this, "GetDataCenterInfo", new OnSoapCompleteListener() {
                    public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                        if (arrSoapData.get(1) instanceof SoapPrimitive) {
                            AccountConfigActivity.this.mszBaseAddress = arrSoapData.get(1).toString();
                            AccountConfigActivity.this.loginTest();
                            return;
                        }
                        Utilities.showAlertMessage(AccountConfigActivity.this, "数据错误", "服务器端返回的数据有错误。");
                        AccountConfigActivity.this.m_ThreadMessageHandler.obtainMessage(-1).sendToTarget();
                    }
                });
                this.m_GetDataCenterInfoThread.setOnSoapFailListener(new OnSoapFailListener() {
                    public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                        if (MyiBaseApplication.isUseSSL()) {
                            String szErrorDescription = ThreadObject.getErrorDescription();
                            if (szErrorDescription == null || szErrorDescription.isEmpty()) {
                                szErrorDescription = ErrorCode.getErrorMessage(nReturnCode);
                            }
                            Utilities.showAlertMessage(AccountConfigActivity.this, "连接错误", "无法连接账户中指定的数据服务器，错误原因：" + szErrorDescription);
                            AccountConfigActivity.this.m_ThreadMessageHandler.obtainMessage(-1).sendToTarget();
                        } else {
                            MyiBaseApplication.enableSSL();
                            AccountConfigActivity.this.ServerTest(szAccount);
                        }
                        return true;
                    }
                });
                this.m_GetDataCenterInfoThread.start();
                bResult = true;
            }
        }
        return bResult;
    }

    private void loginTest() {
        Object szHardwareInfo = HardwareInfo.getHardwareInfo(this);
        this.m_GetDataCenterInfoThread = new LoadExamDataThread3(this, "UsersLoginJson", new OnSoapCompleteListener() {
            public void OnDataOK(Vector arrSoapData, LoadExamDataThread3 ThreadObject, int nReturnCode) {
                new ResourceTestThreads().start();
            }
        });
        this.m_GetDataCenterInfoThread.setOnSoapFailListener(new OnSoapFailListener() {
            public boolean OnDataFail(LoadExamDataThread3 ThreadObject, int nReturnCode) {
                String szErrorDescription = ThreadObject.getErrorDescription();
                if (szErrorDescription == null || szErrorDescription.isEmpty()) {
                    szErrorDescription = ErrorCode.getErrorMessage(nReturnCode);
                }
                Utilities.showAlertMessage(AccountConfigActivity.this, "账户登录错误", "无法登录您所输入的账户，错误原因：" + szErrorDescription);
                AccountConfigActivity.this.m_ThreadMessageHandler.obtainMessage(-1).sendToTarget();
                return false;
            }
        });
        this.m_GetDataCenterInfoThread.addParam("lpszUserName", this.mszUserName);
        this.m_GetDataCenterInfoThread.addParam("lpszPasswordMD5", Utilities.md5(this.mszPassword));
        this.m_GetDataCenterInfoThread.addParam("lpszClientID", MyiBaseApplication.getCommonVariables().MyiApplication.getClientID());
        this.m_GetDataCenterInfoThread.addParam("lpszHardwareKey", szHardwareInfo);
        this.m_GetDataCenterInfoThread.start();
    }

    public static void setCallBack(AccountQRCodeCallBack callBack) {
        mCallBack = callBack;
    }

    public static void setLayoutResID(int nResID, int nLabelResID) {
        mLayoutResID = nResID;
        mLabelResID = nLabelResID;
    }

    protected void onDestroy() {
        mCallBack = null;
        super.onDestroy();
    }
}
