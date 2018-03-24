package com.xsj.crasheye;

import android.net.http.Headers;
import com.xsj.crasheye.util.Utils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract class BaseDTO {
    protected static final String UNKNOWN = "NA";
    protected String appKey;
    protected String appStartTime = Utils.getTime();
    protected String appVersionCode;
    protected String appVersionName;
    protected String carrier;
    protected String channelId;
    protected String connection;
    protected Long crashTime;
    protected HashMap<String, Object> customData;
    protected String device;
    protected ExtraData extraData;
    protected Boolean isRunningService;
    protected String locale;
    protected String osVersion;
    protected String packageName;
    protected String platform = "Android";
    protected String remoteIP;
    protected Boolean rooted;
    protected String screenOrientation;
    protected String screenSize;
    protected String sdkVersion = "2.2.2";
    protected int sessionCount;
    protected String state;
    protected Long timestampMilis = Long.valueOf(System.currentTimeMillis());
    protected EnumActionType type;
    protected String userIdentifier;
    protected String uuid;

    public BaseDTO(EnumActionType dataType, HashMap<String, Object> customData_) {
        this.type = dataType;
        this.appKey = Properties.APP_KEY;
        this.device = new StringBuilder(String.valueOf(Properties.PHONE_BRAND != null ? Properties.PHONE_BRAND + " " : "")).append(Properties.PHONE_MODEL).toString();
        this.osVersion = Properties.OS_VERSION;
        this.appVersionCode = Properties.APP_VERSIONCODE;
        this.appVersionName = Properties.APP_VERSIONNAME;
        this.packageName = Properties.APP_PACKAGE;
        this.locale = Properties.LOCALE;
        this.rooted = Boolean.valueOf(Properties.HAS_ROOT);
        this.uuid = Properties.UID;
        this.userIdentifier = Properties.userIdentifier;
        this.connection = Properties.CONNECTION;
        this.state = Properties.STATE;
        this.extraData = Properties.extraData;
        this.screenOrientation = Properties.SCREEN_ORIENTATION;
        this.screenSize = Properties.SCREEN_SIZE;
        this.customData = customData_;
        this.channelId = Properties.APP_CHANNELID;
        this.sessionCount = Properties.sessionCount;
        this.isRunningService = Boolean.valueOf(!Utils.isRunningService(Properties.AppContent));
    }

    public JSONObject getBasicDataFixtureJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("appstarttime", this.appStartTime);
            json.put("crashtime", this.crashTime);
            json.put("sdkversion", this.sdkVersion);
            json.put("appkey", this.appKey);
            json.put("platform", this.platform);
            json.put("device", this.device);
            json.put("osversion", this.osVersion);
            json.put("locale", this.locale);
            json.put("uuid", this.uuid);
            json.put("useridentifier", this.userIdentifier);
            json.put("carrier", this.carrier);
            json.put("appversioncode", this.appVersionCode);
            json.put("appversionname", this.appVersionName);
            json.put("packagename", this.packageName);
            json.put("netstatus", this.connection);
            json.put(Headers.CONN_DIRECTIVE, this.state);
            json.put("screenorientation", this.screenOrientation);
            json.put("screensize", this.screenSize);
            json.put("channel", this.channelId);
            json.put("sessioncount", this.sessionCount);
            json.put("isservice", this.isRunningService);
            JSONObject extraDataJson = new JSONObject();
            if (!(this.extraData == null || this.extraData.isEmpty())) {
                for (Entry<String, Object> extra : this.extraData.entrySet()) {
                    if (extra.getValue() == null) {
                        extraDataJson.put((String) extra.getKey(), "null");
                    } else {
                        extraDataJson.put((String) extra.getKey(), extra.getValue());
                    }
                }
            }
            if (!(this.customData == null || this.customData.isEmpty())) {
                for (Entry<String, Object> extra2 : this.customData.entrySet()) {
                    if (extra2.getValue() == null) {
                        extraDataJson.put((String) extra2.getKey(), "null");
                    } else {
                        extraDataJson.put((String) extra2.getKey(), extra2.getValue());
                    }
                }
            }
            json.put("extradata", extraDataJson);
            JSONArray transactions = new JSONArray();
            if (Properties.transactions != null) {
                Iterator it = Properties.transactions.iterator();
                while (it.hasNext()) {
                    transactions.put((String) it.next());
                }
            }
            json.put("transactions", transactions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
