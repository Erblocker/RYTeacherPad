package com.foxit.uiextensions.annots;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.uiextensions.DocumentManager;

/* compiled from: DefaultAnnotHandler */
class DefaultAnnotModifyUndoItem extends DefaultAnnotUndoItem {
    public DefaultAnnotModifyUndoItem(DefaultAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
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
            if (annot == null) {
                return false;
            }
            if (annot.getType() != this.mType) {
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
