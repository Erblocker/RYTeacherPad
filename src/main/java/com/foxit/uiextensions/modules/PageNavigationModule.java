package com.foxit.uiextensions.modules;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.textselect.TextSelectModule;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppKeyboardUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.OnPageEventListener;
import com.foxit.uiextensions.utils.ToolUtil;

public class PageNavigationModule implements Module {
    private static final int SHOW_OVER = 100;
    private static final int SHOW_RESET = 200;
    private LinearLayout mClosedPageLabel;
    private TextView mClosedPageLabel_Current;
    private TextView mClosedPageLabel_Total;
    private RelativeLayout mClosedRootLayout;
    private Context mContext;
    private AppDisplay mDisplay;
    private IDocEventListener mDocumentEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc pdfDoc, int errCode) {
            if (pdfDoc != null && errCode == 0) {
                PageNavigationModule.this.mPageEventListener.onPageChanged(PageNavigationModule.this.mPdfViewCtrl.getCurrentPage(), PageNavigationModule.this.mPdfViewCtrl.getCurrentPage());
                PageNavigationModule.this.mIsClosedState = true;
                PageNavigationModule.this.onUIStatusChanged();
            }
        }

        public void onDocWillClose(PDFDoc pdfDoc) {
        }

        public void onDocClosed(PDFDoc pdfDoc, int i) {
        }

        public void onDocWillSave(PDFDoc pdfDoc) {
        }

        public void onDocSaved(PDFDoc pdfDoc, int i) {
        }
    };
    private MyHandler mHandler;
    private InputMethodManager mInputMethodMgr = null;
    private boolean mIsClosedState = true;
    private ImageView mNextImageView;
    private OnKeyListener mOnKeyKListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode != 4 || event.getRepeatCount() != 0 || PageNavigationModule.this.mIsClosedState) {
                return false;
            }
            PageNavigationModule.this.mIsClosedState = true;
            PageNavigationModule.this.onUIStatusChanged();
            return true;
        }
    };
    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            PageNavigationModule.this.onUIStatusChanged();
            return false;
        }
    };
    private OpenJumpPageBackground mOpenJumpPageBackground;
    private ImageView mOpenedClearBtn;
    private TextView mOpenedGoBtn;
    private EditText mOpenedPageIndex;
    private RelativeLayout mOpenedRootLayout;
    private IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPageChanged(int old, int current) {
            PageNavigationModule.this.mClosedPageLabel.setEnabled(true);
            String str = (current + 1) + "/" + PageNavigationModule.this.mPdfViewCtrl.getPageCount();
            PageNavigationModule.this.mClosedPageLabel_Current.setText((current + 1));
            PageNavigationModule.this.mClosedPageLabel_Total.setText("/" + PageNavigationModule.this.mPdfViewCtrl.getPageCount());
            if (!PageNavigationModule.this.mIsClosedState) {
                PageNavigationModule.this.mOpenedPageIndex.setHint(str);
            }
            if (PageNavigationModule.this.mPdfViewCtrl.hasPrevView()) {
                PageNavigationModule.this.mPreImageView.setVisibility(0);
            } else {
                PageNavigationModule.this.mPreImageView.setVisibility(8);
            }
            if (PageNavigationModule.this.mPdfViewCtrl.hasNextView()) {
                PageNavigationModule.this.mNextImageView.setVisibility(0);
            } else {
                PageNavigationModule.this.mNextImageView.setVisibility(8);
            }
            if (PageNavigationModule.this.mClosedRootLayout.getVisibility() != 0) {
                PageNavigationModule.this.startShow();
                PageNavigationModule.this.mClosedRootLayout.setVisibility(0);
            }
            if (old != current) {
                Message msg = new Message();
                msg.what = 200;
                PageNavigationModule.this.mHandler.sendMessage(msg);
            }
        }

        public void onPageJumped() {
            if (PageNavigationModule.this.mPdfViewCtrl.hasPrevView()) {
                PageNavigationModule.this.mPreImageView.setVisibility(0);
            } else {
                PageNavigationModule.this.mPreImageView.setVisibility(8);
            }
            if (PageNavigationModule.this.mPdfViewCtrl.hasNextView()) {
                PageNavigationModule.this.mNextImageView.setVisibility(0);
            } else {
                PageNavigationModule.this.mNextImageView.setVisibility(8);
            }
            if (PageNavigationModule.this.mClosedRootLayout.getVisibility() != 0) {
                PageNavigationModule.this.mClosedRootLayout.setVisibility(0);
            }
            Message msg = new Message();
            msg.what = 200;
            PageNavigationModule.this.mHandler.sendMessage(msg);
        }

        public void onPageVisible(int index) {
        }

        public void onPageInvisible(int index) {
        }
    };
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private ImageView mPreImageView;
    private MyRunnable mRunnable;

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (PageNavigationModule.this.mPreImageView.getVisibility() == 0) {
                        PageNavigationModule.this.mPreImageView.setVisibility(8);
                    }
                    if (PageNavigationModule.this.mNextImageView.getVisibility() == 0) {
                        PageNavigationModule.this.mNextImageView.setVisibility(8);
                    }
                    if (PageNavigationModule.this.mClosedRootLayout.getVisibility() == 0) {
                        PageNavigationModule.this.mClosedRootLayout.setVisibility(8);
                        return;
                    }
                    return;
                case 200:
                    PageNavigationModule.this.mRunnable.reset();
                    return;
                default:
                    return;
            }
        }
    }

    class MyRunnable implements Runnable {
        private int mCurTime;
        private boolean mStart;

        MyRunnable() {
        }

        public void reset() {
            this.mCurTime = 0;
            this.mStart = true;
        }

        public void run() {
            while (true) {
                if (this.mStart) {
                    this.mCurTime = 0;
                    while (this.mCurTime < 3000) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        this.mCurTime += 100;
                    }
                    if (this.mStart) {
                        Message msg = new Message();
                        msg.what = 100;
                        PageNavigationModule.this.mHandler.sendMessage(msg);
                    }
                    this.mStart = false;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    class OpenJumpPageBackground extends RelativeLayout {
        public OpenJumpPageBackground(Context context) {
            super(context);
        }
    }

    public PageNavigationModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mDisplay = new AppDisplay(this.mContext);
    }

    public String getName() {
        return Module.MODULE_NAME_PAGENAV;
    }

    public boolean loadModule() {
        this.mInputMethodMgr = (InputMethodManager) this.mContext.getSystemService("input_method");
        this.mHandler = new MyHandler();
        this.mRunnable = new MyRunnable();
        initClosedUI();
        initOpenedUI();
        this.mPdfViewCtrl.registerDocEventListener(this.mDocumentEventListener);
        this.mPdfViewCtrl.registerPageEventListener(this.mPageEventListener);
        this.mPdfViewCtrl.setOnKeyListener(this.mOnKeyKListener);
        new Thread(this.mRunnable).start();
        onUIStatusChanged();
        return true;
    }

    public boolean unloadModule() {
        disInitClosedUI();
        disInitOpenedUI();
        this.mPdfViewCtrl.unregisterDocEventListener(this.mDocumentEventListener);
        this.mPdfViewCtrl.unregisterPageEventListener(this.mPageEventListener);
        return true;
    }

    private void initClosedUI() {
        this.mClosedRootLayout = (RelativeLayout) View.inflate(this.mContext, R.layout.rd_gotopage_close, null);
        this.mClosedPageLabel = (LinearLayout) this.mClosedRootLayout.findViewById(R.id.rd_gotopage_pagenumber);
        this.mClosedPageLabel_Total = (TextView) this.mClosedRootLayout.findViewById(R.id.rd_gotopage_pagenumber_total);
        this.mClosedPageLabel_Current = (TextView) this.mClosedRootLayout.findViewById(R.id.rd_gotopage_pagenumber_current);
        this.mClosedPageLabel_Current.setText("");
        this.mClosedPageLabel_Current.setTextColor(-1);
        this.mClosedPageLabel_Total.setText("-");
        this.mClosedPageLabel_Total.setTextColor(-1);
        this.mClosedPageLabel.setEnabled(false);
        this.mPreImageView = (ImageView) this.mClosedRootLayout.findViewById(R.id.rd_jumppage_previous);
        this.mNextImageView = (ImageView) this.mClosedRootLayout.findViewById(R.id.rd_jumppage_next);
        setClosedUIClickListener();
        LayoutParams closedLP = new LayoutParams(-2, -2);
        closedLP.addRule(12);
        this.mParent.addView(this.mClosedRootLayout, closedLP);
        if (this.mDisplay.isPad()) {
            this.mClosedRootLayout.setPadding((int) (AppResource.getDimension(this.mContext, R.dimen.ux_horz_left_margin_pad) + ((float) this.mDisplay.dp2px(4.0f))), 0, 0, (int) (AppResource.getDimension(this.mContext, R.dimen.ux_toolbar_height_pad) + ((float) this.mDisplay.dp2px(16.0f))));
        }
        this.mPreImageView.setVisibility(8);
        this.mNextImageView.setVisibility(8);
    }

    private void initOpenedUI() {
        this.mOpenedRootLayout = (RelativeLayout) View.inflate(this.mContext, R.layout.rd_gotopage_open, null);
        this.mOpenedPageIndex = (EditText) this.mOpenedRootLayout.findViewById(R.id.rd_gotopage_index_et);
        this.mOpenedClearBtn = (ImageView) this.mOpenedRootLayout.findViewById(R.id.rd_gotopage_edit_clear);
        this.mOpenedGoBtn = (TextView) this.mOpenedRootLayout.findViewById(R.id.rd_gotopage_togo_iv);
        this.mOpenJumpPageBackground = new OpenJumpPageBackground(this.mContext);
        this.mOpenedClearBtn.setVisibility(4);
        this.mOpenedRootLayout.setVisibility(8);
        this.mOpenJumpPageBackground.setVisibility(8);
        setOpenedClickListener();
    }

    private void addOpenedLayoutToMainFrame() {
        try {
            this.mOpenedRootLayout.setVisibility(0);
            this.mOpenJumpPageBackground.setVisibility(0);
            this.mParent.addView(this.mOpenedRootLayout);
            if (this.mDisplay.isPad()) {
                this.mOpenedRootLayout.getLayoutParams().height = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_height_pad);
            } else {
                this.mOpenedRootLayout.getLayoutParams().height = this.mContext.getResources().getDimensionPixelOffset(R.dimen.ux_toolbar_height_phone);
            }
            LayoutParams openJumpPageBackgroundLP = new LayoutParams(-1, -1);
            openJumpPageBackgroundLP.addRule(3, R.id.rd_gotopage_open_root_layout);
            this.mParent.addView(this.mOpenJumpPageBackground, openJumpPageBackgroundLP);
        } catch (Exception e) {
        }
    }

    private void removeOpenedLayoutFromMainFrame() {
        this.mParent.removeView(this.mOpenedRootLayout);
        this.mParent.removeView(this.mOpenJumpPageBackground);
    }

    private void setOpenedClickListener() {
        this.mOpenedGoBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AppUtil.isFastDoubleClick()) {
                    PageNavigationModule.this.onGotoPage();
                }
            }
        });
        this.mOpenedClearBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AppUtil.isFastDoubleClick() && PageNavigationModule.this.mOpenedPageIndex != null) {
                    PageNavigationModule.this.mOpenedPageIndex.setText("");
                }
            }
        });
        this.mOpenedPageIndex.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (66 != keyCode || event.getAction() != 0) {
                    return false;
                }
                ((InputMethodManager) PageNavigationModule.this.mOpenedPageIndex.getContext().getSystemService("input_method")).hideSoftInputFromWindow(PageNavigationModule.this.mOpenedPageIndex.getWindowToken(), 0);
                PageNavigationModule.this.onGotoPage();
                return true;
            }
        });
        this.mOpenedPageIndex.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (PageNavigationModule.this.mOpenedPageIndex == v && !hasFocus) {
                    ((InputMethodManager) PageNavigationModule.this.mOpenedPageIndex.getContext().getSystemService("input_method")).hideSoftInputFromWindow(PageNavigationModule.this.mOpenedPageIndex.getWindowToken(), 0);
                    PageNavigationModule.this.mIsClosedState = true;
                    PageNavigationModule.this.onUIStatusChanged();
                }
            }
        });
        this.mOpenedPageIndex.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (PageNavigationModule.this.mOpenedPageIndex.getText() == null) {
                    PageNavigationModule.this.mOpenedClearBtn.setVisibility(4);
                } else if (PageNavigationModule.this.mOpenedPageIndex.getText().length() != 0) {
                    PageNavigationModule.this.mOpenedClearBtn.setVisibility(0);
                    Integer number = null;
                    if (!PageNavigationModule.this.mOpenedPageIndex.getText().toString().trim().equals("")) {
                        int index = PageNavigationModule.this.mOpenedPageIndex.getText().toString().indexOf("/");
                        if (index == -1) {
                            try {
                                number = Integer.valueOf(PageNavigationModule.this.mOpenedPageIndex.getText().toString());
                            } catch (Exception e) {
                                number = null;
                            }
                        } else {
                            number = Integer.valueOf(PageNavigationModule.this.mOpenedPageIndex.getText().subSequence(0, index).toString());
                        }
                    }
                    if (number == null || number.intValue() < 0 || number.intValue() > PageNavigationModule.this.mPdfViewCtrl.getPageCount()) {
                        PageNavigationModule.this.mOpenedPageIndex.setText(PageNavigationModule.this.mOpenedPageIndex.getText().toString().substring(0, PageNavigationModule.this.mOpenedPageIndex.getText().length() - 1));
                        PageNavigationModule.this.mOpenedPageIndex.selectAll();
                        Toast toast = new Toast(PageNavigationModule.this.mContext);
                        String str = new StringBuilder(String.valueOf(AppResource.getString(PageNavigationModule.this.mContext, R.string.rv_gotopage_error_toast))).append(" ").append("(1-").append(String.valueOf(PageNavigationModule.this.mPdfViewCtrl.getPageCount())).append(")").toString();
                        View toastLayout = ((LayoutInflater) PageNavigationModule.this.mContext.getSystemService("layout_inflater")).inflate(R.layout.rd_gotopage_tips, null);
                        ((TextView) toastLayout.findViewById(R.id.rd_gotopage_toast_tv)).setText(str);
                        toast.setView(toastLayout);
                        toast.setDuration(1);
                        toast.setGravity(17, 0, 0);
                        toast.show();
                    }
                } else {
                    PageNavigationModule.this.mOpenedClearBtn.setVisibility(4);
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });
        this.mOpenedRootLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        this.mOpenJumpPageBackground.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!PageNavigationModule.this.mIsClosedState) {
                    PageNavigationModule.this.mIsClosedState = true;
                    ((InputMethodManager) PageNavigationModule.this.mOpenedPageIndex.getContext().getSystemService("input_method")).hideSoftInputFromWindow(PageNavigationModule.this.mOpenedPageIndex.getWindowToken(), 0);
                    PageNavigationModule.this.onUIStatusChanged();
                }
            }
        });
    }

    private void disInitClosedUI() {
        this.mParent.removeView(this.mClosedRootLayout);
    }

    private void disInitOpenedUI() {
        AppKeyboardUtil.removeKeyboardListener(this.mOpenedRootLayout);
        this.mParent.removeView(this.mOpenedRootLayout);
    }

    private void triggerDismissMenu() {
        TextSelectModule module = (TextSelectModule) ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_SELECTION);
        if (module != null) {
            module.triggerDismissMenu();
        }
        if (DocumentManager.getInstance(this.mPdfViewCtrl).getCurrentAnnot() != null) {
            DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        }
    }

    private void setClosedUIClickListener() {
        this.mClosedPageLabel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!(PageNavigationModule.this.mPdfViewCtrl.getDoc() == null || ((UIExtensionsManager) PageNavigationModule.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() == null)) {
                    ((UIExtensionsManager) PageNavigationModule.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
                PageNavigationModule.this.triggerDismissMenu();
                PageNavigationModule.this.mIsClosedState = false;
                int pageIndex = PageNavigationModule.this.mPdfViewCtrl.getCurrentPage();
                PageNavigationModule.this.mPageEventListener.onPageChanged(pageIndex, pageIndex);
                PageNavigationModule.this.addOpenedLayoutToMainFrame();
                PageNavigationModule.this.mOpenedPageIndex.selectAll();
                PageNavigationModule.this.mOpenedPageIndex.requestFocus();
                PageNavigationModule.this.mInputMethodMgr.showSoftInput(PageNavigationModule.this.mOpenedPageIndex, 0);
                PageNavigationModule.this.mOpenedPageIndex.setText("");
                PageNavigationModule.this.onUIStatusChanged();
            }
        });
        this.mPreImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!(PageNavigationModule.this.mPdfViewCtrl.getDoc() == null || ((UIExtensionsManager) PageNavigationModule.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() == null)) {
                    ((UIExtensionsManager) PageNavigationModule.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
                PageNavigationModule.this.mPdfViewCtrl.gotoPrevView();
                PageNavigationModule.this.triggerDismissMenu();
                if (PageNavigationModule.this.mPdfViewCtrl.hasPrevView()) {
                    PageNavigationModule.this.mPreImageView.setVisibility(0);
                } else {
                    PageNavigationModule.this.mPreImageView.setVisibility(8);
                }
                if (PageNavigationModule.this.mPdfViewCtrl.hasNextView()) {
                    PageNavigationModule.this.mNextImageView.setVisibility(0);
                } else {
                    PageNavigationModule.this.mNextImageView.setVisibility(8);
                }
                Message msg = new Message();
                msg.what = 200;
                PageNavigationModule.this.mHandler.sendMessage(msg);
            }
        });
        this.mNextImageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!(PageNavigationModule.this.mPdfViewCtrl.getDoc() == null || ((UIExtensionsManager) PageNavigationModule.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() == null)) {
                    ((UIExtensionsManager) PageNavigationModule.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
                PageNavigationModule.this.mPdfViewCtrl.gotoNextView();
                PageNavigationModule.this.triggerDismissMenu();
                if (PageNavigationModule.this.mPdfViewCtrl.hasPrevView()) {
                    PageNavigationModule.this.mPreImageView.setVisibility(0);
                } else {
                    PageNavigationModule.this.mPreImageView.setVisibility(8);
                }
                if (PageNavigationModule.this.mPdfViewCtrl.hasNextView()) {
                    PageNavigationModule.this.mNextImageView.setVisibility(0);
                } else {
                    PageNavigationModule.this.mNextImageView.setVisibility(8);
                }
                Message msg = new Message();
                msg.what = 200;
                PageNavigationModule.this.mHandler.sendMessage(msg);
            }
        });
        this.mClosedRootLayout.findViewById(R.id.rv_gotopage_relativeLayout).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
    }

    private void onUIStatusChanged() {
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager();
        if (!this.mIsClosedState && uiExtensionsManager.getCurrentToolHandler() == null && ToolUtil.getCurrentAnnotHandler(uiExtensionsManager) == null && this.mOpenedPageIndex.hasWindowFocus()) {
            if (this.mClosedRootLayout.getVisibility() != 8) {
                endShow();
                this.mClosedRootLayout.setVisibility(8);
            }
            if (this.mOpenedRootLayout.getVisibility() != 0) {
                this.mOpenedRootLayout.setVisibility(0);
                this.mOpenJumpPageBackground.setVisibility(0);
                addOpenedLayoutToMainFrame();
                return;
            }
            return;
        }
        if (!this.mIsClosedState) {
            this.mIsClosedState = true;
        }
        if (this.mClosedRootLayout.getVisibility() != 0) {
            startShow();
            this.mClosedPageLabel.setEnabled(true);
            this.mClosedRootLayout.setVisibility(0);
            if (this.mPdfViewCtrl.hasPrevView()) {
                this.mPreImageView.setVisibility(0);
            } else {
                this.mPreImageView.setVisibility(8);
            }
            if (this.mPdfViewCtrl.hasNextView()) {
                this.mNextImageView.setVisibility(0);
            } else {
                this.mNextImageView.setVisibility(8);
            }
        }
        Message msg = new Message();
        msg.what = 200;
        this.mHandler.sendMessage(msg);
        if (this.mOpenedRootLayout.getVisibility() != 8) {
            this.mOpenedRootLayout.setVisibility(8);
            this.mOpenJumpPageBackground.setVisibility(8);
            removeOpenedLayoutFromMainFrame();
        }
    }

    private void onGotoPage() {
        Toast toast = new Toast(this.mContext);
        Editable text = this.mOpenedPageIndex.getText();
        Integer number = null;
        if (!text.toString().trim().equals("")) {
            int index = text.toString().indexOf("/");
            if (index == -1) {
                try {
                    number = Integer.valueOf(text.toString());
                } catch (Exception e) {
                    number = null;
                }
            } else {
                number = Integer.valueOf(text.subSequence(0, index).toString());
            }
        }
        if (number == null || number.intValue() <= 0 || number.intValue() > this.mPdfViewCtrl.getPageCount()) {
            String str = new StringBuilder(String.valueOf(AppResource.getString(this.mContext, R.string.rv_gotopage_error_toast))).append(" ").append("(1-").append(String.valueOf(this.mPdfViewCtrl.getPageCount())).append(")").toString();
            View toastLayout = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.rd_gotopage_tips, null);
            ((TextView) toastLayout.findViewById(R.id.rd_gotopage_toast_tv)).setText(str);
            toast.setView(toastLayout);
            toast.setDuration(1);
            toast.setGravity(17, 0, 0);
            toast.show();
            this.mOpenedPageIndex.selectAll();
            return;
        }
        this.mPdfViewCtrl.gotoPage(number.intValue() - 1, 0.0f, 0.0f);
        this.mIsClosedState = true;
        this.mInputMethodMgr.hideSoftInputFromWindow(this.mOpenedPageIndex.getWindowToken(), 0);
        this.mIsClosedState = true;
        onUIStatusChanged();
        if (this.mClosedRootLayout.getVisibility() != 0) {
            startShow();
            this.mClosedRootLayout.setVisibility(0);
        }
        Message msg = new Message();
        msg.what = 200;
        this.mHandler.sendMessage(msg);
    }

    public void resetJumpView() {
        if (this.mPdfViewCtrl.hasPrevView()) {
            this.mPreImageView.setVisibility(0);
        } else {
            this.mPreImageView.setVisibility(8);
        }
        if (this.mPdfViewCtrl.hasNextView()) {
            this.mNextImageView.setVisibility(0);
        } else {
            this.mNextImageView.setVisibility(8);
        }
        if (this.mClosedRootLayout.getVisibility() != 0) {
            this.mClosedRootLayout.setVisibility(0);
        }
        this.mClosedPageLabel_Current.setText((this.mPdfViewCtrl.getCurrentPage() + 1));
        this.mClosedPageLabel_Total.setText("/" + this.mPdfViewCtrl.getPageCount());
        Message msg = new Message();
        msg.what = 200;
        this.mHandler.sendMessage(msg);
    }

    private void startShow() {
        this.mClosedRootLayout.startAnimation(AnimationUtils.loadAnimation(this.mContext, R.anim.view_anim_visible_show));
    }

    private void endShow() {
        this.mClosedRootLayout.startAnimation(AnimationUtils.loadAnimation(this.mContext, R.anim.view_anim_visible_hide));
    }

    public void changPageNumberState(boolean isToolbarsVisible) {
        if (isToolbarsVisible) {
            onUIStatusChanged();
            return;
        }
        if (this.mClosedRootLayout.getVisibility() != 8) {
            endShow();
            this.mClosedRootLayout.setVisibility(8);
        }
        if (this.mIsClosedState && this.mOpenedRootLayout.getVisibility() != 8) {
            this.mOpenedRootLayout.setVisibility(8);
            this.mOpenJumpPageBackground.setVisibility(8);
            removeOpenedLayoutFromMainFrame();
        }
    }
}
