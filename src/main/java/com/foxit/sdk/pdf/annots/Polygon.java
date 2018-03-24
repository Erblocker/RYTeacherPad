package com.foxit.sdk.pdf.annots;

import android.graphics.PointF;
import com.foxit.sdk.common.PDFException;

public class Polygon extends Markup {
    private transient long swigCPtr;

    protected Polygon(long j, boolean z) {
        super(AnnotationsJNI.Polygon_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Polygon polygon) {
        return polygon == null ? 0 : polygon.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Polygon(this.swigCPtr);
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
            return AnnotationsJNI.Polygon_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public long getFillColor() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Polygon_getFillColor(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setFillColor(long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Polygon_setFillColor(this.swigCPtr, this, j);
    }

    public int getVertexCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Polygon_getVertexCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public PointF getVertex(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getVertexCount()) {
            return AnnotationsJNI.Polygon_getVertex(this.swigCPtr, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public void setVertexes(PointF[] pointFArr) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (pointFArr == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Polygon_setVertexes(this.swigCPtr, this, pointFArr);
        }
    }
}
