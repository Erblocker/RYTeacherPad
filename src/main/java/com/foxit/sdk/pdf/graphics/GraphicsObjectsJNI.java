package com.foxit.sdk.pdf.graphics;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import com.foxit.sdk.common.GraphState;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.objects.PDFDictionary;

public class GraphicsObjectsJNI {
    public static final native long PDFFormXObject_SWIGUpcast(long j);

    public static final native long PDFFormXObject_create(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFFormXObject_getGraphicsObjects(long j, PDFFormXObject pDFFormXObject) throws PDFException;

    public static final native long PDFFormXObject_getStream(long j, PDFFormXObject pDFFormXObject) throws PDFException;

    public static final native boolean PDFFormXObject_importPageContent(long j, PDFFormXObject pDFFormXObject, long j2, PDFPage pDFPage, boolean z) throws PDFException;

    public static final native boolean PDFGraphicsObject_addClipPath(long j, PDFGraphicsObject pDFGraphicsObject, long j2, PDFPath pDFPath, int i) throws PDFException;

    public static final native boolean PDFGraphicsObject_addClipTextObject(long j, PDFGraphicsObject pDFGraphicsObject, long j2, PDFTextObject pDFTextObject) throws PDFException;

    public static final native boolean PDFGraphicsObject_clearClips(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native long PDFGraphicsObject_clone(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native long PDFGraphicsObject_getClipPath(long j, PDFGraphicsObject pDFGraphicsObject, int i) throws PDFException;

    public static final native int PDFGraphicsObject_getClipPathCount(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native int PDFGraphicsObject_getClipPathFillMode(long j, PDFGraphicsObject pDFGraphicsObject, int i) throws PDFException;

    public static final native RectF PDFGraphicsObject_getClipRect(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native long PDFGraphicsObject_getClipTextObject(long j, PDFGraphicsObject pDFGraphicsObject, int i) throws PDFException;

    public static final native int PDFGraphicsObject_getClipTextObjectCount(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native long PDFGraphicsObject_getFillColor(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native GraphState PDFGraphicsObject_getGraphState(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native long PDFGraphicsObject_getMarkedContent(long j, PDFGraphicsObject pDFGraphicsObject);

    public static final native Matrix PDFGraphicsObject_getMatrix(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native RectF PDFGraphicsObject_getRect(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native long PDFGraphicsObject_getStrokeColor(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native int PDFGraphicsObject_getType(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native boolean PDFGraphicsObject_hasTransparency(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native void PDFGraphicsObject_release(long j, PDFGraphicsObject pDFGraphicsObject) throws PDFException;

    public static final native boolean PDFGraphicsObject_removeClipPath(long j, PDFGraphicsObject pDFGraphicsObject, int i) throws PDFException;

    public static final native boolean PDFGraphicsObject_removeClipTextObject(long j, PDFGraphicsObject pDFGraphicsObject, int i) throws PDFException;

    public static final native void PDFGraphicsObject_setClipRect(long j, PDFGraphicsObject pDFGraphicsObject, RectF rectF) throws PDFException;

    public static final native void PDFGraphicsObject_setFillColor(long j, PDFGraphicsObject pDFGraphicsObject, long j2) throws PDFException;

    public static final native void PDFGraphicsObject_setGraphState(long j, PDFGraphicsObject pDFGraphicsObject, GraphState graphState) throws PDFException;

    public static final native void PDFGraphicsObject_setMatrix(long j, PDFGraphicsObject pDFGraphicsObject, Matrix matrix) throws PDFException;

    public static final native void PDFGraphicsObject_setStrokeColor(long j, PDFGraphicsObject pDFGraphicsObject, long j2) throws PDFException;

    public static final native boolean PDFGraphicsObject_transform(long j, PDFGraphicsObject pDFGraphicsObject, Matrix matrix, boolean z) throws PDFException;

    public static final native long PDFImageObject_SWIGUpcast(long j);

    public static final native Bitmap PDFImageObject_cloneBitmap(long j, PDFImageObject pDFImageObject, long j2, PDFPage pDFPage) throws PDFException;

    public static final native long PDFImageObject_create(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native int PDFImageObject_getColorSpace(long j, PDFImageObject pDFImageObject) throws PDFException;

    public static final native long PDFImageObject_getStream(long j, PDFImageObject pDFImageObject) throws PDFException;

    public static final native void PDFImageObject_setBitmap(long j, PDFImageObject pDFImageObject, Bitmap bitmap, Bitmap bitmap2) throws PDFException;

    public static final native int PDFMarkedContent_addItem(long j, PDFMarkedContent pDFMarkedContent, String str, long j2, PDFDictionary pDFDictionary) throws PDFException;

    public static final native int PDFMarkedContent_getItemCount(long j, PDFMarkedContent pDFMarkedContent) throws PDFException;

    public static final native int PDFMarkedContent_getItemMCID(long j, PDFMarkedContent pDFMarkedContent, int i) throws PDFException;

    public static final native long PDFMarkedContent_getItemPropertyDict(long j, PDFMarkedContent pDFMarkedContent, int i) throws PDFException;

    public static final native String PDFMarkedContent_getItemTagName(long j, PDFMarkedContent pDFMarkedContent, int i) throws PDFException;

    public static final native boolean PDFMarkedContent_hasTag(long j, PDFMarkedContent pDFMarkedContent, String str) throws PDFException;

    public static final native boolean PDFMarkedContent_removeItem(long j, PDFMarkedContent pDFMarkedContent, String str) throws PDFException;

    public static final native long PDFPathObject_SWIGUpcast(long j);

    public static final native long PDFPathObject_create() throws PDFException;

    public static final native int PDFPathObject_getFillMode(long j, PDFPathObject pDFPathObject) throws PDFException;

    public static final native long PDFPathObject_getPathData(long j, PDFPathObject pDFPathObject) throws PDFException;

    public static final native boolean PDFPathObject_getStrokeState(long j, PDFPathObject pDFPathObject) throws PDFException;

    public static final native void PDFPathObject_setFillMode(long j, PDFPathObject pDFPathObject, int i) throws PDFException;

    public static final native void PDFPathObject_setPathData(long j, PDFPathObject pDFPathObject, long j2, PDFPath pDFPath) throws PDFException;

    public static final native void PDFPathObject_setStrokeState(long j, PDFPathObject pDFPathObject, boolean z) throws PDFException;

    public static final native long PDFShadingObject_SWIGUpcast(long j);

    public static final native long PDFShadingObject_getPDFObject(long j, PDFShadingObject pDFShadingObject) throws PDFException;

    public static final native long PDFTextObject_SWIGUpcast(long j);

    public static final native long PDFTextObject_create();

    public static final native String PDFTextObject_getText(long j, PDFTextObject pDFTextObject) throws PDFException;

    public static final native PDFTextState PDFTextObject_getTextState(long j, PDFTextObject pDFTextObject, long j2, PDFPage pDFPage);

    public static final native void PDFTextObject_setText(long j, PDFTextObject pDFTextObject, String str) throws PDFException;

    public static final native void PDFTextObject_setTextState(long j, PDFTextObject pDFTextObject, long j2, PDFPage pDFPage, PDFTextState pDFTextState, boolean z, int i) throws PDFException;

    public static final native void delete_PDFFormXObject(long j) throws PDFException;

    public static final native void delete_PDFGraphicsObject(long j) throws PDFException;

    public static final native void delete_PDFImageObject(long j) throws PDFException;

    public static final native void delete_PDFMarkedContent(long j) throws PDFException;

    public static final native void delete_PDFPathObject(long j) throws PDFException;

    public static final native void delete_PDFShadingObject(long j) throws PDFException;

    public static final native void delete_PDFTextObject(long j) throws PDFException;
}
