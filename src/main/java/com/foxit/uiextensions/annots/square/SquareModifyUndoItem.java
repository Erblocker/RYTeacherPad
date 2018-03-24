package com.foxit.uiextensions.annots.square;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Square;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: SquareUndoItem */
class SquareModifyUndoItem extends SquareUndoItem {
    public RectF mRedoBbox;
    public int mRedoColor;
    public String mRedoContent;
    public float mRedoLineWidth;
    public float mRedoOpacity;
    public RectF mUndoBbox;
    public int mUndoColor;
    public String mUndoContent;
    public float mUndoLineWidth;
    public float mUndoOpacity;

    public SquareModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        return modifyAnnot(this.mUndoColor, this.mUndoOpacity, this.mUndoLineWidth, this.mUndoBbox, this.mUndoContent);
    }

    public boolean redo() {
        return modifyAnnot(this.mRedoColor, this.mRedoOpacity, this.mRedoLineWidth, this.mRedoBbox, this.mRedoContent);
    }

    private boolean modifyAnnot(int color, float opacity, float lineWidth, RectF bbox, String content) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Square)) {
                return false;
            }
            final RectF oldBbox = annot.getRect();
            this.mColor = (long) color;
            this.mOpacity = opacity;
            this.mBBox = new RectF(bbox);
            this.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            this.mLineWidth = lineWidth;
            this.mContents = content;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new SquareEvent(2, this, (Square) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(SquareModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(SquareModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(SquareModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (SquareModifyUndoItem.this.mPdfViewCtrl.isPageVisible(SquareModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                SquareModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, SquareModifyUndoItem.this.mPageIndex);
                                SquareModifyUndoItem.this.mPdfViewCtrl.refresh(SquareModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
                                SquareModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(oldBbox, oldBbox, SquareModifyUndoItem.this.mPageIndex);
                                SquareModifyUndoItem.this.mPdfViewCtrl.refresh(SquareModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(oldBbox));
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }));
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
