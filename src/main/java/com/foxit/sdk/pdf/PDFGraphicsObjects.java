package com.foxit.sdk.pdf;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.graphics.PDFGraphicsObject;
import java.util.Enumeration;
import java.util.Hashtable;

public class PDFGraphicsObjects {
    private transient long a;
    protected Hashtable<Long, PDFGraphicsObject> mGraphicsObjects = new Hashtable();
    protected transient boolean swigCMemOwn;

    protected PDFGraphicsObject getGraphicsObjectFromCache(Long l) {
        PDFGraphicsObject pDFGraphicsObject = (PDFGraphicsObject) this.mGraphicsObjects.get(l);
        if (pDFGraphicsObject == null) {
            return null;
        }
        return pDFGraphicsObject;
    }

    protected int addGraphicsToCache(PDFGraphicsObject pDFGraphicsObject, Long l) {
        if (((PDFGraphicsObject) this.mGraphicsObjects.get(l)) != null) {
            return 1;
        }
        this.mGraphicsObjects.put(l, pDFGraphicsObject);
        return 0;
    }

    protected int removeGraphicsFromCache(Long l) throws PDFException {
        if (!this.mGraphicsObjects.containsKey(l)) {
            return 0;
        }
        PDFGraphicsObject pDFGraphicsObject = (PDFGraphicsObject) this.mGraphicsObjects.get(l);
        try {
            a.a(pDFGraphicsObject, "resetHandle");
        } catch (PDFException e) {
            e.printStackTrace();
        }
        this.mGraphicsObjects.remove(pDFGraphicsObject);
        return 1;
    }

    protected void clearGraphicsFromCache() {
        Enumeration keys = this.mGraphicsObjects.keys();
        while (keys.hasMoreElements()) {
            try {
                a.a((PDFGraphicsObject) this.mGraphicsObjects.get((Long) keys.nextElement()), "resetHandle");
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
        this.mGraphicsObjects.clear();
    }

    protected PDFGraphicsObjects(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFGraphicsObjects pDFGraphicsObjects) {
        return pDFGraphicsObjects == null ? 0 : pDFGraphicsObjects.a;
    }

    public long getNextGraphicsObjectPosition(int i, long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i < 0 || i > 5) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (j != 0) {
            return PDFJNI.PDFGraphicsObjects_getNextGraphicsObjectPosition(this.a, this, i, j);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public long getPrevGraphicsObjectPosition(int i, long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i < 0 || i > 5) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (j != 0) {
            return PDFJNI.PDFGraphicsObjects_getPrevGraphicsObjectPosition(this.a, this, i, j);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public long getFirstGraphicsObjectPosition(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i >= 0 && i <= 5) {
            return PDFJNI.PDFGraphicsObjects_getFirstGraphicsObjectPosition(this.a, this, i);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public long getLastGraphicsObjectPosition(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i >= 0 && i <= 5) {
            return PDFJNI.PDFGraphicsObjects_getLastGraphicsObjectPosition(this.a, this, i);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public PDFGraphicsObject getGraphicsObject(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (j == 0) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            long PDFGraphicsObjects_getGraphicsObject = PDFJNI.PDFGraphicsObjects_getGraphicsObject(this.a, this, j);
            if (PDFGraphicsObjects_getGraphicsObject == 0) {
                return null;
            }
            PDFGraphicsObject graphicsObjectFromCache = getGraphicsObjectFromCache(Long.valueOf(PDFGraphicsObjects_getGraphicsObject));
            if (graphicsObjectFromCache != null) {
                return graphicsObjectFromCache;
            }
            graphicsObjectFromCache = (PDFGraphicsObject) a.a(PDFGraphicsObject.class, "create", new Class[]{Long.TYPE, Integer.TYPE, PDFGraphicsObjects.class}, new Object[]{Long.valueOf(PDFGraphicsObjects_getGraphicsObject), Integer.valueOf(0), this});
            if (graphicsObjectFromCache == null) {
                return graphicsObjectFromCache;
            }
            addGraphicsToCache(graphicsObjectFromCache, Long.valueOf(PDFGraphicsObjects_getGraphicsObject));
            return graphicsObjectFromCache;
        }
    }

    public long insertGraphicsObject(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pDFGraphicsObject == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            long PDFGraphicsObjects_insertGraphicsObject = PDFJNI.PDFGraphicsObjects_insertGraphicsObject(this.a, this, j, a(pDFGraphicsObject), pDFGraphicsObject);
            if (!(pDFGraphicsObject == null || PDFGraphicsObjects_insertGraphicsObject == 0)) {
                addGraphicsToCache(pDFGraphicsObject, Long.valueOf(a(pDFGraphicsObject)));
            }
            return PDFGraphicsObjects_insertGraphicsObject;
        }
    }

    public boolean removeGraphicsObject(PDFGraphicsObject pDFGraphicsObject) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pDFGraphicsObject == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            long a = a(pDFGraphicsObject);
            boolean PDFGraphicsObjects_removeGraphicsObject = PDFJNI.PDFGraphicsObjects_removeGraphicsObject(this.a, this, a, pDFGraphicsObject);
            if (PDFGraphicsObjects_removeGraphicsObject) {
                removeGraphicsFromCache(Long.valueOf(a));
            }
            a.a(pDFGraphicsObject, "resetHandle");
            return PDFGraphicsObjects_removeGraphicsObject;
        }
    }

    public boolean removeGraphicsObjectByPosition(long j) throws PDFException {
        long j2 = 0;
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (j == 0) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            PDFGraphicsObject graphicsObject = getGraphicsObject(j);
            if (graphicsObject != null) {
                j2 = a(graphicsObject);
            }
            boolean PDFGraphicsObjects_removeGraphicsObjectByPosition = PDFJNI.PDFGraphicsObjects_removeGraphicsObjectByPosition(this.a, this, j);
            if (PDFGraphicsObjects_removeGraphicsObjectByPosition) {
                removeGraphicsFromCache(Long.valueOf(j2));
            }
            return PDFGraphicsObjects_removeGraphicsObjectByPosition;
        }
    }

    public boolean generateContent() throws PDFException {
        if (this.a != 0) {
            return PDFJNI.PDFGraphicsObjects_generateContent(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    private static long a(Object obj) {
        try {
            return ((Long) a.a(obj.getClass(), "getCPtr", obj)).longValue();
        } catch (PDFException e) {
            return 0;
        }
    }
}
