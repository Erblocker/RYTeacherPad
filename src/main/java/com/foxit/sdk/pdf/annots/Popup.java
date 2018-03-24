package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class Popup extends Annot {
    private transient long swigCPtr;

    protected Popup(long j, boolean z) {
        super(AnnotationsJNI.Popup_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Popup popup) {
        return popup == null ? 0 : popup.swigCPtr;
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Popup(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    public boolean getOpenStatus() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Popup_getOpenStatus(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setOpenStatus(boolean z) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Popup_setOpenStatus(this.swigCPtr, this, z);
    }
}
