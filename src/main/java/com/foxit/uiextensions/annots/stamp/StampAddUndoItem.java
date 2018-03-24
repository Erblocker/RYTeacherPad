package com.foxit.uiextensions.annots.stamp;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Stamp;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: StampUndoItem */
class StampAddUndoItem extends StampUndoItem {
    public StampAddUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        StampDeleteUndoItem undoItem = new StampDeleteUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Stamp)) {
                return false;
            }
            final RectF annotRectF = annot.getRect();
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(3, undoItem, (Stamp) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && StampAddUndoItem.this.mPdfViewCtrl.isPageVisible(StampAddUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        StampAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, StampAddUndoItem.this.mPageIndex);
                        StampAddUndoItem.this.mPdfViewCtrl.refresh(StampAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
            final Annot annot = page.addAnnot(13, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(1, this, (Stamp) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(StampAddUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (StampAddUndoItem.this.mPdfViewCtrl.isPageVisible(StampAddUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                StampAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, StampAddUndoItem.this.mPageIndex);
                                StampAddUndoItem.this.mPdfViewCtrl.refresh(StampAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
