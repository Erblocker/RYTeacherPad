package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class Underline extends TextMarkup {
    private transient long swigCPtr;

    protected Underline(long j, boolean z) {
        super(AnnotationsJNI.Underline_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Underline underline) {
        return underline == null ? 0 : underline.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Underline(this.swigCPtr);
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
            return AnnotationsJNI.Underline_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }
}
