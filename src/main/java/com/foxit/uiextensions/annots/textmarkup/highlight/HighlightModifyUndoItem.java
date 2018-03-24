package com.foxit.uiextensions.annots.textmarkup.highlight;

import android.graphics.Paint;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Highlight;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: HighlightUndoItem */
class HighlightModifyUndoItem extends HighlightUndoItem {
    public Paint mPaintBbox;
    public int mRedoColor;
    public String mRedoContents;
    public float mRedoOpacity;
    public int mUndoColor;
    public String mUndoContents;
    public float mUndoOpacity;

    public HighlightModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Highlight)) {
                return false;
            }
            this.mColor = (long) this.mUndoColor;
            this.mOpacity = this.mUndoOpacity;
            this.mContents = this.mUndoContents;
            this.mPaintBbox.setColor(this.mUndoColor | -16777216);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new HighlightEvent(2, this, (Highlight) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(HighlightModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(HighlightModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(HighlightModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (HighlightModifyUndoItem.this.mPdfViewCtrl.isPageVisible(HighlightModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                HighlightModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, HighlightModifyUndoItem.this.mPageIndex);
                                HighlightModifyUndoItem.this.mPdfViewCtrl.refresh(HighlightModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
            if (annot == null || !(annot instanceof Highlight)) {
                return false;
            }
            this.mColor = (long) this.mRedoColor;
            this.mOpacity = this.mRedoOpacity;
            this.mContents = this.mRedoContents;
            this.mPaintBbox.setColor(this.mRedoColor | -16777216);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new HighlightEvent(2, this, (Highlight) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(HighlightModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(HighlightModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(HighlightModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (HighlightModifyUndoItem.this.mPdfViewCtrl.isPageVisible(HighlightModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                HighlightModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, HighlightModifyUndoItem.this.mPageIndex);
                                HighlightModifyUndoItem.this.mPdfViewCtrl.refresh(HighlightModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
