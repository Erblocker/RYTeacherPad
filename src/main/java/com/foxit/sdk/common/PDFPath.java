package com.foxit.sdk.common;

import android.graphics.PointF;

public class PDFPath {
    public static final int e_pointTypeBezierTo = 4;
    public static final int e_pointTypeBezierToCloseFigure = 5;
    public static final int e_pointTypeLineTo = 2;
    public static final int e_pointTypeLineToCloseFigure = 3;
    public static final int e_pointTypeMoveTo = 1;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected PDFPath(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFPath pDFPath) {
        return pDFPath == null ? 0 : pDFPath.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                CommonJNI.delete_PDFPath(this.a);
            }
            this.a = 0;
        }
    }

    public static PDFPath create() throws PDFException {
        long PDFPath_create = CommonJNI.PDFPath_create();
        return PDFPath_create == 0 ? null : new PDFPath(PDFPath_create, false);
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        CommonJNI.PDFPath_release(this.a, this);
        resetHandle();
    }

    public int getPointCount() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.PDFPath_getPointCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public PointF getPoint(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getPointCount()) {
            return CommonJNI.PDFPath_getPoint(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public int getPointType(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getPointCount()) {
            return CommonJNI.PDFPath_getPointType(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean setPoint(int i, PointF pointF, int i2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getPointCount() && pointF != null && !a(i2)) {
            return CommonJNI.PDFPath_setPoint(this.a, this, i, pointF, i2);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean moveTo(PointF pointF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pointF != null) {
            return CommonJNI.PDFPath_moveTo(this.a, this, pointF);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean lineTo(PointF pointF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pointF != null) {
            return CommonJNI.PDFPath_lineTo(this.a, this, pointF);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean cubicBezierTo(PointF pointF, PointF pointF2, PointF pointF3) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pointF != null && pointF2 != null && pointF3 != null) {
            return CommonJNI.PDFPath_cubicBezierTo(this.a, this, pointF, pointF2, pointF3);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean closeFigure() throws PDFException {
        if (this.a != 0) {
            return CommonJNI.PDFPath_closeFigure(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean removePoint(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getPointCount()) {
            return CommonJNI.PDFPath_removePoint(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public void clear() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        CommonJNI.PDFPath_clear(this.a, this);
    }

    private static boolean a(int i) {
        return i < 1 || i > 5;
    }
}
