package com.foxit.uiextensions.annots.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;

public class UIAnnotFrame {
    public static final int CTL_LEFT_BOTTOM = 6;
    public static final int CTL_LEFT_MID = 7;
    public static final int CTL_LEFT_TOP = 0;
    public static final int CTL_MID_BOTTOM = 5;
    public static final int CTL_MID_TOP = 1;
    public static final int CTL_NONE = -1;
    public static final int CTL_RIGHT_BOTTOM = 4;
    public static final int CTL_RIGHT_MID = 3;
    public static final int CTL_RIGHT_TOP = 2;
    public static final int OP_DEFAULT = -1;
    public static final int OP_SCALE = 1;
    public static final int OP_TRANSLATE = 0;
    private static UIAnnotFrame mInstance = null;
    private float mCtlLineWidth = 2.0f;
    private Paint mCtlPaint = new Paint();
    private float mCtlRadius = 5.0f;
    private float mCtlTouchExt = 20.0f;
    private float mFrmLineWidth = 1.0f;
    private Paint mFrmPaint = new Paint();

    public static UIAnnotFrame getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UIAnnotFrame(context);
        }
        return mInstance;
    }

    private UIAnnotFrame(Context context) {
        float d2pFactor = (float) AppDisplay.getInstance(context).dp2px(1.0f);
        this.mFrmLineWidth *= d2pFactor;
        this.mCtlLineWidth *= d2pFactor;
        this.mCtlRadius *= d2pFactor;
        this.mCtlTouchExt *= d2pFactor;
        this.mFrmPaint.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mFrmPaint.setStyle(Style.STROKE);
        this.mFrmPaint.setAntiAlias(true);
        this.mFrmPaint.setStrokeWidth(this.mFrmLineWidth);
        this.mCtlPaint.setStrokeWidth(this.mCtlLineWidth);
    }

    public static RectF calculateBounds(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot) {
        try {
            return calculateBounds(pdfViewCtrl, pageIndex, annot.getRect(), annot.getBorderInfo().getWidth());
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static float getPageViewThickness(PDFViewCtrl pdfViewCtrl, int pageIndex, float thickness) {
        RectF rectF = new RectF(0.0f, 0.0f, thickness, thickness);
        if (pdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex)) {
            return rectF.width();
        }
        return thickness;
    }

    public static RectF calculateBounds(PDFViewCtrl pdfViewCtrl, int pageIndex, RectF docBBox, float docThickness) {
        pdfViewCtrl.convertPdfRectToPageViewRect(docBBox, docBBox, pageIndex);
        float thickness = getPageViewThickness(pdfViewCtrl, pageIndex, docThickness);
        docBBox.inset(((-thickness) / 2.0f) - ((float) AppAnnotUtil.getAnnotBBoxSpace()), ((-thickness) / 2.0f) - ((float) AppAnnotUtil.getAnnotBBoxSpace()));
        return docBBox;
    }

    public static RectF mapBounds(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot, int op, int ctl, float dx, float dy) {
        try {
            RectF bbox = annot.getRect();
            pdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
            calculateOperateMatrix(bbox, op, ctl, dx, dy).mapRect(bbox);
            float thickness = getPageViewThickness(pdfViewCtrl, pageIndex, annot.getBorderInfo().getWidth());
            bbox.inset(((-thickness) / 2.0f) - ((float) AppAnnotUtil.getAnnotBBoxSpace()), ((-thickness) / 2.0f) - ((float) AppAnnotUtil.getAnnotBBoxSpace()));
            return bbox;
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static float[] calculateControls(RectF bounds) {
        float l = bounds.left;
        float t = bounds.top;
        float r = bounds.right;
        float b = bounds.bottom;
        return new float[]{l, t, (l + r) / 2.0f, t, r, t, r, (t + b) / 2.0f, r, b, (l + r) / 2.0f, b, l, b, l, (t + b) / 2.0f};
    }

    public int hitControlTest(RectF bounds, PointF point) {
        float[] ctlPts = calculateControls(bounds);
        RectF area = new RectF();
        for (int i = 0; i < ctlPts.length / 2; i++) {
            area.set(ctlPts[i * 2], ctlPts[(i * 2) + 1], ctlPts[i * 2], ctlPts[(i * 2) + 1]);
            area.inset(-this.mCtlTouchExt, -this.mCtlTouchExt);
            if (area.contains(point.x, point.y)) {
                return i;
            }
        }
        return -1;
    }

    private static Matrix calculateScaleMatrix(RectF bounds, int ctl, float dx, float dy) {
        Matrix matrix = new Matrix();
        float[] ctlPts = calculateControls(bounds);
        float px = ctlPts[ctl * 2];
        float py = ctlPts[(ctl * 2) + 1];
        float oppositeX = 0.0f;
        float oppositeY = 0.0f;
        if (ctl < 4 && ctl >= 0) {
            oppositeX = ctlPts[(ctl * 2) + 8];
            oppositeY = ctlPts[(ctl * 2) + 9];
        } else if (ctl >= 4) {
            oppositeX = ctlPts[(ctl * 2) - 8];
            oppositeY = ctlPts[(ctl * 2) - 7];
        }
        float scaleh = ((px + dx) - oppositeX) / (px - oppositeX);
        float scalev = ((py + dy) - oppositeY) / (py - oppositeY);
        switch (ctl) {
            case 0:
            case 2:
            case 4:
            case 6:
                matrix.postScale(scaleh, scalev, oppositeX, oppositeY);
                break;
            case 1:
            case 5:
                matrix.postScale(1.0f, scalev, oppositeX, oppositeY);
                break;
            case 3:
            case 7:
                matrix.postScale(scaleh, 1.0f, oppositeX, oppositeY);
                break;
        }
        return matrix;
    }

    public static Matrix calculateOperateMatrix(RectF bounds, int op, int ctl, float dx, float dy) {
        Matrix matrix = new Matrix();
        if (op == 1) {
            return calculateScaleMatrix(bounds, ctl, dx, dy);
        }
        matrix.preTranslate(dx, dy);
        return matrix;
    }

    public float getControlExtent() {
        return this.mCtlLineWidth + this.mCtlRadius;
    }

    public void extentBoundsToContainControl(RectF bounds) {
        bounds.inset(-getControlExtent(), -getControlExtent());
    }

    private PointF calculateTranslateCorrection(PDFViewCtrl pdfViewCtrl, int pageIndex, RectF bounds) {
        PointF adjust = new PointF();
        float extent = getControlExtent();
        if (bounds.left < extent) {
            adjust.x = extent - bounds.left;
        }
        if (bounds.top < extent) {
            adjust.y = extent - bounds.top;
        }
        if (bounds.right > ((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - extent) {
            adjust.x = (((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - bounds.right) - extent;
        }
        if (bounds.bottom > ((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - extent) {
            adjust.y = (((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - bounds.bottom) - extent;
        }
        return adjust;
    }

    private PointF calculateScaleCorrection(PDFViewCtrl pdfViewCtrl, int pageIndex, RectF bounds, int ctl) {
        PointF adjust = new PointF();
        float extent = getControlExtent();
        switch (ctl) {
            case 0:
                if (bounds.left < extent) {
                    adjust.x = extent - bounds.left;
                }
                if (bounds.top < extent) {
                    adjust.y = extent - bounds.top;
                    break;
                }
                break;
            case 1:
                if (bounds.top < extent) {
                    adjust.y = extent - bounds.top;
                    break;
                }
                break;
            case 2:
                if (bounds.right > ((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - extent) {
                    adjust.x = (((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - bounds.right) - extent;
                }
                if (bounds.top < extent) {
                    adjust.y = extent - bounds.top;
                    break;
                }
                break;
            case 3:
                if (bounds.right > ((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - extent) {
                    adjust.x = (((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - bounds.right) - extent;
                    break;
                }
                break;
            case 4:
                if (bounds.right > ((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - extent) {
                    adjust.x = (((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - bounds.right) - extent;
                }
                if (bounds.bottom > ((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - extent) {
                    adjust.y = (((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - bounds.bottom) - extent;
                    break;
                }
                break;
            case 5:
                if (bounds.bottom > ((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - extent) {
                    adjust.y = (((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - bounds.bottom) - extent;
                    break;
                }
                break;
            case 6:
                if (bounds.left < extent) {
                    adjust.x = extent - bounds.left;
                }
                if (bounds.bottom > ((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - extent) {
                    adjust.y = (((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - bounds.bottom) - extent;
                    break;
                }
                break;
            case 7:
                if (bounds.left < extent) {
                    adjust.x = extent - bounds.left;
                    break;
                }
                break;
        }
        return adjust;
    }

    public PointF calculateCorrection(PDFViewCtrl pdfViewCtrl, int pageIndex, RectF bounds, int op, int ctl) {
        switch (op) {
            case 0:
                return calculateTranslateCorrection(pdfViewCtrl, pageIndex, bounds);
            case 1:
                return calculateScaleCorrection(pdfViewCtrl, pageIndex, bounds, ctl);
            default:
                return new PointF();
        }
    }

    public static void adjustBounds(RectF bounds, int op, int ctl, PointF adjust) {
        switch (op) {
            case 0:
                bounds.offset(adjust.x, adjust.y);
                return;
            case 1:
                adjustControl(bounds, ctl, adjust);
                return;
            default:
                return;
        }
    }

    public static void adjustControl(RectF bounds, int ctl, PointF adjust) {
        switch (ctl) {
            case 0:
                bounds.left += adjust.x;
                bounds.top += adjust.y;
                return;
            case 1:
                bounds.top += adjust.y;
                return;
            case 2:
                bounds.right += adjust.x;
                bounds.top += adjust.y;
                return;
            case 3:
                bounds.right += adjust.x;
                return;
            case 4:
                bounds.right += adjust.x;
                bounds.bottom += adjust.y;
                return;
            case 5:
                bounds.bottom += adjust.y;
                return;
            case 6:
                bounds.left += adjust.x;
                bounds.bottom += adjust.y;
                return;
            case 7:
                bounds.left += adjust.x;
                return;
            default:
                return;
        }
    }

    public void drawFrame(Canvas canvas, RectF bounds, int color, int opacity) {
        this.mFrmPaint.setColor(color);
        this.mFrmPaint.setAlpha(opacity);
        canvas.drawRect(bounds, this.mFrmPaint);
    }

    public void drawControls(Canvas canvas, RectF bounds, int color, int opacity) {
        float[] ctlPts = calculateControls(bounds);
        for (int i = 0; i < ctlPts.length; i += 2) {
            this.mCtlPaint.setColor(-1);
            this.mCtlPaint.setAlpha(255);
            this.mCtlPaint.setStyle(Style.FILL);
            canvas.drawCircle(ctlPts[i], ctlPts[i + 1], this.mCtlRadius, this.mCtlPaint);
            this.mCtlPaint.setColor(color);
            this.mCtlPaint.setAlpha(opacity);
            this.mCtlPaint.setStyle(Style.STROKE);
            canvas.drawCircle(ctlPts[i], ctlPts[i + 1], this.mCtlRadius, this.mCtlPaint);
        }
    }

    public void draw(Canvas canvas, RectF bounds, int color, int opacity) {
        drawFrame(canvas, bounds, color, opacity);
        drawControls(canvas, bounds, color, opacity);
    }
}
