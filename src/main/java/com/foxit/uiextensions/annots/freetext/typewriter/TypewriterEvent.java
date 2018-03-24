package com.foxit.uiextensions.annots.freetext.typewriter;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.DefaultAppearance;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.FreeText;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;
import com.foxit.uiextensions.utils.AppDmUtil;

public class TypewriterEvent extends EditAnnotEvent {
    public TypewriterEvent(int eventType, TypewriterUndoItem undoItem, FreeText typewriter, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = typewriter;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof FreeText)) {
            return false;
        }
        FreeText annot = this.mAnnot;
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
            TypewriterAddUndoItem undoItem = this.mUndoItem;
            DefaultAppearance da = new DefaultAppearance();
            da.set(undoItem.mDaFlags, undoItem.mFont, undoItem.mFontSize, undoItem.mTextColor);
            annot.setDefaultAppearance(da);
            annot.setIntent(this.mUndoItem.mIntent);
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
        if (this.mAnnot == null || !(this.mAnnot instanceof FreeText)) {
            return false;
        }
        try {
            if (((FreeText) this.mAnnot).getIntent() == null || !((FreeText) this.mAnnot).getIntent().equals("FreeTextTypewriter")) {
                return false;
            }
            FreeText annot = this.mAnnot;
            TypewriterModifyUndoItem undoItem = this.mUndoItem;
            DefaultAppearance da = annot.getDefaultAppearance();
            da.setTextColor(undoItem.mTextColor);
            da.setFont(undoItem.mFont);
            da.setFontSize(undoItem.mFontSize);
            annot.setDefaultAppearance(da);
            annot.setOpacity(undoItem.mOpacity);
            annot.move(this.mUndoItem.mBBox);
            annot.setModifiedDateTime(AppDmUtil.currentDateToDocumentDate());
            annot.setContent(undoItem.mContents);
            annot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete() {
        if (this.mAnnot == null || !(this.mAnnot instanceof FreeText)) {
            return false;
        }
        try {
            if (((FreeText) this.mAnnot).getIntent() == null || !((FreeText) this.mAnnot).getIntent().equals("FreeTextTypewriter")) {
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
