package com.foxit.uiextensions.annots.form;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.form.Form;
import com.foxit.sdk.pdf.form.FormControl;
import com.foxit.sdk.pdf.form.FormField;
import com.foxit.sdk.pdf.form.FormFiller;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.AnnotHandler;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event.Callback;
import com.foxit.uiextensions.utils.ToolUtil;
import java.util.concurrent.CountDownLatch;

public class FormFillerAnnotHandler implements AnnotHandler {
    private static Form mForm;
    protected static boolean mIsNeedRefresh = true;
    protected static long mLastInputInvalidateTime = 0;
    private boolean bInitialize = false;
    private PDFPage curPage = null;
    private boolean isDocFinish = false;
    private boolean isDown = false;
    private boolean isFind = false;
    private boolean mAdjustPosition = false;
    private FormFillerAssistImpl mAssist;
    private Blink mBlink = null;
    private String mChangeText = null;
    private Context mContext;
    private CountDownLatch mCountDownLatch;
    private EditText mEditView = null;
    public FormNavigationModule mFNModule = null;
    private FormFiller mFormFiller;
    private Handler mHandler = null;
    private boolean mIsBackBtnPush = false;
    private boolean mIsShowEditText = false;
    private String mLastInputText = "";
    private PointF mLastTouchPoint = new PointF(0.0f, 0.0f);
    private int mOffset;
    private int mPageOffset;
    private ViewGroup mParent;
    private Paint mPathPaint;
    private PDFViewCtrl mPdfViewCtrl;
    private int nextAnnotIdx;
    private Runnable nextNavigation = new Runnable() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Annot curAnnot = DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot();
            if (curAnnot == null) {
                return;
            }
            if (curAnnot instanceof FormControl) {
                FormFillerAnnotHandler.this.refreshField(((FormControl) curAnnot).getField());
                FormFillerAnnotHandler.this.curPage = curAnnot.getPage();
                int curPageIdx = FormFillerAnnotHandler.this.curPage.getIndex();
                FormFillerAnnotHandler.this.nextPageIdx = curPageIdx;
                int curAnnotIdx = curAnnot.getIndex();
                FormFillerAnnotHandler.this.nextAnnotIdx = curAnnotIdx;
                FormFillerAnnotHandler.this.isFind = false;
                FormFillerAnnotHandler.this.isDocFinish = false;
                while (FormFillerAnnotHandler.this.nextPageIdx < FormFillerAnnotHandler.this.mPdfViewCtrl.getDoc().getPageCount()) {
                    FormFillerAnnotHandler formFillerAnnotHandler;
                    FormFillerAnnotHandler.this.mCountDownLatch = new CountDownLatch(1);
                    FormFillerAnnotHandler.this.curPage = FormFillerAnnotHandler.this.mPdfViewCtrl.getDoc().getPage(FormFillerAnnotHandler.this.nextPageIdx);
                    if (FormFillerAnnotHandler.this.nextPageIdx != curPageIdx || FormFillerAnnotHandler.this.isDocFinish) {
                        FormFillerAnnotHandler.this.nextAnnotIdx = 0;
                    } else {
                        FormFillerAnnotHandler.this.nextAnnotIdx = curAnnotIdx + 1;
                    }
                    while (FormFillerAnnotHandler.this.curPage != null && FormFillerAnnotHandler.this.nextAnnotIdx < FormFillerAnnotHandler.this.curPage.getAnnotCount()) {
                        final Annot nextAnnot = FormFillerAnnotHandler.this.curPage.getAnnot(FormFillerAnnotHandler.this.nextAnnotIdx);
                        if (nextAnnot != null && (nextAnnot instanceof FormControl) && !FormFillerUtil.isReadOnly(nextAnnot) && FormFillerUtil.isVisible(nextAnnot) && FormFillerUtil.getAnnotFieldType(FormFillerAnnotHandler.mForm, nextAnnot) != 1 && FormFillerUtil.getAnnotFieldType(FormFillerAnnotHandler.mForm, nextAnnot) != 7) {
                            FormFillerAnnotHandler.this.isFind = true;
                            FormFillerAnnotHandler.this.mHandler.post(new Runnable() {
                                public void run() {
                                    try {
                                        RectF rect;
                                        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) FormFillerAnnotHandler.this.mPdfViewCtrl.getUIExtensionsManager();
                                        if (FormFillerUtil.getAnnotFieldType(FormFillerAnnotHandler.mForm, nextAnnot) == 4) {
                                            rect = nextAnnot.getRect();
                                            rect.left += 5.0f;
                                            rect.top -= 5.0f;
                                            FormFillerAnnotHandler.this.mLastTouchPoint.set(rect.left, rect.top);
                                        }
                                        DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                        if (nextAnnot != null) {
                                            if (uiExtensionsManager.getCurrentToolHandler() != null) {
                                                uiExtensionsManager.setCurrentToolHandler(null);
                                            }
                                            rect = nextAnnot.getRect();
                                            if (FormFillerAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, FormFillerAnnotHandler.this.nextPageIdx)) {
                                                FormFillerAnnotHandler.this.mPdfViewCtrl.gotoPage(FormFillerAnnotHandler.this.nextPageIdx, rect.left - ((((float) FormFillerAnnotHandler.this.mPdfViewCtrl.getWidth()) - rect.width()) / 2.0f), rect.top - ((((float) FormFillerAnnotHandler.this.mPdfViewCtrl.getHeight()) - rect.height()) / 2.0f));
                                            } else {
                                                FormFillerAnnotHandler.this.mPdfViewCtrl.gotoPage(FormFillerAnnotHandler.this.nextPageIdx, new PointF(rect.left, rect.top));
                                            }
                                            if (uiExtensionsManager.getCurrentToolHandler() != null) {
                                                uiExtensionsManager.setCurrentToolHandler(null);
                                            }
                                            DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(nextAnnot);
                                            if (nextAnnot != null && (nextAnnot instanceof FormControl)) {
                                                FormFillerAnnotHandler.mIsNeedRefresh = true;
                                                FormFillerAnnotHandler.this.mFormFiller.setFocus((FormControl) nextAnnot);
                                            }
                                        }
                                    } catch (PDFException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            break;
                        }
                        formFillerAnnotHandler = FormFillerAnnotHandler.this;
                        formFillerAnnotHandler.nextAnnotIdx = formFillerAnnotHandler.nextAnnotIdx + 1;
                    }
                    FormFillerAnnotHandler.this.mCountDownLatch.countDown();
                    try {
                        if (FormFillerAnnotHandler.this.mCountDownLatch.getCount() > 0) {
                            FormFillerAnnotHandler.this.mCountDownLatch.await();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!FormFillerAnnotHandler.this.isFind) {
                        formFillerAnnotHandler = FormFillerAnnotHandler.this;
                        formFillerAnnotHandler.nextPageIdx = formFillerAnnotHandler.nextPageIdx + 1;
                        if (FormFillerAnnotHandler.this.nextPageIdx >= FormFillerAnnotHandler.this.mPdfViewCtrl.getDoc().getPageCount()) {
                            FormFillerAnnotHandler.this.nextPageIdx = 0;
                            FormFillerAnnotHandler.this.isDocFinish = true;
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    };
    private int nextPageIdx;
    PointF oldPoint = new PointF();
    private int preAnnotIdx;
    private Runnable preNavigation = new Runnable() {
        public void run() {
            Annot curAnnot = DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot();
            if (curAnnot == null) {
                return;
            }
            if (curAnnot instanceof FormControl) {
                FormFillerAnnotHandler.this.refreshField(((FormControl) curAnnot).getField());
                FormFillerAnnotHandler.this.curPage = curAnnot.getPage();
                int curPageIdx = FormFillerAnnotHandler.this.curPage.getIndex();
                FormFillerAnnotHandler.this.prePageIdx = curPageIdx;
                int curAnnotIdx = curAnnot.getIndex();
                FormFillerAnnotHandler.this.preAnnotIdx = curAnnotIdx;
                FormFillerAnnotHandler.this.isFind = false;
                FormFillerAnnotHandler.this.isDocFinish = false;
                while (FormFillerAnnotHandler.this.prePageIdx >= 0) {
                    FormFillerAnnotHandler formFillerAnnotHandler;
                    FormFillerAnnotHandler.this.mCountDownLatch = new CountDownLatch(1);
                    FormFillerAnnotHandler.this.curPage = FormFillerAnnotHandler.this.mPdfViewCtrl.getDoc().getPage(FormFillerAnnotHandler.this.prePageIdx);
                    if (FormFillerAnnotHandler.this.prePageIdx != curPageIdx || FormFillerAnnotHandler.this.isDocFinish) {
                        try {
                            FormFillerAnnotHandler.this.preAnnotIdx = FormFillerAnnotHandler.this.curPage.getAnnotCount() - 1;
                        } catch (PDFException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    FormFillerAnnotHandler.this.preAnnotIdx = curAnnotIdx - 1;
                    while (FormFillerAnnotHandler.this.curPage != null && FormFillerAnnotHandler.this.preAnnotIdx >= 0) {
                        final Annot preAnnot = FormFillerAnnotHandler.this.curPage.getAnnot(FormFillerAnnotHandler.this.preAnnotIdx);
                        if (preAnnot != null && (preAnnot instanceof FormControl) && !FormFillerUtil.isReadOnly(preAnnot) && FormFillerUtil.isVisible(preAnnot) && FormFillerUtil.getAnnotFieldType(FormFillerAnnotHandler.mForm, preAnnot) != 1 && FormFillerUtil.getAnnotFieldType(FormFillerAnnotHandler.mForm, preAnnot) != 7) {
                            FormFillerAnnotHandler.this.isFind = true;
                            FormFillerAnnotHandler.this.mHandler.post(new Runnable() {
                                public void run() {
                                    try {
                                        RectF rect;
                                        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) FormFillerAnnotHandler.this.mPdfViewCtrl.getUIExtensionsManager();
                                        if (FormFillerUtil.getAnnotFieldType(FormFillerAnnotHandler.mForm, preAnnot) == 4) {
                                            rect = preAnnot.getRect();
                                            rect.left += 5.0f;
                                            rect.top -= 5.0f;
                                            FormFillerAnnotHandler.this.mLastTouchPoint.set(rect.left, rect.top);
                                        }
                                        DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                                        if (preAnnot != null) {
                                            if (uiExtensionsManager.getCurrentToolHandler() != null) {
                                                uiExtensionsManager.setCurrentToolHandler(null);
                                            }
                                            rect = preAnnot.getRect();
                                            if (FormFillerAnnotHandler.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rect, FormFillerAnnotHandler.this.prePageIdx)) {
                                                FormFillerAnnotHandler.this.mPdfViewCtrl.gotoPage(FormFillerAnnotHandler.this.prePageIdx, rect.left - ((((float) FormFillerAnnotHandler.this.mPdfViewCtrl.getWidth()) - rect.width()) / 2.0f), rect.top - ((((float) FormFillerAnnotHandler.this.mPdfViewCtrl.getHeight()) - rect.height()) / 2.0f));
                                            } else {
                                                FormFillerAnnotHandler.this.mPdfViewCtrl.gotoPage(FormFillerAnnotHandler.this.prePageIdx, new PointF(rect.left, rect.top));
                                            }
                                            if (uiExtensionsManager.getCurrentToolHandler() != null) {
                                                uiExtensionsManager.setCurrentToolHandler(null);
                                            }
                                            DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(preAnnot);
                                            if (preAnnot != null && (preAnnot instanceof FormControl)) {
                                                try {
                                                    FormFillerAnnotHandler.mIsNeedRefresh = true;
                                                    FormFillerAnnotHandler.this.mFormFiller.setFocus((FormControl) preAnnot);
                                                } catch (PDFException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } catch (PDFException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                            });
                            break;
                        }
                        formFillerAnnotHandler = FormFillerAnnotHandler.this;
                        formFillerAnnotHandler.preAnnotIdx = formFillerAnnotHandler.preAnnotIdx - 1;
                    }
                    FormFillerAnnotHandler.this.mCountDownLatch.countDown();
                    try {
                        if (FormFillerAnnotHandler.this.mCountDownLatch.getCount() > 0) {
                            FormFillerAnnotHandler.this.mCountDownLatch.await();
                        }
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    if (!FormFillerAnnotHandler.this.isFind) {
                        formFillerAnnotHandler = FormFillerAnnotHandler.this;
                        formFillerAnnotHandler.prePageIdx = formFillerAnnotHandler.prePageIdx - 1;
                        if (FormFillerAnnotHandler.this.prePageIdx < 0) {
                            FormFillerAnnotHandler.this.prePageIdx = FormFillerAnnotHandler.this.mPdfViewCtrl.getDoc().getPageCount() - 1;
                            FormFillerAnnotHandler.this.isDocFinish = true;
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    };
    private int prePageIdx;

    private class Blink extends Handler implements Runnable {
        private Annot mAnnot;
        private int mHeight;

        public Blink(Annot annot) {
            this.mAnnot = annot;
        }

        public void setAnnot(Annot annot) {
            this.mAnnot = annot;
        }

        public void run() {
            int height = FormFillerAnnotHandler.this.getKeyboardHeight();
            if (height < AppDisplay.getInstance(FormFillerAnnotHandler.this.mContext).getRawScreenHeight() / 5) {
                FormFillerAnnotHandler.this.mFNModule.getLayout().setPadding(0, 0, 0, 0);
            }
            if (this.mHeight != height) {
                FormFillerAnnotHandler.this.mFNModule.getLayout().setPadding(0, 0, 0, height);
                this.mHeight = height;
            }
            postDelayed(this, 500);
        }
    }

    private class dismissNavigation extends Handler implements Runnable {
        private dismissNavigation() {
        }

        public void run() {
            if (FormFillerAnnotHandler.this.mPdfViewCtrl != null && FormFillerAnnotHandler.this.mPdfViewCtrl.getDoc() != null) {
                if (DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot() == null || !(DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot() instanceof FormControl)) {
                    FormFillerAnnotHandler.this.mFNModule.getLayout().setVisibility(4);
                    AppUtil.dismissInputSoft(FormFillerAnnotHandler.this.mEditView);
                    FormFillerAnnotHandler.this.resetDocViewerOffset();
                }
            }
        }
    }

    public FormFillerAnnotHandler(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
    }

    public void init(Form form) {
        this.mAssist = new FormFillerAssistImpl(this.mPdfViewCtrl);
        this.mAssist.bWillClose = false;
        mForm = form;
        this.mPathPaint = new Paint(1);
        this.mPathPaint.setStyle(Style.STROKE);
        this.mPathPaint.setAntiAlias(true);
        this.mPathPaint.setDither(true);
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mPathPaint.setPathEffect(new DashPathEffect(new float[]{1.0f, 2.0f, 4.0f, 8.0f}, 1.0f));
        try {
            this.mFormFiller = FormFiller.create(form, this.mAssist);
            this.mFormFiller.highlightFormFields(true);
            this.mFNModule = (FormNavigationModule) ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_FORM_NAVIGATION);
            if (this.mFNModule != null) {
                this.mFNModule.getLayout().setVisibility(4);
                this.mFNModule.getPreView().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        new Thread(FormFillerAnnotHandler.this.preNavigation).start();
                    }
                });
                this.mFNModule.getNextView().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        new Thread(FormFillerAnnotHandler.this.nextNavigation).start();
                    }
                });
                this.mFNModule.getClearView().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Annot annot = DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot();
                        if (annot != null && (annot instanceof FormControl)) {
                            FormControl formControl = (FormControl) annot;
                            try {
                                FormFillerAnnotHandler.this.mFormFiller.setFocus(null);
                                FormField field = formControl.getField();
                                field.reset();
                                FormFillerAnnotHandler.this.mFormFiller.setFocus(formControl);
                                FormFillerAnnotHandler.this.refreshField(field);
                            } catch (PDFException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                this.mFNModule.getFinishView().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot() != null) {
                            if (FormFillerAnnotHandler.this.shouldShowInputSoft(DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).getCurrentAnnot())) {
                                if (FormFillerAnnotHandler.this.mBlink != null) {
                                    FormFillerAnnotHandler.this.mBlink.removeCallbacks(FormFillerAnnotHandler.this.mBlink);
                                }
                                FormFillerAnnotHandler.this.mBlink = null;
                                AppUtil.dismissInputSoft(FormFillerAnnotHandler.this.mEditView);
                                FormFillerAnnotHandler.this.mParent.removeView(FormFillerAnnotHandler.this.mEditView);
                            }
                            DocumentManager.getInstance(FormFillerAnnotHandler.this.mPdfViewCtrl).setCurrentAnnot(null);
                        }
                        FormFillerAnnotHandler.this.mFNModule.getLayout().setVisibility(4);
                        FormFillerAnnotHandler.this.resetDocViewerOffset();
                    }
                });
                this.mFNModule.setClearEnable(false);
            }
            this.bInitialize = true;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    protected boolean hasInitialized() {
        return this.bInitialize;
    }

    private void postDismissNavigation() {
        dismissNavigation dn = new dismissNavigation();
        dn.postDelayed(dn, 500);
    }

    private boolean shouldShowNavigation(Annot annot) {
        if (annot == null || !(annot instanceof FormControl) || FormFillerUtil.getAnnotFieldType(mForm, annot) == 1) {
            return false;
        }
        return true;
    }

    public void NavigationDismiss() {
        this.mFNModule.getLayout().setVisibility(4);
        this.mFNModule.getLayout().setPadding(0, 0, 0, 0);
        if (this.mBlink != null) {
            this.mBlink.removeCallbacks(this.mBlink);
        }
        this.mBlink = null;
        if (this.mEditView != null) {
            this.mParent.removeView(this.mEditView);
        }
        resetDocViewerOffset();
        AppUtil.dismissInputSoft(this.mEditView);
    }

    private void refreshField(FormField field) {
        int nPageCount = this.mPdfViewCtrl.getPageCount();
        for (int i = 0; i < nPageCount; i++) {
            if (this.mPdfViewCtrl.isPageVisible(i)) {
                RectF rectF = getRefreshRect(field, i);
                if (rectF != null) {
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(rectF, rectF, i);
                    this.mPdfViewCtrl.refresh(i, AppDmUtil.rectFToRect(rectF));
                }
            }
        }
    }

    private RectF getRefreshRect(FormField field, int pageIndex) {
        PDFException e;
        RectF rectF = null;
        try {
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            int nControlCount = field.getControlCount(page);
            int i = 0;
            RectF rectF2 = null;
            while (i < nControlCount) {
                try {
                    FormControl formControl = field.getControl(page, i);
                    if (rectF2 == null) {
                        rectF = new RectF(formControl.getRect());
                    } else {
                        rectF2.union(formControl.getRect());
                        rectF = rectF2;
                    }
                    i++;
                    rectF2 = rectF;
                } catch (PDFException e2) {
                    e = e2;
                    rectF = rectF2;
                }
            }
            return rectF2;
        } catch (PDFException e3) {
            e = e3;
            e.printStackTrace();
            return rectF;
        }
    }

    protected void clear() {
        if (this.mAssist != null) {
            this.mAssist.bWillClose = true;
        }
        if (this.mFormFiller != null) {
            try {
                this.mFormFiller.release();
                this.mFormFiller = null;
            } catch (PDFException e) {
            }
        }
    }

    public FormFillerAssistImpl getFormFillerAssist() {
        return this.mAssist;
    }

    public int getType() {
        return 20;
    }

    public boolean annotCanAnswer(Annot annot) {
        return true;
    }

    public RectF getAnnotBBox(Annot annot) {
        try {
            return annot.getRect();
        } catch (PDFException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isHitAnnot(Annot annot, PointF point) {
        try {
            RectF r = annot.getRect();
            RectF rf = new RectF(r.left, r.top, r.right, r.bottom);
            PointF p = new PointF(point.x, point.y);
            int pageIndex = annot.getPage().getIndex();
            FormControl control = AppAnnotUtil.getControlAtPos(annot.getPage(), p, 1.0f);
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(rf, rf, pageIndex);
            this.mPdfViewCtrl.convertPdfPtToPageViewPt(p, p, pageIndex);
            if (rf.contains(p.x, p.y) || AppAnnotUtil.isSameAnnot(annot, control)) {
                return true;
            }
            return false;
        } catch (PDFException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void onBackspaceBtnDown() {
        try {
            mIsNeedRefresh = true;
            this.mFormFiller.input('\b');
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    public void onAnnotSelected(Annot annot, boolean needInvalid) {
        if (shouldShowInputSoft(annot)) {
            this.mIsShowEditText = true;
            this.mAdjustPosition = true;
            this.mLastInputText = " ";
            if (this.mEditView != null) {
                this.mParent.removeView(this.mEditView);
            }
            this.mEditView = new EditText(this.mContext);
            this.mEditView.setLayoutParams(new LayoutParams(1, 1));
            this.mEditView.setSingleLine(false);
            this.mEditView.setText(" ");
            this.mEditView.setOnEditorActionListener(new OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId != 6) {
                        return false;
                    }
                    new Thread(FormFillerAnnotHandler.this.nextNavigation).start();
                    return true;
                }
            });
            this.mParent.addView(this.mEditView);
            AppUtil.showSoftInput(this.mEditView);
            this.mEditView.setOnKeyListener(new OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == 67 && event.getAction() == 0) {
                        FormFillerAnnotHandler.this.onBackspaceBtnDown();
                        FormFillerAnnotHandler.this.mIsBackBtnPush = true;
                    }
                    return false;
                }
            });
            this.mEditView.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        if (s.length() >= FormFillerAnnotHandler.this.mLastInputText.length()) {
                            int i;
                            char c;
                            char value;
                            if (!FormFillerAnnotHandler.this.mChangeText.equals(s.subSequence(start, start + before).toString())) {
                                for (i = 0; i < before; i++) {
                                    FormFillerAnnotHandler.this.onBackspaceBtnDown();
                                }
                                for (i = 0; i < count; i++) {
                                    c = s.charAt((s.length() - count) + i);
                                    if (FormFillerUtil.isEmojiCharacter(c)) {
                                        break;
                                    }
                                    if (c == '\n') {
                                        c = '\r';
                                    }
                                    value = c;
                                    FormFillerAnnotHandler.mIsNeedRefresh = true;
                                    FormFillerAnnotHandler.this.mFormFiller.input(value);
                                    FormFillerAnnotHandler.mLastInputInvalidateTime = System.currentTimeMillis();
                                }
                            } else {
                                for (i = 0; i < s.length() - FormFillerAnnotHandler.this.mLastInputText.length(); i++) {
                                    c = s.charAt(FormFillerAnnotHandler.this.mLastInputText.length() + i);
                                    if (FormFillerUtil.isEmojiCharacter(c)) {
                                        break;
                                    }
                                    if (c == '\n') {
                                        c = '\r';
                                    }
                                    value = c;
                                    FormFillerAnnotHandler.mIsNeedRefresh = true;
                                    FormFillerAnnotHandler.this.mFormFiller.input(value);
                                    FormFillerAnnotHandler.mLastInputInvalidateTime = System.currentTimeMillis();
                                }
                            }
                        } else if (s.length() < FormFillerAnnotHandler.this.mLastInputText.length()) {
                            if (!FormFillerAnnotHandler.this.mIsBackBtnPush) {
                                FormFillerAnnotHandler.this.onBackspaceBtnDown();
                            }
                            FormFillerAnnotHandler.this.mIsBackBtnPush = false;
                        }
                        if (s.toString().length() == 0) {
                            FormFillerAnnotHandler.this.mLastInputText = " ";
                        } else {
                            FormFillerAnnotHandler.this.mLastInputText = s.toString();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    FormFillerAnnotHandler.this.mChangeText = s.subSequence(start, start + count).toString();
                }

                public void afterTextChanged(Editable s) {
                    if (s.toString().length() == 0) {
                        s.append(" ");
                    }
                }
            });
            if (this.mBlink == null) {
                this.mBlink = new Blink(annot);
                this.mBlink.postDelayed(this.mBlink, 300);
            } else {
                this.mBlink.setAnnot(annot);
            }
        }
        int fieldType = FormFillerUtil.getAnnotFieldType(mForm, annot);
        if (FormFillerUtil.isReadOnly(annot)) {
            this.mFNModule.setClearEnable(false);
        } else {
            this.mFNModule.setClearEnable(true);
        }
        if (fieldType != 1) {
            this.mFNModule.getLayout().setVisibility(0);
        }
    }

    public void onAnnotDeselected(Annot annot, boolean needInvalid) {
        mIsNeedRefresh = true;
        postDismissNavigation();
        try {
            this.mFormFiller.setFocus(null);
            if (annot != null && (annot instanceof FormControl)) {
                refreshField(((FormControl) annot).getField());
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        if (this.mIsShowEditText) {
            AppUtil.dismissInputSoft(this.mEditView);
            this.mParent.removeView(this.mEditView);
            this.mIsShowEditText = false;
        }
    }

    public boolean onTouchEvent(int pageIndex, MotionEvent motionEvent, Annot annot) {
        mIsNeedRefresh = false;
        try {
            if (!DocumentManager.getInstance(this.mPdfViewCtrl).canFillForm()) {
                return false;
            }
            if (FormFillerUtil.isReadOnly(annot)) {
                return false;
            }
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            RectF annotRectF = annot.getRect();
            this.mPdfViewCtrl.convertPdfRectToPageViewRect(annotRectF, annotRectF, pageIndex);
            PointF devPt = new PointF(motionEvent.getX(), motionEvent.getY());
            PointF pageViewPt = new PointF();
            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(devPt, pageViewPt, pageIndex);
            PointF pdfPointF = new PointF();
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pageViewPt, pdfPointF, pageIndex);
            switch (motionEvent.getActionMasked()) {
                case 0:
                    if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() || pageIndex != annot.getPage().getIndex() || !isHitAnnot(annot, pdfPointF)) {
                        return false;
                    }
                    this.isDown = true;
                    this.mFormFiller.touchDown(page, pdfPointF);
                    refresh(pageIndex);
                    return true;
                case 1:
                case 3:
                    if (pageIndex != annot.getPage().getIndex() || (!isHitAnnot(annot, pdfPointF) && !this.isDown)) {
                        return false;
                    }
                    this.isDown = false;
                    this.mFormFiller.touchUp(page, pdfPointF);
                    refresh(pageIndex);
                    return true;
                case 2:
                    if (getDistanceOfPoints(pageViewPt, this.oldPoint) <= 0.0d || annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() || pageIndex != annot.getPage().getIndex()) {
                        return false;
                    }
                    this.oldPoint.set(pageViewPt);
                    this.mFormFiller.touchMove(page, pdfPointF);
                    refresh(pageIndex);
                    return true;
            }
            return false;
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }

    private void refresh(int pageIndex) {
        RectF r = new RectF(this.mAssist.getInvalidateRect());
        this.mAssist.resetInvalidateRect();
        r.inset(-5.0f, -5.0f);
        this.mPdfViewCtrl.refresh(pageIndex, AppDmUtil.rectFToRect(r));
    }

    private double getDistanceOfPoints(PointF p1, PointF p2) {
        return Math.sqrt((double) Math.abs(((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y))));
    }

    public boolean onLongPress(int pageIndex, MotionEvent motionEvent, Annot annot) {
        if (DocumentManager.getInstance(this.mPdfViewCtrl).canFillForm() && !FormFillerUtil.isReadOnly(annot)) {
            return true;
        }
        return false;
    }

    public boolean onSingleTapConfirmed(int pageIndex, MotionEvent motionEvent, Annot annot) {
        this.mLastTouchPoint.set(0.0f, 0.0f);
        boolean ret = false;
        if (!DocumentManager.getInstance(this.mPdfViewCtrl).canFillForm()) {
            return false;
        }
        if (FormFillerUtil.isReadOnly(annot)) {
            return false;
        }
        try {
            PointF docViewerPt = new PointF(motionEvent.getX(), motionEvent.getY());
            PointF point = new PointF();
            this.mPdfViewCtrl.convertDisplayViewPtToPageViewPt(docViewerPt, point, pageIndex);
            PointF pageViewPt = new PointF(point.x, point.y);
            PointF pdfPointF = new PointF();
            this.mPdfViewCtrl.convertPageViewPtToPdfPt(pageViewPt, pdfPointF, pageIndex);
            PDFPage page = this.mPdfViewCtrl.getDoc().getPage(pageIndex);
            Annot annotTmp = page.getAnnotAtPos(pdfPointF, 1.0f);
            boolean isHit = isHitAnnot(annot, pdfPointF);
            if (annot != DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot()) {
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(annot);
                ret = true;
            } else if (pageIndex == annot.getPage().getIndex() && isHit) {
                ret = true;
            } else {
                if (shouldShowNavigation(annot)) {
                    if (this.mBlink != null) {
                        this.mBlink.removeCallbacks(this.mBlink);
                    }
                    this.mBlink = null;
                    this.mFNModule.getLayout().setVisibility(4);
                    this.mFNModule.getLayout().setPadding(0, 0, 0, 0);
                    resetDocViewerOffset();
                }
                DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
                ret = false;
            }
            PDFPage finalPage = page;
            if (annotTmp == null || (annot == DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() && pageIndex == annot.getPage().getIndex() && isHit)) {
                this.mFormFiller.click(finalPage, pdfPointF);
                refresh(pageIndex);
            }
        } catch (PDFException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static PointF getPageViewOrigin(PDFViewCtrl pdfViewCtrl, int pageIndex, float x, float y) {
        PointF pagePt = new PointF(x, y);
        pdfViewCtrl.convertPageViewPtToDisplayViewPt(pagePt, pagePt, pageIndex);
        RectF rect = new RectF(0.0f, 0.0f, pagePt.x, pagePt.y);
        pdfViewCtrl.convertDisplayViewRectToPageViewRect(rect, rect, pageIndex);
        return new PointF(x - rect.width(), y - rect.height());
    }

    private int getKeyboardHeight() {
        if (this.mContext == null) {
            return 0;
        }
        Rect r = new Rect();
        this.mParent.getWindowVisibleDisplayFrame(r);
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) this.mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels - (r.bottom - r.top);
    }

    public void onDraw(int pageIndex, Canvas canvas) {
        Annot annot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (annot != null && (annot instanceof FormControl) && ToolUtil.getCurrentAnnotHandler((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()) == this) {
            try {
                RectF rect = annot.getRect();
                PointF pointF = new PointF(rect.left, rect.bottom);
                pointF = new PointF(rect.left, rect.bottom);
                this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                this.mPdfViewCtrl.convertPdfPtToPageViewPt(pointF, pointF, pageIndex);
                this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(pointF, pointF, pageIndex);
                int type = FormFillerUtil.getAnnotFieldType(mForm, annot);
                if ((type == 6 || (type == 4 && (((FormControl) annot).getField().getFlags() & 256) != 0)) && this.mAdjustPosition && getKeyboardHeight() > AppDisplay.getInstance(this.mContext).getRawScreenHeight() / 5) {
                    if (((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - pointF.y < ((float) (getKeyboardHeight() + AppDisplay.getInstance(this.mContext).dp2px(116.0f)))) {
                        this.mPageOffset = (int) (((float) getKeyboardHeight()) - (((float) AppDisplay.getInstance(this.mContext).getRawScreenHeight()) - pointF.y));
                        if ((this.mPageOffset != 0 && pageIndex == this.mPdfViewCtrl.getPageCount() - 1) || this.mPdfViewCtrl.getPageLayoutMode() == 1) {
                            pointF = new PointF(0.0f, (float) this.mPdfViewCtrl.getPageViewHeight(pageIndex));
                            this.mPdfViewCtrl.convertPageViewPtToDisplayViewPt(pointF, pointF, pageIndex);
                            if (pointF.y <= ((float) AppDisplay.getInstance(this.mContext).getScreenHeight())) {
                                setBottomOffset(this.mPageOffset + AppDisplay.getInstance(this.mContext).dp2px(116.0f));
                            }
                        }
                        PDFViewCtrl pDFViewCtrl = this.mPdfViewCtrl;
                        float f = getPageViewOrigin(this.mPdfViewCtrl, pageIndex, pointF.x, pointF.y).x;
                        PDFViewCtrl pDFViewCtrl2 = this.mPdfViewCtrl;
                        float f2 = pointF.x;
                        float f3 = pointF.y;
                        pDFViewCtrl.gotoPage(pageIndex, f, (getPageViewOrigin(pDFViewCtrl2, pageIndex, f2, f3).y + ((float) this.mPageOffset)) + ((float) AppDisplay.getInstance(this.mContext).dp2px(116.0f)));
                        this.mAdjustPosition = false;
                    } else {
                        resetDocViewerOffset();
                    }
                }
                if (!(pageIndex == this.mPdfViewCtrl.getPageCount() - 1 || this.mPdfViewCtrl.getPageLayoutMode() == 1)) {
                    resetDocViewerOffset();
                }
                if (getKeyboardHeight() < AppDisplay.getInstance(this.mContext).getRawScreenHeight() / 5 && (pageIndex == this.mPdfViewCtrl.getPageCount() - 1 || this.mPdfViewCtrl.getPageLayoutMode() == 1)) {
                    resetDocViewerOffset();
                }
                Annot currentAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
                int fieldType = FormFillerUtil.getAnnotFieldType(mForm, currentAnnot);
                if (!(currentAnnot == null || !(currentAnnot instanceof FormControl) || fieldType == 1)) {
                    if (fieldType == 6 || (fieldType == 4 && (((FormControl) annot).getField().getFlags() & 256) != 0)) {
                        int paddingBottom = getKeyboardHeight();
                        if (VERSION.SDK_INT < 14 && getKeyboardHeight() < AppDisplay.getInstance(this.mContext).getRawScreenHeight() / 5) {
                            paddingBottom = 0;
                        }
                        this.mFNModule.getLayout().setPadding(0, 0, 0, paddingBottom);
                    } else {
                        this.mFNModule.getLayout().setPadding(0, 0, 0, 0);
                    }
                }
                if (DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() == null) {
                    this.mFNModule.getLayout().setVisibility(4);
                }
                canvas.save();
                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
                if (annot.getPage().getIndex() == pageIndex && fieldType != 1) {
                    RectF bbox = annot.getRect();
                    this.mPdfViewCtrl.convertPdfRectToPageViewRect(bbox, bbox, pageIndex);
                    bbox.sort();
                    bbox.inset(-5.0f, -5.0f);
                    canvas.drawLine(bbox.left, bbox.top, bbox.left, bbox.bottom, this.mPathPaint);
                    canvas.drawLine(bbox.left, bbox.bottom, bbox.right, bbox.bottom, this.mPathPaint);
                    canvas.drawLine(bbox.right, bbox.bottom, bbox.right, bbox.top, this.mPathPaint);
                    canvas.drawLine(bbox.left, bbox.top, bbox.right, bbox.top, this.mPathPaint);
                }
                canvas.restore();
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAnnot(int pageIndex, AnnotContent contentSupplier, boolean addUndo, Callback result) {
    }

    public void modifyAnnot(Annot annot, AnnotContent content, boolean addUndo, Callback result) {
    }

    public void removeAnnot(Annot annot, boolean addUndo, Callback result) {
    }

    private boolean shouldShowInputSoft(Annot annot) {
        if (annot == null || !(annot instanceof FormControl)) {
            return false;
        }
        int type = FormFillerUtil.getAnnotFieldType(mForm, annot);
        if (type != 6) {
            if (type != 4) {
                return false;
            }
            try {
                if ((((FormControl) annot).getField().getFlags() & 256) == 0) {
                    return false;
                }
            } catch (PDFException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void resetDocViewerOffset() {
        if (this.mPageOffset != 0) {
            this.mPageOffset = 0;
            setBottomOffset(0);
        }
    }

    private void setBottomOffset(int offset) {
        if (this.mOffset != (-offset)) {
            this.mOffset = -offset;
            this.mPdfViewCtrl.layout(0, this.mOffset + 0, this.mPdfViewCtrl.getWidth(), this.mPdfViewCtrl.getHeight() + this.mOffset);
        }
    }

    protected boolean onKeyBack() {
        Annot curAnnot = DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot();
        if (curAnnot == null) {
            return false;
        }
        try {
            if (curAnnot.getType() != 20) {
                return false;
            }
            FormField field = ((FormControl) curAnnot).getField();
            if (field == null || field.getType() == 7 || field.getType() == 0) {
                return false;
            }
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
            NavigationDismiss();
            return true;
        } catch (PDFException e) {
            e.printStackTrace();
            return false;
        }
    }
}
