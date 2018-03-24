package com.foxit.uiextensions.annots.freetext.typewriter;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.Font;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FreeText;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: TypewriterUndoItem */
class TypewriterModifyUndoItem extends TypewriterUndoItem {
    Font mOldFont;
    float mOldFontSize;
    long mOldTextColor;

    public TypewriterModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        super(pdfViewCtrl);
    }

    public boolean undo() {
        TypewriterModifyUndoItem undoItem = new TypewriterModifyUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mNM = this.mNM;
        undoItem.mTextColor = this.mOldTextColor;
        undoItem.mOpacity = this.mOldOpacity;
        undoItem.mFont = this.mOldFont;
        undoItem.mFontSize = this.mOldFontSize;
        undoItem.mContents = this.mOldContents;
        undoItem.mBBox = new RectF(this.mOldBBox);
        undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
        return modifyAnnot(undoItem);
    }

    public boolean redo() {
        return modifyAnnot(this);
    }

    private boolean modifyAnnot(TypewriterModifyUndoItem undoItem) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof FreeText)) {
                return false;
            }
            if (((FreeText) annot).getIntent() == null || !((FreeText) annot).getIntent().equals("FreeTextTypewriter")) {
                return false;
            }
            final RectF oldBbox = annot.getRect();
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new TypewriterEvent(2, undoItem, (FreeText) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(TypewriterModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(TypewriterModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(TypewriterModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (TypewriterModifyUndoItem.this.mPdfViewCtrl.isPageVisible(TypewriterModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                TypewriterModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, TypewriterModifyUndoItem.this.mPageIndex);
                                annotRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                TypewriterModifyUndoItem.this.mPdfViewCtrl.refresh(TypewriterModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
                                TypewriterModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(oldBbox, oldBbox, TypewriterModifyUndoItem.this.mPageIndex);
                                oldBbox.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                TypewriterModifyUndoItem.this.mPdfViewCtrl.refresh(TypewriterModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(oldBbox));
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
