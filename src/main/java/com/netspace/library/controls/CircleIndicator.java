package com.netspace.library.controls;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AnimatorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.netspace.pad.library.R;

public class CircleIndicator extends LinearLayout implements OnPageChangeListener {
    private static final int DEFAULT_INDICATOR_WIDTH = 5;
    private Animator mAnimationIn;
    private Animator mAnimationOut;
    private int mAnimatorResId = R.animator.scale_with_alpha;
    private int mAnimatorReverseResId = 0;
    private int mCurrentPosition = 0;
    private int mIndicatorBackgroundResId = R.drawable.white_radius;
    private int mIndicatorHeight = -1;
    private int mIndicatorMargin = -1;
    private int mIndicatorUnselectedBackgroundResId = R.drawable.white_radius;
    private int mIndicatorWidth = -1;
    private ViewPager mViewpager;

    private class ReverseInterpolator implements Interpolator {
        private ReverseInterpolator() {
        }

        public float getInterpolation(float value) {
            return Math.abs(1.0f - value);
        }
    }

    public CircleIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(0);
        setGravity(17);
        handleTypedArray(context, attrs);
        checkIndicatorConfig(context);
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleIndicator);
            this.mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_width, -1);
            this.mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_height, -1);
            this.mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_margin, -1);
            this.mAnimatorResId = typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator, R.animator.scale_with_alpha);
            this.mAnimatorReverseResId = typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator_reverse, 0);
            this.mIndicatorBackgroundResId = typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable, R.drawable.white_radius);
            this.mIndicatorUnselectedBackgroundResId = typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable_unselected, this.mIndicatorBackgroundResId);
            typedArray.recycle();
        }
    }

    public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin) {
        configureIndicator(indicatorWidth, indicatorHeight, indicatorMargin, R.animator.scale_with_alpha, 0, R.drawable.white_radius, R.drawable.white_radius);
    }

    public void configureIndicator(int indicatorWidth, int indicatorHeight, int indicatorMargin, @AnimatorRes int animatorId, @AnimatorRes int animatorReverseId, @DrawableRes int indicatorBackgroundId, @DrawableRes int indicatorUnselectedBackgroundId) {
        this.mIndicatorWidth = indicatorWidth;
        this.mIndicatorHeight = indicatorHeight;
        this.mIndicatorMargin = indicatorMargin;
        this.mAnimatorResId = animatorId;
        this.mAnimatorReverseResId = animatorReverseId;
        this.mIndicatorBackgroundResId = indicatorBackgroundId;
        this.mIndicatorUnselectedBackgroundResId = indicatorUnselectedBackgroundId;
        checkIndicatorConfig(getContext());
    }

    private void checkIndicatorConfig(Context context) {
        int i;
        this.mIndicatorWidth = this.mIndicatorWidth < 0 ? dip2px(5.0f) : this.mIndicatorWidth;
        this.mIndicatorHeight = this.mIndicatorHeight < 0 ? dip2px(5.0f) : this.mIndicatorHeight;
        this.mIndicatorMargin = this.mIndicatorMargin < 0 ? dip2px(5.0f) : this.mIndicatorMargin;
        this.mAnimatorResId = this.mAnimatorResId == 0 ? R.animator.scale_with_alpha : this.mAnimatorResId;
        this.mAnimationOut = AnimatorInflater.loadAnimator(context, this.mAnimatorResId);
        if (this.mAnimatorReverseResId == 0) {
            this.mAnimationIn = AnimatorInflater.loadAnimator(context, this.mAnimatorResId);
            this.mAnimationIn.setInterpolator(new ReverseInterpolator());
        } else {
            this.mAnimationIn = AnimatorInflater.loadAnimator(context, this.mAnimatorReverseResId);
        }
        if (this.mIndicatorBackgroundResId == 0) {
            i = R.drawable.white_radius;
        } else {
            i = this.mIndicatorBackgroundResId;
        }
        this.mIndicatorBackgroundResId = i;
        if (this.mIndicatorUnselectedBackgroundResId == 0) {
            i = this.mIndicatorBackgroundResId;
        } else {
            i = this.mIndicatorUnselectedBackgroundResId;
        }
        this.mIndicatorUnselectedBackgroundResId = i;
    }

    public void setViewPager(ViewPager viewPager) {
        this.mViewpager = viewPager;
        this.mCurrentPosition = this.mViewpager.getCurrentItem();
        createIndicators(viewPager);
        this.mViewpager.removeOnPageChangeListener(this);
        this.mViewpager.addOnPageChangeListener(this);
        onPageSelected(this.mCurrentPosition);
    }

    @Deprecated
    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        if (this.mViewpager == null) {
            throw new NullPointerException("can not find Viewpager , setViewPager first");
        }
        this.mViewpager.removeOnPageChangeListener(onPageChangeListener);
        this.mViewpager.addOnPageChangeListener(onPageChangeListener);
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        if (this.mViewpager.getAdapter() != null && this.mViewpager.getAdapter().getCount() > 0) {
            if (this.mAnimationIn.isRunning()) {
                this.mAnimationIn.end();
            }
            if (this.mAnimationOut.isRunning()) {
                this.mAnimationOut.end();
            }
            View currentIndicator = getChildAt(this.mCurrentPosition);
            currentIndicator.setBackgroundResource(this.mIndicatorUnselectedBackgroundResId);
            this.mAnimationIn.setTarget(currentIndicator);
            this.mAnimationIn.start();
            View selectedIndicator = getChildAt(position);
            selectedIndicator.setBackgroundResource(this.mIndicatorBackgroundResId);
            this.mAnimationOut.setTarget(selectedIndicator);
            this.mAnimationOut.start();
            this.mCurrentPosition = position;
        }
    }

    public void onPageScrollStateChanged(int state) {
    }

    private void createIndicators(ViewPager viewPager) {
        removeAllViews();
        if (viewPager.getAdapter() != null) {
            int count = viewPager.getAdapter().getCount();
            if (count > 0) {
                addIndicator(this.mIndicatorBackgroundResId, this.mAnimationOut);
                for (int i = 1; i < count; i++) {
                    addIndicator(this.mIndicatorUnselectedBackgroundResId, this.mAnimationIn);
                }
            }
        }
    }

    private void addIndicator(@DrawableRes int backgroundDrawableId, Animator animator) {
        if (animator.isRunning()) {
            animator.end();
        }
        View Indicator = new View(getContext());
        Indicator.setBackgroundResource(backgroundDrawableId);
        addView(Indicator, this.mIndicatorWidth, this.mIndicatorHeight);
        LayoutParams lp = (LayoutParams) Indicator.getLayoutParams();
        lp.leftMargin = this.mIndicatorMargin;
        lp.rightMargin = this.mIndicatorMargin;
        Indicator.setLayoutParams(lp);
        animator.setTarget(Indicator);
        animator.start();
    }

    public int dip2px(float dpValue) {
        return (int) ((dpValue * getResources().getDisplayMetrics().density) + 0.5f);
    }
}
