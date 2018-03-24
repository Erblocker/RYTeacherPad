package com.foxit.uiextensions.controls.toolbar;

import android.view.View;

public interface BaseBar {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public enum TB_Position {
        Position_LT,
        Position_CENTER,
        Position_RB
    }

    void addView(BaseItem baseItem, TB_Position tB_Position);

    View getContentView();

    String getName();

    void removeAllItems();

    boolean removeItemByItem(BaseItem baseItem);

    boolean removeItemByTag(int i);

    void setBackgroundColor(int i);

    void setBackgroundResource(int i);

    void setBarVisible(boolean z);

    void setContentView(View view);

    void setHeight(int i);

    void setInterceptTouch(boolean z);

    void setInterval(boolean z);

    void setItemSpace(int i);

    void setName(String str);

    void setNeedResetItemSize(boolean z);

    void setOrientation(int i);

    void setWidth(int i);
}
