package com.foxit.sdk.pdf.annots;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.form.FormControl;
import com.foxit.sdk.pdf.form.FormField;
import com.foxit.sdk.pdf.objects.PDFDictionary;
import com.foxit.sdk.pdf.signature.Signature;
import java.util.Hashtable;

public class Annot {
    public static final int e_annot3D = 25;
    public static final int e_annotCaret = 14;
    public static final int e_annotCircle = 6;
    public static final int e_annotFileAttachment = 17;
    public static final int e_annotFlagHidden = 2;
    public static final int e_annotFlagInvisible = 1;
    public static final int e_annotFlagLocked = 128;
    public static final int e_annotFlagLockedContents = 512;
    public static final int e_annotFlagNoRotate = 16;
    public static final int e_annotFlagNoView = 32;
    public static final int e_annotFlagNoZoom = 8;
    public static final int e_annotFlagPrint = 4;
    public static final int e_annotFlagReadOnly = 64;
    public static final int e_annotFlagToggleNoView = 256;
    public static final int e_annotFreeText = 3;
    public static final int e_annotHighlight = 9;
    public static final int e_annotInk = 15;
    public static final int e_annotLine = 4;
    public static final int e_annotLink = 2;
    public static final int e_annotMovie = 19;
    public static final int e_annotNote = 1;
    public static final int e_annotPSInk = 16;
    public static final int e_annotPolyLine = 8;
    public static final int e_annotPolygon = 7;
    public static final int e_annotPopup = 26;
    public static final int e_annotPrinterMark = 22;
    public static final int e_annotPropertyBorderColor = 2;
    public static final int e_annotPropertyCreationDate = 1;
    public static final int e_annotPropertyFillColor = 3;
    public static final int e_annotPropertyModifiedDate = 0;
    public static final int e_annotScreen = 21;
    public static final int e_annotSound = 18;
    public static final int e_annotSquare = 5;
    public static final int e_annotSquiggly = 11;
    public static final int e_annotStamp = 13;
    public static final int e_annotStateAccepted = 3;
    public static final int e_annotStateCancelled = 5;
    public static final int e_annotStateCompleted = 6;
    public static final int e_annotStateMarked = 1;
    public static final int e_annotStateModelMarked = 1;
    public static final int e_annotStateModelReview = 2;
    public static final int e_annotStateNone = 7;
    public static final int e_annotStateRejected = 4;
    public static final int e_annotStateUnmarked = 2;
    public static final int e_annotStrikeOut = 12;
    public static final int e_annotTrapNet = 23;
    public static final int e_annotUnderline = 10;
    public static final int e_annotUnknownType = 0;
    public static final int e_annotWatermark = 24;
    public static final int e_annotWidget = 20;
    public static final int e_borderStyleBeveled = 3;
    public static final int e_borderStyleCloudy = 5;
    public static final int e_borderStyleDashed = 1;
    public static final int e_borderStyleInset = 4;
    public static final int e_borderStyleSolid = 0;
    public static final int e_borderStyleUnderLine = 2;
    public static final int e_highlightingModeInvert = 1;
    public static final int e_highlightingModeNone = 0;
    public static final int e_highlightingModeOutline = 2;
    public static final int e_highlightingModePush = 3;
    public static final int e_highlightingModeToggle = 4;
    protected PDFPage mPDFPage = null;
    protected Hashtable<Long, Annot> mReplyAnnots = new Hashtable();
    protected Hashtable<Long, Annot> mStateAnnots = new Hashtable();
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected Annot getAnnotFromReplyCache(long j) throws PDFException {
        Annot annot = (Annot) this.mReplyAnnots.get(Long.valueOf(j));
        if (annot != null) {
            return annot;
        }
        annot = getNoteByHandler(j);
        if (annot == null) {
            return null;
        }
        this.mReplyAnnots.put(Long.valueOf(j), annot);
        return annot;
    }

    protected Annot getAnnotFromStateCache(long j) throws PDFException {
        Annot annot = (Annot) this.mStateAnnots.get(Long.valueOf(j));
        if (annot != null) {
            return annot;
        }
        annot = getNoteByHandler(j);
        if (annot == null) {
            return null;
        }
        this.mStateAnnots.put(Long.valueOf(j), annot);
        return annot;
    }

    protected int removeAnnotFromReplyCache(long j) throws PDFException {
        if (!this.mReplyAnnots.containsKey(Long.valueOf(j))) {
            return 0;
        }
        this.mReplyAnnots.remove(Long.valueOf(j));
        a.a(this.mPDFPage, "removeAnnotFromCache", new Class[]{Long.TYPE}, new Object[]{Long.valueOf(j)});
        return 1;
    }

    protected Markup getMarkupByHandler(long j) throws PDFException {
        if (j == 0) {
            return null;
        }
        synchronized (this.mPDFPage) {
            int annotCount = this.mPDFPage.getAnnotCount();
            for (int i = 0; i < annotCount; i++) {
                Annot annot = this.mPDFPage.getAnnot(i);
                if (annot.isMarkup() && getCPtr(annot) == j) {
                    Markup markup = (Markup) annot;
                    return markup;
                }
            }
            return null;
        }
    }

    protected Note getNoteByHandler(long j) throws PDFException {
        if (j == 0) {
            return null;
        }
        synchronized (this.mPDFPage) {
            int annotCount = this.mPDFPage.getAnnotCount();
            for (int i = 0; i < annotCount; i++) {
                Annot annot = this.mPDFPage.getAnnot(i);
                if (annot.getType() == 1 && getCPtr(annot) == j) {
                    Note note = (Note) annot;
                    return note;
                }
            }
            return null;
        }
    }

    protected Annot(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(Annot annot) {
        return annot == null ? 0 : annot.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Annot(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        this.mPDFPage = null;
        synchronized (this.mReplyAnnots) {
            this.mReplyAnnots.clear();
        }
        synchronized (this.mStateAnnots) {
            this.mStateAnnots.clear();
        }
    }

    protected static Annot create(long j, int i, PDFPage pDFPage) {
        Annot create = create(j, i);
        if (create == null) {
            return null;
        }
        create.mPDFPage = pDFPage;
        return create;
    }

    protected static Annot create(long j, int i) {
        if (j == 0) {
            return null;
        }
        Annot note;
        Exception exception;
        if (i <= 0 || i > 27) {
            try {
                i = AnnotationsJNI.Annot_getType(j, null);
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }
        switch (i) {
            case 1:
                note = new Note(j, false);
                break;
            case 2:
                note = new Link(j, false);
                break;
            case 3:
                note = new FreeText(j, false);
                break;
            case 4:
                note = new Line(j, false);
                break;
            case 5:
                note = new Square(j, false);
                break;
            case 6:
                note = new Circle(j, false);
                break;
            case 7:
                note = new Polygon(j, false);
                break;
            case 8:
                note = new PolyLine(j, false);
                break;
            case 9:
                note = new Highlight(j, false);
                break;
            case 10:
                note = new Underline(j, false);
                break;
            case 11:
                note = new Squiggly(j, false);
                break;
            case 12:
                note = new StrikeOut(j, false);
                break;
            case 13:
                note = new Stamp(j, false);
                break;
            case 14:
                note = new Caret(j, false);
                break;
            case 15:
                note = new Ink(j, false);
                break;
            case 16:
                note = new PSInk(j, false);
                break;
            case 17:
                note = new FileAttachment(j, false);
                break;
            case 20:
                FormControl formControl = (FormControl) a.a(FormControl.class, j, false);
                if (formControl != null) {
                    FormField field = formControl.getField();
                    if (field != null) {
                        if (field.getType() == 7) {
                            note = (Annot) a.a(Signature.class, j, false);
                            break;
                        }
                    }
                    note = null;
                    break;
                }
                note = null;
                break;
                break;
            case 26:
                note = new Popup(j, false);
                break;
            default:
                try {
                    Annot annot = new Annot(j, false);
                    try {
                        if (!annot.isMarkup()) {
                            note = annot;
                            break;
                        }
                        note = new Markup(j, false);
                        break;
                    } catch (Exception e2) {
                        exception = e2;
                        note = annot;
                        break;
                    }
                } catch (Exception e22) {
                    Exception exception2 = e22;
                    note = null;
                    exception = exception2;
                    break;
                }
        }
        exception.printStackTrace();
        return note;
    }

    public PDFPage getPage() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (this.mPDFPage != null) {
            return this.mPDFPage;
        } else {
            long Annot_getPage = AnnotationsJNI.Annot_getPage(this.swigCPtr, this);
            if (Annot_getPage == 0) {
                return null;
            }
            PDFPage pDFPage = (PDFPage) a.a(PDFPage.class, Annot_getPage, false);
            this.mPDFPage = pDFPage;
            return pDFPage;
        }
    }

    public boolean isMarkup() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_isMarkup(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getType() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getType(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getIndex() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getIndex(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public String getContent() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getContent(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setContent(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Annot_setContent(this.swigCPtr, this, str);
        }
    }

    public DateTime getModifiedDateTime() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getModifiedDateTime(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setModifiedDateTime(DateTime dateTime) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (dateTime == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Annot_setModifiedDateTime(this.swigCPtr, this, dateTime);
        }
    }

    public long getFlags() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getFlags(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setFlags(long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (j < 0) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Annot_setFlags(this.swigCPtr, this, j);
        }
    }

    public String getUniqueID() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getUniqueID(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setUniqueID(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Annot_setUniqueID(this.swigCPtr, this, str);
        }
    }

    public RectF getRect() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getRect(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public boolean move(RectF rectF) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (rectF == null) {
            throw new PDFException(8);
        } else {
            if (getType() == 15) {
                a.a(this, "resetInkListHandler");
            }
            return AnnotationsJNI.Annot_move(this.swigCPtr, this, rectF);
        }
    }

    public BorderInfo getBorderInfo() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getBorderInfo(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setBorderInfo(BorderInfo borderInfo) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (borderInfo == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Annot_setBorderInfo(this.swigCPtr, this, borderInfo);
        }
    }

    public long getBorderColor() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_getBorderColor(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setBorderColor(long j) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        AnnotationsJNI.Annot_setBorderColor(this.swigCPtr, this, j);
    }

    public boolean resetAppearanceStream() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Annot_resetAppearanceStream(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public Rect getDeviceRect(boolean z, Matrix matrix) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (matrix != null) {
            return AnnotationsJNI.Annot_getDeviceRect(this.swigCPtr, this, z, matrix);
        } else {
            throw new PDFException(8);
        }
    }

    public PDFDictionary getDict() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        long Annot_getDict = AnnotationsJNI.Annot_getDict(this.swigCPtr, this);
        return Annot_getDict == 0 ? null : (PDFDictionary) a.a(PDFDictionary.class, Annot_getDict, false);
    }

    public boolean removeProperty(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i <= 3) {
            return AnnotationsJNI.Annot_removeProperty(this.swigCPtr, this, i);
        } else {
            throw new PDFException(8);
        }
    }
}
