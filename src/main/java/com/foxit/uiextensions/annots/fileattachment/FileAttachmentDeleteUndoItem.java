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
class FileAttachmentDeleteUndoItem extends FileAttachmentUndoItem {
    public FileAttachmentDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        FileAttachmentAddUndoItem undoItem = new FileAttachmentAddUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mNM = this.mNM;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mFlags = this.mFlags;
        undoItem.mSubject = this.mSubject;
        undoItem.mIconName = this.mIconName;
        undoItem.mCreationDate = this.mCreationDate;
        undoItem.mModifiedDate = this.mModifiedDate;
        undoItem.mBBox = new RectF(this.mBBox);
        undoItem.mContents = this.mContents;
        undoItem.mColor = this.mColor;
        undoItem.mOpacity = this.mOpacity;
        undoItem.mPath = this.mPath;
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final FileAttachment annot = (FileAttachment) page.addAnnot(17, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(FileAttachmentDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (FileAttachmentDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(FileAttachmentDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = annot.getRect();
                                FileAttachmentDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, FileAttachmentDeleteUndoItem.this.mPageIndex);
                                FileAttachmentDeleteUndoItem.this.mPdfViewCtrl.refresh(FileAttachmentDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
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

    public boolean redo() {
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof FileAttachment)) {
                return false;
            }
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(3, this, (FileAttachment) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && FileAttachmentDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(FileAttachmentDeleteUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        FileAttachmentDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, FileAttachmentDeleteUndoItem.this.mPageIndex);
                        FileAttachmentDeleteUndoItem.this.mPdfViewCtrl.refresh(FileAttachmentDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
