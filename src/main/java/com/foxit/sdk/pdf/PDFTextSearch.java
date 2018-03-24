package com.foxit.sdk.pdf;

import android.graphics.RectF;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;

public class PDFTextSearch {
    public static final int e_searchConsecutive = 4;
    public static final int e_searchMatchCase = 1;
    public static final int e_searchMatchWholeWord = 2;
    public static final int e_searchNormal = 0;
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected PDFTextSearch(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFTextSearch pDFTextSearch) {
        return pDFTextSearch == null ? 0 : pDFTextSearch.a;
    }

    private synchronized void a() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.PDFTextSearch_release(this.a, this);
            }
            this.a = 0;
        }
    }

    public static PDFTextSearch create(PDFDoc pDFDoc, Pause pause) throws PDFException {
        if (pDFDoc == null) {
            throw new PDFException(8);
        }
        long PDFTextSearch_create = PDFJNI.PDFTextSearch_create(PDFDoc.getCPtr(pDFDoc), pDFDoc, pause);
        if (PDFTextSearch_create != 0) {
            return PDFTextSearch_create == 0 ? null : new PDFTextSearch(PDFTextSearch_create, true);
        } else {
            throw new PDFException(4);
        }
    }

    public void release() throws PDFException {
        a();
    }

    public boolean setKeyWords(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str != null) {
            return PDFJNI.PDFTextSearch_setKeyWords(this.a, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean setStartPage(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0) {
            return PDFJNI.PDFTextSearch_setStartPage(this.a, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean setFlag(long j) throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_setFlag(this.a, this, j);
        }
        throw new PDFException(4);
    }

    public boolean findNext() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_findNext(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean findPrev() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_findPrev(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getMatchRectCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_getMatchRectCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public RectF getMatchRect(int i) throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_getMatchRect(this.a, this, i);
        }
        throw new PDFException(4);
    }

    public int getMatchPageIndex() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_getMatchPageIndex(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getMatchSentence() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_getMatchSentence(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getMatchSentenceStartIndex() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_getMatchSentenceStartIndex(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getMatchStartCharIndex() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_getMatchStartCharIndex(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getMatchEndCharIndex() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFTextSearch_getMatchEndCharIndex(this.a, this);
        }
        throw new PDFException(4);
    }
}
