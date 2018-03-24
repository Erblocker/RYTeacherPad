package com.foxit.sdk.pdf.objects;

import com.foxit.sdk.common.PDFException;

public class PDFStream extends PDFObject {
    private transient long a;

    protected PDFStream(long j, boolean z) {
        super(ObjectsJNI.PDFStream_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFStream pDFStream) {
        return pDFStream == null ? 0 : pDFStream.a;
    }

    protected synchronized void delete() throws PDFException {
        super.delete();
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
            }
            this.a = 0;
        }
    }

    public void release() throws PDFException {
        delete();
    }

    public static PDFStream create(PDFDictionary pDFDictionary) throws PDFException {
        if (pDFDictionary == null) {
            throw new PDFException(8);
        }
        long PDFStream_create = ObjectsJNI.PDFStream_create(PDFDictionary.getCPtr(pDFDictionary), pDFDictionary);
        if (PDFStream_create != 0) {
            return PDFStream_create == 0 ? null : new PDFStream(PDFStream_create, true);
        } else {
            throw new PDFException(4);
        }
    }

    public PDFDictionary getDictionary() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFStream_getDictionary = ObjectsJNI.PDFStream_getDictionary(this.a, this);
        return PDFStream_getDictionary == 0 ? null : new PDFDictionary(PDFStream_getDictionary, false);
    }

    public long getDataSize(boolean z) throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFStream_getDataSize(this.a, this, z);
        }
        throw new PDFException(4);
    }

    public byte[] getData(boolean z, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i <= 0) {
            throw new PDFException(8);
        } else {
            byte[] bArr = new byte[i];
            if (bArr == null) {
                throw new PDFException(10);
            } else if (ObjectsJNI.PDFStream_getData(this.a, this, z, bArr)) {
                return bArr;
            } else {
                return null;
            }
        }
    }

    public void setData(byte[] bArr) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (bArr == null) {
            throw new PDFException(8);
        } else {
            ObjectsJNI.PDFStream_setData(this.a, this, bArr);
        }
    }
}
