package com.foxit.sdk.pdf.form;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.fdf.FDFDoc;
import java.io.File;

public class Form {
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected Form(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(Form form) {
        return form == null ? 0 : form.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FormsJNI.delete_Form(this.a);
            }
            this.a = 0;
        }
    }

    public int getFieldCount(String str) throws PDFException {
        if (this.a != 0) {
            return FormsJNI.Form_getFieldCount(this.a, this, str);
        }
        throw new PDFException(4);
    }

    public FormField getField(String str, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getFieldCount(str)) {
            throw new PDFException(8);
        } else {
            long Form_getField = FormsJNI.Form_getField(this.a, this, str, i);
            return Form_getField == 0 ? null : new FormField(Form_getField, false);
        }
    }

    public FormFiller getFormFiller() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long Form_getFormFiller = FormsJNI.Form_getFormFiller(this.a, this);
        return Form_getFormFiller == 0 ? null : new FormFiller(Form_getFormFiller);
    }

    public boolean reset() throws PDFException {
        if (this.a != 0) {
            return FormsJNI.Form_reset(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean exportToXML(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (a(str)) {
            return FormsJNI.Form_exportToXML(this.a, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean importFromXML(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (!a(str)) {
            throw new PDFException(8);
        } else if (new File(str).exists()) {
            return FormsJNI.Form_importFromXML(this.a, this, str);
        } else {
            throw new PDFException(1);
        }
    }

    public boolean exportToFDFDoc(FDFDoc fDFDoc) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (fDFDoc == null) {
            throw new PDFException(8);
        } else {
            return FormsJNI.Form_exportToFDFDoc(this.a, this, ((Long) a.a(FDFDoc.class, "getCPtr", fDFDoc)).longValue(), fDFDoc);
        }
    }

    public boolean importFromFDFDoc(FDFDoc fDFDoc) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (fDFDoc == null) {
            throw new PDFException(8);
        } else {
            return FormsJNI.Form_importFromFDFDoc(this.a, this, ((Long) a.a(FDFDoc.class, "getCPtr", fDFDoc)).longValue(), fDFDoc);
        }
    }

    private boolean a(String str) {
        if (str == null || str.trim().length() <= 0 || !"xml".equalsIgnoreCase(str.substring(str.lastIndexOf(46) + 1))) {
            return false;
        }
        return true;
    }
}
