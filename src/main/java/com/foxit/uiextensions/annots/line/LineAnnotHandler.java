package com.foxit.uiextensions.annots.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AbstractAnnotHandler;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;

class LineAnnotHandler implements AnnotHandler {
    Context mContext;
    LineDefaultAnnotHandler mDefAnnotHandler;
    ViewGroup mParent;
    PDFViewCtrl mPdfViewCtrl;
    LineRealAnnotHandler mRealAnnotHandler;

    public LineAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl, LineUtil util) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mRealAnnotHandler = new LineRealAnnotHandler(context, parent, pdfViewCtrl, util);
        this.mDefAnnotHandler = new LineDefaultAnnotHandler(context, parent, pdfViewCtrl);
    }

    AnnotHandler getHandler(String intent) {
        if (intent == null || !intent.equals(LineConstants.INTENT_LINE_DIMENSION)) {
            return this.mRealAnnotHandler;
        }
        return this.mDefAnnotHandler;
    }

    public void setAnnotMenu(String intent, AnnotMenu annotMenu) {
        ((AbstractAnnotHandler) getHandler(intent)).setAnnotMenu(annotMenu);
    }

    public AnnotMenu getAnnotMenu(String intent) {
        return ((AbstractAnnotHandler) getHandler(intent)).getAnnotMenu();
    }

    public void setPropertyBar(String intent, PropertyBar propertyBar) {
        ((AbstractAnnotHandler) getHandler(intent)).setPropertyBar(propertyBar);
    }

    public PropertyBar getPropertyBar(String intent) {
        return ((AbstractAnnotHandler) getHandler(intent)).getPropertyBar();
    }

    public int getType() {
        return this.mRealAnnotHandler.getType();
    }

    public boolean annotCanAnswer(Annot annot) {
        try {
            return getHandler(((Line) annot).getIntent()).annotCanAnswer(annot);
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RectF getAnnotBBox(Annot annot) {
        try {
            return getHandler(((Line) annot).getIntent()).getAnnotBBox(annot);
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        try {
            return getHandler(((Line) annot).getIntent()).isHitAnnot(annot, point);
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onAnnotSelected(Annot annot, boolean reRender) {
        try {
            getHandler(((Line) annot).getIntent()).onAnnotSelected(annot, reRender);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        try {
            getHandler(((Line) annot).getIntent()).onAnnotDeselected(annot, reRender);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
        getHandler(content.getIntent()).addAnnot(pageIndex, content, addUndo, result);
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        try {
            getHandler(((Line) annot).getIntent()).modifyAnnot(annot, content, addUndo, result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        try {
            getHandler(((Line) annot).getIntent()).removeAnnot(annot, addUndo, result);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e, Annot annot) {
        try {
            return getHandler(((Line) annot).getIntent()).onTouchEvent(pageIndex, e, annot);
        } catch (PDFException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            return getHandler(((Line) annot).getIntent()).onLongPress(pageIndex, motionEvent, annot);
        } catch (PDFException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        try {
            return getHandler(((Line) annot).getIntent()).onSingleTapConfirmed(pageIndex, motionEvent, annot);
        } catch (PDFException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (annot.getType() == 4) {
                    getHandler(((Line) annot).getIntent()).onDraw(pageIndex, canvas);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                ((AbstractAnnotHandler) getHandler(((Line) annot).getIntent())).onDrawForControls(canvas);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onLanguageChanged() {
        this.mRealAnnotHandler.onLanguageChanged();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.mRealAnnotHandler.onKeyDown(keyCode, event);
    }
}
