package com.foxit.sdk.pdf.annots;

import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;

public class Circle extends Markup {
    private transient long swigCPtr;

    protected Circle(long j, boolean z) {
        super(AnnotationsJNI.Circle_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Circle circle) {
        return circle == null ? 0 : circle.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Circle(this.swigCPtr);
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
            return AnnotationsJNI.Circle_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public long getFillColor() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Circle_getFillColor(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setFillColor(long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Circle_setFillColor(this.swigCPtr, this, j);
    }

    public RectF getInnerRect() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Circle_getInnerRect(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setInnerRect(RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (isValidInnerRect(rectF)) {
            AnnotationsJNI.Circle_setInnerRect(this.swigCPtr, this, rectF);
        } else {
            throw new PDFException(8);
        }
    }
}
