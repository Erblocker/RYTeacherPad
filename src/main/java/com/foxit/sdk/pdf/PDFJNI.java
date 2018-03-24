package com.foxit.sdk.pdf;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.FileRead;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;
import com.foxit.sdk.pdf.action.Destination;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.graphics.PDFGraphicsObject;
import com.foxit.sdk.pdf.objects.PDFObject;

class PDFJNI {
    public static final native long Bookmark_getColor(long j, Bookmark bookmark) throws PDFException;

    public static final native long Bookmark_getDestination(long j, Bookmark bookmark) throws PDFException;

    public static final native long Bookmark_getFirstChild(long j, Bookmark bookmark) throws PDFException;

    public static final native long Bookmark_getNextSibling(long j, Bookmark bookmark) throws PDFException;

    public static final native long Bookmark_getStyle(long j, Bookmark bookmark) throws PDFException;

    public static final native String Bookmark_getTitle(long j, Bookmark bookmark) throws PDFException;

    public static final native long Bookmark_insert(long j, Bookmark bookmark, String str, int i) throws PDFException;

    public static final native boolean Bookmark_moveTo(long j, Bookmark bookmark, long j2, Bookmark bookmark2, int i) throws PDFException;

    public static final native void Bookmark_setColor(long j, Bookmark bookmark, long j2) throws PDFException;

    public static final native void Bookmark_setDestination(long j, Bookmark bookmark, long j2, Destination destination) throws PDFException;

    public static final native void Bookmark_setStyle(long j, Bookmark bookmark, long j2) throws PDFException;

    public static final native void Bookmark_setTitle(long j, Bookmark bookmark, String str) throws PDFException;

    public static final native long PDFDoc_addIndirectObject(long j, PDFDoc pDFDoc, long j2, PDFObject pDFObject) throws PDFException;

    public static final native int PDFDoc_checkPassword(long j, PDFDoc pDFDoc, byte[] bArr) throws PDFException;

    public static final native boolean PDFDoc_closePage__SWIG_0(long j, PDFDoc pDFDoc, int i) throws PDFException;

    public static final native int PDFDoc_continueImportPages(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_create() throws PDFException;

    public static final native long PDFDoc_createFirstBookmark(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_createFromFilePath(String str) throws PDFException;

    public static final native long PDFDoc_createFromHandler(FileRead fileRead) throws PDFException;

    public static final native long PDFDoc_createFromMemory(byte[] bArr) throws PDFException;

    public static final native void PDFDoc_deleteIndirectObject(long j, PDFDoc pDFDoc, long j2) throws PDFException;

    public static final native long PDFDoc_getCatalog(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native DateTime PDFDoc_getCreationDateTime(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native int PDFDoc_getDisplayMode(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getEncryptDict(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native int PDFDoc_getEncryptionType(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getFirstBookmark(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getForm(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getIndirectObject(long j, PDFDoc pDFDoc, long j2) throws PDFException;

    public static final native long PDFDoc_getInfo(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native String PDFDoc_getMetadataValue(long j, PDFDoc pDFDoc, String str) throws PDFException;

    public static final native DateTime PDFDoc_getModifiedDateTime(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getPage(long j, PDFDoc pDFDoc, int i) throws PDFException;

    public static final native int PDFDoc_getPageCount(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getPageLabelInfo(long j, PDFDoc pDFDoc, int i) throws PDFException;

    public static final native int PDFDoc_getPageLabelRangeCount(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native int PDFDoc_getPasswordType(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getReadingBookmark(long j, PDFDoc pDFDoc, int i) throws PDFException;

    public static final native int PDFDoc_getReadingBookmarkCount(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getSecurityHandler(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getSignature(long j, PDFDoc pDFDoc, int i) throws PDFException;

    public static final native int PDFDoc_getSignatureCount(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getTrailer(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native long PDFDoc_getUserPermissions(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean PDFDoc_hasForm(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean PDFDoc_hasMetadataKey(long j, PDFDoc pDFDoc, String str) throws PDFException;

    public static final native long PDFDoc_insertPage(long j, PDFDoc pDFDoc, int i) throws PDFException;

    public static final native long PDFDoc_insertReadingBookmark(long j, PDFDoc pDFDoc, int i, String str, int i2) throws PDFException;

    public static final native boolean PDFDoc_isEncrypted(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean PDFDoc_isModified(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean PDFDoc_isXFA(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native int PDFDoc_load(long j, PDFDoc pDFDoc, byte[] bArr) throws PDFException;

    public static final native boolean PDFDoc_movePageTo(long j, PDFDoc pDFDoc, long j2, PDFPage pDFPage, int i) throws PDFException;

    public static final native void PDFDoc_release(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean PDFDoc_removeBookmark(long j, PDFDoc pDFDoc, long j2, Bookmark bookmark) throws PDFException;

    public static final native boolean PDFDoc_removePage(long j, PDFDoc pDFDoc, long j2, PDFPage pDFPage) throws PDFException;

    public static final native boolean PDFDoc_removeReadingBookmark(long j, PDFDoc pDFDoc, long j2, ReadingBookmark readingBookmark) throws PDFException;

    public static final native boolean PDFDoc_removeSecurity(long j, PDFDoc pDFDoc) throws PDFException;

    public static final native boolean PDFDoc_saveAs(long j, PDFDoc pDFDoc, String str, long j2) throws PDFException;

    public static final native boolean PDFDoc_setSecurityHandler(long j, PDFDoc pDFDoc, long j2) throws PDFException;

    public static final native int PDFDoc_startImportPages(long j, PDFDoc pDFDoc, int i, long j2, String str, long j3, PDFDoc pDFDoc2, int[] iArr, Pause pause) throws PDFException;

    public static final native int PDFDoc_startImportPagesFromFilePath(long j, PDFDoc pDFDoc, int i, long j2, String str, String str2, byte[] bArr, int[] iArr, Pause pause) throws PDFException;

    public static final native boolean PDFGraphicsObjects_generateContent(long j, PDFGraphicsObjects pDFGraphicsObjects);

    public static final native long PDFGraphicsObjects_getFirstGraphicsObjectPosition(long j, PDFGraphicsObjects pDFGraphicsObjects, int i) throws PDFException;

    public static final native long PDFGraphicsObjects_getGraphicsObject(long j, PDFGraphicsObjects pDFGraphicsObjects, long j2);

    public static final native long PDFGraphicsObjects_getLastGraphicsObjectPosition(long j, PDFGraphicsObjects pDFGraphicsObjects, int i) throws PDFException;

    public static final native long PDFGraphicsObjects_getNextGraphicsObjectPosition(long j, PDFGraphicsObjects pDFGraphicsObjects, int i, long j2) throws PDFException;

    public static final native long PDFGraphicsObjects_getPrevGraphicsObjectPosition(long j, PDFGraphicsObjects pDFGraphicsObjects, int i, long j2) throws PDFException;

    public static final native long PDFGraphicsObjects_insertGraphicsObject(long j, PDFGraphicsObjects pDFGraphicsObjects, long j2, long j3, PDFGraphicsObject pDFGraphicsObject);

    public static final native boolean PDFGraphicsObjects_removeGraphicsObject(long j, PDFGraphicsObjects pDFGraphicsObjects, long j2, PDFGraphicsObject pDFGraphicsObject);

    public static final native boolean PDFGraphicsObjects_removeGraphicsObjectByPosition(long j, PDFGraphicsObjects pDFGraphicsObjects, long j2);

    public static final native long PDFPageLinks_create(long j, PDFPage pDFPage) throws PDFException;

    public static final native long PDFPageLinks_getLinkAnnot(long j, PDFPageLinks pDFPageLinks, int i) throws PDFException;

    public static final native int PDFPageLinks_getLinkAnnotCount(long j, PDFPageLinks pDFPageLinks) throws PDFException;

    public static final native long PDFPageLinks_getTextLink(long j, PDFPageLinks pDFPageLinks, int i) throws PDFException;

    public static final native int PDFPageLinks_getTextLinkCount(long j, PDFPageLinks pDFPageLinks) throws PDFException;

    public static final native void PDFPageLinks_release(long j, PDFPageLinks pDFPageLinks) throws PDFException;

    public static final native long PDFPage_addAnnot(long j, PDFPage pDFPage, int i, RectF rectF) throws PDFException;

    public static final native boolean PDFPage_addImageFromFilePath(long j, PDFPage pDFPage, String str, PointF pointF, float f, float f2, boolean z);

    public static final native long PDFPage_addSignature(long j, PDFPage pDFPage, RectF rectF) throws PDFException;

    public static final native RectF PDFPage_calcContentBBox(long j, PDFPage pDFPage, int i) throws PDFException;

    public static final native int PDFPage_continueParse(long j, PDFPage pDFPage) throws PDFException;

    public static final native boolean PDFPage_flatten(long j, PDFPage pDFPage, boolean z, long j2) throws PDFException;

    public static final native boolean PDFPage_generateContent(long j, PDFPage pDFPage) throws PDFException;

    public static final native long PDFPage_getAnnot(long j, PDFPage pDFPage, int i) throws PDFException;

    public static final native long PDFPage_getAnnotAtDevicePos(long j, PDFPage pDFPage, Matrix matrix, PointF pointF, float f) throws PDFException;

    public static final native long PDFPage_getAnnotAtPos(long j, PDFPage pDFPage, PointF pointF, float f) throws PDFException;

    public static final native int PDFPage_getAnnotCount(long j, PDFPage pDFPage) throws PDFException;

    public static final native long PDFPage_getDict(long j, PDFPage pDFPage) throws PDFException;

    public static final native Matrix PDFPage_getDisplayMatrix(long j, PDFPage pDFPage, int i, int i2, int i3, int i4, int i5) throws PDFException;

    public static final native long PDFPage_getDocument(long j, PDFPage pDFPage) throws PDFException;

    public static final native long PDFPage_getGraphicsObjectAtPoint(long j, PDFPage pDFPage, int i, PointF pointF, float f) throws PDFException;

    public static final native float PDFPage_getHeight(long j, PDFPage pDFPage) throws PDFException;

    public static final native int PDFPage_getIndex(long j, PDFPage pDFPage) throws PDFException;

    public static final native int PDFPage_getRotation(long j, PDFPage pDFPage) throws PDFException;

    public static final native float PDFPage_getWidth(long j, PDFPage pDFPage) throws PDFException;

    public static final native boolean PDFPage_hasTransparency(long j, PDFPage pDFPage) throws PDFException;

    public static final native boolean PDFPage_isParsed(long j, PDFPage pDFPage) throws PDFException;

    public static final native Bitmap PDFPage_loadThumbnail(long j, PDFPage pDFPage) throws PDFException;

    public static final native boolean PDFPage_removeAnnot(long j, PDFPage pDFPage, long j2, Annot annot) throws PDFException;

    public static final native boolean PDFPage_setAnnotGroup(long j, PDFPage pDFPage, Markup[] markupArr, int i) throws PDFException;

    public static final native void PDFPage_setBox(long j, PDFPage pDFPage, int i, RectF rectF) throws PDFException;

    public static final native void PDFPage_setClipRect(long j, PDFPage pDFPage, RectF rectF) throws PDFException;

    public static final native void PDFPage_setRotation(long j, PDFPage pDFPage, int i) throws PDFException;

    public static final native void PDFPage_setSize(long j, PDFPage pDFPage, float f, float f2) throws PDFException;

    public static final native void PDFPage_setThumbnail(long j, PDFPage pDFPage, Bitmap bitmap) throws PDFException;

    public static final native int PDFPage_startParse(long j, PDFPage pDFPage, long j2, Pause pause, boolean z) throws PDFException;

    public static final native boolean PDFPage_transform(long j, PDFPage pDFPage, Matrix matrix, boolean z) throws PDFException;

    public static final native int PDFTextLink_getEndCharIndex(long j, PDFTextLink pDFTextLink) throws PDFException;

    public static final native RectF PDFTextLink_getRect(long j, PDFTextLink pDFTextLink, int i) throws PDFException;

    public static final native int PDFTextLink_getRectCount(long j, PDFTextLink pDFTextLink) throws PDFException;

    public static final native int PDFTextLink_getStartCharIndex(long j, PDFTextLink pDFTextLink) throws PDFException;

    public static final native String PDFTextLink_getURI(long j, PDFTextLink pDFTextLink) throws PDFException;

    public static final native long PDFTextSearch_create(long j, PDFDoc pDFDoc, Pause pause) throws PDFException;

    public static final native boolean PDFTextSearch_findNext(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native boolean PDFTextSearch_findPrev(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native int PDFTextSearch_getMatchEndCharIndex(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native int PDFTextSearch_getMatchPageIndex(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native RectF PDFTextSearch_getMatchRect(long j, PDFTextSearch pDFTextSearch, int i) throws PDFException;

    public static final native int PDFTextSearch_getMatchRectCount(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native String PDFTextSearch_getMatchSentence(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native int PDFTextSearch_getMatchSentenceStartIndex(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native int PDFTextSearch_getMatchStartCharIndex(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native void PDFTextSearch_release(long j, PDFTextSearch pDFTextSearch) throws PDFException;

    public static final native boolean PDFTextSearch_setFlag(long j, PDFTextSearch pDFTextSearch, long j2) throws PDFException;

    public static final native boolean PDFTextSearch_setKeyWords(long j, PDFTextSearch pDFTextSearch, String str) throws PDFException;

    public static final native boolean PDFTextSearch_setStartPage(long j, PDFTextSearch pDFTextSearch, int i) throws PDFException;

    public static final native long PDFTextSelect_create(long j, PDFPage pDFPage) throws PDFException;

    public static final native int PDFTextSelect_getBaselineRotation(long j, PDFTextSelect pDFTextSelect, int i) throws PDFException;

    public static final native int PDFTextSelect_getCharCount(long j, PDFTextSelect pDFTextSelect) throws PDFException;

    public static final native String PDFTextSelect_getChars(long j, PDFTextSelect pDFTextSelect, int i, int i2) throws PDFException;

    public static final native int PDFTextSelect_getIndexAtPos(long j, PDFTextSelect pDFTextSelect, float f, float f2, float f3) throws PDFException;

    public static final native long PDFTextSelect_getPage(long j, PDFTextSelect pDFTextSelect) throws PDFException;

    public static final native String PDFTextSelect_getTextInRect(long j, PDFTextSelect pDFTextSelect, RectF rectF) throws PDFException;

    public static final native RectF PDFTextSelect_getTextRect(long j, PDFTextSelect pDFTextSelect, int i) throws PDFException;

    public static final native int PDFTextSelect_getTextRectCount(long j, PDFTextSelect pDFTextSelect, int i, int i2) throws PDFException;

    public static final native boolean PDFTextSelect_getWordAtPos(long j, PDFTextSelect pDFTextSelect, float f, float f2, float f3, Integer num, Integer num2) throws PDFException;

    public static final native void PDFTextSelect_release(long j, PDFTextSelect pDFTextSelect) throws PDFException;

    public static final native int PageLabel_firstPageNumber_get(long j, PageLabel pageLabel) throws PDFException;

    public static final native String PageLabel_prefix_get(long j, PageLabel pageLabel) throws PDFException;

    public static final native int PageLabel_start_get(long j, PageLabel pageLabel) throws PDFException;

    public static final native int PageLabel_style_get(long j, PageLabel pageLabel) throws PDFException;

    public static final native DateTime ReadingBookmark_getDateTime(long j, ReadingBookmark readingBookmark, boolean z) throws PDFException;

    public static final native int ReadingBookmark_getPageIndex(long j, ReadingBookmark readingBookmark) throws PDFException;

    public static final native String ReadingBookmark_getTitle(long j, ReadingBookmark readingBookmark) throws PDFException;

    public static final native void ReadingBookmark_setDateTime(long j, ReadingBookmark readingBookmark, DateTime dateTime, boolean z) throws PDFException;

    public static final native void ReadingBookmark_setPageIndex(long j, ReadingBookmark readingBookmark, int i) throws PDFException;

    public static final native void ReadingBookmark_setTitle(long j, ReadingBookmark readingBookmark, String str) throws PDFException;

    public static final native int ReflowPage_continueParse(long j, ReflowPage reflowPage) throws PDFException;

    public static final native long ReflowPage_create(long j, PDFPage pDFPage) throws PDFException;

    public static final native float ReflowPage_getContentHeight(long j, ReflowPage reflowPage) throws PDFException;

    public static final native float ReflowPage_getContentWidth(long j, ReflowPage reflowPage) throws PDFException;

    public static final native Matrix ReflowPage_getDisplayMatrix(long j, ReflowPage reflowPage, float f, float f2) throws PDFException;

    public static final native String ReflowPage_getFocusData(long j, ReflowPage reflowPage, Matrix matrix, PointF pointF) throws PDFException;

    public static final native PointF ReflowPage_getFocusPosition(long j, ReflowPage reflowPage, Matrix matrix, String str) throws PDFException;

    public static final native boolean ReflowPage_isParsed(long j, ReflowPage reflowPage) throws PDFException;

    public static final native void ReflowPage_release(long j, ReflowPage reflowPage) throws PDFException;

    public static final native void ReflowPage_setLineSpace(long j, ReflowPage reflowPage, float f) throws PDFException;

    public static final native void ReflowPage_setParseFlags(long j, ReflowPage reflowPage, long j2) throws PDFException;

    public static final native void ReflowPage_setScreenSize(long j, ReflowPage reflowPage, float f, float f2) throws PDFException;

    public static final native void ReflowPage_setTopSpace(long j, ReflowPage reflowPage, float f) throws PDFException;

    public static final native void ReflowPage_setZoom(long j, ReflowPage reflowPage, int i) throws PDFException;

    public static final native int ReflowPage_startParse(long j, ReflowPage reflowPage, Pause pause) throws PDFException;

    public static final native int Renderer_continueRender(long j, Renderer renderer) throws PDFException;

    public static final native long Renderer_create(Bitmap bitmap, boolean z) throws PDFException;

    public static final native void Renderer_release(long j, Renderer renderer) throws PDFException;

    public static final native boolean Renderer_renderAnnot(long j, Renderer renderer, long j2, Annot annot, Matrix matrix) throws PDFException;

    public static final native void Renderer_setColorMode(long j, Renderer renderer, int i) throws PDFException;

    public static final native void Renderer_setForceHalftone(long j, Renderer renderer, boolean z) throws PDFException;

    public static final native void Renderer_setMappingModeColors(long j, Renderer renderer, long j2, long j3) throws PDFException;

    public static final native void Renderer_setRenderContent(long j, Renderer renderer, long j2) throws PDFException;

    public static final native void Renderer_setTransformAnnotIcon(long j, Renderer renderer, boolean z) throws PDFException;

    public static final native int Renderer_startRender(long j, Renderer renderer, long j2, PDFPage pDFPage, Matrix matrix, Pause pause) throws PDFException;

    public static final native int Renderer_startRenderReflowPage(long j, Renderer renderer, long j2, ReflowPage reflowPage, Matrix matrix, Pause pause) throws PDFException;

    public static final native void delete_PageLabel(long j) throws PDFException;

    public static final native void delete_ReadingBookmark(long j) throws PDFException;

    public static final native void delete_ReflowPage(long j) throws PDFException;

    public static final native long new_PageLabel() throws PDFException;
}
