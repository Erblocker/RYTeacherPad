package com.xsj.crasheye;

import android.content.Context;
import com.xsj.crasheye.util.EnumStateStatus;
import com.xsj.crasheye.util.Utils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ActionScriptError extends BaseDTO implements InterfaceDataType {
    private JSONArray breadcrumbs = Properties.breadcrumbs.getList();
    private int count;
    private EnumStateStatus gpsStatus = Properties.IS_GPS_ON;
    private Boolean handled = Boolean.valueOf(true);
    private String language;
    private String message;
    private String msFromStart = Utils.getMilisFromStart();
    private String stacktrace;

    public ActionScriptError(String message, String stacktrace, String language, int count, HashMap<String, Object> customData) {
        super(EnumActionType.error, customData);
        this.message = message;
        this.stacktrace = stacktrace;
        this.language = language;
        this.count = count;
    }

    public String toJsonLine() {
        JSONObject json = getBasicDataFixtureJson();
        try {
            JSONObject node = new JSONObject();
            node.put("stack", this.stacktrace);
            node.put("error", this.message);
            json.put("crash", node);
            json.put("screensize", this.screenSize);
            json.put("dumptype", EnumErrorType.script.toString());
            json.put("handled", this.handled);
            json.put("rooted", this.rooted);
            json.put("count", this.count);
            json.put("language", this.language);
            json.put("gpsstatus", this.gpsStatus.toString());
            json.put("msfromstart", this.msFromStart);
            if (this.breadcrumbs != null && this.breadcrumbs.length() > 0) {
                json.put("breadcrumbs", this.breadcrumbs);
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
        new AsyncDataSaver().save(toJsonLine());
    }

    public void send(Context ctx, NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }
}
