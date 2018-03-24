package com.foxit.sdk.pdf;

import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;

public class PDFTextSelect {
    private transient long a;
    protected PDFPage mPDFPage = null;
    protected transient boolean swigCMemOwn;

    protected PDFTextSelect(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFTextSelect pDFTextSelect) {
        return pDFTextSelect == null ? 0 : pDFTextSelect.a;
    }

    private synchronized void a() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.PDFTextSelect_release(this.a, this);
            }
            this.a = 0;
            this.mPDFPage = null;
        }
    }

    public static PDFTextSelect create(PDFPage pDFPage) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        } else if (pDFPage.isParsed()) {
            long PDFTextSelect_create = PDFJNI.PDFTextSelect_create(PDFPage.getCPtr(pDFPage), pDFPage);
            if (PDFTextSelect_create == 0) {
                throw new PDFException(4);
            }
            PDFTextSelect pDFTextSelect = new PDFTextSelect(PDFTextSelect_create, true);
            pDFTextSelect.mPDFPage = pDFPage;
            return pDFTextSelect;
        } else {
            throw new PDFException(12);
        }
    }

    public void release() throws PDFException {
        a();
    }

    public PDFPage getPage() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (this.mPDFPage != null) {
            return this.mPDFPage;
        } else {
            this.mPDFPage = new PDFPage(PDFJNI.PDFTextSelect_getPage(this.a, this), false);
            return this.mPDFPage;
        }
    }

    public int getCharCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSelect_getCharCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getChars(int i, int i2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getCharCount() && i2 >= -1) {
            return PDFJNI.PDFTextSelect_getChars(this.a, this, i, i2);
        } else {
            throw new PDFException(8);
        }
    }

    public int getIndexAtPos(float f, float f2, float f3) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (f3 >= 0.0f && f3 <= 30.0f) {
            return PDFJNI.PDFTextSelect_getIndexAtPos(this.a, this, f, f2, f3);
        } else {
            throw new PDFException(8);
        }
    }

    public String getTextInRect(RectF rectF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (rectF != null) {
            return PDFJNI.PDFTextSelect_getTextInRect(this.a, this, rectF);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean getWordAtPos(float f, float f2, float f3, Integer num, Integer num2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (num == null || num2 == null) {
            throw new PDFException(8);
        } else if (f3 >= 0.0f && f3 <= 30.0f) {
            return PDFJNI.PDFTextSelect_getWordAtPos(this.a, this, f, f2, f3, num, num2);
        } else {
            throw new PDFException(8);
        }
    }

    public int getTextRectCount(int i, int i2) throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSelect_getTextRectCount(this.a, this, i, i2);
        }
        throw new PDFException(4);
    }

    public RectF getTextRect(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0) {
            return PDFJNI.PDFTextSelect_getTextRect(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public int getBaselineRotation(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0) {
            return PDFJNI.PDFTextSelect_getBaselineRotation(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }
}
