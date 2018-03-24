package com.xsj.crasheye;

import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import com.xsj.crasheye.util.EnumStateStatus;
import com.xsj.crasheye.util.Utils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionError extends BaseDTO implements InterfaceDataType {
    private JSONArray breadcrumbs;
    private String crashTime;
    private String errorHash;
    private EnumStateStatus gpsStatus;
    private Boolean handled;
    private String klass;
    private String memAppAvailable;
    private String memAppMax;
    private String memAppTotal;
    private String memSysAvailable = null;
    private String memSysLow;
    private String memSysThreshold;
    private String memSysTotal = null;
    private String message;
    private String msFromStart;
    private String stacktrace;
    private String where;

    public /* bridge */ /* synthetic */ JSONObject getBasicDataFixtureJson() {
        return super.getBasicDataFixtureJson();
    }

    public ActionError(EnumActionType dataType, String stacktrace, EnumExceptionType exceptionType, HashMap<String, Object> customData) {
        super(dataType, customData);
        this.stacktrace = stacktrace;
        if (exceptionType == EnumExceptionType.HANDLED) {
            this.handled = Boolean.valueOf(true);
        } else {
            this.handled = Boolean.valueOf(false);
        }
        HashMap<String, String> stackHashMap = StacktraceHash.manipulateStacktrace(Properties.APP_PACKAGE, stacktrace);
        this.klass = (String) stackHashMap.get("klass");
        this.message = (String) stackHashMap.get("message");
        this.errorHash = (String) stackHashMap.get("errorHash");
        this.where = (String) stackHashMap.get("where");
        this.gpsStatus = Properties.IS_GPS_ON;
        this.msFromStart = Utils.getMilisFromStart();
        MemoryInfo memoryInfo = new MemoryInfo();
        Runtime rt = Runtime.getRuntime();
        if (!this.handled.booleanValue()) {
            HashMap<String, String> memInfo = Utils.getMemoryInfo();
            this.memSysTotal = (String) memInfo.get("memTotal");
            this.memSysAvailable = (String) memInfo.get("memFree");
        }
        this.memSysThreshold = String.valueOf(((double) memoryInfo.threshold) / 1048576.0d);
        this.memSysLow = String.valueOf(memoryInfo.lowMemory);
        this.memAppMax = String.valueOf(((double) rt.maxMemory()) / 1048576.0d);
        this.memAppAvailable = String.valueOf(((double) rt.freeMemory()) / 1048576.0d);
        this.memAppTotal = String.valueOf(((double) rt.totalMemory()) / 1048576.0d);
        this.breadcrumbs = Properties.breadcrumbs.getList();
        this.crashTime = Utils.getTime();
    }

    public ActionError(EnumActionType dataType, String message, String file, String line, String stacktrace, HashMap<String, Object> map, EnumExceptionType exceptionType) {
        super(dataType, map);
        this.stacktrace = stacktrace;
        if (exceptionType == EnumExceptionType.HANDLED) {
            this.handled = Boolean.valueOf(true);
        } else {
            this.handled = Boolean.valueOf(false);
        }
        this.klass = file;
        this.message = message;
        this.errorHash = StacktraceHash.getMD5ForJavascriptError(stacktrace);
        this.where = "line: " + line;
        this.gpsStatus = Properties.IS_GPS_ON;
        this.msFromStart = Utils.getMilisFromStart();
        MemoryInfo memoryInfo = new MemoryInfo();
        Runtime rt = Runtime.getRuntime();
        if (!this.handled.booleanValue()) {
            HashMap<String, String> memInfo = Utils.getMemoryInfo();
            this.memSysTotal = (String) memInfo.get("memTotal");
            this.memSysAvailable = (String) memInfo.get("memFree");
        }
        this.memSysThreshold = String.valueOf(((double) memoryInfo.threshold) / 1048576.0d);
        this.memSysLow = String.valueOf(memoryInfo.lowMemory);
        this.memAppMax = String.valueOf(((double) rt.maxMemory()) / 1048576.0d);
        this.memAppAvailable = String.valueOf(((double) rt.freeMemory()) / 1048576.0d);
        this.memAppTotal = String.valueOf(((double) rt.totalMemory()) / 1048576.0d);
        this.breadcrumbs = Properties.breadcrumbs.getList();
        this.crashTime = Utils.getTime();
    }

    public final String getErrorHash() {
        return this.errorHash;
    }

    public String toJsonLine() {
        JSONObject json = getBasicDataFixtureJson();
        try {
            JSONObject node = new JSONObject();
            node.put("stack", this.stacktrace);
            node.put("error", this.message);
            json.put("crash", node);
            json.put("crashtime", this.crashTime);
            json.put("dumptype", EnumErrorType.java.toString());
            json.put("handled", this.handled);
            json.put("klass", this.klass);
            json.put("message", this.message);
            json.put("errorhash", this.errorHash);
            json.put("where", this.where);
            json.put("rooted", this.rooted);
            json.put("gpsstatus", this.gpsStatus.toString());
            json.put("msfromstart", this.msFromStart);
            if (this.breadcrumbs != null && this.breadcrumbs.length() > 0) {
                json.put("breadcrumbs", this.breadcrumbs);
            }
            json.put("memsyslow", this.memSysLow);
            if (!this.handled.booleanValue()) {
                json.put("memsystotal", this.memSysTotal);
                json.put("memsysavailable", this.memSysAvailable);
            }
            json.put("memsysthreshold", this.memSysThreshold);
            json.put("memappmax", this.memAppMax);
            json.put("memappavailable", this.memAppAvailable);
            json.put("memapptotal", this.memAppTotal);
            if (Properties.SEND_LOG) {
                json.put("log", Utils.readLogs());
            } else {
                json.put("log", "NA");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString() + Properties.getSeparator(this.type);
    }

    public void send(NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }

    public void save(BaseDataSaver dataSaver) {
        Utils.writeFile(CrasheyeFileFilter.createNewFile(), toJsonLine());
    }

    public void send(Context ctx, NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }
}
