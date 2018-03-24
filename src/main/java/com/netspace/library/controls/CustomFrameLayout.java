package com.netspace.library.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class CustomFrameLayout extends FrameLayout {
    private final String TAG = getClass().getSimpleName();
    private boolean touchEventCalled;

    public CustomFrameLayout(Context context) {
        super(context);
    }

    public CustomFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean isHandled = super.dispatchTouchEvent(ev);
        if (isHandled && !this.touchEventCalled) {
            onTouchEvent(ev);
        }
        this.touchEventCalled = false;
        return isHandled;
    }

    public boolean onTouchEvent(MotionEvent event) {
        this.touchEventCalled = true;
        return super.onTouchEvent(event);
    }
}
