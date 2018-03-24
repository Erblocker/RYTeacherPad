package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;

public class PSInk extends Annot {
    private transient long swigCPtr;

    protected PSInk(long j, boolean z) {
        super(AnnotationsJNI.PSInk_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(PSInk pSInk) {
        return pSInk == null ? 0 : pSInk.swigCPtr;
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_PSInk(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.PSInk_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }
}
