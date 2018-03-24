package com.foxit.sdk.pdf.graphics;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;

public class PDFTextObject extends PDFGraphicsObject {
    private transient long a;

    protected PDFTextObject(long j, boolean z) {
        super(GraphicsObjectsJNI.PDFTextObject_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFTextObject pDFTextObject) {
        return pDFTextObject == null ? 0 : pDFTextObject.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                GraphicsObjectsJNI.delete_PDFTextObject(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public static PDFTextObject create() throws PDFException {
        long PDFTextObject_create = GraphicsObjectsJNI.PDFTextObject_create();
        return PDFTextObject_create == 0 ? null : new PDFTextObject(PDFTextObject_create, false);
    }

    public String getText() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFTextObject_getText(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public void setText(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphicsObjectsJNI.PDFTextObject_setText(this.a, this, str);
    }

    public PDFTextState getTextState(PDFPage pDFPage) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pDFPage != null) {
            return GraphicsObjectsJNI.PDFTextObject_getTextState(this.a, this, PDFGraphicsObject.getObjectHandle(pDFPage), pDFPage);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public void setTextState(PDFPage pDFPage, PDFTextState pDFTextState, boolean z, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pDFPage == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            a(pDFTextState);
            GraphicsObjectsJNI.PDFTextObject_setTextState(this.a, this, PDFGraphicsObject.getObjectHandle(pDFPage), pDFPage, pDFTextState, z, i);
        }
    }

    private void a(PDFTextState pDFTextState) throws PDFException {
        if (pDFTextState == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (pDFTextState.getTextMode() < 0 || pDFTextState.getTextMode() > 7) {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }
}
