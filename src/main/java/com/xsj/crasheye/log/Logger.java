package com.xsj.crasheye.log;

import android.util.Log;
import com.xsj.crasheye.Crasheye;

public class Logger {
    public static void logInfo(String string) {
        if (Crasheye.DEBUG && string != null) {
            Log.i("Crasheye", string);
        }
    }

    public static void logWarning(String string) {
        if (string != null) {
            Log.w("Crasheye", string);
        }
    }

    public static void logError(String string) {
        if (string != null) {
            Log.e("Crasheye", string);
        }
    }
}
