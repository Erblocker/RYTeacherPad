package com.foxit.sdk.pdf.annots;

import android.graphics.Bitmap;
import com.foxit.sdk.common.PDFException;

public class Stamp extends Markup {
    public static final String STANDARDICONNAME_APPROVED = "Approved";
    public static final String STANDARDICONNAME_ASIS = "AsIs";
    public static final String STANDARDICONNAME_CODFIDENTIAL = "Confidential";
    public static final String STANDARDICONNAME_DEPARTMENTAL = "Departmental";
    public static final String STANDARDICONNAME_DRAFT = "Draft";
    public static final String STANDARDICONNAME_EXPERIMENTAL = "Experimental";
    public static final String STANDARDICONNAME_EXPIRED = "Expired";
    public static final String STANDARDICONNAME_FINAL = "Final";
    public static final String STANDARDICONNAME_FORCOMMENT = "ForComment";
    public static final String STANDARDICONNAME_FORPUBLICRELEASE = "ForPublicRelease";
    public static final String STANDARDICONNAME_NOTAPPROVED = "NotApproved";
    public static final String STANDARDICONNAME_NOTFORPUBLICRELEASE = "NotForPublicRelease";
    public static final String STANDARDICONNAME_SOLD = "Sold";
    public static final String STANDARDICONNAME_TOPSECRET = "TopSecret";
    private transient long swigCPtr;

    protected Stamp(long j, boolean z) {
        super(AnnotationsJNI.Stamp_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Stamp stamp) {
        return stamp == null ? 0 : stamp.swigCPtr;
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    public synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Stamp(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Stamp_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public String getIconName() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Stamp_getIconName(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setIconName(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() < 1) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Stamp_setIconName(this.swigCPtr, this, str);
        }
    }

    public void setBitmap(Bitmap bitmap) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (bitmap == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Stamp_setBitmap(this.swigCPtr, this, bitmap);
        }
    }
}
