package com.foxit.uiextensions;

import android.view.MotionEvent;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;

public interface ToolHandler extends IDrawEventListener {
    public static final String TH_TYPE_ARROW = "Arrow Tool";
    public static final String TH_TYPE_CIRCLE = "Circle Tool";
    public static final String TH_TYPE_ERASER = "Eraser Tool";
    public static final String TH_TYPE_FORMFILLER = "FormFiller Tool";
    public static final String TH_TYPE_FileAttachment = "FileAttachment Tool";
    public static final String TH_TYPE_HIGHLIGHT = "Highlight Tool";
    public static final String TH_TYPE_INK = "Ink Tool";
    public static final String TH_TYPE_LINE = "Line Tool";
    public static final String TH_TYPE_NOTE = "Note Tool";
    public static final String TH_TYPE_REPLACE = "Replace Tool";
    public static final String TH_TYPE_SIGNATURE = "Signature Tool";
    public static final String TH_TYPE_SQUARE = "Square Tool";
    public static final String TH_TYPE_SQUIGGLY = "Squiggly Tool";
    public static final String TH_TYPE_STAMP = "Stamp Tool";
    public static final String TH_TYPE_STRIKEOUT = "Strikeout Tool";
    public static final String TH_TYPE_TEXTSELECT = "TextSelect Tool";
    public static final String TH_TYPE_TYPEWRITER = "Typewriter Tool";
    public static final String TH_TYPE_UNDERLINE = "Underline Tool";
    public static final String TH_TYPR_INSERTTEXT = "InsetText Tool";

    String getType();

    void onActivate();

    void onDeactivate();

    boolean onLongPress(int i, MotionEvent motionEvent);

    boolean onSingleTapConfirmed(int i, MotionEvent motionEvent);

    boolean onTouchEvent(int i, MotionEvent motionEvent);
}
