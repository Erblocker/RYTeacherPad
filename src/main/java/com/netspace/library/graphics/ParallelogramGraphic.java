package com.netspace.library.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.FriendlyPoint;
import com.netspace.library.controls.Point;
import com.netspace.library.utilities.MoveableObject;

public class ParallelogramGraphic implements DrawComponentGraphic {
    private int mColor;
    private int mLineWidth;
    private Paint mPaint = new Paint();
    private float[] mfX;
    private float[] mfY;

    public boolean init(CustomGraphicCanvas DrawCanvas) {
        this.mfX = DrawCanvas.getXPoints();
        this.mfY = DrawCanvas.getYPoints();
        this.mColor = DrawCanvas.getColor();
        this.mLineWidth = DrawCanvas.getLineWidth();
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setColor(this.mColor);
        this.mPaint.setStrokeWidth((float) this.mLineWidth);
        this.mPaint.setAntiAlias(true);
        return true;
    }

    public String getName() {
        return "平行四边形";
    }

    public boolean addPoint(CustomGraphicCanvas DrawCanvas, float fX, float fY) {
        if (DrawCanvas.getDataCount() != 3) {
            return true;
        }
        DrawCanvas.addPoint(this.mfX[2] - (this.mfX[1] - this.mfX[0]), this.mfY[2] - (this.mfY[1] - this.mfY[0]));
        return false;
    }

    public void onDrawPreviewContent(CustomGraphicCanvas DrawCanvas, Canvas canvas) {
        if (DrawCanvas.getDataCount() >= 4) {
            int[] nPointsIndex = new int[8];
            nPointsIndex[1] = 1;
            nPointsIndex[3] = 3;
            nPointsIndex[4] = 1;
            nPointsIndex[5] = 2;
            nPointsIndex[6] = 2;
            nPointsIndex[7] = 3;
            for (int i = 0; i < nPointsIndex.length; i += 2) {
                canvas.drawLine(this.mfX[nPointsIndex[i]], this.mfY[nPointsIndex[i]], this.mfX[nPointsIndex[i + 1]], this.mfY[nPointsIndex[i + 1]], this.mPaint);
            }
        } else if (DrawCanvas.getDataCount() == 0) {
            DrawCanvas.drawTipText(canvas, "请先确定平行四边形的第一个点");
        } else if (DrawCanvas.getDataCount() == 1) {
            canvas.drawCircle(this.mfX[0], this.mfY[0], 5.0f, this.mPaint);
            DrawCanvas.drawTipText(canvas, "请继续确定平行四边形的第二个点");
        } else if (DrawCanvas.getDataCount() == 2) {
            canvas.drawCircle(this.mfX[0], this.mfY[0], 5.0f, this.mPaint);
            canvas.drawCircle(this.mfX[1], this.mfY[1], 5.0f, this.mPaint);
            DrawCanvas.drawTipText(canvas, "请继续确定平行四边形的第三个点");
        }
    }

    public boolean measureToDrawView(CustomGraphicCanvas DrawCanvas, DrawView drawView, float fXOffset, float fYOffset) {
        Point lastPoint = null;
        if (DrawCanvas.getDataCount() < 3) {
            return false;
        }
        float fScale = 1.0f / drawView.getScale();
        int[] nPointsIndex = new int[5];
        nPointsIndex[1] = 1;
        nPointsIndex[2] = 2;
        nPointsIndex[3] = 3;
        for (int i = 0; i < nPointsIndex.length; i++) {
            if (i == 0) {
                Point startPoint = new Point((this.mfX[nPointsIndex[i]] + fXOffset) * fScale, (this.mfY[nPointsIndex[i]] + fYOffset) * fScale, this.mColor, (int) (((float) this.mLineWidth) * fScale));
                drawView.addPoint(startPoint);
                lastPoint = startPoint;
            } else {
                Point endPoint = new FriendlyPoint((this.mfX[nPointsIndex[i]] + fXOffset) * fScale, (this.mfY[nPointsIndex[i]] + fYOffset) * fScale, this.mColor, lastPoint, (int) (((float) this.mLineWidth) * fScale));
                drawView.addPoint(endPoint);
                lastPoint = endPoint;
            }
        }
        return true;
    }

    public void onPrepareMoveObject(CustomGraphicCanvas DrawCanvas, MoveableObject MoveableObject) {
        MoveableObject.setAllowResize(false);
    }

    public void onMoveObjectResize(CustomGraphicCanvas DrawCanvas) {
    }
}
