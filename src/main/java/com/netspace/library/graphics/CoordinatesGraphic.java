package com.netspace.library.graphics;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.FriendlyPoint;
import com.netspace.library.controls.Point;
import com.netspace.library.utilities.MoveableObject;

public class CoordinatesGraphic implements DrawComponentGraphic {
    private int mColor;
    private final int mIndicateSize = 10;
    private int mLineWidth;
    private Paint mPaint = new Paint();
    private Paint mSecondPaint = new Paint();
    private float[] mfX;
    private float[] mfY;

    public boolean init(CustomGraphicCanvas DrawCanvas) {
        this.mfX = DrawCanvas.getXPoints();
        this.mfY = DrawCanvas.getYPoints();
        this.mColor = DrawCanvas.getColor();
        this.mLineWidth = 3;
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setColor(this.mColor);
        this.mPaint.setStrokeWidth((float) this.mLineWidth);
        this.mPaint.setAntiAlias(true);
        this.mSecondPaint.setColor(-16777216);
        this.mSecondPaint.setStyle(Style.STROKE);
        this.mSecondPaint.setAntiAlias(true);
        this.mSecondPaint.setPathEffect(new DashPathEffect(new float[]{10.0f, 10.0f}, 5.0f));
        return true;
    }

    public String getName() {
        return "直角坐标系";
    }

    public boolean addPoint(CustomGraphicCanvas DrawCanvas, float fX, float fY) {
        if (DrawCanvas.getDataCount() != 3) {
            return true;
        }
        float fX1 = this.mfX[1];
        float fY1 = this.mfY[1];
        float fX2 = this.mfX[2];
        float fY2 = this.mfY[2];
        float fCenterX = this.mfX[0];
        float fCenterY = this.mfY[0];
        this.mfX[1] = fX1;
        this.mfY[1] = fCenterY;
        this.mfX[2] = fCenterX;
        this.mfY[2] = fY1;
        DrawCanvas.addPoint(fX2, fCenterY);
        DrawCanvas.addPoint(fCenterX, fY2);
        DrawCanvas.addPoint(fCenterX - 10.0f, fY1 + 10.0f);
        DrawCanvas.addPoint(fCenterX + 10.0f, fY1 + 10.0f);
        DrawCanvas.addPoint(fX2 - 10.0f, fCenterY - 10.0f);
        DrawCanvas.addPoint(fX2 - 10.0f, fCenterY + 10.0f);
        return false;
    }

    public void onDrawPreviewContent(CustomGraphicCanvas DrawCanvas, Canvas canvas) {
        if (DrawCanvas.getDataCount() >= 8) {
            int[] nPointsIndex = new int[16];
            nPointsIndex[1] = 1;
            nPointsIndex[3] = 2;
            nPointsIndex[5] = 3;
            nPointsIndex[7] = 4;
            nPointsIndex[8] = 2;
            nPointsIndex[9] = 5;
            nPointsIndex[10] = 2;
            nPointsIndex[11] = 6;
            nPointsIndex[12] = 3;
            nPointsIndex[13] = 7;
            nPointsIndex[14] = 3;
            nPointsIndex[15] = 8;
            for (int i = 0; i < nPointsIndex.length; i += 2) {
                canvas.drawLine(this.mfX[nPointsIndex[i]], this.mfY[nPointsIndex[i]], this.mfX[nPointsIndex[i + 1]], this.mfY[nPointsIndex[i + 1]], this.mPaint);
            }
        } else if (DrawCanvas.getDataCount() == 0) {
            DrawCanvas.drawTipText(canvas, "请先确定直角坐标系原点");
        } else if (DrawCanvas.getDataCount() == 1) {
            canvas.drawCircle(this.mfX[0], this.mfY[0], 5.0f, this.mPaint);
            canvas.drawLine(this.mfX[0], 0.0f, this.mfX[0], (float) DrawCanvas.getHeight(), this.mSecondPaint);
            canvas.drawLine(0.0f, this.mfY[0], (float) DrawCanvas.getWidth(), this.mfY[0], this.mSecondPaint);
            DrawCanvas.drawTipText(canvas, "请确定直角坐标系左上角位置");
        } else if (DrawCanvas.getDataCount() == 2) {
            canvas.drawCircle(this.mfX[0], this.mfY[0], 5.0f, this.mPaint);
            canvas.drawCircle(this.mfX[1], this.mfY[1], 5.0f, this.mPaint);
            canvas.drawLine(this.mfX[0], 0.0f, this.mfX[0], (float) DrawCanvas.getHeight(), this.mSecondPaint);
            canvas.drawLine(0.0f, this.mfY[0], (float) DrawCanvas.getWidth(), this.mfY[0], this.mSecondPaint);
            DrawCanvas.drawTipText(canvas, "请继续确定直角坐标系右下角位置");
        }
    }

    public boolean measureToDrawView(CustomGraphicCanvas DrawCanvas, DrawView drawView, float fXOffset, float fYOffset) {
        if (DrawCanvas.getDataCount() < 3) {
            return false;
        }
        float fScale = 1.0f / drawView.getScale();
        int[] nPointsIndex = new int[16];
        nPointsIndex[1] = 1;
        nPointsIndex[3] = 2;
        nPointsIndex[5] = 3;
        nPointsIndex[7] = 4;
        nPointsIndex[8] = 2;
        nPointsIndex[9] = 5;
        nPointsIndex[10] = 2;
        nPointsIndex[11] = 6;
        nPointsIndex[12] = 3;
        nPointsIndex[13] = 7;
        nPointsIndex[14] = 3;
        nPointsIndex[15] = 8;
        for (int i = 0; i < nPointsIndex.length; i += 2) {
            Point startPoint = new Point((this.mfX[nPointsIndex[i]] + fXOffset) * fScale, (this.mfY[nPointsIndex[i]] + fYOffset) * fScale, this.mColor, (int) (((float) this.mLineWidth) * fScale));
            drawView.addPoint(startPoint);
            Point endPoint = new FriendlyPoint((this.mfX[nPointsIndex[i + 1]] + fXOffset) * fScale, (this.mfY[nPointsIndex[i + 1]] + fYOffset) * fScale, this.mColor, startPoint, (int) (((float) this.mLineWidth) * fScale));
            drawView.addPoint(endPoint);
            Point lastPoint = endPoint;
        }
        return true;
    }

    public void onPrepareMoveObject(CustomGraphicCanvas DrawCanvas, MoveableObject MoveableObject) {
        MoveableObject.setAllowResize(false);
    }

    public void onMoveObjectResize(CustomGraphicCanvas DrawCanvas) {
    }
}
