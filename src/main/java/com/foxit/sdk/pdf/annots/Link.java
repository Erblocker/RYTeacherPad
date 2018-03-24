package com.foxit.sdk.pdf.annots;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.action.Action;

public class Link extends Annot {
    private transient long swigCPtr;

    protected Link(long j, boolean z) {
        super(AnnotationsJNI.Link_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Link link) {
        return link == null ? 0 : link.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Link(this.swigCPtr);
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
            return AnnotationsJNI.Link_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getQuadPointsCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Link_getQuadPointsCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public QuadPoints getQuadPoints(int i) throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Link_getQuadPoints(this.swigCPtr, this, i);
        }
        throw new PDFException(4);
    }

    public void setQuadPoints(QuadPoints[] quadPointsArr) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Link_setQuadPoints(this.swigCPtr, this, quadPointsArr);
    }

    public int getHighlightingMode() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Link_getHighlightingMode(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setHighlightingMode(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i > 4) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Link_setHighlightingMode(this.swigCPtr, this, i);
        }
    }

    public Action getAction() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        if (AnnotationsJNI.Link_getAction(this.swigCPtr, this) == 0) {
            return null;
        }
        return (Action) a.a(Action.class, "create", new Class[]{Long.TYPE, Integer.TYPE}, new Object[]{Long.valueOf(AnnotationsJNI.Link_getAction(this.swigCPtr, this)), Integer.valueOf(0)});
    }

    public void setAction(Action action) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (action == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Link_setAction(this.swigCPtr, this, ((Long) a.a(action.getClass(), "getCPtr", (Object) action)).longValue(), action);
        }
    }

    public boolean removeAction() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Link_removeAction(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }
}
