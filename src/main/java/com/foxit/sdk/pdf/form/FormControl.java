package com.foxit.sdk.pdf.form;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;

public class FormControl extends Annot {
    private transient long a;

    protected FormControl(long j, boolean z) throws PDFException {
        super(FormsJNI.FormControl_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(FormControl formControl) {
        return formControl == null ? 0 : formControl.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FormsJNI.delete_FormControl(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public FormField getField() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long FormControl_getField = FormsJNI.FormControl_getField(this.a, this);
        return FormControl_getField == 0 ? null : new FormField(FormControl_getField, false);
    }
}
