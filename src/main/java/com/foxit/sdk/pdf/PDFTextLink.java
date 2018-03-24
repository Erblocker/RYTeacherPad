package com.foxit.sdk.pdf;

import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;

public class PDFTextLink {
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected PDFTextLink(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFTextLink pDFTextLink) {
        return pDFTextLink == null ? 0 : pDFTextLink.a;
    }

    public String getURI() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextLink_getURI(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getStartCharIndex() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextLink_getStartCharIndex(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getEndCharIndex() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextLink_getEndCharIndex(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getRectCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextLink_getRectCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public RectF getRect(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getRectCount()) {
            return PDFJNI.PDFTextLink_getRect(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }
}
