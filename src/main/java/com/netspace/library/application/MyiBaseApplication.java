package com.netspace.library.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.foxit.app.App;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.NovaIconsModule;
import com.netspace.library.activity.WifiConfigActivity;
import com.netspace.library.bluetooth.BlueToothPen;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.components.DrawComponent;
import com.netspace.library.consts.Const;
import com.netspace.library.consts.Features;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.global.CommonVariables;
import com.netspace.library.im.IMService;
import com.netspace.library.parser.ServerConfigurationParser;
import com.netspace.library.plugins.PluginsManager;
import com.netspace.library.receiver.ShutdownReceiver;
import com.netspace.library.receiver.WifiReceiver;
import com.netspace.library.servers.HttpServer;
import com.netspace.library.servers.MJpegServer;
import com.netspace.library.struct.RecentUser;
import com.netspace.library.struct.ServerInfo;
import com.netspace.library.struct.Session;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.threads.LoadExamDataThread3;
import com.netspace.library.threads.LoadExamDataThread3.OnSessionFailListener;
import com.netspace.library.threads.UsageDataUploadThread;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.SSLConnection;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.DataSynchronizeEngine;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.window.ChatWindow;
import com.netspace.pad.library.R;
import java.io.File;
import java.io.IOException;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.HttpHost;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;
import wei.mark.standout.StandOutWindow;

public class MyiBaseApplication extends Application {
    public static boolean DEBUG = true;
    public static boolean EncryptedBuild = false;
    public static boolean PhoneMode = false;
    public static PluginsManager PluginsManager = new PluginsManager();
    public static boolean PowerOff = false;
    public static boolean ReleaseBuild = false;
    private static final String TAG = "MyiBaseApplication";
    public static boolean UseThreadModeForIM = false;
    protected static boolean WakeUpOnWifiConnected = false;
    protected static Context mBaseContext;
    protected static CommonVariables mCommonVariables;
    protected static boolean mConfigured = false;
    protected static File mDexFile = null;
    protected static HttpServer mHttpServer;
    protected static RecentUser mRecentUser;
    protected static int mRequiredService;
    protected static ShutdownReceiver mShutdownReceiver;
    protected static UsageDataUploadThread mUsageDataUploadThread;
    protected static WifiReceiver mWifiReceiver;
    private static boolean mbDebugOn = false;
    private static boolean mbDebugTested = false;
    protected static boolean mbDisableStateSave = false;
    protected static boolean mbUseSSL = false;

    public void onCreate() {
        super.onCreate();
        boolean bPreviousStateLoaded = false;
        App.instance().setApplicationContext(this);
        App.instance().loadModules();
        EventBus.getDefault().register(this);
        try {
            if (getPackageManager().getPackageInfo(getPackageName(), 64).signatures[0].toCharsString().equalsIgnoreCase(Const.MyiAppSign)) {
                ReleaseBuild = true;
                DEBUG = false;
            } else {
                DEBUG = true;
                ReleaseBuild = false;
            }
            EncryptedBuild = false;
            String[] fileList = getApplicationContext().getAssets().list("");
            int i = 0;
            while (i < fileList.length) {
                if (fileList[i].contains("ijiami") || fileList[i].contains("ijm")) {
                    EncryptedBuild = true;
                    DEBUG = false;
                    break;
                }
                i++;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        if (getResources().getBoolean(R.bool.isTablet)) {
            Log.i(TAG, "Tablet mode.");
            PhoneMode = false;
        } else {
            Log.i(TAG, "Phone mode.");
            PhoneMode = true;
        }
        Iconify.with(new FontAwesomeModule()).with(new NovaIconsModule());
        mBaseContext = this;
        if (mbDisableStateSave || !loadState()) {
            Log.i(TAG, "Using new state.");
            mCommonVariables = new CommonVariables();
            mCommonVariables.UserInfo = new UserInfo();
            mCommonVariables.ServerInfo = new ServerInfo();
            mCommonVariables.Session = new Session();
            mCommonVariables.ServerInfo.szServerAddress = "webservice.myi.cn:8089";
        } else {
            mCommonVariables.UserInfo.decodeLoginJson(PreferenceManager.getDefaultSharedPreferences(getBaseAppContext()).getString("OfflineLoginJson", ""));
            Log.i(TAG, "Previous state loaded.");
            bPreviousStateLoaded = true;
        }
        mRecentUser = new RecentUser();
        mDexFile = getBaseAppContext().getDir("dex", 0);
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(mBaseContext);
        if (Settings.contains("FullAccount")) {
            mCommonVariables.UserInfo.szFullAccount = Settings.getString("FullAccount", "");
            mCommonVariables.UserInfo.szUserName = Settings.getString(UserHonourFragment.USERNAME, "");
            mCommonVariables.ServerInfo.szServerAddress = Settings.getString("ServerAddress", "");
            mCommonVariables.ServerInfo.szResourceBaseURL = Settings.getString("BaseAddress", "");
            LoadExamDataThread3.setupServerAddress(mCommonVariables.ServerInfo.szServerAddress);
            LoadExamDataThread3.setUserInfo(mCommonVariables.UserInfo.szUserName, "");
            if (mCommonVariables.UserInfo.isConfigured() && mCommonVariables.ServerInfo.isConfigured()) {
                mConfigured = true;
            } else {
                mConfigured = false;
            }
        } else {
            mConfigured = false;
        }
        if (mWifiReceiver == null) {
            mWifiReceiver = new WifiReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            registerReceiver(mWifiReceiver, intentFilter);
        }
        if (mShutdownReceiver == null) {
            mShutdownReceiver = new ShutdownReceiver();
            registerReceiver(mShutdownReceiver, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"));
        }
        LoadExamDataThread3.setSessionFailListener(new OnSessionFailListener() {
            public void OnSessionFail() {
                MyiBaseApplication.getCommonVariables().Session.logOut();
                MyiBaseApplication.saveState();
            }
        });
        enableSSL();
        if (bPreviousStateLoaded) {
            Log.i(TAG, "Automatic start background service.");
            if (mCommonVariables.ServerInfo.bUseSSL) {
                enableSSL();
            } else {
                disableSSL();
            }
            if (isUseSSL()) {
                LoadExamDataThread3.setSessionID(mCommonVariables.Session.getSessionID());
            }
            Utilities.runOnUIThreadFirst(mBaseContext, new Runnable() {
                public void run() {
                    MyiBaseApplication.startBackgroundService();
                }
            });
        }
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if ("mounted".equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(new StringBuilder(String.valueOf(cachePath)).append(File.separator).append(uniqueName).toString());
    }

    public static boolean getWakeUpOnWifiConnect() {
        return WakeUpOnWifiConnected;
    }

    public static boolean isDebugOn() {
        if (!mbDebugTested) {
            mbDebugOn = mCommonVariables.UserInfo.checkPermission(Features.PERMISSION_DEBUG);
            mbDebugTested = true;
        }
        return mbDebugOn;
    }

    public static MJpegServer createMJpegServer() {
        return mCommonVariables.MyiApplication.createScreenCaptureServer();
    }

    public static void startBackgroundService() {
        mRequiredService = mCommonVariables.MyiApplication.getRequiredService();
        mRecentUser.init();
        LoadExamDataThread3.setupServerAddress(mCommonVariables.ServerInfo.szServerAddress);
        LoadExamDataThread3.setUserInfo(mCommonVariables.UserInfo.szUserName, "");
        DataSynchronizeEngine.setClientID(mCommonVariables.MyiApplication.getClientID());
        VirtualNetworkObject.initEngines(mBaseContext);
        VirtualNetworkObject.getDataSynchronizeEngine().setDownloadTickCount(-1);
        DrawComponent.registerAvailableGraphics();
        BlueToothPen BlueToothPen = new BlueToothPen();
        mUsageDataUploadThread = new UsageDataUploadThread();
        mUsageDataUploadThread.start();
        if ((mRequiredService & 1) == 1) {
            String[] arrBlockedModules = mCommonVariables.MyiApplication.getBlockedModules();
            if ((arrBlockedModules == null || !Utilities.isInArray(arrBlockedModules, IMService.class.getName())) && !VirtualNetworkObject.getOfflineMode()) {
                Intent imService = new Intent(mBaseContext, IMService.class);
                imService.setAction("init");
                String szProtocol = "http://";
                if (isUseSSL()) {
                    szProtocol = "https://";
                }
                imService.putExtra("listenUrl", new StringBuilder(String.valueOf(szProtocol)).append(mCommonVariables.ServerInfo.szServerAddress).append("/WaitResponse?clientid=").append(mCommonVariables.MyiApplication.getClientID()).append("&version=").append(mCommonVariables.MyiApplication.getAppName()).append("&enablehistroy=on&").append("&sessionid=").append(mCommonVariables.Session.getSessionID()).append("&alias=").append(mCommonVariables.UserInfo.getClassesGUIDs("_Class_")).append("_UserGUID_").append(mCommonVariables.UserInfo.szUserGUID).toString());
                imService.putExtra("postUrl", new StringBuilder(String.valueOf(szProtocol)).append(mCommonVariables.ServerInfo.szServerAddress).append("/SendResponse?sessionid=").append(mCommonVariables.Session.getSessionID()).append("&clientid=").toString());
                imService.putExtra("from", mCommonVariables.MyiApplication.getClientID());
                mBaseContext.startService(imService);
            }
        }
        if ((mRequiredService & 2) == 2) {
            if (UI.ScreenJpegServer == null) {
                UI.ScreenJpegServer = createMJpegServer();
                UI.ScreenJpegServer.SetSendOnlyDiff(true);
                UI.ScreenJpegServer.start();
                Log.i(TAG, "MJPEG server start.");
            } else {
                Log.i(TAG, "MJPEG server already running.");
            }
        }
        if ((mRequiredService & 4) == 4 && mHttpServer == null) {
            try {
                mHttpServer = new HttpServer();
            } catch (IOException e) {
                mHttpServer = null;
                e.printStackTrace();
            }
        }
        mCommonVariables.MyiApplication.startAppBackgroundService();
    }

    public static void stopBackgroundService() {
        DrawComponent.unregisterGraphics();
        LoadExamDataThread3.cancelAndWaitAll();
        mRecentUser.clear();
        IMService.hideChatNotifyBar();
        ChatComponent.shutdown();
        StandOutWindow.closeAll(mBaseContext, ChatWindow.class);
        StatusBarDisplayer.shutdownAll();
        if (mUsageDataUploadThread != null) {
            mUsageDataUploadThread.stopThread();
            mUsageDataUploadThread = null;
        }
        if ((mRequiredService & 1) == 1) {
            IMService.hideChatNotifyBar();
            mBaseContext.stopService(new Intent(mBaseContext, IMService.class));
        }
        if ((mRequiredService & 2) == 2 && UI.ScreenJpegServer != null) {
            UI.ScreenJpegServer.Stop();
            UI.ScreenJpegServer = null;
        }
        if ((mRequiredService & 4) == 4) {
            mCommonVariables.MyiApplication.stopAppBackgroundService();
            VirtualNetworkObject.shutDown();
            mCommonVariables.ServerInfo.ServerConfiguration = new ServerConfigurationParser();
        } else {
            mCommonVariables.MyiApplication.stopAppBackgroundService();
            VirtualNetworkObject.shutDown();
            mCommonVariables.ServerInfo.ServerConfiguration = new ServerConfigurationParser();
        }
    }

    public static void enableSSL() {
        if (!mbUseSSL) {
            SSLConnection.allowAllSSL();
            mbUseSSL = true;
            mCommonVariables.ServerInfo.bUseSSL = true;
        }
    }

    public static void disableSSL() {
    }

    public static boolean isUseSSL() {
        return mbUseSSL;
    }

    public static File getDexPath() {
        return mDexFile;
    }

    public static Context getBaseAppContext() {
        return mBaseContext;
    }

    public static CommonVariables getCommonVariables() {
        return mCommonVariables;
    }

    public static RecentUser getRecentUser() {
        return mRecentUser;
    }

    public static boolean isRootRequired() {
        return mCommonVariables.MyiApplication.isAppRootRequired();
    }

    public static boolean canExecuteIPTableScript() {
        if ((mCommonVariables.MyiApplication.getMDMFlags() & 1) == 1) {
            return true;
        }
        return false;
    }

    public static boolean isConfigured() {
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(mBaseContext);
        if (Settings.contains("FullAccount")) {
            if (mCommonVariables.UserInfo.szFullAccount.isEmpty()) {
                mCommonVariables.UserInfo.szFullAccount = Settings.getString("FullAccount", "");
                mCommonVariables.UserInfo.szUserName = Settings.getString(UserHonourFragment.USERNAME, "");
                mCommonVariables.ServerInfo.szServerAddress = Settings.getString("ServerAddress", "");
                mCommonVariables.ServerInfo.szResourceBaseURL = Settings.getString("BaseAddress", "");
                LoadExamDataThread3.setupServerAddress(mCommonVariables.ServerInfo.szServerAddress);
                LoadExamDataThread3.setUserInfo(mCommonVariables.UserInfo.szUserName, "");
            }
            if (mCommonVariables.UserInfo.isConfigured() && mCommonVariables.ServerInfo.isConfigured()) {
                mConfigured = true;
            } else {
                mConfigured = false;
            }
        } else {
            mConfigured = false;
        }
        return mConfigured;
    }

    public static void startMainActivity() {
        mCommonVariables.MyiApplication.startAppMainActivity();
    }

    public static void startLogout() {
        mCommonVariables.MyiApplication.startAppLogout();
    }

    public static boolean isLoggedIn() {
        return mCommonVariables.Session.isLoggedIn();
    }

    public static void startConfigActivity() {
        Intent intent = new Intent(mBaseContext, WifiConfigActivity.class);
        intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        mBaseContext.startActivity(intent);
    }

    public static String getProtocol() {
        if (mbUseSSL) {
            return "https";
        }
        return HttpHost.DEFAULT_SCHEME_NAME;
    }

    public static void saveState() {
        Gson gson4Expose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().setPrettyPrinting().create();
        String szFileName = new StringBuilder(String.valueOf(mBaseContext.getCacheDir().getAbsolutePath())).append("/state.txt").toString();
        if (mCommonVariables.Session.isLoggedIn()) {
            Utilities.writeTextToFile(szFileName, gson4Expose.toJson(mCommonVariables));
        } else {
            cleanState();
        }
    }

    public static void cleanState() {
        new File(new StringBuilder(String.valueOf(mBaseContext.getCacheDir().getAbsolutePath())).append("/state.txt").toString()).delete();
    }

    public static boolean loadState() {
        String szFileName = new StringBuilder(String.valueOf(mBaseContext.getCacheDir().getAbsolutePath())).append("/state.txt").toString();
        if (!new File(szFileName).exists()) {
            return false;
        }
        mCommonVariables = (CommonVariables) new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().setPrettyPrinting().create().fromJson(Utilities.readTextFile(szFileName), CommonVariables.class);
        if (!ReleaseBuild) {
            Toast.makeText(mBaseContext, "已自动恢复上次的状态", 0).show();
        }
        return true;
    }

    @Subscribe
    public void onSubscriberExceptionEvent(SubscriberExceptionEvent e) {
        Log.d(TAG, "SubscriberExceptionEvent happen.");
        e.throwable.printStackTrace();
    }
}
