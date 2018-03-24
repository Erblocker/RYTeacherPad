package com.foxit.uiextensions.security.digitalsignature;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.signature.Signature;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.imp.AnnotMenuImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;

class DigitalSignatureAnnotHandler implements AnnotHandler {
    private AnnotMenu mAnnotMenu;
    private final int mBBoxColor = -11645619;
    private int mBBoxSpace = 0;
    private Context mContext;
    private Annot mLastAnnot;
    private ArrayList<Integer> mMenuItems;
    private Paint mPaintBbox;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private DigitalSignatureSecurityHandler mSignatureHandler;
    private Rect mTmpRect = new Rect();
    private RectF mTmpRectF = new RectF();

    public DigitalSignatureAnnotHandler(Context dmContext, ViewGroup parent, PDFViewCtrl pdfViewCtrl, DigitalSignatureSecurityHandler securityHandler) {
        this.mContext = dmContext;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mSignatureHandler = securityHandler;
        init();
    }

    private void init() {
        this.mPaintBbox = new Paint();
        this.mPaintBbox.setAntiAlias(true);
        this.mPaintBbox.setStyle(Style.STROKE);
        this.mPaintBbox.setStrokeWidth(AppAnnotUtil.getInstance(this.mContext).getAnnotBBoxStrokeWidth());
        Paint paint = this.mPaintBbox;
        AppAnnotUtil.getInstance(this.mContext);
        paint.setPathEffect(AppAnnotUtil.getBBoxPathEffect2());
        this.mPaintBbox.setColor(-11645619);
        this.mAnnotMenu = new AnnotMenuImpl(this.mContext, this.mParent);
        this.mMenuItems = new ArrayList();
        this.mMenuItems.add(0, Integer.valueOf(R.string.rv_security_dsg_verify));
        this.mMenuItems.add(1, Integer.valueOf(R.string.fx_string_cancel));
    }

    public int getType() {
        return 121;
    }

    public boolean annotCanAnswer(Annot annot) {
        return true;
    }

    public RectF getAnnotBBox(Annot annot) {
        try {
            return annot.getRect();
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        RectF rectF = getAnnotBBox(annot);
        if (this.mPdfViewCtrl != null) {
            try {
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, annot.getPage().getIndex());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rectF.contains(point.x, point.y);
    }

    public void onAnnotSelected(final Annot annot, boolean needInvalid) {
        if (annot != null && (annot instanceof Signature)) {
            try {
                int pageIndex = annot.getPage().getIndex();
                RectF annotRect = annot.getRect();
                this.mMenuItems.clear();
                this.mMenuItems.add(Integer.valueOf(16));
                this.mMenuItems.add(Integer.valueOf(15));
                this.mAnnotMenu.setMenuItems(this.mMenuItems);
                this.mAnnotMenu.setListener(new ClickListener() {
                    public void onAMClick(int btType) {
                        if (btType == 16) {
                            try {
                                DigitalSignatureAnnotHandler.this.mSignatureHandler.verifySignature(annot);
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                            DocumentManager.getInstance(DigitalSignatureAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        } else if (btType == 15) {
                            DocumentManager.getInstance(DigitalSignatureAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                    }
                });
                this.mLastAnnot = annot;
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    RectF displayViewRect = new RectF();
                    RectF pageViewRect = new RectF();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, pageViewRect, pageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(pageViewRect, displayViewRect, pageIndex);
                    this.mPdfViewCtrl.refresh(pageIndex, rectRoundOut(pageViewRect, 0));
                    this.mAnnotMenu.show(displayViewRect);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onAnnotDeselected(Annot annot, boolean needInvalid) {
        if (annot != null && (annot instanceof Signature)) {
            this.mAnnotMenu.dismiss();
            this.mMenuItems.clear();
            this.mLastAnnot = null;
            try {
                int pageIndex = annot.getPage().getIndex();
                if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                    RectF rectF = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
                    this.mPdfViewCtrl.refresh(pageIndex, rectRoundOut(rectF, 10));
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, Callback result) {
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        PointF pageViewPt = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
            try {
                if (!(pageIndex == annot.getPage().getIndex() && isHitAnnot(annot, pageViewPt))) {
                    DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        } else {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
        }
        return true;
    }

    private Rect rectRoundOut(RectF rectF, int roundSize) {
        rectF.roundOut(this.mTmpRect);
        this.mTmpRect.inset(-roundSize, -roundSize);
        return this.mTmpRect;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (this.mLastAnnot == annot && annot.getPage().getIndex() == pageIndex) {
                    RectF rect = annot.getRect();
                    this.mTmpRectF.set(rect.left, rect.top, rect.right, rect.bottom);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mTmpRectF, this.mTmpRectF, pageIndex);
                    Rect rectBBox = rectRoundOut(this.mTmpRectF, this.mBBoxSpace);
                    canvas.save();
                    canvas.drawRect(rectBBox, this.mPaintBbox);
                    canvas.restore();
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDrawForControls(Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (annot.getType() == 20) {
                    int pageIndex = annot.getPage().getIndex();
                    RectF rect = annot.getRect();
                    this.mTmpRectF.set(rect.left, rect.top, rect.right, rect.bottom);
                    if (this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                        this.mPdfViewCtrl.convertPdfRectToPageViewRect(this.mTmpRectF, this.mTmpRectF, pageIndex);
                        this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(this.mTmpRectF, this.mTmpRectF, pageIndex);
                        this.mAnnotMenu.update(this.mTmpRectF);
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }
}
