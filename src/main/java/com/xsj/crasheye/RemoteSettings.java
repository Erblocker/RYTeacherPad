package com.xsj.crasheye;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.xsj.crasheye.Properties.RemoteSettingsProps;
import com.xsj.crasheye.log.Logger;
import org.json.JSONException;
import org.json.JSONObject;

class RemoteSettings {
    RemoteSettings() {
    }

    protected static final RemoteSettingsData convertJsonToRemoteSettings(String jsonData) {
        if (jsonData == null || jsonData.length() < 1) {
            return null;
        }
        RemoteSettingsData rsd = new RemoteSettingsData();
        try {
            JSONObject rdjson = new JSONObject(jsonData);
            JSONObject settings = rdjson.optJSONObject("remSetVer1");
            if (settings != null) {
                rsd.logLevel = Integer.valueOf(settings.optInt("logLevel"));
                rsd.eventLevel = Integer.valueOf(settings.getInt("eventLevel"));
                rsd.netMonitoring = Boolean.valueOf(settings.optBoolean("netMonitoring"));
                rsd.sessionTime = Integer.valueOf(settings.optInt("sessionTime"));
                rsd.devSettings = settings.optJSONObject("devSettings").toString();
                rsd.hashCode = settings.optString("hash");
            }
            JSONObject settingdata = rdjson.optJSONObject("data");
            if (settingdata == null) {
                return rsd;
            }
            rsd.actionSpan = Integer.valueOf(settingdata.optInt("st"));
            rsd.actionCounts = Integer.valueOf(settingdata.optInt("sc"));
            rsd.actionHost = Integer.valueOf(settingdata.optInt("sr"));
            return rsd;
        } catch (JSONException e) {
            Logger.logError("Could not convert json to remote data");
            return null;
        } catch (Exception e2) {
            Logger.logError("convert Json To Remote Settings Error");
            return null;
        }
    }

    protected static final boolean saveAndLoadRemoteSettings(Context ctx, RemoteSettingsData rsd) {
        SharedPreferences preferences = ctx.getSharedPreferences("REMOTESETTINGSSETTINGS", 0);
        if (preferences == null) {
            return false;
        }
        Editor editor = preferences.edit();
        if (editor == null) {
            return false;
        }
        if (rsd.logLevel != null && rsd.logLevel.intValue() > 0) {
            editor.putInt("logLevel", rsd.logLevel.intValue());
            RemoteSettingsProps.logLevel = rsd.logLevel;
        }
        if (rsd.eventLevel != null && rsd.eventLevel.intValue() > 0) {
            editor.putInt("eventLevel", rsd.eventLevel.intValue());
            RemoteSettingsProps.eventLevel = rsd.eventLevel;
        }
        if (rsd.netMonitoring != null) {
            editor.putBoolean("netMonitoring", rsd.netMonitoring.booleanValue());
            RemoteSettingsProps.netMonitoringEnabled = rsd.netMonitoring;
        }
        if (rsd.sessionTime != null && rsd.sessionTime.intValue() > 0) {
            editor.putInt("sessionTime", rsd.sessionTime.intValue());
            RemoteSettingsProps.sessionTime = rsd.sessionTime;
        }
        if (rsd.devSettings != null) {
            editor.putString("devSettings", rsd.devSettings);
            try {
                RemoteSettingsProps.devSettings = new JSONObject(rsd.devSettings);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (rsd.hashCode != null && rsd.hashCode.length() > 1) {
            editor.putString("hashCode", rsd.hashCode);
            RemoteSettingsProps.hashCode = rsd.hashCode;
        }
        if (rsd.actionSpan != null && rsd.actionSpan.intValue() >= 1 && rsd.actionSpan.intValue() <= 23) {
            editor.putInt("actionSpan", rsd.actionSpan.intValue());
            RemoteSettingsProps.actionSpan = rsd.actionSpan;
        }
        if (rsd.actionCounts != null && rsd.actionCounts.intValue() > 0) {
            editor.putInt("actionCounts", rsd.actionCounts.intValue());
            RemoteSettingsProps.actionCounts = rsd.actionCounts;
        }
        if (rsd.actionHost != null && rsd.actionHost.intValue() >= 0 && rsd.actionHost.intValue() <= 99) {
            editor.putInt("actionHost", rsd.actionHost.intValue());
            RemoteSettingsProps.actionHost = rsd.actionHost;
        }
        return editor.commit();
    }

    protected static final boolean revertAndLoadSendReoprtHost(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences("REMOTESETTINGSSETTINGS", 0);
        if (preferences == null) {
            return false;
        }
        Editor editor = preferences.edit();
        if (editor == null) {
            return false;
        }
        editor.putInt("actionHost", -1);
        RemoteSettingsProps.actionHost = Integer.valueOf(-1);
        return editor.commit();
    }

    protected static final RemoteSettingsData loadRemoteSettings(Context ctx) {
        if (ctx == null) {
            return null;
        }
        RemoteSettingsData rsd = new RemoteSettingsData();
        SharedPreferences preferences = ctx.getSharedPreferences("REMOTESETTINGSSETTINGS", 0);
        if (preferences == null) {
            return null;
        }
        try {
            rsd.logLevel = Integer.valueOf(preferences.getInt("logLevel", RemoteSettingsProps.logLevel.intValue()));
            rsd.eventLevel = Integer.valueOf(preferences.getInt("eventLevel", RemoteSettingsProps.eventLevel.intValue()));
            rsd.actionSpan = Integer.valueOf(preferences.getInt("actionSpan", RemoteSettingsProps.actionSpan.intValue()));
            rsd.actionCounts = Integer.valueOf(preferences.getInt("actionCounts", RemoteSettingsProps.actionCounts.intValue()));
            rsd.actionHost = Integer.valueOf(preferences.getInt("actionHost", RemoteSettingsProps.actionHost.intValue()));
            rsd.netMonitoring = Boolean.valueOf(preferences.getBoolean("netMonitoring", RemoteSettingsProps.netMonitoringEnabled.booleanValue()));
            rsd.sessionTime = Integer.valueOf(preferences.getInt("sessionTime", RemoteSettingsProps.sessionTime.intValue()));
            rsd.devSettings = preferences.getString("devSettings", RemoteSettingsProps.devSettings.toString());
            rsd.hashCode = preferences.getString("hashCode", RemoteSettingsProps.hashCode);
            return rsd;
        } catch (Exception ex) {
            Logger.logInfo("load remote settings error:" + ex.getMessage());
            return rsd;
        }
    }
}
