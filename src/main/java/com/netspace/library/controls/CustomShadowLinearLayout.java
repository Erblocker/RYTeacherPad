package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.netspace.library.utilities.Shadow;

public class CustomShadowLinearLayout extends LinearLayout {
    public CustomShadowLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public CustomShadowLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public CustomShadowLinearLayout(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (VERSION.SDK_INT >= 16) {
            Shadow.draw(this, canvas);
        }
    }
}
