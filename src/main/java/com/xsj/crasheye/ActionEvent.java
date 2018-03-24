package com.xsj.crasheye;

import android.content.Context;
import com.xsj.crasheye.Properties.RemoteSettingsProps;
import com.xsj.crasheye.log.Logger;
import com.xsj.crasheye.pushstrategy.DateRefreshStrategy;
import com.xsj.crasheye.util.Utils;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionEvent extends BaseDTO implements InterfaceDataType {
    protected static String savedSessionID = "";
    protected long duration = -1;
    protected Integer eventLevel = null;
    protected String eventName = "";
    protected String session_id = "";

    public /* bridge */ /* synthetic */ JSONObject getBasicDataFixtureJson() {
        return super.getBasicDataFixtureJson();
    }

    public ActionEvent(EnumActionType type, String eventName, Integer level, HashMap<String, Object> customData) {
        super(type, customData);
        this.eventName = eventName;
        this.eventLevel = level;
        if (type == EnumActionType.ping) {
            this.session_id = Utils.getRandomSessionNumber();
            savedSessionID = this.session_id;
        } else if (type == EnumActionType.gnip) {
            this.session_id = savedSessionID;
        }
    }

    public static final ActionEvent createEvent(String eventName) {
        return new ActionEvent(EnumActionType.event, eventName, Integer.valueOf(Utils.convertLoggingLevelToInt(CrasheyeLogLevel.Verbose)), null);
    }

    public static final ActionEvent createEvent(String eventName, CrasheyeLogLevel level, HashMap<String, Object> customData) {
        return new ActionEvent(EnumActionType.event, eventName, Integer.valueOf(Utils.convertLoggingLevelToInt(level)), customData);
    }

    public static final ActionEvent createPing() {
        ActionEvent eventPing = new ActionEvent(EnumActionType.ping, null, null, null);
        Properties.lastPingTime = eventPing.timestampMilis.longValue();
        return eventPing;
    }

    public static final ActionEvent createGnip() {
        ActionEvent eventGnip = new ActionEvent(EnumActionType.gnip, null, null, null);
        eventGnip.duration = eventGnip.timestampMilis.longValue() - Properties.lastPingTime;
        return eventGnip;
    }

    public String toJsonLine() {
        JSONObject json = getBasicDataFixtureJson();
        try {
            json.remove("isservice");
        } catch (Exception e) {
        }
        try {
            if (this.duration != -1) {
                json.put("ses_duration", this.duration);
            }
            if (this.eventName != null) {
                json.put("event_name", this.eventName);
            }
            if (this.eventLevel != null) {
                json.put("level", this.eventLevel);
            }
            if (this.type != EnumActionType.event) {
                json.put("sessionid", this.session_id);
            }
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        return json.toString() + Properties.getSeparator(this.type);
    }

    public void send(Context ctx, NetSender netSender, boolean saveOnFail) {
        if (this.type.equals(EnumActionType.ping)) {
            RemoteSettingsData remoteData = RemoteSettings.convertJsonToRemoteSettings(netSender.sendBlocking(null, toJsonLine(), saveOnFail, true).getServerResponse());
            if (remoteData == null) {
                Logger.logInfo("send return RemoteData is null, revert send report host!");
                RemoteSettings.revertAndLoadSendReoprtHost(ctx);
                return;
            } else if ((remoteData.actionSpan.intValue() >= 1 && remoteData.actionSpan.intValue() <= 23) || remoteData.actionSpan.intValue() == -1) {
                if (remoteData.actionCounts.intValue() <= 0 && remoteData.actionCounts.intValue() != -1) {
                    return;
                }
                if (((remoteData.actionHost.intValue() >= 0 && remoteData.actionHost.intValue() <= 99) || remoteData.actionHost.intValue() == -1) && RemoteSettings.saveAndLoadRemoteSettings(ctx, remoteData)) {
                    DateRefreshStrategy.getInstance().updataRecordStartDate(Utils.getTimeForLong());
                    DateRefreshStrategy.getInstance().saveRecordStartDate(ctx);
                    return;
                }
                return;
            } else {
                return;
            }
        }
        netSender.send(toJsonLine(), saveOnFail);
    }

    public void save(BaseDataSaver dataSaver) {
        if (this.eventLevel == null) {
            dataSaver.save(toJsonLine());
        } else if (this.eventLevel.intValue() >= RemoteSettingsProps.eventLevel.intValue()) {
            dataSaver.save(toJsonLine());
        } else {
            Logger.logInfo("Event's level is lower than the minimum level from Remote Settings, event will not be saved");
        }
    }

    public void send(NetSender netSender, boolean saveOnFail) {
        netSender.send(toJsonLine(), saveOnFail);
    }
}
