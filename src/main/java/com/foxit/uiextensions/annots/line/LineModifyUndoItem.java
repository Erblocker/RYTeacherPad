package com.foxit.uiextensions.annots.line;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.uiextensions.DocumentManager;

/* compiled from: LineUndoItem */
class LineModifyUndoItem extends LineUndoItem {
    public LineModifyUndoItem(LineRealAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        return modifyAnnot(true);
    }

    public boolean redo() {
        return modifyAnnot(false);
    }

    private boolean modifyAnnot(boolean userOldValue) {
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || !(annot instanceof Line)) {
                return false;
            }
            this.mAnnotHandler.modifyAnnot((Line) annot, this, userOldValue, false, true, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
