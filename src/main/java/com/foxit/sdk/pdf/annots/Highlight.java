package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class Highlight extends TextMarkup {
    private transient long swigCPtr;

    protected Highlight(long j, boolean z) {
        super(AnnotationsJNI.Highlight_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Highlight highlight) {
        return highlight == null ? 0 : highlight.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Highlight(this.swigCPtr);
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
            return AnnotationsJNI.Highlight_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }
}
