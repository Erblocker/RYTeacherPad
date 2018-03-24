package com.foxit.uiextensions.annots.form;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.form.FormControl;
import com.foxit.sdk.pdf.form.FormFillerAssist;
import com.foxit.uiextensions.utils.AppDmUtil;

public class FormFillerAssistImpl extends FormFillerAssist {
    protected boolean bWillClose = false;
    protected RectF invalidateRect = new RectF();
    protected boolean isAllowInput = false;
    protected boolean isScaling = false;
    private PDFViewCtrl mPDFViewCtrl;

    public FormFillerAssistImpl(PDFViewCtrl pdfViewCtrl) {
        this.mPDFViewCtrl = pdfViewCtrl;
    }

    public void setScaling(boolean scaling) {
        this.isScaling = scaling;
    }

    public void focusGotOnControl(FormControl control, String filedValue) {
        this.isAllowInput = true;
    }

    public void focusLostFromControl(FormControl control, String filedValue) {
        this.isAllowInput = false;
    }

    public void refresh(PDFPage page, RectF pdfRect) {
        try {
            if (!this.bWillClose && !this.isScaling) {
                int pageIndex = page.getIndex();
                if (pageIndex == this.mPDFViewCtrl.getCurrentPage()) {
                    RectF viewRect = new RectF(0.0f, 0.0f, (float) this.mPDFViewCtrl.getDisplayViewWidth(), (float) this.mPDFViewCtrl.getDisplayViewHeight());
                    this.mPDFViewCtrl.convertPdfRectToPageViewRect(pdfRect, pdfRect, pageIndex);
                    RectF rect = new RectF(pdfRect);
                    this.mPDFViewCtrl.convertPageViewRectToDisplayViewRect(pdfRect, pdfRect, pageIndex);
                    this.invalidateRect.union(rect);
                    if (viewRect.intersect(pdfRect) && System.currentTimeMillis() - FormFillerAnnotHandler.mLastInputInvalidateTime > 500 && FormFillerAnnotHandler.mIsNeedRefresh) {
                        rect.inset(-5.0f, -5.0f);
                        this.mPDFViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(rect));
                    }
                }
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public RectF getInvalidateRect() {
        return this.invalidateRect;
    }

    public void resetInvalidateRect() {
        this.invalidateRect.set(0.0f, 0.0f, 0.0f, 0.0f);
    }
}
