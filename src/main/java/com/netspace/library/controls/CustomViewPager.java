package com.netspace.library.controls;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

public class CustomViewPager extends ViewPager {
    private boolean isPagingEnabled = true;
    private boolean mbEnableSameSize = false;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.isPagingEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.isPagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setEnableSameSize(boolean bEnable) {
        this.mbEnableSameSize = bEnable;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mbEnableSameSize) {
            int height = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, 0));
                int h = child.getMeasuredHeight();
                if (h > height) {
                    height = h;
                }
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, 1073741824);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

    public boolean getPagingEnabled() {
        return this.isPagingEnabled;
    }
}
