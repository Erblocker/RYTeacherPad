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
class StampDeleteUndoItem extends StampUndoItem {
    public StampDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        StampAddUndoItem undoItem = new StampAddUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mStampType = this.mStampType;
        undoItem.mDsip = this.mDsip;
        undoItem.mNM = this.mNM;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mFlags = this.mFlags;
        undoItem.mSubject = this.mSubject;
        undoItem.mIconName = this.mIconName;
        undoItem.mCreationDate = this.mCreationDate;
        undoItem.mModifiedDate = this.mModifiedDate;
        undoItem.mBBox = new RectF(this.mBBox);
        undoItem.mBitmap = this.mBitmap;
        undoItem.mContents = this.mContents;
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Stamp annot = (Stamp) page.addAnnot(13, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(StampDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (StampDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(StampDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = annot.getRect();
                                StampDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, StampDeleteUndoItem.this.mPageIndex);
                                StampDeleteUndoItem.this.mPdfViewCtrl.refresh(StampDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
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
            if (annot == null || !(annot instanceof Stamp)) {
                return false;
            }
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(3, this, (Stamp) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && StampDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(StampDeleteUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        StampDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, StampDeleteUndoItem.this.mPageIndex);
                        StampDeleteUndoItem.this.mPdfViewCtrl.refresh(StampDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
