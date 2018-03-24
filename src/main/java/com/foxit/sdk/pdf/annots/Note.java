package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;

public class Note extends Markup {
    private transient long swigCPtr;

    protected Note(long j, boolean z) {
        super(AnnotationsJNI.Note_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Note note) {
        return note == null ? 0 : note.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Note(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    public boolean getOpenStatus() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Note_getOpenStatus(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setOpenStatus(boolean z) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Note_setOpenStatus(this.swigCPtr, this, z);
    }

    public String getIconName() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Note_getIconName(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setIconName(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Note_setIconName(this.swigCPtr, this, str);
        }
    }

    public Markup getReplyTo() throws PDFException {
        if (this.swigCPtr != 0) {
            return getMarkupByHandler(AnnotationsJNI.Note_getReplyTo(this.swigCPtr, this));
        }
        throw new PDFException(4);
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Note_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public boolean isStateAnnot() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Note_isStateAnnot(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getStateModel() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Note_getStateModel(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getState() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Note_getState(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setState(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (isValidState(getStateModel(), i)) {
            AnnotationsJNI.Note_setState(this.swigCPtr, this, i);
        } else {
            throw new PDFException(8);
        }
    }
}
