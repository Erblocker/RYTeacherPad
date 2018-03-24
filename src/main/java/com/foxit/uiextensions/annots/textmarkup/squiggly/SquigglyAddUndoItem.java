package com.foxit.uiextensions.annots.textmarkup.squiggly;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Squiggly;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: SquigglyUndoItem */
class SquigglyAddUndoItem extends SquigglyUndoItem {
    public SquigglyAddUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        SquigglyDeleteUndoItem undoItem = new SquigglyDeleteUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Squiggly)) {
                return false;
            }
            final RectF annotRectF = annot.getRect();
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new SquigglyEvent(3, undoItem, (Squiggly) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && SquigglyAddUndoItem.this.mPdfViewCtrl.isPageVisible(SquigglyAddUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        SquigglyAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, SquigglyAddUndoItem.this.mPageIndex);
                        SquigglyAddUndoItem.this.mPdfViewCtrl.refresh(SquigglyAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
            final Annot annot = page.addAnnot(11, new RectF(0.0f, 0.0f, 0.0f, 0.0f));
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new SquigglyEvent(1, this, (Squiggly) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(SquigglyAddUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (SquigglyAddUndoItem.this.mPdfViewCtrl.isPageVisible(SquigglyAddUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                SquigglyAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, SquigglyAddUndoItem.this.mPageIndex);
                                SquigglyAddUndoItem.this.mPdfViewCtrl.refresh(SquigglyAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
