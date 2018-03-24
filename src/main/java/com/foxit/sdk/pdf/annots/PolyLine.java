package com.foxit.sdk.pdf.annots;

import android.graphics.PointF;
import com.foxit.sdk.common.PDFException;

public class PolyLine extends Markup {
    private transient long swigCPtr;

    protected PolyLine(long j, boolean z) {
        super(AnnotationsJNI.PolyLine_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(PolyLine polyLine) {
        return polyLine == null ? 0 : polyLine.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_PolyLine(this.swigCPtr);
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
            return AnnotationsJNI.PolyLine_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public long getStyleFillColor() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.PolyLine_getStyleFillColor(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setStyleFillColor(long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.PolyLine_setStyleFillColor(this.swigCPtr, this, j);
    }

    public int getVertexCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.PolyLine_getVertexCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public PointF getVertex(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getVertexCount()) {
            return AnnotationsJNI.PolyLine_getVertex(this.swigCPtr, this, i);
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
            AnnotationsJNI.PolyLine_setVertexes(this.swigCPtr, this, pointFArr);
        }
    }

    public String getLineStartingStyle() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.PolyLine_getLineStartingStyle(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setLineStartingStyle(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (Markup.isValidLineEndingStyle(str)) {
            AnnotationsJNI.PolyLine_setLineStartingStyle(this.swigCPtr, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public String getLineEndingStyle() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.PolyLine_getLineEndingStyle(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setLineEndingStyle(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (Markup.isValidLineEndingStyle(str)) {
            AnnotationsJNI.PolyLine_setLineEndingStyle(this.swigCPtr, this, str);
        } else {
            throw new PDFException(8);
        }
    }
}
