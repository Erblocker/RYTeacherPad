package com.foxit.sdk.pdf;

import android.graphics.Matrix;
import android.graphics.PointF;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;

public class ReflowPage {
    public static final int e_reflowNoTruncate = 2;
    public static final int e_reflowNormal = 0;
    public static final int e_reflowWithImage = 1;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected ReflowPage(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(ReflowPage reflowPage) {
        return reflowPage == null ? 0 : reflowPage.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.delete_ReflowPage(this.a);
            }
            this.a = 0;
        }
    }

    public static ReflowPage create(PDFPage pDFPage) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long ReflowPage_create = PDFJNI.ReflowPage_create(PDFPage.getCPtr(pDFPage), pDFPage);
        return ReflowPage_create == 0 ? null : new ReflowPage(ReflowPage_create, false);
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.ReflowPage_release(this.a, this);
        this.a = 0;
    }

    public void setScreenSize(float f, float f2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.ReflowPage_setScreenSize(this.a, this, f, f2);
    }

    public void setZoom(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.ReflowPage_setZoom(this.a, this, i);
    }

    public void setParseFlags(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.ReflowPage_setParseFlags(this.a, this, j);
    }

    public void setLineSpace(float f) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.ReflowPage_setLineSpace(this.a, this, f);
    }

    public void setTopSpace(float f) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.ReflowPage_setTopSpace(this.a, this, f);
    }

    public int startParse(Pause pause) throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReflowPage_startParse(this.a, this, pause);
        }
        throw new PDFException(4);
    }

    public int continueParse() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReflowPage_continueParse(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getContentWidth() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReflowPage_getContentWidth(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getContentHeight() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReflowPage_getContentHeight(this.a, this);
        }
        throw new PDFException(4);
    }

    public Matrix getDisplayMatrix(float f, float f2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (isParsed()) {
            return PDFJNI.ReflowPage_getDisplayMatrix(this.a, this, f, f2);
        } else {
            throw new PDFException(12);
        }
    }

    public String getFocusData(Matrix matrix, PointF pointF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (matrix != null && pointF != null) {
            return PDFJNI.ReflowPage_getFocusData(this.a, this, matrix, pointF);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public PointF getFocusPosition(Matrix matrix, String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (matrix != null) {
            return PDFJNI.ReflowPage_getFocusPosition(this.a, this, matrix, str);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public boolean isParsed() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReflowPage_isParsed(this.a, this);
        }
        throw new PDFException(4);
    }
}
