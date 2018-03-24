package com.foxit.uiextensions.annots.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.DefaultAnnotHandler;
import com.foxit.uiextensions.annots.common.UIAnnotFrame;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;

/* compiled from: LineAnnotHandler */
class LineDefaultAnnotHandler extends DefaultAnnotHandler {
    public LineDefaultAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        super(context, parent, pdfViewCtrl);
    }

    public void setAnnotMenu(AnnotMenu annotMenu) {
        this.mAnnotMenu = annotMenu;
    }

    public AnnotMenu getAnnotMenu() {
        return this.mAnnotMenu;
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mPropertyBar = propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }

    public void onDrawForControls(Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (annot.getType() == 4) {
                    int pageIndex = annot.getPage().getIndex();
                    if (!this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        return;
                    }
                    if (this.mOp != 1 || this.mCtl != -1) {
                        RectF bbox = UIAnnotFrame.mapBounds(this.mPdfViewCtrl, pageIndex, annot, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                        this.mAnnotMenu.update(bbox);
                        if (this.mPropertyBar.isShowing()) {
                            this.mPropertyBar.update(bbox);
                        }
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }
}
