package com.foxit.sdk.pdf.objects;

import android.graphics.Matrix;
import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;

class ObjectsJNI {
    public static final native long PDFArray_SWIGUpcast(long j);

    public static final native void PDFArray_addElement(long j, PDFArray pDFArray, long j2, PDFObject pDFObject) throws PDFException;

    public static final native long PDFArray_create() throws PDFException;

    public static final native long PDFArray_createFromMatrix(Matrix matrix) throws PDFException;

    public static final native long PDFArray_createFromRect(RectF rectF) throws PDFException;

    public static final native long PDFArray_getElement(long j, PDFArray pDFArray, int i) throws PDFException;

    public static final native int PDFArray_getElementCount(long j, PDFArray pDFArray) throws PDFException;

    public static final native void PDFArray_insertAt(long j, PDFArray pDFArray, int i, long j2, PDFObject pDFObject) throws PDFException;

    public static final native void PDFArray_removeAt(long j, PDFArray pDFArray, int i) throws PDFException;

    public static final native void PDFArray_setAt(long j, PDFArray pDFArray, int i, long j2, PDFObject pDFObject) throws PDFException;

    public static final native long PDFDictionary_SWIGUpcast(long j);

    public static final native long PDFDictionary_create() throws PDFException;

    public static final native long PDFDictionary_getElement(long j, PDFDictionary pDFDictionary, String str) throws PDFException;

    public static final native String PDFDictionary_getKey(long j, PDFDictionary pDFDictionary, long j2) throws PDFException;

    public static final native long PDFDictionary_getValue(long j, PDFDictionary pDFDictionary, long j2) throws PDFException;

    public static final native boolean PDFDictionary_hasKey(long j, PDFDictionary pDFDictionary, String str) throws PDFException;

    public static final native long PDFDictionary_moveNext(long j, PDFDictionary pDFDictionary, long j2) throws PDFException;

    public static final native void PDFDictionary_removeAt(long j, PDFDictionary pDFDictionary, String str) throws PDFException;

    public static final native void PDFDictionary_setAt(long j, PDFDictionary pDFDictionary, String str, long j2, PDFObject pDFObject) throws PDFException;

    public static final native long PDFObject_cloneObject(long j, PDFObject pDFObject) throws PDFException;

    public static final native long PDFObject_createFromBoolean(boolean z) throws PDFException;

    public static final native long PDFObject_createFromDateTime(DateTime dateTime) throws PDFException;

    public static final native long PDFObject_createFromFloat(float f) throws PDFException;

    public static final native long PDFObject_createFromInteger(int i) throws PDFException;

    public static final native long PDFObject_createFromName(String str) throws PDFException;

    public static final native long PDFObject_createFromString(String str) throws PDFException;

    public static final native long PDFObject_createReference(long j, PDFDoc pDFDoc, long j2) throws PDFException;

    public static final native boolean PDFObject_getBoolean(long j, PDFObject pDFObject) throws PDFException;

    public static final native DateTime PDFObject_getDateTime(long j, PDFObject pDFObject) throws PDFException;

    public static final native long PDFObject_getDirectObject(long j, PDFObject pDFObject) throws PDFException;

    public static final native float PDFObject_getFloat(long j, PDFObject pDFObject) throws PDFException;

    public static final native int PDFObject_getInteger(long j, PDFObject pDFObject) throws PDFException;

    public static final native Matrix PDFObject_getMatrix(long j, PDFObject pDFObject) throws PDFException;

    public static final native long PDFObject_getObjNum(long j, PDFObject pDFObject) throws PDFException;

    public static final native RectF PDFObject_getRect(long j, PDFObject pDFObject) throws PDFException;

    public static final native String PDFObject_getString(long j, PDFObject pDFObject) throws PDFException;

    public static final native int PDFObject_getType(long j, PDFObject pDFObject) throws PDFException;

    public static final native void PDFObject_release(long j, PDFObject pDFObject) throws PDFException;

    public static final native long PDFStream_SWIGUpcast(long j);

    public static final native long PDFStream_create(long j, PDFDictionary pDFDictionary) throws PDFException;

    public static final native boolean PDFStream_getData(long j, PDFStream pDFStream, boolean z, byte[] bArr) throws PDFException;

    public static final native long PDFStream_getDataSize(long j, PDFStream pDFStream, boolean z) throws PDFException;

    public static final native long PDFStream_getDictionary(long j, PDFStream pDFStream) throws PDFException;

    public static final native void PDFStream_setData(long j, PDFStream pDFStream, byte[] bArr) throws PDFException;
}
