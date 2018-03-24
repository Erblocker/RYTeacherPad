package com.xsj.crasheye;

import android.content.Context;
import com.xsj.crasheye.Properties.RemoteSettingsProps;
import com.xsj.crasheye.exception.ExceptionHandler;
import com.xsj.crasheye.log.Logger;
import com.xsj.crasheye.pushstrategy.DateRefreshStrategy;
import com.xsj.crasheye.util.Utils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import org.json.JSONObject;

public final class Crasheye {
    public static boolean DEBUG = true;
    public static CrasheyeCallback crasheyeCallback = null;
    private static boolean initialize = false;
    private static boolean isSessionActive = false;

    /* renamed from: com.xsj.crasheye.Crasheye$1 */
    class AnonymousClass1 implements Runnable {
        private final /* synthetic */ Context val$context;

        AnonymousClass1(Context context) {
            this.val$context = context;
        }

        public void run() {
            Crasheye.startSession(this.val$context);
            Crasheye.flush();
        }
    }

    public interface NDKExceptionCallback {
        void execute();
    }

    public static void init(Context context, String appKey) {
        if (context == null) {
            Logger.logWarning("Context is null!");
        } else if (appKey == null) {
            Logger.logWarning("appKey is null!");
        } else if (!init(context, appKey, null)) {
        }
    }

    public static void initWithNativeHandle(Context context, String appKey) {
        if (context == null) {
            Logger.logWarning("Context is null!");
        } else if (appKey == null) {
            Logger.logWarning("appKey is null!");
        } else if (init(context, appKey, null)) {
            NativeExceptionHandler.getInstance().init();
        }
    }

    public static void initWithMonoNativeHandle(Context context, String appKey) {
        if (context == null) {
            Logger.logWarning("Context is null!");
        } else if (appKey == null) {
            Logger.logWarning("appKey is null!");
        } else if (init(context, appKey, null)) {
            NativeExceptionHandler.getInstance().initWithMono();
            Properties.crasheyeInitType = "unity";
        }
    }

    public static void initWithNativeHandleUserspaceSig(Context context, String appKey) {
        if (context == null) {
            Logger.logWarning("Context is null!");
        } else if (appKey == null) {
            Logger.logWarning("appKey is null!");
        } else if (init(context, appKey, null)) {
            NativeExceptionHandler.getInstance().initWithHandleUserspaceSig();
            Properties.crasheyeInitType = "unity";
        }
    }

    private static boolean init(Context context, String appKey, String url) {
        if (context == null) {
            Logger.logWarning("Context is null!");
            return false;
        } else if (appKey == null || appKey.length() != 8 || appKey.length() > 14) {
            throw new IllegalArgumentException("Your  API Key is invalid!");
        } else if (initialize) {
            Logger.logWarning("crasheye is init...");
            return true;
        } else {
            Properties.APP_KEY = appKey;
            Properties.TIMESTAMP = System.currentTimeMillis();
            if (Properties.initialize(context)) {
                installExceptionHandler();
                new LowPriorityThreadFactory().newThread(new AnonymousClass1(context)).start();
                Logger.logInfo("init success!");
                initialize = true;
                return true;
            }
            Logger.logError("Crasheye initialize fail, Could not initialize Crasheye!");
            return false;
        }
    }

    public static void setChannelID(String YourChannelID) {
        if (YourChannelID == null) {
            Logger.logError("Set channel id is null!");
        } else {
            Properties.APP_CHANNELID = YourChannelID;
        }
    }

    public static void setAppVersion(String YourAppVersion) {
        if (YourAppVersion == null) {
            Properties.APP_VERSIONNAME = "NA";
        } else {
            Properties.APP_VERSIONNAME = YourAppVersion;
        }
    }

    public static String getSDKVersion() {
        return "2.2.2";
    }

    public static void startSession(Context context) {
        if (context == null) {
            Logger.logWarning("Context is null!");
            return;
        }
        if (!isSessionActive) {
            isSessionActive = true;
            Properties.initialize(context);
        }
        if (!Utils.shouldSendPing(context)) {
            return;
        }
        if (Utils.isRunningService(context)) {
            DateRefreshStrategy.getInstance().load(context);
            ActionEvent eve = ActionEvent.createPing();
            if (DateRefreshStrategy.getInstance().checkCanRefresh()) {
                eve.send(context, new NetSender(), true);
                return;
            }
            new AsyncDataSaver().save(eve.toJsonLine(), CrasheyeFileFilter.createSessionNewFile());
            return;
        }
        Logger.logInfo("is running in service, don't send session to server.");
    }

    public static void closeSession(Context context) {
        if (context == null) {
            Logger.logWarning("Context is null!");
        } else if (Properties.isPluginInitialized() && isSessionActive) {
            isSessionActive = false;
            ActionEvent.createGnip().save(new AsyncDataSaver());
        }
    }

    public static void flush() {
        if (!Properties.isPluginInitialized()) {
            return;
        }
        if (Utils.isRunningService(Properties.AppContent)) {
            new DataFlusher().send();
        } else {
            Logger.logInfo("is running in service, don't start data flusher send.");
        }
    }

    public static final void setCrasheyeCallback(CrasheyeCallback mintCallback) {
        if (mintCallback == null) {
            Logger.logWarning("CrasheyeCallback is null!");
        } else {
            crasheyeCallback = mintCallback;
        }
    }

    public static final void setNDKExceptionCallback(NDKExceptionCallback ndkExceiptn) {
        if (ndkExceiptn == null) {
            Logger.logWarning("NDKExceptionCallback is null!");
        } else {
            NativeExceptionHandler.ndkExceptionCallback = ndkExceiptn;
        }
    }

    public static void leaveBreadcrumb(String breadcrumb) {
        if (Properties.isPluginInitialized() && breadcrumb != null) {
            Properties.breadcrumbs.addToList(breadcrumb);
        }
    }

    public static void logView(String view) {
        if (Properties.isPluginInitialized() && view != null) {
            ActionView.logView(view).save(new AsyncDataSaver());
        }
    }

    public static void logEvent(String eventName) {
        if (Properties.isPluginInitialized() && eventName != null) {
            ActionEvent.createEvent(eventName).save(new AsyncDataSaver());
        }
    }

    public static void logEvent(String eventName, CrasheyeLogLevel logLevel) {
        if (Properties.isPluginInitialized() && eventName != null && logLevel != null) {
            ActionEvent.createEvent(eventName, logLevel, null).save(new AsyncDataSaver());
        }
    }

    public static void logEvent(String eventName, CrasheyeLogLevel logLevel, HashMap<String, Object> customData) {
        if (eventName == null) {
            Logger.logWarning("eventName is null!");
        } else if (logLevel == null) {
            Logger.logWarning("logLevel is null!");
        } else if (customData == null) {
            Logger.logWarning("customData is null!");
        } else if (Properties.isPluginInitialized()) {
            ActionEvent.createEvent(eventName, logLevel, customData).save(new AsyncDataSaver());
        }
    }

    public static void logEvent(String eventName, CrasheyeLogLevel logLevel, String keyName, String keyValue) {
        if (eventName == null) {
            Logger.logWarning("eventName is null!");
        } else if (logLevel == null) {
            Logger.logWarning("logLevel is null!");
        } else if (keyName == null) {
            Logger.logWarning("keyName is null!");
        } else if (keyValue == null) {
            Logger.logWarning("keyValue is null!");
        } else if (Properties.isPluginInitialized()) {
            HashMap<String, Object> customData = new HashMap(1);
            customData.put(keyName, keyValue);
            logEvent(eventName, logLevel, customData);
        }
    }

    public static void setUserIdentifier(String userIdentifier) {
        if (userIdentifier == null) {
            Properties.userIdentifier = "NA";
        } else {
            Properties.userIdentifier = userIdentifier;
        }
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void setFlushOnlyOverWiFi(boolean enabled) {
        Properties.flushOnlyOverWiFi = enabled;
    }

    public static HashMap<String, Object> getExtraData() {
        if (Properties.extraData == null) {
            return null;
        }
        return Properties.extraData.getExtraData();
    }

    public static void addExtraData(String key, String value) {
        if (Properties.extraData == null) {
            Properties.extraData = new ExtraData();
        }
        if (key != null) {
            if (value == null) {
                value = "null";
            }
            Properties.extraData.addExtraData(key, value);
        }
    }

    public static void addExtraDataMap(HashMap<String, Object> extras) {
        if (Properties.extraData == null) {
            Properties.extraData = new ExtraData();
        }
        if (extras != null) {
            Properties.extraData.addExtraDataMap(extras);
        }
    }

    public static void removeExtraData(String key) {
        if (Properties.extraData != null && key != null) {
            Properties.extraData.removeKey(key);
        }
    }

    public static void clearExtraData() {
        if (Properties.extraData != null) {
            Properties.extraData.clearData();
        }
    }

    public static void logException(Exception ex) {
        if (ex == null) {
            Logger.logWarning("Exception is null!");
        } else {
            logExceptionMap(new HashMap(0), ex);
        }
    }

    public static void logExceptionMap(HashMap<String, Object> customData, Exception exception) {
        if (Properties.isPluginInitialized()) {
            Writer stacktrace = new StringWriter();
            exception.printStackTrace(new PrintWriter(stacktrace));
            new ActionError(EnumActionType.error, stacktrace.toString(), EnumExceptionType.HANDLED, customData).send(new NetSender(), true);
        }
    }

    public static void sendScriptException(String errorTitle, String stacktrace) {
        if (Properties.isPluginInitialized()) {
            sendScriptException(errorTitle, stacktrace, "NA");
        }
    }

    public static void sendScriptException(String errorTitle, String stacktrace, String language) {
        if (!Properties.isPluginInitialized()) {
            return;
        }
        if (errorTitle == null) {
            Logger.logWarning("errorTitle is null!");
        } else if (stacktrace == null) {
            Logger.logWarning("stacktrace is null!");
        } else {
            String strLanguage = "NA";
            if (language != null) {
                strLanguage = language;
            }
            ScriptExceptionHanler.logScriptException(errorTitle, stacktrace, strLanguage);
        }
    }

    public static void logExceptionMessage(String key, String value, Exception exception) {
        if (!Properties.isPluginInitialized()) {
            return;
        }
        if (exception == null) {
            Logger.logWarning("exception is null!");
            return;
        }
        HashMap<String, Object> extraData = new HashMap(1);
        if (!(key == null || value == null)) {
            extraData.put(key, value);
        }
        logExceptionMap(extraData, exception);
    }

    public static void enableLogging(boolean enable) {
        Properties.SEND_LOG = enable;
    }

    public static void setLogging(int lines) {
        if (lines > 0) {
            if (lines > 1000) {
                lines = 1000;
            }
            Properties.SEND_LOG = true;
            Properties.LOG_LINES = lines;
        }
    }

    public static void setLogging(String filter) {
        if (filter != null) {
            Properties.SEND_LOG = true;
            Properties.LOG_FILTER = filter;
        }
    }

    public static void setLogging(int lines, String filter) {
        if (filter != null && lines >= 0) {
            if (lines > 1000) {
                lines = 1000;
            }
            Properties.SEND_LOG = true;
            Properties.LOG_LINES = lines;
            Properties.LOG_FILTER = filter;
        }
    }

    public static JSONObject getDevSettings() {
        return RemoteSettingsProps.devSettings;
    }

    private static void installExceptionHandler() {
        Logger.logInfo("Registering the exception handler");
        UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (!(currentHandler instanceof ExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler));
        }
    }

    public static final String getSessionId() {
        if (ActionEvent.savedSessionID != null || ActionEvent.savedSessionID.length() > 0) {
            return ActionEvent.savedSessionID;
        }
        return "NA";
    }

    public static final String getCrasheyeUUID() {
        if (Properties.UID != null || Properties.UID.length() > 0) {
            return Properties.UID;
        }
        return "NA";
    }
}
