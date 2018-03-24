package com.foxit.sdk.pdf.action;

import com.foxit.sdk.common.PDFException;

public class GotoAction extends Action {
    private transient long a;

    protected GotoAction(long j, boolean z) {
        super(ActionsJNI.GotoAction_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(GotoAction gotoAction) {
        return gotoAction == null ? 0 : gotoAction.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                ActionsJNI.delete_GotoAction(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public Destination getDestination() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long GotoAction_getDestination = ActionsJNI.GotoAction_getDestination(this.a, this);
        return GotoAction_getDestination == 0 ? null : new Destination(GotoAction_getDestination, false);
    }

    public void setDestination(Destination destination) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        ActionsJNI.GotoAction_setDestination(this.a, this, Destination.getCPtr(destination), destination);
    }
}
