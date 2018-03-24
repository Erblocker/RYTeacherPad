package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;

public class Ink extends Markup {
    private PDFPath a = null;
    private transient long swigCPtr;

    protected Ink(long j, boolean z) {
        super(AnnotationsJNI.Ink_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Ink ink) {
        return ink == null ? 0 : ink.swigCPtr;
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
        if (this.a != null) {
            try {
                a.a(this.a, "resetHandle");
            } catch (PDFException e) {
                e.printStackTrace();
            }
            this.a = null;
        }
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Ink(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Ink_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public PDFPath getInkList() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        long Ink_getInkList = AnnotationsJNI.Ink_getInkList(this.swigCPtr, this);
        if (Ink_getInkList == 0) {
            return null;
        }
        if (this.a == null) {
            this.a = (PDFPath) a.a(PDFPath.class, Ink_getInkList, false);
        } else if (Ink_getInkList != ((Long) a.a(PDFPath.class, "getCPtr", this.a)).longValue()) {
            resetInkListHandler();
            this.a = (PDFPath) a.a(PDFPath.class, Ink_getInkList, false);
        }
        return this.a;
    }

    public void setInkList(PDFPath pDFPath) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (pDFPath == null || ((Long) a.a(PDFPath.class, "getCPtr", (Object) pDFPath)).longValue() == 0 || pDFPath.getPointCount() < 1) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Ink_setInkList(this.swigCPtr, this, ((Long) a.a(PDFPath.class, "getCPtr", (Object) pDFPath)).longValue(), pDFPath);
            if (!(this.a == null || a(pDFPath, this.a))) {
                resetInkListHandler();
            }
            this.a = pDFPath;
        }
    }

    private static boolean a(PDFPath pDFPath, PDFPath pDFPath2) throws PDFException {
        return ((Long) a.a(PDFPath.class, "getCPtr", (Object) pDFPath)).longValue() == ((Long) a.a(PDFPath.class, "getCPtr", (Object) pDFPath2)).longValue();
    }

    private void resetInkListHandler() throws PDFException {
        if (this.a != null) {
            a.a(this.a, "resetHandle");
        }
        this.a = null;
    }
}
