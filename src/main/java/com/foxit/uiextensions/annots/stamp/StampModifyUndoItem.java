package com.foxit.uiextensions.annots.stamp;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Stamp;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: StampUndoItem */
class StampModifyUndoItem extends StampUndoItem {
    public RectF mRedoBox;
    public String mRedoContent;
    public RectF mUndoBox;
    public String mUndoContent;

    public StampModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        return modifyAnnot(this.mUndoBox, this.mUndoContent);
    }

    public boolean redo() {
        return modifyAnnot(this.mRedoBox, this.mRedoContent);
    }

    private boolean modifyAnnot(RectF bbox, String content) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Stamp)) {
                return false;
            }
            final RectF oldBbox = annot.getRect();
            this.mBBox = new RectF(bbox);
            this.mContents = content;
            this.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new StampEvent(2, this, (Stamp) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(StampModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(StampModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(StampModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (StampModifyUndoItem.this.mPdfViewCtrl.isPageVisible(StampModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                StampModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, StampModifyUndoItem.this.mPageIndex);
                                StampModifyUndoItem.this.mPdfViewCtrl.refresh(StampModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
                                StampModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(oldBbox, oldBbox, StampModifyUndoItem.this.mPageIndex);
                                StampModifyUndoItem.this.mPdfViewCtrl.refresh(StampModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(oldBbox));
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
