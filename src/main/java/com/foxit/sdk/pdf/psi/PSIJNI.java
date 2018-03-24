package com.foxit.sdk.pdf.psi;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;

class PSIJNI {
    public static final native void PSI_addPoint(long j, PSI psi, PointF pointF, int i, float f) throws PDFException;

    public static final native long PSI_convertToPDFAnnot(long j, PSI psi, long j2, PDFPage pDFPage, RectF rectF, int i) throws PDFException;

    public static final native long PSI_create__SWIG_0(Bitmap bitmap, boolean z) throws PDFException;

    public static final native long PSI_create__SWIG_1(int i, int i2, boolean z) throws PDFException;

    public static final native Bitmap PSI_getBitmap(long j, PSI psi) throws PDFException;

    public static final native RectF PSI_getContentsRect(long j, PSI psi) throws PDFException;

    public static final native void PSI_release(long j, PSI psi) throws PDFException;

    public static final native void PSI_setCallback(long j, PSI psi, PSICallback pSICallback) throws PDFException;

    public static final native void PSI_setColor(long j, PSI psi, long j2) throws PDFException;

    public static final native void PSI_setDiameter(long j, PSI psi, int i) throws PDFException;

    public static final native void PSI_setOpacity(long j, PSI psi, float f) throws PDFException;

    public static final native void delete_PSI(long j);
}
