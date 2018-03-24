package com.foxit.sdk.pdf.annots;

import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.common.DefaultAppearance;
import com.foxit.sdk.common.PDFException;

public class FreeText extends Markup {
    private transient long swigCPtr;

    protected FreeText(long j, boolean z) {
        super(AnnotationsJNI.FreeText_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(FreeText freeText) {
        return freeText == null ? 0 : freeText.swigCPtr;
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_FreeText(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FreeText_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public long getFillColor() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FreeText_getFillColor(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setFillColor(long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.FreeText_setFillColor(this.swigCPtr, this, j);
    }

    public int getAlignment() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FreeText_getAlignment(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setAlignment(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.FreeText_setAlignment(this.swigCPtr, this, i);
    }

    public RectF getInnerRect() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FreeText_getInnerRect(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setInnerRect(RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (isValidInnerRect(rectF)) {
            AnnotationsJNI.FreeText_setInnerRect(this.swigCPtr, this, rectF);
        } else {
            throw new PDFException(8);
        }
    }

    public DefaultAppearance getDefaultAppearance() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FreeText_getDefaultAppearance(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public boolean setDefaultAppearance(DefaultAppearance defaultAppearance) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (defaultAppearance == null || defaultAppearance.getFlags() < 0) {
            throw new PDFException(8);
        } else if ((defaultAppearance.getFlags() & 1) != 0 && defaultAppearance.getFont() == null) {
            throw new PDFException(8);
        } else if ((defaultAppearance.getFlags() & 4) == 0 || Float.compare(defaultAppearance.getFontSize(), 0.0f) > 0) {
            return AnnotationsJNI.FreeText_setDefaultAppearance(this.swigCPtr, this, defaultAppearance);
        } else {
            throw new PDFException(8);
        }
    }

    public String getCalloutLineEndingStyle() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FreeText_getCalloutLineEndingStyle(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setCalloutLineEndingStyle(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (Markup.isValidLineEndingStyle(str)) {
            AnnotationsJNI.FreeText_setCalloutLineEndingStyle(this.swigCPtr, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public int getCalloutLinePointCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FreeText_getCalloutLinePointCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public PointF getCalloutLinePoint(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getCalloutLinePointCount()) {
            return AnnotationsJNI.FreeText_getCalloutLinePoint(this.swigCPtr, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public void setCalloutLinePoints(PointF pointF, PointF pointF2, PointF pointF3) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (pointF == null || pointF2 == null || pointF3 == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.FreeText_setCalloutLinePoints(this.swigCPtr, this, pointF, pointF2, pointF3);
        }
    }
}
