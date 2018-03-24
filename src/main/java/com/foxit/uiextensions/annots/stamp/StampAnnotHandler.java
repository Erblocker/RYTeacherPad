package com.foxit.uiextensions.annots.stamp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Stamp;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

public class StampAnnotHandler implements AnnotHandler {
    public static final int CTR_B = 6;
    public static final int CTR_L = 8;
    public static final int CTR_LB = 7;
    public static final int CTR_LT = 1;
    public static final int CTR_NONE = -1;
    public static final int CTR_R = 4;
    public static final int CTR_RB = 5;
    public static final int CTR_RT = 3;
    public static final int CTR_T = 2;
    public static final int OPER_DEFAULT = -1;
    public static final int OPER_SCALE_B = 6;
    public static final int OPER_SCALE_L = 8;
    public static final int OPER_SCALE_LB = 7;
    public static final int OPER_SCALE_LT = 1;
    public static final int OPER_SCALE_R = 4;
    public static final int OPER_SCALE_RB = 5;
    public static final int OPER_SCALE_RT = 3;
    public static final int OPER_SCALE_T = 2;
    public static final int OPER_TRANSLATE = 9;
    private static float mCtlPtDeltyXY = 20.0f;
    private static float mCtlPtLineWidth = 2.0f;
    private static float mCtlPtRadius = 5.0f;
    private static float mCtlPtTouchExt = 20.0f;
    private PointF mAdjustPointF = new PointF(0.0f, 0.0f);
    private AnnotMenu mAnnotMenu;
    private RectF mAnnotMenuRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private RectF mBBoxInOnDraw = new RectF();
    private int mBBoxSpace;
    private Annot mBitmapAnnot;
    private Context mContext;
    private Paint mCtlPtPaint;
    private int mCurrentCtr = -1;
    private RectF mDocViewerBBox = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private PointF mDocViewerPt = new PointF(0.0f, 0.0f);
    private PointF mDownPoint;
    private DrawFilter mDrawFilter = new PaintFlagsDrawFilter(0, 3);
    private Paint mFrmPaint;
    Path mImaginaryPath = new Path();
    private RectF mInvalidateRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private boolean mIsModify;
    private int mLastOper = -1;
    private PointF mLastPoint;
    RectF mMapBounds = new RectF();
    private ArrayList<Integer> mMenuItems;
    private RectF mPageDrawRect = new RectF();
    private RectF mPageViewRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private Paint mPaint;
    private Paint mPaintOut;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private float mThickness = 0.0f;
    private RectF mThicknessRectF = new RectF();
    private StampToolHandler mToolHandler;
    private boolean mTouchCaptured = false;
    private RectF mViewDrawRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private RectF mViewDrawRectInOnDraw = new RectF();
    private RectF tempUndoBBox;

    StampAnnotHandler(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mPaintOut = new Paint();
        this.mPaintOut.setAntiAlias(true);
        this.mPaintOut.setStyle(Style.STROKE);
        this.mPaintOut.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mPaintOut.setStrokeWidth(AppAnnotUtil.getInstance(context).getAnnotBBoxStrokeWidth());
        this.mDownPoint = new PointF();
        this.mLastPoint = new PointF();
        this.mMenuItems = new ArrayList();
        this.mCtlPtPaint = new Paint();
        PathEffect effect = AppAnnotUtil.getAnnotBBoxPathEffect();
        this.mFrmPaint = new Paint();
        this.mFrmPaint.setPathEffect(effect);
        this.mFrmPaint.setStyle(Style.STROKE);
        this.mFrmPaint.setAntiAlias(true);
        this.mBBoxSpace = AppAnnotUtil.getAnnotBBoxSpace();
        this.mBitmapAnnot = null;
    }

    public void setAnnotMenu(AnnotMenu annotMenu) {
        this.mAnnotMenu = annotMenu;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotMenu;
    }

    public int getType() {
        return 13;
    }

    public boolean annotCanAnswer(Annot annot) {
        return true;
    }

    public RectF getAnnotBBox(Annot annot) {
        try {
            return annot.getRect();
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        RectF bbox = getAnnotBBox(annot);
        if (bbox == null) {
            return false;
        }
        try {
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, annot.getPage().getIndex());
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return bbox.contains(point.x, point.y);
    }

    public void onAnnotSelected(final Annot annot, boolean reRender) {
        try {
            this.tempUndoBBox = annot.getRect();
            this.mBitmapAnnot = annot;
            RectF _rect = annot.getRect();
            this.mPageViewRect.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
            this.mAnnotMenu.dismiss();
            this.mMenuItems.clear();
            if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                this.mMenuItems.add(Integer.valueOf(3));
                this.mMenuItems.add(Integer.valueOf(4));
                this.mMenuItems.add(Integer.valueOf(2));
            } else {
                this.mMenuItems.add(Integer.valueOf(3));
            }
            this.mAnnotMenu.setMenuItems(this.mMenuItems);
            this.mAnnotMenu.setListener(new ClickListener() {
                public void onAMClick(int btType) {
                    StampAnnotHandler.this.mAnnotMenu.dismiss();
                    if (btType == 3) {
                        DocumentManager.getInstance(StampAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.showComments(StampAnnotHandler.this.mContext, StampAnnotHandler.this.mPdfViewCtrl, StampAnnotHandler.this.mParent, annot);
                    } else if (btType == 4) {
                        DocumentManager.getInstance(StampAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.replyToAnnot(StampAnnotHandler.this.mContext, StampAnnotHandler.this.mPdfViewCtrl, StampAnnotHandler.this.mParent, annot);
                    } else if (btType == 2) {
                        StampAnnotHandler.this.delAnnot(annot, true, null);
                    }
                }
            });
            RectF viewRect = new RectF(annot.getRect());
            RectF modifyRectF = new RectF(viewRect);
            int pageIndex = annot.getPage().getIndex();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(viewRect, viewRect, pageIndex);
            this.mAnnotMenu.show(viewRect);
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(modifyRectF, modifyRectF, pageIndex);
                this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(modifyRectF));
                if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    this.mBitmapAnnot = annot;
                }
            } else {
                this.mBitmapAnnot = annot;
            }
            this.mIsModify = false;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        this.mAnnotMenu.dismiss();
        try {
            PDFPage page = annot.getPage();
            int pageIndex = page.getIndex();
            if (page != null) {
                RectF pdfRect = annot.getRect();
                RectF viewRect = new RectF(pdfRect.left, pdfRect.top, pdfRect.right, pdfRect.bottom);
                if (this.mIsModify && reRender) {
                    if (this.tempUndoBBox.equals(annot.getRect())) {
                        modifyAnnot(pageIndex, annot, annot.getRect(), annot.getContent(), false, false, null);
                    } else {
                        modifyAnnot(pageIndex, annot, annot.getRect(), annot.getContent(), true, true, null);
                    }
                } else if (this.mIsModify) {
                    annot.move(this.tempUndoBBox);
                    annot.resetAppearanceStream();
                }
                this.mIsModify = false;
                if (this.mPdfViewCtrl.isPageVisible(pageIndex) && reRender) {
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                    this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                    this.mBitmapAnnot = null;
                    return;
                }
            }
            this.mBitmapAnnot = null;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        if (this.mToolHandler != null) {
            this.mToolHandler.addAnnot(pageIndex, content, addUndo, result);
        } else if (result != null) {
            result.result(null, false);
        }
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        try {
            int pageIndex = annot.getPage().getIndex();
            RectF bbox = annot.getRect();
            String contents = annot.getContent();
            this.tempUndoBBox = annot.getRect();
            if (content.getBBox() != null) {
                bbox = content.getBBox();
            }
            if (content.getContents() != null) {
                contents = content.getContents();
            }
            modifyAnnot(pageIndex, annot, bbox, contents, true, addUndo, result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void modifyAnnot(int pageIndex, Annot annot, RectF bbox, String content, boolean isModifyJni, boolean addUndo, Callback result) {
        final StampModifyUndoItem undoItem = new StampModifyUndoItem(this.mPdfViewCtrl);
        undoItem.setCurrentValue(annot);
        undoItem.mPageIndex = pageIndex;
        undoItem.mBBox = new RectF(bbox);
        undoItem.mContents = content;
        undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
        undoItem.mUndoBox = new RectF(this.tempUndoBBox);
        try {
            undoItem.mUndoContent = annot.getContent();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        undoItem.mRedoBox = new RectF(bbox);
        undoItem.mRedoContent = content;
        if (isModifyJni) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(true);
            final boolean z = addUndo;
            final int i = pageIndex;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(2, undoItem, (Stamp) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (z) {
                            DocumentManager.getInstance(StampAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        DocumentManager.getInstance(StampAnnotHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                        if (StampAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                            StampAnnotHandler.this.mPdfViewCtrl.refresh(i, AppDmUtil.rectFToRect(new RectF(0.0f, 0.0f, (float) StampAnnotHandler.this.mPdfViewCtrl.getPageViewWidth(i), (float) StampAnnotHandler.this.mPdfViewCtrl.getPageViewHeight(i))));
                        }
                    }
                    if (callback != null) {
                        callback.result(null, success);
                    }
                }
            }));
        }
        if (isModifyJni) {
            try {
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotModified(annot.getPage(), annot);
            } catch (PDFException e2) {
                e2.printStackTrace();
                return;
            }
        }
        this.mIsModify = true;
        if (!isModifyJni) {
            RectF annotRectF = annot.getRect();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                float thickness = thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth());
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
                annotRectF.inset(((-thickness) - mCtlPtRadius) - mCtlPtDeltyXY, ((-thickness) - mCtlPtRadius) - mCtlPtDeltyXY);
                this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(annotRectF));
            }
        }
    }

    private float thicknessOnPageView(int pageIndex, float thickness) {
        this.mThicknessRectF.set(0.0f, 0.0f, thickness, thickness);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mThicknessRectF, this.mThicknessRectF, pageIndex);
        return Math.abs(this.mThicknessRectF.width());
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        delAnnot(annot, addUndo, result);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e, Annot annot) {
        PointF pointF = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pointF, pointF, pageIndex);
        float envX = pointF.x;
        float envY = pointF.y;
        RectF pageViewBBox;
        switch (e.getAction()) {
            case 0:
                try {
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex()) {
                        pageViewBBox = annot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewBBox, pageViewBBox, pageIndex);
                        RectF pdfRect = annot.getRect();
                        this.mPageViewRect.set(pdfRect.left, pdfRect.top, pdfRect.right, pdfRect.bottom);
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mPageViewRect, this.mPageViewRect, pageIndex);
                        this.mPageViewRect.inset(this.mThickness / 2.0f, this.mThickness / 2.0f);
                        this.mCurrentCtr = isTouchControlPoint(pageViewBBox, envX, envY);
                        this.mDownPoint.set(envX, envY);
                        this.mLastPoint.set(envX, envY);
                        if (this.mCurrentCtr == 1) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 1;
                            return true;
                        } else if (this.mCurrentCtr == 2) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 2;
                            return true;
                        } else if (this.mCurrentCtr == 3) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 3;
                            return true;
                        } else if (this.mCurrentCtr == 4) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 4;
                            return true;
                        } else if (this.mCurrentCtr == 5) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 5;
                            return true;
                        } else if (this.mCurrentCtr == 6) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 6;
                            return true;
                        } else if (this.mCurrentCtr == 7) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 7;
                            return true;
                        } else if (this.mCurrentCtr == 8) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 8;
                            return true;
                        } else if (isHitAnnot(annot, pointF)) {
                            this.mTouchCaptured = true;
                            this.mLastOper = 9;
                            return true;
                        }
                    }
                    return false;
                } catch (PDFException e1) {
                    e1.printStackTrace();
                    break;
                }
            case 1:
            case 3:
                if (this.mTouchCaptured && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex()) {
                    RectF pageViewRect = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewRect, pageViewRect, pageIndex);
                    pageViewRect.inset(this.mThickness / 2.0f, this.mThickness / 2.0f);
                    switch (this.mLastOper) {
                        case 1:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(this.mLastPoint.x, this.mLastPoint.y, pageViewRect.right, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 2:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, this.mLastPoint.y, pageViewRect.right, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 3:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, this.mLastPoint.y, this.mLastPoint.x, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 4:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, pageViewRect.top, this.mLastPoint.x, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 5:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, pageViewRect.top, this.mLastPoint.x, this.mLastPoint.y);
                                break;
                            }
                            break;
                        case 6:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(pageViewRect.left, pageViewRect.top, pageViewRect.right, this.mLastPoint.y);
                                break;
                            }
                            break;
                        case 7:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(this.mLastPoint.x, pageViewRect.top, pageViewRect.right, this.mLastPoint.y);
                                break;
                            }
                            break;
                        case 8:
                            if (!this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                                this.mPageDrawRect.set(this.mLastPoint.x, pageViewRect.top, pageViewRect.right, pageViewRect.bottom);
                                break;
                            }
                            break;
                        case 9:
                            this.mPageDrawRect.set(pageViewRect);
                            this.mPageDrawRect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                            break;
                    }
                    RectF rectF;
                    if (this.mLastOper == -1 || this.mDownPoint.equals(this.mLastPoint.x, this.mLastPoint.y)) {
                        rectF = new RectF(this.mPageDrawRect.left, this.mPageDrawRect.top, this.mPageDrawRect.right, this.mPageDrawRect.bottom);
                        float _lineWidth = annot.getBorderInfo().getWidth();
                        rectF.inset((-thicknessOnPageView(pageIndex, _lineWidth)) / 2.0f, (-thicknessOnPageView(pageIndex, _lineWidth)) / 2.0f);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                        if (this.mAnnotMenu.isShowing()) {
                            this.mAnnotMenu.update(rectF);
                        } else {
                            this.mAnnotMenu.show(rectF);
                        }
                    } else {
                        rectF = new RectF(this.mPageDrawRect.left, this.mPageDrawRect.top, this.mPageDrawRect.right, this.mPageDrawRect.bottom);
                        RectF bboxRect = new RectF();
                        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, bboxRect, pageIndex);
                        modifyAnnot(pageIndex, annot, bboxRect, annot.getContent(), true, false, null);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
                        if (this.mAnnotMenu.isShowing()) {
                            this.mAnnotMenu.update(rectF);
                        } else {
                            this.mAnnotMenu.show(rectF);
                        }
                    }
                    this.mTouchCaptured = false;
                    this.mDownPoint.set(0.0f, 0.0f);
                    this.mLastPoint.set(0.0f, 0.0f);
                    this.mLastOper = -1;
                    this.mCurrentCtr = -1;
                    return true;
                }
                this.mTouchCaptured = false;
                this.mDownPoint.set(0.0f, 0.0f);
                this.mLastPoint.set(0.0f, 0.0f);
                this.mLastOper = -1;
                this.mCurrentCtr = -1;
                this.mTouchCaptured = false;
                return false;
            case 2:
                if (!this.mTouchCaptured || annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() || pageIndex != annot.getPage().getIndex() || !DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                    return false;
                }
                if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                    pageViewBBox = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageViewBBox, pageViewBBox, pageIndex);
                    float deltaXY = (mCtlPtLineWidth + (mCtlPtRadius * 2.0f)) + 2.0f;
                    PointF adjustXY;
                    switch (this.mLastOper) {
                        case -1:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mLastPoint.x, this.mPageViewRect.top, this.mPageViewRect.right, this.mLastPoint.y);
                                this.mAnnotMenuRect.set(envX, this.mPageViewRect.top, this.mPageViewRect.right, envY);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 1:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mLastPoint.x, this.mLastPoint.y, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(envX, envY, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 2:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mLastPoint.y, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, envY, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 3:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mLastPoint.y, this.mLastPoint.x, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, envY, envX, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 4:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mLastPoint.x, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, this.mPageViewRect.top, envX, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 5:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mLastPoint.x, this.mLastPoint.y);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, this.mPageViewRect.top, envX, envY);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 6:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mPageViewRect.right, this.mLastPoint.y);
                                this.mAnnotMenuRect.set(this.mPageViewRect.left, this.mPageViewRect.top, this.mPageViewRect.right, envY);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 7:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mLastPoint.x, this.mPageViewRect.top, this.mPageViewRect.right, this.mLastPoint.y);
                                this.mAnnotMenuRect.set(envX, this.mPageViewRect.top, this.mPageViewRect.right, envY);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 8:
                            if (!(envX == this.mLastPoint.x || envY == this.mLastPoint.y)) {
                                this.mInvalidateRect.set(this.mLastPoint.x, this.mPageViewRect.top, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mAnnotMenuRect.set(envX, this.mPageViewRect.top, this.mPageViewRect.right, this.mPageViewRect.bottom);
                                this.mInvalidateRect.sort();
                                this.mAnnotMenuRect.sort();
                                this.mInvalidateRect.union(this.mAnnotMenuRect);
                                this.mInvalidateRect.inset((-this.mThickness) - mCtlPtDeltyXY, (-this.mThickness) - mCtlPtDeltyXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                                adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                    this.mAnnotMenu.update(this.mAnnotMenuRect);
                                }
                                this.mLastPoint.set(envX, envY);
                                this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                                break;
                            }
                        case 9:
                            this.mInvalidateRect.set(pageViewBBox);
                            this.mAnnotMenuRect.set(pageViewBBox);
                            this.mInvalidateRect.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                            this.mAnnotMenuRect.offset(envX - this.mDownPoint.x, envY - this.mDownPoint.y);
                            adjustXY = adjustScalePointF(pageIndex, this.mAnnotMenuRect, deltaXY);
                            this.mInvalidateRect.union(this.mAnnotMenuRect);
                            this.mInvalidateRect.inset((-deltaXY) - mCtlPtDeltyXY, (-deltaXY) - mCtlPtDeltyXY);
                            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mInvalidateRect, this.mInvalidateRect, pageIndex);
                            this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(this.mInvalidateRect));
                            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mAnnotMenuRect, this.mAnnotMenuRect, pageIndex);
                            if (this.mAnnotMenu.isShowing()) {
                                this.mAnnotMenu.dismiss();
                                this.mAnnotMenu.update(this.mAnnotMenuRect);
                            }
                            this.mLastPoint.set(envX, envY);
                            this.mLastPoint.offset(adjustXY.x, adjustXY.y);
                            break;
                    }
                }
                return true;
            default:
                return false;
        }
        e1.printStackTrace();
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            this.mDocViewerPt.set(motionEvent.getX(), motionEvent.getY());
            PointF point = new PointF(motionEvent.getX(), motionEvent.getY());
            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
            this.mThickness = thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth());
            RectF _rect = annot.getRect();
            this.mPageViewRect.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mPageViewRect, this.mPageViewRect, pageIndex);
            this.mPageViewRect.inset(this.mThickness / 2.0f, this.mThickness / 2.0f);
            if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
                return true;
            } else if (pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, point)) {
                return true;
            } else {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                return true;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    private PointF[] calculateControlPoints(RectF rect) {
        rect.sort();
        this.mMapBounds.set(rect);
        this.mMapBounds.inset((-mCtlPtRadius) - (mCtlPtLineWidth / 2.0f), (-mCtlPtRadius) - (mCtlPtLineWidth / 2.0f));
        PointF p1 = new PointF(this.mMapBounds.left, this.mMapBounds.top);
        PointF p2 = new PointF((this.mMapBounds.right + this.mMapBounds.left) / 2.0f, this.mMapBounds.top);
        PointF p3 = new PointF(this.mMapBounds.right, this.mMapBounds.top);
        PointF p4 = new PointF(this.mMapBounds.right, (this.mMapBounds.bottom + this.mMapBounds.top) / 2.0f);
        PointF p5 = new PointF(this.mMapBounds.right, this.mMapBounds.bottom);
        PointF p6 = new PointF((this.mMapBounds.right + this.mMapBounds.left) / 2.0f, this.mMapBounds.bottom);
        PointF p7 = new PointF(this.mMapBounds.left, this.mMapBounds.bottom);
        PointF p8 = new PointF(this.mMapBounds.left, (this.mMapBounds.bottom + this.mMapBounds.top) / 2.0f);
        return new PointF[]{p1, p2, p3, p4, p5, p6, p7, p8};
    }

    private void drawControlPoints(Canvas canvas, RectF rectBBox, int color) {
        PointF[] ctlPts = calculateControlPoints(rectBBox);
        this.mCtlPtPaint.setStrokeWidth(mCtlPtLineWidth);
        for (PointF ctlPt : ctlPts) {
            this.mCtlPtPaint.setColor(-1);
            this.mCtlPtPaint.setStyle(Style.FILL);
            canvas.drawCircle(ctlPt.x, ctlPt.y, mCtlPtRadius, this.mCtlPtPaint);
            this.mCtlPtPaint.setColor(Color.parseColor("#179CD8"));
            this.mCtlPtPaint.setStyle(Style.STROKE);
            canvas.drawCircle(ctlPt.x, ctlPt.y, mCtlPtRadius, this.mCtlPtPaint);
        }
    }

    private void pathAddLine(Path path, float start_x, float start_y, float end_x, float end_y) {
        path.moveTo(start_x, start_y);
        path.lineTo(end_x, end_y);
    }

    private void drawControlImaginary(Canvas canvas, RectF rectBBox, int color) {
        PointF[] ctlPts = calculateControlPoints(rectBBox);
        this.mFrmPaint.setStrokeWidth(mCtlPtLineWidth);
        this.mFrmPaint.setColor(Color.parseColor("#179CD8"));
        this.mImaginaryPath.reset();
        pathAddLine(this.mImaginaryPath, mCtlPtRadius + ctlPts[0].x, ctlPts[0].y, ctlPts[1].x - mCtlPtRadius, ctlPts[1].y);
        pathAddLine(this.mImaginaryPath, mCtlPtRadius + ctlPts[1].x, ctlPts[1].y, ctlPts[2].x - mCtlPtRadius, ctlPts[2].y);
        pathAddLine(this.mImaginaryPath, ctlPts[2].x, mCtlPtRadius + ctlPts[2].y, ctlPts[3].x, ctlPts[3].y - mCtlPtRadius);
        pathAddLine(this.mImaginaryPath, ctlPts[3].x, mCtlPtRadius + ctlPts[3].y, ctlPts[4].x, ctlPts[4].y - mCtlPtRadius);
        pathAddLine(this.mImaginaryPath, ctlPts[4].x - mCtlPtRadius, ctlPts[4].y, mCtlPtRadius + ctlPts[5].x, ctlPts[5].y);
        pathAddLine(this.mImaginaryPath, ctlPts[5].x - mCtlPtRadius, ctlPts[5].y, mCtlPtRadius + ctlPts[6].x, ctlPts[6].y);
        pathAddLine(this.mImaginaryPath, ctlPts[6].x, ctlPts[6].y - mCtlPtRadius, ctlPts[7].x, mCtlPtRadius + ctlPts[7].y);
        pathAddLine(this.mImaginaryPath, ctlPts[7].x, ctlPts[7].y - mCtlPtRadius, ctlPts[0].x, mCtlPtRadius + ctlPts[0].y);
        canvas.drawPath(this.mImaginaryPath, this.mFrmPaint);
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && (annot instanceof Stamp)) {
            try {
                int annotPageIndex = annot.getPage().getIndex();
                if (this.mBitmapAnnot == annot && annotPageIndex == pageIndex) {
                    canvas.save();
                    canvas.setDrawFilter(this.mDrawFilter);
                    RectF frameRectF = new RectF();
                    RectF rect2 = annot.getRect();
                    float thickness = thicknessOnPageView(pageIndex, annot.getBorderInfo().getWidth());
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect2, rect2, pageIndex);
                    rect2.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    this.mPaint.setStyle(Style.FILL);
                    this.mPaint.setStrokeWidth(LineWidth2PageView(pageIndex, 0.6f));
                    int color = Color.parseColor("#4EA984");
                    this.mPaint.setColor(color);
                    frameRectF.set(rect2.left - ((float) this.mBBoxSpace), rect2.top - ((float) this.mBBoxSpace), rect2.right + ((float) this.mBBoxSpace), rect2.bottom + ((float) this.mBBoxSpace));
                    this.mPaintOut.setColor(color);
                    RectF _rect = annot.getRect();
                    this.mViewDrawRectInOnDraw.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mViewDrawRectInOnDraw, this.mViewDrawRectInOnDraw, pageIndex);
                    this.mViewDrawRectInOnDraw.inset(thickness / 2.0f, thickness / 2.0f);
                    if (this.mLastOper == 1) {
                        this.mBBoxInOnDraw.set(this.mLastPoint.x, this.mLastPoint.y, this.mViewDrawRectInOnDraw.right, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 2) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mLastPoint.y, this.mViewDrawRectInOnDraw.right, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 3) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mLastPoint.y, this.mLastPoint.x, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 4) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mViewDrawRectInOnDraw.top, this.mLastPoint.x, this.mViewDrawRectInOnDraw.bottom);
                    } else if (this.mLastOper == 5) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mViewDrawRectInOnDraw.top, this.mLastPoint.x, this.mLastPoint.y);
                    } else if (this.mLastOper == 6) {
                        this.mBBoxInOnDraw.set(this.mViewDrawRectInOnDraw.left, this.mViewDrawRectInOnDraw.top, this.mViewDrawRectInOnDraw.right, this.mLastPoint.y);
                    } else if (this.mLastOper == 7) {
                        this.mBBoxInOnDraw.set(this.mLastPoint.x, this.mViewDrawRectInOnDraw.top, this.mViewDrawRectInOnDraw.right, this.mLastPoint.y);
                    } else if (this.mLastOper == 8) {
                        this.mBBoxInOnDraw.set(this.mLastPoint.x, this.mViewDrawRectInOnDraw.top, this.mViewDrawRectInOnDraw.right, this.mViewDrawRectInOnDraw.bottom);
                    }
                    this.mBBoxInOnDraw.inset((-thickness) / 2.0f, (-thickness) / 2.0f);
                    if (this.mLastOper == 9 || this.mLastOper == -1) {
                        this.mBBoxInOnDraw = annot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mBBoxInOnDraw, this.mBBoxInOnDraw, pageIndex);
                        this.mBBoxInOnDraw.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    }
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        drawControlPoints(canvas, this.mBBoxInOnDraw, (int) annot.getBorderColor());
                        drawControlImaginary(canvas, this.mBBoxInOnDraw, (int) annot.getBorderColor());
                    }
                    canvas.restore();
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void setToolHandler(StampToolHandler toolHandler) {
        this.mToolHandler = toolHandler;
    }

    private void delAnnot(Annot annot, boolean addUndo, Callback result) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        try {
            PDFPage page = annot.getPage();
            if (page != null) {
                final RectF viewRect = annot.getRect();
                final int pageIndex = page.getIndex();
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
                final StampDeleteUndoItem undoItem = new StampDeleteUndoItem(this.mPdfViewCtrl);
                undoItem.setCurrentValue(annot);
                undoItem.mPageIndex = pageIndex;
                undoItem.mStampType = StampUntil.getStampTypeByName(undoItem.mSubject);
                undoItem.mIconName = undoItem.mSubject;
                undoItem.mDsip = this.mToolHandler.mDsip;
                if (undoItem.mStampType <= 17 && undoItem.mStampType != -1) {
                    undoItem.mBitmap = BitmapFactory.decodeResource(this.mContext.getResources(), this.mToolHandler.mStampIds[undoItem.mStampType].intValue());
                }
                final boolean z = addUndo;
                final Callback callback = result;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(3, undoItem, (Stamp) annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (z) {
                                DocumentManager.getInstance(StampAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            }
                            if (StampAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                                StampAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                                StampAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(viewRect));
                            }
                        }
                        if (callback != null) {
                            callback.result(null, success);
                        }
                    }
                }));
            } else if (result != null) {
                result.result(null, false);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private float LineWidth2PageView(int pageIndex, float linewidth) {
        RectF rectF = new RectF(0.0f, 0.0f, linewidth, linewidth);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
        return Math.abs(rectF.width());
    }

    private PointF adjustScalePointF(int pageIndex, RectF rectF, float dxy) {
        float adjustx = 0.0f;
        float adjusty = 0.0f;
        if (this.mLastOper != 9) {
            rectF.inset((-this.mThickness) / 2.0f, (-this.mThickness) / 2.0f);
        }
        if (((float) ((int) rectF.left)) < dxy) {
            adjustx = (-rectF.left) + dxy;
            rectF.left = dxy;
        }
        if (((float) ((int) rectF.top)) < dxy) {
            adjusty = (-rectF.top) + dxy;
            rectF.top = dxy;
        }
        if (((float) ((int) rectF.right)) > ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - dxy) {
            adjustx = (((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - rectF.right) - dxy;
            rectF.right = ((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - dxy;
        }
        if (((float) ((int) rectF.bottom)) > ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - dxy) {
            adjusty = (((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - rectF.bottom) - dxy;
            rectF.bottom = ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - dxy;
        }
        this.mAdjustPointF.set(adjustx, adjusty);
        return this.mAdjustPointF;
    }

    private int isTouchControlPoint(RectF rect, float x, float y) {
        PointF[] ctlPts = calculateControlPoints(rect);
        RectF area = new RectF();
        int ret = -1;
        for (int i = 0; i < ctlPts.length; i++) {
            area.set(ctlPts[i].x, ctlPts[i].y, ctlPts[i].x, ctlPts[i].y);
            area.inset(-mCtlPtTouchExt, -mCtlPtTouchExt);
            if (area.contains(x, y)) {
                ret = i + 1;
            }
        }
        return ret;
    }

    protected void onDrawForControls(Canvas canvas) {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                int annotPageIndex = curAnnot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(annotPageIndex)) {
                    float thickness = thicknessOnPageView(annotPageIndex, curAnnot.getBorderInfo().getWidth());
                    RectF _rect = curAnnot.getRect();
                    this.mViewDrawRect.set(_rect.left, _rect.top, _rect.right, _rect.bottom);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mViewDrawRect, this.mViewDrawRect, annotPageIndex);
                    this.mViewDrawRect.inset(thickness / 2.0f, thickness / 2.0f);
                    if (this.mLastOper == 1) {
                        this.mDocViewerBBox.left = this.mLastPoint.x;
                        this.mDocViewerBBox.top = this.mLastPoint.y;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 2) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mLastPoint.y;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 3) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mLastPoint.y;
                        this.mDocViewerBBox.right = this.mLastPoint.x;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 4) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mLastPoint.x;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    } else if (this.mLastOper == 5) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mLastPoint.x;
                        this.mDocViewerBBox.bottom = this.mLastPoint.y;
                    } else if (this.mLastOper == 6) {
                        this.mDocViewerBBox.left = this.mViewDrawRect.left;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mLastPoint.y;
                    } else if (this.mLastOper == 7) {
                        this.mDocViewerBBox.left = this.mLastPoint.x;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mLastPoint.y;
                    } else if (this.mLastOper == 8) {
                        this.mDocViewerBBox.left = this.mLastPoint.x;
                        this.mDocViewerBBox.top = this.mViewDrawRect.top;
                        this.mDocViewerBBox.right = this.mViewDrawRect.right;
                        this.mDocViewerBBox.bottom = this.mViewDrawRect.bottom;
                    }
                    this.mDocViewerBBox.inset((-thickness) / 2.0f, (-thickness) / 2.0f);
                    if (this.mLastOper == 9 || this.mLastOper == -1) {
                        this.mDocViewerBBox = curAnnot.getRect();
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mDocViewerBBox, this.mDocViewerBBox, annotPageIndex);
                        this.mDocViewerBBox.offset(this.mLastPoint.x - this.mDownPoint.x, this.mLastPoint.y - this.mDownPoint.y);
                    }
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mDocViewerBBox, this.mDocViewerBBox, annotPageIndex);
                    this.mAnnotMenu.update(this.mDocViewerBBox);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }
}
