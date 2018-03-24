package com.foxit.uiextensions.annots;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.BorderInfo;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;

/* compiled from: DefaultAnnotHandler */
class DefaultAnnotEvent extends EditAnnotEvent {
    public DefaultAnnotEvent(int eventType, DefaultAnnotUndoItem undoItem, Annot annot, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = annot;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null) {
            return false;
        }
        try {
            if (this.mAnnot.getType() != this.mUndoItem.mType) {
                return false;
            }
            this.mAnnot.setBorderColor(this.mUndoItem.mColor);
            ((Markup) this.mAnnot).setOpacity(this.mUndoItem.mOpacity);
            if (this.mUndoItem.mContents == null) {
                this.mUndoItem.mContents = "";
            }
            this.mAnnot.setContent(this.mUndoItem.mContents);
            this.mAnnot.setFlags(this.mUndoItem.mFlags);
            if (this.mUndoItem.mCreationDate != null) {
                ((Markup) this.mAnnot).setCreationDateTime(this.mUndoItem.mCreationDate);
            }
            if (this.mUndoItem.mModifiedDate != null) {
                this.mAnnot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mAuthor != null) {
                ((Markup) this.mAnnot).setTitle(this.mUndoItem.mAuthor);
            }
            if (this.mUndoItem.mIntent != null) {
                ((Markup) this.mAnnot).setIntent(this.mUndoItem.mIntent);
            }
            if (this.mUndoItem.mSubject != null) {
                ((Markup) this.mAnnot).setSubject(this.mUndoItem.mSubject);
            }
            BorderInfo borderInfo = new BorderInfo();
            borderInfo.setWidth(this.mUndoItem.mLineWidth);
            this.mAnnot.setBorderInfo(borderInfo);
            this.mAnnot.setFlags(this.mUndoItem.mFlags);
            this.mAnnot.setUniqueID(this.mUndoItem.mNM);
            this.mAnnot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            if (e.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            return false;
        }
    }

    public boolean modify() {
        if (this.mAnnot == null) {
            return false;
        }
        Markup annot = this.mAnnot;
        try {
            if (this.mAnnot.getType() != this.mUndoItem.mType) {
                return false;
            }
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
                if (this.mUndoItem.mOldContents == null) {
                    this.mUndoItem.mOldContents = "";
                }
                annot.setContent(this.mUndoItem.mOldContents);
            } else {
                annot.setBorderColor(this.mUndoItem.mColor);
                annot.setOpacity(this.mUndoItem.mOpacity);
                borderInfo = new BorderInfo();
                borderInfo.setWidth(this.mUndoItem.mLineWidth);
                annot.setBorderInfo(borderInfo);
                if (this.mUndoItem.mContents == null) {
                    this.mUndoItem.mContents = "";
                }
                annot.setContent(this.mUndoItem.mContents);
            }
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
        if (this.mAnnot == null) {
            return false;
        }
        try {
            if (this.mAnnot.getType() != this.mUndoItem.mType) {
                return false;
            }
            ((Markup) this.mAnnot).removeAllReplies();
            this.mAnnot.getPage().removeAnnot(this.mAnnot);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
