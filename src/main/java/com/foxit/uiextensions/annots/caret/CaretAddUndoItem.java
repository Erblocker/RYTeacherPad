package com.foxit.uiextensions.annots.caret;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Caret;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.textmarkup.TextSelector;
import com.foxit.uiextensions.utils.ToolUtil;

/* compiled from: CaretUndoItem */
class CaretAddUndoItem extends CaretUndoItem {
    TextSelector mTextSelector;

    public CaretAddUndoItem(PDFViewCtrl pdfViewCtrl) {
        super(pdfViewCtrl);
    }

    public boolean undo() {
        CaretDeleteUndoItem undoItem = new CaretDeleteUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || !(annot instanceof Caret)) {
                return false;
            }
            CaretAnnotHandler annotHandler = (CaretAnnotHandler) ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), 14);
            if (annotHandler == null) {
                return false;
            }
            return annotHandler.deleteAnnot(annot, undoItem, false, null);
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean redo() {
        try {
            Annot annot = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(14, this.mBBox);
            CaretAnnotHandler annotHandler = (CaretAnnotHandler) ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), 14);
            if (annotHandler == null) {
                return false;
            }
            CaretToolHandler toolHandler = (CaretToolHandler) annotHandler.getToolHandler(this.mIntent);
            if (toolHandler == null) {
                return false;
            }
            toolHandler.addAnnot(annot, this, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
