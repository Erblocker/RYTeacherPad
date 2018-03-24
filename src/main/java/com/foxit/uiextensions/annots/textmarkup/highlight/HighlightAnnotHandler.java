package com.foxit.uiextensions.annots.textmarkup.highlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.text.ClipboardManager;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Highlight;
import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContent;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

class HighlightAnnotHandler implements AnnotHandler {
    private AnnotMenu mAnnotMenu;
    private PropertyBar mAnnotPropertyBar;
    private AppAnnotUtil mAppAnnotUtil;
    private int mBBoxSpace;
    private Context mContext;
    private boolean mIsAnnotModified;
    private boolean mIsEditProperty;
    private Annot mLastAnnot;
    private ArrayList<Integer> mMenuItems;
    private int mModifyAnnotColor;
    private int mModifyColor;
    private int mModifyOpacity;
    private int[] mPBColors = new int[PropertyBar.PB_COLORS_HIGHLIGHT.length];
    private Paint mPaintBbox;
    private int mPaintBoxOutset;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyChangeListener mPropertyChangeListener;
    private Rect mRect = new Rect();
    private RectF mRectF = new RectF();
    private HighlightToolHandler mToolHandler;
    private int mUndoColor;
    private String mUndoContents;
    private int mUndoOpacity;

    public HighlightAnnotHandler(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mAppAnnotUtil = AppAnnotUtil.getInstance(context);
        this.mBBoxSpace = AppAnnotUtil.getAnnotBBoxSpace();
        this.mPaintBbox = new Paint();
        this.mPaintBbox.setAntiAlias(true);
        this.mPaintBbox.setStyle(Style.STROKE);
        this.mPaintBbox.setStrokeWidth(this.mAppAnnotUtil.getAnnotBBoxStrokeWidth());
        this.mPaintBbox.setPathEffect(AppAnnotUtil.getAnnotBBoxPathEffect());
        this.mMenuItems = new ArrayList();
        this.mPaintBoxOutset = AppResource.getDimensionPixelSize(this.mContext, R.dimen.annot_highlight_paintbox_outset);
    }

    public void setAnnotMenu(AnnotMenu annotMenu) {
        this.mAnnotMenu = annotMenu;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotMenu;
    }

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    public int getType() {
        return 9;
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
        return getAnnotBBox(annot).contains(point.x, point.y);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (this.mPdfViewCtrl != null && annot != null && (annot instanceof Highlight) && this.mPdfViewCtrl.isPageVisible(pageIndex)) {
            try {
                if (pageIndex == annot.getPage().getIndex() && this.mLastAnnot == annot) {
                    RectF rectF = annot.getRect();
                    this.mRectF.set(rectF.left, rectF.top, rectF.right, rectF.bottom);
                    RectF deviceRt = new RectF();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mRectF, deviceRt, pageIndex);
                    deviceRt.roundOut(this.mRect);
                    this.mRect.inset(-this.mPaintBoxOutset, -this.mPaintBoxOutset);
                    canvas.save();
                    canvas.drawRect(this.mRect, this.mPaintBbox);
                    canvas.restore();
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            PointF pointF = AppAnnotUtil.getPdfPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
            if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
            } else if (!(pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pointF))) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return true;
    }

    private int getPBCustomColor() {
        return PropertyBar.PB_COLORS_HIGHLIGHT[0];
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mAnnotPropertyBar = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mAnnotPropertyBar;
    }

    public void onAnnotSelected(final Annot annot, boolean needInvalid) {
        try {
            this.mUndoColor = (int) annot.getBorderColor();
            this.mUndoOpacity = (int) ((((Highlight) annot).getOpacity() * 255.0f) + 0.5f);
            this.mPaintBbox.setColor(((int) annot.getBorderColor()) | -16777216);
            this.mMenuItems.clear();
            if (DocumentManager.getInstance(this.mPdfViewCtrl).canCopy()) {
                this.mMenuItems.add(Integer.valueOf(1));
            }
            this.mAnnotPropertyBar.setArrowVisible(false);
            if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                this.mMenuItems.add(Integer.valueOf(6));
                this.mMenuItems.add(Integer.valueOf(3));
                this.mMenuItems.add(Integer.valueOf(4));
                this.mMenuItems.add(Integer.valueOf(2));
            } else {
                this.mMenuItems.add(Integer.valueOf(3));
            }
            this.mAnnotMenu.setMenuItems(this.mMenuItems);
            this.mAnnotMenu.setListener(new ClickListener() {
                public void onAMClick(int type) {
                    if (1 == type) {
                        try {
                            ((ClipboardManager) HighlightAnnotHandler.this.mContext.getSystemService("clipboard")).setText(annot.getContent());
                            AppAnnotUtil.toastAnnotCopy(HighlightAnnotHandler.this.mContext);
                            DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                    } else if (2 == type) {
                        HighlightAnnotHandler.this.deleteAnnot(annot, true, null);
                    } else if (6 == type) {
                        HighlightAnnotHandler.this.mAnnotMenu.dismiss();
                        HighlightAnnotHandler.this.mIsEditProperty = true;
                        System.arraycopy(PropertyBar.PB_COLORS_HIGHLIGHT, 0, HighlightAnnotHandler.this.mPBColors, 0, HighlightAnnotHandler.this.mPBColors.length);
                        HighlightAnnotHandler.this.mPBColors[0] = HighlightAnnotHandler.this.getPBCustomColor();
                        HighlightAnnotHandler.this.mAnnotPropertyBar.setColors(HighlightAnnotHandler.this.mPBColors);
                        HighlightAnnotHandler.this.mAnnotPropertyBar.setProperty(1, (int) annot.getBorderColor());
                        HighlightAnnotHandler.this.mAnnotPropertyBar.setProperty(2, AppDmUtil.opacity255To100((int) ((((Highlight) annot).getOpacity() * 255.0f) + 0.5f)));
                        HighlightAnnotHandler.this.mAnnotPropertyBar.reset(3);
                        RectF annotRectF = annot.getRect();
                        int _pageIndex = annot.getPage().getIndex();
                        RectF deviceRt = new RectF();
                        if (HighlightAnnotHandler.this.mPdfViewCtrl.isPageVisible(_pageIndex) && HighlightAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRt, _pageIndex)) {
                            HighlightAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(deviceRt, annotRectF, _pageIndex);
                        }
                        HighlightAnnotHandler.this.mAnnotPropertyBar.show(annotRectF, false);
                        HighlightAnnotHandler.this.mAnnotPropertyBar.setPropertyChangeListener(HighlightAnnotHandler.this.mPropertyChangeListener);
                    } else if (3 == type) {
                        DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.showComments(HighlightAnnotHandler.this.mContext, HighlightAnnotHandler.this.mPdfViewCtrl, HighlightAnnotHandler.this.mParent, annot);
                    } else if (4 == type) {
                        DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.replyToAnnot(HighlightAnnotHandler.this.mContext, HighlightAnnotHandler.this.mPdfViewCtrl, HighlightAnnotHandler.this.mParent, annot);
                    }
                }
            });
            int _pageIndex = annot.getPage().getIndex();
            RectF annotRectF = annot.getRect();
            if (this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                RectF deviceRt = new RectF();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRt, _pageIndex);
                Rect rect = rectRoundOut(deviceRt, 0);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(deviceRt, annotRectF, _pageIndex);
                this.mAnnotMenu.show(annotRectF);
                this.mPdfViewCtrl.refresh(_pageIndex, rect);
                if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    this.mLastAnnot = annot;
                    return;
                }
                return;
            }
            this.mLastAnnot = annot;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean needInvalid) {
        this.mAnnotMenu.dismiss();
        this.mMenuItems.clear();
        if (this.mIsEditProperty) {
            this.mIsEditProperty = false;
            this.mAnnotPropertyBar.dismiss();
        }
        if (this.mIsAnnotModified && needInvalid) {
            if (!(this.mUndoColor == this.mModifyAnnotColor && this.mUndoOpacity == this.mModifyOpacity)) {
                modifyAnnot(annot, this.mModifyColor, this.mModifyOpacity, null, true, null);
            }
        } else if (this.mIsAnnotModified) {
            try {
                annot.setBorderColor((long) this.mUndoColor);
                ((Highlight) annot).setOpacity(((float) this.mUndoOpacity) / 255.0f);
                annot.resetAppearanceStream();
            } catch (PDFException e) {
                if (e.getLastError() == PDFError.OOM.getCode()) {
                    this.mPdfViewCtrl.recoverForOOM();
                    return;
                }
                return;
            }
        }
        this.mIsAnnotModified = false;
        if (needInvalid) {
            try {
                int _pageIndex = annot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                    RectF rectF = new RectF();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(annot.getRect(), rectF, _pageIndex);
                    this.mPdfViewCtrl.refresh(_pageIndex, rectRoundOut(rectF, 2));
                    this.mLastAnnot = null;
                    return;
                }
                return;
            } catch (Exception e2) {
                e2.printStackTrace();
                return;
            }
        }
        this.mLastAnnot = null;
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        deleteAnnot(annot, addUndo, result);
    }

    public void modifyAnnotColor(int color) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            this.mModifyColor = ViewCompat.MEASURED_SIZE_MASK & color;
            try {
                this.mModifyOpacity = (int) (((Highlight) annot).getOpacity() * 255.0f);
                this.mModifyAnnotColor = this.mModifyColor;
                if (annot.getBorderColor() != ((long) this.mModifyAnnotColor)) {
                    this.mIsAnnotModified = true;
                    annot.setBorderColor((long) this.mModifyAnnotColor);
                    ((Highlight) annot).setOpacity(((float) this.mModifyOpacity) / 255.0f);
                    annot.resetAppearanceStream();
                    this.mPaintBbox.setColor(this.mModifyAnnotColor | -16777216);
                    invalidateForToolModify(annot);
                }
            } catch (PDFException e) {
                if (e.getLastError() == PDFError.OOM.getCode()) {
                    this.mPdfViewCtrl.recoverForOOM();
                }
            }
        }
    }

    public void modifyAnnotOpacity(int opacity) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                this.mModifyColor = ((int) annot.getBorderColor()) & ViewCompat.MEASURED_SIZE_MASK;
                this.mModifyOpacity = opacity;
                this.mModifyAnnotColor = this.mModifyColor;
                if (((Highlight) annot).getOpacity() * 255.0f != ((float) this.mModifyOpacity)) {
                    this.mIsAnnotModified = true;
                    annot.setBorderColor((long) this.mModifyAnnotColor);
                    ((Highlight) annot).setOpacity(((float) this.mModifyOpacity) / 255.0f);
                    annot.resetAppearanceStream();
                    this.mPaintBbox.setColor(this.mModifyAnnotColor | -16777216);
                    invalidateForToolModify(annot);
                }
            } catch (PDFException e) {
                if (e.getLastError() == PDFError.OOM.getCode()) {
                    this.mPdfViewCtrl.recoverForOOM();
                }
            }
        }
    }

    private void modifyAnnot(Annot annot, int color, int opacity, DateTime modifyDate, boolean addUndo, Callback callback) {
        try {
            final PDFPage page = annot.getPage();
            if (page != null) {
                if (modifyDate == null) {
                    modifyDate = new DateTime();
                }
                final HighlightModifyUndoItem undoItem = new HighlightModifyUndoItem(this.mPdfViewCtrl);
                undoItem.setCurrentValue(annot);
                undoItem.mPageIndex = page.getIndex();
                undoItem.mColor = (long) color;
                undoItem.mOpacity = ((float) opacity) / 255.0f;
                undoItem.mModifiedDate = modifyDate;
                undoItem.mRedoColor = color;
                undoItem.mRedoOpacity = ((float) opacity) / 255.0f;
                undoItem.mRedoContents = annot.getContent();
                undoItem.mUndoColor = this.mUndoColor;
                undoItem.mUndoOpacity = ((float) this.mUndoOpacity) / 255.0f;
                undoItem.mUndoContents = this.mUndoContents;
                undoItem.mPaintBbox = this.mPaintBbox;
                final Annot annot2 = annot;
                final boolean z = addUndo;
                final Callback callback2 = callback;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new HighlightEvent(2, undoItem, (Highlight) annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).onAnnotModified(page, annot2);
                            if (z) {
                                DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            } else {
                                try {
                                    if (HighlightAnnotHandler.this.mPdfViewCtrl.isPageVisible(page.getIndex())) {
                                        RectF annotRectF = new RectF();
                                        HighlightAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annot2.getRect(), annotRectF, page.getIndex());
                                        HighlightAnnotHandler.this.mPdfViewCtrl.refresh(page.getIndex(), AppDmUtil.rectFToRect(annotRectF));
                                    }
                                } catch (PDFException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (callback2 != null) {
                            callback2.result(null, success);
                        }
                    }
                }));
            } else if (callback != null) {
                callback.result(null, false);
            }
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    private void deleteAnnot(Annot annot, boolean addUndo, Callback result) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        try {
            final RectF annotRectF = annot.getRect();
            PDFPage page = annot.getPage();
            if (page != null) {
                final int pageIndex = page.getIndex();
                DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
                final HighlightDeleteUndoItem undoItem = new HighlightDeleteUndoItem(this.mPdfViewCtrl);
                undoItem.setCurrentValue(annot);
                undoItem.mPageIndex = pageIndex;
                int count = ((Highlight) annot).getQuadPointsCount();
                undoItem.quadPointsArray = new QuadPoints[count];
                for (int i = 0; i < count; i++) {
                    undoItem.quadPointsArray[i] = ((Highlight) annot).getQuadPoints(i);
                }
                final boolean z = addUndo;
                final Annot annot2 = annot;
                final Callback callback = result;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new HighlightEvent(3, undoItem, (Highlight) annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            if (z) {
                                DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            }
                            if (HighlightAnnotHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                                RectF deviceRectF = new RectF();
                                HighlightAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, pageIndex);
                                HighlightAnnotHandler.this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(deviceRectF));
                                if (annot2 == DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                                    HighlightAnnotHandler.this.mLastAnnot = null;
                                }
                            } else if (annot2 == DocumentManager.getInstance(HighlightAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                                HighlightAnnotHandler.this.mLastAnnot = null;
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
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    private void invalidateForToolModify(Annot annot) {
        try {
            int pageIndex = annot.getPage().getIndex();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                RectF rectF = annot.getRect();
                RectF pvRect = new RectF();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, pvRect, pageIndex);
                Rect rect = rectRoundOut(pvRect, this.mBBoxSpace);
                rect.inset(-this.mPaintBoxOutset, -this.mPaintBoxOutset);
                this.mPdfViewCtrl.refresh(pageIndex, rect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Rect rectRoundOut(RectF rectF, int roundSize) {
        Rect rect = new Rect();
        rectF.roundOut(rect);
        rect.inset(-roundSize, -roundSize);
        return rect;
    }

    public void addAnnot(int pageIndex, AnnotContent contentSupplier, boolean addUndo, Callback result) {
        if (this.mToolHandler != null) {
            if (!(contentSupplier instanceof TextMarkupContent)) {
                this.mToolHandler.setFromSelector(true);
            }
            this.mToolHandler.addAnnot(pageIndex, addUndo, contentSupplier, result);
        } else if (result != null) {
            result.result(null, false);
        }
    }

    public void setToolHandler(HighlightToolHandler toolHandler) {
        this.mToolHandler = toolHandler;
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        if (content != null) {
            try {
                this.mUndoColor = (int) annot.getBorderColor();
                this.mUndoOpacity = (int) (((Highlight) annot).getOpacity() * 255.0f);
                this.mUndoContents = annot.getContent();
                if (content.getContents() != null) {
                    annot.setContent(content.getContents());
                } else {
                    annot.setContent("");
                }
                if (this.mLastAnnot == annot) {
                    this.mPaintBbox.setColor(content.getColor() | -16777216);
                }
                modifyAnnot(annot, content.getColor(), content.getOpacity(), content.getModifiedDate(), addUndo, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (result != null) {
            result.result(null, false);
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (this.mPdfViewCtrl != null && annot != null && (annot instanceof Highlight) && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                int annotPageIndex = annot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(annotPageIndex)) {
                    this.mRectF.set(annot.getRect());
                    RectF deviceRt = new RectF();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mRectF, deviceRt, annotPageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(deviceRt, this.mRectF, annotPageIndex);
                    if (this.mIsEditProperty) {
                        ((PropertyBarImpl) this.mAnnotPropertyBar).onConfigurationChanged(this.mRectF);
                    }
                    this.mAnnotMenu.update(this.mRectF);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeProbarListener() {
        this.mPropertyChangeListener = null;
    }
}
