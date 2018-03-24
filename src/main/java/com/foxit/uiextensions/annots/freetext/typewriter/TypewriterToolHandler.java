package com.foxit.uiextensions.annots.freetext.typewriter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.FreeText;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.common.EditAnnotEvent;
import com.foxit.uiextensions.annots.common.EditAnnotTask;
import com.foxit.uiextensions.annots.freetext.FtTextUtil;
import com.foxit.uiextensions.annots.freetext.FtTextUtil.OnTextValuesChangedListener;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppKeyboardUtil;
import com.foxit.uiextensions.utils.AppKeyboardUtil.IKeyboardListener;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.http.protocol.HTTP;

public class TypewriterToolHandler implements ToolHandler {
    private String mAnnotText;
    private float mBBoxHeight;
    private float mBBoxWidth;
    private int mColor;
    private Context mContext;
    private boolean mCreateAlive = true;
    private int mCreateIndex;
    private boolean mCreating;
    private PointF mEditPoint = new PointF(0.0f, 0.0f);
    private EditText mEditView;
    private String mFont;
    private float mFontSize;
    private boolean mIsContinue;
    private boolean mIsContinuousCreate = false;
    private boolean mIsCreated;
    private boolean mIsSelcetEndText = false;
    public int mLastPageIndex = -1;
    private CreateAnnotResult mListener;
    private int mOpacity;
    private float mPageViewHeigh;
    private float mPageViewWidth;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PointF mStartPdfPoint = new PointF(0.0f, 0.0f);
    private PointF mStartPoint = new PointF(0.0f, 0.0f);
    private FtTextUtil mTextUtil;

    public interface CreateAnnotResult {
        void callBack();
    }

    public TypewriterToolHandler(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        pdfViewCtrl.registerDocEventListener(new IDocEventListener() {
            public void onDocWillOpen() {
            }

            public void onDocOpened(PDFDoc document, int errCode) {
            }

            public void onDocWillClose(PDFDoc document) {
            }

            public void onDocClosed(PDFDoc document, int errCode) {
                TypewriterToolHandler.this.mAnnotText = "";
                TypewriterToolHandler.this.mStartPoint.set(0.0f, 0.0f);
                TypewriterToolHandler.this.mEditPoint.set(0.0f, 0.0f);
                TypewriterToolHandler.this.mLastPageIndex = -1;
                AppUtil.dismissInputSoft(TypewriterToolHandler.this.mEditView);
                TypewriterToolHandler.this.mParent.removeView(TypewriterToolHandler.this.mEditView);
                TypewriterToolHandler.this.mPdfViewCtrl.layout(0, 0, TypewriterToolHandler.this.mPdfViewCtrl.getWidth(), TypewriterToolHandler.this.mPdfViewCtrl.getHeight());
                if (TypewriterToolHandler.this.mTextUtil != null) {
                    TypewriterToolHandler.this.mTextUtil.setKeyboardOffset(0);
                }
                TypewriterToolHandler.this.mEditView = null;
                TypewriterToolHandler.this.mBBoxHeight = 0.0f;
                TypewriterToolHandler.this.mBBoxWidth = 0.0f;
                TypewriterToolHandler.this.mCreating = false;
                if (TypewriterToolHandler.this.mTextUtil != null) {
                    TypewriterToolHandler.this.mTextUtil.getBlink().removeCallbacks((Runnable) TypewriterToolHandler.this.mTextUtil.getBlink());
                }
                TypewriterToolHandler.this.mIsContinue = false;
            }

            public void onDocWillSave(PDFDoc document) {
            }

            public void onDocSaved(PDFDoc document, int errCode) {
            }
        });
    }

    public int getColor() {
        return this.mColor;
    }

    public int getOpacity() {
        return this.mOpacity;
    }

    public String getFontName() {
        return this.mFont;
    }

    public float getFontSize() {
        return this.mFontSize;
    }

    public String getType() {
        return ToolHandler.TH_TYPE_TYPEWRITER;
    }

    public void onActivate() {
        this.mLastPageIndex = -1;
        this.mCreateAlive = true;
        this.mIsCreated = false;
        AppKeyboardUtil.setKeyboardListener(this.mParent, this.mParent, new IKeyboardListener() {
            public void onKeyboardOpened(int keyboardHeight) {
            }

            public void onKeyboardClosed() {
                TypewriterToolHandler.this.mCreateAlive = false;
            }
        });
    }

    public void onDeactivate() {
        if (this.mEditView != null) {
            this.mIsContinue = false;
            if (!this.mIsCreated) {
                createFTAnnot();
            }
        }
        AppKeyboardUtil.removeKeyboardListener(this.mParent);
        this.mCreateAlive = true;
    }

    public boolean onTouchEvent(final int pageIndex, MotionEvent e) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        PointF point = new PointF(e.getX(), e.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        PointF pdfPoint = new PointF(point.x, point.y);
        this.mPdfViewCtrl.convertPageViewPtToPdfPt(pdfPoint, pdfPoint, pageIndex);
        float x = point.x;
        float y = point.y;
        switch (e.getAction()) {
            case 0:
                if (uiExtensionsManager.getCurrentToolHandler() != this || this.mCreating) {
                    return false;
                }
                this.mTextUtil = new FtTextUtil(this.mContext, this.mPdfViewCtrl);
                this.mPageViewWidth = (float) this.mPdfViewCtrl.getPageViewWidth(pageIndex);
                this.mPageViewHeigh = (float) this.mPdfViewCtrl.getPageViewHeight(pageIndex);
                this.mStartPoint.set(x, y);
                adjustStartPt(this.mPdfViewCtrl, pageIndex, this.mStartPoint);
                this.mStartPdfPoint.set(this.mStartPoint.x, this.mStartPoint.y);
                this.mPdfViewCtrl.convertPageViewPtToPdfPt(this.mStartPdfPoint, this.mStartPdfPoint, pageIndex);
                if (this.mLastPageIndex == -1) {
                    this.mLastPageIndex = pageIndex;
                }
                this.mCreateIndex = pageIndex;
                return true;
            case 1:
                if (uiExtensionsManager.getCurrentToolHandler() == this && this.mEditView == null) {
                    this.mEditView = new EditText(this.mContext);
                    this.mEditView.setLayoutParams(new LayoutParams(1, 1));
                    this.mEditView.addTextChangedListener(new TextWatcher() {
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            TypewriterToolHandler.this.mAnnotText = FtTextUtil.filterEmoji(String.valueOf(s));
                            TypewriterToolHandler.this.mPdfViewCtrl.invalidate();
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        public void afterTextChanged(Editable s) {
                        }
                    });
                    this.mTextUtil.setOnWidthChanged(new OnTextValuesChangedListener() {
                        public void onMaxWidthChanged(float maxWidth) {
                            TypewriterToolHandler.this.mBBoxWidth = maxWidth;
                        }

                        public void onMaxHeightChanged(float maxHeight) {
                            TypewriterToolHandler.this.mBBoxHeight = maxHeight;
                        }

                        public void onCurrentSelectIndex(int selectIndex) {
                            if (selectIndex >= TypewriterToolHandler.this.mEditView.getText().length()) {
                                selectIndex = TypewriterToolHandler.this.mEditView.getText().length();
                                TypewriterToolHandler.this.mIsSelcetEndText = true;
                            } else {
                                TypewriterToolHandler.this.mIsSelcetEndText = false;
                            }
                            TypewriterToolHandler.this.mEditView.setSelection(selectIndex);
                        }

                        public void onEditPointChanged(float editPointX, float editPointY) {
                            PointF point = new PointF(editPointX, editPointY);
                            TypewriterToolHandler.this.mPdfViewCtrl.convertPageViewPtToPdfPt(point, point, pageIndex);
                            TypewriterToolHandler.this.mEditPoint.set(point.x, point.y);
                        }
                    });
                    this.mParent.addView(this.mEditView);
                    this.mPdfViewCtrl.invalidate();
                    AppUtil.showSoftInput(this.mEditView);
                    this.mTextUtil.getBlink().postDelayed((Runnable) this.mTextUtil.getBlink(), 500);
                    this.mCreating = true;
                }
                this.mCreateAlive = true;
                return false;
            case 2:
                return true;
            case 3:
                this.mStartPoint.set(0.0f, 0.0f);
                this.mEditPoint.set(0.0f, 0.0f);
                this.mCreateAlive = true;
                return true;
            default:
                return false;
        }
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent) {
        PointF point = new PointF(motionEvent.getX(), motionEvent.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        return onSingleTapOrLongPress(pageIndex, point);
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent) {
        PointF point = new PointF(motionEvent.getX(), motionEvent.getY());
        this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(point, point, pageIndex);
        return onSingleTapOrLongPress(pageIndex, point);
    }

    public boolean onSingleTapOrLongPress(final int pageIndex, final PointF point) {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        PointF pdfPoint = new PointF(point.x, point.y);
        this.mPdfViewCtrl.convertPageViewPtToPdfPt(pdfPoint, pdfPoint, pageIndex);
        float x = point.x;
        float y = point.y;
        if (uiExtensionsManager.getCurrentToolHandler() != this || this.mEditView == null) {
            return false;
        }
        RectF rectF = new RectF(this.mStartPoint.x, this.mStartPoint.y, this.mStartPoint.x + this.mBBoxWidth, this.mStartPoint.y + this.mBBoxHeight);
        if (rectF.contains(x, y)) {
            PointF pointF = new PointF(x, y);
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pointF, pointF, pageIndex);
            this.mEditPoint.set(pointF.x, pointF.y);
            this.mTextUtil.resetEditState();
            this.mPdfViewCtrl.invalidate(AppDmUtil.rectFToRect(rectF));
            AppUtil.showSoftInput(this.mEditView);
            return true;
        }
        if (!this.mIsContinuousCreate) {
            uiExtensionsManager.setCurrentToolHandler(null);
        }
        if (this.mCreateAlive) {
            this.mCreateAlive = false;
            if (uiExtensionsManager.getCurrentToolHandler() != this) {
                return true;
            }
            createFTAnnot();
            return true;
        }
        this.mCreateAlive = true;
        this.mIsContinue = true;
        setCreateAnnotListener(new CreateAnnotResult() {
            public void callBack() {
                TypewriterToolHandler.this.mStartPoint.set(point.x, point.y);
                TypewriterToolHandler.this.adjustStartPt(TypewriterToolHandler.this.mPdfViewCtrl, pageIndex, TypewriterToolHandler.this.mStartPoint);
                TypewriterToolHandler.this.mStartPdfPoint.set(TypewriterToolHandler.this.mStartPoint.x, TypewriterToolHandler.this.mStartPoint.y);
                TypewriterToolHandler.this.mPdfViewCtrl.convertPageViewPtToPdfPt(TypewriterToolHandler.this.mStartPdfPoint, TypewriterToolHandler.this.mStartPdfPoint, pageIndex);
                if (TypewriterToolHandler.this.mLastPageIndex == -1) {
                    TypewriterToolHandler.this.mLastPageIndex = pageIndex;
                }
                TypewriterToolHandler.this.mCreateIndex = pageIndex;
                if (TypewriterToolHandler.this.mEditView != null) {
                    AppUtil.showSoftInput(TypewriterToolHandler.this.mEditView);
                }
            }
        });
        createFTAnnot();
        return true;
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        canvas.save();
        if (this.mLastPageIndex == pageIndex && this.mEditView != null) {
            PointF startPoint = new PointF(this.mStartPdfPoint.x, this.mStartPdfPoint.y);
            this.mPdfViewCtrl.convertPdfPtToPageViewPt(startPoint, startPoint, pageIndex);
            PointF editPoint = new PointF(this.mEditPoint.x, this.mEditPoint.y);
            if (!(editPoint.x == 0.0f && editPoint.y == 0.0f)) {
                this.mPdfViewCtrl.convertPdfPtToPageViewPt(editPoint, editPoint, pageIndex);
            }
            this.mTextUtil.setTextString(pageIndex, this.mAnnotText, true);
            this.mTextUtil.setStartPoint(startPoint);
            this.mTextUtil.setEditPoint(editPoint);
            this.mTextUtil.setMaxRect(((float) this.mPdfViewCtrl.getPageViewWidth(pageIndex)) - startPoint.x, ((float) this.mPdfViewCtrl.getPageViewHeight(pageIndex)) - startPoint.y);
            this.mTextUtil.setTextColor(this.mColor, AppDmUtil.opacity100To255(this.mOpacity));
            this.mTextUtil.setFont(this.mFont, this.mFontSize);
            if (this.mIsSelcetEndText) {
                this.mTextUtil.setEndSelection(this.mEditView.getSelectionEnd() + 1);
            } else {
                this.mTextUtil.setEndSelection(this.mEditView.getSelectionEnd());
            }
            this.mTextUtil.loadText();
            this.mTextUtil.DrawText(canvas);
        }
        canvas.restore();
    }

    private void createFTAnnot() {
        PointF pointF;
        if (this.mAnnotText == null || this.mAnnotText.length() <= 0) {
            if (this.mIsContinue && this.mCreateAlive && this.mListener != null) {
                this.mLastPageIndex = -1;
                this.mListener.callBack();
            } else {
                AppUtil.dismissInputSoft(this.mEditView);
                this.mParent.removeView(this.mEditView);
                this.mEditView = null;
                this.mCreating = false;
                this.mTextUtil.getBlink().removeCallbacks((Runnable) this.mTextUtil.getBlink());
            }
            this.mPdfViewCtrl.layout(0, 0, this.mPdfViewCtrl.getWidth(), this.mPdfViewCtrl.getHeight());
            if (!this.mPdfViewCtrl.isPageVisible(this.mCreateIndex)) {
                return;
            }
            if (this.mCreateIndex == this.mPdfViewCtrl.getPageCount() - 1 || this.mPdfViewCtrl.getPageLayoutMode() == 1) {
                pointF = new PointF((float) this.mPdfViewCtrl.getPageViewWidth(this.mCreateIndex), (float) this.mPdfViewCtrl.getPageViewHeight(this.mCreateIndex));
                this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(pointF, pointF, this.mCreateIndex);
                if (((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - (pointF.y - this.mTextUtil.getKeyboardOffset()) > 0.0f) {
                    this.mPdfViewCtrl.layout(0, 0, this.mPdfViewCtrl.getWidth(), this.mPdfViewCtrl.getHeight());
                    this.mTextUtil.setKeyboardOffset(0);
                    pointF = new PointF(this.mStartPdfPoint.x, this.mStartPdfPoint.y);
                    this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, this.mCreateIndex);
                    this.mPdfViewCtrl.gotoPage(this.mCreateIndex, this.mTextUtil.getPageViewOrigin(this.mPdfViewCtrl, this.mCreateIndex, pointF.x, pointF.y).x, this.mTextUtil.getPageViewOrigin(this.mPdfViewCtrl, this.mCreateIndex, pointF.x, pointF.y).y);
                    return;
                }
                return;
            }
            return;
        }
        pointF = new PointF(this.mStartPdfPoint.x, this.mStartPdfPoint.y);
        this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, this.mCreateIndex);
        final RectF rect = new RectF(pointF.x, pointF.y, pointF.x + this.mBBoxWidth, pointF.y + this.mBBoxHeight);
        RectF rectF = new RectF(pointF.x, pointF.y, pointF.x + this.mBBoxWidth, pointF.y + this.mBBoxHeight);
        this.mPdfViewCtrl.convertPageViewRectToPdfRect(rectF, rectF, this.mCreateIndex);
        RectF _rect = new RectF(rectF);
        this.mPdfViewCtrl.convertPdfRectToPageViewRect(_rect, _rect, this.mCreateIndex);
        String content = "";
        try {
            content = new String(this.mAnnotText.getBytes(), HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ArrayList<String> composeText = this.mTextUtil.getComposedText(this.mPdfViewCtrl, this.mCreateIndex, _rect, content, this.mFont, this.mFontSize);
        String annotContent = "";
        for (int i = 0; i < composeText.size(); i++) {
            annotContent = new StringBuilder(String.valueOf(annotContent)).append((String) composeText.get(i)).toString();
            if (!(i == composeText.size() - 1 || annotContent.charAt(annotContent.length() - 1) == '\n')) {
                annotContent = new StringBuilder(String.valueOf(annotContent)).append("\r").toString();
            }
        }
        try {
            final PDFPage page = this.mPdfViewCtrl.getDoc().getPage(this.mCreateIndex);
            final Annot annot = page.addAnnot(3, new RectF(rectF.left, rectF.top, rectF.right, rectF.bottom));
            final TypewriterAddUndoItem undoItem = new TypewriterAddUndoItem(this.mPdfViewCtrl);
            undoItem.mNM = AppDmUtil.randomUUID(null);
            undoItem.mPageIndex = this.mCreateIndex;
            undoItem.mColor = (long) this.mColor;
            undoItem.mOpacity = ((float) AppDmUtil.opacity100To255(this.mOpacity)) / 255.0f;
            undoItem.mContents = annotContent;
            undoItem.mFont = this.mTextUtil.getSupportFont(this.mFont);
            undoItem.mFontSize = this.mFontSize;
            undoItem.mTextColor = (long) this.mColor;
            undoItem.mDaFlags = 7;
            undoItem.mAuthor = AppDmUtil.getAnnotAuthor();
            undoItem.mBBox = new RectF(rectF.left, rectF.top, rectF.right, rectF.bottom);
            undoItem.mModifiedDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mCreationDate = AppDmUtil.currentDateToDocumentDate();
            undoItem.mFlags = 4;
            undoItem.mIntent = "FreeTextTypewriter";
            EditAnnotEvent typewriterEvent = new TypewriterEvent(1, undoItem, (FreeText) annot, this.mPdfViewCtrl);
            DocumentManager.getInstance(this.mPdfViewCtrl).setHasModifyTask(true);
            this.mPdfViewCtrl.addTask(new EditAnnotTask(typewriterEvent, new Callback() {
                public void result(Event event, boolean success) {
                    if (success) {
                        DocumentManager.getInstance(TypewriterToolHandler.this.mPdfViewCtrl).onAnnotAdded(page, annot);
                        DocumentManager.getInstance(TypewriterToolHandler.this.mPdfViewCtrl).addUndoItem(undoItem);
                        DocumentManager.getInstance(TypewriterToolHandler.this.mPdfViewCtrl).setHasModifyTask(false);
                        if (TypewriterToolHandler.this.mPdfViewCtrl.isPageVisible(TypewriterToolHandler.this.mCreateIndex)) {
                            TypewriterToolHandler.this.mPdfViewCtrl.refresh(TypewriterToolHandler.this.mCreateIndex, new Rect((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom));
                            if (TypewriterToolHandler.this.mIsContinue && TypewriterToolHandler.this.mCreateAlive) {
                                TypewriterToolHandler.this.mEditView.setText("");
                            } else {
                                AppUtil.dismissInputSoft(TypewriterToolHandler.this.mEditView);
                                TypewriterToolHandler.this.mParent.removeView(TypewriterToolHandler.this.mEditView);
                                TypewriterToolHandler.this.mEditView = null;
                                TypewriterToolHandler.this.mCreating = false;
                                TypewriterToolHandler.this.mTextUtil.getBlink().removeCallbacks((Runnable) TypewriterToolHandler.this.mTextUtil.getBlink());
                                TypewriterToolHandler.this.mPdfViewCtrl.layout(0, 0, TypewriterToolHandler.this.mPdfViewCtrl.getWidth(), TypewriterToolHandler.this.mPdfViewCtrl.getHeight());
                                if (TypewriterToolHandler.this.mPdfViewCtrl.isPageVisible(TypewriterToolHandler.this.mCreateIndex) && ((TypewriterToolHandler.this.mCreateIndex == TypewriterToolHandler.this.mPdfViewCtrl.getPageCount() - 1 || TypewriterToolHandler.this.mPdfViewCtrl.getPageLayoutMode() == 1) && TypewriterToolHandler.this.mCreateIndex == TypewriterToolHandler.this.mPdfViewCtrl.getCurrentPage())) {
                                    PointF endPoint = new PointF((float) TypewriterToolHandler.this.mPdfViewCtrl.getPageViewWidth(TypewriterToolHandler.this.mCreateIndex), (float) TypewriterToolHandler.this.mPdfViewCtrl.getPageViewHeight(TypewriterToolHandler.this.mCreateIndex));
                                    TypewriterToolHandler.this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(endPoint, endPoint, TypewriterToolHandler.this.mCreateIndex);
                                    if (((float) AppDisplay.getInstance(TypewriterToolHandler.this.mContext).getRawScreenHeight()) - (endPoint.y - TypewriterToolHandler.this.mTextUtil.getKeyboardOffset()) > 0.0f) {
                                        TypewriterToolHandler.this.mPdfViewCtrl.layout(0, 0, TypewriterToolHandler.this.mPdfViewCtrl.getWidth(), TypewriterToolHandler.this.mPdfViewCtrl.getHeight());
                                        TypewriterToolHandler.this.mTextUtil.setKeyboardOffset(0);
                                        PointF startPoint = new PointF(TypewriterToolHandler.this.mStartPdfPoint.x, TypewriterToolHandler.this.mStartPdfPoint.y);
                                        TypewriterToolHandler.this.mPdfViewCtrl.convertPdfPtToPageViewPt(startPoint, startPoint, TypewriterToolHandler.this.mCreateIndex);
                                        TypewriterToolHandler.this.mPdfViewCtrl.gotoPage(TypewriterToolHandler.this.mCreateIndex, TypewriterToolHandler.this.mTextUtil.getPageViewOrigin(TypewriterToolHandler.this.mPdfViewCtrl, TypewriterToolHandler.this.mCreateIndex, startPoint.x, startPoint.y).x, TypewriterToolHandler.this.mTextUtil.getPageViewOrigin(TypewriterToolHandler.this.mPdfViewCtrl, TypewriterToolHandler.this.mCreateIndex, startPoint.x, startPoint.y).y);
                                    }
                                }
                            }
                            TypewriterToolHandler.this.mAnnotText = "";
                            TypewriterToolHandler.this.mStartPoint.set(0.0f, 0.0f);
                            TypewriterToolHandler.this.mEditPoint.set(0.0f, 0.0f);
                            TypewriterToolHandler.this.mLastPageIndex = -1;
                            if (TypewriterToolHandler.this.mIsContinue) {
                                TypewriterToolHandler.this.mListener.callBack();
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    TypewriterToolHandler.this.mAnnotText = "";
                    TypewriterToolHandler.this.mStartPoint.set(0.0f, 0.0f);
                    TypewriterToolHandler.this.mEditPoint.set(0.0f, 0.0f);
                    TypewriterToolHandler.this.mLastPageIndex = -1;
                    AppUtil.dismissInputSoft(TypewriterToolHandler.this.mEditView);
                    TypewriterToolHandler.this.mParent.removeView(TypewriterToolHandler.this.mEditView);
                    TypewriterToolHandler.this.mEditView = null;
                    TypewriterToolHandler.this.mBBoxHeight = 0.0f;
                    TypewriterToolHandler.this.mBBoxWidth = 0.0f;
                    TypewriterToolHandler.this.mCreating = false;
                    TypewriterToolHandler.this.mTextUtil.getBlink().removeCallbacks((Runnable) TypewriterToolHandler.this.mTextUtil.getBlink());
                }
            }));
        } catch (PDFException e2) {
            if (e2.getLastError() == 10) {
                this.mPdfViewCtrl.recoverForOOM();
            }
        }
    }

    public void onColorValueChanged(int color) {
        this.mColor = color;
        if (this.mPdfViewCtrl.isPageVisible(this.mLastPageIndex)) {
            PointF pdfPointF = new PointF(this.mStartPdfPoint.x, this.mStartPdfPoint.y);
            this.mPdfViewCtrl.convertPdfPtToPageViewPt(pdfPointF, pdfPointF, this.mLastPageIndex);
            RectF rectF = new RectF(pdfPointF.x, pdfPointF.y, pdfPointF.x + this.mBBoxWidth, pdfPointF.y + this.mBBoxHeight);
            this.mPdfViewCtrl.refresh(this.mLastPageIndex, new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        }
    }

    public void onOpacityValueChanged(int opacity) {
        this.mOpacity = opacity;
        if (this.mPdfViewCtrl.isPageVisible(this.mLastPageIndex)) {
            PointF pdfPointF = new PointF(this.mStartPdfPoint.x, this.mStartPdfPoint.y);
            this.mPdfViewCtrl.convertPdfPtToPageViewPt(pdfPointF, pdfPointF, this.mLastPageIndex);
            RectF rectF = new RectF(pdfPointF.x, pdfPointF.y, pdfPointF.x + this.mBBoxWidth, pdfPointF.y + this.mBBoxHeight);
            this.mPdfViewCtrl.refresh(this.mLastPageIndex, new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        }
    }

    public void onFontValueChanged(String font) {
        this.mFont = font;
        if (this.mPdfViewCtrl.isPageVisible(this.mLastPageIndex)) {
            PointF pdfPointF = new PointF(this.mStartPdfPoint.x, this.mStartPdfPoint.y);
            this.mPdfViewCtrl.convertPdfPtToPageViewPt(pdfPointF, pdfPointF, this.mLastPageIndex);
            RectF rectF = new RectF(pdfPointF.x, pdfPointF.y, pdfPointF.x + this.mBBoxWidth, pdfPointF.y + this.mBBoxHeight);
            this.mPdfViewCtrl.refresh(this.mLastPageIndex, new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        }
    }

    public void onFontSizeValueChanged(float fontSize) {
        this.mFontSize = fontSize;
        if (this.mPdfViewCtrl.isPageVisible(this.mLastPageIndex)) {
            PointF pdfPointF = new PointF(this.mStartPdfPoint.x, this.mStartPdfPoint.y);
            this.mPdfViewCtrl.convertPdfPtToPageViewPt(pdfPointF, pdfPointF, this.mLastPageIndex);
            adjustStartPt(this.mPdfViewCtrl, this.mLastPageIndex, pdfPointF);
            PointF pdfPtChanged = new PointF(pdfPointF.x, pdfPointF.y);
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pdfPtChanged, pdfPtChanged, this.mLastPageIndex);
            this.mStartPdfPoint.x = pdfPtChanged.x;
            this.mStartPdfPoint.y = pdfPtChanged.y;
            RectF rectF = new RectF(pdfPointF.x, pdfPointF.y, pdfPointF.x + this.mBBoxWidth, pdfPointF.y + this.mBBoxHeight);
            this.mPdfViewCtrl.refresh(this.mLastPageIndex, new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        }
    }

    private void adjustStartPt(PDFViewCtrl pdfViewCtrl, int pageIndex, PointF point) {
        if (((float) pdfViewCtrl.getPageViewWidth(pageIndex)) - point.x < this.mTextUtil.getFontWidth(pdfViewCtrl, pageIndex, this.mFont, this.mFontSize)) {
            point.x = this.mPageViewWidth - this.mTextUtil.getFontWidth(pdfViewCtrl, pageIndex, this.mFont, this.mFontSize);
        }
        if (((float) pdfViewCtrl.getPageViewHeight(pageIndex)) - point.y < this.mTextUtil.getFontHeight(pdfViewCtrl, pageIndex, this.mFont, this.mFontSize)) {
            point.y = this.mPageViewHeigh - this.mTextUtil.getFontHeight(pdfViewCtrl, pageIndex, this.mFont, this.mFontSize);
        }
    }

    private void setCreateAnnotListener(CreateAnnotResult listener) {
        this.mListener = listener;
    }

    public boolean getIsContinuousCreate() {
        return this.mIsContinuousCreate;
    }

    public void setIsContinuousCreate(boolean isContinuousCreate) {
        this.mIsContinuousCreate = isContinuousCreate;
    }
}
