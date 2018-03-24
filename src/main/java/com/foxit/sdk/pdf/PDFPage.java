package com.foxit.sdk.pdf;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.graphics.PDFGraphicsObject;
import com.foxit.sdk.pdf.objects.PDFDictionary;
import com.foxit.sdk.pdf.signature.Signature;
import java.util.Enumeration;
import java.util.Hashtable;

public class PDFPage extends PDFGraphicsObjects {
    public static final int e_calcContentsBox = 0;
    public static final int e_calcDetection = 1;
    public static final int e_flattenOptionAll = 0;
    public static final int e_flattenOptionNoAnnot = 1;
    public static final int e_flattenOptionNoFormControl = 2;
    public static final int e_pageArtBox = 3;
    public static final int e_pageBleedBox = 4;
    public static final int e_pageCropBox = 1;
    public static final int e_pageMediaBox = 0;
    public static final int e_pageTrimBox = 2;
    public static final int e_parsePageNormal = 0;
    public static final int e_parsePageTextOnly = 1;
    public static final int e_parseTextOutputHyphen = 2;
    protected Hashtable<Long, Annot> mAnnots = new Hashtable();
    protected PDFDoc mPDFDoc = null;
    protected transient boolean swigCMemOwn;
    protected transient long swigCPtr;

    protected Annot getAnnotFromCache(long j) {
        Annot annot = (Annot) this.mAnnots.get(Long.valueOf(j));
        if (annot == null) {
            return null;
        }
        return annot;
    }

    protected int addAnnotToCache(Annot annot, long j) {
        if (((Annot) this.mAnnots.get(Long.valueOf(j))) != null) {
            return 1;
        }
        this.mAnnots.put(Long.valueOf(j), annot);
        return 0;
    }

    protected int removeAnnotFromCache(long j) throws PDFException {
        if (!this.mAnnots.containsKey(Long.valueOf(j))) {
            return 0;
        }
        this.mAnnots.remove(Long.valueOf(j));
        return 1;
    }

    protected void clearAnnotFromCache() {
        Enumeration keys = this.mAnnots.keys();
        while (keys.hasMoreElements()) {
            try {
                a.a((Annot) this.mAnnots.get((Long) keys.nextElement()), "resetHandle");
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
        this.mAnnots.clear();
    }

    protected PDFPage(long j, boolean z) {
        super(j, z);
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(PDFPage pDFPage) {
        return pDFPage == null ? 0 : pDFPage.swigCPtr;
    }

    protected void resetHandle() {
        this.swigCPtr = 0;
        this.swigCMemOwn = false;
        this.mPDFDoc = null;
        clearAnnotFromCache();
    }

    protected void setDocument(PDFDoc pDFDoc) {
        this.mPDFDoc = pDFDoc;
    }

    public PDFDoc getDocument() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (this.mPDFDoc != null) {
            return this.mPDFDoc;
        } else {
            long PDFPage_getDocument = PDFJNI.PDFPage_getDocument(this.swigCPtr, this);
            if (PDFPage_getDocument == 0) {
                throw new PDFException(4);
            }
            this.mPDFDoc = new PDFDoc(PDFPage_getDocument, false);
            return this.mPDFDoc;
        }
    }

    public PDFDictionary getDict() throws PDFException {
        if (this.swigCPtr != 0) {
            return (PDFDictionary) a.a(PDFDictionary.class, PDFJNI.PDFPage_getDict(this.swigCPtr, this), false);
        }
        throw new PDFException(4);
    }

    public boolean isParsed() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_isParsed(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int startParse(long j, Pause pause, boolean z) throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_startParse(this.swigCPtr, this, j, pause, z);
        }
        throw new PDFException(4);
    }

    public int continueParse() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_continueParse(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getIndex() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_getIndex(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public float getHeight() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_getHeight(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public float getWidth() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_getWidth(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getRotation() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_getRotation(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public Bitmap loadThumbnail() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_loadThumbnail(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public Matrix getDisplayMatrix(int i, int i2, int i3, int i4, int i5) throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_getDisplayMatrix(this.swigCPtr, this, i, i2, i3, i4, i5);
        }
        throw new PDFException(4);
    }

    public RectF calcContentBBox(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (isParsed()) {
            return PDFJNI.PDFPage_calcContentBBox(this.swigCPtr, this, i);
        } else {
            throw new PDFException(12);
        }
    }

    public int getAnnotCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_getAnnotCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public Annot getAnnot(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getAnnotCount()) {
            throw new PDFException(8);
        } else {
            long PDFPage_getAnnot = PDFJNI.PDFPage_getAnnot(this.swigCPtr, this, i);
            if (PDFPage_getAnnot == 0) {
                return null;
            }
            Annot annotFromCache = getAnnotFromCache(PDFPage_getAnnot);
            if (annotFromCache != null) {
                return annotFromCache;
            }
            annotFromCache = (Annot) a.a(Annot.class, "create", new Class[]{Long.TYPE, Integer.TYPE, PDFPage.class}, new Object[]{Long.valueOf(PDFPage_getAnnot), Integer.valueOf(0), this});
            if (annotFromCache == null) {
                return annotFromCache;
            }
            addAnnotToCache(annotFromCache, PDFPage_getAnnot);
            return annotFromCache;
        }
    }

    public Annot getAnnotAtPos(PointF pointF, float f) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (pointF == null || f < 0.0f || f > 30.0f) {
            throw new PDFException(8);
        } else {
            long PDFPage_getAnnotAtPos = PDFJNI.PDFPage_getAnnotAtPos(this.swigCPtr, this, pointF, f);
            if (PDFPage_getAnnotAtPos == 0) {
                return null;
            }
            Annot annotFromCache = getAnnotFromCache(PDFPage_getAnnotAtPos);
            if (annotFromCache != null) {
                return annotFromCache;
            }
            annotFromCache = (Annot) a.a(Annot.class, "create", new Class[]{Long.TYPE, Integer.TYPE, PDFPage.class}, new Object[]{Long.valueOf(PDFPage_getAnnotAtPos), Integer.valueOf(0), this});
            if (annotFromCache == null) {
                return annotFromCache;
            }
            addAnnotToCache(annotFromCache, PDFPage_getAnnotAtPos);
            return annotFromCache;
        }
    }

    public Annot getAnnotAtDevicePos(Matrix matrix, PointF pointF, float f) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (matrix == null || pointF == null || f < 0.0f || f > 30.0f) {
            throw new PDFException(8);
        } else {
            long PDFPage_getAnnotAtDevicePos = PDFJNI.PDFPage_getAnnotAtDevicePos(this.swigCPtr, this, matrix, pointF, f);
            if (PDFPage_getAnnotAtDevicePos == 0) {
                return null;
            }
            Annot annotFromCache = getAnnotFromCache(PDFPage_getAnnotAtDevicePos);
            if (annotFromCache != null) {
                return annotFromCache;
            }
            annotFromCache = (Annot) a.a(Annot.class, "create", new Class[]{Long.TYPE, Integer.TYPE, PDFPage.class}, new Object[]{Long.valueOf(PDFPage_getAnnotAtDevicePos), Integer.valueOf(0), this});
            if (annotFromCache == null) {
                return annotFromCache;
            }
            addAnnotToCache(annotFromCache, PDFPage_getAnnotAtDevicePos);
            return annotFromCache;
        }
    }

    public Annot addAnnot(int i, RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (rectF == null) {
            throw new PDFException(8);
        } else {
            long PDFPage_addAnnot = PDFJNI.PDFPage_addAnnot(this.swigCPtr, this, i, rectF);
            if (PDFPage_addAnnot == 0) {
                return null;
            }
            Annot annot = (Annot) a.a(Annot.class, "create", new Class[]{Long.TYPE, Integer.TYPE, PDFPage.class}, new Object[]{Long.valueOf(PDFPage_addAnnot), Integer.valueOf(i), this});
            if (annot == null) {
                return annot;
            }
            addAnnotToCache(annot, PDFPage_addAnnot);
            return annot;
        }
    }

    public boolean removeAnnot(Annot annot) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (annot == null) {
            throw new PDFException(8);
        } else {
            Long l = (Long) a.a(annot.getClass(), "getCPtr", (Object) annot);
            removeAnnotFromCache(l.longValue());
            boolean PDFPage_removeAnnot = PDFJNI.PDFPage_removeAnnot(this.swigCPtr, this, l.longValue(), annot);
            a.a(annot, "resetHandle");
            return PDFPage_removeAnnot;
        }
    }

    public boolean hasTransparency() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (isParsed()) {
            return PDFJNI.PDFPage_hasTransparency(this.swigCPtr, this);
        } else {
            throw new PDFException(12);
        }
    }

    public boolean flatten(boolean z, long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (j < 0 || (j != 0 && (j & 1) == 0 && (2 & j) == 0)) {
            throw new PDFException(8);
        } else {
            if ((j & 1) != 1) {
                clearAnnotFromCache();
            }
            return PDFJNI.PDFPage_flatten(this.swigCPtr, this, z, j);
        }
    }

    public boolean setAnnotGroup(Markup[] markupArr, int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (markupArr != null && markupArr.length >= 2 && i >= 0 && i < markupArr.length) {
            return PDFJNI.PDFPage_setAnnotGroup(this.swigCPtr, this, markupArr, i);
        } else {
            throw new PDFException(8);
        }
    }

    public Signature addSignature(RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (rectF == null) {
            throw new PDFException(8);
        } else {
            long PDFPage_addSignature = PDFJNI.PDFPage_addSignature(this.swigCPtr, this, rectF);
            return PDFPage_addSignature == 0 ? null : (Signature) a.a(Signature.class, PDFPage_addSignature, false);
        }
    }

    public void setRotation(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        PDFJNI.PDFPage_setRotation(this.swigCPtr, this, i);
    }

    public void setSize(float f, float f2) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        PDFJNI.PDFPage_setSize(this.swigCPtr, this, f, f2);
    }

    public void setBox(int i, RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (rectF == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            PDFJNI.PDFPage_setBox(this.swigCPtr, this, i, rectF);
        }
    }

    public boolean transform(Matrix matrix, boolean z) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (matrix != null) {
            return PDFJNI.PDFPage_transform(this.swigCPtr, this, matrix, z);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public void setClipRect(RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (rectF == null) {
            throw new PDFException(8);
        } else {
            PDFJNI.PDFPage_setClipRect(this.swigCPtr, this, rectF);
        }
    }

    public void setThumbnail(Bitmap bitmap) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (bitmap == null) {
            throw new PDFException(8);
        } else {
            PDFJNI.PDFPage_setThumbnail(this.swigCPtr, this, bitmap);
        }
    }

    public PDFGraphicsObject getGraphicsObjectAtPoint(int i, PointF pointF, float f) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i < 0 || i > 5) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (pointF == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (f < 0.0f) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            long PDFPage_getGraphicsObjectAtPoint = PDFJNI.PDFPage_getGraphicsObjectAtPoint(this.swigCPtr, this, i, pointF, f);
            if (PDFPage_getGraphicsObjectAtPoint == 0) {
                return null;
            }
            PDFGraphicsObject graphicsObjectFromCache = getGraphicsObjectFromCache(Long.valueOf(PDFPage_getGraphicsObjectAtPoint));
            if (graphicsObjectFromCache != null) {
                return graphicsObjectFromCache;
            }
            graphicsObjectFromCache = (PDFGraphicsObject) a.a(PDFGraphicsObject.class, "create", new Class[]{Long.TYPE, Integer.TYPE, PDFGraphicsObjects.class}, new Object[]{Long.valueOf(PDFPage_getGraphicsObjectAtPoint), Integer.valueOf(0), this});
            if (graphicsObjectFromCache == null) {
                return graphicsObjectFromCache;
            }
            addGraphicsToCache(graphicsObjectFromCache, Long.valueOf(PDFPage_getGraphicsObjectAtPoint));
            return graphicsObjectFromCache;
        }
    }

    public boolean generateContent() throws PDFException {
        if (this.swigCPtr != 0) {
            return PDFJNI.PDFPage_generateContent(this.swigCPtr, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public boolean addImageFromFilePath(String str, PointF pointF, float f, float f2, boolean z) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (a(str)) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (a(str, ".tif", false)) {
            throw new PDFException(PDFError.UNSUPPORTED);
        } else if (pointF != null) {
            return PDFJNI.PDFPage_addImageFromFilePath(this.swigCPtr, this, str, pointF, f, f2, z);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    private boolean a(String str) {
        if (str != null) {
            int length = str.length();
            if (length != 0) {
                for (int i = 0; i < length; i++) {
                    if (!Character.isWhitespace(str.charAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return true;
    }

    private boolean a(String str, String str2, boolean z) {
        if (str == null || str2 == null) {
            if (str == null && str2 == null) {
                return true;
            }
            return false;
        } else if (str2.length() > str.length()) {
            return false;
        } else {
            return str.regionMatches(z, str.length() - str2.length(), str2, 0, str2.length());
        }
    }
}
