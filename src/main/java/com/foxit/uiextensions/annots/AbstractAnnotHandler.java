package com.foxit.uiextensions.annots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.BorderInfo;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.IAnnotTaskResult;
import com.foxit.uiextensions.annots.common.UIAnnotFrame;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

public abstract class AbstractAnnotHandler implements AnnotHandler, PropertyChangeListener {
    protected AnnotMenu mAnnotMenu;
    protected RectF mBackRect;
    protected float mBackThickness;
    protected int mColor;
    protected Context mContext;
    protected int mCtl;
    protected PointF mDownPt;
    protected boolean mIsModified;
    protected PointF mLastPt;
    protected int mOp;
    protected int mOpacity;
    protected Paint mPaint;
    protected ViewGroup mParent;
    protected PDFViewCtrl mPdfViewCtrl;
    protected PropertyBar mPropertyBar;
    protected Annot mSelectedAnnot;
    protected float mThickness;
    protected boolean mTouchCaptured;
    protected int mType;
    private Rect tv_rect1 = new Rect();

    protected abstract void dismissPopupMenu();

    protected abstract ArrayList<Path> generatePathData(PDFViewCtrl pDFViewCtrl, int i, Annot annot);

    protected abstract long getSupportedProperties();

    protected abstract AbstractToolHandler getToolHandler();

    protected abstract void resetStatus();

    protected abstract void showPopupMenu();

    protected abstract void transformAnnot(PDFViewCtrl pDFViewCtrl, int i, Annot annot, Matrix matrix);

    public AbstractAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, int type) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mType = type;
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mDownPt = new PointF();
        this.mLastPt = new PointF();
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

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
        if (this.mSelectedAnnot != null) {
            try {
                this.mSelectedAnnot.setBorderColor((long) color);
                this.mSelectedAnnot.resetAppearanceStream();
            } catch (PDFException e) {
                e.printStackTrace();
            }
            this.mIsModified = true;
            invalidatePageView(this.mSelectedAnnot, 0.0f, 0.0f);
        }
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    public void setOpacity(int opacity) {
        this.mOpacity = opacity;
        if (this.mSelectedAnnot != null) {
            try {
                ((Markup) this.mSelectedAnnot).setOpacity(((float) AppDmUtil.opacity100To255(opacity)) / 255.0f);
                this.mSelectedAnnot.resetAppearanceStream();
            } catch (PDFException e) {
                e.printStackTrace();
            }
            this.mIsModified = true;
            invalidatePageView(this.mSelectedAnnot, 0.0f, 0.0f);
        }
    }

    public float getThickness() {
        return this.mThickness;
    }

    public void setThickness(float thickness) {
        this.mThickness = thickness;
        if (this.mSelectedAnnot != null) {
            try {
                BorderInfo borderInfo = this.mSelectedAnnot.getBorderInfo();
                float dt = (thickness - borderInfo.getWidth()) / 2.0f;
                this.mSelectedAnnot.getRect().inset(-dt, -dt);
                borderInfo.setWidth(thickness);
                this.mSelectedAnnot.setBorderInfo(borderInfo);
                this.mSelectedAnnot.resetAppearanceStream();
                this.mIsModified = true;
                if (dt > 0.0f) {
                    invalidatePageView(this.mSelectedAnnot, 0.0f, 0.0f);
                } else {
                    invalidatePageView(this.mSelectedAnnot, (-dt) + 1.0f, (-dt) + 1.0f);
                }
            } catch (PDFException e) {
            }
        }
    }

    public String getFontName() {
        return null;
    }

    public void setFontName(String name) {
    }

    public float getFontSize() {
        return 0.0f;
    }

    public void setFontSize(float size) {
    }

    public void onValueChanged(long property, int value) {
        if (property == 1 || property == 128) {
            setColor(value);
        } else if (property == 2) {
            setOpacity(value);
        }
    }

    public void onValueChanged(long property, float value) {
        if (property == 4) {
            setThickness(value);
        }
    }

    public void onValueChanged(long property, String value) {
    }

    public int getType() {
        return this.mType;
    }

    public boolean annotCanAnswer(Annot annot) {
        try {
            if (annot.getType() == this.mType) {
                return true;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return false;
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
        try {
            int pageIndex = annot.getPage().getIndex();
            RectF rectF = getAnnotBBox(annot);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
            return rectF.contains(point.x, point.y);
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addAnnot(int pageIndex, AnnotContent supplier, boolean addUndo, Callback result) {
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) != this || keyCode != 4) {
            return false;
        }
        DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        return true;
    }

    public void onAnnotSelected(Annot annot, boolean reRender) {
        this.mIsModified = false;
        try {
            int pageIndex = annot.getPage().getIndex();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                RectF docBBox = annot.getRect();
                RectF pvBBox = new RectF();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(docBBox, pvBBox, pageIndex);
                this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(pvBBox));
                if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    this.mBackRect = annot.getRect();
                    this.mBackThickness = annot.getBorderInfo().getWidth();
                    this.mSelectedAnnot = annot;
                }
            } else {
                this.mBackRect = annot.getRect();
                this.mBackThickness = annot.getBorderInfo().getWidth();
                this.mSelectedAnnot = annot;
            }
            showPopupMenu();
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        try {
            int pageIndex = annot.getPage().getIndex();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                RectF bounds = UIAnnotFrame.calculateBounds(this.mPdfViewCtrl, pageIndex, annot);
                Rect rect = new Rect();
                bounds.roundOut(rect);
                if (reRender) {
                    this.mPdfViewCtrl.refresh(pageIndex, rect);
                    if (this.mSelectedAnnot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        resetStatus();
                    }
                } else {
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(AppDmUtil.rectToRectF(rect), AppDmUtil.rectToRectF(rect), pageIndex);
                    this.mPdfViewCtrl.invalidate(rect);
                    resetStatus();
                }
            } else {
                resetStatus();
            }
            dismissPopupMenu();
            hidePropertyBar();
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e, Annot annot) {
        PointF pointF = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(pointF, pointF, pageIndex);
        try {
            int action = e.getAction();
            switch (action) {
                case 0:
                    if (pageIndex == annot.getPage().getIndex() && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        this.mCtl = UIAnnotFrame.getInstance(this.mContext).hitControlTest(UIAnnotFrame.calculateBounds(this.mPdfViewCtrl, pageIndex, annot), pointF);
                        if (this.mCtl != -1) {
                            this.mTouchCaptured = true;
                            this.mOp = 1;
                            this.mDownPt.set(pointF);
                            this.mLastPt.set(pointF);
                            return true;
                        } else if (isHitAnnot(annot, pointF)) {
                            this.mTouchCaptured = true;
                            this.mOp = 0;
                            this.mDownPt.set(pointF);
                            this.mLastPt.set(pointF);
                            return true;
                        }
                    }
                    break;
                case 1:
                case 2:
                case 3:
                    if (this.mTouchCaptured && pageIndex == annot.getPage().getIndex() && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        RectF bbox;
                        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                            if (!(pointF.x == this.mLastPt.x && pointF.y == this.mLastPt.y)) {
                                if (this.mAnnotMenu.isShowing()) {
                                    this.mAnnotMenu.dismiss();
                                }
                                RectF bounds0 = UIAnnotFrame.mapBounds(this.mPdfViewCtrl, pageIndex, annot, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                                RectF bounds1 = UIAnnotFrame.mapBounds(this.mPdfViewCtrl, pageIndex, annot, this.mOp, this.mCtl, pointF.x - this.mDownPt.x, pointF.y - this.mDownPt.y);
                                PointF adjust = UIAnnotFrame.getInstance(this.mContext).calculateCorrection(this.mPdfViewCtrl, pageIndex, bounds1, this.mOp, this.mCtl);
                                UIAnnotFrame.adjustBounds(bounds1, this.mOp, this.mCtl, adjust);
                                this.mLastPt.set(pointF.x + adjust.x, pointF.y + adjust.y);
                                bounds1.union(bounds0);
                                UIAnnotFrame.getInstance(this.mContext).extentBoundsToContainControl(bounds1);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bounds1, bounds1, pageIndex);
                                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(bounds1));
                            }
                            if (action == 1 || action == 3) {
                                if (!this.mLastPt.equals(this.mDownPt)) {
                                    bbox = annot.getRect();
                                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                                    Matrix matrix = UIAnnotFrame.calculateOperateMatrix(bbox, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                                    transformAnnot(this.mPdfViewCtrl, pageIndex, annot, matrix);
                                    this.mIsModified = true;
                                }
                                this.mTouchCaptured = false;
                                this.mDownPt.set(0.0f, 0.0f);
                                this.mLastPt.set(0.0f, 0.0f);
                                this.mOp = -1;
                                this.mCtl = -1;
                                if (this.mSelectedAnnot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                                    bbox = annot.getRect();
                                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                                    this.mAnnotMenu.show(bbox);
                                }
                            }
                            return true;
                        }
                        if (action == 1 || action == 3) {
                            this.mTouchCaptured = false;
                            this.mDownPt.set(0.0f, 0.0f);
                            this.mLastPt.set(0.0f, 0.0f);
                            this.mOp = -1;
                            this.mCtl = -1;
                            if (this.mSelectedAnnot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                                bbox = annot.getRect();
                                this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                                this.mAnnotMenu.show(bbox);
                            }
                        }
                        return true;
                    }
            }
        } catch (PDFException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    private boolean onSingleTapOrLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        PointF point = new PointF(motionEvent.getX(), motionEvent.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        try {
            if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
            } else if (!(pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, point))) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return onSingleTapOrLongPress(pageIndex, motionEvent, annot);
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return onSingleTapOrLongPress(pageIndex, motionEvent, annot);
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (annot.getType() == this.mType && this.mSelectedAnnot == annot && annot.getPage().getIndex() == pageIndex) {
                    RectF bbox = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                    Matrix matrix = UIAnnotFrame.calculateOperateMatrix(bbox, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                    RectF mapBounds = UIAnnotFrame.mapBounds(this.mPdfViewCtrl, pageIndex, annot, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                    ArrayList<Path> paths = generatePathData(this.mPdfViewCtrl, pageIndex, this.mSelectedAnnot);
                    if (paths != null) {
                        for (int i = 0; i < paths.size(); i++) {
                            ((Path) paths.get(i)).transform(matrix);
                            setPaintProperty(this.mPdfViewCtrl, pageIndex, this.mPaint, this.mSelectedAnnot);
                            canvas.drawPath((Path) paths.get(i), this.mPaint);
                        }
                    }
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        UIAnnotFrame.getInstance(this.mContext).draw(canvas, mapBounds, (int) annot.getBorderColor(), (int) ((((Markup) annot).getOpacity() * 255.0f) + 0.5f));
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                int pageIndex = annot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    RectF bbox = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                    this.mAnnotMenu.update(bbox);
                    if (this.mPropertyBar.isShowing()) {
                        this.mPropertyBar.update(bbox);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    protected RectF getBBox(PDFViewCtrl pdfViewCtrl, Annot annot) {
        try {
            int pageIndex = annot.getPage().getIndex();
            RectF bbox = annot.getRect();
            pdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
            return bbox;
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void invalidatePageView(Annot annot, float ddx, float ddy) {
        try {
            int pageIndex = annot.getPage().getIndex();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                RectF bounds = UIAnnotFrame.calculateBounds(this.mPdfViewCtrl, pageIndex, annot);
                UIAnnotFrame.getInstance(this.mContext).extentBoundsToContainControl(bounds);
                bounds.inset((-UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, ddx)) - 5.0f, (-UIAnnotFrame.getPageViewThickness(this.mPdfViewCtrl, pageIndex, ddy)) - 5.0f);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bounds, bounds, pageIndex);
                this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(bounds));
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public Annot handleAddAnnot(int pageIndex, Annot annot, EditAnnotEvent addEvent, boolean addUndo, IAnnotTaskResult<PDFPage, Annot, Void> result) {
        try {
            final PDFPage page = annot.getPage();
            final Annot annot2 = annot;
            final boolean z = addUndo;
            final EditAnnotEvent editAnnotEvent = addEvent;
            final int i = pageIndex;
            final IAnnotTaskResult<PDFPage, Annot, Void> iAnnotTaskResult = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(addEvent, new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(AbstractAnnotHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot2);
                        if (z) {
                            DocumentManager.getInstance(AbstractAnnotHandler.this.mPdfViewCtrl).addUndoItem(editAnnotEvent.mUndoItem);
                        }
                        if (AbstractAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                            RectF pvRect = AbstractAnnotHandler.this.getBBox(AbstractAnnotHandler.this.mPdfViewCtrl, annot2);
                            Rect tv_rect1 = new Rect();
                            pvRect.roundOut(tv_rect1);
                            AbstractAnnotHandler.this.mPdfViewCtrl.refresh(i, tv_rect1);
                        }
                    }
                    if (iAnnotTaskResult != null) {
                        iAnnotTaskResult.onResult(success, page, annot2, null);
                    }
                }
            }));
            return annot;
        } catch (PDFException e) {
            return null;
        }
    }

    protected void handleModifyAnnot(Annot annot, EditAnnotEvent modifyEvent, boolean addUndo, boolean reRender, IAnnotTaskResult<PDFPage, Annot, Void> result) {
        DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(addUndo);
        final Annot annot2 = annot;
        final boolean z = addUndo;
        final EditAnnotEvent editAnnotEvent = modifyEvent;
        final boolean z2 = reRender;
        final IAnnotTaskResult<PDFPage, Annot, Void> iAnnotTaskResult = result;
        this.mPdfViewCtrl.addTask(new EditAnnotTask(modifyEvent, new Callback() {
            public void result(Event event, boolean success) {
                if (success) {
                    try {
                        PDFPage page = annot2.getPage();
                        int pageIndex = page.getIndex();
                        DocumentManager.getInstance(AbstractAnnotHandler.this.mPdfViewCtrl).onAnnotModified(page, annot2);
                        if (z) {
                            DocumentManager.getInstance(AbstractAnnotHandler.this.mPdfViewCtrl).addUndoItem(editAnnotEvent.mUndoItem);
                        }
                        DocumentManager.getInstance(AbstractAnnotHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                        if (z2 && AbstractAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            float oldLineWidth;
                            RectF oldBbox = new RectF();
                            if (editAnnotEvent.useOldValue) {
                                oldBbox.set(editAnnotEvent.mUndoItem.mBBox);
                                oldLineWidth = editAnnotEvent.mUndoItem.mLineWidth;
                            } else {
                                oldBbox.set(editAnnotEvent.mUndoItem.mOldBBox);
                                oldLineWidth = editAnnotEvent.mUndoItem.mOldLineWidth;
                            }
                            RectF oldRect = UIAnnotFrame.calculateBounds(AbstractAnnotHandler.this.mPdfViewCtrl, pageIndex, oldBbox, oldLineWidth);
                            RectF pvRect = UIAnnotFrame.calculateBounds(AbstractAnnotHandler.this.mPdfViewCtrl, pageIndex, annot2);
                            pvRect.union(oldRect);
                            pvRect.roundOut(AbstractAnnotHandler.this.tv_rect1);
                            AbstractAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AbstractAnnotHandler.this.tv_rect1);
                        } else if (iAnnotTaskResult != null) {
                            iAnnotTaskResult.onResult(true, page, annot2, null);
                        }
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                }
                if (iAnnotTaskResult != null) {
                    iAnnotTaskResult.onResult(success, null, null, null);
                }
            }
        }));
    }

    protected void handleRemoveAnnot(Annot annot, EditAnnotEvent deleteEvent, boolean addUndo, IAnnotTaskResult<PDFPage, Void, Void> result) {
        try {
            if (annot.getUniqueID() != null && annot.getUniqueID().length() > 0 && DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() != null && annot.getUniqueID().equals(DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot().getUniqueID())) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            final PDFPage page = annot.getPage();
            final int pageIndex = page.getIndex();
            final RectF pvRect = getBBox(this.mPdfViewCtrl, annot);
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final boolean z = addUndo;
            final EditAnnotEvent editAnnotEvent = deleteEvent;
            final IAnnotTaskResult<PDFPage, Void, Void> iAnnotTaskResult = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(deleteEvent, new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (z) {
                            DocumentManager.getInstance(AbstractAnnotHandler.this.mPdfViewCtrl).addUndoItem(editAnnotEvent.mUndoItem);
                        }
                        if (AbstractAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            pvRect.roundOut(AbstractAnnotHandler.this.tv_rect1);
                            AbstractAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AbstractAnnotHandler.this.tv_rect1);
                        }
                    }
                    if (iAnnotTaskResult != null) {
                        iAnnotTaskResult.onResult(success, page, null, null);
                    }
                }
            }));
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void setPaintProperty(PDFViewCtrl pdfViewCtrl, int pageIndex, Paint paint, Annot annot) {
        try {
            paint.setColor((int) annot.getBorderColor());
            paint.setAlpha((int) (((Markup) annot).getOpacity() * 255.0f));
            paint.setStrokeWidth(UIAnnotFrame.getPageViewThickness(pdfViewCtrl, pageIndex, annot.getBorderInfo().getWidth()));
        } catch (PDFException e) {
        }
    }

    protected void showPropertyBar(long curProperty) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            this.mPropertyBar.setPropertyChangeListener(this);
            setPropertyBarProperties(this.mPropertyBar);
            this.mPropertyBar.reset(getSupportedProperties());
            try {
                RectF bbox = annot.getRect();
                int pageIndex = annot.getPage().getIndex();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                this.mPropertyBar.show(bbox, false);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    protected void hidePropertyBar() {
        if (this.mPropertyBar.isShowing()) {
            this.mPropertyBar.dismiss();
        }
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        propertyBar.setProperty(1, getColor());
        propertyBar.setProperty(2, getOpacity());
        propertyBar.setProperty(4, getThickness());
        propertyBar.setArrowVisible(false);
    }
}
