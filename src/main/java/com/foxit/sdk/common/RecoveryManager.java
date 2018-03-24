package com.foxit.sdk.common;

import com.foxit.sdk.pdf.PDFDoc;

public class RecoveryManager {
    protected static byte[] mBuffer = null;
    protected static String mFilePath = null;
    protected static int mOriginDocType = 0;
    protected static byte[] mPassword = null;
    protected static PDFDoc mPdfDoc = null;
    protected static FileRead mRead = null;

    protected RecoveryManager() {
    }

    public static PDFDoc reloadDoc(PDFDoc pDFDoc) throws PDFException {
        if (pDFDoc == null) {
            throw new PDFException(8);
        }
        if (pDFDoc != null) {
            pDFDoc.release();
        }
        Library.reinit();
        return (PDFDoc) a.a(pDFDoc, "recover");
    }

    protected static void setFilePath(String str) {
        mOriginDocType = 0;
        mFilePath = str;
    }

    protected static void setMemory(byte[] bArr) {
        mOriginDocType = 1;
        mBuffer = bArr;
    }

    protected static void setFileRead(FileRead fileRead) {
        mOriginDocType = 2;
        mRead = fileRead;
    }
}
