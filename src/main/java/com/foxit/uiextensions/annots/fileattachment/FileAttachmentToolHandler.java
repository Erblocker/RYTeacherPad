package com.foxit.uiextensions.annots.fileattachment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FileAttachment;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFileSelectDialog;
import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

public class FileAttachmentToolHandler implements ToolHandler {
    private final int MAX_ATTACHMENT_FILE_SIZE = 314572800;
    private int MaxFileSize;
    private HashMap<Annot, String> mAttachmentPath = new HashMap();
    private int mColor;
    private Context mContext;
    private String mIconName;
    private boolean mIsContinuousCreate = false;
    private int mOpacity;
    private String mPath;
    private PDFViewCtrl mPdfViewCtrl;
    private UIFileSelectDialog mfileSelectDialog;

    public FileAttachmentToolHandler(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public String getType() {
        return ToolHandler.TH_TYPE_FileAttachment;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        PointF point = AppAnnotUtil.getPdfPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        switch (action) {
            case 0:
                RectF pageRect = new RectF();
                try {
                    PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
                    pageRect = new RectF(0.0f, page.getHeight(), page.getWidth(), 0.0f);
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                if (point.x < pageRect.left) {
                    point.x = pageRect.left;
                }
                if (point.x > pageRect.right - 20.0f) {
                    point.x = pageRect.right - 20.0f;
                }
                if (point.y < 24.0f) {
                    point.y = 24.0f;
                }
                if (point.y > pageRect.top) {
                    point.y = pageRect.top;
                }
                showFileSelectDialog(pageIndex, point);
                break;
        }
        return true;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
    }

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }

    public void setPaint(int color, int opacity, int FlagType) {
        this.mColor = color;
        this.mOpacity = opacity;
        this.mIconName = FileAttachmentUtil.getIconNames()[FlagType];
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != this || keyCode != 4) {
            return false;
        }
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
        return true;
    }

    protected String getAttachmentPath(final Annot annot) {
        String path = null;
        if (annot != null) {
            path = (String) this.mAttachmentPath.get(annot);
            if (path == null) {
                String fileName = null;
                try {
                    fileName = ((FileAttachment) annot).getFileSpec().getFileName();
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                final String tmpPath = this.mContext.getFilesDir() + "/" + AppDmUtil.randomUUID(null) + fileName;
                FileAttachmentUtil.saveAttachment(this.mPdfViewCtrl, tmpPath, annot, new Callback() {
                    public void result(Event event, boolean success) {
                        FileAttachmentToolHandler.this.mAttachmentPath.put(annot, tmpPath);
                    }
                });
                return tmpPath;
            }
        }
        return path;
    }

    protected void setAttachmentPath(Annot annot, String path) {
        this.mAttachmentPath.put(annot, path);
    }

    protected void addAnnot(int pageIndex, RectF rect, Callback result) {
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            final FileAttachment annot = (FileAttachment) page.addAnnot(17, rect);
            final FileAttachmentAddUndoItem undoItem = new FileAttachmentAddUndoItem(this.mPdfViewCtrl);
            undoItem.mPageIndex = pageIndex;
            undoItem.mNM = AppDmUtil.randomUUID(null);
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mFlags = 4;
            undoItem.mColor = (long) this.mColor;
            undoItem.mOpacity = ((float) AppDmUtil.opacity100To255(this.mOpacity)) / 255.0f;
            undoItem.mIconName = this.mIconName;
            undoItem.mPath = this.mPath;
            undoItem.mBBox = new RectF(rect);
            final int i = pageIndex;
            final RectF rectF = rect;
            final Callback callback = result;
            this.mPdfViewCtrl.addTask(new EditAnnotTask(new FileAttachmentEvent(1, undoItem, annot, this.mPdfViewCtrl), new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(FileAttachmentToolHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        DocumentManager.getInstance(FileAttachmentToolHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        if (FileAttachmentToolHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                            FileAttachmentToolHandler.this.invalidate(i, rectF, callback);
                        }
                    }
                }
            }));
            if (!this.mIsContinuousCreate) {
                ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
            }
            this.mPdfViewCtrl.getDoc().closePage(pageIndex);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void invalidate(int pageIndex, RectF annotRect, Callback result) {
        if (annotRect != null) {
            RectF rectF = new RectF();
            rectF.set(annotRect);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
            Rect rect = new Rect();
            rectF.roundOut(rect);
            this.mPdfViewCtrl.refresh(pageIndex, rect);
        } else if (result != null) {
            result.result(null, true);
        }
    }

    private void showFileSelectDialog(final int pageIndex, PointF pointf) {
        if (this.mfileSelectDialog == null || !this.mfileSelectDialog.isShowing()) {
            final PointF point = new PointF();
            if (pointf != null) {
                point.set(pointf.x, pointf.y);
            }
            this.MaxFileSize = 314572800;
            this.mfileSelectDialog = new UIFileSelectDialog(this.mContext);
            this.mfileSelectDialog.init(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.isHidden() || !pathname.canRead()) {
                        return false;
                    }
                    return true;
                }
            }, true);
            this.mfileSelectDialog.setTitle(R.string.fx_string_open);
            this.mfileSelectDialog.setButton(5);
            this.mfileSelectDialog.setButtonEnable(false, 4);
            this.mfileSelectDialog.setListener(new DialogListener() {
                public void onResult(long btType) {
                    if (btType == 4) {
                        FileAttachmentToolHandler.this.mPath = ((FileItem) FileAttachmentToolHandler.this.mfileSelectDialog.getSelectedFiles().get(0)).path;
                        if (FileAttachmentToolHandler.this.mPath != null && FileAttachmentToolHandler.this.mPath.length() >= 1) {
                            if (new File(FileAttachmentToolHandler.this.mPath).length() > ((long) FileAttachmentToolHandler.this.MaxFileSize)) {
                                Toast.makeText(FileAttachmentToolHandler.this.mContext, String.format(AppResource.getString(FileAttachmentToolHandler.this.mContext, R.string.annot_fat_filesizelimit_meg), new Object[]{Integer.valueOf(FileAttachmentToolHandler.this.MaxFileSize / 1048576)}), 0).show();
                                return;
                            }
                            FileAttachmentToolHandler.this.mfileSelectDialog.dismiss();
                            FileAttachmentToolHandler.this.addAnnot(pageIndex, new RectF(point.x, point.y, point.x + 20.0f, point.y - 24.0f), null);
                        }
                    } else if (btType == 1) {
                        FileAttachmentToolHandler.this.mfileSelectDialog.dismiss();
                    }
                }

                public void onBackClick() {
                }
            });
            this.mfileSelectDialog.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == 4) {
                        FileAttachmentToolHandler.this.mfileSelectDialog.dismiss();
                    }
                    return true;
                }
            });
            this.mfileSelectDialog.showDialog(false);
        }
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public int getColor() {
        return this.mColor;
    }

    public void setOpacity(int opacity) {
        this.mOpacity = opacity;
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    public String getIconName() {
        return this.mIconName;
    }

    public void setIconName(String iconName) {
        this.mIconName = iconName;
    }
}
