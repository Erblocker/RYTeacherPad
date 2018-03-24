package com.foxit.uiextensions.annots;

import com.foxit.sdk.PDFViewCtrl;

/* compiled from: DefaultAnnotHandler */
abstract class DefaultAnnotUndoItem extends AnnotUndoItem {
    DefaultAnnotHandler mAnnotHandler;

    public DefaultAnnotUndoItem(DefaultAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        this.mAnnotHandler = annotHandler;
        this.mPdfViewCtrl = pdfViewCtrl;
    }
}
