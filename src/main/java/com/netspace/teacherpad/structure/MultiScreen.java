package com.netspace.teacherpad.structure;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.netspace.library.utilities.Utilities;
import java.util.ArrayList;

public class MultiScreen {
    public final int SCREEN_FUNCTION_ERASE = 3;
    public final int SCREEN_FUNCTION_MOVE = 4;
    public final int SCREEN_FUNCTION_PEN = 1;
    public final int SCREEN_FUNCTION_POINTER = 2;
    public ArrayList<PlayPos> arrPlayPos = new ArrayList();
    public ArrayList<String> arrPlayStack = new ArrayList();
    public ArrayList<Integer> arrPlayStackFlags = new ArrayList();
    public boolean bMaximized;
    public Drawable drawableActiveButton;
    public int nCurrentFunctionButtonID;
    public int nCurrentPenColor;
    public Rect rectCornerButton;
    public Rect rectOldCornerButton;
    public Rect rectScreen;

    public boolean isResourceExsit(String szGUIDOrUrl) {
        return Utilities.isInArray(this.arrPlayStack, szGUIDOrUrl);
    }
}
