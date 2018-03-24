package com.foxit.sdk.pdf.annots;

import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.annots.line.LineConstants;
import java.util.Enumeration;

public class Markup extends Annot {
    public static final String LINEENDINGSTYLE_BUTT = "Butt";
    public static final String LINEENDINGSTYLE_CIRCLE = "Circle";
    public static final String LINEENDINGSTYLE_CLOSEDARROW = "ClosedArrow";
    public static final String LINEENDINGSTYLE_DIAMOND = "Diamond";
    public static final String LINEENDINGSTYLE_NONE = "None";
    public static final String LINEENDINGSTYLE_OPENARROW = "OpenArrow";
    public static final String LINEENDINGSTYLE_REVERSECLOSEDARROW = "RClosedArrow";
    public static final String LINEENDINGSTYLE_REVERSEOPENARROW = "ROpenArrow";
    public static final String LINEENDINGSTYLE_SLASH = "Slash";
    public static final String LINEENDINGSTYLE_SQUARE = "Square";
    private transient long swigCPtr;

    protected Markup(long j, boolean z) {
        super(AnnotationsJNI.Markup_SWIGUpcast(j), z);
        this.swigCPtr = j;
    }

    protected static long getCPtr(Markup markup) {
        return markup == null ? 0 : markup.swigCPtr;
    }

    protected synchronized void delete() throws PDFException {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AnnotationsJNI.delete_Markup(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
        super.delete();
    }

    protected synchronized void resetHandle() {
        this.swigCPtr = 0;
        super.resetHandle();
    }

    public Popup getPopup() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        long Markup_getPopup = AnnotationsJNI.Markup_getPopup(this.swigCPtr, this);
        return Markup_getPopup == 0 ? null : new Popup(Markup_getPopup, false);
    }

    public void setPopup(Popup popup) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (popup == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Markup_setPopup(this.swigCPtr, this, Popup.getCPtr(popup), popup);
        }
    }

    public String getTitle() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_getTitle(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setTitle(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Markup_setTitle(this.swigCPtr, this, str);
        }
    }

    public String getSubject() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_getSubject(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setSubject(String str) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Markup_setSubject(this.swigCPtr, this, str);
        }
    }

    public float getOpacity() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_getOpacity(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setOpacity(float f) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (((double) f) < 0.0d || ((double) f) > 1.0d) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Markup_setOpacity(this.swigCPtr, this, f);
        }
    }

    public String getIntent() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_getIntent(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setIntent(String str) throws PDFException {
        Object obj = null;
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            switch (getType()) {
                case 4:
                    if (str.equals(LineConstants.INTENT_LINE_DIMENSION)) {
                        break;
                    }
                case 7:
                    if (str.equals("PolygonDimension")) {
                        break;
                    }
                case 8:
                    if (str.equals("PolyLineDimension")) {
                        break;
                    }
                default:
                    obj = 1;
                    break;
            }
            if (obj == null) {
                throw new PDFException(9);
            }
            AnnotationsJNI.Markup_setIntent(this.swigCPtr, this, str);
        }
    }

    public DateTime getCreationDateTime() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_getCreationDateTime(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public void setCreationDateTime(DateTime dateTime) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (dateTime == null) {
            throw new PDFException(8);
        } else {
            AnnotationsJNI.Markup_setCreationDateTime(this.swigCPtr, this, dateTime);
        }
    }

    public int getReplyCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_getReplyCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public Note getReply(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getReplyCount()) {
            return (Note) getAnnotFromReplyCache(AnnotationsJNI.Markup_getReply(this.swigCPtr, this, i));
        } else {
            throw new PDFException(8);
        }
    }

    public Note addReply() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        long Markup_addReply = AnnotationsJNI.Markup_addReply(this.swigCPtr, this);
        if (Markup_addReply == 0) {
            return null;
        }
        return (Note) getAnnotFromReplyCache(Markup_addReply);
    }

    public boolean removeReply(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i < 0 || i >= getReplyCount()) {
            throw new PDFException(8);
        } else {
            boolean z;
            synchronized (this.mReplyAnnots) {
                long Markup_getReply = AnnotationsJNI.Markup_getReply(this.swigCPtr, this, i);
                if (AnnotationsJNI.Markup_removeReply(this.swigCPtr, this, i)) {
                    Annot annotFromReplyCache = getAnnotFromReplyCache(Markup_getReply);
                    removeAnnotFromReplyCache(Markup_getReply);
                    annotFromReplyCache.resetHandle();
                    z = true;
                } else {
                    z = false;
                }
            }
            return z;
        }
    }

    public boolean removeAllReplies() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        synchronized (this.mReplyAnnots) {
            if (AnnotationsJNI.Markup_removeAllReplies(this.swigCPtr, this)) {
                Enumeration keys = this.mReplyAnnots.keys();
                while (keys.hasMoreElements()) {
                    Annot annot = (Annot) this.mReplyAnnots.get((Long) keys.nextElement());
                    a.a(this.mPDFPage, "removeAnnotFromCache", new Class[]{Long.TYPE}, new Object[]{(Long) keys.nextElement()});
                    annot.resetHandle();
                }
                this.mReplyAnnots.clear();
                return true;
            }
            return false;
        }
    }

    public boolean isGrouped() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_isGrouped(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public Markup getGroupHeader() throws PDFException {
        if (this.swigCPtr != 0) {
            return getMarkupByHandler(AnnotationsJNI.Markup_getGroupHeader(this.swigCPtr, this));
        }
        throw new PDFException(4);
    }

    public int getGroupElementCount() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_getGroupElementCount(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public Markup getGroupElement(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i >= 0 && i < getGroupElementCount()) {
            return getMarkupByHandler(AnnotationsJNI.Markup_getGroupElement(this.swigCPtr, this, i));
        } else {
            throw new PDFException(8);
        }
    }

    public boolean ungroup() throws PDFException {
        if (this.swigCPtr != 0) {
            return AnnotationsJNI.Markup_ungroup(this.swigCPtr, this);
        }
        throw new PDFException(4);
    }

    public int getStateAnnotCount(int i) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i == 1 || i == 2) {
            return AnnotationsJNI.Markup_getStateAnnotCount(this.swigCPtr, this, i);
        } else {
            throw new PDFException(8);
        }
    }

    public Note getStateAnnot(int i, int i2) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (i2 >= 0 && i2 < getStateAnnotCount(i)) {
            return (Note) getAnnotFromStateCache(AnnotationsJNI.Markup_getStateAnnot(this.swigCPtr, this, i, i2));
        } else {
            throw new PDFException(8);
        }
    }

    public Note addStateAnnot(int i, int i2) throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        } else if (isValidState(i, i2)) {
            return (Note) getAnnotFromStateCache(AnnotationsJNI.Markup_addStateAnnot(this.swigCPtr, this, i, i2));
        } else {
            throw new PDFException(8);
        }
    }

    public boolean removeAllStateAnnots() throws PDFException {
        if (this.swigCPtr == 0) {
            throw new PDFException(4);
        }
        synchronized (this.mStateAnnots) {
            if (AnnotationsJNI.Markup_removeAllStateAnnots(this.swigCPtr, this)) {
                Enumeration keys = this.mStateAnnots.keys();
                while (keys.hasMoreElements()) {
                    Annot annot = (Annot) this.mStateAnnots.get((Long) keys.nextElement());
                    a.a(this.mPDFPage, "removeAnnotFromCache", new Class[]{Long.TYPE}, new Object[]{(Long) keys.nextElement()});
                    annot.resetHandle();
                }
                this.mStateAnnots.clear();
                return true;
            }
            return false;
        }
    }

    protected static boolean isValidLineEndingStyle(String str) {
        return str != null && (str.equals(LINEENDINGSTYLE_NONE) || str.equals(LINEENDINGSTYLE_SQUARE) || str.equals(LINEENDINGSTYLE_CIRCLE) || str.equals(LINEENDINGSTYLE_DIAMOND) || str.equals(LINEENDINGSTYLE_OPENARROW) || str.equals(LINEENDINGSTYLE_CLOSEDARROW) || str.equals(LINEENDINGSTYLE_BUTT) || str.equals(LINEENDINGSTYLE_REVERSEOPENARROW) || str.equals(LINEENDINGSTYLE_REVERSECLOSEDARROW) || str.equals(LINEENDINGSTYLE_SLASH));
    }

    protected boolean isValidInnerRect(RectF rectF) throws PDFException {
        RectF rect = getRect();
        rect.contains(rectF);
        return rectF != null && rectF.left < rectF.right && rectF.bottom < rectF.top && rect.left <= rectF.left && rect.bottom <= rectF.bottom && rect.top >= rectF.top && rect.right >= rectF.right;
    }

    protected boolean isValidState(int i, int i2) {
        switch (i) {
            case 1:
                if (!(i2 == 1 || i2 == 2)) {
                    return false;
                }
            case 2:
                if (i2 < 3) {
                    return false;
                }
                if (i2 > 7) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
