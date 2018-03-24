package com.foxit.sdk.pdf.form;

import android.graphics.Matrix;
import android.graphics.PointF;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.fdf.FDFDoc;

class FormsJNI {
    public static final native long FormControl_SWIGUpcast(long j) throws PDFException;

    public static final native long FormControl_getField(long j, FormControl formControl) throws PDFException;

    public static final native long FormField_getControl(long j, FormField formField, long j2, PDFPage pDFPage, int i) throws PDFException;

    public static final native int FormField_getControlCount(long j, FormField formField, long j2, PDFPage pDFPage) throws PDFException;

    public static final native String FormField_getDefaultValue(long j, FormField formField) throws PDFException;

    public static final native int FormField_getFlags(long j, FormField formField) throws PDFException;

    public static final native String FormField_getName(long j, FormField formField) throws PDFException;

    public static final native int FormField_getType(long j, FormField formField) throws PDFException;

    public static final native String FormField_getValue(long j, FormField formField) throws PDFException;

    public static final native boolean FormField_reset(long j, FormField formField) throws PDFException;

    public static final native boolean FormFiller_click(long j, long j2, PointF pointF) throws PDFException;

    public static final native long FormFiller_create(long j, FormFillerAssist formFillerAssist) throws PDFException;

    public static final native void FormFiller_highlightFormFields(long j, boolean z) throws PDFException;

    public static final native boolean FormFiller_input(long j, char c) throws PDFException;

    public static final native void FormFiller_release(long j);

    public static final native void FormFiller_render(long j, long j2, Matrix matrix, long j3) throws PDFException;

    public static final native boolean FormFiller_setFocus(long j, FormFiller formFiller, long j2, FormControl formControl) throws PDFException;

    public static final native void FormFiller_setHighlightColor(long j, long j2) throws PDFException;

    public static final native boolean FormFiller_touchDown(long j, long j2, PointF pointF) throws PDFException;

    public static final native boolean FormFiller_touchMove(long j, long j2, PointF pointF) throws PDFException;

    public static final native boolean FormFiller_touchUp(long j, long j2, PointF pointF) throws PDFException;

    public static final native boolean Form_exportToFDFDoc(long j, Form form, long j2, FDFDoc fDFDoc) throws PDFException;

    public static final native boolean Form_exportToXML(long j, Form form, String str) throws PDFException;

    public static final native long Form_getField(long j, Form form, String str, int i) throws PDFException;

    public static final native int Form_getFieldCount(long j, Form form, String str) throws PDFException;

    public static final native long Form_getFormFiller(long j, Form form) throws PDFException;

    public static final native boolean Form_importFromFDFDoc(long j, Form form, long j2, FDFDoc fDFDoc) throws PDFException;

    public static final native boolean Form_importFromXML(long j, Form form, String str) throws PDFException;

    public static final native boolean Form_reset(long j, Form form) throws PDFException;

    public static final native long TimerFunc_callTimerFunc(long j);

    public static final native void delete_Form(long j) throws PDFException;

    public static final native void delete_FormControl(long j) throws PDFException;

    public static final native void delete_FormField(long j) throws PDFException;
}
