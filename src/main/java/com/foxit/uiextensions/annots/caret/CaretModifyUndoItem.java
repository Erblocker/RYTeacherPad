package com.foxit.uiextensions.annots.caret;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Caret;
import com.foxit.sdk.pdf.annots.StrikeOut;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;

/* compiled from: CaretUndoItem */
class CaretModifyUndoItem extends CaretUndoItem {
    public int mLastColor;
    public String mLastContent;
    public float mLastOpacity;

    public CaretModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        super(pdfViewCtrl);
    }

    public boolean undo() {
        CaretModifyUndoItem undoItem = new CaretModifyUndoItem(this.mPdfViewCtrl);
        undoItem.mPageIndex = this.mPageIndex;
        undoItem.mNM = this.mNM;
        undoItem.mColor = (long) this.mLastColor;
        undoItem.mOpacity = this.mLastOpacity;
        undoItem.mBBox = this.mBBox;
        undoItem.mAuthor = this.mAuthor;
        undoItem.mContents = this.mLastContent;
        undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
        undoItem.mIntent = this.mIntent;
        return modifyAnnot(undoItem);
    }

    public boolean redo() {
        return modifyAnnot(this);
    }

    private boolean modifyAnnot(final CaretModifyUndoItem undoItem) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Caret)) {
                return false;
            }
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new CaretEvent(2, undoItem, (Caret) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(CaretModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (CaretModifyUndoItem.this.mPdfViewCtrl.isPageVisible(CaretModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF viewRect = annot.getRect();
                                CaretModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, CaretModifyUndoItem.this.mPageIndex);
                                viewRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                CaretModifyUndoItem.this.mPdfViewCtrl.refresh(CaretModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(viewRect));
                            } catch (PDFException e) {
                                return;
                            }
                        }
                        if (AppAnnotUtil.isReplaceCaret(annot)) {
                            final StrikeOut subAnnot = CaretAnnotHandler.getStrikeOutFromCaret(annot);
                            if (subAnnot != null) {
                                AnnotHandler annotHandler = ToolUtil.getAnnotHandlerByType((UIExtensionsManager) CaretModifyUndoItem.this.mPdfViewCtrl.getUIExtensionsManager(), 12);
                                final CaretModifyUndoItem caretModifyUndoItem = undoItem;
                                final Annot annot = annot;
                                annotHandler.modifyAnnot(subAnnot, new AnnotContent() {
                                    public int getPageIndex() {
                                        return caretModifyUndoItem.mPageIndex;
                                    }

                                    public int getType() {
                                        return 12;
                                    }

                                    public String getNM() {
                                        try {
                                            return subAnnot.getUniqueID();
                                        } catch (PDFException e) {
                                            return null;
                                        }
                                    }

                                    public RectF getBBox() {
                                        try {
                                            return subAnnot.getRect();
                                        } catch (PDFException e) {
                                            return null;
                                        }
                                    }

                                    public int getColor() {
                                        return (int) caretModifyUndoItem.mColor;
                                    }

                                    public int getOpacity() {
                                        return (int) ((caretModifyUndoItem.mOpacity * 255.0f) + 0.5f);
                                    }

                                    public float getLineWidth() {
                                        try {
                                            return subAnnot.getBorderInfo().getWidth();
                                        } catch (PDFException e) {
                                            return 0.0f;
                                        }
                                    }

                                    public String getSubject() {
                                        try {
                                            return subAnnot.getSubject();
                                        } catch (PDFException e) {
                                            return null;
                                        }
                                    }

                                    public DateTime getModifiedDate() {
                                        try {
                                            return annot.getModifiedDateTime();
                                        } catch (PDFException e) {
                                            return null;
                                        }
                                    }

                                    public String getContents() {
                                        try {
                                            return annot.getContent();
                                        } catch (PDFException e) {
                                            return null;
                                        }
                                    }

                                    public String getIntent() {
                                        try {
                                            return subAnnot.getIntent();
                                        } catch (PDFException e) {
                                            return null;
                                        }
                                    }
                                }, false, null);
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
