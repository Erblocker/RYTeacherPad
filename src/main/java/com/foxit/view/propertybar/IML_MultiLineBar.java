package com.foxit.view.propertybar;

import android.view.View;

public interface IML_MultiLineBar {
    public static final int TYPE_DAYNIGHT = 2;
    public static final int TYPE_LIGHT = 1;
    public static final int TYPE_LOCKSCREEN = 6;
    public static final int TYPE_REFLOW = 7;
    public static final int TYPE_SINGLEPAGE = 4;
    public static final int TYPE_SYSLIGHT = 3;
    public static final int TYPE_THUMBNAIL = 5;

    public interface IML_ValueChangeListener {
        int getType();

        void onDismiss();

        void onValueChanged(int i, Object obj);
    }

    void dismiss();

    View getContentView();

    boolean isShowing();

    void registerListener(IML_ValueChangeListener iML_ValueChangeListener);

    void setProperty(int i, Object obj);

    void show();

    void unRegisterListener(IML_ValueChangeListener iML_ValueChangeListener);
}
