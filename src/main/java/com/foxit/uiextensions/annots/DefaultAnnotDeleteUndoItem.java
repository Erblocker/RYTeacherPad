package com.foxit.uiextensions.annots;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.DocumentManager;

/* compiled from: DefaultAnnotHandler */
class DefaultAnnotDeleteUndoItem extends DefaultAnnotUndoItem {
    public DefaultAnnotDeleteUndoItem(DefaultAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        try {
            Annot annot = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(this.mType, this.mBBox);
            DefaultAnnotAddUndoItem undoItem = new DefaultAnnotAddUndoItem(this.mAnnotHandler, this.mPdfViewCtrl);
            undoItem.mNM = this.mNM;
            undoItem.mPageIndex = this.mPageIndex;
            undoItem.mAuthor = this.mAuthor;
            undoItem.mFlags = this.mFlags;
            undoItem.mSubject = this.mSubject;
            undoItem.mCreationDate = this.mCreationDate;
            undoItem.mModifiedDate = this.mModifiedDate;
            undoItem.mColor = this.mColor;
            undoItem.mOpacity = this.mOpacity;
            undoItem.mLineWidth = this.mLineWidth;
            undoItem.mIntent = this.mIntent;
            undoItem.mContents = this.mContents;
            this.mAnnotHandler.addAnnot(this.mPageIndex, annot, undoItem, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean redo() {
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || annot.getType() != this.mType) {
                return false;
            }
            this.mAnnotHandler.removeAnnot(annot, this, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
