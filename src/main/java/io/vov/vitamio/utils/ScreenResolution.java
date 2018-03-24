package io.vov.vitamio.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;

public class ScreenResolution {
    public static Pair<Integer, Integer> getResolution(Context ctx) {
        if (VERSION.SDK_INT >= 17) {
            return getRealResolution(ctx);
        }
        return getRealResolutionOnOldDevice(ctx);
    }

    private static Pair<Integer, Integer> getRealResolutionOnOldDevice(Context ctx) {
        try {
            Display display = ((WindowManager) ctx.getSystemService("window")).getDefaultDisplay();
            return new Pair((Integer) Display.class.getMethod("getRawWidth", new Class[0]).invoke(display, new Object[0]), (Integer) Display.class.getMethod("getRawHeight", new Class[0]).invoke(display, new Object[0]));
        } catch (Exception e) {
            DisplayMetrics disp = ctx.getResources().getDisplayMetrics();
            return new Pair(Integer.valueOf(disp.widthPixels), Integer.valueOf(disp.heightPixels));
        }
    }

    @TargetApi(17)
    private static Pair<Integer, Integer> getRealResolution(Context ctx) {
        Display display = ((WindowManager) ctx.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        return new Pair(Integer.valueOf(metrics.widthPixels), Integer.valueOf(metrics.heightPixels));
    }
}
