package com.foxit.sdk;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.ReflowPage;
import com.foxit.sdk.pdf.Renderer;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.form.Form;
import com.foxit.sdk.pdf.form.FormFiller;

/* compiled from: Task */
class f extends Task {
    protected int a;
    protected int b;
    protected int c;
    protected int d;
    protected Rect e;
    protected Point f;
    protected int g;
    protected Bitmap h;
    protected boolean i;
    protected int j;
    protected boolean k;
    private PDFDoc l;

    protected f(d dVar, PDFDoc pDFDoc, int i, int i2, int i3, int i4, Rect rect, Point point, int i5, int i6, boolean z, CallBack callBack) {
        super(callBack);
        this.mDocManager = dVar;
        this.l = pDFDoc;
        this.a = i;
        this.b = i2;
        this.c = i3;
        this.d = i4;
        this.e = rect;
        this.f = point;
        this.g = i5;
        this.mPriority = 3;
        this.j = i6;
        this.k = z;
    }

    public String toString() {
        return "DrawPageTask - pageIndex:" + this.a + " drawFor:" + this.g;
    }

    protected boolean a() {
        return this.i;
    }

    protected void a(boolean z) {
        this.i = z;
    }

    protected void prepare() {
        if (this.h != null) {
            return;
        }
        if (this.g == 2) {
            this.h = this.mDocManager.c(this.a);
        } else if (this.g == 4) {
            this.h = this.mDocManager.g();
        } else {
            this.h = Bitmap.createBitmap(this.e.width(), this.e.height(), Config.ARGB_8888);
        }
    }

    protected void execute() {
        if (this.mStatus == 1) {
            this.mStatus = 2;
            if (this.e.width() == 0 || this.e.height() == 0) {
                this.mStatus = -1;
            } else if (this.h == null) {
                this.mErr = 6;
                this.mStatus = -1;
            } else if (this.b != 3) {
                b(this.a);
            } else {
                a(this.a);
            }
        }
    }

    private void a(int i) {
        int i2 = 2;
        try {
            PDFPage page = getPage(this.l, i);
            ReflowPage create = ReflowPage.create(page);
            PDFViewCtrl d = this.mDocManager.d();
            float f = d.getViewStatus().n;
            float displayViewHeight = (float) (d.getDisplayViewHeight() - 40);
            float displayViewWidth = (float) (d.getDisplayViewWidth() - 20);
            float max = Math.max(0.25f, Math.min(Math.min(displayViewHeight, displayViewWidth) / page.getWidth(), Math.max(displayViewHeight, displayViewWidth) / page.getHeight()) * 1.2f);
            create.setScreenSize(displayViewWidth, displayViewHeight);
            create.setLineSpace(2.0f * max);
            create.setZoom((int) ((f * 100.0f) * max));
            create.setParseFlags((long) d.getReflowMode());
            if (!(create.isParsed() || create.startParse(null) == 2)) {
                this.mErr = 12;
                this.mStatus = -1;
            }
            if (this.k) {
                if (page.hasTransparency()) {
                    this.h.eraseColor(0);
                } else {
                    this.h.eraseColor(c.d);
                }
            } else if (page.hasTransparency()) {
                this.h.eraseColor(0);
                i2 = 0;
            } else {
                this.h.eraseColor(-1);
                i2 = 0;
            }
            Renderer create2 = Renderer.create(this.h);
            create2.setColorMode(i2);
            create2.setMappingModeColors((long) c.d, (long) c.e);
            for (i2 = create2.startRenderReflowPage(create, create.getDisplayMatrix((float) (-this.e.left), (float) (-this.e.top)), null); i2 == 1; i2 = create2.continueRender()) {
            }
            create.release();
            create2.release();
            closePage(this.l, i);
            this.mErr = 0;
            this.mStatus = 3;
        } catch (PDFException e) {
            this.mErr = e.getLastError();
            this.mStatus = -1;
        }
    }

    private void b(int i) {
        try {
            int i2;
            FormFiller formFiller;
            PDFPage page = getPage(this.l, i);
            Matrix displayMatrix = page.getDisplayMatrix(-this.e.left, -this.e.top, this.f.x, this.f.y, 0);
            if (this.k) {
                i2 = 2;
                if (page.hasTransparency()) {
                    this.h.eraseColor(0);
                } else {
                    this.h.eraseColor(c.d);
                }
            } else if (page.hasTransparency()) {
                this.h.eraseColor(0);
                i2 = 0;
            } else {
                this.h.eraseColor(-1);
                i2 = 0;
            }
            Renderer create = Renderer.create(this.h);
            create.setColorMode(i2);
            create.setMappingModeColors((long) c.d, (long) c.e);
            PDFViewCtrl d = this.mDocManager.d();
            if (this.mDocManager.j() == null || this.mDocManager.j().getPage().getIndex() != d.getCurrentPage()) {
                create.setRenderContent(3);
            } else {
                create.setRenderContent(1);
            }
            for (i2 = create.startRender(page, displayMatrix, null); i2 == 1; i2 = create.continueRender()) {
            }
            Form form = this.l.getForm();
            if (form != null) {
                formFiller = form.getFormFiller();
            } else {
                formFiller = null;
            }
            if (this.mDocManager.j() != null && this.mDocManager.j().getPage().getIndex() == d.getCurrentPage()) {
                int annotCount = page.getAnnotCount();
                for (i2 = 0; i2 < annotCount; i2++) {
                    Annot annot = page.getAnnot(i2);
                    if (annot != null && this.mDocManager.a(annot)) {
                        create.renderAnnot(annot, displayMatrix);
                    }
                }
            }
            if (formFiller != null) {
                formFiller.render(page, displayMatrix, create);
            }
            create.release();
            closePage(this.l, i);
            this.mErr = 0;
            this.mStatus = 3;
        } catch (PDFException e) {
            this.mErr = e.getLastError();
            this.mStatus = -1;
        }
    }
}
