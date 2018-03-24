package com.foxit.sdk.pdf.annots;

import android.graphics.PointF;
import com.foxit.sdk.common.PDFException;

public class Line extends Markup {
    private transient long swigCPtr;

    protected Line(long j, boolean z) {
        super(AnnotationsJNI.Line_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Line line) {
        return line == null ? 0 : line.swigCPtr;
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Line(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public String getLineStartingStyle() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_getLineStartingStyle(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setLineStartingStyle(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (Markup.isValidLineEndingStyle(str)) {
            AnnotationsJNI.Line_setLineStartingStyle(this.swigCPtr, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public String getLineEndingStyle() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_getLineEndingStyle(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setLineEndingStyle(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (Markup.isValidLineEndingStyle(str)) {
            AnnotationsJNI.Line_setLineEndingStyle(this.swigCPtr, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public long getStyleFillColor() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_getStyleFillColor(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setStyleFillColor(long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Line_setStyleFillColor(this.swigCPtr, this, j);
    }

    public PointF getStartPoint() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_getStartPoint(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setStartPoint(PointF pointF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (pointF == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Line_setStartPoint(this.swigCPtr, this, pointF);
        }
    }

    public PointF getEndPoint() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_getEndPoint(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setEndPoint(PointF pointF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (pointF == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Line_setEndPoint(this.swigCPtr, this, pointF);
        }
    }

    public boolean hasCaption() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_hasCaption(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void enableCaption(boolean z) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Line_enableCaption(this.swigCPtr, this, z);
    }

    public String getCaptionPositionType() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_getCaptionPositionType(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setCaptionPositionType(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() < 1) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Line_setCaptionPositionType(this.swigCPtr, this, str);
        }
    }

    public PointF getCaptionOffset() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Line_getCaptionOffset(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setCaptionOffset(PointF pointF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (pointF == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Line_setCaptionOffset(this.swigCPtr, this, pointF);
        }
    }
}
