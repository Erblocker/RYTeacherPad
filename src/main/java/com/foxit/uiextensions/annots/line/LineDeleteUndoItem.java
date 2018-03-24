package com.foxit.uiextensions.annots.line;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.uiextensions.DocumentManager;

/* compiled from: LineUndoItem */
class LineDeleteUndoItem extends LineUndoItem {
    public LineDeleteUndoItem(LineRealAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        try {
            Line annot = (Line) this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(4, this.mBBox);
            LineAddUndoItem undoItem = new LineAddUndoItem(this.mAnnotHandler, this.mPdfViewCtrl);
            undoItem.mNM = this.mNM;
            undoItem.mPageIndex = this.mPageIndex;
            undoItem.mStartPt.set(this.mStartPt);
            undoItem.mEndPt.set(this.mEndPt);
            undoItem.mStartingStyle = this.mStartingStyle;
            undoItem.mEndingStyle = this.mEndingStyle;
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
            if (annot == null || !(annot instanceof Line)) {
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
