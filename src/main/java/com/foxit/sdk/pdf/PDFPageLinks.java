package com.foxit.sdk.pdf;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Link;

public class PDFPageLinks {
    private transient long a;
    protected PDFPage mPage = null;
    protected transient boolean swigCMemOwn;

    protected PDFPageLinks(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFPageLinks pDFPageLinks) {
        return pDFPageLinks == null ? 0 : pDFPageLinks.a;
    }

    private synchronized void a() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.PDFPageLinks_release(this.a, this);
            }
            this.a = 0;
            this.mPage = null;
        }
    }

    public static PDFPageLinks create(PDFPage pDFPage) throws PDFException {
        if (pDFPage == null) {
            throw new PDFException(8);
        } else if (pDFPage.isParsed()) {
            long PDFPageLinks_create = PDFJNI.PDFPageLinks_create(PDFPage.getCPtr(pDFPage), pDFPage);
            if (PDFPageLinks_create == 0) {
                return null;
            }
            PDFPageLinks pDFPageLinks = new PDFPageLinks(PDFPageLinks_create, false);
            pDFPageLinks.mPage = pDFPage;
            return pDFPageLinks;
        } else {
            throw new PDFException(12);
        }
    }

    public void release() throws PDFException {
        a();
    }

    public int getTextLinkCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFPageLinks_getTextLinkCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public PDFTextLink getTextLink(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFPageLinks_getTextLink = PDFJNI.PDFPageLinks_getTextLink(this.a, this, i);
        return PDFPageLinks_getTextLink == 0 ? null : new PDFTextLink(PDFPageLinks_getTextLink, false);
    }

    public int getLinkAnnotCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFPageLinks_getLinkAnnotCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public Link getLinkAnnot(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getLinkAnnotCount()) {
            throw new PDFException(8);
        } else {
            long PDFPageLinks_getLinkAnnot = PDFJNI.PDFPageLinks_getLinkAnnot(this.a, this, i);
            if (PDFPageLinks_getLinkAnnot == 0) {
                return null;
            }
            synchronized (this.mPage.mAnnots) {
                Annot annotFromCache = this.mPage.getAnnotFromCache(PDFPageLinks_getLinkAnnot);
                Link link;
                if (annotFromCache != null) {
                    link = (Link) annotFromCache;
                    return link;
                }
                link = (Link) a.a(Link.class, PDFPageLinks_getLinkAnnot, false);
                this.mPage.addAnnotToCache(link, PDFPageLinks_getLinkAnnot);
                link = link;
                return link;
            }
        }
    }
}
