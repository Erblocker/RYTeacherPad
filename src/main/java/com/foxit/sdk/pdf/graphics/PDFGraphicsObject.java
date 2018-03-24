package com.foxit.sdk.pdf.graphics;

import android.graphics.Matrix;
import android.graphics.RectF;
import com.foxit.sdk.common.GraphState;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;
import com.foxit.sdk.pdf.PDFGraphicsObjects;

public class PDFGraphicsObject {
    public static final int e_fillModeAlternate = 1;
    public static final int e_fillModeNone = 0;
    public static final int e_fillModeWinding = 2;
    public static final int e_graphicsObjTypeAll = 0;
    public static final int e_graphicsObjTypeFormXObject = 5;
    public static final int e_graphicsObjTypeImage = 3;
    public static final int e_graphicsObjTypePath = 2;
    public static final int e_graphicsObjTypeShading = 4;
    public static final int e_graphicsObjTypeText = 1;
    public static final int e_imgColorSpaceCalGray = 4;
    public static final int e_imgColorSpaceCalRGB = 5;
    public static final int e_imgColorSpaceDeviceCMYK = 3;
    public static final int e_imgColorSpaceDeviceGray = 1;
    public static final int e_imgColorSpaceDeviceN = 9;
    public static final int e_imgColorSpaceDeviceRGB = 2;
    public static final int e_imgColorSpaceICCBasedDeviceCMYK = 14;
    public static final int e_imgColorSpaceICCBasedDeviceGray = 12;
    public static final int e_imgColorSpaceICCBasedDeviceRGB = 13;
    public static final int e_imgColorSpaceInvalid = 0;
    public static final int e_imgColorSpaceLab = 6;
    public static final int e_imgColorSpacePattern = 11;
    public static final int e_imgColorSpaceSeparation = 8;
    public static final int e_textModeClip = 7;
    public static final int e_textModeFill = 0;
    public static final int e_textModeFillClip = 4;
    public static final int e_textModeFillStroke = 2;
    public static final int e_textModeFillStrokeClip = 6;
    public static final int e_textModeInvisible = 3;
    public static final int e_textModeStroke = 1;
    public static final int e_textModeStrokeClip = 5;
    private transient long a;
    private PDFGraphicsObjects b;
    protected transient boolean swigCMemOwn;

    protected PDFGraphicsObject(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(PDFGraphicsObject pDFGraphicsObject) {
        return pDFGraphicsObject == null ? 0 : pDFGraphicsObject.a;
    }

    protected synchronized void resetHandle() {
        this.b = null;
        this.a = 0;
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                GraphicsObjectsJNI.delete_PDFGraphicsObject(this.a);
            }
            this.a = 0;
        }
    }

    protected static PDFGraphicsObject create(long j, int i, PDFGraphicsObjects pDFGraphicsObjects) {
        PDFGraphicsObject create = create(j, i);
        if (create == null) {
            return null;
        }
        create.b = pDFGraphicsObjects;
        return create;
    }

    protected static PDFGraphicsObject create(long j, int i) {
        if (j == 0) {
            return null;
        }
        if (i <= 0 || i > 6) {
            try {
                i = GraphicsObjectsJNI.PDFGraphicsObject_getType(j, null);
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }
        switch (i) {
            case 1:
                return new PDFTextObject(j, false);
            case 2:
                return new PDFPathObject(j, false);
            case 3:
                return new PDFImageObject(j, false);
            case 4:
                return new PDFShadingObject(j, false);
            case 5:
                return new PDFFormXObject(j, false);
            default:
                return new PDFGraphicsObject(j, false);
        }
    }

    public int getType() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getType(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public RectF getRect() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getRect(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public boolean hasTransparency() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_hasTransparency(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public long getStrokeColor() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getStrokeColor(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public long getFillColor() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getFillColor(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public void setStrokeColor(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphicsObjectsJNI.PDFGraphicsObject_setStrokeColor(this.a, this, j);
    }

    public void setFillColor(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphicsObjectsJNI.PDFGraphicsObject_setFillColor(this.a, this, j);
    }

    public Matrix getMatrix() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getMatrix(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public void setMatrix(Matrix matrix) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (matrix == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            GraphicsObjectsJNI.PDFGraphicsObject_setMatrix(this.a, this, matrix);
        }
    }

    public boolean transform(Matrix matrix, boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (matrix != null) {
            return GraphicsObjectsJNI.PDFGraphicsObject_transform(this.a, this, matrix, z);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public PDFGraphicsObject cloneGraphicsObject() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFGraphicsObject_clone = GraphicsObjectsJNI.PDFGraphicsObject_clone(this.a, this);
        return PDFGraphicsObject_clone == 0 ? null : new PDFGraphicsObject(PDFGraphicsObject_clone, false);
    }

    public GraphState getGraphState() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphState PDFGraphicsObject_getGraphState = GraphicsObjectsJNI.PDFGraphicsObject_getGraphState(this.a, this);
        if (PDFGraphicsObject_getGraphState.getLineJoin() == -1 && PDFGraphicsObject_getGraphState.getBlendMode() == -1 && PDFGraphicsObject_getGraphState.getLineCap() == -1) {
            return null;
        }
        return PDFGraphicsObject_getGraphState;
    }

    public void setGraphState(GraphState graphState) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        a(graphState);
        GraphicsObjectsJNI.PDFGraphicsObject_setGraphState(this.a, this, graphState);
    }

    private void a(GraphState graphState) throws PDFException {
        if (graphState == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (graphState.getBlendMode() < 0 || graphState.getBlendMode() > 24 || (graphState.getBlendMode() > 11 && graphState.getBlendMode() < 21)) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (graphState.getLineWidth() < 0.0f) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (graphState.getLineJoin() < 0 || graphState.getLineJoin() > 2) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (graphState.getLineCap() < 0 || graphState.getLineCap() > 2) {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public int getClipPathCount() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getClipPathCount(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public PDFPath getClipPath(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i >= getClipPathCount() || i < 0) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            long PDFGraphicsObject_getClipPath = GraphicsObjectsJNI.PDFGraphicsObject_getClipPath(this.a, this, i);
            if (PDFGraphicsObject_getClipPath == 0) {
                return null;
            }
            return (PDFPath) a.a(PDFPath.class, PDFGraphicsObject_getClipPath, false);
        }
    }

    public int getClipPathFillMode(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i < getClipPathCount() && i >= 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getClipPathFillMode(this.a, this, i);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public boolean addClipPath(PDFPath pDFPath, int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pDFPath == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (i >= 0 && i <= 2) {
            return GraphicsObjectsJNI.PDFGraphicsObject_addClipPath(this.a, this, getObjectHandle(pDFPath), pDFPath, i);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public boolean removeClipPath(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i < getClipPathCount() && i >= 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_removeClipPath(this.a, this, i);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public int getClipTextObjectCount() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getClipTextObjectCount(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public PDFTextObject getClipTextObject(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i >= getClipTextObjectCount() || i < 0) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            long PDFGraphicsObject_getClipTextObject = GraphicsObjectsJNI.PDFGraphicsObject_getClipTextObject(this.a, this, i);
            return PDFGraphicsObject_getClipTextObject == 0 ? null : new PDFTextObject(PDFGraphicsObject_getClipTextObject, false);
        }
    }

    public boolean addClipTextObject(PDFTextObject pDFTextObject) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (pDFTextObject != null) {
            return GraphicsObjectsJNI.PDFGraphicsObject_addClipTextObject(this.a, this, PDFTextObject.getCPtr(pDFTextObject), pDFTextObject);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public boolean removeClipTextObject(int i) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (i < getClipTextObjectCount() && i >= 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_removeClipTextObject(this.a, this, i);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public RectF getClipRect() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_getClipRect(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public void setClipRect(RectF rectF) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (rectF == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (rectF.top < rectF.bottom || rectF.left > rectF.right) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            GraphicsObjectsJNI.PDFGraphicsObject_setClipRect(this.a, this, rectF);
        }
    }

    public boolean clearClips() throws PDFException {
        if (this.a != 0) {
            return GraphicsObjectsJNI.PDFGraphicsObject_clearClips(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public PDFMarkedContent getMarkedContent() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        long PDFGraphicsObject_getMarkedContent = GraphicsObjectsJNI.PDFGraphicsObject_getMarkedContent(this.a, this);
        return PDFGraphicsObject_getMarkedContent == 0 ? null : new PDFMarkedContent(PDFGraphicsObject_getMarkedContent, false);
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        GraphicsObjectsJNI.PDFGraphicsObject_release(this.a, this);
        if (this.b != null) {
            a.a(this.b, PDFGraphicsObjects.class, "removeGraphicsFromCache", Long.valueOf(this.a));
        } else {
            resetHandle();
        }
    }

    protected static long getObjectHandle(Object obj) {
        if (obj == null) {
            return 0;
        }
        long longValue;
        try {
            longValue = ((Long) a.a(obj.getClass(), "getCPtr", obj)).longValue();
        } catch (PDFException e) {
            longValue = 0;
        }
        return longValue;
    }
}
