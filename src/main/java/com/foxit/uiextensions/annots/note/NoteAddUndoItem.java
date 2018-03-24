package com.foxit.uiextensions.annots.note;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Note;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: NoteUndoItem */
class NoteAddUndoItem extends NoteUndoItem {
    public NoteAddUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        NoteDeleteUndoItem undoItem = new NoteDeleteUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Note)) {
                return false;
            }
            final RectF annotRectF = annot.getRect();
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new NoteEvent(3, undoItem, (Note) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && NoteAddUndoItem.this.mPdfViewCtrl.isPageVisible(NoteAddUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        NoteAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, NoteAddUndoItem.this.mPageIndex);
                        NoteAddUndoItem.this.mPdfViewCtrl.refresh(NoteAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
            Annot annot;
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            if (this.mIsFromReplyModule) {
                Annot note = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mParentNM);
                if (note == null) {
                    return false;
                }
                annot = ((Markup) note).addReply();
            } else {
                annot = page.addAnnot(1, this.mBBox);
            }
            final Annot finalAnnot = annot;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new NoteEvent(1, this, (Note) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(NoteAddUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, finalAnnot);
                        if (NoteAddUndoItem.this.mPdfViewCtrl.isPageVisible(NoteAddUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = finalAnnot.getRect();
                                NoteAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, NoteAddUndoItem.this.mPageIndex);
                                NoteAddUndoItem.this.mPdfViewCtrl.refresh(NoteAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
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
