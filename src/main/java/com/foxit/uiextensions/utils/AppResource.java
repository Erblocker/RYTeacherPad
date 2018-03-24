package com.foxit.uiextensions.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;

public class AppResource {
    public static String getString(Context context, int id) {
        return context.getString(id);
    }

    public static int getDimensionPixelSize(Context context, int id) {
        return context.getResources().getDimensionPixelSize(id);
    }

    public static float getDimension(Context context, int id) {
        return context.getResources().getDimension(id);
    }

    public static Drawable getDrawable(Context context, int id) {
        return context.getResources().getDrawable(id);
    }

    @TargetApi(21)
    public static Drawable getDrawable(Context context, int id, Theme theme) {
        return context.getResources().getDrawable(id, theme);
    }
}
