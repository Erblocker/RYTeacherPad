package com.foxit.uiextensions.annots.line;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AbstractToolHandler;
import com.foxit.uiextensions.annots.common.IAnnotTaskResult;
import com.foxit.uiextensions.annots.common.UIAnnotFrame;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.AppDmUtil;

public class LineToolHandler extends AbstractToolHandler {
    protected LineRealAnnotHandler mAnnotHandler;
    protected int mCapturedPage = -1;
    protected String mIntent;
    protected Paint mPaint;
    protected PointF mStartPt = new PointF();
    protected PointF mStopPt = new PointF();
    protected boolean mTouchCaptured = false;
    protected LineUtil mUtil;

    public LineToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, LineUtil util, String intent) {
        super(context, parent, pdfViewCtrl, util.getToolName(intent), util.getToolPropertyKey(intent));
        if (intent.equals(LineConstants.INTENT_LINE_ARROW)) {
            this.mColor = PropertyBar.PB_COLORS_ARROW[0];
            this.mCustomColor = PropertyBar.PB_COLORS_ARROW[0];
        } else {
            this.mColor = PropertyBar.PB_COLORS_LINE[0];
            this.mCustomColor = PropertyBar.PB_COLORS_LINE[0];
        }
        this.mUtil = util;
        this.mIntent = intent;
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
    }

    protected void initUiElements() {
    }

    protected void uninitUiElements() {
        removeToolButton();
    }

    protected String getIntent() {
        return this.mIntent;
    }

    public void onActivate() {
        this.mCapturedPage = -1;
    }

    public void onDeactivate() {
        if (this.mTouchCaptured) {
            if (this.mPdfViewCtrl.isPageVisible(this.mCapturedPage)) {
                addAnnot(this.mCapturedPage);
            }
            this.mTouchCaptured = false;
            this.mCapturedPage = -1;
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e) {
        PointF pt = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pt, pt, pageIndex);
        int action = e.getAction();
        switch (action) {
            case 0:
                if (!this.mTouchCaptured || this.mCapturedPage == pageIndex) {
                    this.mTouchCaptured = true;
                    this.mStartPt.set(pt);
                    this.mStopPt.set(pt);
                    if (this.mCapturedPage == -1) {
                        this.mCapturedPage = pageIndex;
                        break;
                    }
                }
                break;
            case 1:
            case 2:
            case 3:
                if (this.mTouchCaptured) {
                    PointF point = new PointF(pt.x, pt.y);
                    this.mUtil.correctPvPoint(this.mPdfViewCtrl, pageIndex, point, this.mThickness);
                    if (this.mCapturedPage == pageIndex && !point.equals(this.mStopPt.x, this.mStopPt.y)) {
                        float thickness = UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, this.mThickness);
                        RectF rect1 = this.mUtil.getArrowBBox(this.mStartPt, this.mStopPt, thickness);
                        RectF rect2 = this.mUtil.getArrowBBox(this.mStartPt, point, thickness);
                        rect2.union(rect1);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rect2, rect2, pageIndex);
                        this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rect2));
                        this.mStopPt.set(point);
                    }
                    if (action == 1 || action == 3) {
                        addAnnot(pageIndex);
                        this.mTouchCaptured = false;
                        if (!this.mIsContinuousCreate) {
                            ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                            break;
                        }
                    }
                }
                break;
        }
        return true;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mCapturedPage == pageIndex) {
            float distance = AppDmUtil.distanceOfTwoPoints(this.mStartPt, this.mStopPt);
            float thickness = this.mThickness;
            if (thickness < 1.0f) {
                thickness = 1.0f;
            }
            thickness = UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, ((3.0f + thickness) * 15.0f) / 8.0f);
            if (distance > (LineUtil.ARROW_WIDTH_SCALE * thickness) / 2.0f) {
                setPaintProperty(this.mPdfViewCtrl, pageIndex, this.mPaint);
                canvas.drawPath(this.mUtil.getLinePath(getIntent(), this.mStartPt, this.mStopPt, thickness), this.mPaint);
            }
        }
    }

    protected void setPaintProperty(PDFViewCtrl pdfViewCtrl, int pageIndex, Paint paint) {
        paint.setColor(this.mColor);
        paint.setAlpha(AppDmUtil.opacity100To255(this.mOpacity));
        paint.setStrokeWidth(UIAnnotFrame.getPageViewThickness(pdfViewCtrl, pageIndex, this.mThickness));
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public long getSupportedProperties() {
        return this.mUtil.getSupportedProperties();
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        int[] colors;
        if (this.mIntent.equals(LineConstants.INTENT_LINE_ARROW)) {
            colors = new int[PropertyBar.PB_COLORS_ARROW.length];
            System.arraycopy(PropertyBar.PB_COLORS_ARROW, 0, colors, 0, colors.length);
            colors[0] = this.mCustomColor;
            propertyBar.setColors(colors);
        } else {
            colors = new int[PropertyBar.PB_COLORS_LINE.length];
            System.arraycopy(PropertyBar.PB_COLORS_LINE, 0, colors, 0, colors.length);
            colors[0] = this.mCustomColor;
            propertyBar.setColors(colors);
        }
        super.setPropertyBarProperties(propertyBar);
    }

    void addAnnot(int pageIndex) {
        if (this.mTouchCaptured && this.mCapturedPage >= 0) {
            float distance = AppDmUtil.distanceOfTwoPoints(this.mStartPt, this.mStopPt);
            float thickness = UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, this.mThickness);
            RectF bbox;
            if (distance > (LineUtil.ARROW_WIDTH_SCALE * thickness) / 2.0f) {
                bbox = this.mUtil.getArrowBBox(this.mStartPt, this.mStopPt, thickness);
                PointF startPt = new PointF(this.mStartPt.x, this.mStartPt.y);
                PointF stopPt = new PointF(this.mStopPt.x, this.mStopPt.y);
                this.mPdfViewCtrl.convertPageViewRectToPdfRect(bbox, bbox, pageIndex);
                this.mPdfViewCtrl.convertPageViewPtToPdfPt(startPt, startPt, pageIndex);
                this.mPdfViewCtrl.convertPageViewPtToPdfPt(stopPt, stopPt, pageIndex);
                Line addAnnot = this.mAnnotHandler.addAnnot(pageIndex, new RectF(bbox), this.mColor, AppDmUtil.opacity100To255(this.mOpacity), this.mThickness, startPt, stopPt, getIntent(), new IAnnotTaskResult<PDFPage, Annot, Void>() {
                    public void onResult(boolean success, PDFPage p1, Annot p2, Void p3) {
                        LineToolHandler.this.mCapturedPage = -1;
                    }
                });
                return;
            }
            bbox = this.mUtil.getArrowBBox(this.mStartPt, this.mStopPt, thickness);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
            this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(bbox));
        }
    }
}
