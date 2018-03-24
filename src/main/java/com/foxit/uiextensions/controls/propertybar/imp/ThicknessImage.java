package com.foxit.uiextensions.controls.propertybar.imp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.internal.view.SupportMenu;
import android.util.AttributeSet;
import android.view.View;
import com.foxit.uiextensions.R;

public class ThicknessImage extends View {
    private float MAX_THICKNESS;
    private float mBorderThickness;
    private int mColor;
    private float mMax_Thickness_px;
    private Paint mPaint;

    public ThicknessImage(Context context) {
        this(context, null);
    }

    public ThicknessImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThicknessImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.MAX_THICKNESS = 60.0f;
        this.mMax_Thickness_px = this.MAX_THICKNESS * (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(1.0f);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThicknessImage);
        this.mColor = typedArray.getColor(R.styleable.ThicknessImage_borderColor, SupportMenu.CATEGORY_MASK);
        float borderThickness = typedArray.getDimension(R.styleable.ThicknessImage_borderThickness, 1.0f);
        if (borderThickness * 2.0f > this.mMax_Thickness_px) {
            this.mBorderThickness = this.mMax_Thickness_px / 2.0f;
        } else if (borderThickness < 1.0f) {
            this.mBorderThickness = 1.0f;
        } else {
            this.mBorderThickness = borderThickness;
        }
        typedArray.recycle();
    }

    protected void onDraw(Canvas canvas) {
        this.mPaint.setColor(this.mColor);
        canvas.drawCircle(((float) getWidth()) / 2.0f, ((float) getHeight()) / 2.0f, this.mBorderThickness, this.mPaint);
        super.onDraw(canvas);
    }

    public void setColor(int color) {
        this.mColor = color;
        invalidate();
    }

    public void setBorderThickness(float borderThickness) {
        if (borderThickness * 2.0f > this.mMax_Thickness_px) {
            this.mBorderThickness = this.mMax_Thickness_px / 2.0f;
        } else if (borderThickness < 1.0f) {
            this.mBorderThickness = 1.0f;
        } else {
            this.mBorderThickness = borderThickness;
        }
        invalidate();
    }
}
