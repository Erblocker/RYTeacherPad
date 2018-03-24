package com.foxit.sdk;

import android.graphics.Matrix;
import android.graphics.PointF;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.ReflowPage;

/* compiled from: Task */
class l extends Task {
    protected int a;
    protected int b;
    protected int c;
    protected int d;
    protected PointF e;
    protected PointF f;
    protected Matrix g;
    protected int h = 1;
    private PDFDoc i;

    protected l(d dVar, PDFDoc pDFDoc, int i, int i2, int i3, int i4, CallBack callBack) {
        super(callBack);
        this.mDocManager = dVar;
        this.i = pDFDoc;
        this.a = i;
        this.b = i2;
        this.c = i3;
        this.d = i4;
    }

    public String toString() {
        return "PageSizeTask";
    }

    protected void execute() {
        if (this.mStatus == 1) {
            this.mStatus = 2;
            try {
                PDFPage page = this.i.getPage(this.a);
                if (this.b != 3) {
                    this.e = new PointF(page.getWidth(), page.getHeight());
                    this.f = new PointF(page.getWidth(), page.getHeight());
                    this.g = new Matrix();
                } else {
                    this.e = new PointF(page.getWidth(), page.getHeight());
                    if (page.isParsed() || 2 == page.startParse(0, null, false)) {
                        PDFViewCtrl d = this.mDocManager.d();
                        ReflowPage create = ReflowPage.create(page);
                        float f = d.getViewStatus().n;
                        float displayViewHeight = (float) (d.getDisplayViewHeight() - 40);
                        float displayViewWidth = (float) (d.getDisplayViewWidth() - 20);
                        float max = Math.max(0.25f, Math.min(Math.min(displayViewHeight, displayViewWidth) / page.getWidth(), Math.max(displayViewHeight, displayViewWidth) / page.getHeight()) * 1.2f);
                        create.setScreenSize(displayViewWidth, displayViewHeight);
                        create.setZoom((int) ((f * 100.0f) * max));
                        create.setLineSpace(max * 2.0f);
                        this.h = d.getReflowMode();
                        create.setParseFlags((long) this.h);
                        create.startParse(null);
                        this.f = new PointF(displayViewWidth, Math.max(create.getContentHeight(), displayViewHeight));
                        this.g = new Matrix();
                        create.release();
                    } else {
                        this.mStatus = -1;
                        this.mErr = 12;
                        return;
                    }
                }
                closePage(this.i, this.a);
                this.mErr = 0;
                this.mStatus = 3;
            } catch (PDFException e) {
                this.mErr = e.getLastError();
                this.mStatus = -1;
                e.printStackTrace();
            }
        }
    }
}
