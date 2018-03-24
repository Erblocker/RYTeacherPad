package com.netspace.library.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.controls.CirclePoint;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.DrawView;
import com.netspace.library.utilities.MoveableObject;

public class CircleGraphic implements DrawComponentGraphic {
    private int mColor;
    private int mLineWidth;
    private Paint mPaint = new Paint();
    private float mfR;
    private float[] mfX;
    private float[] mfY;

    public boolean init(CustomGraphicCanvas DrawCanvas) {
        this.mfX = DrawCanvas.getXPoints();
        this.mfY = DrawCanvas.getYPoints();
        this.mColor = DrawCanvas.getColor();
        this.mLineWidth = DrawCanvas.getLineWidth();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setColor(this.mColor);
        this.mPaint.setStrokeWidth((float) this.mLineWidth);
        this.mfR = 0.0f;
        return true;
    }

    public String getName() {
        return "圆形";
    }

    public boolean addPoint(CustomGraphicCanvas DrawCanvas, float fX, float fY) {
        if (DrawCanvas.getDataCount() != 2) {
            return true;
        }
        this.mfR = (float) Math.sqrt((double) ((Math.abs(this.mfY[0] - this.mfY[1]) * Math.abs(this.mfY[0] - this.mfY[1])) + (Math.abs(this.mfX[0] - this.mfX[1]) * Math.abs(this.mfX[0] - this.mfX[1]))));
        return false;
    }

    public void onDrawPreviewContent(CustomGraphicCanvas DrawCanvas, Canvas canvas) {
        if (DrawCanvas.getDataCount() >= 2) {
            canvas.drawCircle(this.mfX[0], this.mfY[0], this.mfR, this.mPaint);
        } else if (DrawCanvas.getDataCount() == 0) {
            DrawCanvas.drawTipText(canvas, "请先通过点击一个点来确定圆心");
        } else if (DrawCanvas.getDataCount() == 1) {
            canvas.drawCircle(this.mfX[0], this.mfY[0], 5.0f, this.mPaint);
            DrawCanvas.drawTipText(canvas, "下面请通过点击另外一个点来确定半径");
        }
    }

    public boolean measureToDrawView(CustomGraphicCanvas DrawCanvas, DrawView drawView, float fXOffset, float fYOffset) {
        float fScale = 1.0f / drawView.getScale();
        drawView.addPoint(new CirclePoint((this.mfX[0] + fXOffset) * fScale, (this.mfY[0] + fYOffset) * fScale, this.mColor, (int) (this.mfR * fScale), (float) ((int) (((float) this.mLineWidth) * fScale))));
        return true;
    }

    public void onPrepareMoveObject(CustomGraphicCanvas DrawCanvas, MoveableObject MoveableObject) {
        float fR = this.mfR + ((float) DrawCanvas.getPadding());
        MoveableObject.setAllowResize(true);
        DrawCanvas.addPoint(this.mfX[0] - fR, this.mfY[0] - fR);
        DrawCanvas.addPoint(this.mfX[0] + fR, this.mfY[0] + fR);
    }

    public void onMoveObjectResize(CustomGraphicCanvas DrawCanvas) {
        this.mfX[0] = (float) (DrawCanvas.getWidth() / 2);
        this.mfY[0] = (float) (DrawCanvas.getHeight() / 2);
        this.mfR = (float) Math.min(DrawCanvas.getWidth() / 2, DrawCanvas.getHeight() / 2);
        this.mfR -= (float) (DrawCanvas.getPadding() * 2);
    }
}
