package com.foxit.uiextensions.annots.ink;

import android.graphics.PointF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFPath;
import com.foxit.uiextensions.annots.AnnotUndoItem;
import java.util.ArrayList;

public abstract class InkUndoItem extends AnnotUndoItem {
    InkAnnotHandler mAnnotHandler;
    ArrayList<ArrayList<PointF>> mInkLists;
    ArrayList<ArrayList<PointF>> mOldInkLists;
    PDFPath mOldPath;
    PDFPath mPath;

    public InkUndoItem(InkAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        this.mAnnotHandler = annotHandler;
        this.mPdfViewCtrl = pdfViewCtrl;
    }
}
