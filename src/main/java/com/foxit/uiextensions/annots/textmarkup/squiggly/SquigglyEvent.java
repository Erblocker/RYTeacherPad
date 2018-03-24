package com.foxit.uiextensions.annots.textmarkup.squiggly;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Squiggly;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;

public class SquigglyEvent extends EditAnnotEvent {
    public SquigglyEvent(int eventType, SquigglyUndoItem undoItem, Squiggly squiggly, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = squiggly;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Squiggly)) {
            return false;
        }
        Squiggly annot = this.mAnnot;
        try {
            annot.setBorderColor(this.mUndoItem.mColor);
            if (((SquigglyAddUndoItem) this.mUndoItem).mQuadPoints != null) {
                annot.setQuadPoints(((SquigglyAddUndoItem) this.mUndoItem).mQuadPoints);
            }
            annot.setOpacity(this.mUndoItem.mOpacity);
            if (this.mUndoItem.mContents != null) {
                annot.setContent(this.mUndoItem.mContents);
            }
            annot.setFlags(this.mUndoItem.mFlags);
            if (this.mUndoItem.mCreationDate != null) {
                annot.setCreationDateTime(this.mUndoItem.mCreationDate);
            }
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mAuthor != null) {
                annot.setTitle(this.mUndoItem.mAuthor);
            }
            if (this.mUndoItem.mSubject != null) {
                annot.setSubject(this.mUndoItem.mSubject);
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
        if (this.mAnnot == null || !(this.mAnnot instanceof Squiggly)) {
            return false;
        }
        Squiggly annot = this.mAnnot;
        try {
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mContents != null) {
                annot.setContent(this.mUndoItem.mContents);
            }
            annot.setBorderColor(this.mUndoItem.mColor);
            annot.setOpacity(this.mUndoItem.mOpacity);
            annot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            if (e.getLastError() != PDFError.OOM.getCode()) {
                return false;
            }
            this.mPdfViewCtrl.recoverForOOM();
            return false;
        }
    }

    public boolean delete() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Squiggly)) {
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
