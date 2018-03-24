package com.foxit.uiextensions.annots.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.AbstractAnnotHandler;
import com.foxit.uiextensions.annots.AbstractToolHandler;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.common.IAnnotTaskResult;
import com.foxit.uiextensions.annots.common.UIAnnotFrame;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;

/* compiled from: LineAnnotHandler */
class LineRealAnnotHandler extends AbstractAnnotHandler {
    protected int mBackColor;
    protected PointF mBackEndPt = new PointF();
    protected String mBackEndingStyle;
    protected float mBackOpacity;
    protected PointF mBackStartPt = new PointF();
    protected String mBackStartingStyle;
    protected ArrayList<Integer> mMenuText;
    protected LineUtil mUtil;

    public LineRealAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, LineUtil util) {
        super(context, parent, pdfViewCtrl, 4);
        this.mUtil = util;
        this.mColor = getToolHandler().getColor();
        this.mOpacity = getToolHandler().getOpacity();
        this.mThickness = getToolHandler().getThickness();
        this.mMenuText = new ArrayList();
    }

    protected AbstractToolHandler getToolHandler() {
        if (this.mPdfViewCtrl != null) {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
            if (annot != null) {
                try {
                    if (annot.getType() == this.mType) {
                        return this.mUtil.getToolHandler(((Line) annot).getIntent());
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.mUtil.getToolHandler("");
    }

    public void setThickness(float thickness) {
        super.setThickness(thickness);
    }

    public boolean annotCanAnswer(Annot annot) {
        return true;
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        try {
            PointF startPt = ((Line) annot).getStartPoint();
            PointF stopPt = ((Line) annot).getEndPoint();
            float distance = AppDmUtil.distanceFromPointToLine(point, startPt, stopPt);
            boolean isOnLine = AppDmUtil.isPointVerticalIntersectOnLine(point, startPt, stopPt);
            if (distance >= (annot.getBorderInfo().getWidth() * LineUtil.ARROW_WIDTH_SCALE) / 2.0f) {
                return false;
            }
            if (isOnLine) {
                return true;
            }
            if (AppDmUtil.distanceOfTwoPoints(startPt, stopPt) < (annot.getBorderInfo().getWidth() * LineUtil.ARROW_WIDTH_SCALE) / 2.0f) {
                return true;
            }
            return false;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onAnnotSelected(Annot annot, boolean reRender) {
        try {
            this.mColor = (int) annot.getBorderColor();
            this.mOpacity = AppDmUtil.opacity255To100((int) ((((Line) annot).getOpacity() * 255.0f) + 0.5f));
            this.mThickness = annot.getBorderInfo().getWidth();
            this.mBackColor = this.mColor;
            this.mBackOpacity = ((Line) annot).getOpacity();
            this.mBackStartPt.set(((Line) annot).getStartPoint());
            this.mBackEndPt.set(((Line) annot).getEndPoint());
            this.mBackStartingStyle = ((Line) annot).getLineStartingStyle();
            this.mBackEndingStyle = ((Line) annot).getLineEndingStyle();
            super.onAnnotSelected(annot, reRender);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        if (this.mIsModified) {
            LineModifyUndoItem undoItem = new LineModifyUndoItem(this, this.mPdfViewCtrl);
            undoItem.setCurrentValue(this.mSelectedAnnot);
            try {
                undoItem.mStartPt = ((Line) this.mSelectedAnnot).getStartPoint();
                undoItem.mEndPt = ((Line) this.mSelectedAnnot).getEndPoint();
                undoItem.mStartingStyle = ((Line) this.mSelectedAnnot).getLineStartingStyle();
                undoItem.mEndingStyle = ((Line) this.mSelectedAnnot).getLineEndingStyle();
                undoItem.mOldColor = (long) this.mBackColor;
                undoItem.mOldOpacity = this.mBackOpacity;
                undoItem.mOldBBox = new RectF(this.mBackRect);
                undoItem.mOldLineWidth = this.mBackThickness;
                undoItem.mOldStartPt.set(this.mBackStartPt);
                undoItem.mOldEndPt.set(this.mBackEndPt);
                undoItem.mOldStartingStyle = this.mBackStartingStyle;
                undoItem.mOldEndingStyle = this.mBackEndingStyle;
            } catch (PDFException e) {
                e.printStackTrace();
            }
            modifyAnnot(this.mSelectedAnnot, undoItem, false, true, reRender, new Callback() {
                public void result(Event event, boolean success) {
                    if (LineRealAnnotHandler.this.mSelectedAnnot != DocumentManager.getInstance(LineRealAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                        LineRealAnnotHandler.this.resetStatus();
                    }
                }
            });
            dismissPopupMenu();
            hidePropertyBar();
            return;
        }
        super.onAnnotDeselected(annot, reRender);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e, Annot annot) {
        PointF point = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        try {
            Annot lAnnot = (Line) annot;
            int action = e.getAction();
            PointF pointF;
            switch (action) {
                case 0:
                    if (pageIndex == lAnnot.getPage().getIndex() && lAnnot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        pointF = new PointF(lAnnot.getStartPoint().x, lAnnot.getStartPoint().y);
                        pointF = new PointF(lAnnot.getEndPoint().x, lAnnot.getEndPoint().y);
                        this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                        this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                        this.mCtl = this.mUtil.hitControlTest(pointF, pointF, point);
                        if (this.mCtl != -1) {
                            this.mTouchCaptured = true;
                            this.mOp = 1;
                            this.mDownPt.set(point);
                            this.mLastPt.set(point);
                            return true;
                        }
                        PointF docPt = new PointF(point.x, point.y);
                        this.mPdfViewCtrl.convertPageViewPtToPdfPt(docPt, docPt, pageIndex);
                        if (isHitAnnot(lAnnot, docPt)) {
                            this.mTouchCaptured = true;
                            this.mOp = 0;
                            this.mDownPt.set(point);
                            this.mLastPt.set(point);
                            return true;
                        }
                    }
                    break;
                case 1:
                case 2:
                case 3:
                    if (this.mTouchCaptured && pageIndex == lAnnot.getPage().getIndex() && lAnnot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        RectF bbox;
                        if (!DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                            if (action == 1 || action == 3) {
                                this.mTouchCaptured = false;
                                this.mDownPt.set(0.0f, 0.0f);
                                this.mLastPt.set(0.0f, 0.0f);
                                this.mOp = -1;
                                this.mCtl = -1;
                                if (this.mSelectedAnnot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                                    bbox = UIAnnotFrame.mapBounds(this.mPdfViewCtrl, pageIndex, lAnnot, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                                    this.mAnnotMenu.show(bbox);
                                }
                            }
                            return true;
                        } else if (this.mOp == 0) {
                            return super.onTouchEvent(pageIndex, e, annot);
                        } else {
                            if (this.mOp == 1) {
                                float thickness = UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, lAnnot.getBorderInfo().getWidth());
                                PointF pointBak = new PointF(point.x, point.y);
                                this.mUtil.correctPvPoint(this.mPdfViewCtrl, pageIndex, pointBak, thickness);
                                if (!(pointBak.x == this.mLastPt.x && pointBak.y == this.mLastPt.y)) {
                                    RectF rect0;
                                    RectF rect1;
                                    if (this.mAnnotMenu.isShowing()) {
                                        this.mAnnotMenu.dismiss();
                                    }
                                    pointF = new PointF(lAnnot.getStartPoint().x, lAnnot.getStartPoint().y);
                                    pointF = new PointF(lAnnot.getEndPoint().x, lAnnot.getEndPoint().y);
                                    this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                                    this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                                    if (this.mCtl == 0) {
                                        rect0 = this.mUtil.getArrowBBox(this.mLastPt, pointF, thickness);
                                        rect1 = this.mUtil.getArrowBBox(pointBak, pointF, thickness);
                                    } else {
                                        rect0 = this.mUtil.getArrowBBox(pointF, this.mLastPt, thickness);
                                        rect1 = this.mUtil.getArrowBBox(pointF, pointBak, thickness);
                                    }
                                    rect1.union(rect0);
                                    this.mUtil.extentBoundsToContainControl(rect1);
                                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rect1, rect1, pageIndex);
                                    this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rect1));
                                    this.mLastPt.set(pointBak);
                                }
                                if (action == 1 || action == 3) {
                                    if (!this.mLastPt.equals(this.mDownPt)) {
                                        pointF = new PointF(lAnnot.getStartPoint().x, lAnnot.getStartPoint().y);
                                        pointF = new PointF(lAnnot.getEndPoint().x, lAnnot.getEndPoint().y);
                                        this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                                        this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                                        if (this.mCtl == 0) {
                                            pointF.set(this.mUtil.calculateEndingPoint(pointF, this.mLastPt));
                                            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pointF, pointF, pageIndex);
                                            lAnnot.setStartPoint(pointF);
                                        } else {
                                            pointF.set(this.mUtil.calculateEndingPoint(pointF, this.mLastPt));
                                            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pointF, pointF, pageIndex);
                                            lAnnot.setEndPoint(pointF);
                                        }
                                        lAnnot.resetAppearanceStream();
                                        this.mIsModified = true;
                                    }
                                    this.mTouchCaptured = false;
                                    this.mDownPt.set(0.0f, 0.0f);
                                    this.mLastPt.set(0.0f, 0.0f);
                                    this.mOp = -1;
                                    this.mCtl = -1;
                                    if (this.mSelectedAnnot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                                        bbox = UIAnnotFrame.mapBounds(this.mPdfViewCtrl, pageIndex, lAnnot, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                                        this.mAnnotMenu.show(bbox);
                                    }
                                }
                            }
                            return true;
                        }
                    }
            }
        } catch (PDFException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return super.onLongPress(pageIndex, motionEvent, annot);
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return super.onSingleTapConfirmed(pageIndex, motionEvent, annot);
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (annot.getType() == this.mType && this.mSelectedAnnot == annot && annot.getPage().getIndex() == pageIndex) {
                    Line lAnnot = (Line) annot;
                    PointF startPt = new PointF(lAnnot.getStartPoint().x, lAnnot.getStartPoint().y);
                    PointF stopPt = new PointF(lAnnot.getEndPoint().x, lAnnot.getEndPoint().y);
                    this.mPdfViewCtrl.convertPdfPtToPageViewPt(startPt, startPt, pageIndex);
                    this.mPdfViewCtrl.convertPdfPtToPageViewPt(stopPt, stopPt, pageIndex);
                    if (this.mOp == 0) {
                        float dx = this.mLastPt.x - this.mDownPt.x;
                        float dy = this.mLastPt.y - this.mDownPt.y;
                        startPt.offset(dx, dy);
                        stopPt.offset(dx, dy);
                    } else if (this.mOp == 1) {
                        if (this.mCtl == 0) {
                            startPt.set(this.mUtil.calculateEndingPoint(stopPt, this.mLastPt));
                        } else {
                            stopPt.set(this.mUtil.calculateEndingPoint(startPt, this.mLastPt));
                        }
                    }
                    float thickness = lAnnot.getBorderInfo().getWidth();
                    if (thickness < 1.0f) {
                        thickness = 1.0f;
                    }
                    Path path = this.mUtil.getLinePath(lAnnot.getIntent(), startPt, stopPt, UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, ((3.0f + thickness) * 15.0f) / 8.0f));
                    setPaintProperty(this.mPdfViewCtrl, pageIndex, this.mPaint, this.mSelectedAnnot);
                    canvas.drawPath(path, this.mPaint);
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        this.mUtil.drawControls(canvas, startPt, stopPt, (int) (annot.getBorderColor() | -16777216), (int) (lAnnot.getOpacity() * 255.0f));
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (annot.getType() == this.mType) {
                    int pageIndex = annot.getPage().getIndex();
                    if (!this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        return;
                    }
                    if (this.mOp != 1 || this.mCtl != -1) {
                        RectF bbox = annot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                        this.mAnnotMenu.update(bbox);
                        if (this.mPropertyBar.isShowing()) {
                            this.mPropertyBar.update(bbox);
                        }
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        try {
            Line annot = (Line) this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(4, content.getBBox());
            LineAddUndoItem undoItem = new LineAddUndoItem(this, this.mPdfViewCtrl);
            undoItem.mPageIndex = pageIndex;
            undoItem.mNM = content.getNM();
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mFlags = 4;
            undoItem.mColor = (long) content.getColor();
            undoItem.mOpacity = ((float) content.getOpacity()) / 255.0f;
            undoItem.mBBox = new RectF(content.getBBox());
            undoItem.mIntent = content.getIntent();
            undoItem.mLineWidth = content.getLineWidth();
            undoItem.mSubject = this.mUtil.getSubject(content.getIntent());
            if (content instanceof LineAnnotContent) {
                if (((LineAnnotContent) content).getEndingPoints().size() == 2) {
                    undoItem.mStartPt.set((PointF) ((LineAnnotContent) content).getEndingPoints().get(0));
                    undoItem.mEndPt.set((PointF) ((LineAnnotContent) content).getEndingPoints().get(1));
                }
                if (((LineAnnotContent) content).getEndingStyles().size() == 2) {
                    undoItem.mStartingStyle = (String) ((LineAnnotContent) content).getEndingStyles().get(0);
                    undoItem.mEndingStyle = (String) ((LineAnnotContent) content).getEndingStyles().get(1);
                }
            }
            if (!(undoItem.mStartPt == null || undoItem.mEndPt == null)) {
                RectF bbox = this.mUtil.getArrowBBox(undoItem.mStartPt, undoItem.mEndPt, undoItem.mLineWidth);
                undoItem.mBBox.set(new RectF(bbox.left, bbox.bottom, bbox.right, bbox.top));
            }
            final Callback callback = result;
            addAnnot(pageIndex, annot, undoItem, addUndo, new IAnnotTaskResult<PDFPage, Annot, Void>() {
                public void onResult(boolean success, PDFPage p1, Annot p2, Void p3) {
                    if (callback != null) {
                        callback.result(null, true);
                    }
                }
            });
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected Line addAnnot(int pageIndex, RectF bbox, int color, int opacity, float thickness, PointF startPt, PointF stopPt, String intent, IAnnotTaskResult<PDFPage, Annot, Void> result) {
        try {
            Line annot = (Line) this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(4, bbox);
            LineAddUndoItem undoItem = new LineAddUndoItem(this, this.mPdfViewCtrl);
            undoItem.mPageIndex = pageIndex;
            undoItem.mNM = AppDmUtil.randomUUID(null);
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mFlags = 4;
            undoItem.mColor = (long) color;
            undoItem.mOpacity = ((float) opacity) / 255.0f;
            undoItem.mBBox = new RectF(bbox);
            undoItem.mIntent = intent;
            undoItem.mLineWidth = thickness;
            undoItem.mSubject = this.mUtil.getSubject(intent);
            undoItem.mStartPt.set(startPt);
            undoItem.mEndPt.set(stopPt);
            ArrayList<String> endingStyles = this.mUtil.getEndingStyles(intent);
            if (endingStyles != null) {
                undoItem.mStartingStyle = (String) endingStyles.get(0);
                undoItem.mEndingStyle = (String) endingStyles.get(1);
            }
            addAnnot(pageIndex, annot, undoItem, true, result);
            return annot;
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void addAnnot(int pageIndex, Line annot, LineUndoItem undoItem, boolean addUndo, IAnnotTaskResult<PDFPage, Annot, Void> result) {
        handleAddAnnot(pageIndex, annot, new LineEvent(1, undoItem, annot, this.mPdfViewCtrl), addUndo, result);
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        LineModifyUndoItem undoItem = new LineModifyUndoItem(this, this.mPdfViewCtrl);
        undoItem.setCurrentValue(content);
        if (content instanceof LineAnnotContent) {
            undoItem.mStartPt.set((PointF) ((LineAnnotContent) content).getEndingPoints().get(0));
            undoItem.mEndPt.set((PointF) ((LineAnnotContent) content).getEndingPoints().get(1));
            undoItem.mStartingStyle = (String) ((LineAnnotContent) content).getEndingStyles().get(0);
            undoItem.mEndingStyle = (String) ((LineAnnotContent) content).getEndingStyles().get(1);
        }
        try {
            Line line = (Line) annot;
            undoItem.mOldContents = annot.getContent();
            undoItem.mOldColor = (long) ((int) line.getBorderColor());
            undoItem.mOldOpacity = line.getOpacity();
            undoItem.mOldBBox = new RectF(line.getRect());
            undoItem.mOldLineWidth = line.getBorderInfo().getWidth();
            undoItem.mOldStartPt.set(line.getStartPoint());
            undoItem.mOldEndPt.set(line.getEndPoint());
            undoItem.mOldStartingStyle = line.getLineStartingStyle();
            undoItem.mOldEndingStyle = line.getLineEndingStyle();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        modifyAnnot(annot, undoItem, false, addUndo, true, result);
    }

    protected void modifyAnnot(Annot annot, LineUndoItem undoItem, boolean useOldValue, boolean addUndo, boolean reRender, final Callback result) {
        LineEvent event = new LineEvent(2, undoItem, (Line) annot, this.mPdfViewCtrl);
        event.useOldValue = useOldValue;
        handleModifyAnnot(annot, event, addUndo, reRender, new IAnnotTaskResult<PDFPage, Annot, Void>() {
            public void onResult(boolean success, PDFPage p1, Annot p2, Void p3) {
                if (result != null) {
                    result.result(null, success);
                }
            }
        });
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        LineDeleteUndoItem undoItem = new LineDeleteUndoItem(this, this.mPdfViewCtrl);
        undoItem.setCurrentValue(annot);
        try {
            undoItem.mStartPt = ((Line) annot).getStartPoint();
            undoItem.mEndPt = ((Line) annot).getEndPoint();
            undoItem.mStartingStyle = ((Line) annot).getLineStartingStyle();
            undoItem.mEndingStyle = ((Line) annot).getLineEndingStyle();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        removeAnnot(annot, undoItem, addUndo, result);
    }

    protected void removeAnnot(Annot annot, LineDeleteUndoItem undoItem, boolean addUndo, final Callback result) {
        handleRemoveAnnot(annot, new LineEvent(3, undoItem, (Line) annot, this.mPdfViewCtrl), addUndo, new IAnnotTaskResult<PDFPage, Void, Void>() {
            public void onResult(boolean success, PDFPage p1, Void p2, Void p3) {
                if (result != null) {
                    result.result(null, success);
                }
            }
        });
    }

    protected ArrayList<Path> generatePathData(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot) {
        Line lAnnot = (Line) annot;
        try {
            float thickness = lAnnot.getBorderInfo().getWidth();
            if (thickness < 1.0f) {
                thickness = 1.0f;
            }
            thickness = UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, ((3.0f + thickness) * 15.0f) / 8.0f);
            PointF startPt = new PointF();
            PointF stopPt = new PointF();
            startPt.set(lAnnot.getStartPoint());
            stopPt.set(lAnnot.getEndPoint());
            pdfViewCtrl.convertPdfPtToPageViewPt(startPt, startPt, pageIndex);
            pdfViewCtrl.convertPdfPtToPageViewPt(stopPt, stopPt, pageIndex);
            Path path = this.mUtil.getLinePath(lAnnot.getIntent(), startPt, stopPt, thickness);
            ArrayList<Path> paths = new ArrayList();
            paths.add(path);
            return paths;
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void transformAnnot(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot, Matrix matrix) {
        try {
            float[] pts = new float[]{0.0f, 0.0f};
            Line lAnnot = (Line) annot;
            PointF startPt = lAnnot.getStartPoint();
            PointF stopPt = lAnnot.getEndPoint();
            pdfViewCtrl.convertPdfPtToPageViewPt(startPt, startPt, pageIndex);
            pdfViewCtrl.convertPdfPtToPageViewPt(stopPt, stopPt, pageIndex);
            pts[0] = startPt.x;
            pts[1] = startPt.y;
            matrix.mapPoints(pts);
            startPt.set(pts[0], pts[1]);
            pdfViewCtrl.convertPageViewPtToPdfPt(startPt, startPt, pageIndex);
            pts[0] = stopPt.x;
            pts[1] = stopPt.y;
            matrix.mapPoints(pts);
            stopPt.set(pts[0], pts[1]);
            pdfViewCtrl.convertPageViewPtToPdfPt(stopPt, stopPt, pageIndex);
            ((Line) annot).setStartPoint(startPt);
            ((Line) annot).setEndPoint(stopPt);
            annot.resetAppearanceStream();
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected void resetStatus() {
        this.mBackRect = null;
        this.mSelectedAnnot = null;
        this.mIsModified = false;
    }

    protected void showPopupMenu() {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null) {
            try {
                if (curAnnot.getType() == 4) {
                    reloadPopupMenuString();
                    this.mAnnotMenu.setMenuItems(this.mMenuText);
                    RectF bbox = curAnnot.getRect();
                    int pageIndex = curAnnot.getPage().getIndex();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                    this.mAnnotMenu.show(bbox);
                    this.mAnnotMenu.setListener(new ClickListener() {
                        public void onAMClick(int flag) {
                            if (LineRealAnnotHandler.this.mSelectedAnnot != null) {
                                if (flag == 3) {
                                    DocumentManager.getInstance(LineRealAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                    UIAnnotReply.showComments(LineRealAnnotHandler.this.mContext, LineRealAnnotHandler.this.mPdfViewCtrl, LineRealAnnotHandler.this.mParent, LineRealAnnotHandler.this.mSelectedAnnot);
                                } else if (flag == 4) {
                                    DocumentManager.getInstance(LineRealAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                    UIAnnotReply.replyToAnnot(LineRealAnnotHandler.this.mContext, LineRealAnnotHandler.this.mPdfViewCtrl, LineRealAnnotHandler.this.mParent, LineRealAnnotHandler.this.mSelectedAnnot);
                                } else if (flag == 2) {
                                    if (LineRealAnnotHandler.this.mSelectedAnnot == DocumentManager.getInstance(LineRealAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                                        LineRealAnnotHandler.this.removeAnnot(LineRealAnnotHandler.this.mSelectedAnnot, true, null);
                                    }
                                } else if (flag == 6) {
                                    LineRealAnnotHandler.this.dismissPopupMenu();
                                    LineRealAnnotHandler.this.showPropertyBar(1);
                                }
                            }
                        }
                    });
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    protected void dismissPopupMenu() {
        this.mAnnotMenu.setListener(null);
        this.mAnnotMenu.dismiss();
    }

    protected long getSupportedProperties() {
        return this.mUtil.getSupportedProperties();
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        try {
            if (!(this.mPdfViewCtrl == null || DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() == null)) {
                String intent = ((Line) DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()).getIntent();
                int[] colors;
                if (intent == null || !intent.equals(LineConstants.INTENT_LINE_ARROW)) {
                    colors = new int[PropertyBar.PB_COLORS_LINE.length];
                    System.arraycopy(PropertyBar.PB_COLORS_LINE, 0, colors, 0, colors.length);
                    colors[0] = getToolHandler().getCustomColor();
                    propertyBar.setColors(colors);
                } else {
                    colors = new int[PropertyBar.PB_COLORS_ARROW.length];
                    System.arraycopy(PropertyBar.PB_COLORS_ARROW, 0, colors, 0, colors.length);
                    colors[0] = getToolHandler().getCustomColor();
                    propertyBar.setColors(colors);
                }
            }
            super.setPropertyBarProperties(propertyBar);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected void reloadPopupMenuString() {
        if (DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() != null) {
            this.mMenuText.clear();
            if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                this.mMenuText.add(Integer.valueOf(6));
                this.mMenuText.add(Integer.valueOf(3));
                this.mMenuText.add(Integer.valueOf(4));
                this.mMenuText.add(Integer.valueOf(2));
                return;
            }
            this.mMenuText.add(Integer.valueOf(3));
        }
    }

    public void onLanguageChanged() {
        this.mMenuText.clear();
    }

    public void setAnnotMenu(AnnotMenu annotMenu) {
        this.mAnnotMenu = annotMenu;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotMenu;
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mPropertyBar = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }
}
