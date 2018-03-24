package com.foxit.sdk.pdf;

import com.foxit.sdk.common.PDFException;

public class PageLabel {
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected PageLabel(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PageLabel pageLabel) {
        return pageLabel == null ? 0 : pageLabel.a;
    }

    private synchronized void a() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.delete_PageLabel(this.a);
            }
            this.a = 0;
        }
    }

    public void release() throws PDFException {
        a();
    }

    public int getStart() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PageLabel_start_get(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getStyle() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PageLabel_style_get(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getPrefix() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PageLabel_prefix_get(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getFirstPageNumber() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PageLabel_firstPageNumber_get(this.a, this);
        }
        throw new PDFException(4);
    }

    public PageLabel() throws PDFException {
        this(PDFJNI.new_PageLabel(), true);
        if (this.a == 0) {
            throw new PDFException(4);
        }
    }
}
