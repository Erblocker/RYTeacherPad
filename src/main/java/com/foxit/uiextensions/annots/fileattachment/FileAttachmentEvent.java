package com.foxit.uiextensions.annots.fileattachment;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.FileSpec;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.FileAttachment;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;
import com.foxit.uiextensions.utils.AppDmUtil;
import java.io.File;
import java.util.Calendar;

public class FileAttachmentEvent extends EditAnnotEvent {
    public FileAttachmentEvent(int eventType, FileAttachmentUndoItem undoItem, FileAttachment highlight, PDFViewCtrl pdfViewCtrl) {
        this.mType = eventType;
        this.mUndoItem = undoItem;
        this.mAnnot = highlight;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public boolean add() {
        if (this.mAnnot == null || !(this.mAnnot instanceof FileAttachment)) {
            return false;
        }
        FileAttachment annot = this.mAnnot;
        FileAttachmentUndoItem undoItem = this.mUndoItem;
        try {
            annot.setBorderColor(this.mUndoItem.mColor);
            annot.setOpacity(this.mUndoItem.mOpacity);
            if (this.mUndoItem.mContents != null) {
                annot.setContent(this.mUndoItem.mContents);
            }
            annot.setFlags(this.mUndoItem.mFlags);
            if (this.mUndoItem.mCreationDate != null) {
                annot.setCreationDateTime(this.mUndoItem.mCreationDate);
            }
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mAuthor != null) {
                annot.setTitle(this.mUndoItem.mAuthor);
            }
            annot.setIconName(undoItem.mIconName);
            annot.setFlags(this.mUndoItem.mFlags);
            annot.setUniqueID(this.mUndoItem.mNM);
            File file = new File(undoItem.mPath);
            FileSpec fileSpec = FileSpec.create(this.mPdfViewCtrl.getDoc());
            annot.setSubject("FileAttachment");
            annot.setContent(file.getName());
            fileSpec.setFileName(file.getName());
            fileSpec.embed(undoItem.mPath);
            Calendar cal = Calendar.getInstance();
            long time = file.lastModified();
            cal.setTimeInMillis(time);
            fileSpec.setModifiedDateTime(AppDmUtil.javaDateToDocumentDate(time));
            annot.setFileSpec(fileSpec);
            annot.resetAppearanceStream();
            FileAttachmentToolHandler toolHandler = (FileAttachmentToolHandler) ((FileAttachmentModule) ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_FILEATTACHMENT)).getToolHandler();
            if (toolHandler != null) {
                toolHandler.setAttachmentPath(annot, undoItem.mPath);
            }
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            if (e.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            return false;
        }
    }

    public boolean modify() {
        if (this.mAnnot == null || !(this.mAnnot instanceof FileAttachment)) {
            return false;
        }
        FileAttachment annot = this.mAnnot;
        try {
            if (this.mUndoItem.mModifiedDate != null) {
                annot.setModifiedDateTime(this.mUndoItem.mModifiedDate);
            }
            if (this.mUndoItem.mContents == null) {
                this.mUndoItem.mContents = "";
            }
            annot.setBorderColor(this.mUndoItem.mColor);
            annot.setOpacity(this.mUndoItem.mOpacity);
            annot.setIconName(((FileAttachmentModifyUndoItem) this.mUndoItem).mIconName);
            annot.move(this.mUndoItem.mBBox);
            annot.resetAppearanceStream();
            return true;
        } catch (PDFException e) {
            if (e.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
            return false;
        }
    }

    public boolean delete() {
        if (this.mAnnot == null || !(this.mAnnot instanceof FileAttachment)) {
            return false;
        }
        try {
            this.mAnnot.getPage().removeAnnot(this.mAnnot);
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
