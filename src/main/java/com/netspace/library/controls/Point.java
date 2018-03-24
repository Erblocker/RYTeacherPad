package com.netspace.library.controls;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;

public class Point {
    public final int col;
    public final int width;
    public final float x;
    public final float y;

    public Point(float x, float y, int col, int width) {
        this.x = x;
        this.y = y;
        this.col = col;
        this.width = width;
    }

    public void addToPath(Path path, float fScale) {
        path.addCircle(this.x * fScale, this.y * fScale, (((float) this.width) * fScale) / 2.0f, Direction.CW);
    }

    public void draw(Canvas canvas, Paint paint, float fScale) {
        float fTargetX = this.x * fScale;
        float fTargetY = this.y * fScale;
        paint.setColor(this.col);
        canvas.drawCircle(fTargetX, fTargetY, (((float) this.width) * fScale) / 2.0f, paint);
    }

    public String toString() {
        return this.x + ", " + this.y + ", " + this.col;
    }
}
