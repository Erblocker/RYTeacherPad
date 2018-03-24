package com.joanzapata.iconify.internal;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.style.ReplacementSpan;
import com.joanzapata.iconify.Icon;

public class CustomTypefaceSpan extends ReplacementSpan {
    private static final float BASELINE_RATIO = 0.14285715f;
    private static final Paint LOCAL_PAINT = new Paint();
    private static final int ROTATION_DURATION = 2000;
    private static final Rect TEXT_BOUNDS = new Rect();
    private final String icon;
    private final int iconColor;
    private final float iconSizePx;
    private final float iconSizeRatio;
    private final boolean rotate;
    private final long rotationStartTime = System.currentTimeMillis();
    private final Typeface type;

    public CustomTypefaceSpan(Icon icon, Typeface type, float iconSizePx, float iconSizeRatio, int iconColor, boolean rotate) {
        this.rotate = rotate;
        this.icon = String.valueOf(icon.character());
        this.type = type;
        this.iconSizePx = iconSizePx;
        this.iconSizeRatio = iconSizeRatio;
        this.iconColor = iconColor;
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        LOCAL_PAINT.set(paint);
        applyCustomTypeFace(LOCAL_PAINT, this.type);
        LOCAL_PAINT.getTextBounds(this.icon, 0, 1, TEXT_BOUNDS);
        if (fm != null) {
            fm.descent = (int) (((float) TEXT_BOUNDS.height()) * BASELINE_RATIO);
            fm.ascent = -(TEXT_BOUNDS.height() - fm.descent);
            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }
        return TEXT_BOUNDS.width();
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        applyCustomTypeFace(paint, this.type);
        paint.getTextBounds(this.icon, 0, 1, TEXT_BOUNDS);
        canvas.save();
        if (this.rotate) {
            canvas.rotate((((float) (System.currentTimeMillis() - this.rotationStartTime)) / 2000.0f) * 360.0f, x + (((float) TEXT_BOUNDS.width()) / 2.0f), (((float) y) - (((float) TEXT_BOUNDS.height()) / 2.0f)) + (((float) TEXT_BOUNDS.height()) * BASELINE_RATIO));
        }
        canvas.drawText(this.icon, x - ((float) TEXT_BOUNDS.left), ((float) (y - TEXT_BOUNDS.bottom)) + (((float) TEXT_BOUNDS.height()) * BASELINE_RATIO), paint);
        canvas.restore();
    }

    public boolean isAnimated() {
        return this.rotate;
    }

    private void applyCustomTypeFace(Paint paint, Typeface tf) {
        paint.setFakeBoldText(false);
        paint.setTextSkewX(0.0f);
        paint.setTypeface(tf);
        if (this.rotate) {
            paint.clearShadowLayer();
        }
        if (this.iconSizeRatio > 0.0f) {
            paint.setTextSize(paint.getTextSize() * this.iconSizeRatio);
        } else if (this.iconSizePx > 0.0f) {
            paint.setTextSize(this.iconSizePx);
        }
        if (this.iconColor < Integer.MAX_VALUE) {
            paint.setColor(this.iconColor);
        }
    }
}
