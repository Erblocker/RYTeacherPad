package com.netspace.library.consts;

import com.netspace.library.application.MyiBaseApplication;

public class Features {
    public static final String PERMISSION_CAN_SWITCH_ACCOUNTS = "can_switch_accounts";
    public static final String PERMISSION_CHAT_CLASSGROUPCHAT = "chat_classgroupchat";
    public static final String PERMISSION_DEBUG = "debug";
    public static final String PERMISSION_DISABLE_LOCALSUBMIT = "disable_local_submit";
    public static final String PERMISSION_DISABLE_NORMAL_UPGRADE = "disable_local_normal_upgrade";
    public static final String PERMISSION_DISCUSS = "resource_discuss";
    public static final String PERMISSION_DISCUSS_READ = "resource_discuss_read";
    public static final String PERMISSION_DISCUSS_WRITE = "resource_discuss_write";
    public static final String PERMISSION_EXPERMENTAL = "expermental";
    public static final String PERMISSION_H264 = "enableH264";
    public static final String PERMISSION_H264_RECEIVEONLY = "enableH264_receiveonly";
    public static final String PERMISSION_H264_TEACHERPADONLY = "enableH264_teacherpadonly";
    public static final String PERMISSION_IMAGE_ENHANCE = "enableImageEnhance";
    public static final String PERMISSION_JYEOOLIBRARY = "jyeoolibrary";
    public static final String PERMISSION_JYEOOLIBRARY_TEACHERPADONLY = "jyeoolibrary_teacherpadonly";
    public static final String PERMISSION_LESSONCLASS = "lessonclass";
    public static final String PERMISSION_LESSONCLASS_PRIVATE_ONLY = "lessonclass_private_only";
    public static final String PERMISSION_MULTICAST_PCSCREEN_BROADCAST = "enablemulticast_pc_screen_broadcast";
    public static final String PERMISSION_MYILIBRARY = "myilibrary";
    public static final String PERMISSION_MYILIBRARY_ADDFOLDER = "myilibrary_addfolder";
    public static final String PERMISSION_MYILIBRARY_ADDPICTURE = "myilibrary_addpicture";
    public static final String PERMISSION_MYILIBRARY_ADDVIDEO = "myilibrary_addvideo";
    public static final String PERMISSION_MYILIBRARY_SHARETOIM = "myilibrary_sharetoim";
    public static final String PERMISSION_MYILIBRARY_SHARETOSTUDENTS = "myilibrary_sharetostudents";
    public static final String PERMISSION_SUBJECTLEARN = "subjectlearn";
    public static final String PERMISSION_VIDEOCHAT = "videochat";
    public static final String PERMISSION_VOICECHAT = "voicechat";
    public static final String PERMISSION_ZXXKLIBRARY = "zxxklibrary";
    public static final String PERMISSION_ZXXKLIBRARY_TEACHERPADONLY = "zxxklibrary_teacherpadonly";

    public static String getName(String szPermissionKey) {
        int resId = MyiBaseApplication.getBaseAppContext().getResources().getIdentifier(szPermissionKey, "string", MyiBaseApplication.getBaseAppContext().getPackageName());
        if (resId == 0) {
            return null;
        }
        return MyiBaseApplication.getBaseAppContext().getString(resId);
    }
}
