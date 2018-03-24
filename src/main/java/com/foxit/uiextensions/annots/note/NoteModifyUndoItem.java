package com.foxit.uiextensions.annots.note;

import android.graphics.RectF;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Note;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

/* compiled from: NoteUndoItem */
class NoteModifyUndoItem extends NoteUndoItem {
    public RectF mRedoBbox;
    public int mRedoColor;
    public String mRedoContent;
    public String mRedoIconName;
    public float mRedoOpacity;
    public RectF mUndoBbox;
    public int mUndoColor;
    public String mUndoContent;
    public String mUndoIconName;
    public float mUndoOpacity;

    public NoteModifyUndoItem(PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean undo() {
        return modifyAnnot(this.mUndoColor, this.mUndoOpacity, this.mUndoContent, this.mUndoIconName, this.mUndoBbox);
    }

    public boolean redo() {
        return modifyAnnot(this.mRedoColor, this.mRedoOpacity, this.mRedoContent, this.mRedoIconName, this.mRedoBbox);
    }

    private boolean modifyAnnot(int color, float opacity, String content, String iconName, RectF bbox) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mPageIndex);
            final Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(page, this.mNM);
            if (annot == null || !(annot instanceof Note)) {
                return false;
            }
            final RectF oldBbox = annot.getRect();
            this.mBBox = new RectF(bbox);
            this.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            this.mColor = (long) color;
            this.mOpacity = opacity;
            this.mIconName = iconName;
            this.mContents = content;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new NoteEvent(2, this, (Note) annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        if (annot == DocumentManager.getInstance(NoteModifyUndoItem.this.mPdfViewCtrl).getCurrentAnnot()) {
                            DocumentManager.getInstance(NoteModifyUndoItem.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        DocumentManager.getInstance(NoteModifyUndoItem.this.mPdfViewCtrl).onAnnotModified(page, annot);
                        if (NoteModifyUndoItem.this.mPdfViewCtrl.isPageVisible(NoteModifyUndoItem.this.mPageIndex)) {
                            try {
                                RectF annotRect = annot.getRect();
                                NoteModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, annotRect, NoteModifyUndoItem.this.mPageIndex);
                                annotRect.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                NoteModifyUndoItem.this.mPdfViewCtrl.refresh(NoteModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(annotRect));
                                NoteModifyUndoItem.this.mPdfViewCtrl.convertPdfRectToPageViewRect(oldBbox, oldBbox, NoteModifyUndoItem.this.mPageIndex);
                                oldBbox.inset((float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3), (float) ((-AppAnnotUtil.getAnnotBBoxSpace()) - 3));
                                NoteModifyUndoItem.this.mPdfViewCtrl.refresh(NoteModifyUndoItem.this.mPageIndex, AppDmUtil.rectFToRect(oldBbox));
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
