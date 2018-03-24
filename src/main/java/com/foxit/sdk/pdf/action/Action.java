package com.foxit.sdk.pdf.action;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;

public class Action {
    public static final int e_actionTypeGoTo3DView = 18;
    public static final int e_actionTypeGoToE = 3;
    public static final int e_actionTypeGoToR = 2;
    public static final int e_actionTypeGoto = 1;
    public static final int e_actionTypeHide = 9;
    public static final int e_actionTypeImportData = 13;
    public static final int e_actionTypeJavaScript = 14;
    public static final int e_actionTypeLaunch = 4;
    public static final int e_actionTypeMovie = 8;
    public static final int e_actionTypeNamed = 10;
    public static final int e_actionTypeRendition = 16;
    public static final int e_actionTypeResetForm = 12;
    public static final int e_actionTypeSetOCGState = 15;
    public static final int e_actionTypeSound = 7;
    public static final int e_actionTypeSubmitForm = 11;
    public static final int e_actionTypeThread = 5;
    public static final int e_actionTypeTrans = 17;
    public static final int e_actionTypeURI = 6;
    public static final int e_actionTypeUnknown = 0;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected Action(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(Action action) {
        return action == null ? 0 : action.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                ActionsJNI.Action_release(this.a, this);
            }
            this.a = 0;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static Action create(long j, int i) {
        if (j == 0) {
            return null;
        }
        Action gotoAction;
        if (i <= 0 || i > 18) {
            try {
                i = ActionsJNI.Action_getType(j, null);
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }
        switch (i) {
            case 1:
                gotoAction = new GotoAction(j, false);
                break;
            case 6:
                gotoAction = new URIAction(j, false);
                break;
            default:
                try {
                    gotoAction = new Action(j, false);
                    break;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    gotoAction = null;
                    break;
                }
        }
        return gotoAction;
    }

    public static Action create(PDFDoc pDFDoc, int i) throws PDFException {
        if (i < 0 || i > 18 || pDFDoc == null) {
            throw new PDFException(8);
        }
        long Action_create = ActionsJNI.Action_create(((Long) a.a(PDFDoc.class, "getCPtr", pDFDoc)).longValue(), pDFDoc, i);
        if (Action_create == 0) {
            return null;
        }
        switch (i) {
            case 1:
                return new GotoAction(Action_create, false);
            case 6:
                return new URIAction(Action_create, false);
            default:
                return new Action(Action_create, false);
        }
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        ActionsJNI.Action_release(this.a, this);
        this.a = 0;
    }

    public int getType() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Action_getType(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getSubActionCount() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Action_getSubActionCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public Action getSubAction(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getSubActionCount()) {
            throw new PDFException(8);
        } else {
            long Action_getSubAction = ActionsJNI.Action_getSubAction(this.a, this, i);
            return Action_getSubAction == 0 ? null : create(Action_getSubAction, 0);
        }
    }

    public void setSubAction(int i, Action action) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (action == null || i < 0 || i >= getSubActionCount()) {
            throw new PDFException(8);
        } else {
            ActionsJNI.Action_setSubAction(this.a, this, i, getCPtr(action), action);
        }
    }

    public boolean insertSubAction(int i, Action action) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (action == null) {
            throw new PDFException(8);
        } else {
            return ActionsJNI.Action_insertSubAction(this.a, this, i, getCPtr(action), action);
        }
    }

    public boolean removeSubAction(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getSubActionCount()) {
            return ActionsJNI.Action_removeSubAction(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean removeAllSubActions() throws PDFException {
        if (this.a != 0) {
            return ActionsJNI.Action_removeAllSubActions(this.a, this);
        }
        throw new PDFException(4);
    }
}
