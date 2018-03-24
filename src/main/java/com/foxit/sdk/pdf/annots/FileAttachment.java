package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.FileSpec;
import com.foxit.sdk.common.PDFException;

public class FileAttachment extends Markup {
    private transient long swigCPtr;

    protected FileAttachment(long j, boolean z) {
        super(AnnotationsJNI.FileAttachment_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(FileAttachment fileAttachment) {
        return fileAttachment == null ? 0 : fileAttachment.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_FileAttachment(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FileAttachment_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public boolean setFileSpec(FileSpec fileSpec) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (fileSpec == null) {
            throw new PDFException(8);
        } else {
            return AnnotationsJNI.FileAttachment_setFileSpec(this.swigCPtr, this, ((Long) a.a(FileSpec.class, "getCPtr", (Object) fileSpec)).longValue(), fileSpec);
        }
    }

    public FileSpec getFileSpec() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        long FileAttachment_getFileSpec = AnnotationsJNI.FileAttachment_getFileSpec(this.swigCPtr, this);
        return FileAttachment_getFileSpec == 0 ? null : (FileSpec) a.a(FileSpec.class, FileAttachment_getFileSpec, false);
    }

    public String getIconName() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.FileAttachment_getIconName(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setIconName(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() < 1) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.FileAttachment_setIconName(this.swigCPtr, this, str);
        }
    }
}
