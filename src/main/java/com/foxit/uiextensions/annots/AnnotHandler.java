package com.foxit.uiextensions.annots;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import com.foxit.sdk.PDFViewCtrl.IDrawEventListener;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.uiextensions.utils.Event.Callback;

public interface AnnotHandler extends IDrawEventListener {
    void addAnnot(int i, AnnotContent annotContent, boolean z, Callback callback);

    boolean annotCanAnswer(Annot annot);

    RectF getAnnotBBox(Annot annot);

    int getType();

    boolean isHitAnnot(Annot annot, PointF pointF);

    void modifyAnnot(Annot annot, AnnotContent annotContent, boolean z, Callback callback);

    void onAnnotDeselected(Annot annot, boolean z);

    void onAnnotSelected(Annot annot, boolean z);

    boolean onLongPress(int i, MotionEvent motionEvent, Annot annot);

    boolean onSingleTapConfirmed(int i, MotionEvent motionEvent, Annot annot);

    boolean onTouchEvent(int i, MotionEvent motionEvent, Annot annot);

    void removeAnnot(Annot annot, boolean z, Callback callback);
}
