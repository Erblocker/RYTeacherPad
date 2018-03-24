package com.foxit.uiextensions.annots.caret;

import android.graphics.Rect;
import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Caret;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.StrikeOut;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContent;
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutEvent;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;

/* compiled from: CaretUndoItem */
class CaretDeleteUndoItem extends CaretUndoItem {
    boolean mIsReplace;
    TextMarkupContent mTMContent;

    public CaretDeleteUndoItem(PDFViewCtrl pdfViewCtrl) {
        super(pdfViewCtrl);
    }

    public boolean undo() {
        final CaretAddUndoItem undoItem = new CaretAddUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mNM = this.mNM;
        undoItem.mColor = this.mColor;
        undoItem.mOpacity = this.mOpacity;
        undoItem.mBBox = this.mBBox;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mContents = this.mContents;
        undoItem.mModifiedDate = this.mModifiedDate;
        undoItem.mCreationDate = this.mCreationDate;
        undoItem.mFlags = this.mFlags;
        undoItem.mSubject = this.mSubject;
        undoItem.mIntent = this.mIntent;
        try {
            final Annot annot = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex).addAnnot(14, this.mBBox);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CaretEvent(1, undoItem, (Caret) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        try {
                            final PDFPage page = annot.getPage();
                            int pageIndex = page.getIndex();
                            if (!CaretDeleteUndoItem.this.mIsReplace) {
                                DocumentManager.getInstance(CaretDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                            }
                            if (CaretDeleteUndoItem.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                                RectF viewRect = annot.getRect();
                                CaretDeleteUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                                Rect rect = new Rect();
                                viewRect.roundOut(rect);
                                rect.inset(-10, -10);
                                CaretDeleteUndoItem.this.mPdfViewCtrl.refresh(pageIndex, rect);
                            }
                            if (CaretDeleteUndoItem.this.mIsReplace) {
                                AnnotHandler annotHandler = ToolUtil.getAnnotHandlerByType((UIExtensionsManager) CaretDeleteUndoItem.this.mPdfViewCtrl.getUIExtensionsManager(), 12);
                                AnnotContent annotContent = CaretDeleteUndoItem.this.mTMContent;
                                final Annot annot = annot;
                                final CaretAddUndoItem caretAddUndoItem = undoItem;
                                annotHandler.addAnnot(pageIndex, annotContent, false, new Callback() {
                                    public void result(Event event, boolean success) {
                                        if (success) {
                                            StrikeoutEvent strikeoutEvent = (StrikeoutEvent) event;
                                            if (strikeoutEvent.mAnnot != null && (strikeoutEvent.mAnnot instanceof StrikeOut)) {
                                                StrikeOut strikeOut = strikeoutEvent.mAnnot;
                                                try {
                                                    strikeOut.setIntent("StrikeOutTextEdit");
                                                    page.setAnnotGroup(new Markup[]{(Caret) annot, strikeOut}, 0);
                                                    DocumentManager.getInstance(CaretDeleteUndoItem.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                                                    strikeOut.setBorderColor(caretAddUndoItem.mColor);
                                                    strikeOut.setOpacity(caretAddUndoItem.mOpacity);
                                                    strikeOut.resetAppearanceStream();
                                                } catch (PDFException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (PDFException e) {
                            e.printStackTrace();
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
            if (annot == null || !(annot instanceof Caret)) {
                return false;
            }
            CaretAnnotHandler annotHandler = (CaretAnnotHandler) ToolUtil.getAnnotHandlerByType((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager(), 14);
            if (annotHandler == null) {
                return false;
            }
            return annotHandler.deleteAnnot(annot, this, false, null);
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
