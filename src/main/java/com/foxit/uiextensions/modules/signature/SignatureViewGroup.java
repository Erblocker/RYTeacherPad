package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Scroller;

class SignatureViewGroup extends ViewGroup {
    private IMoveCallBack mCallback;
    private Context mContext;
    private int mCurIndex;
    private Scroller mScroller;

    public interface IMoveCallBack {
        void onStart();

        void onStop();
    }

    public SignatureViewGroup(Context context) {
        this(context, null);
    }

    public SignatureViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        setLayoutParams(new LayoutParams(-1, -1));
        this.mScroller = new Scroller(this.mContext);
    }

    public void init(int width, int height) {
        snapToScreen(0);
        invalidate();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 2) {
            invalidate();
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).layout(l, getHeight() * i, r, getHeight() + (getHeight() * i));
        }
    }

    private void snapToScreen(int index) {
        if (this.mCurIndex != index) {
            if (!this.mScroller.isFinished()) {
                this.mScroller.forceFinished(true);
            }
            int delta = (getHeight() * index) - getScrollY();
            this.mScroller.startScroll(0, getScrollY(), 0, delta, Math.abs(delta));
            invalidate();
            this.mCurIndex = index;
        }
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            scrollTo(0, this.mScroller.getCurrY());
            postInvalidate();
        }
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.mCallback == null) {
            return;
        }
        if (getScrollY() == 0 || getScrollY() == getHeight()) {
            this.mCallback.onStop();
            this.mCallback = null;
        }
    }

    public void moveToTop(IMoveCallBack callback) {
        this.mCallback = callback;
        callback.onStart();
        snapToScreen(0);
    }
}
