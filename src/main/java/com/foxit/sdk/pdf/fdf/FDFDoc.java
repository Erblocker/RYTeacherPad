package com.foxit.sdk.pdf.fdf;

import com.foxit.sdk.common.FileRead;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.annots.Annot;

public class FDFDoc {
    public static final int e_fdfDocTypeFDF = 0;
    public static final int e_fdfDocTypeXFDF = 1;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected FDFDoc(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(FDFDoc fDFDoc) {
        return fDFDoc == null ? 0 : fDFDoc.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FDFJNI.delete_FDFDoc(this.a);
            }
            this.a = 0;
        }
    }

    public static FDFDoc create(int i) throws PDFException {
        if (i == 0 || i == 1) {
            long FDFDoc_create = FDFJNI.FDFDoc_create(i);
            return FDFDoc_create == 0 ? null : new FDFDoc(FDFDoc_create, false);
        } else {
            throw new PDFException(8);
        }
    }

    public static FDFDoc loadFromFilePath(String str) throws PDFException {
        if (str == null || str.trim().length() == 0) {
            throw new PDFException(8);
        }
        long FDFDoc_loadFromFilePath = FDFJNI.FDFDoc_loadFromFilePath(str);
        return FDFDoc_loadFromFilePath == 0 ? null : new FDFDoc(FDFDoc_loadFromFilePath, false);
    }

    public static FDFDoc loadFromMemory(byte[] bArr) throws PDFException {
        if (bArr == null) {
            throw new PDFException(8);
        }
        long FDFDoc_loadFromMemory = FDFJNI.FDFDoc_loadFromMemory(bArr);
        return FDFDoc_loadFromMemory == 0 ? null : new FDFDoc(FDFDoc_loadFromMemory, false);
    }

    public static FDFDoc loadFromHandler(FileRead fileRead) throws PDFException {
        if (fileRead == null) {
            throw new PDFException(8);
        }
        long FDFDoc_loadFromHandler = FDFJNI.FDFDoc_loadFromHandler(fileRead);
        return FDFDoc_loadFromHandler == 0 ? null : new FDFDoc(FDFDoc_loadFromHandler, false);
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        FDFJNI.FDFDoc_release(this.a, this);
        this.a = 0;
    }

    public String getPDFPath() throws PDFException {
        if (this.a != 0) {
            return FDFJNI.FDFDoc_getPDFPath(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean setPDFPath(String str) throws PDFException {
        if (this.a != 0) {
            return FDFJNI.FDFDoc_setPDFPath(this.a, this, str);
        }
        throw new PDFException(4);
    }

    public boolean saveAs(String str) throws PDFException {
        if (this.a != 0) {
            return FDFJNI.FDFDoc_saveAs(this.a, this, str);
        }
        throw new PDFException(4);
    }

    public boolean importAnnotFromPDFDoc(Annot annot) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (annot == null) {
            throw new PDFException(8);
        } else {
            return FDFJNI.FDFDoc_importAnnotFromPDFDoc(this.a, this, ((Long) a.a(annot.getClass(), "getCPtr", annot)).longValue(), annot);
        }
    }

    public boolean importAllAnnotsFromPDFDoc(PDFDoc pDFDoc) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFDoc == null) {
            throw new PDFException(8);
        } else {
            return FDFJNI.FDFDoc_importAllAnnotsFromPDFDoc(this.a, this, ((Long) a.a(pDFDoc.getClass(), "getCPtr", pDFDoc)).longValue(), pDFDoc);
        }
    }

    public boolean exportAllAnnotsToPDFDoc(PDFDoc pDFDoc) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFDoc == null) {
            throw new PDFException(8);
        } else {
            return FDFJNI.FDFDoc_exportAllAnnotsToPDFDoc(this.a, this, ((Long) a.a(pDFDoc.getClass(), "getCPtr", pDFDoc)).longValue(), pDFDoc);
        }
    }
}
