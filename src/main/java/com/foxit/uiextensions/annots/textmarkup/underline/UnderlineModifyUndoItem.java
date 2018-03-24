package com.foxit.uiextensions.annots.textmarkup.underline;

import android.graphics.Paint;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Underline;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: UnderlineUndoItem */
class UnderlineModifyUndoItem extends UnderlineUndoItem {
    public Paint mPaintBbox;
    public int mRedoColor;
    public String mRedoContent;
    public float mRedoOpacity;
    public int mUndoColor;
    public String mUndoContent;
    public float mUndoOpacity;

    public UnderlineModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Underline)) {
                return false;
            }
            this.mColor = (long) this.mUndoColor;
            this.mOpacity = this.mUndoOpacity;
            this.mContents = this.mUndoContent;
            this.mPaintBbox.setColor(this.mUndoColor | -16777216);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new UnderlineEvent(2, this, (Underline) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(UnderlineModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(UnderlineModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(UnderlineModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (UnderlineModifyUndoItem.this.mPdfViewCtrl.isPageVisible(UnderlineModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                UnderlineModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, UnderlineModifyUndoItem.this.mPageIndex);
                                UnderlineModifyUndoItem.this.mPdfViewCtrl.refresh(UnderlineModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
            if (annot == null || !(annot instanceof Underline)) {
                return false;
            }
            this.mColor = (long) this.mRedoColor;
            this.mOpacity = this.mRedoOpacity;
            this.mContents = this.mRedoContent;
            this.mPaintBbox.setColor(this.mRedoColor | -16777216);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new UnderlineEvent(2, this, (Underline) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(UnderlineModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(UnderlineModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(UnderlineModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (UnderlineModifyUndoItem.this.mPdfViewCtrl.isPageVisible(UnderlineModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                UnderlineModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, UnderlineModifyUndoItem.this.mPageIndex);
                                UnderlineModifyUndoItem.this.mPdfViewCtrl.refresh(UnderlineModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
