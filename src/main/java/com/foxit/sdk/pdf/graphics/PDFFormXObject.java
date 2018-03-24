package com.foxit.sdk.pdf.graphics;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFGraphicsObjects;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.objects.PDFStream;

public class PDFFormXObject extends PDFGraphicsObject {
    private transient long a;

    protected PDFFormXObject(long j, boolean z) {
        super(GraphicsObjectsJNI.PDFFormXObject_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFFormXObject pDFFormXObject) {
        return pDFFormXObject == null ? 0 : pDFFormXObject.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                GraphicsObjectsJNI.delete_PDFFormXObject(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public static PDFFormXObject create(PDFDoc pDFDoc) throws PDFException {
        long PDFFormXObject_create = GraphicsObjectsJNI.PDFFormXObject_create(PDFGraphicsObject.getObjectHandle(pDFDoc), pDFDoc);
        return PDFFormXObject_create == 0 ? null : new PDFFormXObject(PDFFormXObject_create, false);
    }

    public PDFStream getStream() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFFormXObject_getStream = GraphicsObjectsJNI.PDFFormXObject_getStream(this.a, this);
        return PDFFormXObject_getStream == 0 ? null : (PDFStream) a.a(PDFStream.class, PDFFormXObject_getStream, false);
    }

    public PDFGraphicsObjects getPageObjects() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFFormXObject_getGraphicsObjects = GraphicsObjectsJNI.PDFFormXObject_getGraphicsObjects(this.a, this);
        return PDFFormXObject_getGraphicsObjects == 0 ? null : (PDFGraphicsObjects) a.a(PDFGraphicsObjects.class, PDFFormXObject_getGraphicsObjects, false);
    }

    public boolean importPageContent(PDFPage pDFPage, boolean z) throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFFormXObject_importPageContent(this.a, this, PDFGraphicsObject.getObjectHandle(pDFPage), pDFPage, z);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }
}
