package com.foxit.uiextensions.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import io.vov.vitamio.ThumbnailUtils;

@SuppressLint({"NewApi"})
public class AppDisplay {
    private static AppDisplay mAppDisplay = null;
    private Context mContext;
    private int mHeightPixels;
    private DisplayMetrics mMetrics;
    private boolean mPadDevice;
    private int mWidthPixels;

    public static AppDisplay getInstance(Context context) {
        if (mAppDisplay == null) {
            mAppDisplay = new AppDisplay(context);
        }
        return mAppDisplay;
    }

    public AppDisplay(Context context) {
        this.mContext = context;
        this.mMetrics = context.getResources().getDisplayMetrics();
        Log.d("AppDisplay", "DPI:" + this.mMetrics.densityDpi);
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        if (VERSION.SDK_INT < 13) {
            this.mWidthPixels = this.mMetrics.widthPixels;
            this.mHeightPixels = this.mMetrics.heightPixels;
        } else if (VERSION.SDK_INT == 13) {
            try {
                methodWidth = Display.class.getMethod("getRealWidth", new Class[0]);
                methodHeight = Display.class.getMethod("getRealHeight", new Class[0]);
                this.mWidthPixels = ((Integer) methodWidth.invoke(display, new Object[0])).intValue();
                this.mHeightPixels = ((Integer) methodHeight.invoke(display, new Object[0])).intValue();
            } catch (Exception e) {
                this.mWidthPixels = this.mMetrics.widthPixels;
                this.mHeightPixels = this.mMetrics.heightPixels;
            }
        } else if (VERSION.SDK_INT > 13 && VERSION.SDK_INT < 17) {
            try {
                methodWidth = Display.class.getMethod("getRawWidth", new Class[0]);
                methodHeight = Display.class.getMethod("getRawHeight", new Class[0]);
                this.mWidthPixels = ((Integer) methodWidth.invoke(display, new Object[0])).intValue();
                this.mHeightPixels = ((Integer) methodHeight.invoke(display, new Object[0])).intValue();
            } catch (Exception e2) {
                this.mWidthPixels = this.mMetrics.widthPixels;
                this.mHeightPixels = this.mMetrics.heightPixels;
            }
        } else if (VERSION.SDK_INT >= 17) {
            display.getRealMetrics(this.mMetrics);
            this.mWidthPixels = this.mMetrics.widthPixels;
            this.mHeightPixels = this.mMetrics.heightPixels;
        }
        float screenSize = ((float) Math.sqrt(Math.pow((double) getRawScreenWidth(), 2.0d) + Math.pow((double) getRawScreenHeight(), 2.0d))) / ((float) this.mMetrics.densityDpi);
        if (screenSize < 7.0f) {
            this.mPadDevice = false;
        } else if (screenSize < 7.0f || screenSize >= 8.0f || this.mMetrics.densityDpi >= ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT) {
            this.mPadDevice = true;
        } else {
            this.mPadDevice = false;
        }
    }

    public int dp2px(float value) {
        return (int) (((double) (this.mMetrics.density * value)) + 0.5d);
    }

    public float px2dp(float pxValue) {
        return pxValue / this.mMetrics.density;
    }

    public int getScreenWidth() {
        return this.mMetrics.widthPixels;
    }

    public int getScreenHeight() {
        return this.mMetrics.heightPixels;
    }

    public int getDialogWidth() {
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            return (getScreenHeight() * 4) / 5;
        }
        return (getScreenWidth() * 4) / 5;
    }

    public int getUITextEditDialogWidth() {
        if (isPad()) {
            if (this.mContext.getResources().getConfiguration().orientation == 2) {
                return (getScreenHeight() * 3) / 5;
            }
            return (getScreenWidth() * 3) / 5;
        } else if (this.mContext.getResources().getConfiguration().orientation == 2) {
            return (getScreenHeight() * 4) / 5;
        } else {
            return (getScreenWidth() * 4) / 5;
        }
    }

    public int getDialogHeight() {
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            return getScreenWidth() / 2;
        }
        return getScreenHeight() / 2;
    }

    public boolean isLandscape() {
        if (getScreenWidth() > getScreenHeight()) {
            return true;
        }
        return false;
    }

    public int getRawScreenWidth() {
        if (isLandscape()) {
            return Math.max(this.mWidthPixels, this.mHeightPixels);
        }
        return Math.min(this.mWidthPixels, this.mHeightPixels);
    }

    public int getRawScreenHeight() {
        if (isLandscape()) {
            return Math.min(this.mWidthPixels, this.mHeightPixels);
        }
        return Math.max(this.mWidthPixels, this.mHeightPixels);
    }

    public boolean isPad() {
        return this.mPadDevice;
    }
}
