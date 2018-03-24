package com.foxit.sdk.pdf;

import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;

public class ReadingBookmark {
    private transient long a;
    private PDFDoc b;
    protected transient boolean swigCMemOwn;

    protected ReadingBookmark(long j, boolean z, PDFDoc pDFDoc) {
        this.swigCMemOwn = z;
        this.a = j;
        this.b = pDFDoc;
    }

    protected static long getCPtr(ReadingBookmark readingBookmark) {
        return readingBookmark == null ? 0 : readingBookmark.a;
    }

    protected void resetHandle() throws PDFException {
        this.a = 0;
        this.b = null;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.delete_ReadingBookmark(this.a);
            }
            this.a = 0;
        }
    }

    public String getTitle() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReadingBookmark_getTitle(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setTitle(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            PDFJNI.ReadingBookmark_setTitle(this.a, this, str);
        }
    }

    public int getPageIndex() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReadingBookmark_getPageIndex(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setPageIndex(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= this.b.getPageCount()) {
            throw new PDFException(8);
        } else {
            PDFJNI.ReadingBookmark_setPageIndex(this.a, this, i);
        }
    }

    public DateTime getDateTime(boolean z) throws PDFException {
        if (this.a != 0) {
            return PDFJNI.ReadingBookmark_getDateTime(this.a, this, z);
        }
        throw new PDFException(4);
    }

    public void setDateTime(DateTime dateTime, boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (dateTime == null) {
            throw new PDFException(8);
        } else {
            PDFJNI.ReadingBookmark_setDateTime(this.a, this, dateTime, z);
        }
    }
}
