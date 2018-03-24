package com.foxit.sdk.pdf.graphics;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.objects.PDFStream;

public class PDFImageObject extends PDFGraphicsObject {
    private transient long a;

    protected PDFImageObject(long j, boolean z) {
        super(GraphicsObjectsJNI.PDFImageObject_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(PDFImageObject pDFImageObject) {
        return pDFImageObject == null ? 0 : pDFImageObject.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                GraphicsObjectsJNI.delete_PDFImageObject(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public static PDFImageObject create(PDFDoc pDFDoc) throws PDFException {
        long PDFImageObject_create = GraphicsObjectsJNI.PDFImageObject_create(PDFGraphicsObject.getObjectHandle(pDFDoc), pDFDoc);
        return PDFImageObject_create == 0 ? null : new PDFImageObject(PDFImageObject_create, false);
    }

    public void setBitmap(Bitmap bitmap, Bitmap bitmap2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (bitmap == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (!bitmap.getConfig().equals(Config.ARGB_8888)) {
            throw new PDFException(PDFError.UNSUPPORTED);
        } else if (bitmap2 == null || bitmap2.getConfig().equals(Config.ALPHA_8)) {
            GraphicsObjectsJNI.PDFImageObject_setBitmap(this.a, this, bitmap, bitmap2);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public Bitmap cloneBitmap(PDFPage pDFPage) throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFImageObject_cloneBitmap(this.a, this, PDFGraphicsObject.getObjectHandle(pDFPage), pDFPage);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public int getColorSpace() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFImageObject_getColorSpace(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public PDFStream getStream() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long longValue = Long.valueOf(GraphicsObjectsJNI.PDFImageObject_getStream(this.a, this)).longValue();
        if (longValue == 0) {
            return null;
        }
        return (PDFStream) a.a(PDFStream.class, longValue, false);
    }
}
