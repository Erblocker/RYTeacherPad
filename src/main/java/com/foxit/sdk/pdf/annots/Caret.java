package com.foxit.sdk.pdf.annots;

import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;

public class Caret extends Markup {
    private transient long swigCPtr;

    protected Caret(long j, boolean z) {
        super(AnnotationsJNI.Caret_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Caret caret) {
        return caret == null ? 0 : caret.swigCPtr;
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Caret(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Caret_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public RectF getInnerRect() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Caret_getInnerRect(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setInnerRect(RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (isValidInnerRect(rectF)) {
            AnnotationsJNI.Caret_setInnerRect(this.swigCPtr, this, rectF);
        } else {
            throw new PDFException(8);
        }
    }
}
