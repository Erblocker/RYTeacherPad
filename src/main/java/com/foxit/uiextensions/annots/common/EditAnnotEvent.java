package com.foxit.uiextensions.annots.common;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.annots.AnnotUndoItem;
import com.foxit.uiextensions.utils.Event;

public abstract class EditAnnotEvent extends Event {
    public static final int EVENTTYPE_ADD = 1;
    public static final int EVENTTYPE_DELETE = 3;
    public static final int EVENTTYPE_MODIFY = 2;
    public Annot mAnnot;
    public PDFViewCtrl mPdfViewCtrl;
    public AnnotUndoItem mUndoItem;
    public boolean useOldValue;

    public abstract boolean add();

    public abstract boolean delete();

    public abstract boolean modify();

    protected boolean execute() {
        if (this.mType == 1) {
            return add();
        }
        if (this.mType == 2) {
            return modify();
        }
        if (this.mType == 3) {
            return delete();
        }
        return false;
    }

    public boolean isModifyDocument() {
        return true;
    }
}
