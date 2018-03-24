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
class InkAddUndoItem extends InkUndoItem {
    public InkAddUndoItem(InkAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        InkDeleteUndoItem undoItem = new InkDeleteUndoItem(this.mAnnotHandler, this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mInkLists = InkAnnotUtil.cloneInkList(this.mInkLists);
        try {
            undoItem.mPath = PDFPath.create();
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
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || !(annot instanceof Ink)) {
                return false;
            }
            this.mAnnotHandler.removeAnnot(annot, undoItem, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean redo() {
        try {
            Annot annot = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(15, this.mBBox);
            if (this.mInkLists == null) {
                return false;
            }
            this.mPath = PDFPath.create();
            for (int li = 0; li < this.mInkLists.size(); li++) {
                ArrayList<PointF> line = (ArrayList) this.mInkLists.get(li);
                for (int pi = 0; pi < line.size(); pi++) {
                    if (pi == 0) {
                        this.mPath.moveTo((PointF) line.get(pi));
                    } else {
                        this.mPath.lineTo((PointF) line.get(pi));
                    }
                }
            }
            this.mAnnotHandler.addAnnot(this.mPageIndex, (Ink) annot, this, false, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
