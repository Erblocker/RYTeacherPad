package com.foxit.sdk.common;

import android.graphics.PointF;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.annots.AnnotIconProvider;
import com.foxit.sdk.pdf.security.a;

class CommonJNI {
    public static final native long FileSpec_create(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean FileSpec_embed(long j, FileSpec fileSpec, String str) throws PDFException;

    public static final native byte[] FileSpec_getChecksum(long j, FileSpec fileSpec) throws PDFException;

    public static final native DateTime FileSpec_getCreationDateTime(long j, FileSpec fileSpec) throws PDFException;

    public static final native String FileSpec_getDescription(long j, FileSpec fileSpec) throws PDFException;

    public static final native byte[] FileSpec_getFileData(long j, FileSpec fileSpec, long j2, long j3, long j4) throws PDFException;

    public static final native String FileSpec_getFileName(long j, FileSpec fileSpec) throws PDFException;

    public static final native long FileSpec_getFileRead(long j, FileSpec fileSpec) throws PDFException;

    public static final native long FileSpec_getFileSize(long j, FileSpec fileSpec) throws PDFException;

    public static final native DateTime FileSpec_getModifiedDateTime(long j, FileSpec fileSpec) throws PDFException;

    public static final native boolean FileSpec_isEmbedded(long j, FileSpec fileSpec) throws PDFException;

    public static final native void FileSpec_setChecksum(long j, FileSpec fileSpec, byte[] bArr) throws PDFException;

    public static final native void FileSpec_setCreationDateTime(long j, FileSpec fileSpec, DateTime dateTime) throws PDFException;

    public static final native void FileSpec_setDescription(long j, FileSpec fileSpec, String str) throws PDFException;

    public static final native void FileSpec_setFileName(long j, FileSpec fileSpec, String str) throws PDFException;

    public static final native void FileSpec_setModifiedDateTime(long j, FileSpec fileSpec, DateTime dateTime) throws PDFException;

    public static final native long Font_create(String str, long j, int i, int i2) throws PDFException;

    public static final native long Font_createStandard(int i) throws PDFException;

    public static final native String Font_getName(long j, Font font) throws PDFException;

    public static final native void Font_release(long j, Font font) throws PDFException;

    public static final native int Library_getModuleRight(int i) throws PDFException;

    public static final native String Library_getVersion() throws PDFException;

    public static final native int Library_init(String str, String str2) throws PDFException;

    public static final native boolean Library_registerDefaultSignatureHandler() throws PDFException;

    public static final native boolean Library_registerSecurityCallback(String str, a aVar) throws PDFException;

    public static final native int Library_reinit() throws PDFException;

    public static final native void Library_release() throws PDFException;

    public static final native boolean Library_setActionHandler(ActionHandler actionHandler) throws PDFException;

    public static final native boolean Library_setAnnotIconProvider(AnnotIconProvider annotIconProvider) throws PDFException;

    public static final native boolean Library_setNotifier(Notifier notifier) throws PDFException;

    public static final native boolean Library_unregisterSecurityCallback(String str) throws PDFException;

    public static final native void PDFPath_clear(long j, PDFPath pDFPath) throws PDFException;

    public static final native boolean PDFPath_closeFigure(long j, PDFPath pDFPath) throws PDFException;

    public static final native long PDFPath_create() throws PDFException;

    public static final native boolean PDFPath_cubicBezierTo(long j, PDFPath pDFPath, PointF pointF, PointF pointF2, PointF pointF3) throws PDFException;

    public static final native PointF PDFPath_getPoint(long j, PDFPath pDFPath, int i) throws PDFException;

    public static final native int PDFPath_getPointCount(long j, PDFPath pDFPath) throws PDFException;

    public static final native int PDFPath_getPointType(long j, PDFPath pDFPath, int i) throws PDFException;

    public static final native boolean PDFPath_lineTo(long j, PDFPath pDFPath, PointF pointF) throws PDFException;

    public static final native boolean PDFPath_moveTo(long j, PDFPath pDFPath, PointF pointF) throws PDFException;

    public static final native void PDFPath_release(long j, PDFPath pDFPath) throws PDFException;

    public static final native boolean PDFPath_removePoint(long j, PDFPath pDFPath, int i) throws PDFException;

    public static final native boolean PDFPath_setPoint(long j, PDFPath pDFPath, int i, PointF pointF, int i2) throws PDFException;

    public static final native void Pause_release(Pause pause) throws PDFException;

    public static final native void delete_FileSpec(long j) throws PDFException;

    public static final native void delete_Font(long j) throws PDFException;

    public static final native void delete_PDFPath(long j) throws PDFException;
}
