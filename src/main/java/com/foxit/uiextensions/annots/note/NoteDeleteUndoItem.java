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
class NoteDeleteUndoItem extends NoteUndoItem {
    public NoteDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        NoteAddUndoItem undoItem = new NoteAddUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mNM = this.mNM;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mFlags = this.mFlags;
        undoItem.mSubject = this.mSubject;
        undoItem.mIconName = this.mIconName;
        undoItem.mCreationDate = this.mCreationDate;
        undoItem.mModifiedDate = this.mModifiedDate;
        undoItem.mBBox = new RectF(this.mBBox);
        undoItem.mContents = this.mContents;
        undoItem.mColor = this.mColor;
        undoItem.mOpacity = this.mOpacity;
        undoItem.mIsFromReplyModule = this.mIsFromReplyModule;
        undoItem.mParentNM = this.mParentNM;
        try {
            Note annot;
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            if (this.mIsFromReplyModule) {
                Annot note = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mParentNM);
                if (note == null) {
                    return false;
                }
                annot = ((Markup) note).addReply();
            } else {
                annot = (Note) page.addAnnot(1, this.mBBox);
            }
            final Note finalAnnot = annot;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new NoteEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(NoteDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, finalAnnot);
                        if (NoteDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(NoteDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = finalAnnot.getRect();
                                NoteDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, NoteDeleteUndoItem.this.mPageIndex);
                                NoteDeleteUndoItem.this.mPdfViewCtrl.refresh(NoteDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
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
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Note)) {
                return false;
            }
            if (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).onAnnotDeleted(page, annot);
            final RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new NoteEvent(3, this, (Note) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success && NoteDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(NoteDeleteUndoItem.this.mPageIndex)) {
                        RectF deviceRectF = new RectF();
                        NoteDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, deviceRectF, NoteDeleteUndoItem.this.mPageIndex);
                        NoteDeleteUndoItem.this.mPdfViewCtrl.refresh(NoteDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(deviceRectF));
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
