package com.xsj.crasheye;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.http.Headers;
import android.os.Build;
import android.os.Build.VERSION;
import com.xsj.crasheye.log.Logger;
import com.xsj.crasheye.util.EnumStateStatus;
import com.xsj.crasheye.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public class Properties {
    protected static final String API_VERSION = "1";
    static String APP_CHANNELID = "NA";
    static String APP_KEY = "NA";
    public static String APP_PACKAGE = "NA";
    static String APP_VERSIONCODE = "NA";
    public static String APP_VERSIONNAME = "NA";
    public static Context AppContent = null;
    static String CARRIER = "NA";
    public static String CONNECTION = "NA";
    public static String FILES_PATH = null;
    static boolean HAS_ROOT = false;
    static EnumStateStatus IS_GPS_ON = EnumStateStatus.NA;
    static String LIB_MONOPATH = null;
    static String LOCALE = "NA";
    public static String LOG_FILTER = "";
    public static int LOG_LINES = 1000;
    static String OS_VERSION = "NA";
    static String PHONE_BRAND = null;
    static String PHONE_MODEL = "NA";
    static final String REMOTEIP_PLACEHOLDER = "{%#@@#%}";
    public static final String REST_VERSION = "1.0";
    static String SCREEN_ORIENTATION = "NA";
    static String SCREEN_SIZE = "NA";
    protected static final String SDK_PLATFORM = "Android";
    protected static final String SDK_VERSION = "2.2.2";
    static boolean SEND_LOG = false;
    static String STATE = "NA";
    static final String TAG = "Crasheye";
    public static long TIMESTAMP = 0;
    static String UID = "";
    public static final Pattern actionTypeRegx = Pattern.compile("\\{\\^1\\^([a-z]+?)\\^[0-9]+?\\}");
    static BreadcrumbsLimited breadcrumbs = new BreadcrumbsLimited();
    public static String crasheyeInitType = "NA";
    static ExtraData extraData = new ExtraData();
    public static boolean flushOnlyOverWiFi = false;
    private static boolean initialized = false;
    public static boolean isKitKat = false;
    public static long lastPingTime = 0;
    static boolean proxyEnabled = false;
    static boolean sendOnlyWiFi = false;
    public static int sessionCount = -1;
    public static ArrayList<String> transactions = new ArrayList(2);
    public static volatile TransactionsDatabase transactionsDatabase = new TransactionsDatabase();
    static String userIdentifier = "NA";

    public static class RemoteSettingsProps {
        public static Integer actionCounts = Integer.valueOf(-1);
        public static Integer actionHost = Integer.valueOf(-1);
        public static Integer actionSpan = Integer.valueOf(-1);
        static JSONObject devSettings = new JSONObject();
        static Integer eventLevel = Integer.valueOf(Utils.convertLoggingLevelToInt(CrasheyeLogLevel.Verbose));
        static String hashCode = "none";
        static Integer logLevel = Integer.valueOf(Utils.convertLoggingLevelToInt(CrasheyeLogLevel.Verbose));
        static Boolean netMonitoringEnabled = Boolean.valueOf(true);
        public static Integer sessionTime = Integer.valueOf(5);

        public static String toReadableFormat() {
            return "loglevel: " + String.valueOf(logLevel) + " eventLevel: " + String.valueOf(eventLevel) + " actionSpan: " + String.valueOf(actionSpan) + " actionCounts: " + String.valueOf(actionCounts) + " actionHost: " + String.valueOf(actionHost) + " netMonitoring: " + String.valueOf(netMonitoringEnabled) + " sessionTime: " + String.valueOf(sessionTime) + " devSettings: " + devSettings.toString() + " hashCode: " + hashCode;
        }
    }

    public static boolean isPluginInitialized() {
        if (!initialized) {
            Logger.logWarning("Crasheye SDK is not initialized!");
        }
        return initialized;
    }

    protected static boolean initialize(Context context) {
        if (!initialized) {
            AppContent = context;
            UID = UidManager.getUid(context);
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                if (APP_VERSIONNAME.equals("NA") || APP_VERSIONNAME.isEmpty()) {
                    APP_VERSIONNAME = pi.versionName;
                }
                APP_VERSIONCODE = String.valueOf(pi.versionCode);
                APP_PACKAGE = pi.packageName;
            } catch (Exception e) {
                Logger.logError("Error collecting information about the package!");
                if (Crasheye.DEBUG) {
                    e.printStackTrace();
                }
            }
            PHONE_MODEL = Build.MODEL;
            PHONE_BRAND = Build.MANUFACTURER;
            OS_VERSION = VERSION.RELEASE;
            HAS_ROOT = Utils.checkForRoot();
            if (breadcrumbs == null) {
                breadcrumbs = new BreadcrumbsLimited();
            }
            if (extraData == null) {
                extraData = new ExtraData();
            }
            if (transactionsDatabase == null) {
                transactionsDatabase = new TransactionsDatabase();
            }
            FILES_PATH = Utils.getAbsolutePath(context);
            if (FILES_PATH == null) {
                return false;
            }
            LIB_MONOPATH = Utils.getParentFilePath(context);
            RemoteSettingsData remoteSettings = RemoteSettings.loadRemoteSettings(context);
            if (remoteSettings != null) {
                RemoteSettingsProps.logLevel = remoteSettings.logLevel;
                RemoteSettingsProps.eventLevel = remoteSettings.eventLevel;
                RemoteSettingsProps.actionSpan = remoteSettings.actionSpan;
                RemoteSettingsProps.actionCounts = remoteSettings.actionCounts;
                RemoteSettingsProps.actionHost = remoteSettings.actionHost;
                RemoteSettingsProps.netMonitoringEnabled = remoteSettings.netMonitoring;
                RemoteSettingsProps.sessionTime = remoteSettings.sessionTime;
                RemoteSettingsProps.hashCode = remoteSettings.hashCode;
                try {
                    RemoteSettingsProps.devSettings = new JSONObject(remoteSettings.devSettings);
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
            initialized = true;
        }
        IS_GPS_ON = Utils.isGPSOn(context);
        LOCALE = Locale.getDefault().getCountry();
        if (LOCALE == null || LOCALE.length() == 0) {
            LOCALE = "NA";
        }
        CARRIER = Utils.getCarrier(context);
        SCREEN_ORIENTATION = Utils.getScreenOrientation(context);
        SCREEN_SIZE = Utils.getScreenSize(context);
        HashMap<String, String> conInfo = Utils.getConnectionInfo(context);
        CONNECTION = (String) conInfo.get(Headers.CONN_DIRECTIVE);
        STATE = (String) conInfo.get("state");
        APP_CHANNELID = Utils.getChannelIdByConfig(context, APP_CHANNELID);
        sessionCount = 1;
        return true;
    }

    protected static final String getSeparator(EnumActionType actionName) {
        return "{^1^" + actionName.toString() + "^" + Utils.getTime() + "}";
    }

    protected static final EnumActionType findActionType(String data) {
        Matcher m = actionTypeRegx.matcher(data);
        if (m.find()) {
            return EnumActionType.valueOf(m.group(1));
        }
        return EnumActionType.invalid;
    }
}
