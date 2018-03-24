package com.netspace.library.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView {
    private int _overrideHeight = 360;
    private int _overrideWidth = 480;

    public CustomSurfaceView(Context context) {
        super(context);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void resizeVideo(int width, int height) {
        this._overrideHeight = height;
        this._overrideWidth = width;
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this._overrideWidth, this._overrideHeight);
    }
}
