package com.foxit.uiextensions.annots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.IAnnotTaskResult;
import com.foxit.uiextensions.annots.common.UIAnnotFrame;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.controls.propertybar.AnnotMenu.ClickListener;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;

public class DefaultAnnotHandler extends AbstractAnnotHandler {
    protected ArrayList<Integer> mMenuText = new ArrayList();

    public DefaultAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        super(context, parent, pdfViewCtrl, 0);
    }

    protected AbstractToolHandler getToolHandler() {
        return null;
    }

    public boolean annotCanAnswer(Annot annot) {
        return AppAnnotUtil.isSupportEditAnnot(annot);
    }

    public void onAnnotSelected(Annot annot, boolean reRender) {
        super.onAnnotSelected(annot, reRender);
    }

    public void onAnnotDeselected(Annot annot, boolean reRender) {
        if (!this.mIsModified) {
            super.onAnnotDeselected(annot, reRender);
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent e, Annot annot) {
        int action = e.getAction();
        switch (action) {
            case 0:
                return super.onTouchEvent(pageIndex, e, annot);
            case 1:
            case 2:
            case 3:
                PointF point = new PointF(e.getX(), e.getY());
                this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
                try {
                    if (this.mTouchCaptured && pageIndex == annot.getPage().getIndex() && annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        if (action != 1 && action != 3) {
                            return true;
                        }
                        this.mTouchCaptured = false;
                        this.mDownPt.set(0.0f, 0.0f);
                        this.mLastPt.set(0.0f, 0.0f);
                        this.mOp = -1;
                        this.mCtl = -1;
                        return true;
                    }
                } catch (PDFException e1) {
                    e1.printStackTrace();
                    break;
                }
                break;
        }
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return super.onSingleTapConfirmed(pageIndex, motionEvent, annot);
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        return super.onLongPress(pageIndex, motionEvent, annot);
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            try {
                if (this.mSelectedAnnot == annot && annot.getPage().getIndex() == pageIndex) {
                    RectF bbox = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                    RectF mapBounds = UIAnnotFrame.mapBounds(this.mPdfViewCtrl, pageIndex, annot, this.mOp, this.mCtl, this.mLastPt.x - this.mDownPt.x, this.mLastPt.y - this.mDownPt.y);
                    if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                        UIAnnotFrame.getInstance(this.mContext).drawFrame(canvas, mapBounds, ((int) annot.getBorderColor()) | -16777216, (int) (((Markup) annot).getOpacity() * 255.0f));
                    }
                }
            } catch (PDFException e) {
            }
        }
    }

    public void addAnnot(int pageIndex, AnnotContent content, boolean addUndo, final Callback result) {
        try {
            Annot annot = this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(content.getType(), content.getBBox());
            DefaultAnnotAddUndoItem undoItem = new DefaultAnnotAddUndoItem(this, this.mPdfViewCtrl);
            undoItem.mPageIndex = pageIndex;
            undoItem.mNM = content.getNM();
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mFlags = 4;
            undoItem.mColor = (long) content.getColor();
            undoItem.mOpacity = ((float) content.getOpacity()) / 255.0f;
            undoItem.mBBox = new RectF(content.getBBox());
            undoItem.mIntent = content.getIntent();
            undoItem.mLineWidth = content.getLineWidth();
            undoItem.mType = content.getType();
            addAnnot(pageIndex, annot, undoItem, addUndo, new IAnnotTaskResult<PDFPage, Annot, Void>() {
                public void onResult(boolean success, PDFPage p1, Annot p2, Void p3) {
                    if (result != null) {
                        result.result(null, true);
                    }
                }
            });
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected void addAnnot(int pageIndex, Annot annot, DefaultAnnotAddUndoItem undoItem, boolean addUndo, IAnnotTaskResult<PDFPage, Annot, Void> result) {
        handleAddAnnot(pageIndex, annot, new DefaultAnnotEvent(1, undoItem, annot, this.mPdfViewCtrl), addUndo, result);
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
        DefaultAnnotModifyUndoItem undoItem = new DefaultAnnotModifyUndoItem(this, this.mPdfViewCtrl);
        undoItem.setOldValue(annot);
        undoItem.setCurrentValue(content);
        modifyAnnot(annot, undoItem, false, addUndo, true, result);
    }

    protected void modifyAnnot(Annot annot, DefaultAnnotUndoItem undoItem, boolean useOldValue, boolean addUndo, boolean reRender, final Callback result) {
        DefaultAnnotEvent modifyEvent = new DefaultAnnotEvent(2, undoItem, annot, this.mPdfViewCtrl);
        modifyEvent.useOldValue = useOldValue;
        handleModifyAnnot(annot, modifyEvent, addUndo, reRender, new IAnnotTaskResult<PDFPage, Annot, Void>() {
            public void onResult(boolean success, PDFPage p1, Annot p2, Void p3) {
                if (result != null) {
                    result.result(null, success);
                }
            }
        });
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
        DefaultAnnotDeleteUndoItem undoItem = new DefaultAnnotDeleteUndoItem(this, this.mPdfViewCtrl);
        undoItem.setCurrentValue(annot);
        removeAnnot(annot, undoItem, addUndo, result);
    }

    protected void removeAnnot(Annot annot, DefaultAnnotDeleteUndoItem undoItem, boolean addUndo, final Callback result) {
        handleRemoveAnnot(annot, new DefaultAnnotEvent(3, undoItem, annot, this.mPdfViewCtrl), addUndo, new IAnnotTaskResult<PDFPage, Void, Void>() {
            public void onResult(boolean success, PDFPage p1, Void p2, Void p3) {
                if (result != null) {
                    result.result(null, success);
                }
            }
        });
    }

    protected ArrayList<Path> generatePathData(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot) {
        return new ArrayList();
    }

    protected void transformAnnot(PDFViewCtrl pdfViewCtrl, int pageIndex, Annot annot, Matrix matrix) {
    }

    protected void resetStatus() {
        this.mBackRect = null;
        this.mSelectedAnnot = null;
        this.mIsModified = false;
    }

    protected void showPopupMenu() {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot != null) {
            try {
                if (AppAnnotUtil.isSupportReply(curAnnot)) {
                    reloadPopupMenuString();
                    this.mAnnotMenu.setMenuItems(this.mMenuText);
                    RectF bbox = curAnnot.getRect();
                    int pageIndex = curAnnot.getPage().getIndex();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                    this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                    this.mAnnotMenu.show(bbox);
                    this.mAnnotMenu.setListener(new ClickListener() {
                        public void onAMClick(int flag) {
                            if (DefaultAnnotHandler.this.mSelectedAnnot != null) {
                                if (flag == 3) {
                                    DocumentManager.getInstance(DefaultAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                    UIAnnotReply.showComments(DefaultAnnotHandler.this.mContext, DefaultAnnotHandler.this.mPdfViewCtrl, DefaultAnnotHandler.this.mParent, DefaultAnnotHandler.this.mSelectedAnnot);
                                } else if (flag == 4) {
                                    DocumentManager.getInstance(DefaultAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                    UIAnnotReply.replyToAnnot(DefaultAnnotHandler.this.mContext, DefaultAnnotHandler.this.mPdfViewCtrl, DefaultAnnotHandler.this.mParent, DefaultAnnotHandler.this.mSelectedAnnot);
                                }
                            }
                        }
                    });
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    protected void dismissPopupMenu() {
        this.mAnnotMenu.dismiss();
    }

    protected void showPropertyBar(long curProperty) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null) {
            this.mPropertyBar.setPropertyChangeListener(this);
            this.mPropertyBar.setProperty(1, getColor());
            this.mPropertyBar.setProperty(2, getColor());
            this.mPropertyBar.setProperty(4, getThickness());
            this.mPropertyBar.reset(getSupportedProperties());
            try {
                RectF bbox = annot.getRect();
                int pageIndex = annot.getPage().getIndex();
                this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(bbox, bbox, pageIndex);
                this.mPropertyBar.show(bbox, false);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    protected void hidePropertyBar() {
        if (this.mPropertyBar.isShowing()) {
            this.mPropertyBar.dismiss();
        }
    }

    protected long getSupportedProperties() {
        return 1;
    }

    protected void setPropertyBarProperties(PropertyBar propertyBar) {
        propertyBar.setProperty(1, getColor());
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            propertyBar.setArrowVisible(true);
        }
    }

    public void setPaintProperty(PDFViewCtrl pdfViewCtrl, int pageIndex, Paint paint, Annot annot) {
        super.setPaintProperty(pdfViewCtrl, pageIndex, paint, annot);
    }

    protected void onLanguageChanged() {
        reloadPopupMenuString();
    }

    protected void reloadPopupMenuString() {
        this.mMenuText.clear();
        this.mMenuText.add(Integer.valueOf(3));
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mMenuText.add(Integer.valueOf(4));
        }
    }
}
