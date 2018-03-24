package com.foxit.uiextensions.annots.line;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.BorderInfo;
import com.foxit.sdk.pdf.annots.Line;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;

public class LineEvent extends EditAnnotEvent {
    public LineEvent(int eventType, LineUndoItem undoItem, Line line, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = line;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Line)) {
            return false;
        }
        Line annot = this.mAnnot;
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
            LineUndoItem undoItem = this.mUndoItem;
            annot.setStartPoint(undoItem.mStartPt);
            annot.setEndPoint(undoItem.mEndPt);
            if (undoItem.mStartingStyle != null) {
                annot.setLineStartingStyle(undoItem.mStartingStyle);
            }
            if (undoItem.mEndingStyle != null) {
                annot.setLineEndingStyle(undoItem.mEndingStyle);
            }
            BorderInfo borderInfo = new BorderInfo();
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
        if (this.mAnnot == null || !(this.mAnnot instanceof Line)) {
            return false;
        }
        Line annot = this.mAnnot;
        try {
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            BorderInfo borderInfo;
            LineModifyUndoItem undoItem;
            if (this.useOldValue) {
                annot.setBorderColor(this.mUndoItem.mOldColor);
                annot.setOpacity(this.mUndoItem.mOldOpacity);
                borderInfo = new BorderInfo();
                borderInfo.setWidth(this.mUndoItem.mOldLineWidth);
                annot.setBorderInfo(borderInfo);
                undoItem = (LineModifyUndoItem) this.mUndoItem;
                annot.setStartPoint(undoItem.mOldStartPt);
                annot.setEndPoint(undoItem.mOldEndPt);
                if (undoItem.mOldStartingStyle != null) {
                    annot.setLineStartingStyle(undoItem.mOldStartingStyle);
                }
                if (undoItem.mOldEndingStyle != null) {
                    annot.setLineEndingStyle(undoItem.mOldEndingStyle);
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
                undoItem = this.mUndoItem;
                if (!(undoItem.mStartPt.equals(0.0f, 0.0f) && undoItem.mEndPt.equals(0.0f, 0.0f))) {
                    annot.setStartPoint(undoItem.mStartPt);
                    annot.setEndPoint(undoItem.mEndPt);
                }
                if (undoItem.mStartingStyle != null) {
                    annot.setLineStartingStyle(undoItem.mStartingStyle);
                }
                if (undoItem.mEndingStyle != null) {
                    annot.setLineEndingStyle(undoItem.mEndingStyle);
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
            if (e.getLastError() != PDFError.OOM.getCode()) {
                return false;
            }
            this.mPdfViewCtrl.recoverForOOM();
            return false;
        }
    }

    public boolean delete() {
        if (this.mAnnot == null || !(this.mAnnot instanceof Line)) {
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
