package com.netspace.library.controls;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class LockableScrollView extends NestedScrollView {
    private float lastX;
    private float lastY;
    private InterceptHoverEvent mInterceptHoverEventListener = null;
    private InterceptTouchEvent mInterceptTouchEventListener = null;
    private boolean mRightSideActive = false;
    private ScrollRightSideListener mScrollRightSideListener = null;
    private ScrollViewListener mScrollViewListener = null;
    private boolean mScrollable = true;
    private boolean mSmartCheckMove = false;
    private double mSpeedRate = 1.0d;
    private int mXPos = 0;
    private int mYPos = 0;
    private float startX;
    private float startY;
    private float xDistance;
    private float yDistance;

    public interface InterceptHoverEvent {
        boolean onInterceptHoverEvent(MotionEvent motionEvent);
    }

    public interface InterceptTouchEvent {
        boolean onInterceptTouchEvent(MotionEvent motionEvent);
    }

    public interface ScrollRightSideListener {
        void onRightSideActive();

        void onRightSideDisactive();

        void onRightSideMove(int i, int i2);
    }

    public interface ScrollViewListener {
        void onScrollChanged(LockableScrollView lockableScrollView, int i, int i2, int i3, int i4);
    }

    public LockableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableScrollView(Context context) {
        super(context);
    }

    public void setScrollingEnabled(boolean enabled) {
        this.mScrollable = enabled;
    }

    public void setSmartCheckMove(boolean bEnabled) {
        this.mSmartCheckMove = bEnabled;
    }

    public boolean isScrollable() {
        return this.mScrollable;
    }

    public int getCurrentXPos() {
        return this.mXPos;
    }

    public int getCurrentYPos() {
        return this.mYPos;
    }

    public void setSpeedRate(double fRate) {
        this.mSpeedRate = fRate;
    }

    public void fling(int velocityY) {
        super.fling((int) (((double) velocityY) * this.mSpeedRate));
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                if (!this.mScrollable) {
                    return this.mScrollable;
                }
                if (ev.getX() > ((float) (getWidth() - 100))) {
                    this.mRightSideActive = true;
                    if (this.mScrollRightSideListener != null) {
                        this.mScrollRightSideListener.onRightSideActive();
                    }
                }
                return super.onTouchEvent(ev);
            case 1:
                if (this.mRightSideActive) {
                    if (this.mScrollRightSideListener != null) {
                        this.mScrollRightSideListener.onRightSideDisactive();
                    }
                    this.mRightSideActive = false;
                }
                return super.onTouchEvent(ev);
            case 2:
                if (ev.getX() > ((float) (getWidth() - 100)) && this.mScrollRightSideListener != null) {
                    this.mScrollRightSideListener.onRightSideMove(this.mXPos, this.mYPos);
                }
                return super.onTouchEvent(ev);
            default:
                return super.onTouchEvent(ev);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mInterceptHoverEventListener != null) {
            boolean bResult = this.mInterceptHoverEventListener.onInterceptHoverEvent(ev);
        }
        if (!this.mScrollable) {
            return false;
        }
        if (!this.mSmartCheckMove) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case 0:
                this.yDistance = 0.0f;
                this.xDistance = 0.0f;
                float x = ev.getX();
                this.lastX = x;
                this.startX = x;
                x = ev.getY();
                this.lastY = x;
                this.startY = x;
                break;
            case 2:
                float curX = ev.getX();
                float curY = ev.getY();
                this.xDistance += Math.abs(curX - this.lastX);
                this.yDistance += Math.abs(curY - this.lastY);
                if (this.xDistance <= this.yDistance) {
                    if (Math.abs(curY - this.lastY) >= 60.0f) {
                        this.lastX = curX;
                        this.lastY = curY;
                        break;
                    }
                    Log.d("yDistance", String.valueOf(Math.abs(curY - this.lastY)));
                    this.lastX = curX;
                    this.lastY = curY;
                    return false;
                }
                return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.mScrollViewListener = scrollViewListener;
    }

    public void setScrollRightSideListener(ScrollRightSideListener scrollRightListener) {
        this.mScrollRightSideListener = scrollRightListener;
    }

    public void setOnInterceptHoverEventListener(InterceptHoverEvent InterceptHoverEventListener) {
        this.mInterceptHoverEventListener = InterceptHoverEventListener;
    }

    public void setOnInterceptTouchEventListener(InterceptTouchEvent InterceptTouchEventListener) {
        this.mInterceptTouchEventListener = InterceptTouchEventListener;
    }

    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        this.mXPos = x;
        this.mYPos = y;
        if (this.mScrollViewListener != null) {
            this.mScrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    public boolean onInterceptHoverEvent(MotionEvent event) {
        boolean bResult = false;
        if (this.mInterceptHoverEventListener != null) {
            bResult = this.mInterceptHoverEventListener.onInterceptHoverEvent(event);
        }
        if (bResult) {
            return true;
        }
        return super.onInterceptHoverEvent(event);
    }
}
