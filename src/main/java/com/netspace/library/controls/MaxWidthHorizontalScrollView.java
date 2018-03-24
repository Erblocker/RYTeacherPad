package com.netspace.library.controls;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.HorizontalScrollView;
import com.netspace.pad.library.R;

public class MaxWidthHorizontalScrollView extends HorizontalScrollView {
    private final int defaultWidth = 200;
    private int maxWidth;

    public MaxWidthHorizontalScrollView(Context context) {
        super(context);
    }

    public MaxWidthHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    public MaxWidthHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    @TargetApi(21)
    public MaxWidthHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init(context, attrs);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxWidthHorizontalScrollView);
            this.maxWidth = styledAttrs.getDimensionPixelSize(R.styleable.MaxWidthHorizontalScrollView_maxWidth, 200);
            styledAttrs.recycle();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(this.maxWidth, Integer.MIN_VALUE), heightMeasureSpec);
    }
}
