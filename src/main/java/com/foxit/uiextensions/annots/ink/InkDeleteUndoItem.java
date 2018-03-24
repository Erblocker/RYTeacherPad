package com.foxit.uiextensions.annots.ink;

import android.graphics.PointF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.PDFPath;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Ink;
import com.foxit.uiextensions.DocumentManager;
import java.util.ArrayList;

/* compiled from: InkUndoItem */
class InkDeleteUndoItem extends InkUndoItem {
    public InkDeleteUndoItem(InkAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        try {
            Ink annot = (Ink) this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(15, this.mBBox);
            InkAddUndoItem undoItem = new InkAddUndoItem(this.mAnnotHandler, this.mPdfViewCtrl);
            undoItem.mNM = this.mNM;
            undoItem.mPageIndex = this.mPageIndex;
            undoItem.mAuthor = this.mAuthor;
            undoItem.mFlags = this.mFlags;
            undoItem.mSubject = this.mSubject;
            undoItem.mCreationDate = this.mCreationDate;
            undoItem.mModifiedDate = this.mModifiedDate;
            undoItem.mColor = this.mColor;
            undoItem.mOpacity = this.mOpacity;
            undoItem.mLineWidth = this.mLineWidth;
            undoItem.mIntent = this.mIntent;
            undoItem.mBBox = this.mBBox;
            undoItem.mContents = this.mContents;
            undoItem.mPath = PDFPath.create();
            if (this.mInkLists != null) {
                for (int li = 0; li < this.mInkLists.size(); li++) {
                    ArrayList<PointF> line = (ArrayList) this.mInkLists.get(li);
                    for (int pi = 0; pi < line.size(); pi++) {
                        if (pi == 0) {
                            undoItem.mPath.moveTo((PointF) line.get(pi));
                        } else {
                            undoItem.mPath.lineTo((PointF) line.get(pi));
                        }
                    }
                }
            }
            this.mAnnotHandler.addAnnot(this.mPageIndex, annot, undoItem, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean redo() {
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || !(annot instanceof Ink)) {
                return false;
            }
            this.mAnnotHandler.removeAnnot(annot, this, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
