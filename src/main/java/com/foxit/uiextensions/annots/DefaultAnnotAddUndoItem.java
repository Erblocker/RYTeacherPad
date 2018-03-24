package com.foxit.uiextensions.annots;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.DocumentManager;

/* compiled from: DefaultAnnotHandler */
class DefaultAnnotAddUndoItem extends DefaultAnnotUndoItem {
    public DefaultAnnotAddUndoItem(DefaultAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        DefaultAnnotDeleteUndoItem undoItem = new DefaultAnnotDeleteUndoItem(this.mAnnotHandler, this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || annot.getType() != this.mType) {
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
            this.mAnnotHandler.addAnnot(this.mPageIndex, this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(this.mType, this.mBBox), this, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
