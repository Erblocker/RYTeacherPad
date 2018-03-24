package com.foxit.sdk.pdf.graphics;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.objects.PDFDictionary;

public class PDFMarkedContent {
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected PDFMarkedContent(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFMarkedContent pDFMarkedContent) {
        return pDFMarkedContent == null ? 0 : pDFMarkedContent.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                GraphicsObjectsJNI.delete_PDFMarkedContent(this.a);
            }
            this.a = 0;
        }
    }

    public boolean hasTag(String str) throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFMarkedContent_hasTag(this.a, this, str);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public int getItemCount() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFMarkedContent_getItemCount(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public String getItemTagName(int i) throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFMarkedContent_getItemTagName(this.a, this, i);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public int getItemMCID(int i) throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFMarkedContent_getItemMCID(this.a, this, i);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public PDFDictionary getItemPropertyDict(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFMarkedContent_getItemPropertyDict = GraphicsObjectsJNI.PDFMarkedContent_getItemPropertyDict(this.a, this, i);
        return PDFMarkedContent_getItemPropertyDict == 0 ? null : (PDFDictionary) a.a(PDFDictionary.class, PDFMarkedContent_getItemPropertyDict, false);
    }

    public int addItem(String str, PDFDictionary pDFDictionary) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        return GraphicsObjectsJNI.PDFMarkedContent_addItem(this.a, this, str, PDFGraphicsObject.getObjectHandle(pDFDictionary), pDFDictionary);
    }

    public boolean removeItem(String str) throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFMarkedContent_removeItem(this.a, this, str);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }
}
