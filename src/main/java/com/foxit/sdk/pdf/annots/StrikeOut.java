package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class StrikeOut extends TextMarkup {
    private transient long swigCPtr;

    protected StrikeOut(long j, boolean z) {
        super(AnnotationsJNI.StrikeOut_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(StrikeOut strikeOut) {
        return strikeOut == null ? 0 : strikeOut.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_StrikeOut(this.swigCPtr);
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
            return AnnotationsJNI.StrikeOut_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }
}
