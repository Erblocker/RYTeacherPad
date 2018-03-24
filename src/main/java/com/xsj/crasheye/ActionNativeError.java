package com.xsj.crasheye;

import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.util.Base64;
import com.xsj.crasheye.util.EnumStateStatus;
import com.xsj.crasheye.util.Utils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ActionNativeError extends BaseDTO implements InterfaceDataType {
    private JSONArray breadcrumbs;
    private String dumpFile;
    private String dumptype;
    private EnumStateStatus gpsStatus;
    private Boolean handled;
    private Boolean lastNotSave;
    private String memAppAvailable;
    private String memAppMax;
    private String memAppTotal;
    private String memSysAvailable;
    private String memSysLow;
    private String memSysThreshold;
    private String memSysTotal;
    private String msFromStart;
    private String nativefile;
    private String sigName;
    private String stacktrace;

    public ActionNativeError(String sigName, String stacktrace) {
        super(EnumActionType.ndkerror, null);
        this.stacktrace = "";
        this.memSysTotal = null;
        this.memSysAvailable = null;
        this.lastNotSave = Boolean.valueOf(false);
        this.stacktrace = stacktrace;
        this.handled = Boolean.valueOf(false);
        this.dumptype = EnumErrorType.unityndk.toString();
        this.sigName = sigName;
        this.stacktrace = stacktrace;
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
        this.extraData = Properties.extraData;
    }

    public ActionNativeError(String dumpFile) {
        super(EnumActionType.ndkerror, null);
        this.stacktrace = "";
        this.memSysTotal = null;
        this.memSysAvailable = null;
        this.lastNotSave = Boolean.valueOf(false);
        this.dumptype = EnumErrorType.ndk.toString();
        this.handled = Boolean.valueOf(false);
        this.dumpFile = dumpFile;
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
    }

    public void SetNativeCrashData(String dumpFile) {
        try {
            byte[] bytes = new byte[((int) new File(dumpFile).length())];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(dumpFile));
            buf.read(bytes, 0, bytes.length);
            buf.close();
            this.nativefile = Base64.encodeToString(bytes, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetLastUnSave() {
        this.lastNotSave = Boolean.valueOf(true);
    }

    public JSONObject toJson() {
        JSONObject json = getBasicDataFixtureJson();
        try {
            JSONObject node = new JSONObject();
            node.put("stack", this.stacktrace);
            node.put("error", this.sigName);
            node.put("dumpfile", this.dumpFile);
            node.put("file", this.nativefile);
            json.put("crash", node);
            json.put("dumptype", this.dumptype);
            json.put("handled", this.handled);
            json.put("rooted", this.rooted);
            if (this.lastNotSave.booleanValue()) {
                json.remove("extradata");
                json.remove("transactions");
            } else {
                json.put("gpsstatus", this.gpsStatus.toString());
                json.put("msfromstart", this.msFromStart);
                if (this.breadcrumbs != null && this.breadcrumbs.length() > 0) {
                    json.put("breadcrumbs", this.breadcrumbs);
                }
                json.put("memsysLow", this.memSysLow);
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String toJsonLine() {
        return toJson().toString();
    }

    public void send(NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }

    public void save(BaseDataSaver dataSaver) {
        new NativeExceptionDataSaver().save(toJsonLine());
    }

    public void send(Context ctx, NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }
}
