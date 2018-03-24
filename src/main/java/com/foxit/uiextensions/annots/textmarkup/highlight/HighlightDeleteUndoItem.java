package com.foxit.uiextensions.annots.textmarkup.highlight;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Highlight;
import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: HighlightUndoItem */
class HighlightDeleteUndoItem extends HighlightUndoItem {
    public HighlightDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        HighlightAddUndoItem undoItem = new HighlightAddUndoItem(this.mPdfViewCtrl);
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
        undoItem.quadPointsArray = new QuadPoints[this.quadPointsArray.length];
        System.arraycopy(this.quadPointsArray, 0, undoItem.quadPointsArray, 0, this.quadPointsArray.length);
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Highlight highlight = (Highlight) page.addAnnot(9, new RectF(0.0f, 0.0f, 0.0f, 0.0f));
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new HighlightEvent(1, undoItem, highlight, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(HighlightDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, highlight);
                        if (HighlightDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(HighlightDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = highlight.getRect();
                                HighlightDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, HighlightDeleteUndoItem.this.mPageIndex);
                                HighlightDeleteUndoItem.this.mPdfViewCtrl.refresh(HighlightDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
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
            if (annot == null || !(annot instanceof Highlight)) {
                return false;
            }
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new HighlightEvent(3, this, (Highlight) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && HighlightDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(HighlightDeleteUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        HighlightDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, HighlightDeleteUndoItem.this.mPageIndex);
                        HighlightDeleteUndoItem.this.mPdfViewCtrl.refresh(HighlightDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
