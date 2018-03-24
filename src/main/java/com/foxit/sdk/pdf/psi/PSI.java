package com.foxit.sdk.pdf.psi;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;

public class PSI {
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected PSI(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PSI psi) {
        return psi == null ? 0 : psi.a;
    }

    protected void finalize() {
        delete();
    }

    protected synchronized void delete() {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PSIJNI.delete_PSI(this.a);
            }
            this.a = 0;
        }
    }

    public static PSI create(Bitmap bitmap, boolean z) throws PDFException {
        if (bitmap == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (bitmap.getConfig().equals(Config.ARGB_8888)) {
            long PSI_create__SWIG_0 = PSIJNI.PSI_create__SWIG_0(bitmap, z);
            return PSI_create__SWIG_0 == 0 ? null : new PSI(PSI_create__SWIG_0, false);
        } else {
            throw new PDFException(PDFError.UNSUPPORTED);
        }
    }

    public static PSI create(int i, int i2, boolean z) throws PDFException {
        long PSI_create__SWIG_1 = PSIJNI.PSI_create__SWIG_1(i, i2, z);
        return PSI_create__SWIG_1 == 0 ? null : new PSI(PSI_create__SWIG_1, false);
    }

    public void setCallback(PSICallback pSICallback) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pSICallback == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            PSIJNI.PSI_setCallback(this.a, this, pSICallback);
        }
    }

    public void setColor(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        PSIJNI.PSI_setColor(this.a, this, j);
    }

    public void setDiameter(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        PSIJNI.PSI_setDiameter(this.a, this, i);
    }

    public void setOpacity(float f) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        PSIJNI.PSI_setOpacity(this.a, this, f);
    }

    public void addPoint(PointF pointF, int i, float f) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pointF == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            PSIJNI.PSI_addPoint(this.a, this, pointF, i, f);
        }
    }

    public RectF getContentsRect() throws PDFException {
        if (this.a != 0) {
            return PSIJNI.PSI_getContentsRect(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public Bitmap getBitmap() throws PDFException {
        if (this.a != 0) {
            return PSIJNI.PSI_getBitmap(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public Annot convertToPDFAnnot(PDFPage pDFPage, RectF rectF, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pDFPage == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            if (PSIJNI.PSI_convertToPDFAnnot(this.a, this, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, rectF, i) == 0) {
                return null;
            }
            Annot annot = (Annot) a.a(Annot.class, "create", new Class[]{Long.TYPE, Integer.TYPE, PDFPage.class}, new Object[]{Long.valueOf(PSIJNI.PSI_convertToPDFAnnot(this.a, this, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, rectF, i)), Integer.valueOf(16), pDFPage});
            if (annot == null) {
                return annot;
            }
            a.a((Object) pDFPage, "addAnnotToCache", new Class[]{Annot.class, Long.TYPE}, new Object[]{annot, Long.valueOf(r2)});
            return annot;
        }
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        PSIJNI.PSI_release(this.a, this);
        this.a = 0;
    }
}
