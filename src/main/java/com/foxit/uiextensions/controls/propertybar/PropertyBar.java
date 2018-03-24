package com.foxit.uiextensions.controls.propertybar;

import android.graphics.Color;
import android.graphics.RectF;
import android.view.View;
import com.foxit.sdk.common.Font;
import com.netspace.library.threads.CheckNewVersionThread2;
import org.kxml2.wap.Wbxml;

public interface PropertyBar {
    public static final int ARROW_BOTTOM = 4;
    public static final int ARROW_CENTER = 5;
    public static final int ARROW_LEFT = 1;
    public static final int ARROW_NONE = 0;
    public static final int ARROW_RIGHT = 3;
    public static final int ARROW_TOP = 2;
    public static final String[] ICONNAMES = new String[]{"Comment", "Key", "Note", "Help", "NewParagraph", "Paragraph", "Insert"};
    public static final int[] ICONTYPES = new int[]{1, 2, 3, 4, 5, 6, 7};
    public static final int[] PB_COLORS_ARROW = PB_COLORS_LINE;
    public static final int[] PB_COLORS_CALLOUT = PB_COLORS_TYPEWRITER;
    public static final int[] PB_COLORS_CARET = PB_COLORS_STRIKEOUT;
    public static final int[] PB_COLORS_CIRCLE = PB_COLORS_LINE;
    public static final int[] PB_COLORS_FILEATTACHMENT = PB_COLORS_LINE;
    public static final int[] PB_COLORS_HIGHLIGHT = new int[]{Color.argb(255, 116, 128, 252), Color.argb(255, 255, 255, 0), Color.argb(255, 204, 255, 102), Color.argb(255, 0, 255, 255), Color.argb(255, CheckNewVersionThread2.MSG_CREATEPROGRESS, 204, 255), Color.argb(255, 204, CheckNewVersionThread2.MSG_CREATEPROGRESS, 255), Color.argb(255, 255, CheckNewVersionThread2.MSG_CREATEPROGRESS, CheckNewVersionThread2.MSG_CREATEPROGRESS), Color.argb(255, 255, 255, 255), Color.argb(255, Wbxml.OPAQUE, Wbxml.OPAQUE, Wbxml.OPAQUE), Color.argb(255, 0, 0, 0)};
    public static final int[] PB_COLORS_LINE = new int[]{Color.argb(255, 255, 159, 64), Color.argb(255, 128, 128, 255), Color.argb(255, Font.e_fontCharsetBaltic, 233, 76), Color.argb(255, 255, 241, 96), Color.argb(255, CheckNewVersionThread2.MSG_CREATEPROGRESS, 102, 102), Color.argb(255, 255, 76, 76), Color.argb(255, 102, CheckNewVersionThread2.MSG_CREATEPROGRESS, CheckNewVersionThread2.MSG_CREATEPROGRESS), Color.argb(255, 255, 255, 255), Color.argb(255, Wbxml.OPAQUE, Wbxml.OPAQUE, Wbxml.OPAQUE), Color.argb(255, 0, 0, 0)};
    public static final int[] PB_COLORS_PENCIL = PB_COLORS_LINE;
    public static final int[] PB_COLORS_SIGN = new int[]{Color.argb(255, 0, 0, 51), Color.argb(255, 0, 0, 102), Color.argb(255, 50, 50, 50), Color.argb(255, 50, 102, 0), Color.argb(255, 102, 0, 0), Color.argb(255, 0, 50, 50), Color.argb(255, 50, 0, CheckNewVersionThread2.MSG_CREATEPROGRESS), Color.argb(255, 255, 255, 255), Color.argb(255, Wbxml.OPAQUE, Wbxml.OPAQUE, Wbxml.OPAQUE), Color.argb(255, 0, 0, 0)};
    public static final int[] PB_COLORS_SQUARE = PB_COLORS_LINE;
    public static final int[] PB_COLORS_SQUARENESS = PB_COLORS_LINE;
    public static final int[] PB_COLORS_SQUIGGLY = PB_COLORS_UNDERLINE;
    public static final int[] PB_COLORS_STRIKEOUT = new int[]{Color.argb(255, CheckNewVersionThread2.MSG_CREATEPROGRESS, 102, 102), Color.argb(255, 255, 51, 51), Color.argb(255, 255, 0, 255), Color.argb(255, CheckNewVersionThread2.MSG_CREATEPROGRESS, 102, 255), Color.argb(255, 102, 204, 51), Color.argb(255, 0, 204, 255), Color.argb(255, 255, CheckNewVersionThread2.MSG_CREATEPROGRESS, 0), Color.argb(255, 255, 255, 255), Color.argb(255, Wbxml.OPAQUE, Wbxml.OPAQUE, Wbxml.OPAQUE), Color.argb(255, 0, 0, 0)};
    public static final int[] PB_COLORS_TEXT = PB_COLORS_LINE;
    public static final int[] PB_COLORS_TYPEWRITER = new int[]{Color.argb(255, 51, 102, 204), Color.argb(255, 102, CheckNewVersionThread2.MSG_CREATEPROGRESS, 51), Color.argb(255, 204, 102, 0), Color.argb(255, 204, CheckNewVersionThread2.MSG_CREATEPROGRESS, 0), Color.argb(255, 163, 163, 5), Color.argb(255, 204, 0, 0), Color.argb(255, 51, 102, 102), Color.argb(255, 255, 255, 255), Color.argb(255, Wbxml.OPAQUE, Wbxml.OPAQUE, Wbxml.OPAQUE), Color.argb(255, 0, 0, 0)};
    public static final int[] PB_COLORS_UNDERLINE = new int[]{Color.argb(255, 0, CheckNewVersionThread2.MSG_CREATEPROGRESS, 204), Color.argb(255, 51, 204, 0), Color.argb(255, 204, 204, 0), Color.argb(255, 255, CheckNewVersionThread2.MSG_CREATEPROGRESS, 51), Color.argb(255, 255, 0, 0), Color.argb(255, 51, 102, 255), Color.argb(255, 204, 51, 255), Color.argb(255, 255, 255, 255), Color.argb(255, Wbxml.OPAQUE, Wbxml.OPAQUE, Wbxml.OPAQUE), Color.argb(255, 0, 0, 0)};
    public static final String[] PB_FONTNAMES = new String[]{"Courier", "Helvetica", "Times"};
    public static final float[] PB_FONTSIZES = new float[]{6.0f, 8.0f, 10.0f, 12.0f, 18.0f, 24.0f, 36.0f, 48.0f, 64.0f, 72.0f};
    public static final float PB_FONTSIZE_DEFAULT = 24.0f;
    public static final int[] PB_OPACITYS = new int[]{25, 50, 75, 100};
    public static final long PROPERTY_ALL = 1023;
    public static final long PROPERTY_ANNOT_TYPE = 64;
    public static final long PROPERTY_COLOR = 1;
    public static final long PROPERTY_FILEATTACHMENT = 268435456;
    public static final long PROPERTY_FONTNAME = 8;
    public static final long PROPERTY_FONTSIZE = 16;
    public static final long PROPERTY_LINEWIDTH = 4;
    public static final long PROPERTY_LINE_STYLE = 32;
    public static final long PROPERTY_OPACITY = 2;
    public static final long PROPERTY_SCALE_PERCENT = 256;
    public static final long PROPERTY_SCALE_SWITCH = 512;
    public static final long PROPERTY_SELF_COLOR = 128;
    public static final long PROPERTY_UNKNOWN = 0;

    public interface DismissListener {
        void onDismiss();
    }

    public interface PropertyChangeListener {
        void onValueChanged(long j, float f);

        void onValueChanged(long j, int i);

        void onValueChanged(long j, String str);
    }

    public interface UpdateViewListener {
        void onUpdate(long j, int i);
    }

    void addContentView(View view);

    void addCustomItem(long j, View view, int i, int i2);

    void addTab(String str, int i);

    void addTab(String str, int i, String str2, int i2);

    void dismiss();

    View getContentView();

    int getCurrentTabIndex();

    int getItemIndex(long j);

    PropertyChangeListener getPropertyChangeListener();

    boolean isShowing();

    void reset(long j);

    void setArrowVisible(boolean z);

    void setColors(int[] iArr);

    void setCurrentTab(int i);

    void setDismissListener(DismissListener dismissListener);

    void setPhoneFullScreen(boolean z);

    void setProperty(long j, float f);

    void setProperty(long j, int i);

    void setProperty(long j, String str);

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener);

    void setTopTitleVisible(boolean z);

    void show(RectF rectF, boolean z);

    void update(RectF rectF);
}
