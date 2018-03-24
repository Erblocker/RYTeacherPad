package com.foxit.uiextensions.annots.caret;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Caret;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.objects.PDFObject;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;
import com.foxit.uiextensions.utils.AppAnnotUtil;

public class CaretEvent extends EditAnnotEvent {
    public CaretEvent(int eventType, CaretUndoItem undoItem, Caret caret, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = caret;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Caret)) {
            return false;
        }
        try {
            Caret annot = this.mAnnot;
            annot.setBorderColor(this.mUndoItem.mColor);
            annot.setOpacity(this.mUndoItem.mOpacity);
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
            if (this.mUndoItem.mSubject != null) {
                annot.setSubject(this.mUndoItem.mSubject);
            }
            annot.setIntent(this.mUndoItem.mIntent);
            annot.setUniqueID(this.mUndoItem.mNM);
            int rotate = ((CaretAddUndoItem) this.mUndoItem).mRotate;
            if (rotate < 0 || rotate > 4) {
                rotate = 0;
            }
            annot.getDict().setAt("Rotate", PDFObject.createFromInteger(360 - (rotate * 90)));
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
        if (this.mAnnot == null || !(this.mAnnot instanceof Caret)) {
            return false;
        }
        try {
            Caret annot = this.mAnnot;
            annot.setBorderColor(this.mUndoItem.mColor);
            annot.setOpacity(this.mUndoItem.mOpacity);
            if (this.mUndoItem.mContents != null) {
                annot.setContent(this.mUndoItem.mContents);
            }
            annot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Caret)) {
            return false;
        }
        try {
            PDFPage page = this.mAnnot.getPage();
            if (AppAnnotUtil.isReplaceCaret(this.mAnnot)) {
                Caret caret = this.mAnnot;
                for (int i = caret.getGroupElementCount() - 1; i >= 0; i--) {
                    Markup groupAnnot = caret.getGroupElement(i);
                    if (groupAnnot.getType() == 12) {
                        page.removeAnnot(groupAnnot);
                        break;
                    }
                }
            }
            page.removeAnnot(this.mAnnot);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
