package com.foxit.uiextensions.controls.toolbar.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class BarRelativeLayoutImpl extends RelativeLayout {
    private BaseBarImpl mBar;
    private boolean mInterceptTouch = true;

    public BarRelativeLayoutImpl(Context context, BaseBarImpl bar) {
        super(context);
        this.mBar = bar;
    }

    public BarRelativeLayoutImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarRelativeLayoutImpl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mBar.measure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mBar.layout(l, t, r, b);
        super.onLayout(changed, l, t, r, b);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mInterceptTouch) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setInterceptTouch(boolean isInterceptTouch) {
        this.mInterceptTouch = isInterceptTouch;
    }
}
