package com.xsj.crasheye;

import com.xsj.crasheye.util.Utils;
import java.nio.ByteBuffer;

class CrasheyeUrls {
    public static final String baseURL = Utils.getSendReportUrl();

    CrasheyeUrls() {
    }

    public static String getURL(EnumActionType actiontype, String postData) {
        StringBuilder settingsUrl = new StringBuilder();
        settingsUrl.append(baseURL);
        String sendType = "";
        if (actiontype == EnumActionType.ping) {
            settingsUrl.append("/session?appkey=");
            sendType = "session";
        } else if (actiontype == EnumActionType.error || actiontype == EnumActionType.ndkerror) {
            settingsUrl.append("/crash?appkey=");
            sendType = "crash";
        }
        settingsUrl.append(Properties.APP_KEY);
        settingsUrl.append("&uid=");
        settingsUrl.append(Properties.UID);
        settingsUrl.append("&sig=");
        settingsUrl.append(getPostSig(sendType, Properties.APP_KEY, Properties.UID, postData));
        return settingsUrl.toString();
    }

    public static String getURL(EnumActionType actiontype, byte[] postData) {
        StringBuilder settingsUrl = new StringBuilder();
        settingsUrl.append(baseURL);
        String sendType = "";
        if (actiontype == EnumActionType.ping) {
            settingsUrl.append("/session?appkey=");
            sendType = "session";
        } else if (actiontype == EnumActionType.error || actiontype == EnumActionType.ndkerror) {
            settingsUrl.append("/crash?appkey=");
            sendType = "crash";
        }
        settingsUrl.append(Properties.APP_KEY);
        settingsUrl.append("&uid=");
        settingsUrl.append(Properties.UID);
        settingsUrl.append("&sig=");
        settingsUrl.append(getPostSig(sendType, Properties.APP_KEY, Properties.UID, postData));
        return settingsUrl.toString();
    }

    private static String getPostSig(String httpPostType, String appKey, String uid, String postJsonData) {
        String md5Result = "";
        try {
            md5Result = Utils.MD5(String.format("_uri=%s&appkey=%s&uid=%s&request_body=%s", new Object[]{httpPostType, appKey, uid, postJsonData}));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5Result;
    }

    private static String getPostSig(String httpPostType, String appKey, String uid, byte[] postJsonData) {
        String encallUrl = String.format("_uri=%s&appkey=%s&uid=%s&request_body=", new Object[]{httpPostType, appKey, uid});
        ByteBuffer urlData = ByteBuffer.allocate(encallUrl.length() + postJsonData.length);
        urlData.put(encallUrl.getBytes());
        urlData.put(postJsonData);
        String md5Result = "";
        try {
            md5Result = Utils.MD5(urlData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5Result;
    }
}
