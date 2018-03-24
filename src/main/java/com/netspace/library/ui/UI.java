package com.netspace.library.ui;

import android.app.Activity;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import com.netspace.library.servers.MJpegServer;
import com.netspace.library.service.ScreenRecorderService;
import java.lang.ref.WeakReference;

public class UI {
    public static MJpegServer ScreenJpegServer = null;
    private static WeakReference<Activity> mCurrentActivity;
    public static Intent mLockedIntent;
    public static OnClickListener mOnAirplayButton = null;
    private static WeakReference<Activity> mPreviousActivity;
    private static boolean mScreenLocked = false;
    public static int mSynchronizeIcon = 17301581;
    private static boolean mTimeLocked = false;

    public static void setLockedIntent(Intent intent) {
        mLockedIntent = intent;
    }

    public static Intent getLockedIntent() {
        return mLockedIntent;
    }

    public static void setCurrentActivity(Activity Activity) {
        mPreviousActivity = mCurrentActivity;
        if (Activity != null) {
            mCurrentActivity = new WeakReference(Activity);
        } else {
            mCurrentActivity = null;
        }
        if (ScreenRecorderService.isActive()) {
            ScreenRecorderService.getService().registerActivity(Activity);
        }
    }

    public static Activity getPreviousActivity() {
        if (mPreviousActivity != null) {
            return (Activity) mPreviousActivity.get();
        }
        return null;
    }

    public static Activity getCurrentActivity() {
        if (mCurrentActivity != null) {
            return (Activity) mCurrentActivity.get();
        }
        return null;
    }

    public static boolean isScreenLocked() {
        return mScreenLocked;
    }

    public static void setScreenLocked(boolean bScreenLocked) {
        mScreenLocked = bScreenLocked;
    }

    public static boolean isTimeLocked() {
        return mTimeLocked;
    }

    public static void setTimeLocked(boolean bTimeLocked) {
        mTimeLocked = bTimeLocked;
    }
}
