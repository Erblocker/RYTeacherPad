package com.foxit.app.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;
import android.view.WindowManager.LayoutParams;
import com.foxit.app.App;
import com.foxit.home.R;

@TargetApi(4)
public class AppTheme {
    public static int getDialogTheme() {
        if (VERSION.SDK_INT >= 21) {
            return 16973941;
        }
        if (VERSION.SDK_INT >= 14) {
            return 16974132;
        }
        if (VERSION.SDK_INT >= 11) {
            return 16973941;
        }
        return R.style.rv_dialog_style;
    }

    public static void setThemeNoTitle(Activity activity) {
        activity.requestWindowFeature(1);
    }

    public static void setThemeNeedMenuKey(Activity activity) {
        if (VERSION.SDK_INT >= 22) {
            try {
                LayoutParams.class.getField("needsMenuKey").setInt(activity.getWindow().getAttributes(), LayoutParams.class.getField("NEEDS_MENU_SET_TRUE").getInt(null));
                return;
            } catch (Exception e) {
                return;
            }
        }
        try {
            activity.getWindow().addFlags(LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
        } catch (Exception e2) {
        }
    }

    public static void setThemeFullScreen(Activity activity) {
        activity.requestWindowFeature(1);
        activity.getWindow().setFlags(1024, 1024);
    }

    @SuppressLint({"NewApi"})
    public static int getThemeFullScreen() {
        if (VERSION.SDK_INT >= 21) {
            return 16974065;
        }
        if (VERSION.SDK_INT >= 14) {
            return 16974125;
        }
        if (VERSION.SDK_INT >= 11) {
            return 16974065;
        }
        return 16973838;
    }

    public static int getThemeNoTitle() {
        if (VERSION.SDK_INT >= 21) {
            return 16974064;
        }
        if (VERSION.SDK_INT >= 14) {
            return 16974124;
        }
        if (VERSION.SDK_INT >= 11) {
            return 16974064;
        }
        return 16973837;
    }

    public static void showMenu(Context context) {
        if (VERSION.SDK_INT >= 21 && (context.getResources().getConfiguration().screenLayout & 15) == 4) {
            context.getApplicationInfo().targetSdkVersion = 10;
            App.instance().getThreadManager().getMainThreadHandler().postDelayed(new Runnable() {
                public void run() {
                    App.instance().getApplicationContext().getApplicationInfo().targetSdkVersion = VERSION.SDK_INT;
                }
            }, 200);
        }
    }
}
