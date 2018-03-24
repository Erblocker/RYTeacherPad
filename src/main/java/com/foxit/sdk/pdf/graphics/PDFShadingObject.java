package com.foxit.sdk.pdf.graphics;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.objects.PDFObject;

public class PDFShadingObject extends PDFGraphicsObject {
    private transient long a;

    protected PDFShadingObject(long j, boolean z) {
        super(GraphicsObjectsJNI.PDFShadingObject_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFShadingObject pDFShadingObject) {
        return pDFShadingObject == null ? 0 : pDFShadingObject.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                GraphicsObjectsJNI.delete_PDFShadingObject(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public PDFObject getPDFObject() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFShadingObject_getPDFObject = GraphicsObjectsJNI.PDFShadingObject_getPDFObject(this.a, this);
        return PDFShadingObject_getPDFObject == 0 ? null : (PDFObject) a.a(PDFObject.class, PDFShadingObject_getPDFObject, false);
    }
}
