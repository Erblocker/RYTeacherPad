package com.netspace.library.controls;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class CirclePoint extends Point {
    private float mStockWidth;

    public CirclePoint(float x, float y, int col, int width, float nStockWidth) {
        super(x, y, col, width);
        this.mStockWidth = nStockWidth;
    }

    public void draw(Canvas canvas, Paint paint, float fScale) {
        paint.setColor(this.col);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(this.mStockWidth * fScale);
        canvas.drawCircle(this.x * fScale, this.y * fScale, ((float) this.width) * fScale, paint);
        paint.setStyle(Style.FILL);
    }

    public String toString() {
        return this.x + ", " + this.y + ", " + this.col + "," + this.width + "; ";
    }

    public float getStockWidth() {
        return this.mStockWidth;
    }
}
