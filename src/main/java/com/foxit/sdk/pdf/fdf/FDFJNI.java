package com.foxit.sdk.pdf.fdf;

import com.foxit.sdk.common.FileRead;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.annots.Annot;

public class FDFJNI {
    public static final native long FDFDoc_create(int i);

    public static final native boolean FDFDoc_exportAllAnnotsToPDFDoc(long j, FDFDoc fDFDoc, long j2, PDFDoc pDFDoc) throws PDFException;

    public static final native String FDFDoc_getPDFPath(long j, FDFDoc fDFDoc) throws PDFException;

    public static final native boolean FDFDoc_importAllAnnotsFromPDFDoc(long j, FDFDoc fDFDoc, long j2, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean FDFDoc_importAnnotFromPDFDoc(long j, FDFDoc fDFDoc, long j2, Annot annot) throws PDFException;

    public static final native long FDFDoc_loadFromFilePath(String str) throws PDFException;

    public static final native long FDFDoc_loadFromHandler(FileRead fileRead) throws PDFException;

    public static final native long FDFDoc_loadFromMemory(byte[] bArr) throws PDFException;

    public static final native void FDFDoc_release(long j, FDFDoc fDFDoc) throws PDFException;

    public static final native boolean FDFDoc_saveAs(long j, FDFDoc fDFDoc, String str) throws PDFException;

    public static final native boolean FDFDoc_setPDFPath(long j, FDFDoc fDFDoc, String str) throws PDFException;

    public static final native void delete_FDFDoc(long j) throws PDFException;
}
