package com.netspace.library.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.OvalPoint;
import com.netspace.library.utilities.MoveableObject;

public class OvalGraphic implements DrawComponentGraphic {
    private int mColor;
    private int mLineWidth;
    private Paint mPaint = new Paint();
    private RectF mRect;
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
        this.mRect = new RectF();
        return true;
    }

    public String getName() {
        return "椭圆形";
    }

    public boolean addPoint(CustomGraphicCanvas DrawCanvas, float fX, float fY) {
        if (DrawCanvas.getDataCount() == 2) {
            return false;
        }
        return true;
    }

    public void onDrawPreviewContent(CustomGraphicCanvas DrawCanvas, Canvas canvas) {
        if (DrawCanvas.getDataCount() >= 2) {
            this.mRect.left = this.mfX[0];
            this.mRect.top = this.mfY[0];
            this.mRect.right = this.mfX[1];
            this.mRect.bottom = this.mfY[1];
            canvas.drawOval(this.mRect, this.mPaint);
        } else if (DrawCanvas.getDataCount() == 0) {
            DrawCanvas.drawTipText(canvas, "请先通过点击一个点来确定椭圆左上角");
        } else if (DrawCanvas.getDataCount() == 1) {
            canvas.drawCircle(this.mfX[0], this.mfY[0], 5.0f, this.mPaint);
            DrawCanvas.drawTipText(canvas, "下面请通过点击另外一个点来确定椭圆右下角");
        }
    }

    public boolean measureToDrawView(CustomGraphicCanvas DrawCanvas, DrawView drawView, float fXOffset, float fYOffset) {
        float fScale = 1.0f / drawView.getScale();
        drawView.addPoint(new OvalPoint((this.mfX[0] + fXOffset) * fScale, (this.mfY[0] + fYOffset) * fScale, (this.mfX[1] + fXOffset) * fScale, (this.mfY[1] + fYOffset) * fScale, this.mColor, (int) (((float) this.mLineWidth) * fScale)));
        return true;
    }

    public void onPrepareMoveObject(CustomGraphicCanvas DrawCanvas, MoveableObject MoveableObject) {
        MoveableObject.setAllowResize(true);
    }

    public void onMoveObjectResize(CustomGraphicCanvas DrawCanvas) {
        this.mfX[0] = (float) DrawCanvas.getPadding();
        this.mfY[0] = (float) DrawCanvas.getPadding();
        this.mfX[1] = (float) (DrawCanvas.getWidth() - DrawCanvas.getPadding());
        this.mfY[1] = (float) (DrawCanvas.getHeight() - DrawCanvas.getPadding());
    }
}
