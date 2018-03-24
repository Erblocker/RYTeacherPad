package com.foxit.uiextensions.controls.toolbar;

import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public interface BaseItem {
    public static final int RELATION_BELOW = 13;
    public static final int RELATION_LEFT = 10;
    public static final int RELATION_RIGNT = 12;
    public static final int RELATION_TOP = 11;

    public enum ItemType {
        Item_Text,
        Item_Image,
        Item_Text_Image,
        Item_custom
    }

    View getContentView();

    int getId();

    int getTag();

    String getText();

    void onItemLayout(int i, int i2, int i3, int i4);

    void setBackgroundResource(int i);

    void setContentView(View view);

    void setDisplayStyle(ItemType itemType);

    void setEnable(boolean z);

    void setId(int i);

    boolean setImageResource(int i);

    void setInterval(int i);

    void setOnClickListener(OnClickListener onClickListener);

    void setOnLongClickListener(OnLongClickListener onLongClickListener);

    void setRelation(int i);

    void setSelected(boolean z);

    void setTag(int i);

    void setText(int i);

    void setText(String str);

    void setTextColor(int i);

    void setTextColor(int i, int i2);

    void setTextColorResource(int i);

    void setTextSize(float f);

    void setTypeface(Typeface typeface);
}
