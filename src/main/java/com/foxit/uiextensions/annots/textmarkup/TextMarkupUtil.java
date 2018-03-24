package com.foxit.uiextensions.annots.textmarkup;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;

public class TextMarkupUtil {
    public static float SQGPI = 3.1415927f;

    public static void stepNormalize(PointF point, float width) {
        double dLen = Math.sqrt((double) ((point.x * point.x) + (point.y * point.y)));
        point.x = ((float) (((double) point.x) / dLen)) * width;
        point.y = ((float) (((double) point.y) / dLen)) * width;
    }

    public static void stepRotate(double pi, PointF point) {
        double cosValue = Math.cos(pi);
        double sinValue = Math.sin(pi);
        double xx = (double) point.x;
        double yy = (double) point.y;
        point.x = (float) ((xx * cosValue) - (yy * sinValue));
        point.y = (float) ((xx * sinValue) - (yy * cosValue));
    }

    public static void resetDrawLineWidth(PDFViewCtrl pdfViewer, int pageIndex, Paint paint, float top, float bottom) {
        float LineWidth = Math.abs(top - bottom) / 16.0f;
        if (LineWidth < 1.0f) {
            LineWidth = 1.0f;
        }
        RectF rect = new RectF(0.0f, 0.0f, LineWidth, LineWidth);
        RectF deviceRt = new RectF();
        pdfViewer.convertPdfRectToPageViewRect(rect, deviceRt, pageIndex);
        paint.setStrokeWidth(Math.abs(deviceRt.width()));
    }

    public static Rect rectRoundOut(RectF rectF, int roundSize) {
        Rect rect = new Rect();
        rectF.roundOut(rect);
        rect.inset(-roundSize, -roundSize);
        return rect;
    }
}
