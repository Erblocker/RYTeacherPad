package com.xsj.crasheye;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

class ActionView extends BaseDTO implements InterfaceDataType {
    public String viewName = "";

    public ActionView(EnumActionType type, String viewName) {
        super(type, null);
        this.viewName = viewName;
    }

    public static final ActionView logView(String viewName) {
        return new ActionView(EnumActionType.view, viewName);
    }

    public String toJsonLine() {
        JSONObject json = getBasicDataFixtureJson();
        try {
            if (this.viewName != null) {
                json.put("view_name", this.viewName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString() + Properties.getSeparator(EnumActionType.view);
    }

    public void send(Context ctx, NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }

    public void save(BaseDataSaver dataSaver) {
        dataSaver.save(toJsonLine());
    }

    public void send(NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }
}
