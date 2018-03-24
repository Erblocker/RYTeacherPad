package com.foxit.uiextensions.annots.square;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.BorderInfo;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Square;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;

public class SquareEvent extends EditAnnotEvent {
    public SquareEvent(int eventType, SquareUndoItem undoItem, Square square, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = square;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Square)) {
            return false;
        }
        Square annot = this.mAnnot;
        try {
            annot.setBorderColor(this.mUndoItem.mColor);
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
            BorderInfo borderInfo = new BorderInfo();
            borderInfo.setStyle(this.mUndoItem.mBorderStyle);
            borderInfo.setWidth(this.mUndoItem.mLineWidth);
            annot.setBorderInfo(borderInfo);
            annot.setUniqueID(this.mUndoItem.mNM);
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

    public boolean modify() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Square)) {
            return false;
        }
        Square annot = this.mAnnot;
        try {
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mContents == null) {
                this.mUndoItem.mContents = "";
            }
            annot.setContent(this.mUndoItem.mContents);
            annot.setBorderColor(this.mUndoItem.mColor);
            annot.setOpacity(this.mUndoItem.mOpacity);
            BorderInfo borderInfo = new BorderInfo();
            borderInfo.setWidth(this.mUndoItem.mLineWidth);
            annot.setBorderInfo(borderInfo);
            annot.move(this.mUndoItem.mBBox);
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
        if (this.mAnnot == null || !(this.mAnnot instanceof Square)) {
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
