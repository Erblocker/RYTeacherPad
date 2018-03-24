package com.xsj.crasheye;

import android.content.Context;
import com.xsj.crasheye.Properties.RemoteSettingsProps;
import com.xsj.crasheye.log.Logger;
import org.json.JSONException;
import org.json.JSONObject;

class ActionLog extends BaseDTO implements InterfaceDataType {
    public Integer eventLevel = Integer.valueOf(2);
    public String eventName = "";

    public ActionLog(EnumActionType type, String eventName, Integer level) {
        super(type, null);
        this.eventName = eventName;
        this.eventLevel = level;
    }

    public String toJsonLine() {
        JSONObject json = getBasicDataFixtureJson();
        try {
            json.put("log_name", this.eventName);
            json.put("level", this.eventLevel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString() + Properties.getSeparator(this.type);
    }

    public void send(Context ctx, NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }

    public void save(BaseDataSaver dataSaver) {
        if (this.eventLevel == null) {
            dataSaver.save(toJsonLine());
        } else if (this.eventLevel.intValue() >= RemoteSettingsProps.logLevel.intValue()) {
            dataSaver.save(toJsonLine());
        } else {
            Logger.logInfo("Logs's level is lower than the minimum level from Remote Settings, log will not be saved");
        }
    }

    public void send(NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }
}
