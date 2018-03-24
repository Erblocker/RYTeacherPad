package com.foxit.uiextensions.modules;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.pdf.Bookmark;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.UIExtensionsManager.ConfigurationChangedListener;
import com.foxit.uiextensions.controls.panel.PanelHost;
import com.foxit.uiextensions.controls.panel.PanelSpec;
import com.foxit.uiextensions.controls.panel.impl.PanelHostImpl;
import com.foxit.uiextensions.utils.AppDisplay;
import java.util.ArrayList;

public class OutlineModule implements Module, PanelSpec {
    public static final float RD_PANEL_WIDTH_SCALE_H = 0.338f;
    public static final float RD_PANEL_WIDTH_SCALE_V = 0.535f;
    private ImageView mBack;
    private ConfigurationChangedListener mConfigurationChangedListener = new ConfigurationChangedListener() {
        public void onConfigurationChanged(Configuration newConfig) {
            if (OutlineModule.this.mPanelPopupWindow != null && OutlineModule.this.mPanelPopupWindow.isShowing() && OutlineModule.this.mPanelHost != null && OutlineModule.this.mPanelHost.getCurrentSpec() == OutlineModule.this) {
                OutlineModule.this.mPanelPopupWindow.dismiss();
                OutlineModule.this.show();
            }
        }
    };
    private LinearLayout mContent;
    private RelativeLayout mContentView;
    private Context mContext;
    private AppDisplay mDisplay;
    private IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc pdfDoc, int errCode) {
            if (pdfDoc != null && errCode == PDFError.NO_ERROR.getCode()) {
                OutlineModule.this.mOutlineSupport.init();
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
    private int mLevel = 0;
    private ListView mListView;
    private LinearLayout mLlBack;
    private TextView mNoInfoView;
    private ArrayList<OutlineItem> mOutlineArr = new ArrayList();
    private OutlineSupport mOutlineSupport;
    private FrameLayout mOutlineTopBar;
    private PanelHost mPanelHost;
    private PopupWindow mPanelPopupWindow = null;
    private RelativeLayout mPanel_outline_topbar;
    private ImageView mPanel_outline_topbar_close;
    private TextView mPanel_outline_topbar_title;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private ImageView mSeparate;

    public static class OutlineItem {
        public Bookmark mBookmark;
        public ArrayList<OutlineItem> mChildren = new ArrayList();
        public boolean mHaveChild = true;
        public boolean mIsExpanded = false;
        public int mLevel = -1;
        public long mNdkAddr = 0;
        public int mPageIndex = -1;
        public int mParentPos;
        public String mTitle = null;
        public float mX = 0.0f;
        public float mY = 0.0f;
    }

    public OutlineModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mContext = context;
        this.mParent = parent;
        this.mPanelHost = null;
    }

    public void setPanelHost(PanelHost panelHost) {
        this.mPanelHost = panelHost;
    }

    public PanelHost getPanelHost() {
        return this.mPanelHost;
    }

    public PopupWindow getPopupWindow() {
        return this.mPanelPopupWindow;
    }

    public void setPopupWindow(PopupWindow window) {
        this.mPanelPopupWindow = window;
    }

    public void show() {
        boolean bVertical;
        int height;
        int width;
        int viewWidth = this.mParent.getWidth();
        int viewHeight = this.mParent.getHeight();
        if (this.mContext.getResources().getConfiguration().orientation == 1) {
            bVertical = true;
        } else {
            bVertical = false;
        }
        if (bVertical) {
            height = Math.max(viewWidth, viewHeight);
            width = Math.min(viewWidth, viewHeight);
        } else {
            height = Math.min(viewWidth, viewHeight);
            width = Math.max(viewWidth, viewHeight);
        }
        if (this.mDisplay.isPad()) {
            float scale = 0.535f;
            if (width > height) {
                scale = 0.338f;
            }
            width = (int) (((float) this.mDisplay.getScreenWidth()) * scale);
        }
        this.mPanelPopupWindow.setWidth(width);
        this.mPanelPopupWindow.setHeight(height);
        this.mPanelPopupWindow.setSoftInputMode(1);
        this.mPanelPopupWindow.setSoftInputMode(48);
        this.mPanelHost.setCurrentSpec(1);
        this.mPanelPopupWindow.showAtLocation(this.mPdfViewCtrl, 51, 0, 0);
    }

    public String getName() {
        return Module.MODULE_NAME_OUTLINE;
    }

    public boolean loadModule() {
        if (this.mPanelHost == null) {
            this.mPanelHost = new PanelHostImpl(this.mContext);
        }
        this.mDisplay = new AppDisplay(this.mContext);
        initView();
        if (this.mPanelPopupWindow == null) {
            this.mPanelPopupWindow = new PopupWindow(this.mPanelHost.getContentView(), -1, -1, true);
            this.mPanelPopupWindow.setBackgroundDrawable(new ColorDrawable(ViewCompat.MEASURED_SIZE_MASK));
            this.mPanelPopupWindow.setAnimationStyle(R.style.View_Animation_LtoR);
            this.mPanelPopupWindow.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                }
            });
        }
        this.mOutlineSupport = new OutlineSupport(this.mContext, this.mPdfViewCtrl, this.mDisplay, this.mPanelPopupWindow, this.mBack) {
            public void OutlineBindingListView(BaseAdapter adapter) {
                OutlineModule.this.mListView.setAdapter(adapter);
            }

            public void getShowOutline(ArrayList<OutlineItem> mOutlineList) {
                OutlineModule.this.mOutlineArr.clear();
                OutlineModule.this.mOutlineArr.addAll(mOutlineList);
            }

            public void updateUI(int level, int state) {
                OutlineModule.this.mLevel = level;
                if (state == 2) {
                    OutlineModule.this.mNoInfoView.setText(OutlineModule.this.mContext.getResources().getString(R.string.rv_panel_outline_noinfo));
                } else if (state == 0) {
                    OutlineModule.this.mNoInfoView.setText(OutlineModule.this.mContext.getResources().getString(R.string.rv_panel_outline_noinfo));
                } else if (state == 1) {
                    OutlineModule.this.mNoInfoView.setText(OutlineModule.this.mContext.getResources().getString(R.string.rv_panel_annot_loading_start));
                }
                if (OutlineModule.this.mOutlineArr.size() > 0) {
                    OutlineModule.this.mContent.setVisibility(0);
                    if (OutlineModule.this.mLevel > 0) {
                        OutlineModule.this.mLlBack.setVisibility(0);
                        OutlineModule.this.mSeparate.setVisibility(0);
                    } else {
                        OutlineModule.this.mLlBack.setVisibility(8);
                        OutlineModule.this.mSeparate.setVisibility(8);
                    }
                    OutlineModule.this.mNoInfoView.setVisibility(8);
                    return;
                }
                OutlineModule.this.mContent.setVisibility(8);
                OutlineModule.this.mLlBack.setVisibility(8);
                OutlineModule.this.mSeparate.setVisibility(8);
                OutlineModule.this.mNoInfoView.setVisibility(0);
            }
        };
        this.mPdfViewCtrl.registerDocEventListener(this.mDocEventListener);
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).registerConfigurationChangedListener(this.mConfigurationChangedListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPanelHost.removeSpec(this);
        this.mPdfViewCtrl.unregisterDocEventListener(this.mDocEventListener);
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).unregisterConfigurationChangedListener(this.mConfigurationChangedListener);
        return true;
    }

    private void initView() {
        this.mOutlineTopBar = (FrameLayout) LayoutInflater.from(this.mContext).inflate(R.layout.panel_outline_topbar, null, false);
        this.mPanel_outline_topbar = (RelativeLayout) this.mOutlineTopBar.findViewById(R.id.panel_outline_topbar);
        this.mPanel_outline_topbar_close = (ImageView) this.mOutlineTopBar.findViewById(R.id.panel_outline_topbar_close);
        this.mPanel_outline_topbar_title = (TextView) this.mOutlineTopBar.findViewById(R.id.panel_outline_topbar_title);
        if (this.mDisplay.isPad()) {
            this.mPanel_outline_topbar_close.setVisibility(8);
            LayoutParams rl_topLayoutParams = (LayoutParams) this.mPanel_outline_topbar.getLayoutParams();
            rl_topLayoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_pad);
            this.mPanel_outline_topbar.setLayoutParams(rl_topLayoutParams);
            RelativeLayout.LayoutParams topCloseLayoutParams = (RelativeLayout.LayoutParams) this.mPanel_outline_topbar_close.getLayoutParams();
            topCloseLayoutParams.leftMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
            this.mPanel_outline_topbar_close.setLayoutParams(topCloseLayoutParams);
        } else {
            this.mPanel_outline_topbar_close.setVisibility(0);
            RelativeLayout.LayoutParams topTitleLayoutParams = (RelativeLayout.LayoutParams) this.mPanel_outline_topbar_title.getLayoutParams();
            topTitleLayoutParams.addRule(13, 0);
            topTitleLayoutParams.addRule(15);
            topTitleLayoutParams.leftMargin = this.mDisplay.dp2px(70.0f);
            this.mPanel_outline_topbar_title.setLayoutParams(topTitleLayoutParams);
        }
        this.mPanel_outline_topbar_close.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (OutlineModule.this.mPanelPopupWindow.isShowing()) {
                    OutlineModule.this.mPanelPopupWindow.dismiss();
                }
            }
        });
        this.mContentView = new RelativeLayout(this.mContext);
        this.mContentView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        this.mContentView.setBackgroundResource(R.color.ux_color_white);
        this.mContent = new LinearLayout(this.mContext);
        this.mContent.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        this.mContent.setOrientation(1);
        this.mLlBack = new LinearLayout(this.mContext);
        this.mLlBack.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        this.mLlBack.setBackgroundResource(R.drawable.panel_outline_item_bg);
        this.mLlBack.setGravity(16);
        this.mContent.addView(this.mLlBack);
        this.mBack = new ImageView(this.mContext);
        LinearLayout.LayoutParams backLayoutParams = new LinearLayout.LayoutParams(-1, -2);
        LinearLayout.LayoutParams backLlLayoutParams = (LinearLayout.LayoutParams) this.mLlBack.getLayoutParams();
        if (this.mDisplay.isPad()) {
            backLlLayoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad);
            backLayoutParams.leftMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
            backLayoutParams.rightMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
        } else {
            backLlLayoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone);
            backLayoutParams.leftMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
            backLayoutParams.rightMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
        }
        this.mLlBack.setLayoutParams(backLlLayoutParams);
        this.mBack.setLayoutParams(backLayoutParams);
        this.mBack.setImageResource(R.drawable.panel_outline_back_selector);
        this.mBack.setPadding(0, this.mDisplay.dp2px(5.0f), 0, this.mDisplay.dp2px(5.0f));
        this.mBack.setScaleType(ScaleType.FIT_START);
        this.mBack.setFocusable(false);
        this.mLlBack.addView(this.mBack);
        this.mSeparate = new ImageView(this.mContext);
        this.mSeparate.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
        this.mSeparate.setImageResource(R.color.ux_color_seperator_gray);
        this.mContent.addView(this.mSeparate);
        this.mListView = new ListView(this.mContext);
        this.mListView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        this.mListView.setCacheColorHint(this.mContext.getResources().getColor(R.color.ux_color_translucent));
        this.mListView.setDivider(this.mContext.getResources().getDrawable(R.color.ux_color_seperator_gray));
        this.mListView.setDividerHeight(1);
        this.mListView.setFastScrollEnabled(false);
        this.mContent.addView(this.mListView);
        this.mNoInfoView = new TextView(this.mContext);
        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(-1, -1);
        textViewParams.addRule(13);
        this.mNoInfoView.setLayoutParams(textViewParams);
        this.mNoInfoView.setGravity(17);
        this.mNoInfoView.setText(this.mContext.getResources().getString(R.string.rv_panel_outline_noinfo));
        this.mNoInfoView.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
        this.mNoInfoView.setTextSize(0, this.mContext.getResources().getDimension(R.dimen.ux_text_height_body2));
        this.mContentView.addView(this.mContent);
        this.mContentView.addView(this.mNoInfoView);
        this.mPanelHost.addSpec(this);
    }

    public int getTag() {
        return 1;
    }

    public int getIcon() {
        return R.drawable.panel_tabimg_outline_selector;
    }

    public View getTopToolbar() {
        return this.mOutlineTopBar;
    }

    public View getContentView() {
        return this.mContentView;
    }

    public void onActivated() {
        this.mOutlineSupport.updateUI(this.mLevel, this.mOutlineSupport.getCurrentState());
    }

    public void onDeactivated() {
    }
}
