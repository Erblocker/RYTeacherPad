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
class CircleAddUndoItem extends CircleUndoItem {
    public CircleAddUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        CircleDeleteUndoItem undoItem = new CircleDeleteUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Circle)) {
                return false;
            }
            final RectF annotRectF = annot.getRect();
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(3, undoItem, (Circle) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && CircleAddUndoItem.this.mPdfViewCtrl.isPageVisible(CircleAddUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        CircleAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, CircleAddUndoItem.this.mPageIndex);
                        CircleAddUndoItem.this.mPdfViewCtrl.refresh(CircleAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
                    }
                }
            }));
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean redo() {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = page.addAnnot(6, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(1, this, (Circle) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(CircleAddUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (CircleAddUndoItem.this.mPdfViewCtrl.isPageVisible(CircleAddUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                CircleAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, CircleAddUndoItem.this.mPageIndex);
                                CircleAddUndoItem.this.mPdfViewCtrl.refresh(CircleAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
