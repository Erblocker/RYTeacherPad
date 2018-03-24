package com.foxit.uiextensions.annots.freetext;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;

public class FtUtil {
    public static float widthOnPageView(PDFViewCtrl pdfViewCtrl, int pageIndex, float width) {
        RectF rectF = new RectF(0.0f, 0.0f, width, width);
        pdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
        return Math.abs(rectF.width());
    }
}
