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
class TypewriterDeleteUndoItem extends TypewriterUndoItem {
    public TypewriterDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        super(pdfViewCtrl);
    }

    public boolean undo() {
        TypewriterAddUndoItem undoItem = new TypewriterAddUndoItem(this.mPdfViewCtrl);
        undoItem.mNM = this.mNM;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mBBox = new RectF(this.mBBox);
        undoItem.mColor = this.mColor;
        undoItem.mContents = this.mContents;
        undoItem.mModifiedDate = this.mModifiedDate;
        undoItem.mOpacity = this.mOpacity;
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mFlags = this.mFlags;
        undoItem.mFont = this.mFont;
        undoItem.mFontSize = this.mFontSize;
        undoItem.mTextColor = this.mTextColor;
        undoItem.mDaFlags = this.mDaFlags;
        undoItem.mIntent = this.mIntent;
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final FreeText annot = (FreeText) page.addAnnot(3, new RectF(this.mBBox));
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new TypewriterEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(TypewriterDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        if (TypewriterDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(TypewriterDeleteUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRectF = annot.getRect();
                                TypewriterDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, TypewriterDeleteUndoItem.this.mPageIndex);
                                TypewriterDeleteUndoItem.this.mPdfViewCtrl.refresh(TypewriterDeleteUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRectF));
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

    public boolean redo() {
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
            annotHandler.deleteAnnot(annot, this, null);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
