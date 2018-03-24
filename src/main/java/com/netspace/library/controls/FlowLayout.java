package com.netspace.library.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
    static final /* synthetic */ boolean $assertionsDisabled = (!FlowLayout.class.desiredAssertionStatus());
    private static final int PAD_H = 2;
    private static final int PAD_V = 2;
    private int mHeight;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if ($assertionsDisabled || MeasureSpec.getMode(widthMeasureSpec) != 0) {
            int childHeightMeasureSpec;
            int width = (MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()) - getPaddingRight();
            int height = (MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()) - getPaddingBottom();
            int count = getChildCount();
            int xpos = getPaddingLeft();
            int ypos = getPaddingTop();
            if (MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE) {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
            } else {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
            }
            this.mHeight = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != 8) {
                    child.measure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), childHeightMeasureSpec);
                    int childw = child.getMeasuredWidth();
                    this.mHeight = Math.max(this.mHeight, child.getMeasuredHeight() + 2);
                    if (xpos + childw > width) {
                        xpos = getPaddingLeft();
                        ypos += this.mHeight;
                    }
                    xpos += childw + 2;
                }
            }
            if (MeasureSpec.getMode(heightMeasureSpec) == 0) {
                height = ypos + this.mHeight;
            } else if (MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE && this.mHeight + ypos < height) {
                height = ypos + this.mHeight;
            }
            setMeasuredDimension(width, height + 5);
            return;
        }
        throw new AssertionError();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int childw = child.getMeasuredWidth();
                int childh = child.getMeasuredHeight();
                if (xpos + childw > width) {
                    xpos = getPaddingLeft();
                    ypos += this.mHeight;
                }
                child.layout(xpos, ypos, xpos + childw, ypos + childh);
                xpos += childw + 2;
            }
        }
    }
}
