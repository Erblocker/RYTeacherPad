package com.netspace.library.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Iterator;

public class StickyScrollView extends LockableScrollView {
    private static final int DEFAULT_SHADOW_HEIGHT = 10;
    public static final String FLAG_HASTRANSPARANCY = "-hastransparancy";
    public static final String FLAG_NONCONSTANT = "-nonconstant";
    public static final String STICKY_TAG = "sticky";
    private boolean clipToPaddingHasBeenSet;
    private boolean clippingToPadding;
    private View currentlyStickingView;
    private boolean hasNotDoneActionDown;
    private final Runnable invalidateRunnable;
    private Drawable mShadowDrawable;
    private int mShadowHeight;
    private boolean redirectTouchesToStickyView;
    private int stickyViewLeftOffset;
    private float stickyViewTopOffset;
    private ArrayList<View> stickyViews;

    public StickyScrollView(Context context) {
        this(context, null);
    }

    public StickyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842880);
    }

    public StickyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.invalidateRunnable = new Runnable() {
            public void run() {
                if (StickyScrollView.this.currentlyStickingView != null) {
                    StickyScrollView.this.invalidate(StickyScrollView.this.getLeftForViewRelativeOnlyChild(StickyScrollView.this.currentlyStickingView), StickyScrollView.this.getBottomForViewRelativeOnlyChild(StickyScrollView.this.currentlyStickingView), StickyScrollView.this.getRightForViewRelativeOnlyChild(StickyScrollView.this.currentlyStickingView), (int) (((float) StickyScrollView.this.getScrollY()) + (((float) StickyScrollView.this.currentlyStickingView.getHeight()) + StickyScrollView.this.stickyViewTopOffset)));
                }
                StickyScrollView.this.postDelayed(this, 16);
            }
        };
        this.hasNotDoneActionDown = true;
        setup();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StickyScrollView, defStyle, 0);
        this.mShadowHeight = a.getDimensionPixelSize(R.styleable.StickyScrollView_stuckShadowHeight, (int) ((10.0f * context.getResources().getDisplayMetrics().density) + 0.5f));
        int shadowDrawableRes = a.getResourceId(R.styleable.StickyScrollView_stuckShadowDrawable, -1);
        if (shadowDrawableRes != -1) {
            this.mShadowDrawable = context.getResources().getDrawable(shadowDrawableRes);
        }
        a.recycle();
    }

    public void setShadowHeight(int height) {
        this.mShadowHeight = height;
    }

    public void setup() {
        this.stickyViews = new ArrayList();
    }

    private int getLeftForViewRelativeOnlyChild(View v) {
        int left = v.getLeft();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            left += v.getLeft();
        }
        return left;
    }

    private int getTopForViewRelativeOnlyChild(View v) {
        int top = v.getTop();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            top += v.getTop();
        }
        return top;
    }

    private int getRightForViewRelativeOnlyChild(View v) {
        int right = v.getRight();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            right += v.getRight();
        }
        return right;
    }

    private int getBottomForViewRelativeOnlyChild(View v) {
        int bottom = v.getBottom();
        while (v.getParent() != getChildAt(0)) {
            v = (View) v.getParent();
            bottom += v.getBottom();
        }
        return bottom;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!this.clipToPaddingHasBeenSet) {
            this.clippingToPadding = true;
        }
        notifyHierarchyChanged();
    }

    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        this.clippingToPadding = clipToPadding;
        this.clipToPaddingHasBeenSet = true;
    }

    public void addView(View child) {
        super.addView(child);
        findStickyViews(child);
    }

    public void addView(View child, int index) {
        super.addView(child, index);
        findStickyViews(child);
    }

    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        findStickyViews(child);
    }

    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        findStickyViews(child);
    }

    public void addView(View child, LayoutParams params) {
        super.addView(child, params);
        findStickyViews(child);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.currentlyStickingView != null) {
            float f;
            canvas.save();
            canvas.translate((float) (getPaddingLeft() + this.stickyViewLeftOffset), ((float) (this.clippingToPadding ? getPaddingTop() : 0)) + (this.stickyViewTopOffset + ((float) getScrollY())));
            if (this.clippingToPadding) {
                f = -this.stickyViewTopOffset;
            } else {
                f = 0.0f;
            }
            canvas.clipRect(0.0f, f, (float) (getWidth() - this.stickyViewLeftOffset), (float) ((this.currentlyStickingView.getHeight() + this.mShadowHeight) + 1));
            if (this.mShadowDrawable != null) {
                this.mShadowDrawable.setBounds(0, this.currentlyStickingView.getHeight(), this.currentlyStickingView.getWidth(), this.currentlyStickingView.getHeight() + this.mShadowHeight);
                this.mShadowDrawable.draw(canvas);
            }
            canvas.clipRect(0.0f, this.clippingToPadding ? -this.stickyViewTopOffset : 0.0f, (float) getWidth(), (float) this.currentlyStickingView.getHeight());
            if (getStringTagForView(this.currentlyStickingView).contains(FLAG_HASTRANSPARANCY)) {
                showView(this.currentlyStickingView);
                this.currentlyStickingView.draw(canvas);
                hideView(this.currentlyStickingView);
            } else {
                this.currentlyStickingView.draw(canvas);
            }
            canvas.restore();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean z = true;
        if (ev.getAction() == 0) {
            this.redirectTouchesToStickyView = true;
        }
        if (this.redirectTouchesToStickyView) {
            this.redirectTouchesToStickyView = this.currentlyStickingView != null;
            if (this.redirectTouchesToStickyView) {
                if (ev.getY() > ((float) this.currentlyStickingView.getHeight()) + this.stickyViewTopOffset || ev.getX() < ((float) getLeftForViewRelativeOnlyChild(this.currentlyStickingView)) || ev.getX() > ((float) getRightForViewRelativeOnlyChild(this.currentlyStickingView))) {
                    z = false;
                }
                this.redirectTouchesToStickyView = z;
            }
        } else if (this.currentlyStickingView == null) {
            this.redirectTouchesToStickyView = false;
        }
        if (this.redirectTouchesToStickyView) {
            ev.offsetLocation(0.0f, -1.0f * ((((float) getScrollY()) + this.stickyViewTopOffset) - ((float) getTopForViewRelativeOnlyChild(this.currentlyStickingView))));
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.redirectTouchesToStickyView) {
            ev.offsetLocation(0.0f, (((float) getScrollY()) + this.stickyViewTopOffset) - ((float) getTopForViewRelativeOnlyChild(this.currentlyStickingView)));
        }
        if (ev.getAction() == 0) {
            this.hasNotDoneActionDown = false;
        }
        if (this.hasNotDoneActionDown) {
            MotionEvent down = MotionEvent.obtain(ev);
            down.setAction(0);
            super.onTouchEvent(down);
            this.hasNotDoneActionDown = false;
        }
        if (ev.getAction() == 1 || ev.getAction() == 3) {
            this.hasNotDoneActionDown = true;
        }
        return super.onTouchEvent(ev);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        doTheStickyThing();
    }

    private void doTheStickyThing() {
        int i = 0;
        View viewThatShouldStick = null;
        View approachingView = null;
        Iterator it = this.stickyViews.iterator();
        while (it.hasNext()) {
            View v = (View) it.next();
            int viewTop = (getTopForViewRelativeOnlyChild(v) - getScrollY()) + (this.clippingToPadding ? 0 : getPaddingTop());
            if (viewTop <= 0) {
                if (viewThatShouldStick != null) {
                    if (viewTop <= (this.clippingToPadding ? 0 : getPaddingTop()) + (getTopForViewRelativeOnlyChild(viewThatShouldStick) - getScrollY())) {
                    }
                }
                viewThatShouldStick = v;
            } else {
                if (approachingView != null) {
                    if (viewTop >= (this.clippingToPadding ? 0 : getPaddingTop()) + (getTopForViewRelativeOnlyChild(approachingView) - getScrollY())) {
                    }
                }
                approachingView = v;
            }
        }
        if (viewThatShouldStick != null) {
            if (approachingView != null) {
                i = Math.min(0, ((this.clippingToPadding ? 0 : getPaddingTop()) + (getTopForViewRelativeOnlyChild(approachingView) - getScrollY())) - viewThatShouldStick.getHeight());
            }
            this.stickyViewTopOffset = (float) i;
            if (viewThatShouldStick != this.currentlyStickingView) {
                if (this.currentlyStickingView != null) {
                    stopStickingCurrentlyStickingView();
                }
                this.stickyViewLeftOffset = getLeftForViewRelativeOnlyChild(viewThatShouldStick);
                startStickingView(viewThatShouldStick);
            }
        } else if (this.currentlyStickingView != null) {
            stopStickingCurrentlyStickingView();
        }
    }

    private void startStickingView(View viewThatShouldStick) {
        this.currentlyStickingView = viewThatShouldStick;
        if (getStringTagForView(this.currentlyStickingView).contains(FLAG_HASTRANSPARANCY)) {
            hideView(this.currentlyStickingView);
        }
        if (((String) this.currentlyStickingView.getTag()).contains(FLAG_NONCONSTANT)) {
            post(this.invalidateRunnable);
        }
    }

    private void stopStickingCurrentlyStickingView() {
        if (getStringTagForView(this.currentlyStickingView).contains(FLAG_HASTRANSPARANCY)) {
            showView(this.currentlyStickingView);
        }
        this.currentlyStickingView = null;
        removeCallbacks(this.invalidateRunnable);
    }

    public void notifyStickyAttributeChanged() {
        notifyHierarchyChanged();
    }

    private void notifyHierarchyChanged() {
        if (this.currentlyStickingView != null) {
            stopStickingCurrentlyStickingView();
        }
        this.stickyViews.clear();
        findStickyViews(getChildAt(0));
        doTheStickyThing();
        invalidate();
    }

    private void findStickyViews(View v) {
        String tag;
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                tag = getStringTagForView(vg.getChildAt(i));
                if (tag != null && tag.contains(STICKY_TAG)) {
                    this.stickyViews.add(vg.getChildAt(i));
                } else if (vg.getChildAt(i) instanceof ViewGroup) {
                    findStickyViews(vg.getChildAt(i));
                }
            }
            return;
        }
        tag = (String) v.getTag();
        if (tag != null && tag.contains(STICKY_TAG)) {
            this.stickyViews.add(v);
        }
    }

    private String getStringTagForView(View v) {
        return String.valueOf(v.getTag());
    }

    private void hideView(View v) {
        if (VERSION.SDK_INT >= 11) {
            v.setAlpha(0.0f);
            return;
        }
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(0);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }

    private void showView(View v) {
        if (VERSION.SDK_INT >= 11) {
            v.setAlpha(1.0f);
            return;
        }
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(0);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }
}
