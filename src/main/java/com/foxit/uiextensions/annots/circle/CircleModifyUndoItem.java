package com.foxit.uiextensions.annots.circle;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Circle;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: CircleUndoItem */
class CircleModifyUndoItem extends CircleUndoItem {
    public RectF mRedoBbox;
    public int mRedoColor;
    public String mRedoContent;
    public float mRedoLineWidth;
    public float mRedoOpacity;
    public RectF mUndoBbox;
    public int mUndoColor;
    public String mUndoContent;
    public float mUndoLineWidth;
    public float mUndoOpacity;

    public CircleModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        return modifyAnnot(this.mUndoColor, this.mUndoOpacity, this.mUndoLineWidth, this.mUndoBbox, this.mUndoContent);
    }

    public boolean redo() {
        return modifyAnnot(this.mRedoColor, this.mRedoOpacity, this.mRedoLineWidth, this.mRedoBbox, this.mRedoContent);
    }

    private boolean modifyAnnot(int color, float opacity, float lineWidth, RectF bbox, String content) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Circle)) {
                return false;
            }
            final RectF oldBbox = annot.getRect();
            this.mColor = (long) color;
            this.mOpacity = opacity;
            this.mBBox = new RectF(bbox);
            this.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            this.mLineWidth = lineWidth;
            this.mContents = content;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(2, this, (Circle) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(CircleModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(CircleModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(CircleModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (CircleModifyUndoItem.this.mPdfViewCtrl.isPageVisible(CircleModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                CircleModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, CircleModifyUndoItem.this.mPageIndex);
                                CircleModifyUndoItem.this.mPdfViewCtrl.refresh(CircleModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
                                CircleModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(oldBbox, oldBbox, CircleModifyUndoItem.this.mPageIndex);
                                CircleModifyUndoItem.this.mPdfViewCtrl.refresh(CircleModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(oldBbox));
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }));
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
