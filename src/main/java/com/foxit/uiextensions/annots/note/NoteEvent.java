package com.foxit.uiextensions.annots.note;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Note;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;

public class NoteEvent extends EditAnnotEvent {
    public NoteEvent(int eventType, NoteUndoItem undoItem, Note note, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = note;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Note)) {
            return false;
        }
        Note annot = this.mAnnot;
        try {
            if (this.mUndoItem.mContents != null) {
                annot.setContent(this.mUndoItem.mContents);
            }
            if (this.mUndoItem.mCreationDate != null) {
                annot.setCreationDateTime(this.mUndoItem.mCreationDate);
            }
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mAuthor != null) {
                annot.setTitle(this.mUndoItem.mAuthor);
            }
            if (!((NoteAddUndoItem) this.mUndoItem).mIsFromReplyModule) {
                annot.setBorderColor(this.mUndoItem.mColor);
                annot.setOpacity(this.mUndoItem.mOpacity);
                annot.setIconName(((NoteAddUndoItem) this.mUndoItem).mIconName);
                annot.setOpenStatus(((NoteAddUndoItem) this.mUndoItem).mOpenStatus);
                annot.setFlags(this.mUndoItem.mFlags);
            }
            annot.setUniqueID(this.mUndoItem.mNM);
            annot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            return false;
        }
    }

    public boolean modify() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Note)) {
            return false;
        }
        Note annot = this.mAnnot;
        try {
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mContents == null) {
                this.mUndoItem.mContents = "";
            }
            annot.setContent(this.mUndoItem.mContents);
            if (!((NoteModifyUndoItem) this.mUndoItem).mIsFromReplyModule) {
                annot.setBorderColor(this.mUndoItem.mColor);
                annot.setOpacity(this.mUndoItem.mOpacity);
                annot.setIconName(((NoteModifyUndoItem) this.mUndoItem).mIconName);
                annot.move(this.mUndoItem.mBBox);
            }
            annot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            if (e.getLastError() == PDFError.OOM.getCode()) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            return false;
        }
    }

    public boolean delete() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Note)) {
            return false;
        }
        try {
            ((Markup) this.mAnnot).removeAllReplies();
            this.mAnnot.getPage().removeAnnot(this.mAnnot);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
