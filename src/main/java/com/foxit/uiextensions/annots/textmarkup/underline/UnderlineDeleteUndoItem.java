package com.foxit.uiextensions.annots.textmarkup.underline;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.QuadPoints;
import com.foxit.sdk.pdf.annots.Underline;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: UnderlineUndoItem */
class UnderlineDeleteUndoItem extends UnderlineUndoItem {
    public UnderlineDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        UnderlineAddUndoItem undoItem = new UnderlineAddUndoItem(this.mPdfViewCtrl);
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
            final Underline underline = (Underline) page.addAnnot(10, new RectF(0.0f, 0.0f, 0.0f, 0.0f));
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new UnderlineEvent(1, undoItem, underline, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(UnderlineDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, underline);
                        if (UnderlineDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(UnderlineDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = underline.getRect();
                                UnderlineDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, UnderlineDeleteUndoItem.this.mPageIndex);
                                UnderlineDeleteUndoItem.this.mPdfViewCtrl.refresh(UnderlineDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
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
            if (annot == null || !(annot instanceof Underline)) {
                return false;
            }
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new UnderlineEvent(3, this, (Underline) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && UnderlineDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(UnderlineDeleteUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        UnderlineDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, UnderlineDeleteUndoItem.this.mPageIndex);
                        UnderlineDeleteUndoItem.this.mPdfViewCtrl.refresh(UnderlineDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
