package com.foxit.uiextensions.annots.ink;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.BorderInfo;
import com.foxit.sdk.pdf.annots.Ink;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;

public class InkEvent extends EditAnnotEvent {
    public InkEvent(int eventType, InkUndoItem undoItem, Ink ink, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = ink;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Ink)) {
            return false;
        }
        Ink annot = this.mAnnot;
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
            if (this.mUndoItem.mIntent != null) {
                annot.setIntent(this.mUndoItem.mIntent);
            }
            if (this.mUndoItem.mSubject != null) {
                annot.setSubject(this.mUndoItem.mSubject);
            }
            if (((InkAddUndoItem) this.mUndoItem).mPath != null) {
                annot.setInkList(((InkAddUndoItem) this.mUndoItem).mPath);
            }
            BorderInfo borderInfo = new BorderInfo();
            borderInfo.setWidth(this.mUndoItem.mLineWidth);
            annot.setBorderInfo(borderInfo);
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
        if (this.mAnnot == null || !(this.mAnnot instanceof Ink)) {
            return false;
        }
        Ink annot = this.mAnnot;
        try {
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            BorderInfo borderInfo;
            if (this.useOldValue) {
                annot.setBorderColor(this.mUndoItem.mOldColor);
                annot.setOpacity(this.mUndoItem.mOldOpacity);
                borderInfo = new BorderInfo();
                borderInfo.setWidth(this.mUndoItem.mOldLineWidth);
                annot.setBorderInfo(borderInfo);
                if (((InkModifyUndoItem) this.mUndoItem).mOldPath != null) {
                    annot.setInkList(((InkModifyUndoItem) this.mUndoItem).mOldPath);
                }
                if (this.mUndoItem.mOldContents != null) {
                    annot.setContent(this.mUndoItem.mOldContents);
                } else {
                    annot.setContent("");
                }
            } else {
                annot.setBorderColor(this.mUndoItem.mColor);
                annot.setOpacity(this.mUndoItem.mOpacity);
                borderInfo = new BorderInfo();
                borderInfo.setWidth(this.mUndoItem.mLineWidth);
                annot.setBorderInfo(borderInfo);
                if (((InkModifyUndoItem) this.mUndoItem).mPath != null) {
                    annot.setInkList(((InkModifyUndoItem) this.mUndoItem).mPath);
                }
                if (this.mUndoItem.mContents != null) {
                    annot.setContent(this.mUndoItem.mContents);
                } else {
                    annot.setContent("");
                }
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
        if (this.mAnnot == null || !(this.mAnnot instanceof Ink)) {
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
