package com.foxit.sdk.pdf.action;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;

public class Destination {
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected Destination(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(Destination destination) {
        return destination == null ? 0 : destination.a;
    }

    private synchronized void a() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                ActionsJNI.Destination_release(this.a, this);
            }
            this.a = 0;
        }
    }

    public static Destination createXYZ(PDFPage pDFPage, float f, float f2, float f3) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createXYZ = ActionsJNI.Destination_createXYZ(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, f, f2, f3);
        if (Destination_createXYZ != 0) {
            return Destination_createXYZ == 0 ? null : new Destination(Destination_createXYZ, false);
        } else {
            throw new PDFException(4);
        }
    }

    public static Destination createFitPage(PDFPage pDFPage) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createFitPage = ActionsJNI.Destination_createFitPage(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage);
        if (Destination_createFitPage != 0) {
            return Destination_createFitPage == 0 ? null : new Destination(Destination_createFitPage, false);
        } else {
            throw new PDFException(4);
        }
    }

    public static Destination createFitHorz(PDFPage pDFPage, float f) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createFitHorz = ActionsJNI.Destination_createFitHorz(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, f);
        if (Destination_createFitHorz != 0) {
            return Destination_createFitHorz == 0 ? null : new Destination(Destination_createFitHorz, false);
        } else {
            throw new PDFException(4);
        }
    }

    public static Destination createFitVert(PDFPage pDFPage, float f) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createFitVert = ActionsJNI.Destination_createFitVert(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, f);
        if (Destination_createFitVert != 0) {
            return Destination_createFitVert == 0 ? null : new Destination(Destination_createFitVert, false);
        } else {
            throw new PDFException(4);
        }
    }

    public static Destination createFitRect(PDFPage pDFPage, float f, float f2, float f3, float f4) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createFitRect = ActionsJNI.Destination_createFitRect(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, f, f2, f3, f4);
        if (Destination_createFitRect != 0) {
            return Destination_createFitRect == 0 ? null : new Destination(Destination_createFitRect, false);
        } else {
            throw new PDFException(4);
        }
    }

    public static Destination createFitBBox(PDFPage pDFPage) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createFitBBox = ActionsJNI.Destination_createFitBBox(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage);
        if (Destination_createFitBBox != 0) {
            return Destination_createFitBBox == 0 ? null : new Destination(Destination_createFitBBox, false);
        } else {
            throw new PDFException(4);
        }
    }

    public static Destination createFitBHorz(PDFPage pDFPage, float f) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createFitBHorz = ActionsJNI.Destination_createFitBHorz(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, f);
        if (Destination_createFitBHorz != 0) {
            return Destination_createFitBHorz == 0 ? null : new Destination(Destination_createFitBHorz, false);
        } else {
            throw new PDFException(4);
        }
    }

    public static Destination createFitBVert(PDFPage pDFPage, float f) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        }
        long Destination_createFitBVert = ActionsJNI.Destination_createFitBVert(((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, f);
        if (Destination_createFitBVert != 0) {
            return Destination_createFitBVert == 0 ? null : new Destination(Destination_createFitBVert, false);
        } else {
            throw new PDFException(4);
        }
    }

    public void release() throws PDFException {
        a();
    }

    public int getPageIndex() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Destination_getPageIndex(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getZoomMode() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Destination_getZoomMode(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getLeft() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Destination_getLeft(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getTop() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Destination_getTop(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getRight() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Destination_getRight(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getBottom() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Destination_getBottom(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getZoomFactor() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Destination_getZoomFactor(this.a, this);
        }
        throw new PDFException(4);
    }
}
