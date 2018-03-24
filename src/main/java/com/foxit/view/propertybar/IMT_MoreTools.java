package com.foxit.view.propertybar;

import android.graphics.RectF;
import android.view.View;

public interface IMT_MoreTools {
    public static final int MT_TYPE_ANNOTTEXT = 2;
    public static final int MT_TYPE_ARROW = 15;
    public static final int MT_TYPE_CIRCLE = 6;
    public static final int MT_TYPE_ERASER = 12;
    public static final int MT_TYPE_FILEATTACHMENT = 16;
    public static final int MT_TYPE_HIGHLIGHT = 1;
    public static final int MT_TYPE_INK = 13;
    public static final int MT_TYPE_INSERTTEXT = 10;
    public static final int MT_TYPE_LINE = 14;
    public static final int MT_TYPE_REPLACE = 11;
    public static final int MT_TYPE_SQUARE = 7;
    public static final int MT_TYPE_SQUIGGLY = 4;
    public static final int MT_TYPE_STAMP = 9;
    public static final int MT_TYPE_STRIKEOUT = 3;
    public static final int MT_TYPE_TYPEWRITER = 8;
    public static final int MT_TYPE_UNDERLINE = 5;

    public interface IMT_DismissListener {
        void onMTDismiss();
    }

    public interface IMT_MoreClickListener {
        int getType();

        void onMTClick(int i);
    }

    void dismiss();

    View getContentView();

    boolean isShowing();

    void registerListener(IMT_MoreClickListener iMT_MoreClickListener);

    void setButtonEnable(int i, boolean z);

    void setMTDismissListener(IMT_DismissListener iMT_DismissListener);

    void show(RectF rectF, boolean z);

    void unRegisterListener(IMT_MoreClickListener iMT_MoreClickListener);

    void update(RectF rectF);
}
