package com.foxit.uiextensions.annots.freetext.typewriter;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.Font;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class TypewriterUndoItem extends AnnotUndoItem {
    long mDaFlags;
    Font mFont;
    float mFontSize;
    long mTextColor;

    public TypewriterUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }
}
