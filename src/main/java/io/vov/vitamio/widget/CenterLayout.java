package io.vov.vitamio.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class CenterLayout extends ViewGroup {
    private int mHeight;
    private int mPaddingBottom = 0;
    private int mPaddingLeft = 0;
    private int mPaddingRight = 0;
    private int mPaddingTop = 0;
    private int mWidth;

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        public int x;
        public int y;

        public LayoutParams(int width, int height, int x, int y) {
            super(width, height);
            this.x = x;
            this.y = y;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public CenterLayout(Context context) {
        super(context);
    }

    public CenterLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CenterLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        int maxWidth = 0;
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childBottom = lp.y + child.getMeasuredHeight();
                maxWidth = Math.max(maxWidth, lp.x + child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, childBottom);
            }
        }
        setMeasuredDimension(resolveSize(Math.max(maxWidth + (this.mPaddingLeft + this.mPaddingRight), getSuggestedMinimumWidth()), widthMeasureSpec), resolveSize(Math.max(maxHeight + (this.mPaddingTop + this.mPaddingBottom), getSuggestedMinimumHeight()), heightMeasureSpec));
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        this.mWidth = getMeasuredWidth();
        this.mHeight = getMeasuredHeight();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childLeft = this.mPaddingLeft + lp.x;
                if (lp.width > 0) {
                    childLeft += (int) (((double) (this.mWidth - lp.width)) / 2.0d);
                } else {
                    childLeft += (int) (((double) (this.mWidth - child.getMeasuredWidth())) / 2.0d);
                }
                int childTop = this.mPaddingTop + lp.y;
                if (lp.height > 0) {
                    childTop += (int) (((double) (this.mHeight - lp.height)) / 2.0d);
                } else {
                    childTop += (int) (((double) (this.mHeight - child.getMeasuredHeight())) / 2.0d);
                }
                child.layout(childLeft, childTop, child.getMeasuredWidth() + childLeft, child.getMeasuredHeight() + childTop);
            }
        }
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }
}
