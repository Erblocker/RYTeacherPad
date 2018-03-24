package com.foxit.uiextensions.annots.textmarkup.squiggly;

import android.graphics.Paint;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Squiggly;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: SquigglyUndoItem */
class SquigglyModifyUndoItem extends SquigglyUndoItem {
    public Paint mPaintBbox;
    public int mRedoColor;
    public String mRedoContents;
    public float mRedoOpacity;
    public int mUndoColor;
    public String mUndoContents;
    public float mUndoOpacity;

    public SquigglyModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Squiggly)) {
                return false;
            }
            this.mColor = (long) this.mUndoColor;
            this.mOpacity = this.mUndoOpacity;
            this.mContents = this.mUndoContents;
            this.mPaintBbox.setColor(this.mUndoColor | -16777216);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new SquigglyEvent(2, this, (Squiggly) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(SquigglyModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(SquigglyModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(SquigglyModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (SquigglyModifyUndoItem.this.mPdfViewCtrl.isPageVisible(SquigglyModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                SquigglyModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, SquigglyModifyUndoItem.this.mPageIndex);
                                SquigglyModifyUndoItem.this.mPdfViewCtrl.refresh(SquigglyModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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

    public boolean redo() {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Squiggly)) {
                return false;
            }
            this.mColor = (long) this.mRedoColor;
            this.mOpacity = this.mRedoOpacity;
            this.mContents = this.mRedoContents;
            this.mPaintBbox.setColor(this.mRedoColor | -16777216);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new SquigglyEvent(2, this, (Squiggly) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(SquigglyModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(SquigglyModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(SquigglyModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (SquigglyModifyUndoItem.this.mPdfViewCtrl.isPageVisible(SquigglyModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                SquigglyModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, SquigglyModifyUndoItem.this.mPageIndex);
                                SquigglyModifyUndoItem.this.mPdfViewCtrl.refresh(SquigglyModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
