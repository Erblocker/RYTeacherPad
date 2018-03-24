package com.foxit.sdk.common;

import com.foxit.sdk.pdf.PDFDoc;

public abstract class ActionHandler {
    public void release() {
    }

    public int getCurrentPage(PDFDoc pDFDoc) {
        return 0;
    }

    public void setCurrentPage(PDFDoc pDFDoc, int i) {
    }

    public int getPageRotation(PDFDoc pDFDoc, int i) {
        return 0;
    }

    public void setPageRotation(PDFDoc pDFDoc, int i, int i2) {
    }

    public int alert(String str, String str2, int i, int i2) {
        return 1;
    }

    public IdentityProperties getIdentityProperties() {
        return null;
    }
}
