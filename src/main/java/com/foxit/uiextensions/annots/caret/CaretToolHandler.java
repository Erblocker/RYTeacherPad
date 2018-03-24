package com.foxit.uiextensions.annots.caret;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.PDFTextSelect;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Caret;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.sdk.pdf.annots.StrikeOut;
import com.foxit.sdk.pdf.objects.PDFObject;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.textmarkup.TextMarkupContentAbs;
import com.foxit.uiextensions.annots.textmarkup.TextSelector;
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutEvent;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.PropertyBar.PropertyChangeListener;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.PropertyCircleItem;
import com.foxit.uiextensions.controls.toolbar.ToolbarItemConfig;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.Iterator;

public class CaretToolHandler implements ToolHandler {
    private BaseItem mAnnotButton;
    private PropertyBarImpl mAnnotationProperty;
    private int mCaretRotate = 0;
    private final RectF mCharSelectedRectF;
    private int mColor;
    private int[] mColors;
    private final Context mContext;
    private Dialog mDialog;
    private EditText mDlgContent;
    private boolean mIsContinuousCreate = false;
    private boolean mIsInsertTextModule;
    private int mOpacity;
    private final Paint mPaint;
    private final PDFViewCtrl mPdfViewCtrl;
    private PropertyChangeListener mPropertyChangeListener;
    private boolean mRPLCreating = false;
    private int mSelectedPageIndex;
    private boolean mSelecting = false;
    private final TextSelector mTextSelector;
    private final RectF mTmpRect = new RectF();
    private PropertyCircleItem mToolCirclItem;

    public CaretToolHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
        this.mTextSelector = new TextSelector(this.mPdfViewCtrl);
        this.mCharSelectedRectF = new RectF();
        this.mAnnotationProperty = new PropertyBarImpl(context, pdfViewCtrl, parent);
        this.mRPLCreating = false;
    }

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }

    void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.mPropertyChangeListener = propertyChangeListener;
    }

    public void removePropertyBarListener() {
        this.mPropertyChangeListener = null;
    }

    public void setPropertyBar(PropertyBar propertyBar) {
        this.mAnnotationProperty = (PropertyBarImpl) propertyBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mAnnotationProperty;
    }

    public void init(boolean isInsertTextModule) {
        this.mIsInsertTextModule = isInsertTextModule;
        this.mAnnotButton = new CircleItemImpl(this.mContext);
        this.mColors = PropertyBar.PB_COLORS_CARET;
        if (this.mIsInsertTextModule) {
            this.mAnnotButton.setImageResource(R.drawable.annot_tool_prompt_insert);
            this.mAnnotButton.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_CARET);
        } else {
            this.mAnnotButton.setImageResource(R.drawable.annot_tool_prompt_replace);
            this.mAnnotButton.setTag(ToolbarItemConfig.ANNOTS_BAR_ITEM_REPLACE);
        }
        this.mAnnotButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                CaretToolHandler.this.mTextSelector.clear();
                ((UIExtensionsManager) CaretToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(CaretToolHandler.this);
            }
        });
        if (this.mColor == 0) {
            this.mColor = this.mColors[0];
        }
        if (this.mOpacity == 0) {
            this.mOpacity = 255;
        }
    }

    public void changeCurrentColor(int currentColor) {
        this.mColor = currentColor;
    }

    public void changeCurrentOpacity(int currentOpacity) {
        this.mOpacity = currentOpacity;
    }

    public String getType() {
        if (this.mIsInsertTextModule) {
            return ToolHandler.TH_TYPR_INSERTTEXT;
        }
        return ToolHandler.TH_TYPE_REPLACE;
    }

    public void onActivate() {
        this.mTextSelector.clear();
        this.mCharSelectedRectF.setEmpty();
        this.mAnnotButton.setSelected(true);
        resetPropertyBar();
    }

    public void onDeactivate() {
        this.mCharSelectedRectF.setEmpty();
        this.mAnnotButton.setSelected(false);
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        return false;
    }

    private int getCharIndexAtPoint(int pageIndex, PointF point) {
        int index = 0;
        try {
            index = getCharIndexAtPoint(this.mPdfViewCtrl.getDoc().getPage(pageIndex), point);
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return index;
    }

    public int getColor() {
        return this.mColor;
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    private void resetPropertyBar() {
        System.arraycopy(PropertyBar.PB_COLORS_CARET, 0, this.mColors, 0, this.mColors.length);
        this.mAnnotationProperty.setColors(this.mColors);
        this.mAnnotationProperty.setProperty(1, this.mColor);
        this.mAnnotationProperty.setProperty(2, AppDmUtil.opacity255To100(this.mOpacity));
        this.mAnnotationProperty.reset(3);
        this.mAnnotationProperty.setPropertyChangeListener(this.mPropertyChangeListener);
    }

    private boolean OnSelectDown(int pageIndex, PointF point, TextSelector selectInfo) {
        if (selectInfo == null) {
            return false;
        }
        int index = getCharIndexAtPoint(pageIndex, point);
        if (index < 0) {
            return false;
        }
        selectInfo.setStart(index);
        selectInfo.setEnd(index);
        return true;
    }

    private boolean OnSelectMove(int pageIndex, PointF point, TextSelector selectInfo) {
        if (selectInfo == null || selectInfo.getStart() < 0 || this.mSelectedPageIndex != pageIndex) {
            return false;
        }
        int index = getCharIndexAtPoint(pageIndex, point);
        if (index < 0) {
            return false;
        }
        selectInfo.setEnd(index);
        return true;
    }

    private boolean OnSelectRelease(final int pageIndex, final TextSelector selectorInfo) {
        try {
            if (!this.mIsInsertTextModule && this.mRPLCreating) {
                if (selectorInfo.getStart() >= 0 && selectorInfo.getEnd() >= 0) {
                    selectorInfo.computeSelected(this.mPdfViewCtrl.getDoc().getPage(pageIndex), selectorInfo.getStart(), selectorInfo.getEnd());
                    this.mCharSelectedRectF.set((RectF) selectorInfo.getRectFList().get(selectorInfo.getRectFList().size() - 1));
                }
                initDialog(new OnClickListener() {
                    public void onClick(View v) {
                        CaretToolHandler.this.mDialog.dismiss();
                        AppUtil.dismissInputSoft(CaretToolHandler.this.mDlgContent);
                        selectorInfo.clear();
                    }
                }, new OnClickListener() {
                    public void onClick(View v) {
                        CaretToolHandler.this.addCaretAnnot(pageIndex, CaretToolHandler.this.getCaretRectFromSelectRect(selectorInfo, pageIndex, null), CaretToolHandler.this.mCaretRotate, CaretToolHandler.this.mTextSelector, null);
                        CaretToolHandler.this.mDialog.dismiss();
                        AppUtil.dismissInputSoft(CaretToolHandler.this.mDlgContent);
                        if (!CaretToolHandler.this.mIsContinuousCreate) {
                            ((UIExtensionsManager) CaretToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                        }
                    }
                });
                this.mDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        CaretToolHandler.this.mSelecting = false;
                        CaretToolHandler.this.mRPLCreating = false;
                        RectF selectedRectF = new RectF(CaretToolHandler.this.mCharSelectedRectF);
                        CaretToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(selectedRectF, selectedRectF, pageIndex);
                        Rect selectedRect = new Rect();
                        selectedRectF.roundOut(selectedRect);
                        CaretToolHandler.this.getInvalidateRect(selectedRect);
                        CaretToolHandler.this.mPdfViewCtrl.invalidate(selectedRect);
                        CaretToolHandler.this.mCharSelectedRectF.setEmpty();
                    }
                });
                return true;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getCharIndexAtPoint(PDFPage page, PointF pdfPt) {
        int index = 0;
        try {
            index = PDFTextSelect.create(page).getIndexAtPos(pdfPt.x, pdfPt.y, 10.0f);
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return index;
    }

    private void setCaretRotate(Annot caret, int rotate) {
        if (caret != null && (caret instanceof Caret)) {
            if (rotate < 0 || rotate > 4) {
                rotate = 0;
            }
            try {
                caret.getDict().setAt("Rotate", PDFObject.createFromInteger(360 - (rotate * 90)));
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    private RectF getCaretRectFromSelectRect(TextSelector selector, int pageIndex, PointF point) {
        try {
            PDFTextSelect textSelect = PDFTextSelect.create(this.mPdfViewCtrl.getDoc().getPage(pageIndex));
            int start = Math.min(selector.getStart(), selector.getEnd());
            int nCount = textSelect.getTextRectCount(start, (Math.max(selector.getStart(), selector.getEnd()) - start) + 1);
            RectF docSelectedRectF = textSelect.getTextRect(nCount - 1);
            if (docSelectedRectF == null) {
                return null;
            }
            float w;
            float h;
            this.mCaretRotate = textSelect.getBaselineRotation(nCount - 1) % 4;
            RectF caretRect = new RectF();
            if (this.mCaretRotate % 2 != 0) {
                w = docSelectedRectF.right - docSelectedRectF.left;
                h = (2.0f * w) / 3.0f;
            } else {
                h = docSelectedRectF.top - docSelectedRectF.bottom;
                w = (2.0f * h) / 3.0f;
            }
            float offsetY = h * 0.9f;
            float offsetX = w * 0.9f;
            switch (this.mCaretRotate) {
                case 0:
                    if (point == null || point.x - docSelectedRectF.left > (docSelectedRectF.right - docSelectedRectF.left) / 2.0f) {
                        caretRect.set(docSelectedRectF.right - (w / 2.0f), docSelectedRectF.bottom + h, docSelectedRectF.right + (w / 2.0f), docSelectedRectF.bottom);
                    } else {
                        caretRect.set(docSelectedRectF.left - (w / 2.0f), docSelectedRectF.bottom + h, docSelectedRectF.left + (w / 2.0f), docSelectedRectF.bottom);
                    }
                    caretRect.offset(0.0f, 0.0f - offsetY);
                    return caretRect;
                case 1:
                    if (point == null || point.y - docSelectedRectF.bottom < (docSelectedRectF.top - docSelectedRectF.bottom) / 2.0f) {
                        caretRect.set(docSelectedRectF.left, docSelectedRectF.bottom + (h / 2.0f), docSelectedRectF.left + w, docSelectedRectF.bottom - (h / 2.0f));
                    } else {
                        caretRect.set(docSelectedRectF.left, docSelectedRectF.top + (h / 2.0f), docSelectedRectF.left + w, docSelectedRectF.top - (h / 2.0f));
                    }
                    caretRect.offset(0.0f - offsetX, 0.0f);
                    return caretRect;
                case 2:
                    if (point == null || point.x - docSelectedRectF.left < (docSelectedRectF.right - docSelectedRectF.left) / 2.0f) {
                        caretRect.set(docSelectedRectF.left - (w / 2.0f), docSelectedRectF.top, docSelectedRectF.left + (w / 2.0f), docSelectedRectF.top - h);
                    } else {
                        caretRect.set(docSelectedRectF.right - (w / 2.0f), docSelectedRectF.top, docSelectedRectF.right + (w / 2.0f), docSelectedRectF.top - h);
                    }
                    caretRect.offset(0.0f, offsetY);
                    return caretRect;
                case 3:
                    if (point == null || point.y - docSelectedRectF.bottom > (docSelectedRectF.top - docSelectedRectF.bottom) / 2.0f) {
                        caretRect.set(docSelectedRectF.right - w, docSelectedRectF.top + (h / 2.0f), docSelectedRectF.right, docSelectedRectF.top - (h / 2.0f));
                    } else {
                        caretRect.set(docSelectedRectF.right - w, docSelectedRectF.bottom + (h / 2.0f), docSelectedRectF.right, docSelectedRectF.bottom - (h / 2.0f));
                    }
                    caretRect.offset(offsetX, 0.0f);
                    return caretRect;
                default:
                    return caretRect;
            }
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean onTouchEvent(final int pageIndex, MotionEvent motionEvent) {
        PointF point = AppAnnotUtil.getPageViewPoint(this.mPdfViewCtrl, pageIndex, motionEvent);
        try {
            PointF docPoint;
            switch (motionEvent.getAction()) {
                case 0:
                    if (this.mIsInsertTextModule) {
                        this.mTextSelector.clear();
                        this.mSelectedPageIndex = pageIndex;
                        docPoint = new PointF(point.x, point.y);
                        this.mPdfViewCtrl.convertPageViewPtToPdfPt(docPoint, docPoint, pageIndex);
                        PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
                        int index = getCharIndexAtPoint(page, docPoint);
                        if (index == -1) {
                            return true;
                        }
                        if (index < 0) {
                            return false;
                        }
                        this.mSelecting = true;
                        this.mTextSelector.setStart(index);
                        this.mTextSelector.setEnd(index);
                        this.mTextSelector.computeSelected(page, this.mTextSelector.getStart(), this.mTextSelector.getEnd());
                        this.mCharSelectedRectF.set(this.mTextSelector.getBbox());
                        invalidateTouch(pageIndex, this.mTextSelector);
                        final PointF pointTemp = new PointF(point.x, point.y);
                        initDialog(new OnClickListener() {
                            public void onClick(View v) {
                                CaretToolHandler.this.mDialog.dismiss();
                                AppUtil.dismissInputSoft(CaretToolHandler.this.mDlgContent);
                            }
                        }, new OnClickListener() {
                            public void onClick(View v) {
                                PointF pdfPoint = new PointF(pointTemp.x, pointTemp.y);
                                CaretToolHandler.this.mPdfViewCtrl.convertPageViewPtToPdfPt(pdfPoint, pdfPoint, pageIndex);
                                CaretToolHandler.this.addCaretAnnot(pageIndex, CaretToolHandler.this.getCaretRectFromSelectRect(CaretToolHandler.this.mTextSelector, pageIndex, pdfPoint), CaretToolHandler.this.mCaretRotate, CaretToolHandler.this.mTextSelector, null);
                                CaretToolHandler.this.mDialog.dismiss();
                                AppUtil.dismissInputSoft(CaretToolHandler.this.mDlgContent);
                                if (!CaretToolHandler.this.mIsContinuousCreate) {
                                    ((UIExtensionsManager) CaretToolHandler.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                                }
                            }
                        });
                        this.mDialog.setOnDismissListener(new OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                CaretToolHandler.this.mSelecting = false;
                                RectF selectedRectF = new RectF(CaretToolHandler.this.mCharSelectedRectF);
                                CaretToolHandler.this.clearSelectedRectF();
                                CaretToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(selectedRectF, selectedRectF, pageIndex);
                                CaretToolHandler.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(selectedRectF, selectedRectF, pageIndex);
                                Rect selectedRect = new Rect();
                                selectedRectF.roundOut(selectedRect);
                                CaretToolHandler.this.getInvalidateRect(selectedRect);
                                CaretToolHandler.this.mPdfViewCtrl.invalidate(selectedRect);
                            }
                        });
                        return true;
                    }
                    this.mTextSelector.clear();
                    this.mSelectedPageIndex = pageIndex;
                    docPoint = new PointF(point.x, point.y);
                    this.mPdfViewCtrl.convertPageViewPtToPdfPt(docPoint, docPoint, pageIndex);
                    this.mRPLCreating = OnSelectDown(pageIndex, docPoint, this.mTextSelector);
                    this.mSelecting = this.mRPLCreating;
                    return this.mRPLCreating;
                case 1:
                    return OnSelectRelease(pageIndex, this.mTextSelector);
                case 2:
                    if (!this.mIsInsertTextModule && this.mRPLCreating) {
                        docPoint = new PointF(point.x, point.y);
                        this.mPdfViewCtrl.convertPageViewPtToPdfPt(docPoint, docPoint, pageIndex);
                        if (OnSelectMove(pageIndex, docPoint, this.mTextSelector)) {
                            this.mTextSelector.computeSelected(this.mPdfViewCtrl.getDoc().getPage(pageIndex), this.mTextSelector.getStart(), this.mTextSelector.getEnd());
                            invalidateTouch(pageIndex, this.mTextSelector);
                            return true;
                        }
                    }
                    break;
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initDialog(OnClickListener cancelClickListener, OnClickListener okClickListener) {
        Context context = this.mContext;
        View mView = View.inflate(context, R.layout.rd_note_dialog_edit, null);
        TextView dialogTitle = (TextView) mView.findViewById(R.id.rd_note_dialog_edit_title);
        this.mDlgContent = (EditText) mView.findViewById(R.id.rd_note_dialog_edit);
        Button cancelButton = (Button) mView.findViewById(R.id.rd_note_dialog_edit_cancel);
        final Button applayButton = (Button) mView.findViewById(R.id.rd_note_dialog_edit_ok);
        mView.setLayoutParams(new LayoutParams(-1, -2));
        this.mDialog = new Dialog(context, R.style.rv_dialog_style);
        this.mDialog.setContentView(mView, new LayoutParams(AppDisplay.getInstance(this.mContext).getUITextEditDialogWidth(), -2));
        this.mDlgContent.setMaxLines(10);
        this.mDialog.getWindow().setFlags(1024, 1024);
        this.mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
        if (this.mIsInsertTextModule) {
            dialogTitle.setText(this.mContext.getResources().getString(R.string.fx_string_inserttext));
        } else {
            dialogTitle.setText(this.mContext.getResources().getString(R.string.fx_string_replacetext));
        }
        applayButton.setEnabled(false);
        applayButton.setTextColor(this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
        this.mDlgContent.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (CaretToolHandler.this.mDlgContent.getText().length() == 0) {
                    applayButton.setEnabled(false);
                    applayButton.setTextColor(CaretToolHandler.this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
                    return;
                }
                applayButton.setEnabled(true);
                applayButton.setTextColor(CaretToolHandler.this.mContext.getResources().getColor(R.color.dlg_bt_text_selector));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        cancelButton.setOnClickListener(cancelClickListener);
        applayButton.setOnClickListener(okClickListener);
        this.mDialog.show();
        AppUtil.showSoftInput(this.mDlgContent);
    }

    public void addAnnot(int pageIndex, CaretAnnotContent content, boolean addUndo, Callback result) {
        CaretAddUndoItem undoItem = new CaretAddUndoItem(this.mPdfViewCtrl);
        undoItem.setCurrentValue((AnnotContent) content);
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            Annot caret = (Caret) page.addAnnot(14, content.getBBox());
            if (caret != null) {
                undoItem.mAuthor = content.getAuthor();
                undoItem.mContents = content.getContents();
                undoItem.mCreationDate = content.getCreatedDate();
                undoItem.mModifiedDate = content.getModifiedDate();
                undoItem.mRotate = content.getRotate();
                TextSelector selector = new TextSelector(this.mPdfViewCtrl);
                selector.setStart(this.mTextSelector.getStart());
                selector.setEnd(this.mTextSelector.getEnd());
                selector.computeSelected(page, selector.getStart(), selector.getEnd());
                selector.setContents(this.mTextSelector.getContents());
                undoItem.mTextSelector = selector;
                addAnnot(caret, undoItem, addUndo, result);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected void addAnnot(Annot annot, CaretAddUndoItem undoItem, boolean addUndo, Callback result) {
        final Annot annot2 = annot;
        final boolean z = addUndo;
        final CaretAddUndoItem caretAddUndoItem = undoItem;
        final Callback callback = result;
        this.mPdfViewCtrl.addTask(new EditAnnotTask(new CaretEvent(1, undoItem, (Caret) annot, this.mPdfViewCtrl), new Callback() {
            public void result(Event event, boolean success) {
                if (success) {
                    try {
                        final PDFPage page = annot2.getPage();
                        final int pageIndex = page.getIndex();
                        if (CaretToolHandler.this.mIsInsertTextModule) {
                            DocumentManager.getInstance(CaretToolHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot2);
                        }
                        if (z) {
                            DocumentManager.getInstance(CaretToolHandler.this.mPdfViewCtrl).addUndoItem(caretAddUndoItem);
                        }
                        if (CaretToolHandler.this.mPdfViewCtrl.isPageVisible(pageIndex)) {
                            RectF viewRect = annot2.getRect();
                            CaretToolHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(viewRect, viewRect, pageIndex);
                            Rect rect = new Rect();
                            viewRect.roundOut(rect);
                            rect.inset(-10, -10);
                            CaretToolHandler.this.mPdfViewCtrl.refresh(pageIndex, rect);
                        }
                        if (!CaretToolHandler.this.mIsInsertTextModule) {
                            final CaretAddUndoItem caretAddUndoItem = caretAddUndoItem;
                            TextMarkupContentAbs strikeoutAbs = new TextMarkupContentAbs() {
                                public TextSelector getTextSelector() {
                                    return caretAddUndoItem.mTextSelector;
                                }

                                public int getPageIndex() {
                                    return pageIndex;
                                }

                                public int getType() {
                                    return 12;
                                }

                                public String getIntent() {
                                    return "StrikeOutTextEdit";
                                }

                                public int getColor() {
                                    return (int) caretAddUndoItem.mColor;
                                }

                                public int getOpacity() {
                                    return (int) ((caretAddUndoItem.mOpacity * 255.0f) + 0.5f);
                                }

                                public String getSubject() {
                                    return "Replace";
                                }
                            };
                            AnnotHandler annotHandler = ToolUtil.getAnnotHandlerByType((UIExtensionsManager) CaretToolHandler.this.mPdfViewCtrl.getUIExtensionsManager(), 12);
                            final Annot annot = annot2;
                            final CaretAddUndoItem caretAddUndoItem2 = caretAddUndoItem;
                            annotHandler.addAnnot(pageIndex, strikeoutAbs, false, new Callback() {
                                public void result(Event event, boolean success) {
                                    if (success) {
                                        StrikeoutEvent strikeoutEvent = (StrikeoutEvent) event;
                                        if (strikeoutEvent.mAnnot != null && (strikeoutEvent.mAnnot instanceof StrikeOut)) {
                                            StrikeOut strikeOut = strikeoutEvent.mAnnot;
                                            try {
                                                strikeOut.setIntent("StrikeOutTextEdit");
                                                page.setAnnotGroup(new Markup[]{(Caret) annot, strikeOut}, 0);
                                                DocumentManager.getInstance(CaretToolHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                                                strikeOut.setBorderColor(caretAddUndoItem2.mColor);
                                                strikeOut.setOpacity(caretAddUndoItem2.mOpacity);
                                                strikeOut.resetAppearanceStream();
                                            } catch (PDFException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                        if (callback != null) {
                            callback.result(null, success);
                        }
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    private void addCaretAnnot(int pageIndex, RectF annotRect, int rotate, TextSelector textSelector, Callback result) {
        final DateTime dateTime = AppDmUtil.currentDateToDocumentDate();
        final int i = pageIndex;
        final int i2 = rotate;
        final TextSelector textSelector2 = textSelector;
        final RectF rectF = annotRect;
        addAnnot(pageIndex, new CaretAnnotContent(this.mIsInsertTextModule) {
            public int getPageIndex() {
                return i;
            }

            public int getType() {
                return 14;
            }

            public float getLineWidth() {
                return 0.0f;
            }

            public int getRotate() {
                return i2;
            }

            public TextSelector getTextSelector() {
                return textSelector2;
            }

            public DateTime getCreatedDate() {
                return dateTime;
            }

            public RectF getBBox() {
                return rectF;
            }

            public int getColor() {
                return CaretToolHandler.this.mColor;
            }

            public int getOpacity() {
                return AppDmUtil.opacity100To255(CaretToolHandler.this.mOpacity);
            }

            public DateTime getModifiedDate() {
                return dateTime;
            }

            public String getContents() {
                return CaretToolHandler.this.mDlgContent.getText().toString();
            }
        }, true, result);
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        if (this.mSelectedPageIndex == pageIndex) {
            Rect clipRect;
            RectF tmp;
            Rect r;
            RectF start;
            RectF end;
            if (this.mIsInsertTextModule) {
                if (this.mSelecting && this.mTextSelector != null && this.mCharSelectedRectF.left < this.mCharSelectedRectF.right && this.mCharSelectedRectF.top > this.mCharSelectedRectF.bottom) {
                    this.mPaint.setColor(calColorByMultiply(7586273, 150));
                    clipRect = canvas.getClipBounds();
                    tmp = new RectF(this.mTextSelector.getBbox());
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(tmp, tmp, pageIndex);
                    r = new Rect();
                    tmp.round(r);
                    if (r.intersect(clipRect)) {
                        canvas.save();
                        canvas.drawRect(r, this.mPaint);
                        if (this.mTextSelector.getRectFList().size() > 0) {
                            start = new RectF((RectF) this.mTextSelector.getRectFList().get(0));
                            end = new RectF((RectF) this.mTextSelector.getRectFList().get(this.mTextSelector.getRectFList().size() - 1));
                            this.mPdfViewCtrl.convertPdfRectToPageViewRect(start, start, pageIndex);
                            this.mPdfViewCtrl.convertPdfRectToPageViewRect(end, end, pageIndex);
                            this.mPaint.setARGB(255, 76, 121, 164);
                            canvas.drawLine(start.left, start.top, start.left, start.bottom, this.mPaint);
                            canvas.drawLine(end.right, end.top, end.right, end.bottom, this.mPaint);
                        }
                        canvas.restore();
                    }
                }
            } else if (this.mSelecting && this.mTextSelector != null && this.mTextSelector.getStart() >= 0) {
                this.mPaint.setColor(calColorByMultiply(7586273, 150));
                clipRect = canvas.getClipBounds();
                Iterator it = this.mTextSelector.getRectFList().iterator();
                while (it.hasNext()) {
                    tmp = new RectF((RectF) it.next());
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(tmp, tmp, pageIndex);
                    r = new Rect();
                    tmp.round(r);
                    if (r.intersect(clipRect)) {
                        canvas.save();
                        canvas.drawRect(r, this.mPaint);
                        canvas.restore();
                    }
                }
                if (this.mTextSelector.getRectFList().size() > 0) {
                    start = new RectF((RectF) this.mTextSelector.getRectFList().get(0));
                    end = new RectF((RectF) this.mTextSelector.getRectFList().get(this.mTextSelector.getRectFList().size() - 1));
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(start, start, pageIndex);
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(end, end, pageIndex);
                    this.mPaint.setARGB(255, 76, 121, 164);
                    canvas.drawLine(start.left, start.top, start.left, start.bottom, this.mPaint);
                    canvas.drawLine(end.right, end.top, end.right, end.bottom, this.mPaint);
                }
            }
        }
    }

    private void invalidateTouch(int pageIndex, TextSelector textSelector) {
        if (textSelector != null) {
            RectF rectF = new RectF();
            rectF.set(textSelector.getBbox());
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, pageIndex);
            this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, rectF, pageIndex);
            RectF rF = calculate(rectF, this.mTmpRect);
            Rect rect = new Rect();
            rF.roundOut(rect);
            getInvalidateRect(rect);
            this.mPdfViewCtrl.invalidate(rect);
            this.mTmpRect.set(rectF);
        }
    }

    private RectF calculate(RectF desRectF, RectF srcRectF) {
        RectF mTmpDesRect = new RectF();
        if (srcRectF.isEmpty()) {
            return desRectF;
        }
        int count = 0;
        if (desRectF.left == srcRectF.left && desRectF.top == srcRectF.top) {
            count = 0 + 1;
        }
        if (desRectF.right == srcRectF.right && desRectF.top == srcRectF.top) {
            count++;
        }
        if (desRectF.left == srcRectF.left && desRectF.bottom == srcRectF.bottom) {
            count++;
        }
        if (desRectF.right == srcRectF.right && desRectF.bottom == srcRectF.bottom) {
            count++;
        }
        mTmpDesRect.set(desRectF);
        if (count == 2) {
            mTmpDesRect.union(srcRectF);
            RectF rectF = new RectF();
            rectF.set(mTmpDesRect);
            mTmpDesRect.intersect(srcRectF);
            rectF.intersect(mTmpDesRect);
            return rectF;
        } else if (count == 3 || count == 4) {
            return mTmpDesRect;
        } else {
            mTmpDesRect.union(srcRectF);
            return mTmpDesRect;
        }
    }

    private void getInvalidateRect(Rect rect) {
        rect.top -= 20;
        rect.bottom += 20;
        rect.left -= 10;
        rect.right += 10;
        rect.inset(-20, -20);
    }

    private void clearSelectedRectF() {
        this.mTextSelector.clear();
        this.mCharSelectedRectF.setEmpty();
    }

    private int calColorByMultiply(int color, int opacity) {
        int rColor = color | -16777216;
        float rOpacity = ((float) opacity) / 255.0f;
        return (((rColor & -16777216) | (((int) ((((float) ((16711680 & rColor) >> 16)) * rOpacity) + ((1.0f - rOpacity) * 255.0f))) << 16)) | (((int) ((((float) ((MotionEventCompat.ACTION_POINTER_INDEX_MASK & rColor) >> 8)) * rOpacity) + ((1.0f - rOpacity) * 255.0f))) << 8)) | ((int) ((((float) (rColor & 255)) * rOpacity) + ((1.0f - rOpacity) * 255.0f)));
    }

    public void onToolHandlerChanged(ToolHandler lastTool, ToolHandler currentTool) {
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
            this.mAnnotButton.setEnable(true);
        } else {
            this.mAnnotButton.setEnable(false);
        }
    }

    public void onColorValueChanged(int color) {
        this.mColor = color;
        this.mToolCirclItem.setCentreCircleColor(this.mColor);
    }

    public void onOpacityValueChanged(int opacity) {
        this.mOpacity = opacity;
    }
}
