package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class LinedEditText extends EditText {
    private Paint mPaint;
    private Rect mRect;

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LinedEditText(Context context) {
        super(context);
        init();
    }

    public void init() {
        this.mRect = new Rect();
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setColor(-2147483393);
    }

    public void setLineColor(int nColor) {
        this.mPaint.setColor(nColor);
    }

    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        Rect r = this.mRect;
        Paint paint = this.mPaint;
        for (int i = 0; i < count; i++) {
            int baseline = getLineBounds(i, r);
            canvas.drawLine((float) r.left, (float) (baseline + 1), (float) r.right, (float) (baseline + 1), paint);
        }
        super.onDraw(canvas);
    }
}
