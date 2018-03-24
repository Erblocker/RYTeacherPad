package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class Squiggly extends TextMarkup {
    private transient long swigCPtr;

    protected Squiggly(long j, boolean z) {
        super(AnnotationsJNI.Squiggly_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Squiggly squiggly) {
        return squiggly == null ? 0 : squiggly.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Squiggly(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Squiggly_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }
}
