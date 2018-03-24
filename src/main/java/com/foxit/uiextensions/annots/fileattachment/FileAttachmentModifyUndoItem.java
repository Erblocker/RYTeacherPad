package com.foxit.uiextensions.annots.fileattachment;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FileAttachment;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: FileAttachmentUndoItem */
class FileAttachmentModifyUndoItem extends FileAttachmentUndoItem {
    public RectF mRedoBbox;
    public int mRedoColor;
    public String mRedoIconName;
    public float mRedoOpacity;
    public RectF mUndoBbox;
    public int mUndoColor;
    public String mUndoIconName;
    public float mUndoOpacity;

    public FileAttachmentModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        return modifyAnnot(this.mUndoColor, this.mUndoOpacity, this.mUndoIconName, this.mUndoBbox);
    }

    public boolean redo() {
        return modifyAnnot(this.mRedoColor, this.mRedoOpacity, this.mRedoIconName, this.mRedoBbox);
    }

    private boolean modifyAnnot(int color, float opacity, String iconName, RectF bbox) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof FileAttachment)) {
                return false;
            }
            final RectF oldBbox = annot.getRect();
            this.mBBox = new RectF(bbox);
            this.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            this.mColor = (long) color;
            this.mOpacity = opacity;
            this.mIconName = iconName;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(2, this, (FileAttachment) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(FileAttachmentModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(FileAttachmentModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(FileAttachmentModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (FileAttachmentModifyUndoItem.this.mPdfViewCtrl.isPageVisible(FileAttachmentModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                FileAttachmentModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, FileAttachmentModifyUndoItem.this.mPageIndex);
                                annotRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                FileAttachmentModifyUndoItem.this.mPdfViewCtrl.refresh(FileAttachmentModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
                                FileAttachmentModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(oldBbox, oldBbox, FileAttachmentModifyUndoItem.this.mPageIndex);
                                oldBbox.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                FileAttachmentModifyUndoItem.this.mPdfViewCtrl.refresh(FileAttachmentModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(oldBbox));
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
