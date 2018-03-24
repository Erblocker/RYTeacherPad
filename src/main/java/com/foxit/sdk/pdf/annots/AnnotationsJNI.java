package com.foxit.sdk.pdf.annots;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.DefaultAppearance;
import com.foxit.sdk.common.FileSpec;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;
import com.foxit.sdk.pdf.action.Action;

class AnnotationsJNI {
    public static final native long Annot_getBorderColor(long j, Annot annot) throws PDFException;

    public static final native BorderInfo Annot_getBorderInfo(long j, Annot annot) throws PDFException;

    public static final native String Annot_getContent(long j, Annot annot) throws PDFException;

    public static final native Rect Annot_getDeviceRect(long j, Annot annot, boolean z, Matrix matrix) throws PDFException;

    public static final native long Annot_getDict(long j, Annot annot) throws PDFException;

    public static final native long Annot_getFlags(long j, Annot annot) throws PDFException;

    public static final native int Annot_getIndex(long j, Annot annot) throws PDFException;

    public static final native DateTime Annot_getModifiedDateTime(long j, Annot annot) throws PDFException;

    public static final native long Annot_getPage(long j, Annot annot) throws PDFException;

    public static final native RectF Annot_getRect(long j, Annot annot) throws PDFException;

    public static final native int Annot_getType(long j, Annot annot) throws PDFException;

    public static final native String Annot_getUniqueID(long j, Annot annot) throws PDFException;

    public static final native boolean Annot_isMarkup(long j, Annot annot) throws PDFException;

    public static final native boolean Annot_move(long j, Annot annot, RectF rectF) throws PDFException;

    public static final native boolean Annot_removeProperty(long j, Annot annot, int i) throws PDFException;

    public static final native boolean Annot_resetAppearanceStream(long j, Annot annot) throws PDFException;

    public static final native void Annot_setBorderColor(long j, Annot annot, long j2) throws PDFException;

    public static final native void Annot_setBorderInfo(long j, Annot annot, BorderInfo borderInfo) throws PDFException;

    public static final native void Annot_setContent(long j, Annot annot, String str) throws PDFException;

    public static final native void Annot_setFlags(long j, Annot annot, long j2) throws PDFException;

    public static final native void Annot_setModifiedDateTime(long j, Annot annot, DateTime dateTime) throws PDFException;

    public static final native void Annot_setUniqueID(long j, Annot annot, String str) throws PDFException;

    public static final native long Caret_SWIGUpcast(long j);

    public static final native RectF Caret_getInnerRect(long j, Caret caret) throws PDFException;

    public static final native boolean Caret_resetAppearanceStream(long j, Caret caret) throws PDFException;

    public static final native void Caret_setInnerRect(long j, Caret caret, RectF rectF) throws PDFException;

    public static final native long Circle_SWIGUpcast(long j);

    public static final native long Circle_getFillColor(long j, Circle circle) throws PDFException;

    public static final native RectF Circle_getInnerRect(long j, Circle circle) throws PDFException;

    public static final native boolean Circle_resetAppearanceStream(long j, Circle circle) throws PDFException;

    public static final native void Circle_setFillColor(long j, Circle circle, long j2) throws PDFException;

    public static final native void Circle_setInnerRect(long j, Circle circle, RectF rectF) throws PDFException;

    public static final native long FileAttachment_SWIGUpcast(long j);

    public static final native long FileAttachment_getFileSpec(long j, FileAttachment fileAttachment) throws PDFException;

    public static final native String FileAttachment_getIconName(long j, FileAttachment fileAttachment) throws PDFException;

    public static final native boolean FileAttachment_resetAppearanceStream(long j, FileAttachment fileAttachment) throws PDFException;

    public static final native boolean FileAttachment_setFileSpec(long j, FileAttachment fileAttachment, long j2, FileSpec fileSpec) throws PDFException;

    public static final native void FileAttachment_setIconName(long j, FileAttachment fileAttachment, String str) throws PDFException;

    public static final native long FreeText_SWIGUpcast(long j);

    public static final native int FreeText_getAlignment(long j, FreeText freeText) throws PDFException;

    public static final native String FreeText_getCalloutLineEndingStyle(long j, FreeText freeText) throws PDFException;

    public static final native PointF FreeText_getCalloutLinePoint(long j, FreeText freeText, int i) throws PDFException;

    public static final native int FreeText_getCalloutLinePointCount(long j, FreeText freeText) throws PDFException;

    public static final native DefaultAppearance FreeText_getDefaultAppearance(long j, FreeText freeText);

    public static final native long FreeText_getFillColor(long j, FreeText freeText) throws PDFException;

    public static final native RectF FreeText_getInnerRect(long j, FreeText freeText) throws PDFException;

    public static final native boolean FreeText_resetAppearanceStream(long j, FreeText freeText) throws PDFException;

    public static final native void FreeText_setAlignment(long j, FreeText freeText, int i) throws PDFException;

    public static final native void FreeText_setCalloutLineEndingStyle(long j, FreeText freeText, String str) throws PDFException;

    public static final native void FreeText_setCalloutLinePoints(long j, FreeText freeText, PointF pointF, PointF pointF2, PointF pointF3) throws PDFException;

    public static final native boolean FreeText_setDefaultAppearance(long j, FreeText freeText, DefaultAppearance defaultAppearance);

    public static final native void FreeText_setFillColor(long j, FreeText freeText, long j2) throws PDFException;

    public static final native void FreeText_setInnerRect(long j, FreeText freeText, RectF rectF) throws PDFException;

    public static final native long Highlight_SWIGUpcast(long j);

    public static final native boolean Highlight_resetAppearanceStream(long j, Highlight highlight) throws PDFException;

    public static final native long Ink_SWIGUpcast(long j);

    public static final native long Ink_getInkList(long j, Ink ink) throws PDFException;

    public static final native boolean Ink_resetAppearanceStream(long j, Ink ink) throws PDFException;

    public static final native void Ink_setInkList(long j, Ink ink, long j2, PDFPath pDFPath) throws PDFException;

    public static final native long Line_SWIGUpcast(long j);

    public static final native void Line_enableCaption(long j, Line line, boolean z) throws PDFException;

    public static final native PointF Line_getCaptionOffset(long j, Line line) throws PDFException;

    public static final native String Line_getCaptionPositionType(long j, Line line) throws PDFException;

    public static final native PointF Line_getEndPoint(long j, Line line) throws PDFException;

    public static final native String Line_getLineEndingStyle(long j, Line line) throws PDFException;

    public static final native String Line_getLineStartingStyle(long j, Line line) throws PDFException;

    public static final native PointF Line_getStartPoint(long j, Line line) throws PDFException;

    public static final native long Line_getStyleFillColor(long j, Line line) throws PDFException;

    public static final native boolean Line_hasCaption(long j, Line line) throws PDFException;

    public static final native boolean Line_resetAppearanceStream(long j, Line line) throws PDFException;

    public static final native void Line_setCaptionOffset(long j, Line line, PointF pointF) throws PDFException;

    public static final native void Line_setCaptionPositionType(long j, Line line, String str) throws PDFException;

    public static final native void Line_setEndPoint(long j, Line line, PointF pointF) throws PDFException;

    public static final native void Line_setLineEndingStyle(long j, Line line, String str) throws PDFException;

    public static final native void Line_setLineStartingStyle(long j, Line line, String str) throws PDFException;

    public static final native void Line_setStartPoint(long j, Line line, PointF pointF) throws PDFException;

    public static final native void Line_setStyleFillColor(long j, Line line, long j2) throws PDFException;

    public static final native long Link_SWIGUpcast(long j);

    public static final native long Link_getAction(long j, Link link) throws PDFException;

    public static final native int Link_getHighlightingMode(long j, Link link) throws PDFException;

    public static final native QuadPoints Link_getQuadPoints(long j, Link link, int i) throws PDFException;

    public static final native int Link_getQuadPointsCount(long j, Link link) throws PDFException;

    public static final native boolean Link_removeAction(long j, Link link) throws PDFException;

    public static final native boolean Link_resetAppearanceStream(long j, Link link) throws PDFException;

    public static final native void Link_setAction(long j, Link link, long j2, Action action) throws PDFException;

    public static final native void Link_setHighlightingMode(long j, Link link, int i) throws PDFException;

    public static final native void Link_setQuadPoints(long j, Link link, QuadPoints[] quadPointsArr) throws PDFException;

    public static final native long Markup_SWIGUpcast(long j);

    public static final native long Markup_addReply(long j, Markup markup) throws PDFException;

    public static final native long Markup_addStateAnnot(long j, Markup markup, int i, int i2) throws PDFException;

    public static final native DateTime Markup_getCreationDateTime(long j, Markup markup) throws PDFException;

    public static final native long Markup_getGroupElement(long j, Markup markup, int i) throws PDFException;

    public static final native int Markup_getGroupElementCount(long j, Markup markup) throws PDFException;

    public static final native long Markup_getGroupHeader(long j, Markup markup) throws PDFException;

    public static final native String Markup_getIntent(long j, Markup markup) throws PDFException;

    public static final native float Markup_getOpacity(long j, Markup markup) throws PDFException;

    public static final native long Markup_getPopup(long j, Markup markup) throws PDFException;

    public static final native long Markup_getReply(long j, Markup markup, int i) throws PDFException;

    public static final native int Markup_getReplyCount(long j, Markup markup) throws PDFException;

    public static final native long Markup_getStateAnnot(long j, Markup markup, int i, int i2) throws PDFException;

    public static final native int Markup_getStateAnnotCount(long j, Markup markup, int i) throws PDFException;

    public static final native String Markup_getSubject(long j, Markup markup) throws PDFException;

    public static final native String Markup_getTitle(long j, Markup markup) throws PDFException;

    public static final native boolean Markup_isGrouped(long j, Markup markup) throws PDFException;

    public static final native boolean Markup_removeAllReplies(long j, Markup markup) throws PDFException;

    public static final native boolean Markup_removeAllStateAnnots(long j, Markup markup) throws PDFException;

    public static final native boolean Markup_removeReply(long j, Markup markup, int i) throws PDFException;

    public static final native void Markup_setCreationDateTime(long j, Markup markup, DateTime dateTime) throws PDFException;

    public static final native void Markup_setIntent(long j, Markup markup, String str) throws PDFException;

    public static final native void Markup_setOpacity(long j, Markup markup, float f) throws PDFException;

    public static final native void Markup_setPopup(long j, Markup markup, long j2, Popup popup) throws PDFException;

    public static final native void Markup_setSubject(long j, Markup markup, String str) throws PDFException;

    public static final native void Markup_setTitle(long j, Markup markup, String str) throws PDFException;

    public static final native boolean Markup_ungroup(long j, Markup markup) throws PDFException;

    public static final native long Note_SWIGUpcast(long j);

    public static final native String Note_getIconName(long j, Note note) throws PDFException;

    public static final native boolean Note_getOpenStatus(long j, Note note) throws PDFException;

    public static final native long Note_getReplyTo(long j, Note note) throws PDFException;

    public static final native int Note_getState(long j, Note note) throws PDFException;

    public static final native int Note_getStateModel(long j, Note note) throws PDFException;

    public static final native boolean Note_isStateAnnot(long j, Note note) throws PDFException;

    public static final native boolean Note_resetAppearanceStream(long j, Note note) throws PDFException;

    public static final native void Note_setIconName(long j, Note note, String str) throws PDFException;

    public static final native void Note_setOpenStatus(long j, Note note, boolean z) throws PDFException;

    public static final native void Note_setState(long j, Note note, int i) throws PDFException;

    public static final native long PSInk_SWIGUpcast(long j);

    public static final native boolean PSInk_resetAppearanceStream(long j, PSInk pSInk);

    public static final native long PolyLine_SWIGUpcast(long j);

    public static final native String PolyLine_getLineEndingStyle(long j, PolyLine polyLine) throws PDFException;

    public static final native String PolyLine_getLineStartingStyle(long j, PolyLine polyLine) throws PDFException;

    public static final native long PolyLine_getStyleFillColor(long j, PolyLine polyLine) throws PDFException;

    public static final native PointF PolyLine_getVertex(long j, PolyLine polyLine, int i) throws PDFException;

    public static final native int PolyLine_getVertexCount(long j, PolyLine polyLine) throws PDFException;

    public static final native boolean PolyLine_resetAppearanceStream(long j, PolyLine polyLine) throws PDFException;

    public static final native void PolyLine_setLineEndingStyle(long j, PolyLine polyLine, String str) throws PDFException;

    public static final native void PolyLine_setLineStartingStyle(long j, PolyLine polyLine, String str) throws PDFException;

    public static final native void PolyLine_setStyleFillColor(long j, PolyLine polyLine, long j2) throws PDFException;

    public static final native void PolyLine_setVertexes(long j, PolyLine polyLine, PointF[] pointFArr) throws PDFException;

    public static final native long Polygon_SWIGUpcast(long j);

    public static final native long Polygon_getFillColor(long j, Polygon polygon) throws PDFException;

    public static final native PointF Polygon_getVertex(long j, Polygon polygon, int i) throws PDFException;

    public static final native int Polygon_getVertexCount(long j, Polygon polygon) throws PDFException;

    public static final native boolean Polygon_resetAppearanceStream(long j, Polygon polygon) throws PDFException;

    public static final native void Polygon_setFillColor(long j, Polygon polygon, long j2) throws PDFException;

    public static final native void Polygon_setVertexes(long j, Polygon polygon, PointF[] pointFArr) throws PDFException;

    public static final native long Popup_SWIGUpcast(long j);

    public static final native boolean Popup_getOpenStatus(long j, Popup popup) throws PDFException;

    public static final native void Popup_setOpenStatus(long j, Popup popup, boolean z) throws PDFException;

    public static final native long Square_SWIGUpcast(long j);

    public static final native long Square_getFillColor(long j, Square square) throws PDFException;

    public static final native RectF Square_getInnerRect(long j, Square square) throws PDFException;

    public static final native boolean Square_resetAppearanceStream(long j, Square square) throws PDFException;

    public static final native void Square_setFillColor(long j, Square square, long j2) throws PDFException;

    public static final native void Square_setInnerRect(long j, Square square, RectF rectF) throws PDFException;

    public static final native long Squiggly_SWIGUpcast(long j);

    public static final native boolean Squiggly_resetAppearanceStream(long j, Squiggly squiggly) throws PDFException;

    public static final native long Stamp_SWIGUpcast(long j);

    public static final native String Stamp_getIconName(long j, Stamp stamp) throws PDFException;

    public static final native boolean Stamp_resetAppearanceStream(long j, Stamp stamp) throws PDFException;

    public static final native void Stamp_setBitmap(long j, Stamp stamp, Bitmap bitmap) throws PDFException;

    public static final native void Stamp_setIconName(long j, Stamp stamp, String str) throws PDFException;

    public static final native long StrikeOut_SWIGUpcast(long j);

    public static final native boolean StrikeOut_resetAppearanceStream(long j, StrikeOut strikeOut) throws PDFException;

    public static final native long TextMarkup_SWIGUpcast(long j);

    public static final native QuadPoints TextMarkup_getQuadPoints(long j, TextMarkup textMarkup, int i) throws PDFException;

    public static final native int TextMarkup_getQuadPointsCount(long j, TextMarkup textMarkup) throws PDFException;

    public static final native void TextMarkup_setQuadPoints(long j, TextMarkup textMarkup, QuadPoints[] quadPointsArr) throws PDFException;

    public static final native long Underline_SWIGUpcast(long j);

    public static final native boolean Underline_resetAppearanceStream(long j, Underline underline) throws PDFException;

    public static final native void delete_Annot(long j) throws PDFException;

    public static final native void delete_Caret(long j) throws PDFException;

    public static final native void delete_Circle(long j) throws PDFException;

    public static final native void delete_FileAttachment(long j) throws PDFException;

    public static final native void delete_FreeText(long j) throws PDFException;

    public static final native void delete_Highlight(long j) throws PDFException;

    public static final native void delete_Ink(long j) throws PDFException;

    public static final native void delete_Line(long j) throws PDFException;

    public static final native void delete_Link(long j) throws PDFException;

    public static final native void delete_Markup(long j) throws PDFException;

    public static final native void delete_Note(long j) throws PDFException;

    public static final native void delete_PSInk(long j);

    public static final native void delete_PolyLine(long j) throws PDFException;

    public static final native void delete_Polygon(long j) throws PDFException;

    public static final native void delete_Popup(long j) throws PDFException;

    public static final native void delete_Square(long j) throws PDFException;

    public static final native void delete_Squiggly(long j) throws PDFException;

    public static final native void delete_Stamp(long j) throws PDFException;

    public static final native void delete_StrikeOut(long j) throws PDFException;

    public static final native void delete_TextMarkup(long j) throws PDFException;

    public static final native void delete_Underline(long j) throws PDFException;
}
