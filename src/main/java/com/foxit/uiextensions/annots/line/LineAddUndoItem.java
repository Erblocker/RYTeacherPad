package com.foxit.uiextensions.annots.line;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.uiextensions.DocumentManager;

/* compiled from: LineUndoItem */
class LineAddUndoItem extends LineUndoItem {
    public LineAddUndoItem(LineRealAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        LineDeleteUndoItem undoItem = new LineDeleteUndoItem(this.mAnnotHandler, this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mStartPt.set(this.mStartPt);
        undoItem.mEndPt.set(this.mEndPt);
        undoItem.mStartingStyle = this.mStartingStyle;
        undoItem.mEndingStyle = this.mEndingStyle;
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || !(annot instanceof Line)) {
                return false;
            }
            this.mAnnotHandler.removeAnnot(annot, undoItem, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean redo() {
        try {
            this.mAnnotHandler.addAnnot(this.mPageIndex, (Line) this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(4, this.mBBox), this, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
