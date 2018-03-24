package com.foxit.uiextensions.annots.fileattachment;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FileAttachment;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: FileAttachmentUndoItem */
class FileAttachmentAddUndoItem extends FileAttachmentUndoItem {
    public FileAttachmentAddUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean redo() {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = page.addAnnot(17, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(1, this, (FileAttachment) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(FileAttachmentAddUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (FileAttachmentAddUndoItem.this.mPdfViewCtrl.isPageVisible(FileAttachmentAddUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                FileAttachmentAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, FileAttachmentAddUndoItem.this.mPageIndex);
                                FileAttachmentAddUndoItem.this.mPdfViewCtrl.refresh(FileAttachmentAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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

    public boolean undo() {
        FileAttachmentDeleteUndoItem undoItem = new FileAttachmentDeleteUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof FileAttachment)) {
                return false;
            }
            final RectF annotRectF = annot.getRect();
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(3, undoItem, (FileAttachment) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && FileAttachmentAddUndoItem.this.mPdfViewCtrl.isPageVisible(FileAttachmentAddUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        FileAttachmentAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, FileAttachmentAddUndoItem.this.mPageIndex);
                        FileAttachmentAddUndoItem.this.mPdfViewCtrl.refresh(FileAttachmentAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
