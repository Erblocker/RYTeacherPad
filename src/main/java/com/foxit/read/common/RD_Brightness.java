package com.foxit.read.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.view.WindowManager.LayoutParams;
import com.foxit.app.event.LifecycleEventListener;
import com.foxit.home.R;
import com.foxit.read.ILifecycleEventListener;
import com.foxit.read.IRD_StateChangeListener;
import com.foxit.read.RD_Read;
import com.foxit.uiextensions.Module;
import com.foxit.view.propertybar.IML_MultiLineBar;
import com.foxit.view.propertybar.IML_MultiLineBar.IML_ValueChangeListener;

public class RD_Brightness implements Module {
    private int mBrightnessSeekValue = 3;
    IML_ValueChangeListener mBrightnessSeekValueChangeListener = new IML_ValueChangeListener() {
        public void onValueChanged(int type, Object value) {
            if (type == 1) {
                RD_Brightness.this.mBrightnessSeekValue = ((Integer) value).intValue();
                if (!RD_Brightness.this.mLinkToSystem) {
                    if (RD_Brightness.this.mBrightnessSeekValue <= 1) {
                        RD_Brightness.this.mBrightnessSeekValue = 1;
                    }
                    RD_Brightness.this.setManualBrightness();
                }
            }
        }

        public void onDismiss() {
            if (RD_Brightness.this.mBrightnessSeekValue < 1) {
                RD_Brightness.this.mBrightnessSeekValue = 1;
            }
            if (RD_Brightness.this.mBrightnessSeekValue > 255) {
                RD_Brightness.this.mBrightnessSeekValue = 255;
            }
        }

        public int getType() {
            return 1;
        }
    };
    private Context mContext;
    IML_ValueChangeListener mDayNightModeChangeListener = new IML_ValueChangeListener() {
        public void onValueChanged(int type, Object value) {
            if (type == 2) {
                if (((Boolean) value).booleanValue()) {
                    RD_Brightness.this.mNightMode = false;
                    RD_Brightness.this.mRead.getDocViewer().setBackgroundResource(R.color.ux_bg_color_docviewer);
                } else {
                    RD_Brightness.this.mNightMode = true;
                    RD_Brightness.this.mRead.getDocViewer().setBackgroundResource(R.color.ux_bg_color_docviewer_night);
                }
                RD_Brightness.this.mRead.getDocViewer().setNightMode(RD_Brightness.this.mNightMode);
            }
        }

        public void onDismiss() {
        }

        public int getType() {
            return 2;
        }
    };
    private ILifecycleEventListener mLifecycleEventListener = new LifecycleEventListener() {
        public void onCreate(Activity act, Bundle savedInstanceState) {
            super.onCreate(act, savedInstanceState);
            RD_Brightness.this.initValue();
        }

        public void onStart(Activity act) {
            RD_Brightness.this.initMLBarValue();
            RD_Brightness.this.applyValue();
            RD_Brightness.this.registerMLListener();
            RD_Brightness.this.mRead.registerStateChangeListener(RD_Brightness.this.mWindowDismissListener);
        }

        public void onStop(Activity act) {
            RD_Brightness.this.mRead.unregisterStateChangeListener(RD_Brightness.this.mWindowDismissListener);
        }

        public void onDestroy(Activity act) {
            RD_Brightness.this.unRegisterMLListener();
        }
    };
    private boolean mLinkToSystem = true;
    IML_ValueChangeListener mLinkToSystemChangeListener = new IML_ValueChangeListener() {
        public void onValueChanged(int type, Object value) {
            if (type == 3) {
                RD_Brightness.this.mLinkToSystem = ((Boolean) value).booleanValue();
                if (RD_Brightness.this.mLinkToSystem) {
                    RD_Brightness.this.setSystemBrightness();
                } else {
                    RD_Brightness.this.setManualBrightness();
                }
            }
        }

        public void onDismiss() {
        }

        public int getType() {
            return 3;
        }
    };
    private boolean mNightMode = false;
    private RD_Read mRead;
    private IML_MultiLineBar mSettingBar;
    IRD_StateChangeListener mWindowDismissListener = new IRD_StateChangeListener() {
        public void onStateChanged(int oldState, int newState) {
            if (newState != oldState && oldState == 4) {
                if (RD_Brightness.this.mBrightnessSeekValue < 1) {
                    RD_Brightness.this.mBrightnessSeekValue = 1;
                }
                if (RD_Brightness.this.mBrightnessSeekValue > 255) {
                    RD_Brightness.this.mBrightnessSeekValue = 255;
                }
            }
        }
    };

    public RD_Brightness(Context context, RD_Read mRead) {
        this.mContext = context;
        this.mRead = mRead;
    }

    public String getName() {
        return Module.MODULE_NAME_BRIGHTNESS;
    }

    public boolean loadModule() {
        this.mRead.registerLifecycleListener(this.mLifecycleEventListener);
        return true;
    }

    public boolean unloadModule() {
        this.mRead.unregisterLifecycleListener(this.mLifecycleEventListener);
        return true;
    }

    private void initValue() {
        this.mLinkToSystem = true;
        this.mNightMode = false;
        this.mBrightnessSeekValue = getSavedBrightSeekValue();
    }

    private void initMLBarValue() {
        this.mSettingBar = this.mRead.getMainFrame().getSettingBar();
        if (this.mNightMode) {
            this.mSettingBar.setProperty(2, Boolean.valueOf(false));
        } else {
            this.mSettingBar.setProperty(2, Boolean.valueOf(true));
        }
        this.mSettingBar.setProperty(3, Boolean.valueOf(this.mLinkToSystem));
        this.mSettingBar.setProperty(1, Integer.valueOf(this.mBrightnessSeekValue));
    }

    private void applyValue() {
        if (this.mLinkToSystem) {
            setSystemBrightness();
        } else {
            setManualBrightness();
        }
    }

    private void registerMLListener() {
        this.mSettingBar.registerListener(this.mDayNightModeChangeListener);
        this.mSettingBar.registerListener(this.mLinkToSystemChangeListener);
        this.mSettingBar.registerListener(this.mBrightnessSeekValueChangeListener);
    }

    private void unRegisterMLListener() {
        this.mSettingBar.unRegisterListener(this.mDayNightModeChangeListener);
        this.mSettingBar.unRegisterListener(this.mLinkToSystemChangeListener);
        this.mSettingBar.unRegisterListener(this.mBrightnessSeekValueChangeListener);
    }

    private int getSavedBrightSeekValue() {
        int progress = getSysBrightnessProgress();
        if (progress <= 0 || progress > 255) {
            return 102;
        }
        return progress;
    }

    private int getSysBrightnessProgress() {
        try {
            return System.getInt(this.mContext.getContentResolver(), "screen_brightness");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            return 102;
        }
    }

    private void setSystemBrightness() {
        LayoutParams params = this.mRead.getMainFrame().getAttachedActivity().getWindow().getAttributes();
        params.screenBrightness = -1.0f;
        this.mRead.getMainFrame().getAttachedActivity().getWindow().setAttributes(params);
    }

    private void setManualBrightness() {
        if (this.mBrightnessSeekValue <= 0 || this.mBrightnessSeekValue > 255) {
            this.mBrightnessSeekValue = getSysBrightnessProgress();
        }
        LayoutParams params = this.mRead.getMainFrame().getAttachedActivity().getWindow().getAttributes();
        if (this.mBrightnessSeekValue < 3) {
            params.screenBrightness = 0.01f;
        } else {
            params.screenBrightness = ((float) this.mBrightnessSeekValue) / 255.0f;
        }
        this.mRead.getMainFrame().getAttachedActivity().getWindow().setAttributes(params);
        if (this.mBrightnessSeekValue < 1) {
            this.mBrightnessSeekValue = 1;
        }
        if (this.mBrightnessSeekValue > 255) {
            this.mBrightnessSeekValue = 255;
        }
    }
}
