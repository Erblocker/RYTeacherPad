package com.foxit.sdk.pdf.objects;

import com.foxit.sdk.common.PDFException;

public class PDFDictionary extends PDFObject {
    private transient long a;

    protected PDFDictionary(long j, boolean z) {
        super(ObjectsJNI.PDFDictionary_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFDictionary pDFDictionary) {
        return pDFDictionary == null ? 0 : pDFDictionary.a;
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

    public static PDFDictionary create() throws PDFException {
        long PDFDictionary_create = ObjectsJNI.PDFDictionary_create();
        if (PDFDictionary_create != 0) {
            return PDFDictionary_create == 0 ? null : new PDFDictionary(PDFDictionary_create, true);
        } else {
            throw new PDFException(4);
        }
    }

    public boolean hasKey(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str != null) {
            return ObjectsJNI.PDFDictionary_hasKey(this.a, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public PDFObject getElement(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            long PDFDictionary_getElement = ObjectsJNI.PDFDictionary_getElement(this.a, this, str);
            return PDFDictionary_getElement == 0 ? null : PDFObject.create(PDFDictionary_getElement, 0);
        }
    }

    public long moveNext(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (j >= 0) {
            return ObjectsJNI.PDFDictionary_moveNext(this.a, this, j);
        } else {
            throw new PDFException(8);
        }
    }

    public String getKey(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (j > 0) {
            return ObjectsJNI.PDFDictionary_getKey(this.a, this, j);
        } else {
            throw new PDFException(8);
        }
    }

    public PDFObject getValue(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (j <= 0) {
            throw new PDFException(8);
        } else {
            long PDFDictionary_getValue = ObjectsJNI.PDFDictionary_getValue(this.a, this, j);
            return PDFDictionary_getValue == 0 ? null : PDFObject.create(PDFDictionary_getValue, 0);
        }
    }

    public void setAt(String str, PDFObject pDFObject) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || pDFObject == null) {
            throw new PDFException(8);
        } else {
            ObjectsJNI.PDFDictionary_setAt(this.a, this, str, PDFObject.getCPtr(pDFObject), pDFObject);
        }
    }

    public void removeAt(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            ObjectsJNI.PDFDictionary_removeAt(this.a, this, str);
        }
    }
}
