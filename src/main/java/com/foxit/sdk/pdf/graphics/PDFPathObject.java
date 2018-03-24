package com.foxit.sdk.pdf.graphics;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;

public class PDFPathObject extends PDFGraphicsObject {
    private transient long a;

    protected PDFPathObject(long j, boolean z) {
        super(GraphicsObjectsJNI.PDFPathObject_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFPathObject pDFPathObject) {
        return pDFPathObject == null ? 0 : pDFPathObject.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                GraphicsObjectsJNI.delete_PDFPathObject(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public static PDFPathObject create() throws PDFException {
        long PDFPathObject_create = GraphicsObjectsJNI.PDFPathObject_create();
        return PDFPathObject_create == 0 ? null : new PDFPathObject(PDFPathObject_create, false);
    }

    public int getFillMode() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFPathObject_getFillMode(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public void setFillMode(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphicsObjectsJNI.PDFPathObject_setFillMode(this.a, this, i);
    }

    public boolean getStrokeState() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFPathObject_getStrokeState(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public void setStrokeState(boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphicsObjectsJNI.PDFPathObject_setStrokeState(this.a, this, z);
    }

    public PDFPath getPathData() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFPathObject_getPathData = GraphicsObjectsJNI.PDFPathObject_getPathData(this.a, this);
        return PDFPathObject_getPathData == 0 ? null : (PDFPath) a.a(PDFPath.class, PDFPathObject_getPathData, false);
    }

    public void setPathData(PDFPath pDFPath) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphicsObjectsJNI.PDFPathObject_setPathData(this.a, this, PDFGraphicsObject.getObjectHandle(pDFPath), pDFPath);
    }
}
