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
class InkModifyUndoItem extends InkUndoItem {
    public InkModifyUndoItem(InkAnnotHandler annotHandler, PDFViewCtrl pdfViewCtrl) {
        super(annotHandler, pdfViewCtrl);
    }

    public boolean undo() {
        return modifyAnnot(true);
    }

    public boolean redo() {
        return modifyAnnot(false);
    }

    private boolean modifyAnnot(boolean userOldValue) {
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || !(annot instanceof Ink)) {
                return false;
            }
            int li;
            ArrayList<PointF> line;
            int pi;
            if (userOldValue) {
                if (this.mOldInkLists != null) {
                    this.mOldPath = PDFPath.create();
                    for (li = 0; li < this.mOldInkLists.size(); li++) {
                        line = (ArrayList) this.mOldInkLists.get(li);
                        for (pi = 0; pi < line.size(); pi++) {
                            if (pi == 0) {
                                this.mOldPath.moveTo((PointF) line.get(pi));
                            } else {
                                this.mOldPath.lineTo((PointF) line.get(pi));
                            }
                        }
                    }
                }
            } else if (this.mInkLists != null) {
                this.mPath = PDFPath.create();
                for (li = 0; li < this.mInkLists.size(); li++) {
                    line = (ArrayList) this.mInkLists.get(li);
                    for (pi = 0; pi < line.size(); pi++) {
                        if (pi == 0) {
                            this.mPath.moveTo((PointF) line.get(pi));
                        } else {
                            this.mPath.lineTo((PointF) line.get(pi));
                        }
                    }
                }
            }
            this.mAnnotHandler.modifyAnnot((Ink) annot, this, userOldValue, false, true, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
