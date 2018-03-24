package com.foxit.sdk.pdf.objects;

import android.graphics.Matrix;
import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;

public class PDFObject {
    public static final int e_objArray = 5;
    public static final int e_objBoolean = 1;
    public static final int e_objDictionary = 6;
    public static final int e_objInvalidType = 0;
    public static final int e_objName = 4;
    public static final int e_objNull = 8;
    public static final int e_objNumber = 2;
    public static final int e_objReference = 9;
    public static final int e_objStream = 7;
    public static final int e_objString = 3;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected PDFObject(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFObject pDFObject) {
        return pDFObject == null ? 0 : pDFObject.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                ObjectsJNI.PDFObject_release(this.a, this);
            }
            this.a = 0;
        }
    }

    public static PDFObject createFromBoolean(boolean z) throws PDFException {
        long PDFObject_createFromBoolean = ObjectsJNI.PDFObject_createFromBoolean(z);
        if (PDFObject_createFromBoolean != 0) {
            return PDFObject_createFromBoolean == 0 ? null : new PDFObject(PDFObject_createFromBoolean, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFObject createFromFloat(float f) throws PDFException {
        long PDFObject_createFromFloat = ObjectsJNI.PDFObject_createFromFloat(f);
        if (PDFObject_createFromFloat != 0) {
            return PDFObject_createFromFloat == 0 ? null : new PDFObject(PDFObject_createFromFloat, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFObject createFromInteger(int i) throws PDFException {
        long PDFObject_createFromInteger = ObjectsJNI.PDFObject_createFromInteger(i);
        if (PDFObject_createFromInteger != 0) {
            return PDFObject_createFromInteger == 0 ? null : new PDFObject(PDFObject_createFromInteger, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFObject createFromString(String str) throws PDFException {
        long PDFObject_createFromString = ObjectsJNI.PDFObject_createFromString(str);
        if (PDFObject_createFromString != 0) {
            return PDFObject_createFromString == 0 ? null : new PDFObject(PDFObject_createFromString, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFObject createFromName(String str) throws PDFException {
        long PDFObject_createFromName = ObjectsJNI.PDFObject_createFromName(str);
        if (PDFObject_createFromName != 0) {
            return PDFObject_createFromName == 0 ? null : new PDFObject(PDFObject_createFromName, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFObject createFromDateTime(DateTime dateTime) throws PDFException {
        if (dateTime == null) {
            throw new PDFException(8);
        }
        long longValue = Long.valueOf(ObjectsJNI.PDFObject_createFromDateTime(dateTime)).longValue();
        if (longValue != 0) {
            return longValue == 0 ? null : new PDFObject(longValue, true);
        } else {
            throw new PDFException(4);
        }
    }

    public static PDFObject createReference(PDFDoc pDFDoc, long j) throws PDFException {
        if (pDFDoc == null || j < 1 || pDFDoc.getIndirectObject(j) == null) {
            throw new PDFException(8);
        }
        long PDFObject_createReference = ObjectsJNI.PDFObject_createReference(((Long) a.a(PDFDoc.class, "getCPtr", pDFDoc)).longValue(), pDFDoc, j);
        if (PDFObject_createReference != 0) {
            return PDFObject_createReference == 0 ? null : new PDFObject(PDFObject_createReference, true);
        } else {
            throw new PDFException(4);
        }
    }

    public void release() throws PDFException {
        delete();
    }

    protected static PDFObject create(long j, int i) {
        return create(j, i, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static PDFObject create(long j, int i, boolean z) {
        if (j == 0) {
            return null;
        }
        PDFObject pDFArray;
        if (i <= 0 || i > 9) {
            try {
                i = ObjectsJNI.PDFObject_getType(j, null);
                if (i == 0) {
                    return null;
                }
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }
        switch (i) {
            case 5:
                pDFArray = new PDFArray(j, z);
                break;
            case 6:
                pDFArray = new PDFDictionary(j, z);
                break;
            case 7:
                pDFArray = new PDFStream(j, z);
                break;
            default:
                try {
                    pDFArray = new PDFObject(j, z);
                    break;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    pDFArray = null;
                    break;
                }
        }
        return pDFArray;
    }

    public PDFObject cloneObject() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFObject_cloneObject = ObjectsJNI.PDFObject_cloneObject(this.a, this);
        if (PDFObject_cloneObject != 0) {
            return PDFObject_cloneObject == 0 ? null : create(PDFObject_cloneObject, getType(), true);
        } else {
            throw new PDFException(4);
        }
    }

    public int getType() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getType(this.a, this);
        }
        throw new PDFException(4);
    }

    public long getObjNum() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getObjNum(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getInteger() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getInteger(this.a, this);
        }
        throw new PDFException(4);
    }

    public float getFloat() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getFloat(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean getBoolean() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getBoolean(this.a, this);
        }
        throw new PDFException(4);
    }

    public Matrix getMatrix() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getMatrix(this.a, this);
        }
        throw new PDFException(4);
    }

    public RectF getRect() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getRect(this.a, this);
        }
        throw new PDFException(4);
    }

    public PDFObject getDirectObject() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFObject_getDirectObject = ObjectsJNI.PDFObject_getDirectObject(this.a, this);
        return PDFObject_getDirectObject == 0 ? null : create(PDFObject_getDirectObject, 0);
    }

    public DateTime getDateTime() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getDateTime(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getString() throws PDFException {
        if (this.a != 0) {
            return ObjectsJNI.PDFObject_getString(this.a, this);
        }
        throw new PDFException(4);
    }
}
