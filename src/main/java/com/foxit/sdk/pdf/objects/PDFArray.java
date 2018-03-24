package com.foxit.sdk.pdf.objects;

import android.graphics.Matrix;
import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;

public class PDFArray extends PDFObject {
    private transient long a;

    protected PDFArray(long j, boolean z) {
        super(ObjectsJNI.PDFArray_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFArray pDFArray) {
        return pDFArray == null ? 0 : pDFArray.a;
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

    public static PDFArray create() throws PDFException {
        long PDFArray_create = ObjectsJNI.PDFArray_create();
        if (PDFArray_create != 0) {
            return PDFArray_create == 0 ? null : new PDFArray(PDFArray_create, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFArray createFromMatrix(Matrix matrix) throws PDFException {
        if (matrix == null) {
            throw new PDFException(8);
        }
        long PDFArray_createFromMatrix = ObjectsJNI.PDFArray_createFromMatrix(matrix);
        if (PDFArray_createFromMatrix != 0) {
            return PDFArray_createFromMatrix == 0 ? null : new PDFArray(PDFArray_createFromMatrix, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFArray createFromRect(RectF rectF) throws PDFException {
        if (rectF == null) {
            throw new PDFException(8);
        }
        long PDFArray_createFromRect = ObjectsJNI.PDFArray_createFromRect(rectF);
        if (PDFArray_createFromRect != 0) {
            return PDFArray_createFromRect == 0 ? null : new PDFArray(PDFArray_createFromRect, true);
        } else {
            throw new PDFException(4);
        }
    }

    public int getElementCount() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFArray_getElementCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public PDFObject getElement(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getElementCount()) {
            throw new PDFException(8);
        } else {
            long PDFArray_getElement = ObjectsJNI.PDFArray_getElement(this.a, this, i);
            return PDFArray_getElement == 0 ? null : PDFObject.create(PDFArray_getElement, 0);
        }
    }

    public void addElement(PDFObject pDFObject) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFObject == null) {
            throw new PDFException(8);
        } else {
            ObjectsJNI.PDFArray_addElement(this.a, this, PDFObject.getCPtr(pDFObject), pDFObject);
        }
    }

    public void insertAt(int i, PDFObject pDFObject) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFObject == null) {
            throw new PDFException(8);
        } else {
            ObjectsJNI.PDFArray_insertAt(this.a, this, i, PDFObject.getCPtr(pDFObject), pDFObject);
        }
    }

    public void setAt(int i, PDFObject pDFObject) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFObject == null || i < 0 || i >= getElementCount()) {
            throw new PDFException(8);
        } else {
            ObjectsJNI.PDFArray_setAt(this.a, this, i, PDFObject.getCPtr(pDFObject), pDFObject);
        }
    }

    public void removeAt(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getElementCount()) {
            throw new PDFException(8);
        } else {
            ObjectsJNI.PDFArray_removeAt(this.a, this, i);
        }
    }
}
