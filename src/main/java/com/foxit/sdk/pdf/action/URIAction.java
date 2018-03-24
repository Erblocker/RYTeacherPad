package com.foxit.sdk.pdf.action;

import com.foxit.sdk.common.PDFException;

public class URIAction extends Action {
    private transient long a;

    protected URIAction(long j, boolean z) {
        super(ActionsJNI.URIAction_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(URIAction uRIAction) {
        return uRIAction == null ? 0 : uRIAction.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                ActionsJNI.delete_URIAction(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public String getURI() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.URIAction_getURI(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setURI(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            ActionsJNI.URIAction_setURI(this.a, this, str);
        }
    }

    public boolean isTrackPosition() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.URIAction_isTrackPosition(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setTrackPositionFlag(boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        ActionsJNI.URIAction_setTrackPositionFlag(this.a, this, z);
    }
}
