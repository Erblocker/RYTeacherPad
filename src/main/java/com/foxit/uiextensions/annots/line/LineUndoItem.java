package com.foxit.uiextensions.annots.line;

import android.graphics.PointF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.annots.AnnotUndoItem;

public abstract class LineUndoItem extends AnnotUndoItem {
    LineRealAnnotHandler mAnnotHandler;
    PointF mEndPt = new PointF();
    String mEndingStyle;
    PointF mOldEndPt = new PointF();
    String mOldEndingStyle;
    PointF mOldStartPt = new PointF();
    String mOldStartingStyle;
    PointF mStartPt = new PointF();
    String mStartingStyle;

    public LineUndoItem(LineRealAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        this.mAnnotHandler = annotHandler;
        this.mPdfViewCtrl = pdfViewCtrl;
    }
}
