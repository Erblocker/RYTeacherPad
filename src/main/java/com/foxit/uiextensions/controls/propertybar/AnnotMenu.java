package com.foxit.uiextensions.controls.propertybar;

import android.graphics.RectF;
import android.view.View;
import android.widget.PopupWindow;
import java.util.ArrayList;

public interface AnnotMenu {
    public static final int AM_BT_CANCEL = 15;
    public static final int AM_BT_COMMENT = 3;
    public static final int AM_BT_COPY = 1;
    public static final int AM_BT_DELETE = 2;
    public static final int AM_BT_EDIT = 5;
    public static final int AM_BT_HIGHLIGHT = 7;
    public static final int AM_BT_NOTE = 11;
    public static final int AM_BT_REPLY = 4;
    public static final int AM_BT_SIGN = 14;
    public static final int AM_BT_SIGNATURE = 12;
    public static final int AM_BT_SIGN_LIST = 13;
    public static final int AM_BT_SQUIGGLY = 10;
    public static final int AM_BT_STRIKEOUT = 9;
    public static final int AM_BT_STYLE = 6;
    public static final int AM_BT_UNDERLINE = 8;
    public static final int AM_BT_VERIFY_SIGNATURE = 16;

    public interface ClickListener {
        void onAMClick(int i);
    }

    void dismiss();

    PopupWindow getPopupWindow();

    boolean isShowing();

    void setListener(ClickListener clickListener);

    void setMenuItems(ArrayList<Integer> arrayList);

    void setShowAlways(boolean z);

    void show(RectF rectF);

    void show(RectF rectF, int i, int i2, boolean z);

    void show(RectF rectF, View view);

    void update(RectF rectF);

    void update(RectF rectF, int i, int i2, boolean z);
}
