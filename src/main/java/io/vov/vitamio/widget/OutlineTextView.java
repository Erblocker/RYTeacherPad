package io.vov.vitamio.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.TextView;

public class OutlineTextView extends TextView {
    private int mAscent = 0;
    private int mBorderColor;
    private float mBorderSize;
    private int mColor;
    private boolean mIncludePad = true;
    private float mSpacingAdd = 0.0f;
    private float mSpacingMult = 1.0f;
    private String mText = "";
    private TextPaint mTextPaint;
    private TextPaint mTextPaintOutline;

    public OutlineTextView(Context context) {
        super(context);
        initPaint();
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    private void initPaint() {
        this.mTextPaint = new TextPaint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(getTextSize());
        this.mTextPaint.setColor(this.mColor);
        this.mTextPaint.setStyle(Style.FILL);
        this.mTextPaint.setTypeface(getTypeface());
        this.mTextPaintOutline = new TextPaint();
        this.mTextPaintOutline.setAntiAlias(true);
        this.mTextPaintOutline.setTextSize(getTextSize());
        this.mTextPaintOutline.setColor(this.mBorderColor);
        this.mTextPaintOutline.setStyle(Style.STROKE);
        this.mTextPaintOutline.setTypeface(getTypeface());
        this.mTextPaintOutline.setStrokeWidth(this.mBorderSize);
    }

    public void setText(String text) {
        super.setText(text);
        this.mText = text.toString();
        requestLayout();
        invalidate();
    }

    public void setTextSize(float size) {
        super.setTextSize(size);
        requestLayout();
        invalidate();
        initPaint();
    }

    public void setTextColor(int color) {
        super.setTextColor(color);
        this.mColor = color;
        invalidate();
        initPaint();
    }

    public void setShadowLayer(float radius, float dx, float dy, int color) {
        super.setShadowLayer(radius, dx, dy, color);
        this.mBorderSize = radius;
        this.mBorderColor = color;
        requestLayout();
        invalidate();
        initPaint();
    }

    public void setTypeface(Typeface tf, int style) {
        super.setTypeface(tf, style);
        requestLayout();
        invalidate();
        initPaint();
    }

    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
        requestLayout();
        invalidate();
        initPaint();
    }

    protected void onDraw(Canvas canvas) {
        new StaticLayout(getText(), this.mTextPaintOutline, getWidth(), Alignment.ALIGN_CENTER, this.mSpacingMult, this.mSpacingAdd, this.mIncludePad).draw(canvas);
        new StaticLayout(getText(), this.mTextPaint, getWidth(), Alignment.ALIGN_CENTER, this.mSpacingMult, this.mSpacingAdd, this.mIncludePad).draw(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ex = (int) ((this.mBorderSize * 2.0f) + 1.0f);
        setMeasuredDimension(measureWidth(widthMeasureSpec) + ex, (measureHeight(heightMeasureSpec) * new StaticLayout(getText(), this.mTextPaintOutline, measureWidth(widthMeasureSpec), Alignment.ALIGN_CENTER, this.mSpacingMult, this.mSpacingAdd, this.mIncludePad).getLineCount()) + ex);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return specSize;
        }
        int result = (((int) this.mTextPaintOutline.measureText(this.mText)) + getPaddingLeft()) + getPaddingRight();
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(result, specSize);
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        this.mAscent = (int) this.mTextPaintOutline.ascent();
        if (specMode == 1073741824) {
            return specSize;
        }
        int result = (((int) (((float) (-this.mAscent)) + this.mTextPaintOutline.descent())) + getPaddingTop()) + getPaddingBottom();
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(result, specSize);
        }
        return result;
    }
}
