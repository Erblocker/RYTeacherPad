package com.netspace.library.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.netspace.library.components.DrawComponent.DrawComponentGraphic;
import com.netspace.library.controls.CustomGraphicCanvas;
import com.netspace.library.controls.DrawView;
import com.netspace.library.controls.FriendlyPoint;
import com.netspace.library.controls.Point;
import com.netspace.library.utilities.MoveableObject;

public class MultiLineGraphic implements DrawComponentGraphic {
    public boolean init(CustomGraphicCanvas DrawCanvas) {
        return true;
    }

    public String getName() {
        return "直线和多边形";
    }

    public boolean addPoint(CustomGraphicCanvas DrawCanvas, float fX, float fY) {
        return true;
    }

    public void onDrawPreviewContent(CustomGraphicCanvas DrawCanvas, Canvas canvas) {
        if (DrawCanvas.getDataCount() == 0) {
            DrawCanvas.drawTipText(canvas, "点击任意位置作为起点");
        } else if (DrawCanvas.getDataCount() == 1) {
            DrawCanvas.drawTipText(canvas, "点击其余位置将产生直线连接上一个点");
        } else {
            DrawCanvas.drawTipText(canvas, "可以画更多的线段，画完后请点击对勾确认");
        }
        float[] fX = DrawCanvas.getXPoints();
        float[] fY = DrawCanvas.getYPoints();
        Paint paint = new Paint();
        int nColor = DrawCanvas.getColor();
        int nWidth = DrawCanvas.getLineWidth();
        paint.setColor(nColor);
        paint.setStrokeWidth((float) nWidth);
        if (DrawCanvas.getDataCount() >= 1) {
            canvas.drawCircle(fX[0], fY[0], (float) (nWidth / 2), paint);
            for (int i = 0; i < DrawCanvas.getDataCount() - 1; i++) {
                canvas.drawLine(fX[i], fY[i], fX[i + 1], fY[i + 1], paint);
            }
        }
    }

    public boolean measureToDrawView(CustomGraphicCanvas DrawCanvas, DrawView drawView, float fXOffset, float fYOffset) {
        float[] fX = DrawCanvas.getXPoints();
        float[] fY = DrawCanvas.getYPoints();
        int nColor = DrawCanvas.getColor();
        int nWidth = DrawCanvas.getLineWidth();
        Point lastPoint = null;
        float fScale = 1.0f / drawView.getScale();
        for (int i = 0; i < DrawCanvas.getDataCount(); i++) {
            if (i == 0) {
                Point startPoint = new Point((fX[i] + fXOffset) * fScale, (fY[i] + fYOffset) * fScale, nColor, (int) (((float) nWidth) * fScale));
                drawView.addPoint(startPoint);
                lastPoint = startPoint;
            } else {
                Point endPoint = new FriendlyPoint((fX[i] + fXOffset) * fScale, (fY[i] + fYOffset) * fScale, nColor, lastPoint, (int) (((float) nWidth) * fScale));
                drawView.addPoint(endPoint);
                lastPoint = endPoint;
            }
        }
        return true;
    }

    public void onPrepareMoveObject(CustomGraphicCanvas DrawCanvas, MoveableObject MoveableObject) {
    }

    public void onMoveObjectResize(CustomGraphicCanvas DrawCanvas) {
    }
}
