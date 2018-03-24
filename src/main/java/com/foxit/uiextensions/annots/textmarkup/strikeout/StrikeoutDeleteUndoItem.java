package com.foxit.uiextensions.annots.textmarkup.strikeout;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.sdk.pdf.annots.StrikeOut;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: StrikeoutUndoItem */
class StrikeoutDeleteUndoItem extends StrikeoutUndoItem {
    public StrikeoutDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        StrikeoutAddUndoItem undoItem = new StrikeoutAddUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mBBox = new RectF(this.mBBox);
        undoItem.mColor = this.mColor;
        undoItem.mContents = this.mContents;
        undoItem.mModifiedDate = this.mModifiedDate;
        undoItem.mOpacity = this.mOpacity;
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mType = this.mType;
        undoItem.mFlags = this.mFlags;
        undoItem.mQuadPoints = new QuadPoints[this.mQuadPoints.length];
        System.arraycopy(this.mQuadPoints, 0, undoItem.mQuadPoints, 0, this.mQuadPoints.length);
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final StrikeOut strikeOut = (StrikeOut) page.addAnnot(12, new RectF(0.0f, 0.0f, 0.0f, 0.0f));
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StrikeoutEvent(1, undoItem, strikeOut, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(StrikeoutDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, strikeOut);
                        if (StrikeoutDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(StrikeoutDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = strikeOut.getRect();
                                StrikeoutDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, StrikeoutDeleteUndoItem.this.mPageIndex);
                                StrikeoutDeleteUndoItem.this.mPdfViewCtrl.refresh(StrikeoutDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }));
            return true;
        } catch (PDFException e) {
            return false;
        }
    }

    public boolean redo() {
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof StrikeOut)) {
                return false;
            }
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StrikeoutEvent(3, this, (StrikeOut) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && StrikeoutDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(StrikeoutDeleteUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        StrikeoutDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, StrikeoutDeleteUndoItem.this.mPageIndex);
                        StrikeoutDeleteUndoItem.this.mPdfViewCtrl.refresh(StrikeoutDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
