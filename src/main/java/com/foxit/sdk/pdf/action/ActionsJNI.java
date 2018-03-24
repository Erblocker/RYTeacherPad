package com.foxit.sdk.pdf.action;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;

class ActionsJNI {
    public static final native long Action_create(long j, PDFDoc pDFDoc, int i) throws PDFException;

    public static final native long Action_getSubAction(long j, Action action, int i) throws PDFException;

    public static final native int Action_getSubActionCount(long j, Action action) throws PDFException;

    public static final native int Action_getType(long j, Action action) throws PDFException;

    public static final native boolean Action_insertSubAction(long j, Action action, int i, long j2, Action action2) throws PDFException;

    public static final native void Action_release(long j, Action action) throws PDFException;

    public static final native boolean Action_removeAllSubActions(long j, Action action) throws PDFException;

    public static final native boolean Action_removeSubAction(long j, Action action, int i) throws PDFException;

    public static final native void Action_setSubAction(long j, Action action, int i, long j2, Action action2) throws PDFException;

    public static final native long Destination_createFitBBox(long j, PDFPage pDFPage) throws PDFException;

    public static final native long Destination_createFitBHorz(long j, PDFPage pDFPage, float f) throws PDFException;

    public static final native long Destination_createFitBVert(long j, PDFPage pDFPage, float f) throws PDFException;

    public static final native long Destination_createFitHorz(long j, PDFPage pDFPage, float f) throws PDFException;

    public static final native long Destination_createFitPage(long j, PDFPage pDFPage) throws PDFException;

    public static final native long Destination_createFitRect(long j, PDFPage pDFPage, float f, float f2, float f3, float f4) throws PDFException;

    public static final native long Destination_createFitVert(long j, PDFPage pDFPage, float f) throws PDFException;

    public static final native long Destination_createXYZ(long j, PDFPage pDFPage, float f, float f2, float f3) throws PDFException;

    public static final native float Destination_getBottom(long j, Destination destination) throws PDFException;

    public static final native float Destination_getLeft(long j, Destination destination) throws PDFException;

    public static final native int Destination_getPageIndex(long j, Destination destination) throws PDFException;

    public static final native float Destination_getRight(long j, Destination destination) throws PDFException;

    public static final native float Destination_getTop(long j, Destination destination) throws PDFException;

    public static final native float Destination_getZoomFactor(long j, Destination destination) throws PDFException;

    public static final native int Destination_getZoomMode(long j, Destination destination) throws PDFException;

    public static final native void Destination_release(long j, Destination destination) throws PDFException;

    public static final native long GotoAction_SWIGUpcast(long j);

    public static final native long GotoAction_getDestination(long j, GotoAction gotoAction) throws PDFException;

    public static final native void GotoAction_setDestination(long j, GotoAction gotoAction, long j2, Destination destination) throws PDFException;

    public static final native long URIAction_SWIGUpcast(long j);

    public static final native String URIAction_getURI(long j, URIAction uRIAction) throws PDFException;

    public static final native boolean URIAction_isTrackPosition(long j, URIAction uRIAction) throws PDFException;

    public static final native void URIAction_setTrackPositionFlag(long j, URIAction uRIAction, boolean z) throws PDFException;

    public static final native void URIAction_setURI(long j, URIAction uRIAction, String str) throws PDFException;

    public static final native void delete_GotoAction(long j) throws PDFException;

    public static final native void delete_URIAction(long j) throws PDFException;
}
