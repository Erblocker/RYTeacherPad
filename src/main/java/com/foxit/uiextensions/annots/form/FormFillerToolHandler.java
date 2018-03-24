package com.foxit.uiextensions.annots.form;

import android.graphics.Canvas;
import android.view.MotionEvent;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.annots.AnnotActionHandler;

public class FormFillerToolHandler implements ToolHandler {
    private AnnotActionHandler mActionHandler;
    private PDFViewCtrl mPdfViewCtrl;

    public FormFillerToolHandler(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
        initActionHandler();
    }

    protected void initActionHandler() {
        this.mActionHandler = (AnnotActionHandler) DocumentManager.getInstance(this.mPdfViewCtrl).getActionHandler();
        if (this.mActionHandler == null) {
            this.mActionHandler = new AnnotActionHandler(this.mPdfViewCtrl);
        }
        DocumentManager.getInstance(this.mPdfViewCtrl).setActionHandler(this.mActionHandler);
    }

    public String getType() {
        return ToolHandler.TH_TYPE_FORMFILLER;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
    }
}
