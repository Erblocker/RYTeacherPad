package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class TextMarkup extends Markup {
    private transient long swigCPtr;

    protected TextMarkup(long j, boolean z) {
        super(AnnotationsJNI.TextMarkup_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(TextMarkup textMarkup) {
        return textMarkup == null ? 0 : textMarkup.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_TextMarkup(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    public int getQuadPointsCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.TextMarkup_getQuadPointsCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public QuadPoints getQuadPoints(int i) throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.TextMarkup_getQuadPoints(this.swigCPtr, this, i);
        }
        throw new PDFException(4);
    }

    public void setQuadPoints(QuadPoints[] quadPointsArr) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (quadPointsArr == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.TextMarkup_setQuadPoints(this.swigCPtr, this, quadPointsArr);
        }
    }
}
