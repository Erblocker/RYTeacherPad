package com.foxit.sdk.pdf;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.action.Destination;
import java.util.Enumeration;
import java.util.Hashtable;

public class Bookmark {
    public static final int e_bookmarkPosFirstChild = 0;
    public static final int e_bookmarkPosFirstSibling = 4;
    public static final int e_bookmarkPosLastChild = 1;
    public static final int e_bookmarkPosLastSibling = 5;
    public static final int e_bookmarkPosNextSibling = 3;
    public static final int e_bookmarkPosPrevSibling = 2;
    public static final int e_bookmarkStyleBold = 2;
    public static final int e_bookmarkStyleItalic = 1;
    public static final int e_bookmarkStyleNormal = 0;
    private transient long a;
    private Bookmark b = null;
    protected Hashtable<Long, Bookmark> mChildren = new Hashtable();
    protected transient boolean swigCMemOwn;

    private void a(Bookmark bookmark) {
        this.mChildren.put(Long.valueOf(bookmark.a), bookmark);
        bookmark.b = this;
    }

    private void b(Bookmark bookmark) {
        this.mChildren.remove(Long.valueOf(bookmark.a));
        bookmark.b = null;
    }

    private void a() {
        Enumeration keys = this.mChildren.keys();
        while (keys.hasMoreElements()) {
            Bookmark bookmark = (Bookmark) this.mChildren.get(keys.nextElement());
            bookmark.a();
            b(bookmark);
            bookmark.a = 0;
        }
    }

    protected Bookmark(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(Bookmark bookmark) {
        return bookmark == null ? 0 : bookmark.a;
    }

    protected synchronized void resetHandle() {
        if (this.b != null) {
            this.b.b(this);
        }
        a();
        this.a = 0;
    }

    public Bookmark getParent() throws PDFException {
        if (this.a != 0) {
            return this.b;
        }
        throw new PDFException(4);
    }

    public boolean hasChild() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.Bookmark_getFirstChild(this.a, this) != 0;
        } else {
            throw new PDFException(4);
        }
    }

    public Bookmark getFirstChild() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long Bookmark_getFirstChild = PDFJNI.Bookmark_getFirstChild(this.a, this);
        if (Bookmark_getFirstChild == 0) {
            return null;
        }
        if (this.mChildren.containsKey(Long.valueOf(Bookmark_getFirstChild))) {
            return (Bookmark) this.mChildren.get(Long.valueOf(Bookmark_getFirstChild));
        }
        Bookmark bookmark = new Bookmark(Bookmark_getFirstChild, false);
        a(bookmark);
        return bookmark;
    }

    public Bookmark getNextSibling() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long Bookmark_getNextSibling = PDFJNI.Bookmark_getNextSibling(this.a, this);
        if (Bookmark_getNextSibling == 0) {
            return null;
        }
        if (this.b.mChildren.containsKey(Long.valueOf(Bookmark_getNextSibling))) {
            return (Bookmark) this.b.mChildren.get(Long.valueOf(Bookmark_getNextSibling));
        }
        Bookmark bookmark = new Bookmark(Bookmark_getNextSibling, false);
        this.b.a(bookmark);
        return bookmark;
    }

    public Destination getDestination() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long Bookmark_getDestination = PDFJNI.Bookmark_getDestination(this.a, this);
        return Bookmark_getDestination == 0 ? null : (Destination) a.a(Destination.class, Bookmark_getDestination, false);
    }

    public void setDestination(Destination destination) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (destination == null) {
            throw new PDFException(8);
        } else {
            PDFJNI.Bookmark_setDestination(this.a, this, ((Long) a.a(Destination.class, "getCPtr", (Object) destination)).longValue(), destination);
        }
    }

    public String getTitle() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.Bookmark_getTitle(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setTitle(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() == 0) {
            throw new PDFException(8);
        } else {
            PDFJNI.Bookmark_setTitle(this.a, this, str);
        }
    }

    public long getColor() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.Bookmark_getColor(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setColor(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.Bookmark_setColor(this.a, this, j);
    }

    public long getStyle() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.Bookmark_getStyle(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setStyle(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        PDFJNI.Bookmark_setStyle(this.a, this, j);
    }

    public Bookmark insert(String str, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || str.trim().length() == 0 || a(i)) {
            throw new PDFException(8);
        } else {
            long Bookmark_insert = PDFJNI.Bookmark_insert(this.a, this, str, i);
            if (Bookmark_insert == 0) {
                return null;
            }
            Bookmark bookmark;
            switch (i) {
                case 0:
                case 1:
                    bookmark = new Bookmark(Bookmark_insert, false);
                    a(bookmark);
                    return bookmark;
                case 2:
                case 3:
                case 4:
                case 5:
                    bookmark = new Bookmark(Bookmark_insert, false);
                    this.b.a(bookmark);
                    return bookmark;
                default:
                    return null;
            }
        }
    }

    public boolean moveTo(Bookmark bookmark, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (bookmark == null || a(i)) {
            throw new PDFException(8);
        } else {
            if (!PDFJNI.Bookmark_moveTo(this.a, this, getCPtr(bookmark), bookmark, i)) {
                return false;
            }
            this.b.b(this);
            switch (i) {
                case 0:
                case 1:
                    bookmark.a(this);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    bookmark.b.a(this);
                    break;
            }
            return true;
        }
    }

    private boolean a(int i) {
        return i < 0 || i > 5;
    }
}
