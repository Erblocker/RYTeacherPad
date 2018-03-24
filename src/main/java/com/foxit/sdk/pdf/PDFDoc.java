package com.foxit.sdk.pdf;

import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.FileRead;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;
import com.foxit.sdk.pdf.form.Form;
import com.foxit.sdk.pdf.objects.PDFDictionary;
import com.foxit.sdk.pdf.objects.PDFObject;
import com.foxit.sdk.pdf.signature.Signature;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

public class PDFDoc {
    public static final int e_encryptCertificate = 2;
    public static final int e_encryptCustom = 4;
    public static final int e_encryptFoxitDRM = 3;
    public static final int e_encryptNone = 0;
    public static final int e_encryptPassword = 1;
    public static final int e_encryptRMS = 5;
    public static final int e_encryptUnknown = -1;
    public static final int e_importFlagNormal = 0;
    public static final int e_importFlagShareStream = 2;
    public static final int e_importFlagWithLayers = 1;
    public static final int e_pageLabelStyleDecimalNums = 1;
    public static final int e_pageLabelStyleLowerLetters = 5;
    public static final int e_pageLabelStyleLowerRomanNums = 3;
    public static final int e_pageLabelStyleNone = 0;
    public static final int e_pageLabelStyleUpperLetters = 4;
    public static final int e_pageLabelStyleUpperRomanNums = 2;
    public static final int e_permAnnotForm = 32;
    public static final int e_permAssemble = 1024;
    public static final int e_permExtract = 16;
    public static final int e_permExtractAccess = 512;
    public static final int e_permFillForm = 256;
    public static final int e_permModify = 8;
    public static final int e_permPrint = 4;
    public static final int e_permPrintHigh = 2048;
    public static final int e_pwdInvalid = 0;
    public static final int e_pwdNoPassword = 1;
    public static final int e_pwdOwner = 3;
    public static final int e_pwdUser = 2;
    public static final int e_saveFlagIncremental = 1;
    public static final int e_saveFlagNoOriginal = 2;
    public static final int e_saveFlagNormal = 0;
    public static final int e_saveFlagObjectStream = 4;
    private transient long a;
    private int b = 0;
    private String c = null;
    private byte[] d = null;
    private byte[] e = null;
    private FileRead f = null;
    private Bookmark g = null;
    private Hashtable<Integer, a> h = new Hashtable();
    private Hashtable<Long, ReadingBookmark> i = new Hashtable();
    protected transient boolean swigCMemOwn;

    protected class a {
        public Integer a = Integer.valueOf(0);
        public PDFPage b = null;
        final /* synthetic */ PDFDoc c;

        protected a(PDFDoc pDFDoc) {
            this.c = pDFDoc;
        }
    }

    protected PDFPage getPageFromCache(int i) {
        a aVar = (a) this.h.get(Integer.valueOf(i));
        if (aVar == null) {
            return null;
        }
        Integer num = aVar.a;
        aVar.a = Integer.valueOf(aVar.a.intValue() + 1);
        return aVar.b;
    }

    protected int addPageToCache(PDFPage pDFPage) throws PDFException {
        int index = pDFPage.getIndex();
        a aVar = (a) this.h.get(Integer.valueOf(index));
        if (aVar != null) {
            Integer num = aVar.a;
            aVar.a = Integer.valueOf(aVar.a.intValue() + 1);
            return aVar.a.intValue();
        }
        aVar = new a(this);
        aVar.b = pDFPage;
        Integer num2 = aVar.a;
        aVar.a = Integer.valueOf(aVar.a.intValue() + 1);
        this.h.put(Integer.valueOf(index), aVar);
        return aVar.a.intValue();
    }

    protected int removePageFromCache(PDFPage pDFPage) throws PDFException {
        int index = pDFPage.getIndex();
        a aVar = (a) this.h.get(Integer.valueOf(index));
        if (aVar == null) {
            return 0;
        }
        Integer num = aVar.a;
        aVar.a = Integer.valueOf(aVar.a.intValue() - 1);
        if (aVar.a.intValue() == 0) {
            this.h.remove(Integer.valueOf(index));
        }
        return aVar.a.intValue();
    }

    protected a removePageFromCache(int i) throws PDFException {
        a aVar = (a) this.h.get(Integer.valueOf(i));
        if (aVar == null) {
            return null;
        }
        Integer num = aVar.a;
        aVar.a = Integer.valueOf(aVar.a.intValue() - 1);
        if (aVar.a.intValue() != 0) {
            return aVar;
        }
        this.h.remove(Integer.valueOf(i));
        return aVar;
    }

    protected PDFDoc(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFDoc pDFDoc) {
        return pDFDoc == null ? 0 : pDFDoc.a;
    }

    private synchronized void a() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PDFJNI.PDFDoc_release(this.a, this);
            }
            this.a = 0;
        }
        this.h.clear();
    }

    private PDFDoc recover() throws PDFException {
        PDFDoc pDFDoc = null;
        switch (this.b) {
            case 0:
                pDFDoc = createFromFilePath(this.c);
                break;
            case 1:
                pDFDoc = createFromMemory(this.e);
                break;
            case 2:
                pDFDoc = createFromHandler(this.f);
                break;
        }
        pDFDoc.load(this.d);
        return pDFDoc;
    }

    public static PDFDoc create() throws PDFException {
        long PDFDoc_create = PDFJNI.PDFDoc_create();
        return PDFDoc_create == 0 ? null : new PDFDoc(PDFDoc_create, false);
    }

    public static PDFDoc createFromFilePath(String str) throws PDFException {
        if (str == null) {
            throw new PDFException(8);
        } else if (new File(str).isFile()) {
            long PDFDoc_createFromFilePath = PDFJNI.PDFDoc_createFromFilePath(str);
            if (PDFDoc_createFromFilePath != 0) {
                PDFDoc pDFDoc = PDFDoc_createFromFilePath == 0 ? null : new PDFDoc(PDFDoc_createFromFilePath, true);
                pDFDoc.c = str;
                pDFDoc.b = 0;
                return pDFDoc;
            } else if (str.substring(str.lastIndexOf(".") + 1).compareToIgnoreCase("pdf") != 0) {
                throw new PDFException(1);
            } else {
                throw new PDFException(4);
            }
        } else {
            throw new PDFException(1);
        }
    }

    public static PDFDoc createFromMemory(byte[] bArr) throws PDFException {
        if (bArr == null) {
            throw new PDFException(8);
        }
        long PDFDoc_createFromMemory = PDFJNI.PDFDoc_createFromMemory(bArr);
        if (PDFDoc_createFromMemory == 0) {
            throw new PDFException(4);
        }
        PDFDoc pDFDoc = PDFDoc_createFromMemory == 0 ? null : new PDFDoc(PDFDoc_createFromMemory, true);
        pDFDoc.e = bArr;
        pDFDoc.b = 1;
        return pDFDoc;
    }

    public static PDFDoc createFromHandler(FileRead fileRead) throws PDFException {
        if (fileRead == null) {
            throw new PDFException(8);
        }
        long PDFDoc_createFromHandler = PDFJNI.PDFDoc_createFromHandler(fileRead);
        if (PDFDoc_createFromHandler == 0) {
            throw new PDFException(4);
        }
        PDFDoc pDFDoc = PDFDoc_createFromHandler == 0 ? null : new PDFDoc(PDFDoc_createFromHandler, true);
        pDFDoc.f = fileRead;
        pDFDoc.b = 2;
        return pDFDoc;
    }

    public void release() throws PDFException {
        synchronized (this.h) {
            Enumeration keys = this.h.keys();
            while (keys.hasMoreElements()) {
                a aVar = (a) this.h.get((Integer) keys.nextElement());
                closePage(aVar.b.getIndex());
                aVar.b = null;
                aVar.a = Integer.valueOf(0);
            }
            this.h.clear();
        }
        synchronized (this.i) {
            keys = this.i.keys();
            while (keys.hasMoreElements()) {
                ((ReadingBookmark) this.i.get((Long) keys.nextElement())).resetHandle();
            }
            this.i.clear();
        }
        a();
    }

    public void load(byte[] bArr) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        int PDFDoc_load = PDFJNI.PDFDoc_load(this.a, this, bArr);
        if (PDFDoc_load != 0) {
            throw new PDFException(PDFDoc_load);
        }
        this.d = bArr;
    }

    public boolean isXFA() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_isXFA(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean isEncrypted() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_isEncrypted(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean isModified() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_isModified(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getPasswordType() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getPasswordType(this.a, this);
        }
        throw new PDFException(4);
    }

    public int getEncryptionType() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getEncryptionType(this.a, this);
        }
        throw new PDFException(4);
    }

    public int checkPassword(byte[] bArr) throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_checkPassword(this.a, this, bArr);
        }
        throw new PDFException(4);
    }

    public boolean saveAs(String str, long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str != null && j >= 0 && j <= 7) {
            return PDFJNI.PDFDoc_saveAs(this.a, this, str, j);
        } else {
            throw new PDFException(8);
        }
    }

    public Bookmark getFirstBookmark() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (this.g != null) {
            return this.g;
        } else {
            long PDFDoc_getFirstBookmark = PDFJNI.PDFDoc_getFirstBookmark(this.a, this);
            if (PDFDoc_getFirstBookmark == 0) {
                return null;
            }
            this.g = new Bookmark(PDFDoc_getFirstBookmark, false);
            return this.g;
        }
    }

    public Bookmark createFirstBookmark() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        if (this.g != null) {
            this.g.resetHandle();
        }
        long PDFDoc_createFirstBookmark = PDFJNI.PDFDoc_createFirstBookmark(this.a, this);
        if (PDFDoc_createFirstBookmark == 0) {
            return null;
        }
        this.g = new Bookmark(PDFDoc_createFirstBookmark, false);
        return this.g;
    }

    public boolean removeBookmark(Bookmark bookmark) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (bookmark == null) {
            throw new PDFException(8);
        } else if (!PDFJNI.PDFDoc_removeBookmark(this.a, this, Bookmark.getCPtr(bookmark), bookmark)) {
            return false;
        } else {
            if (bookmark == this.g) {
                this.g = null;
            }
            bookmark.resetHandle();
            return true;
        }
    }

    public int getPageCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getPageCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public PDFPage getPage(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getPageCount()) {
            throw new PDFException(8);
        } else {
            PDFPage pageFromCache;
            synchronized (this.h) {
                pageFromCache = getPageFromCache(i);
                if (pageFromCache != null) {
                } else {
                    long PDFDoc_getPage = PDFJNI.PDFDoc_getPage(this.a, this, i);
                    if (PDFDoc_getPage == 0) {
                        throw new PDFException(4);
                    }
                    pageFromCache = new PDFPage(PDFDoc_getPage, false);
                    pageFromCache.setDocument(this);
                    addPageToCache(pageFromCache);
                }
            }
            return pageFromCache;
        }
    }

    public boolean closePage(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getPageCount()) {
            throw new PDFException(8);
        } else {
            boolean z;
            synchronized (this.h) {
                a removePageFromCache = removePageFromCache(i);
                if (removePageFromCache == null || removePageFromCache.a.intValue() > 0) {
                    z = true;
                } else {
                    removePageFromCache.b.resetHandle();
                    z = PDFJNI.PDFDoc_closePage__SWIG_0(this.a, this, i);
                }
            }
            return z;
        }
    }

    public int getDisplayMode() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getDisplayMode(this.a, this);
        }
        throw new PDFException(4);
    }

    public PDFDictionary getCatalog() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFDoc_getCatalog = PDFJNI.PDFDoc_getCatalog(this.a, this);
        if (PDFDoc_getCatalog != 0) {
            return PDFDoc_getCatalog == 0 ? null : (PDFDictionary) a.a(PDFDictionary.class, PDFDoc_getCatalog, false);
        } else {
            throw new PDFException(4);
        }
    }

    public PDFDictionary getTrailer() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFDoc_getTrailer = PDFJNI.PDFDoc_getTrailer(this.a, this);
        return PDFDoc_getTrailer == 0 ? null : (PDFDictionary) a.a(PDFDictionary.class, PDFDoc_getTrailer, false);
    }

    public PDFDictionary getInfo() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFDoc_getInfo = PDFJNI.PDFDoc_getInfo(this.a, this);
        return PDFDoc_getInfo == 0 ? null : (PDFDictionary) a.a(PDFDictionary.class, PDFDoc_getInfo, false);
    }

    public PDFDictionary getEncryptDict() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFDoc_getEncryptDict = PDFJNI.PDFDoc_getEncryptDict(this.a, this);
        return PDFDoc_getEncryptDict == 0 ? null : (PDFDictionary) a.a(PDFDictionary.class, PDFDoc_getEncryptDict, false);
    }

    public PDFObject getIndirectObject(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (j < 0) {
            throw new PDFException(8);
        } else {
            return PDFJNI.PDFDoc_getIndirectObject(this.a, this, j) == 0 ? null : (PDFObject) a.a(PDFObject.class, "create", new Class[]{Long.TYPE, Integer.TYPE}, new Object[]{Long.valueOf(PDFJNI.PDFDoc_getIndirectObject(this.a, this, j)), Integer.valueOf(0)});
        }
    }

    public long addIndirectObject(PDFObject pDFObject) throws PDFException {
        if (pDFObject == null) {
            throw new PDFException(8);
        } else if (this.a == 0) {
            throw new PDFException(4);
        } else {
            return PDFJNI.PDFDoc_addIndirectObject(this.a, this, ((Long) a.a(pDFObject.getClass(), "getCPtr", (Object) pDFObject)).longValue(), pDFObject);
        }
    }

    public void deleteIndirectObject(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.PDFDoc_deleteIndirectObject(this.a, this, j);
    }

    public long getUserPermissions() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getUserPermissions(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean hasMetadataKey(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str != null) {
            return PDFJNI.PDFDoc_hasMetadataKey(this.a, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public DateTime getCreationDateTime() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getCreationDateTime(this.a, this);
        }
        throw new PDFException(4);
    }

    public DateTime getModifiedDateTime() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getModifiedDateTime(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getMetadataValue(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str != null) {
            return PDFJNI.PDFDoc_getMetadataValue(this.a, this, str);
        } else {
            throw new PDFException(8);
        }
    }

    public int getPageLabelRangeCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getPageLabelRangeCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public PageLabel getPageLabelInfo(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getPageLabelRangeCount()) {
            return new PageLabel(PDFJNI.PDFDoc_getPageLabelInfo(this.a, this, i), true);
        } else {
            throw new PDFException(8);
        }
    }

    public boolean hasForm() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_hasForm(this.a, this);
        }
        throw new PDFException(4);
    }

    public Form getForm() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long PDFDoc_getForm = PDFJNI.PDFDoc_getForm(this.a, this);
        if (PDFDoc_getForm == 0) {
            return null;
        }
        return (Form) a.a(Form.class, PDFDoc_getForm, false);
    }

    public int getReadingBookmarkCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getReadingBookmarkCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public ReadingBookmark getReadingBookmark(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getReadingBookmarkCount()) {
            throw new PDFException(8);
        } else {
            long PDFDoc_getReadingBookmark = PDFJNI.PDFDoc_getReadingBookmark(this.a, this, i);
            if (this.i.containsKey(Long.valueOf(PDFDoc_getReadingBookmark))) {
                return (ReadingBookmark) this.i.get(Long.valueOf(PDFDoc_getReadingBookmark));
            }
            ReadingBookmark readingBookmark = new ReadingBookmark(PDFDoc_getReadingBookmark, false, this);
            this.i.put(Long.valueOf(PDFDoc_getReadingBookmark), readingBookmark);
            return readingBookmark;
        }
    }

    public ReadingBookmark insertReadingBookmark(int i, String str, int i2) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || i2 < 0 || i2 >= getPageCount()) {
            throw new PDFException(8);
        } else {
            long PDFDoc_insertReadingBookmark = PDFJNI.PDFDoc_insertReadingBookmark(this.a, this, i, str, i2);
            ReadingBookmark readingBookmark = new ReadingBookmark(PDFDoc_insertReadingBookmark, false, this);
            this.i.put(Long.valueOf(PDFDoc_insertReadingBookmark), readingBookmark);
            return readingBookmark;
        }
    }

    public boolean removeReadingBookmark(ReadingBookmark readingBookmark) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (readingBookmark == null) {
            throw new PDFException(8);
        } else {
            long cPtr = ReadingBookmark.getCPtr(readingBookmark);
            if (!PDFJNI.PDFDoc_removeReadingBookmark(this.a, this, ReadingBookmark.getCPtr(readingBookmark), readingBookmark)) {
                return false;
            }
            this.i.remove(Long.valueOf(cPtr));
            readingBookmark.resetHandle();
            return true;
        }
    }

    public int getSignatureCount() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_getSignatureCount(this.a, this);
        }
        throw new PDFException(4);
    }

    public Signature getSignature(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getSignatureCount()) {
            throw new PDFException(8);
        } else {
            long PDFDoc_getSignature = PDFJNI.PDFDoc_getSignature(this.a, this, i);
            return PDFDoc_getSignature == 0 ? null : (Signature) a.a(Signature.class, PDFDoc_getSignature, false);
        }
    }

    public PDFPage insertPage(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFPage pDFPage = new PDFPage(PDFJNI.PDFDoc_insertPage(this.a, this, i), false);
        pDFPage.setDocument(this);
        synchronized (this.h) {
            addPageToCache(pDFPage);
            b();
        }
        return pDFPage;
    }

    public boolean removePage(PDFPage pDFPage) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage == null) {
            throw new PDFException(8);
        } else {
            this.h.remove(Integer.valueOf(pDFPage.getIndex()));
            boolean PDFDoc_removePage = PDFJNI.PDFDoc_removePage(this.a, this, PDFPage.getCPtr(pDFPage), pDFPage);
            pDFPage.resetHandle();
            if (!PDFDoc_removePage) {
                return false;
            }
            synchronized (this.h) {
                b();
            }
            return true;
        }
    }

    public boolean movePageTo(PDFPage pDFPage, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (pDFPage == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (i < 0 || i >= getPageCount()) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (!PDFJNI.PDFDoc_movePageTo(this.a, this, PDFPage.getCPtr(pDFPage), pDFPage, i)) {
            return false;
        } else {
            synchronized (this.h) {
                b();
            }
            return true;
        }
    }

    public int startImportPagesFromFilePath(int i, long j, String str, String str2, byte[] bArr, int[] iArr, Pause pause) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (j < 0) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (str2 == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            try {
                int PDFDoc_startImportPagesFromFilePath = PDFJNI.PDFDoc_startImportPagesFromFilePath(this.a, this, i, j, str, str2, bArr, iArr, pause);
                if (PDFDoc_startImportPagesFromFilePath == 2) {
                    synchronized (this.h) {
                        b();
                    }
                }
                return PDFDoc_startImportPagesFromFilePath;
            } catch (PDFException e) {
                if (e.getLastError() == PDFError.PARAM_INVALID.getCode() || e.getLastError() == PDFError.PARAM_INVALID.getCode()) {
                    throw new PDFException(PDFError.PARAM_INVALID);
                }
                throw e;
            }
        }
    }

    public int startImportPages(int i, long j, String str, PDFDoc pDFDoc, int[] iArr, Pause pause) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (j < 0) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (pDFDoc == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            try {
                int PDFDoc_startImportPages = PDFJNI.PDFDoc_startImportPages(this.a, this, i, j, str, getCPtr(pDFDoc), pDFDoc, iArr, pause);
                if (PDFDoc_startImportPages == 2) {
                    synchronized (this.h) {
                        b();
                    }
                }
                return PDFDoc_startImportPages;
            } catch (PDFException e) {
                if (e.getLastError() == PDFError.PARAM_INVALID.getCode()) {
                    throw new PDFException(PDFError.PARAM_INVALID);
                }
                throw e;
            }
        }
    }

    public int continueImportPages() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        int PDFDoc_continueImportPages = PDFJNI.PDFDoc_continueImportPages(this.a, this);
        if (PDFDoc_continueImportPages == 2) {
            synchronized (this.h) {
                b();
            }
        }
        return PDFDoc_continueImportPages;
    }

    private void b() throws PDFException {
        Enumeration keys = this.h.keys();
        Hashtable hashtable = new Hashtable();
        while (keys.hasMoreElements()) {
            a aVar = (a) this.h.get((Integer) keys.nextElement());
            hashtable.put(Integer.valueOf(aVar.b.getIndex()), aVar);
        }
        this.h.clear();
        Enumeration keys2 = hashtable.keys();
        while (keys2.hasMoreElements()) {
            Integer num = (Integer) keys2.nextElement();
            this.h.put(num, (a) hashtable.get(num));
        }
        hashtable.clear();
    }

    public boolean setSecurityHandler(SecurityHandler securityHandler) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (securityHandler != null) {
            return PDFJNI.PDFDoc_setSecurityHandler(this.a, this, SecurityHandler.getCPtr(securityHandler));
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public SecurityHandler getSecurityHandler() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFDoc_getSecurityHandler = PDFJNI.PDFDoc_getSecurityHandler(this.a, this);
        return PDFDoc_getSecurityHandler == 0 ? null : new SecurityHandler(PDFDoc_getSecurityHandler, false);
    }

    public boolean removeSecurity() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFDoc_removeSecurity(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }
}
