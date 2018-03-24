package com.foxit.sdk.pdf.form;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;

public class FormField {
    public static final int e_formFieldCheckBox = 2;
    public static final int e_formFieldComboBox = 4;
    public static final int e_formFieldFlagButtonNoToggleToOff = 256;
    public static final int e_formFieldFlagButtonRadiosInUnison = 512;
    public static final int e_formFieldFlagChoiseMultiselect = 256;
    public static final int e_formFieldFlagComboEdit = 256;
    public static final int e_formFieldFlagNoExport = 4;
    public static final int e_formFieldFlagReadonly = 1;
    public static final int e_formFieldFlagRequired = 2;
    public static final int e_formFieldFlagTextCombo = 2048;
    public static final int e_formFieldFlagTextDoNotScroll = 1024;
    public static final int e_formFieldFlagTextMultiline = 256;
    public static final int e_formFieldFlagTextPassword = 512;
    public static final int e_formFieldListBox = 5;
    public static final int e_formFieldPushButton = 1;
    public static final int e_formFieldRadioButton = 3;
    public static final int e_formFieldSignature = 7;
    public static final int e_formFieldTextField = 6;
    public static final int e_formFieldUnknownType = 0;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected FormField(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(FormField formField) {
        return formField == null ? 0 : formField.a;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FormsJNI.delete_FormField(this.a);
            }
            this.a = 0;
        }
    }

    public int getType() throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormField_getType(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getFlags() throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormField_getFlags(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getName() throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormField_getName(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getDefaultValue() throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormField_getDefaultValue(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getValue() throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormField_getValue(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getControlCount(PDFPage pDFPage) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage == null) {
            throw new PDFException(8);
        } else {
            return FormsJNI.FormField_getControlCount(this.a, this, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage);
        }
    }

    public FormControl getControl(PDFPage pDFPage, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage == null || i < 0 || i >= getControlCount(pDFPage)) {
            throw new PDFException(8);
        } else {
            long FormField_getControl = FormsJNI.FormField_getControl(this.a, this, ((Long) a.a(PDFPage.class, "getCPtr", pDFPage)).longValue(), pDFPage, i);
            return FormField_getControl == 0 ? null : new FormControl(FormField_getControl, false);
        }
    }

    public boolean reset() throws PDFException {
        if (this.a != 0) {
            return FormsJNI.FormField_reset(this.a, this);
        }
        throw new PDFException(4);
    }
}
