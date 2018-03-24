package com.foxit.uiextensions.utils;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class UIMarqueeTextView extends TextView {
    public UIMarqueeTextView(Context context) {
        this(context, null, 0);
    }

    public UIMarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UIMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setSingleLine(true);
        setEllipsize(TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
    }

    public boolean isFocused() {
        return true;
    }
}
