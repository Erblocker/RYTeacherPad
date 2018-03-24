package com.foxit.uiextensions.annots.ink;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Ink;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AbstractToolHandler;
import com.foxit.uiextensions.annots.common.IAnnotTaskResult;
import com.foxit.uiextensions.annots.common.UIAnnotFrame;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.AppDmUtil;
import java.util.ArrayList;

class InkToolHandler extends AbstractToolHandler {
    public static int IA_MIN_DIST = 2;
    protected static final String PROPERTY_KEY = "INK";
    protected InkAnnotHandler mAnnotHandler;
    private int mCapturedPage;
    private boolean mConfigChanged;
    private PointF mLastPt;
    private ArrayList<PointF> mLine;
    private ArrayList<ArrayList<PointF>> mLineList;
    private Paint mPaint;
    private Path mPath;
    private ArrayList<Path> mPathList;
    private boolean mTouchCaptured;
    protected InkAnnotUtil mUtil;
    float mbx;
    float mby;
    float mcx;
    float mcy;
    float mex;
    float mey;
    RectF tv_invalid;
    PointF tv_pt;
    Rect tv_rect;

    public InkToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, InkAnnotUtil util) {
        super(context, parent, pdfViewCtrl, Module.MODULE_NAME_INK, PROPERTY_KEY);
        this.mTouchCaptured = false;
        this.mCapturedPage = -1;
        this.mLastPt = new PointF(0.0f, 0.0f);
        this.mConfigChanged = false;
        this.tv_pt = new PointF();
        this.tv_rect = new Rect();
        this.tv_invalid = new RectF();
        this.mColor = PropertyBar.PB_COLORS_PENCIL[0];
        this.mUtil = util;
        this.mLineList = new ArrayList();
        this.mPathList = new ArrayList();
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
    }

    public String getType() {
        return ToolHandler.TH_TYPE_INK;
    }

    protected void initUiElements() {
    }

    protected void uninitUiElements() {
        removeToolButton();
    }

    public void updateToolButtonStatus() {
    }

    public void setColor(int color) {
        if (this.mColor != color) {
            addAnnot(null);
            this.mColor = color;
        }
    }

    public void setOpacity(int opacity) {
        if (this.mOpacity != opacity) {
            addAnnot(null);
            this.mOpacity = opacity;
        }
    }

    public void setThickness(float thickness) {
        if (this.mThickness != thickness) {
            addAnnot(null);
            this.mThickness = thickness;
        }
    }

    public void onActivate() {
        this.mCapturedPage = -1;
        this.mLineList.clear();
        this.mPathList.clear();
    }

    public void onDeactivate() {
        if (this.mTouchCaptured) {
            this.mTouchCaptured = false;
            if (this.mLine != null) {
                this.mLineList.add(this.mLine);
                this.mLine = null;
            }
            this.mLastPt.set(0.0f, 0.0f);
        }
        addAnnot(null);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e) {
        PointF point = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        float thickness = UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, this.mThickness);
        int action = e.getAction();
        switch (action) {
            case 0:
                if (!this.mTouchCaptured) {
                    if (this.mCapturedPage == -1) {
                        this.mTouchCaptured = true;
                        this.mCapturedPage = pageIndex;
                    } else if (pageIndex == this.mCapturedPage) {
                        this.mTouchCaptured = true;
                    }
                    if (this.mTouchCaptured) {
                        this.mPath = new Path();
                        this.mPath.moveTo(point.x, point.y);
                        this.mbx = point.x;
                        this.mby = point.y;
                        this.mcx = point.x;
                        this.mcy = point.y;
                        this.mLine = new ArrayList();
                        this.mLine.add(new PointF(point.x, point.y));
                        this.mLastPt.set(point.x, point.y);
                    }
                }
                return true;
            case 1:
            case 2:
            case 3:
                if (this.mTouchCaptured) {
                    this.tv_pt.set(point);
                    InkAnnotUtil.correctPvPoint(this.mPdfViewCtrl, pageIndex, this.tv_pt);
                    float dx = Math.abs(this.tv_pt.x - this.mcx);
                    float dy = Math.abs(this.tv_pt.y - this.mcy);
                    if (this.mCapturedPage == pageIndex && (dx >= ((float) IA_MIN_DIST) || dy >= ((float) IA_MIN_DIST))) {
                        this.tv_invalid.set(this.tv_pt.x, this.tv_pt.y, this.tv_pt.x, this.tv_pt.y);
                        for (int i = 0; i < e.getHistorySize(); i++) {
                            this.tv_pt.set(e.getHistoricalX(i), e.getHistoricalY(i));
                            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(this.tv_pt, this.tv_pt, pageIndex);
                            InkAnnotUtil.correctPvPoint(this.mPdfViewCtrl, pageIndex, this.tv_pt);
                            if (this.tv_pt.x - this.mLastPt.x >= ((float) IA_MIN_DIST) || this.tv_pt.y - this.mLastPt.y >= ((float) IA_MIN_DIST)) {
                                this.mex = (this.mcx + this.tv_pt.x) / 2.0f;
                                this.mey = (this.mcy + this.tv_pt.y) / 2.0f;
                                this.mLine.add(new PointF(this.tv_pt.x, this.tv_pt.y));
                                this.mPath.quadTo(this.mcx, this.mcy, this.mex, this.mey);
                                this.mLastPt.set(this.tv_pt);
                                this.tv_invalid.union(this.mbx, this.mby);
                                this.tv_invalid.union(this.mcx, this.mcy);
                                this.tv_invalid.union(this.mex, this.mey);
                                this.mbx = this.mex;
                                this.mby = this.mey;
                                this.mcx = this.tv_pt.x;
                                this.mcy = this.tv_pt.y;
                            }
                        }
                        this.tv_pt.set(point);
                        InkAnnotUtil.correctPvPoint(this.mPdfViewCtrl, pageIndex, this.tv_pt);
                        this.mex = (this.mcx + this.tv_pt.x) / 2.0f;
                        this.mey = (this.mcy + this.tv_pt.y) / 2.0f;
                        this.mLine.add(new PointF(this.tv_pt.x, this.tv_pt.y));
                        this.mPath.quadTo(this.mcx, this.mcy, this.mex, this.mey);
                        this.mLastPt.set(this.tv_pt.x, this.tv_pt.y);
                        this.tv_invalid.union(this.mbx, this.mby);
                        this.tv_invalid.union(this.mcx, this.mcy);
                        this.tv_invalid.union(this.mex, this.mey);
                        this.mbx = this.mex;
                        this.mby = this.mey;
                        this.mcx = this.tv_pt.x;
                        this.mcy = this.tv_pt.y;
                        this.tv_invalid.inset(-thickness, -thickness);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.tv_invalid, this.tv_invalid, pageIndex);
                        this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.tv_invalid));
                    }
                    if (action == 1 || action == 3) {
                        this.tv_pt.set(point);
                        InkAnnotUtil.correctPvPoint(this.mPdfViewCtrl, pageIndex, this.tv_pt);
                        if (this.mLine.size() == 1) {
                            if (this.tv_pt.equals(this.mLine.get(0))) {
                                PointF pointF = this.tv_pt;
                                pointF.x = (float) (((double) pointF.x) + 0.1d);
                                pointF = this.tv_pt;
                                pointF.y = (float) (((double) pointF.y) + 0.1d);
                            }
                            this.mex = (this.mcx + this.tv_pt.x) / 2.0f;
                            this.mey = (this.mcy + this.tv_pt.y) / 2.0f;
                            this.mLine.add(new PointF(this.tv_pt.x, this.tv_pt.y));
                            this.mPath.quadTo(this.mcx, this.mcy, this.mex, this.mey);
                            this.mLastPt.set(this.tv_pt.x, this.tv_pt.y);
                        }
                        this.mPath.lineTo(this.mLastPt.x, this.mLastPt.y);
                        this.mPathList.add(this.mPath);
                        this.mPath = null;
                        this.tv_invalid.set(this.mbx, this.mby, this.mbx, this.mby);
                        this.tv_invalid.union(this.mcx, this.mcy);
                        this.tv_invalid.union(this.mex, this.mey);
                        this.tv_invalid.union(this.mLastPt.x, this.mLastPt.y);
                        this.tv_invalid.inset(-thickness, -thickness);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.tv_invalid, this.tv_invalid, pageIndex);
                        this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.tv_invalid));
                        this.mLineList.add(this.mLine);
                        this.mLine = null;
                        this.mTouchCaptured = false;
                        this.mLastPt.set(0.0f, 0.0f);
                    }
                    return true;
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
        if (this.mPathList != null && this.mCapturedPage == pageIndex) {
            setPaintProperty(this.mPdfViewCtrl, pageIndex, this.mPaint);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
            int count = this.mPathList.size();
            for (int i = 0; i < count; i++) {
                canvas.drawPath((Path) this.mPathList.get(i), this.mPaint);
            }
            if (this.mPath != null) {
                canvas.drawPath(this.mPath, this.mPaint);
            }
        }
    }

    protected void setPaintProperty(PDFViewCtrl pdfViewCtrl, int pageIndex, Paint paint) {
        paint.setColor(this.mColor);
        paint.setAlpha(AppDmUtil.opacity100To255(this.mOpacity));
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeWidth(UIAnnotFrame.getPageViewThickness(pdfViewCtrl, pageIndex, this.mThickness));
    }

    public long getSupportedProperties() {
        return this.mUtil.getSupportedProperties();
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        int[] colors = new int[PropertyBar.PB_COLORS_PENCIL.length];
        System.arraycopy(PropertyBar.PB_COLORS_PENCIL, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_PENCIL[0];
        propertyBar.setColors(colors);
        propertyBar.setProperty(2, this.mOpacity);
        super.setPropertyBarProperties(propertyBar);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        Object curToolHandler = ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler();
        if (curToolHandler == null || curToolHandler != this || this.mLineList.size() <= 0) {
            this.mConfigChanged = false;
        } else {
            this.mConfigChanged = true;
        }
        addAnnot(null);
    }

    protected void addAnnot(final IAnnotTaskResult<PDFPage, Annot, Void> result) {
        if (this.mCapturedPage != -1 && this.mLineList.size() != 0) {
            RectF bbox = new RectF();
            ArrayList<ArrayList<PointF>> docLines = this.mUtil.docLinesFromPageView(this.mPdfViewCtrl, this.mCapturedPage, this.mLineList, bbox);
            bbox.inset(-this.mThickness, -this.mThickness);
            Ink annot = (Ink) this.mAnnotHandler.addAnnot(this.mCapturedPage, new RectF(bbox), this.mColor, AppDmUtil.opacity100To255(this.mOpacity), this.mThickness, docLines, new IAnnotTaskResult<PDFPage, Annot, Void>() {
                public void onResult(boolean success, PDFPage p1, Annot p2, Void p3) {
                    if (result != null) {
                        result.onResult(success, p1, p2, p3);
                    }
                }
            });
            this.mCapturedPage = -1;
            this.mLineList.clear();
            this.mPathList.clear();
        }
    }
}
