package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class ColorPanelView extends View {
    private static final float BORDER_WIDTH_PX = 1.0f;
    private static float mDensity = BORDER_WIDTH_PX;
    private AlphaPatternDrawable mAlphaPattern;
    private int mBorderColor;
    private Paint mBorderPaint;
    private int mColor;
    private Paint mColorPaint;
    private RectF mColorRect;
    private RectF mDrawingRect;

    public ColorPanelView(Context context) {
        this(context, null);
    }

    public ColorPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mBorderColor = -9539986;
        this.mColor = -16777216;
        init();
    }

    private void init() {
        this.mBorderPaint = new Paint();
        this.mColorPaint = new Paint();
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }

    protected void onDraw(Canvas canvas) {
        RectF rect = this.mColorRect;
        this.mBorderPaint.setColor(this.mBorderColor);
        canvas.drawRect(this.mDrawingRect, this.mBorderPaint);
        if (this.mAlphaPattern != null) {
            this.mAlphaPattern.draw(canvas);
        }
        this.mColorPaint.setColor(this.mColor);
        canvas.drawRect(rect, this.mColorPaint);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mDrawingRect = new RectF();
        this.mDrawingRect.left = (float) getPaddingLeft();
        this.mDrawingRect.right = (float) (w - getPaddingRight());
        this.mDrawingRect.top = (float) getPaddingTop();
        this.mDrawingRect.bottom = (float) (h - getPaddingBottom());
        setUpColorRect();
    }

    private void setUpColorRect() {
        RectF dRect = this.mDrawingRect;
        this.mColorRect = new RectF(dRect.left + BORDER_WIDTH_PX, dRect.top + BORDER_WIDTH_PX, dRect.right - BORDER_WIDTH_PX, dRect.bottom - BORDER_WIDTH_PX);
        this.mAlphaPattern = new AlphaPatternDrawable((int) (5.0f * mDensity));
        this.mAlphaPattern.setBounds(Math.round(this.mColorRect.left), Math.round(this.mColorRect.top), Math.round(this.mColorRect.right), Math.round(this.mColorRect.bottom));
    }

    public void setColor(int color) {
        this.mColor = color;
        invalidate();
    }

    public int getColor() {
        return this.mColor;
    }

    public void setBorderColor(int color) {
        this.mBorderColor = color;
        invalidate();
    }

    public int getBorderColor() {
        return this.mBorderColor;
    }
}
