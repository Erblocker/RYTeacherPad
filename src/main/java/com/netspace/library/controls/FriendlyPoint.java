package com.netspace.library.controls;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class FriendlyPoint extends Point {
    public final Point neighbour;

    public FriendlyPoint(float x, float y, int col, Point neighbour, int width) {
        super(x, y, col, width);
        this.neighbour = neighbour;
    }

    public void addToPath(Path path, float fScale) {
        if (this.neighbour != null) {
            path.quadTo(this.neighbour.x * fScale, this.neighbour.y * fScale, this.x * fScale, this.y * fScale);
        } else {
            path.lineTo(this.x * fScale, this.y * fScale);
        }
    }

    public void draw(Canvas canvas, Paint paint, float fScale) {
        paint.setColor(this.col);
        float fX1 = this.x * fScale;
        float fY1 = this.y * fScale;
        float fX2 = this.neighbour.x * fScale;
        float fY2 = this.neighbour.y * fScale;
        float fLength = (float) Math.sqrt((double) (((fX1 - fX2) * (fX1 - fX2)) + ((fY1 - fY2) * (fY1 - fY2))));
        float fWidthDiff = (float) (this.width - this.neighbour.width);
        if (fWidthDiff == 0.0f) {
            paint.setStrokeWidth(((float) this.width) * fScale);
            canvas.drawLine(this.x * fScale, this.y * fScale, this.neighbour.x * fScale, this.neighbour.y * fScale, paint);
        } else {
            float fWidthSep = fWidthDiff / fLength;
            float fXDiff = (fX1 - fX2) / fLength;
            float fYDiff = (fY1 - fY2) / fLength;
            float fY22 = fY2;
            float fX22 = fX2;
            for (float i = 0.0f; i < fLength; i += 1.0f) {
                if (((double) (((float) this.neighbour.width) + (fWidthSep * i))) > 0.5d) {
                    paint.setStrokeWidth((((float) this.neighbour.width) + (fWidthSep * i)) * fScale);
                }
                canvas.drawLine(fX22, fY22, fX22 + fXDiff, fY22 + fYDiff, paint);
                fX22 += fXDiff;
                fY22 += fYDiff;
            }
            canvas.drawLine(fX22, fY22, this.neighbour.x * fScale, this.neighbour.y * fScale, paint);
        }
        canvas.drawCircle(this.x * fScale, this.y * fScale, (((float) this.width) * fScale) / 2.0f, paint);
    }

    public String toString() {
        return this.x + ", " + this.y + ", " + this.col + "; N[" + this.neighbour.toString() + "]";
    }
}
