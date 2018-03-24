package com.netspace.library.controls;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class OvalPoint extends Point {
    public float mx2;
    public float my2;

    public OvalPoint(float x, float y, float x2, float y2, int col, int nStockWidth) {
        super(x, y, col, nStockWidth);
        this.mx2 = x2;
        this.my2 = y2;
    }

    public void draw(Canvas canvas, Paint paint, float fScale) {
        paint.setColor(this.col);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(((float) this.width) * fScale);
        RectF oval = new RectF();
        oval.left = this.x * fScale;
        oval.top = this.y * fScale;
        oval.right = this.mx2 * fScale;
        oval.bottom = this.my2 * fScale;
        canvas.drawOval(oval, paint);
        paint.setStyle(Style.FILL);
    }

    public String toString() {
        return this.x + ", " + this.y + ", " + this.col + "," + this.width + "; ";
    }
}
