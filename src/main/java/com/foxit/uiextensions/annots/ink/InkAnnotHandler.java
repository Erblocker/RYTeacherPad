package com.foxit.uiextensions.annots.ink;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Ink;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.AbstractAnnotHandler;
import com.foxit.uiextensions.annots.AbstractToolHandler;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.common.IAnnotTaskResult;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;

class InkAnnotHandler extends AbstractAnnotHandler {
    protected int mBackColor;
    protected float mBackOpacity;
    protected ArrayList<Integer> mMenuText;
    protected ArrayList<ArrayList<PointF>> mOldInkLists;
    protected String mSubject = "Pencil";
    protected InkToolHandler mToolHandler;
    protected InkAnnotUtil mUtil;

    public InkAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, InkToolHandler toolHandler, InkAnnotUtil util) {
        super(context, parent, pdfViewCtrl, 15);
        this.mToolHandler = toolHandler;
        this.mColor = this.mToolHandler.getColor();
        this.mOpacity = this.mToolHandler.getOpacity();
        this.mThickness = this.mToolHandler.getThickness();
        this.mUtil = util;
        this.mMenuText = new ArrayList();
        this.mPaint.setStrokeJoin(Join.ROUND);
        this.mPaint.setStrokeCap(Cap.ROUND);
    }

    protected AbstractToolHandler getToolHandler() {
        return this.mToolHandler;
    }

    public void onAnnotSelected(Annot annot, boolean reRender) {
        try {
            this.mColor = (int) annot.getBorderColor();
            this.mOpacity = AppDmUtil.opacity255To100((int) ((((Ink) annot).getOpacity() * 255.0f) + 0.5f));
            this.mThickness = annot.getBorderInfo().getWidth();
            this.mBackColor = this.mColor;
            this.mBackOpacity = ((Ink) annot).getOpacity();
            this.mOldInkLists = InkAnnotUtil.generateInkList(((Ink) annot).getInkList());
            super.onAnnotSelected(annot, reRender);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        if (this.mIsModified) {
            InkModifyUndoItem undoItem = new InkModifyUndoItem(this, this.mPdfViewCtrl);
            undoItem.setCurrentValue(this.mSelectedAnnot);
            try {
                undoItem.mPath = ((Ink) this.mSelectedAnnot).getInkList();
                undoItem.mInkLists = InkAnnotUtil.generateInkList(undoItem.mPath);
                undoItem.mOldColor = (long) this.mBackColor;
                undoItem.mOldOpacity = this.mBackOpacity;
                undoItem.mOldBBox = new RectF(this.mBackRect);
                undoItem.mOldLineWidth = this.mBackThickness;
                undoItem.mOldInkLists = InkAnnotUtil.cloneInkList(this.mOldInkLists);
                undoItem.mOldPath = PDFPath.create();
                for (int li = 0; li < this.mOldInkLists.size(); li++) {
                    ArrayList<PointF> line = (ArrayList) this.mOldInkLists.get(li);
                    for (int pi = 0; pi < line.size(); pi++) {
                        if (pi == 0) {
                            undoItem.mOldPath.moveTo((PointF) line.get(pi));
                        } else {
                            undoItem.mOldPath.lineTo((PointF) line.get(pi));
                        }
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
            modifyAnnot(this.mSelectedAnnot, undoItem, false, true, reRender, new Callback() {
                public void result(Event event, boolean success) {
                    if (InkAnnotHandler.this.mSelectedAnnot != DocumentManager.getInstance(InkAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                        InkAnnotHandler.this.resetStatus();
                    }
                }
            });
            dismissPopupMenu();
            hidePropertyBar();
            return;
        }
        super.onAnnotDeselected(annot, reRender);
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        try {
            InkAnnotContent inkAnnotContent = (InkAnnotContent) content;
            Ink annot = (Ink) this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(15, inkAnnotContent.getBBox());
            InkAddUndoItem undoItem = new InkAddUndoItem(this, this.mPdfViewCtrl);
            undoItem.setCurrentValue((AnnotContent) inkAnnotContent);
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            ArrayList<ArrayList<PointF>> lines = ((InkAnnotContent) content).getInkLisk();
            if (lines != null) {
                undoItem.mPath = PDFPath.create();
                for (int i = 0; i < lines.size(); i++) {
                    ArrayList<PointF> line = (ArrayList) lines.get(i);
                    for (int j = 0; j < line.size(); j++) {
                        if (j == 0) {
                            undoItem.mPath.moveTo((PointF) line.get(j));
                        } else {
                            undoItem.mPath.lineTo((PointF) line.get(j));
                        }
                    }
                }
            }
            undoItem.mInkLists = InkAnnotUtil.cloneInkList(lines);
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

    protected Annot addAnnot(int pageIndex, RectF bbox, int color, int opacity, float thickness, ArrayList<ArrayList<PointF>> lines, IAnnotTaskResult<PDFPage, Annot, Void> result) {
        try {
            Ink annot = (Ink) this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(15, bbox);
            InkAddUndoItem undoItem = new InkAddUndoItem(this, this.mPdfViewCtrl);
            undoItem.mPageIndex = pageIndex;
            undoItem.mNM = AppDmUtil.randomUUID(null);
            undoItem.mBBox = new RectF(bbox);
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mFlags = 4;
            undoItem.mSubject = this.mSubject;
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mColor = (long) color;
            undoItem.mOpacity = ((float) opacity) / 255.0f;
            undoItem.mLineWidth = thickness;
            undoItem.mPath = PDFPath.create();
            for (int li = 0; li < lines.size(); li++) {
                ArrayList<PointF> line = (ArrayList) lines.get(li);
                for (int pi = 0; pi < line.size(); pi++) {
                    if (pi == 0) {
                        undoItem.mPath.moveTo((PointF) line.get(pi));
                    } else {
                        undoItem.mPath.lineTo((PointF) line.get(pi));
                    }
                }
            }
            undoItem.mInkLists = InkAnnotUtil.cloneInkList(lines);
            addAnnot(pageIndex, annot, undoItem, true, result);
            return annot;
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void addAnnot(int pageIndex, Annot annot, InkAddUndoItem undoItem, boolean addUndo, IAnnotTaskResult<PDFPage, Annot, Void> result) {
        handleAddAnnot(pageIndex, annot, new InkEvent(1, undoItem, (Ink) annot, this.mPdfViewCtrl), addUndo, result);
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
                        DocumentManager.getInstance(InkAnnotHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot2);
                        if (z) {
                            DocumentManager.getInstance(InkAnnotHandler.this.mPdfViewCtrl).addUndoItem(editAnnotEvent.mUndoItem);
                        }
                        if (InkAnnotHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                            RectF pvRect = InkAnnotHandler.this.getBBox(InkAnnotHandler.this.mPdfViewCtrl, annot2);
                            Rect tv_rect1 = new Rect();
                            pvRect.roundOut(tv_rect1);
                            InkAnnotHandler.this.mPdfViewCtrl.refresh(i, tv_rect1);
                        }
                    }
                    if (iAnnotTaskResult != null) {
                        iAnnotTaskResult.onResult(success, page, annot2, null);
                    }
                }
            }));
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return annot;
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        try {
            InkModifyUndoItem undoItem = new InkModifyUndoItem(this, this.mPdfViewCtrl);
            undoItem.setOldValue(annot);
            undoItem.mOldPath = ((Ink) annot).getInkList();
            undoItem.mOldInkLists = InkAnnotUtil.generateInkList(undoItem.mOldPath);
            undoItem.setCurrentValue(content);
            if (content instanceof InkAnnotContent) {
                ArrayList<ArrayList<PointF>> lines = ((InkAnnotContent) content).getInkLisk();
                if (lines != null) {
                    undoItem.mPath = PDFPath.create();
                    for (int i = 0; i < lines.size(); i++) {
                        ArrayList<PointF> line = (ArrayList) lines.get(i);
                        for (int j = 0; j < line.size(); j++) {
                            if (j == 0) {
                                undoItem.mPath.moveTo((PointF) line.get(j));
                            } else {
                                undoItem.mPath.lineTo((PointF) line.get(j));
                            }
                        }
                    }
                }
                undoItem.mInkLists = InkAnnotUtil.cloneInkList(lines);
            }
            modifyAnnot(annot, undoItem, false, addUndo, true, result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected void modifyAnnot(Annot annot, InkUndoItem undoItem, boolean useOldValue, boolean addUndo, boolean reRender, final Callback result) {
        InkEvent event = new InkEvent(2, undoItem, (Ink) annot, this.mPdfViewCtrl);
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
        InkDeleteUndoItem undoItem = new InkDeleteUndoItem(this, this.mPdfViewCtrl);
        undoItem.setCurrentValue(annot);
        try {
            undoItem.mPath = ((Ink) annot).getInkList();
            undoItem.mInkLists = InkAnnotUtil.generateInkList(undoItem.mPath);
        } catch (PDFException e) {
            e.printStackTrace();
        }
        removeAnnot(annot, undoItem, addUndo, result);
    }

    protected void removeAnnot(Annot annot, InkDeleteUndoItem undoItem, boolean addUndo, final Callback result) {
        handleRemoveAnnot(annot, new InkEvent(3, undoItem, (Ink) annot, this.mPdfViewCtrl), addUndo, new IAnnotTaskResult<PDFPage, Void, Void>() {
            public void onResult(boolean success, PDFPage p1, Void p2, Void p3) {
                if (result != null) {
                    result.result(null, success);
                }
            }
        });
    }

    protected ArrayList<Path> generatePathData(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot) {
        return InkAnnotUtil.generatePathData(this.mPdfViewCtrl, pageIndex, (Ink) annot);
    }

    protected void transformAnnot(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot, Matrix matrix) {
        RectF bbox = getBBox(pdfViewCtrl, annot);
        matrix.mapRect(bbox);
        pdfViewCtrl.convertPageViewRectToPdfRect(bbox, bbox, pageIndex);
        try {
            annot.move(bbox);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected void resetStatus() {
        this.mBackRect = null;
        this.mBackThickness = 0.0f;
        this.mSelectedAnnot = null;
        this.mIsModified = false;
    }

    protected void showPopupMenu() {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null) {
            try {
                if (curAnnot.getType() == 15) {
                    reloadPopupMenuString();
                    this.mAnnotMenu.setMenuItems(this.mMenuText);
                    RectF bbox = curAnnot.getRect();
                    int pageIndex = curAnnot.getPage().getIndex();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                    this.mAnnotMenu.show(bbox);
                    this.mAnnotMenu.setListener(new ClickListener() {
                        public void onAMClick(int flag) {
                            if (InkAnnotHandler.this.mSelectedAnnot != null) {
                                if (flag == 3) {
                                    DocumentManager.getInstance(InkAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                    UIAnnotReply.showComments(InkAnnotHandler.this.mContext, InkAnnotHandler.this.mPdfViewCtrl, InkAnnotHandler.this.mParent, InkAnnotHandler.this.mSelectedAnnot);
                                } else if (flag == 4) {
                                    DocumentManager.getInstance(InkAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                    UIAnnotReply.replyToAnnot(InkAnnotHandler.this.mContext, InkAnnotHandler.this.mPdfViewCtrl, InkAnnotHandler.this.mParent, InkAnnotHandler.this.mSelectedAnnot);
                                } else if (flag == 2) {
                                    if (InkAnnotHandler.this.mSelectedAnnot == DocumentManager.getInstance(InkAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot()) {
                                        InkAnnotHandler.this.removeAnnot(InkAnnotHandler.this.mSelectedAnnot, true, null);
                                    }
                                } else if (flag == 6) {
                                    InkAnnotHandler.this.dismissPopupMenu();
                                    InkAnnotHandler.this.showPropertyBar(1);
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

    protected void showPropertyBar(long curProperty) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && (annot instanceof Ink)) {
            long properties = getSupportedProperties();
            this.mPropertyBar.setPropertyChangeListener(this);
            setPropertyBarProperties(this.mPropertyBar);
            this.mPropertyBar.reset(properties);
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

    public void setPaintProperty(PDFViewCtrl pdfViewCtrl, int pageIndex, Paint paint, Annot annot) {
        super.setPaintProperty(pdfViewCtrl, pageIndex, paint, annot);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStyle(Style.STROKE);
    }

    protected long getSupportedProperties() {
        return this.mUtil.getSupportedProperties();
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        int[] colors = new int[PropertyBar.PB_COLORS_PENCIL.length];
        System.arraycopy(PropertyBar.PB_COLORS_PENCIL, 0, colors, 0, colors.length);
        colors[0] = PropertyBar.PB_COLORS_PENCIL[0];
        propertyBar.setColors(colors);
        super.setPropertyBarProperties(propertyBar);
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
}
