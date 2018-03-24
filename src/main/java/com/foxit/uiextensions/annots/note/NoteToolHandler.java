package com.foxit.uiextensions.annots.note;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.Note;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;

public class NoteToolHandler implements ToolHandler {
    private Button mCancel;
    private int mColor;
    private Context mContext;
    private TextView mDialog_title;
    private AppDisplay mDisplay;
    private EditText mET_Content;
    private String mIconType;
    private boolean mIsContinuousCreate;
    private int mOpacity;
    private PDFViewCtrl mPdfViewCtrl;
    private Button mSave;

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }

    public NoteToolHandler(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mDisplay = new AppDisplay(context);
        this.mPdfViewCtrl = pdfViewCtrl;
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    public void onDraw(int pageIndex, Canvas canvas) {
    }

    @SuppressLint({"NewApi"})
    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        PointF pageViewPt = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        switch (action) {
            case 0:
                initDialog(pageIndex, pageViewPt);
                return true;
            default:
                return false;
        }
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public void initDialog(final int pageIndex, final PointF point) {
        Context context = this.mContext;
        View mView = View.inflate(context, R.layout.rd_note_dialog_edit, null);
        this.mDialog_title = (TextView) mView.findViewById(R.id.rd_note_dialog_edit_title);
        this.mET_Content = (EditText) mView.findViewById(R.id.rd_note_dialog_edit);
        this.mCancel = (Button) mView.findViewById(R.id.rd_note_dialog_edit_cancel);
        this.mSave = (Button) mView.findViewById(R.id.rd_note_dialog_edit_ok);
        mView.setLayoutParams(new LayoutParams(-1, -2));
        final Dialog dialog = new Dialog(context, R.style.rv_dialog_style);
        dialog.setContentView(mView, new LayoutParams(this.mDisplay.getUITextEditDialogWidth(), -2));
        this.mET_Content.setMaxLines(10);
        dialog.getWindow().setFlags(1024, 1024);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
        this.mDialog_title.setText(this.mContext.getResources().getString(R.string.fx_string_note));
        this.mSave.setEnabled(false);
        this.mSave.setTextColor(this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
        this.mET_Content.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (NoteToolHandler.this.mET_Content.getText().length() == 0) {
                    NoteToolHandler.this.mSave.setEnabled(false);
                    NoteToolHandler.this.mSave.setTextColor(NoteToolHandler.this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
                    return;
                }
                NoteToolHandler.this.mSave.setEnabled(true);
                NoteToolHandler.this.mSave.setTextColor(NoteToolHandler.this.mContext.getResources().getColor(R.color.dlg_bt_text_selector));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.mCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                AppUtil.dismissInputSoft(NoteToolHandler.this.mET_Content);
            }
        });
        this.mSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PointF pdfPoint = new PointF(point.x, point.y);
                NoteToolHandler.this.mPdfViewCtrl.convertPageViewPtToPdfPt(point, pdfPoint, pageIndex);
                RectF rect = new RectF(pdfPoint.x - 10.0f, pdfPoint.y + 10.0f, pdfPoint.x + 10.0f, pdfPoint.y - 10.0f);
                try {
                    Annot annot = NoteToolHandler.this.mPdfViewCtrl.getDoc().getPage(pageIndex).addAnnot(1, rect);
                    if (annot == null) {
                        if (!NoteToolHandler.this.mIsContinuousCreate) {
                            ((UIExtensionsManager) NoteToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                        }
                        dialog.dismiss();
                        return;
                    }
                    NoteAddUndoItem undoItem = new NoteAddUndoItem(NoteToolHandler.this.mPdfViewCtrl);
                    undoItem.mPageIndex = pageIndex;
                    undoItem.mNM = AppDmUtil.randomUUID(null);
                    undoItem.mContents = NoteToolHandler.this.mET_Content.getText().toString();
                    undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
                    undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
                    undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
                    undoItem.mFlags = 28;
                    undoItem.mColor = (long) NoteToolHandler.this.mColor;
                    undoItem.mOpacity = ((float) AppDmUtil.opacity100To255(NoteToolHandler.this.mOpacity)) / 255.0f;
                    undoItem.mOpenStatus = false;
                    undoItem.mIconName = NoteToolHandler.this.mIconType;
                    undoItem.mBBox = new RectF(rect);
                    NoteToolHandler.this.addAnnot(pageIndex, annot, undoItem, true, null);
                    dialog.dismiss();
                    AppUtil.dismissInputSoft(NoteToolHandler.this.mET_Content);
                    if (!NoteToolHandler.this.mIsContinuousCreate) {
                        ((UIExtensionsManager) NoteToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
        AppUtil.showSoftInput(this.mET_Content);
    }

    protected void addAnnot(int pageIndex, NoteAnnotContent content, boolean addUndo, Callback result) {
        if (content.getFromType().equals(Module.MODULE_NAME_SELECTION)) {
            PointF point = new PointF(content.getBBox().left, content.getBBox().top);
            this.mPdfViewCtrl.convertPdfPtToPageViewPt(point, point, pageIndex);
            initDialog(pageIndex, point);
        } else if (content.getFromType().equals(Module.MODULE_NAME_REPLY)) {
            NoteAddUndoItem undoItem = new NoteAddUndoItem(this.mPdfViewCtrl);
            undoItem.setCurrentValue((AnnotContent) content);
            undoItem.mIsFromReplyModule = true;
            undoItem.mParentNM = content.getParentNM();
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            try {
                int i = pageIndex;
                addAnnot(i, ((Markup) DocumentManager.getInstance(this.mPdfViewCtrl).getAnnot(this.mPdfViewCtrl.getDoc().getPage(pageIndex), undoItem.mParentNM)).addReply(), undoItem, addUndo, result);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        } else if (result != null) {
            result.result(null, false);
        }
    }

    protected void addAnnot(int pageIndex, Annot annot, NoteAddUndoItem undoItem, boolean addUndo, Callback result) {
        final Annot annot2 = annot;
        final boolean z = addUndo;
        final NoteAddUndoItem noteAddUndoItem = undoItem;
        final int i = pageIndex;
        final Callback callback = result;
        this.mPdfViewCtrl.addTask(new EditAnnotTask(new NoteEvent(1, undoItem, (Note) annot, this.mPdfViewCtrl), new Callback() {
            public void result(Event event, boolean success) {
                if (success) {
                    try {
                        DocumentManager.getInstance(NoteToolHandler.this.mPdfViewCtrl).onAnnotAdded(annot2.getPage(), annot2);
                        if (z) {
                            DocumentManager.getInstance(NoteToolHandler.this.mPdfViewCtrl).addUndoItem(noteAddUndoItem);
                        }
                        if (NoteToolHandler.this.mPdfViewCtrl.isPageVisible(i)) {
                            RectF annotRect = annot2.getRect();
                            RectF pageViewRect = new RectF();
                            NoteToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRect, pageViewRect, i);
                            Rect rectResult = new Rect();
                            pageViewRect.roundOut(rectResult);
                            rectResult.inset(-10, -10);
                            NoteToolHandler.this.mPdfViewCtrl.refresh(i, rectResult);
                        }
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    callback.result(event, success);
                }
            }
        }));
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

    public void setIconType(String iconType) {
        this.mIconType = iconType;
    }

    public String getIconType() {
        return this.mIconType;
    }

    public String getType() {
        return ToolHandler.TH_TYPE_NOTE;
    }
}
