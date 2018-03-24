package com.foxit.uiextensions.annots.textmarkup.underline;

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
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.sdk.pdf.annots.Underline;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContent;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContentAbs;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupUtil;
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineToolHandler.SelectInfo;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.ArrayList;

class UnderlineAnnotHandler implements AnnotHandler {
    private AnnotMenu mAnnotMenu;
    private PropertyBar mAnnotPropertyBar;
    private AppAnnotUtil mAppAnnotUtil;
    private int mBBoxSpace;
    private Context mContext;
    private RectF mDrawLocal_tmpF;
    private boolean mIsAnnotModified;
    private boolean mIsEditProperty;
    private Annot mLastAnnot;
    private ArrayList<Integer> mMenuItems;
    private int mModifyAnnotColor;
    private int mModifyColor;
    private int mModifyOpacity;
    private int[] mPBColors = new int[PropertyBar.PB_COLORS_UNDERLINE.length];
    private Paint mPaintBbox;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PropertyChangeListener mPropertyChangeListener;
    private int mTmpUndoColor;
    private String mTmpUndoContents;
    private int mTmpUndoOpacity;
    private UnderlineToolHandler mUnderlineToolHandler;

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    public UnderlineAnnotHandler(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
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
        this.mDrawLocal_tmpF = new RectF();
        this.mMenuItems = new ArrayList();
    }

    public void setAnnotMenu(AnnotMenu annotMenu) {
        this.mAnnotMenu = annotMenu;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotMenu;
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mAnnotPropertyBar = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mAnnotPropertyBar;
    }

    public void setToolHandler(UnderlineToolHandler toolHandler) {
        this.mUnderlineToolHandler = toolHandler;
    }

    public int getType() {
        return 10;
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

    public int getPBCustomColor() {
        return PropertyBar.PB_COLORS_UNDERLINE[0];
    }

    public void onAnnotSelected(final Annot annot, boolean needInvalid) {
        try {
            this.mTmpUndoColor = (int) annot.getBorderColor();
            this.mTmpUndoOpacity = (int) ((((Markup) annot).getOpacity() * 255.0f) + 0.5f);
            this.mPaintBbox.setColor(this.mTmpUndoColor | -16777216);
            this.mAnnotPropertyBar.setArrowVisible(false);
            resetMenuItems();
            this.mAnnotMenu.setMenuItems(this.mMenuItems);
            this.mAnnotMenu.setListener(new ClickListener() {
                public void onAMClick(int btType) {
                    if (btType == 1) {
                        try {
                            ((ClipboardManager) UnderlineAnnotHandler.this.mContext.getSystemService("clipboard")).setText(annot.getContent());
                            AppAnnotUtil.toastAnnotCopy(UnderlineAnnotHandler.this.mContext);
                            DocumentManager.getInstance(UnderlineAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                    } else if (btType == 2) {
                        UnderlineAnnotHandler.this.DeleteAnnot(annot, true, null);
                    } else if (btType == 6) {
                        UnderlineAnnotHandler.this.mAnnotMenu.dismiss();
                        UnderlineAnnotHandler.this.mIsEditProperty = true;
                        System.arraycopy(PropertyBar.PB_COLORS_UNDERLINE, 0, UnderlineAnnotHandler.this.mPBColors, 0, UnderlineAnnotHandler.this.mPBColors.length);
                        UnderlineAnnotHandler.this.mPBColors[0] = UnderlineAnnotHandler.this.getPBCustomColor();
                        UnderlineAnnotHandler.this.mAnnotPropertyBar.setColors(UnderlineAnnotHandler.this.mPBColors);
                        UnderlineAnnotHandler.this.mAnnotPropertyBar.setProperty(1, (int) annot.getBorderColor());
                        UnderlineAnnotHandler.this.mAnnotPropertyBar.setProperty(2, AppDmUtil.opacity255To100((int) ((((Markup) annot).getOpacity() * 255.0f) + 0.5f)));
                        UnderlineAnnotHandler.this.mAnnotPropertyBar.reset(3);
                        RectF annotRectF = new RectF();
                        int _pageIndex = annot.getPage().getIndex();
                        if (UnderlineAnnotHandler.this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                            UnderlineAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annot.getRect(), annotRectF, _pageIndex);
                            UnderlineAnnotHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(annotRectF, annotRectF, _pageIndex);
                        }
                        UnderlineAnnotHandler.this.mAnnotPropertyBar.show(annotRectF, false);
                        UnderlineAnnotHandler.this.mAnnotPropertyBar.setPropertyChangeListener(UnderlineAnnotHandler.this.mPropertyChangeListener);
                    } else if (btType == 3) {
                        DocumentManager.getInstance(UnderlineAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.showComments(UnderlineAnnotHandler.this.mContext, UnderlineAnnotHandler.this.mPdfViewCtrl, UnderlineAnnotHandler.this.mParent, annot);
                    } else if (btType == 4) {
                        DocumentManager.getInstance(UnderlineAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        UIAnnotReply.replyToAnnot(UnderlineAnnotHandler.this.mContext, UnderlineAnnotHandler.this.mPdfViewCtrl, UnderlineAnnotHandler.this.mParent, annot);
                    }
                }
            });
            RectF annotRectF = annot.getRect();
            int _pageIndex = annot.getPage().getIndex();
            if (this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(annot.getRect(), annotRectF, _pageIndex);
                Rect rect = TextMarkupUtil.rectRoundOut(annotRectF, 0);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(annotRectF, annotRectF, _pageIndex);
                this.mPdfViewCtrl.refresh(_pageIndex, rect);
                if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                    this.mLastAnnot = annot;
                }
            } else {
                this.mLastAnnot = annot;
            }
            this.mAnnotMenu.show(annotRectF);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean needInvalid) {
        try {
            this.mAnnotMenu.dismiss();
            if (this.mIsEditProperty) {
                this.mIsEditProperty = false;
            }
            if (this.mIsAnnotModified && needInvalid) {
                if (!(this.mTmpUndoColor == this.mModifyAnnotColor && this.mTmpUndoOpacity == this.mModifyOpacity)) {
                    ModifyAnnot(annot, this.mModifyColor, this.mModifyOpacity, null, true, null);
                }
            } else if (this.mIsAnnotModified) {
                annot.setBorderColor((long) this.mTmpUndoColor);
                ((Markup) annot).setOpacity((float) this.mTmpUndoOpacity);
                annot.resetAppearanceStream();
            }
            if (needInvalid) {
                this.mIsAnnotModified = false;
                int _pageIndex = annot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                    RectF rectF = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(annot.getRect(), rectF, _pageIndex);
                    this.mPdfViewCtrl.refresh(_pageIndex, TextMarkupUtil.rectRoundOut(rectF, 0));
                    return;
                }
                return;
            }
            this.mLastAnnot = null;
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    private void ModifyAnnot(Annot annot, int color, int opacity, DateTime modifyDate, boolean addUndo, Callback callback) {
        PDFException e;
        try {
            final PDFPage page = annot.getPage();
            if (page != null) {
                if (modifyDate == null) {
                    DateTime modifyDate2 = new DateTime();
                    try {
                        annot.setBorderColor((long) this.mModifyAnnotColor);
                        modifyDate = modifyDate2;
                    } catch (PDFException e2) {
                        e = e2;
                        modifyDate = modifyDate2;
                        if (e.getLastError() == PDFError.OOM.getCode()) {
                            this.mPdfViewCtrl.recoverForOOM();
                        }
                    }
                }
                annot.setBorderColor((long) color);
                ((Markup) annot).setOpacity(((float) opacity) / 255.0f);
                final int _pageIndex = page.getIndex();
                final UnderlineModifyUndoItem undoItem = new UnderlineModifyUndoItem(this.mPdfViewCtrl);
                undoItem.setCurrentValue(annot);
                undoItem.mPageIndex = _pageIndex;
                undoItem.mColor = (long) color;
                undoItem.mOpacity = ((float) opacity) / 255.0f;
                undoItem.mModifiedDate = modifyDate;
                undoItem.mRedoColor = color;
                undoItem.mRedoOpacity = ((float) opacity) / 255.0f;
                undoItem.mRedoContent = annot.getContent();
                undoItem.mUndoColor = this.mTmpUndoColor;
                undoItem.mUndoOpacity = ((float) this.mTmpUndoOpacity) / 255.0f;
                undoItem.mUndoContent = this.mTmpUndoContents;
                undoItem.mPaintBbox = this.mPaintBbox;
                final Annot annot2 = annot;
                final boolean z = addUndo;
                final Callback callback2 = callback;
                this.mPdfViewCtrl.addTask(new EditAnnotTask(new UnderlineEvent(2, undoItem, (Underline) annot, this.mPdfViewCtrl), new Callback() {
                    public void result(Event event, boolean success) {
                        if (success) {
                            DocumentManager.getInstance(UnderlineAnnotHandler.this.mPdfViewCtrl).onAnnotModified(page, annot2);
                            if (z) {
                                DocumentManager.getInstance(UnderlineAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                            }
                            if (UnderlineAnnotHandler.this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                                try {
                                    RectF annotRectF = annot2.getRect();
                                    UnderlineAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, _pageIndex);
                                    UnderlineAnnotHandler.this.mPdfViewCtrl.refresh(_pageIndex, AppDmUtil.rectFToRect(annotRectF));
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
            }
        } catch (PDFException e3) {
            e = e3;
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    private void DeleteAnnot(Annot annot, boolean addUndo, Callback result) {
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
        try {
            final RectF annotRectF = annot.getRect();
            PDFPage page = annot.getPage();
            final int _pageIndex = page.getIndex();
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final UnderlineDeleteUndoItem undoItem = new UnderlineDeleteUndoItem(this.mPdfViewCtrl);
            undoItem.setCurrentValue(annot);
            undoItem.mPageIndex = _pageIndex;
            int count = ((Underline) annot).getQuadPointsCount();
            undoItem.mQuadPoints = new QuadPoints[count];
            for (int i = 0; i < count; i++) {
                undoItem.mQuadPoints[i] = ((Underline) annot).getQuadPoints(i);
            }
            final boolean z = addUndo;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new UnderlineEvent(3, undoItem, (Underline) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (z) {
                            DocumentManager.getInstance(UnderlineAnnotHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        }
                        RectF deviceRectF = new RectF();
                        if (UnderlineAnnotHandler.this.mPdfViewCtrl.isPageVisible(_pageIndex)) {
                            UnderlineAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, _pageIndex);
                            UnderlineAnnotHandler.this.mPdfViewCtrl.refresh(_pageIndex, AppDmUtil.rectFToRect(deviceRectF));
                        }
                    }
                    if (callback != null) {
                        callback.result(event, success);
                    }
                }
            }));
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    public void addAnnot(int pageIndex, AnnotContent contentSupplier, boolean addUndo, Callback result) {
        if (this.mUnderlineToolHandler == null) {
            return;
        }
        if (contentSupplier instanceof TextMarkupContent) {
            this.mUnderlineToolHandler.AddAnnot(pageIndex, addUndo, contentSupplier, null, contentSupplier.getBBox(), null, result);
            return;
        }
        TextMarkupContentAbs tmSelector = (TextMarkupContentAbs) TextMarkupContentAbs.class.cast(contentSupplier);
        SelectInfo info = this.mUnderlineToolHandler.mSelectInfo;
        info.clear();
        info.mIsFromTS = true;
        info.mStartChar = tmSelector.getTextSelector().getStart();
        info.mEndChar = tmSelector.getTextSelector().getEnd();
        this.mUnderlineToolHandler.setFromSelector(true);
        this.mUnderlineToolHandler.SelectCountRect(contentSupplier.getPageIndex(), info);
        this.mUnderlineToolHandler.OnSelectRelease(contentSupplier.getPageIndex(), info, result);
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        DeleteAnnot(annot, addUndo, result);
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && (annot instanceof Markup) && this.mPdfViewCtrl.isPageVisible(pageIndex)) {
            try {
                if (pageIndex == annot.getPage().getIndex() && this.mLastAnnot == annot) {
                    RectF rectF = annot.getRect();
                    RectF deviceRt = new RectF();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, deviceRt, pageIndex);
                    Rect rectBBox = TextMarkupUtil.rectRoundOut(deviceRt, this.mBBoxSpace);
                    canvas.save();
                    canvas.drawRect(rectBBox, this.mPaintBbox);
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

    public void onDrawForControls(Canvas canvas) {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null && (curAnnot instanceof Markup) && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                int annotPageIndex = curAnnot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(annotPageIndex)) {
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(curAnnot.getRect(), this.mDrawLocal_tmpF, annotPageIndex);
                    RectF canvasRt = new RectF();
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mDrawLocal_tmpF, canvasRt, annotPageIndex);
                    if (this.mIsEditProperty) {
                        ((PropertyBarImpl) this.mAnnotPropertyBar).onConfigurationChanged(canvasRt);
                    }
                    this.mAnnotMenu.update(canvasRt);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void modifyAnnotColor(int color) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                this.mModifyColor = ViewCompat.MEASURED_SIZE_MASK & color;
                this.mModifyOpacity = (int) (((Markup) annot).getOpacity() * 255.0f);
                this.mModifyAnnotColor = this.mModifyColor;
                if (annot.getBorderColor() != ((long) this.mModifyAnnotColor)) {
                    this.mIsAnnotModified = true;
                    annot.setBorderColor((long) this.mModifyAnnotColor);
                    ((Markup) annot).setOpacity(((float) this.mModifyOpacity) / 255.0f);
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

    private void invalidateForToolModify(Annot annot) {
        try {
            int pageIndex = annot.getPage().getIndex();
            if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                RectF rectF = annot.getRect();
                RectF pvRect = new RectF();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, pvRect, pageIndex);
                Rect rect = TextMarkupUtil.rectRoundOut(pvRect, this.mBBoxSpace);
                rect.inset(-1, -1);
                this.mPdfViewCtrl.refresh(pageIndex, rect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void modifyAnnotOpacity(int opacity) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                this.mModifyColor = ((int) annot.getBorderColor()) & ViewCompat.MEASURED_SIZE_MASK;
                this.mModifyOpacity = opacity;
                this.mModifyAnnotColor = this.mModifyColor;
                if (((Markup) annot).getOpacity() * 255.0f != ((float) this.mModifyOpacity)) {
                    this.mIsAnnotModified = true;
                    annot.setBorderColor((long) this.mModifyAnnotColor);
                    ((Markup) annot).setOpacity(((float) this.mModifyOpacity) / 255.0f);
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

    public void resetMenuItems() {
        this.mMenuItems.clear();
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canCopy()) {
            this.mMenuItems.add(Integer.valueOf(1));
        }
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mMenuItems.add(Integer.valueOf(6));
            this.mMenuItems.add(Integer.valueOf(3));
            this.mMenuItems.add(Integer.valueOf(4));
            this.mMenuItems.add(Integer.valueOf(2));
            return;
        }
        this.mMenuItems.add(Integer.valueOf(3));
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        if (content != null) {
            try {
                this.mTmpUndoColor = (int) annot.getBorderColor();
                this.mTmpUndoOpacity = (int) (((Markup) annot).getOpacity() * 255.0f);
                this.mTmpUndoContents = annot.getContent();
                try {
                    if (content.getContents() != null) {
                        annot.setContent(content.getContents());
                    } else {
                        annot.setContent("");
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                if (this.mLastAnnot == annot) {
                    this.mPaintBbox.setColor(content.getColor());
                }
                ModifyAnnot(annot, content.getColor(), content.getOpacity(), content.getModifiedDate(), addUndo, result);
            } catch (PDFException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void removeProbarListener() {
        this.mPropertyChangeListener = null;
    }
}
