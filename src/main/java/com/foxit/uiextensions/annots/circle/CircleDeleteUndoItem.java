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
class CircleDeleteUndoItem extends CircleUndoItem {
    public CircleDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        CircleAddUndoItem undoItem = new CircleAddUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mNM = this.mNM;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mFlags = this.mFlags;
        undoItem.mSubject = this.mSubject;
        undoItem.mCreationDate = this.mCreationDate;
        undoItem.mModifiedDate = this.mModifiedDate;
        undoItem.mBBox = new RectF(this.mBBox);
        undoItem.mColor = this.mColor;
        undoItem.mOpacity = this.mOpacity;
        undoItem.mLineWidth = this.mLineWidth;
        undoItem.mBorderStyle = this.mBorderStyle;
        undoItem.mContents = this.mContents;
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Circle annot = (Circle) page.addAnnot(6, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(CircleDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (CircleDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(CircleDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = annot.getRect();
                                CircleDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, CircleDeleteUndoItem.this.mPageIndex);
                                CircleDeleteUndoItem.this.mPdfViewCtrl.refresh(CircleDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
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
            if (annot == null || !(annot instanceof Circle)) {
                return false;
            }
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CircleEvent(3, this, (Circle) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && CircleDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(CircleDeleteUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        CircleDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, CircleDeleteUndoItem.this.mPageIndex);
                        CircleDeleteUndoItem.this.mPdfViewCtrl.refresh(CircleDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
