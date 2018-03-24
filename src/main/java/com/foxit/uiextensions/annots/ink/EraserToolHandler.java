package com.foxit.uiextensions.annots.ink;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.internal.view.SupportMenu;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Ink;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AbstractToolHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.achartengine.renderer.DefaultRenderer;

class EraserToolHandler extends AbstractToolHandler {
    private int mCapturedPage = -1;
    private int mCtlPtRadius = 5;
    private Paint mEraserPaint;
    private PointF mLastPt = new PointF();
    private Paint mPaint;
    private ArrayList<Path> mPaths;
    protected float mRadius = 15.0f;
    private ArrayList<AnnotInfo> mRootList;
    private boolean mTouchCaptured = false;
    Ink tempAnnot;

    class AnnotInfo implements Comparable<AnnotInfo> {
        Ink mAnnot;
        boolean mDrawAtJava;
        boolean mIsPSIMode;
        boolean mModifyFlag;
        ArrayList<LineInfo> mNewLines;

        public AnnotInfo() {
            this.mIsPSIMode = false;
            this.mModifyFlag = false;
            this.mDrawAtJava = false;
            this.mNewLines = new ArrayList();
            this.mIsPSIMode = false;
        }

        public int compareTo(AnnotInfo another) {
            try {
                if (another.mNewLines.isEmpty()) {
                    if (!this.mNewLines.isEmpty()) {
                        return 1;
                    }
                    if (another.mAnnot.getIndex() > this.mAnnot.getIndex()) {
                        return -1;
                    }
                    if (another.mAnnot.getIndex() == this.mAnnot.getIndex()) {
                        return 0;
                    }
                    return 1;
                } else if (this.mNewLines.isEmpty() || another.mAnnot.getIndex() > this.mAnnot.getIndex()) {
                    return -1;
                } else {
                    if (another.mAnnot.getIndex() == this.mAnnot.getIndex()) {
                        return 0;
                    }
                    return 1;
                }
            } catch (PDFException e) {
                return 1;
            }
        }
    }

    class LineInfo {
        ArrayList<PointF> mLine = new ArrayList();
        RectF mLineBBox = new RectF();
        ArrayList<Float> mPresses = new ArrayList();
    }

    public EraserToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        super(context, parent, pdfViewCtrl, ToolHandler.TH_TYPE_ERASER, "ERASER");
        this.mCtlPtRadius = AppDisplay.getInstance(context).dp2px((float) this.mCtlPtRadius);
        this.mRootList = new ArrayList();
        this.mPaths = new ArrayList();
        this.mEraserPaint = new Paint();
        this.mEraserPaint.setStyle(Style.STROKE);
        this.mEraserPaint.setAntiAlias(true);
        this.mEraserPaint.setDither(true);
        this.mEraserPaint.setColor(SupportMenu.CATEGORY_MASK);
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mPaint.setStrokeCap(Cap.ROUND);
        if (0.0f > 0.0f) {
            setRadius(0.0f);
        }
        this.mColor = DefaultRenderer.TEXT_COLOR;
    }

    public void initUiElements() {
    }

    public void onActivate() {
        this.mCapturedPage = -1;
        this.mRootList.clear();
        this.mPaths.clear();
    }

    public void onDeactivate() {
        for (int k = 0; k < this.mRootList.size(); k++) {
            AnnotInfo annotInfo = (AnnotInfo) this.mRootList.get(k);
            if (!annotInfo.mModifyFlag) {
                this.mRootList.remove(k);
                invalidateJniAnnots(annotInfo, 1, null);
            }
        }
        if (!this.mRootList.isEmpty()) {
            Collections.sort(this.mRootList);
            try {
                int pageIndex = ((AnnotInfo) this.mRootList.get(0)).mAnnot.getPage().getIndex();
                RectF rmRect = new RectF(((AnnotInfo) this.mRootList.get(0)).mAnnot.getRect());
                EraserUndoItem undoItems = new EraserUndoItem();
                boolean isLast = false;
                for (int i = this.mRootList.size() - 1; i >= 0; i--) {
                    if (i == 0) {
                        isLast = true;
                    }
                    annotInfo = (AnnotInfo) this.mRootList.get(i);
                    this.tempAnnot = annotInfo.mAnnot;
                    if (annotInfo.mModifyFlag) {
                        if (annotInfo.mNewLines.isEmpty()) {
                            deleteAnnot(this.tempAnnot, null, undoItems, isLast);
                        } else {
                            modifyAnnot(pageIndex, annotInfo, undoItems, isLast);
                        }
                    }
                    invalidateJniAnnots(annotInfo, 1, null);
                    rmRect.union(annotInfo.mAnnot.getRect());
                }
                RectF rectF = rmRect;
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
                    this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(rectF));
                    this.mCapturedPage = -1;
                    this.mRootList.clear();
                    this.mPaths.clear();
                    return;
                }
                this.mCapturedPage = -1;
                this.mRootList.clear();
                this.mPaths.clear();
            } catch (PDFException e) {
            }
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e) {
        if (!DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            return false;
        }
        PointF point = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        float x = point.x;
        float y = point.y;
        switch (e.getAction()) {
            case 0:
                if (!this.mTouchCaptured) {
                    if (this.mCapturedPage == -1) {
                        this.mTouchCaptured = true;
                        this.mCapturedPage = pageIndex;
                    } else if (pageIndex == this.mCapturedPage) {
                        this.mTouchCaptured = true;
                    }
                    if (this.mTouchCaptured) {
                        this.mLastPt.set(x, y);
                    }
                }
                return true;
            case 1:
            case 3:
                if (this.mTouchCaptured) {
                    this.mTouchCaptured = false;
                    RectF invaRect2 = getEraserBBox(this.mLastPt, point);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(invaRect2, invaRect2, pageIndex);
                    this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(invaRect2));
                    this.mLastPt.set(-this.mRadius, -this.mRadius);
                }
                return true;
            case 2:
                if (this.mTouchCaptured && this.mCapturedPage == pageIndex && !this.mLastPt.equals(x, y)) {
                    calculateNewLines(this.mPdfViewCtrl, pageIndex, point);
                    RectF invaRect = getEraserBBox(this.mLastPt, point);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(invaRect, invaRect, pageIndex);
                    this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(invaRect));
                    this.mLastPt.set(x, y);
                }
                return true;
            default:
                return true;
        }
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mCapturedPage == pageIndex) {
            canvas.drawCircle(this.mLastPt.x, this.mLastPt.y, this.mRadius, this.mEraserPaint);
            if (this.mRootList.size() != 0) {
                for (int i = 0; i < this.mRootList.size(); i++) {
                    AnnotInfo annotInfo = (AnnotInfo) this.mRootList.get(i);
                    if (annotInfo.mDrawAtJava && !annotInfo.mIsPSIMode) {
                        setPaint(annotInfo.mAnnot);
                        this.mPaths = getNewPaths(this.mPdfViewCtrl, pageIndex, annotInfo);
                        if (this.mPaths != null) {
                            int count = this.mPaths.size();
                            for (int j = 0; j < count; j++) {
                                canvas.drawPath((Path) this.mPaths.get(j), this.mPaint);
                            }
                        }
                    }
                }
            }
        }
    }

    private void calculateNewLines(PDFViewCtrl pdfViewCtrl, int pageIndex, PointF point) {
        RectF rectF = new RectF(point.x, point.y, point.x, point.y);
        rectF.union(this.mLastPt.x, this.mLastPt.y);
        rectF.inset(-this.mRadius, -this.mRadius);
        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, rectF, pageIndex);
        try {
            int i;
            int j;
            LineInfo lineInfo;
            PDFPage page = pdfViewCtrl.getDoc().getPage(pageIndex);
            ArrayList<Annot> annotList = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnotsInteractRect(page, new RectF(rectF), 15);
            PointF tv_pt = new PointF();
            RectF tv_rectF = new RectF();
            RectF eraseBBox = getEraserBBox(this.mLastPt, point);
            this.mPdfViewCtrl.convertPageViewRectToPdfRect(eraseBBox, eraseBBox, pageIndex);
            Iterator it = annotList.iterator();
            while (it.hasNext()) {
                Annot annot = (Annot) it.next();
                if (DocumentManager.intersects(annot.getRect(), eraseBBox)) {
                    boolean isExist = false;
                    for (i = 0; i < this.mRootList.size(); i++) {
                        if (((AnnotInfo) this.mRootList.get(i)).mAnnot == annot) {
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        AnnotInfo annotInfo = new AnnotInfo();
                        annotInfo.mAnnot = (Ink) annot;
                        PDFPath path = ((Ink) annot).getInkList();
                        LineInfo lineInfo2 = null;
                        int ptCount = path.getPointCount();
                        for (j = 0; j < ptCount; j++) {
                            if (path.getPointType(j) == 1) {
                                lineInfo = new LineInfo();
                            }
                            lineInfo2.mLine.add(path.getPoint(j));
                            if (j != ptCount - 1) {
                                if (j + 1 < ptCount) {
                                    if (path.getPointType(j + 1) != 1) {
                                    }
                                }
                            }
                            lineInfo2.mLineBBox = getLineBBox(lineInfo2.mLine, annot.getBorderInfo().getWidth());
                            annotInfo.mNewLines.add(lineInfo2);
                        }
                        this.mRootList.add(annotInfo);
                        final Annot annot2 = annot;
                        final int i2 = pageIndex;
                        final AnnotInfo annotInfo2 = annotInfo;
                        invalidateJniAnnots(annotInfo, 0, new Callback() {
                            public void result(Event event, boolean success) {
                                try {
                                    RectF viewRect = annot2.getRect();
                                    EraserToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, i2);
                                    EraserToolHandler.this.mPdfViewCtrl.refresh(i2, AppDmUtil.rectFToRect(viewRect));
                                } catch (PDFException e) {
                                    e.printStackTrace();
                                }
                                annotInfo2.mDrawAtJava = true;
                            }
                        });
                    }
                }
            }
            PointF pdfDP = new PointF(this.mLastPt.x, this.mLastPt.y);
            PointF pdfCP = new PointF(point.x, point.y);
            RectF eBBox = getEraserBBox(this.mLastPt, point);
            rectF = new RectF(0.0f, 0.0f, this.mRadius, this.mRadius);
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pdfDP, pdfDP, pageIndex);
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pdfCP, pdfCP, pageIndex);
            this.mPdfViewCtrl.convertPageViewRectToPdfRect(eBBox, eBBox, pageIndex);
            this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, rectF, pageIndex);
            float pdfR = rectF.width();
            PointF intersectPoint = new PointF();
            PointF pdfPoint1 = new PointF();
            PointF pdfPoint2 = new PointF();
            for (i = 0; i < this.mRootList.size(); i++) {
                AnnotInfo annotNode = (AnnotInfo) this.mRootList.get(i);
                if (DocumentManager.intersects(annotNode.mAnnot.getRect(), eBBox)) {
                    int lineIndex = 0;
                    while (lineIndex < annotNode.mNewLines.size()) {
                        LineInfo lineNode = (LineInfo) annotNode.mNewLines.get(lineIndex);
                        ArrayList<PointF> pdfLine = lineNode.mLine;
                        ArrayList<Float> presses = lineNode.mPresses;
                        int end1_PointIndex = -1;
                        int begin2_PointIndex = -1;
                        if (DocumentManager.intersects(lineNode.mLineBBox, eBBox)) {
                            j = 0;
                            while (j < pdfLine.size()) {
                                pdfPoint1.set(((PointF) pdfLine.get(j)).x, ((PointF) pdfLine.get(j)).y);
                                boolean createNewLine = false;
                                boolean reachEnd = false;
                                if (j == pdfLine.size() - 1) {
                                    reachEnd = true;
                                } else {
                                    pdfPoint2.set(((PointF) pdfLine.get(j + 1)).x, ((PointF) pdfLine.get(j + 1)).y);
                                }
                                int type = getIntersection(pdfPoint1, pdfPoint2, pdfDP, pdfCP, intersectPoint);
                                int p;
                                int q;
                                if (!reachEnd && type == 1) {
                                    createNewLine = true;
                                    tv_rectF.set(intersectPoint.x, intersectPoint.y, intersectPoint.x, intersectPoint.y);
                                    p = j;
                                    while (p >= 0) {
                                        tv_pt.set(((PointF) pdfLine.get(p)).x, ((PointF) pdfLine.get(p)).y);
                                        tv_rectF.union(tv_pt.x, tv_pt.y);
                                        if (getDistanceOfTwoPoints(tv_pt.x, tv_pt.y, intersectPoint.x, intersectPoint.y) > pdfR) {
                                            end1_PointIndex = p;
                                            if (p > 0) {
                                                rectF = tv_rectF;
                                                rectF.union(((PointF) pdfLine.get(p - 1)).x, ((PointF) pdfLine.get(p - 1)).y);
                                            }
                                            q = j + 1;
                                            while (q < pdfLine.size()) {
                                                tv_pt.set(((PointF) pdfLine.get(q)).x, ((PointF) pdfLine.get(q)).y);
                                                tv_rectF.union(tv_pt.x, tv_pt.y);
                                                if (getDistanceOfTwoPoints(tv_pt.x, tv_pt.y, intersectPoint.x, intersectPoint.y) <= pdfR) {
                                                    begin2_PointIndex = q;
                                                    if (q < pdfLine.size() - 1) {
                                                        rectF = tv_rectF;
                                                        rectF.union(((PointF) pdfLine.get(q + 1)).x, ((PointF) pdfLine.get(q + 1)).y);
                                                    }
                                                } else {
                                                    q++;
                                                }
                                            }
                                        } else {
                                            p--;
                                        }
                                    }
                                    q = j + 1;
                                    while (q < pdfLine.size()) {
                                        tv_pt.set(((PointF) pdfLine.get(q)).x, ((PointF) pdfLine.get(q)).y);
                                        tv_rectF.union(tv_pt.x, tv_pt.y);
                                        if (getDistanceOfTwoPoints(tv_pt.x, tv_pt.y, intersectPoint.x, intersectPoint.y) <= pdfR) {
                                            q++;
                                        } else {
                                            begin2_PointIndex = q;
                                            if (q < pdfLine.size() - 1) {
                                                rectF = tv_rectF;
                                                rectF.union(((PointF) pdfLine.get(q + 1)).x, ((PointF) pdfLine.get(q + 1)).y);
                                            }
                                        }
                                    }
                                } else if (getDistanceOfPointToLine(pdfPoint1.x, pdfPoint1.y, pdfDP.x, pdfDP.y, pdfCP.x, pdfCP.y) < ((double) pdfR) && (isIntersectPointInLine(pdfPoint1.x, pdfPoint1.y, pdfDP.x, pdfDP.y, pdfCP.x, pdfCP.y) || getDistanceOfTwoPoints(pdfPoint1.x, pdfPoint1.y, pdfDP.x, pdfDP.y) < pdfR || getDistanceOfTwoPoints(pdfPoint1.x, pdfPoint1.y, pdfCP.x, pdfCP.y) < pdfR)) {
                                    createNewLine = true;
                                    p = j;
                                    while (p >= 0) {
                                        tv_pt.set(((PointF) pdfLine.get(p)).x, ((PointF) pdfLine.get(p)).y);
                                        tv_rectF.union(tv_pt.x, tv_pt.y);
                                        if (getDistanceOfPointToLine(tv_pt.x, tv_pt.y, pdfDP.x, pdfDP.y, pdfCP.x, pdfCP.y) >= ((double) pdfR) || (!isIntersectPointInLine(tv_pt.x, tv_pt.y, pdfDP.x, pdfDP.y, pdfCP.x, pdfCP.y) && getDistanceOfTwoPoints(tv_pt.x, tv_pt.y, pdfDP.x, pdfDP.y) >= pdfR && getDistanceOfTwoPoints(tv_pt.x, tv_pt.y, pdfCP.x, pdfCP.y) >= pdfR)) {
                                            end1_PointIndex = p;
                                            if (p > 0) {
                                                rectF = tv_rectF;
                                                rectF.union(((PointF) pdfLine.get(p - 1)).x, ((PointF) pdfLine.get(p - 1)).y);
                                            }
                                            q = j + 1;
                                            while (q < pdfLine.size()) {
                                                tv_pt.set(((PointF) pdfLine.get(q)).x, ((PointF) pdfLine.get(q)).y);
                                                tv_rectF.union(tv_pt.x, tv_pt.y);
                                                if (getDistanceOfPointToLine(tv_pt.x, tv_pt.y, pdfDP.x, pdfDP.y, pdfCP.x, pdfCP.y) < ((double) pdfR) || (!isIntersectPointInLine(tv_pt.x, tv_pt.y, pdfDP.x, pdfDP.y, pdfCP.x, pdfCP.y) && getDistanceOfTwoPoints(tv_pt.x, tv_pt.y, pdfDP.x, pdfDP.y) >= pdfR && getDistanceOfTwoPoints(tv_pt.x, tv_pt.y, pdfCP.x, pdfCP.y) >= pdfR)) {
                                                    begin2_PointIndex = q;
                                                    if (q < pdfLine.size() - 1) {
                                                        rectF = tv_rectF;
                                                        rectF.union(((PointF) pdfLine.get(q + 1)).x, ((PointF) pdfLine.get(q + 1)).y);
                                                    }
                                                } else {
                                                    q++;
                                                }
                                            }
                                        } else {
                                            p--;
                                        }
                                    }
                                    q = j + 1;
                                    while (q < pdfLine.size()) {
                                        tv_pt.set(((PointF) pdfLine.get(q)).x, ((PointF) pdfLine.get(q)).y);
                                        tv_rectF.union(tv_pt.x, tv_pt.y);
                                        if (getDistanceOfPointToLine(tv_pt.x, tv_pt.y, pdfDP.x, pdfDP.y, pdfCP.x, pdfCP.y) < ((double) pdfR)) {
                                        }
                                        begin2_PointIndex = q;
                                        if (q < pdfLine.size() - 1) {
                                            rectF = tv_rectF;
                                            rectF.union(((PointF) pdfLine.get(q + 1)).x, ((PointF) pdfLine.get(q + 1)).y);
                                        }
                                    }
                                }
                                if (createNewLine) {
                                    int k;
                                    ArrayList<PointF> newLine1 = new ArrayList();
                                    ArrayList<Float> newPresses1 = new ArrayList();
                                    if (end1_PointIndex >= 0 && end1_PointIndex < pdfLine.size()) {
                                        for (k = 0; k <= end1_PointIndex; k++) {
                                            newLine1.add((PointF) pdfLine.get(k));
                                        }
                                    }
                                    ArrayList<PointF> newLine2 = new ArrayList();
                                    ArrayList<Float> newPresses2 = new ArrayList();
                                    if (begin2_PointIndex >= 0 && begin2_PointIndex < pdfLine.size()) {
                                        for (k = pdfLine.size() - 1; k >= begin2_PointIndex; k--) {
                                            newLine2.add((PointF) pdfLine.get(k));
                                        }
                                    }
                                    annotNode.mNewLines.remove(lineIndex);
                                    if (newLine1.size() == 0 && newLine2.size() == 0) {
                                        lineIndex--;
                                    } else {
                                        if (newLine2.size() != 0) {
                                            lineInfo = new LineInfo();
                                            lineInfo.mLine = newLine2;
                                            lineInfo.mPresses = newPresses2;
                                            lineInfo.mLineBBox = getLineBBox(newLine2, annotNode.mAnnot.getBorderInfo().getWidth());
                                            annotNode.mNewLines.add(lineIndex, lineInfo);
                                        }
                                        if (newLine1.size() != 0) {
                                            lineInfo = new LineInfo();
                                            lineInfo.mLine = newLine1;
                                            lineInfo.mPresses = newPresses1;
                                            lineInfo.mLineBBox = getLineBBox(newLine1, annotNode.mAnnot.getBorderInfo().getWidth());
                                            annotNode.mNewLines.add(lineIndex, lineInfo);
                                        } else {
                                            lineIndex--;
                                        }
                                    }
                                    annotNode.mModifyFlag = true;
                                    invalidateNewLine(pdfViewCtrl, pageIndex, annotNode.mAnnot, tv_rectF);
                                } else {
                                    j++;
                                }
                            }
                        }
                        lineIndex++;
                    }
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void invalidateNewLine(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot, RectF rect) {
        try {
            rect.inset(annot.getBorderInfo().getWidth(), annot.getBorderInfo().getWidth());
            float tmp = rect.top;
            rect.top = rect.bottom;
            rect.bottom = tmp;
            pdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, pageIndex);
            pdfViewCtrl.convertPageViewRectToDisplayViewRect(rect, rect, pageIndex);
            pdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rect));
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private double getDistanceOfPointToLine(float x, float y, float x1, float y1, float x2, float y2) {
        if (x1 == x2) {
            return (double) Math.abs(x - x1);
        }
        if (y1 == y2) {
            return (double) Math.abs(y - y1);
        }
        float k = (y2 - y1) / (x2 - x1);
        return ((double) Math.abs(((k * x) - y) + (y2 - (k * x2)))) / Math.sqrt((double) ((k * k) + 1.0f));
    }

    private boolean isIntersectPointInLine(float x, float y, float x1, float y1, float x2, float y2) {
        double r = ((double) (((x2 - x1) * (x - x1)) + ((y2 - y1) * (y - y1)))) / ((double) (((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1))));
        if (r <= 0.0d || r >= 1.0d) {
            return false;
        }
        return true;
    }

    private int getIntersection(PointF a, PointF b, PointF c, PointF d, PointF intersection) {
        float f;
        if (((Math.abs(b.y - a.y) + Math.abs(b.x - a.x)) + Math.abs(d.y - c.y)) + Math.abs(d.x - c.x) == 0.0f) {
            f = c.x;
            f = a.x;
            f = c.y;
            f = a.y;
            return 0;
        } else if (Math.abs(b.y - a.y) + Math.abs(b.x - a.x) == 0.0f) {
            f = a.x;
            f = d.x;
            f = c.y;
            f = d.y;
            f = a.y;
            f = d.y;
            f = c.x;
            f = d.x;
            return 0;
        } else if (Math.abs(d.y - c.y) + Math.abs(d.x - c.x) == 0.0f) {
            f = d.x;
            f = b.x;
            f = a.y;
            f = b.y;
            f = d.y;
            f = b.y;
            f = a.x;
            f = b.x;
            return 0;
        } else if (((b.y - a.y) * (c.x - d.x)) - ((b.x - a.x) * (c.y - d.y)) == 0.0f) {
            return 0;
        } else {
            intersection.x = (((((b.x - a.x) * (c.x - d.x)) * (c.y - a.y)) - ((c.x * (b.x - a.x)) * (c.y - d.y))) + ((a.x * (b.y - a.y)) * (c.x - d.x))) / (((b.y - a.y) * (c.x - d.x)) - ((b.x - a.x) * (c.y - d.y)));
            intersection.y = (((((b.y - a.y) * (c.y - d.y)) * (c.x - a.x)) - ((c.y * (b.y - a.y)) * (c.x - d.x))) + ((a.y * (b.x - a.x)) * (c.y - d.y))) / (((b.x - a.x) * (c.y - d.y)) - ((b.y - a.y) * (c.x - d.x)));
            if ((intersection.x - a.x) * (intersection.x - b.x) > 0.0f || (intersection.x - c.x) * (intersection.x - d.x) > 0.0f || (intersection.y - a.y) * (intersection.y - b.y) > 0.0f || (intersection.y - c.y) * (intersection.y - d.y) > 0.0f) {
                return -1;
            }
            return 1;
        }
    }

    private RectF getEraserBBox(PointF downPoint, PointF point) {
        RectF eraserBBox = new RectF();
        eraserBBox.left = Math.min(downPoint.x, point.x);
        eraserBBox.top = Math.min(downPoint.y, point.y);
        eraserBBox.right = Math.max(downPoint.x, point.x);
        eraserBBox.bottom = Math.max(downPoint.y, point.y);
        eraserBBox.inset((-this.mRadius) - 2.0f, (-this.mRadius) - 2.0f);
        return eraserBBox;
    }

    private RectF getLineBBox(ArrayList<PointF> line, float thickness) {
        if (line.size() == 0) {
            return new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        }
        RectF lineBBox = new RectF(((PointF) line.get(0)).x, ((PointF) line.get(0)).y, ((PointF) line.get(0)).x, ((PointF) line.get(0)).y);
        for (int i = 0; i < line.size(); i++) {
            lineBBox.left = Math.min(lineBBox.left, ((PointF) line.get(i)).x);
            lineBBox.top = Math.max(lineBBox.top, ((PointF) line.get(i)).y);
            lineBBox.right = Math.max(lineBBox.right, ((PointF) line.get(i)).x);
            lineBBox.bottom = Math.min(lineBBox.bottom, ((PointF) line.get(i)).y);
        }
        lineBBox.inset((-thickness) / 2.0f, (-thickness) / 2.0f);
        return lineBBox;
    }

    private float getDistanceOfTwoPoints(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((double) (((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2))));
    }

    private ArrayList<Path> getNewPaths(PDFViewCtrl pdfViewCtrl, int pageIndex, AnnotInfo info) {
        ArrayList<LineInfo> pdfLines = info.mNewLines;
        ArrayList<Path> paths = new ArrayList();
        PointF pointF = new PointF();
        float cx = 0.0f;
        float cy = 0.0f;
        for (int i = 0; i < pdfLines.size(); i++) {
            ArrayList<PointF> pdfLine = ((LineInfo) pdfLines.get(i)).mLine;
            int ptCount = pdfLine.size();
            if (ptCount != 0) {
                Path path;
                if (ptCount == 1) {
                    path = new Path();
                    pointF.set(((PointF) pdfLine.get(0)).x, ((PointF) pdfLine.get(0)).y);
                    pdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                    path.moveTo(pointF.x, pointF.y);
                    path.lineTo(pointF.x + 0.1f, pointF.y + 0.1f);
                    paths.add(path);
                } else {
                    path = new Path();
                    for (int j = 0; j < ptCount; j++) {
                        pointF.set(((PointF) pdfLine.get(j)).x, ((PointF) pdfLine.get(j)).y);
                        pdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                        if (j == 0) {
                            path.moveTo(pointF.x, pointF.y);
                            cx = pointF.x;
                            cy = pointF.y;
                        } else {
                            path.quadTo(cx, cy, (pointF.x + cx) / 2.0f, (pointF.y + cy) / 2.0f);
                            cx = pointF.x;
                            cy = pointF.y;
                            if (j == pdfLine.size() - 1) {
                                path.lineTo(pointF.x, pointF.y);
                            }
                        }
                    }
                    paths.add(path);
                }
            }
        }
        return paths;
    }

    private void invalidateJniAnnots(AnnotInfo annotInfo, int flag, Callback result) {
        if (flag == 0) {
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotStartEraser(annotInfo.mAnnot);
        } else if (flag == 1) {
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotEndEraser();
        }
        if (result != null) {
            result.result(null, true);
        }
    }

    private RectF getNewBBox(AnnotInfo annotInfo) {
        Ink annot = annotInfo.mAnnot;
        ArrayList<ArrayList<PointF>> pdfLines = getNewPdfLines(annotInfo);
        RectF newBBox = null;
        for (int i = 0; i < pdfLines.size(); i++) {
            for (int j = 0; j < ((ArrayList) pdfLines.get(i)).size(); j++) {
                PointF pdfPt = (PointF) ((ArrayList) pdfLines.get(i)).get(j);
                if (newBBox == null) {
                    newBBox = new RectF(pdfPt.x, pdfPt.y, pdfPt.x, pdfPt.y);
                } else {
                    newBBox.left = Math.min(newBBox.left, pdfPt.x);
                    newBBox.bottom = Math.min(newBBox.bottom, pdfPt.y);
                    newBBox.right = Math.max(newBBox.right, pdfPt.x);
                    newBBox.top = Math.max(newBBox.top, pdfPt.y);
                }
            }
        }
        try {
            newBBox.inset(((-annot.getBorderInfo().getWidth()) * 0.5f) - ((float) this.mCtlPtRadius), ((-annot.getBorderInfo().getWidth()) * 0.5f) - ((float) this.mCtlPtRadius));
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return newBBox;
    }

    private ArrayList<ArrayList<PointF>> getNewPdfLines(AnnotInfo annotInfo) {
        ArrayList<ArrayList<PointF>> pdfLines = new ArrayList();
        for (int i = 0; i < annotInfo.mNewLines.size(); i++) {
            ArrayList<PointF> oldLine = ((LineInfo) annotInfo.mNewLines.get(i)).mLine;
            ArrayList<PointF> newLine = new ArrayList();
            for (int j = 0; j < oldLine.size(); j++) {
                newLine.add((PointF) oldLine.get(j));
            }
            pdfLines.add(newLine);
        }
        return pdfLines;
    }

    private void modifyAnnot(int pageIndex, AnnotInfo annotInfo, EraserUndoItem undoItems, boolean isLast) {
        try {
            final Annot annot = annotInfo.mAnnot;
            final InkModifyUndoItem undoItem = new InkModifyUndoItem((InkAnnotHandler) ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), annot.getType()), this.mPdfViewCtrl);
            undoItem.setCurrentValue(annot);
            undoItem.setOldValue(annot);
            undoItem.mBBox = new RectF(getNewBBox(annotInfo));
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mInkLists = getNewPdfLines(annotInfo);
            if (undoItem.mInkLists != null) {
                undoItem.mPath = PDFPath.create();
                for (int i = 0; i < undoItem.mInkLists.size(); i++) {
                    ArrayList<PointF> line = (ArrayList) undoItem.mInkLists.get(i);
                    for (int j = 0; j < line.size(); j++) {
                        if (j == 0) {
                            undoItem.mPath.moveTo((PointF) line.get(j));
                        } else {
                            undoItem.mPath.lineTo((PointF) line.get(j));
                        }
                    }
                }
            }
            undoItem.setOldValue(annot);
            undoItem.mOldInkLists = InkAnnotUtil.generateInkList(((Ink) annot).getInkList());
            InkEvent event = new InkEvent(2, undoItem, (Ink) annot, this.mPdfViewCtrl);
            event.useOldValue = false;
            DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(isLast);
            final EraserUndoItem eraserUndoItem = undoItems;
            final boolean z = isLast;
            final int i2 = pageIndex;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(event, new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        try {
                            eraserUndoItem.addUndoItem(undoItem);
                            if (z) {
                                DocumentManager.getInstance(EraserToolHandler.this.mPdfViewCtrl).addUndoItem(eraserUndoItem);
                                DocumentManager.getInstance(EraserToolHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                            }
                            DocumentManager.getInstance(EraserToolHandler.this.mPdfViewCtrl).onAnnotModified(annot.getPage(), annot);
                            if (EraserToolHandler.this.mPdfViewCtrl.isPageVisible(i2)) {
                                RectF annotRectF = annot.getRect();
                                EraserToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, i2);
                                EraserToolHandler.this.mPdfViewCtrl.refresh(i2, AppDmUtil.rectFToRect(annotRectF));
                            }
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }));
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void deleteAnnot(Annot annot, Callback result, EraserUndoItem undoItems, boolean isLast) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        try {
            final RectF viewRect = annot.getRect();
            PDFPage page = annot.getPage();
            final int pageIndex = page.getIndex();
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final InkDeleteUndoItem undoItem = new InkDeleteUndoItem((InkAnnotHandler) ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), annot.getType()), this.mPdfViewCtrl);
            undoItem.setCurrentValue(annot);
            try {
                undoItem.mPath = ((Ink) annot).getInkList();
                undoItem.mInkLists = InkAnnotUtil.generateInkList(undoItem.mPath);
            } catch (PDFException e) {
                e.printStackTrace();
            }
            InkEvent event = new InkEvent(3, undoItem, (Ink) annot, this.mPdfViewCtrl);
            DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(true);
            final EraserUndoItem eraserUndoItem = undoItems;
            final boolean z = isLast;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(event, new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        eraserUndoItem.addUndoItem(undoItem);
                        if (z) {
                            DocumentManager.getInstance(EraserToolHandler.this.mPdfViewCtrl).addUndoItem(eraserUndoItem);
                            DocumentManager.getInstance(EraserToolHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                        }
                        if (EraserToolHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            EraserToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                            EraserToolHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                        }
                    }
                    if (callback != null) {
                        callback.result(null, success);
                    }
                }
            }));
        } catch (PDFException e2) {
            e2.printStackTrace();
        }
    }

    public void setPaint(Annot annot) {
        try {
            float thickness = thicknessOnPageView(annot.getPage().getIndex(), annot.getBorderInfo().getWidth());
            this.mPaint.setColor((int) annot.getBorderColor());
            this.mPaint.setStrokeWidth(thickness);
            this.mPaint.setAlpha((int) ((((Ink) annot).getOpacity() * 255.0f) + 0.5f));
            this.mPaint.setStyle(Style.STROKE);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private float thicknessOnPageView(int pageIndex, float thickness) {
        RectF rectF = new RectF(0.0f, 0.0f, thickness, thickness);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
        return Math.abs(rectF.width());
    }

    public void setRadius(float radius) {
        this.mRadius = (float) AppDisplay.getInstance(this.mContext).dp2px(radius);
    }

    public void setThickness(float thickness) {
        super.setThickness(thickness);
        setRadius(thickness);
    }

    protected void setPaintProperty(PDFViewCtrl pdfViewCtrl, int pageIndex, Paint paint) {
    }

    public long getSupportedProperties() {
        return 4;
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        int[] colors = new int[PropertyBar.PB_COLORS_PENCIL.length];
        System.arraycopy(PropertyBar.PB_COLORS_PENCIL, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_PENCIL[0];
        propertyBar.setColors(colors);
        propertyBar.setProperty(1, this.mColor);
        propertyBar.setProperty(4, getThickness());
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            propertyBar.setArrowVisible(true);
        } else {
            propertyBar.setArrowVisible(false);
        }
    }

    public String getType() {
        return ToolHandler.TH_TYPE_ERASER;
    }
}
