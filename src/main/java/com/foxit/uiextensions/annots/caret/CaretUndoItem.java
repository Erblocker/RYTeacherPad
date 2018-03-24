package com.foxit.uiextensions.annots.caret;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class CaretUndoItem extends AnnotUndoItem {
    int mRotate;

    public CaretUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }
}
