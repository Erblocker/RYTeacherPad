package com.foxit.sdk.pdf.form;

import android.graphics.Matrix;
import android.graphics.PointF;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.Renderer;

public class FormFiller {
    private long a;

    protected FormFiller(long j) {
        this.a = j;
    }

    protected long getHandle() {
        return this.a;
    }

    public static FormFiller create(Form form, FormFillerAssist formFillerAssist) throws PDFException {
        if (form == null || formFillerAssist == null) {
            throw new PDFException(8);
        }
        long FormFiller_create = FormsJNI.FormFiller_create(Form.getCPtr(form), formFillerAssist);
        return FormFiller_create == 0 ? null : new FormFiller(FormFiller_create);
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        FormsJNI.FormFiller_release(this.a);
        this.a = 0;
    }

    public void render(PDFPage pDFPage, Matrix matrix, Renderer renderer) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage == null || renderer == null || matrix == null) {
            throw new PDFException(8);
        } else {
            FormsJNI.FormFiller_render(this.a, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), matrix, ((Long) a.a(Renderer.class, "getCPtr", renderer)).longValue());
        }
    }

    public boolean click(PDFPage pDFPage, PointF pointF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage != null && pointF != null) {
            return FormsJNI.FormFiller_click(this.a, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pointF);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean touchDown(PDFPage pDFPage, PointF pointF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage != null && pointF != null) {
            return FormsJNI.FormFiller_touchDown(this.a, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pointF);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean touchUp(PDFPage pDFPage, PointF pointF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage != null && pointF != null) {
            return FormsJNI.FormFiller_touchUp(this.a, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pointF);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean touchMove(PDFPage pDFPage, PointF pointF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage != null && pointF != null) {
            return FormsJNI.FormFiller_touchMove(this.a, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pointF);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean input(char c) throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormFiller_input(this.a, c);
        }
        throw new PDFException(4);
    }

    public void highlightFormFields(boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        FormsJNI.FormFiller_highlightFormFields(this.a, z);
    }

    public void setHighlightColor(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        FormsJNI.FormFiller_setHighlightColor(this.a, j);
    }

    public boolean setFocus(FormControl formControl) throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormFiller_setFocus(this.a, this, FormControl.getCPtr(formControl), formControl);
        }
        throw new PDFException(4);
    }
}
