package com.foxit.uiextensions.annots.freetext.typewriter;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FreeText;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;

/* compiled from: TypewriterUndoItem */
class TypewriterAddUndoItem extends TypewriterUndoItem {
    public TypewriterAddUndoItem(PDFViewCtrl pdfViewCtrl) {
        super(pdfViewCtrl);
    }

    public boolean undo() {
        TypewriterDeleteUndoItem undoItem = new TypewriterDeleteUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mPageIndex = this.mPageIndex;
        try {
            Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex), this.mNM);
            if (annot == null || !(annot instanceof FreeText)) {
                return false;
            }
            if (((FreeText) annot).getIntent() == null || !((FreeText) annot).getIntent().equals("FreeTextTypewriter")) {
                return false;
            }
            TypewriterAnnotHandler annotHandler = (TypewriterAnnotHandler) ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), 3);
            if (annotHandler == null) {
                return false;
            }
            annotHandler.deleteAnnot(annot, undoItem, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean redo() {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = page.addAnnot(3, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new TypewriterEvent(1, this, (FreeText) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(TypewriterAddUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (TypewriterAddUndoItem.this.mPdfViewCtrl.isPageVisible(TypewriterAddUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                TypewriterAddUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, TypewriterAddUndoItem.this.mPageIndex);
                                TypewriterAddUndoItem.this.mPdfViewCtrl.refresh(TypewriterAddUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }));
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
