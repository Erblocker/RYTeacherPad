package com.foxit.uiextensions.modules;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.UIExtensionsManager.ConfigurationChangedListener;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.panel.PanelHost;
import com.foxit.uiextensions.controls.panel.PanelSpec;
import com.foxit.uiextensions.controls.panel.impl.PanelHostImpl;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.OnPageEventListener;
import java.util.ArrayList;

public class AnnotPanelModule implements Module, PanelSpec {
    private static final float RD_PANEL_WIDTH_SCALE_H = 0.338f;
    private static final float RD_PANEL_WIDTH_SCALE_V = 0.535f;
    private boolean isTouchHold;
    private AnnotPanel mAnnotPanel;
    private View mClearView;
    private ConfigurationChangedListener mConfigurationChangedListener = new ConfigurationChangedListener() {
        public void onConfigurationChanged(Configuration newConfig) {
            if (AnnotPanelModule.this.mPanelPopupWindow != null && AnnotPanelModule.this.mPanelPopupWindow.isShowing() && AnnotPanelModule.this.mPanelHost != null && AnnotPanelModule.this.mPanelHost.getCurrentSpec() == AnnotPanelModule.this) {
                AnnotPanelModule.this.mPanelPopupWindow.dismiss();
                AnnotPanelModule.this.show();
            }
        }
    };
    private View mContentView;
    private Context mContext;
    private UITextEditDialog mDialog;
    private AppDisplay mDisplay;
    private IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc document, int errCode) {
            if (errCode == 0) {
                AnnotPanelModule.this.mAnnotPanel.onDocOpened();
            }
        }

        public void onDocWillClose(PDFDoc document) {
            AnnotPanelModule.this.mAnnotPanel.onDocWillClose();
            AnnotPanelModule.this.mSearchingTextView.setVisibility(8);
        }

        public void onDocClosed(PDFDoc document, int errCode) {
            AnnotPanelModule.this.mSearchingTextView.setVisibility(8);
        }

        public void onDocWillSave(PDFDoc document) {
        }

        public void onDocSaved(PDFDoc document, int errCode) {
        }
    };
    private Boolean mIsPad;
    private ArrayList<Boolean> mItemMoreViewShow;
    private TextView mNoInfoView;
    private IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPagesRemoved(boolean success, int[] pageIndexes) {
            for (int i = 0; i < pageIndexes.length; i++) {
                AnnotPanelModule.this.mAnnotPanel.getAdapter().onPageRemoved(success, pageIndexes[i] - i);
            }
        }

        public void onPageMoved(boolean success, int index, int dstIndex) {
            AnnotPanelModule.this.mAnnotPanel.getAdapter().onPageMoved(success, index, dstIndex);
        }

        public void onPagesInserted(boolean success, int dstIndex, int[] range) {
            AnnotPanelModule.this.mAnnotPanel.getAdapter().onPagesInsert(success, dstIndex, range);
        }
    };
    private PanelHost mPanelHost;
    private PopupWindow mPanelPopupWindow = null;
    private ViewGroup mParent;
    private int mPausedPageIndex = 0;
    private PDFViewCtrl mPdfViewCtrl;
    private TextView mSearchingTextView;
    private View mTopBarView;

    public AnnotPanelModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        if (context == null || pdfViewCtrl == null) {
            throw new NullPointerException();
        }
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mItemMoreViewShow = new ArrayList();
        this.mDisplay = new AppDisplay(this.mContext);
        this.mIsPad = Boolean.valueOf(this.mDisplay.isPad());
    }

    public void setPanelHost(PanelHost panelHost) {
        this.mPanelHost = panelHost;
    }

    public PanelHost getPanelHost() {
        return this.mPanelHost;
    }

    public void setPopupWindow(PopupWindow window) {
        this.mPanelPopupWindow = window;
    }

    public PopupWindow getPopupWindow() {
        return this.mPanelPopupWindow;
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
        if (VERSION.SDK_INT >= 11) {
            this.mPanelPopupWindow.setSoftInputMode(48);
        }
        this.mPanelHost.setCurrentSpec(2);
        this.mPanelPopupWindow.showAtLocation(this.mPdfViewCtrl, 51, 0, 0);
    }

    public boolean loadModule() {
        if (this.mPanelHost == null) {
            this.mPanelHost = new PanelHostImpl(this.mContext);
        }
        this.mTopBarView = View.inflate(this.mContext, R.layout.panel_annot_topbar, null);
        View closeView = this.mTopBarView.findViewById(R.id.panel_annot_top_close_iv);
        TextView topTitle = (TextView) this.mTopBarView.findViewById(R.id.rv_panel_annot_title);
        this.mClearView = this.mTopBarView.findViewById(R.id.panel_annot_top_clear_tv);
        if (this.mIsPad.booleanValue()) {
            closeView.setVisibility(8);
        } else {
            closeView.setVisibility(0);
            closeView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (AnnotPanelModule.this.mPanelPopupWindow.isShowing()) {
                        AnnotPanelModule.this.mPanelPopupWindow.dismiss();
                    }
                }
            });
        }
        View topNormalView = this.mTopBarView.findViewById(R.id.panel_annot_top_normal);
        topNormalView.setVisibility(0);
        LayoutParams topNormalLayoutParams;
        RelativeLayout.LayoutParams topCloseLayoutParams;
        RelativeLayout.LayoutParams topClearLayoutParams;
        if (this.mIsPad.booleanValue()) {
            topNormalLayoutParams = (LayoutParams) topNormalView.getLayoutParams();
            topNormalLayoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_pad);
            topNormalView.setLayoutParams(topNormalLayoutParams);
            topCloseLayoutParams = (RelativeLayout.LayoutParams) closeView.getLayoutParams();
            topCloseLayoutParams.leftMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
            closeView.setLayoutParams(topCloseLayoutParams);
            topClearLayoutParams = (RelativeLayout.LayoutParams) this.mClearView.getLayoutParams();
            topClearLayoutParams.rightMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
            this.mClearView.setLayoutParams(topClearLayoutParams);
        } else {
            topNormalLayoutParams = (LayoutParams) topNormalView.getLayoutParams();
            topNormalLayoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_phone);
            topNormalView.setLayoutParams(topNormalLayoutParams);
            RelativeLayout.LayoutParams topTitleLayoutParams = (RelativeLayout.LayoutParams) topTitle.getLayoutParams();
            topTitleLayoutParams.addRule(13, 0);
            topTitleLayoutParams.addRule(15);
            topTitleLayoutParams.leftMargin = this.mDisplay.dp2px(70.0f);
            topTitle.setLayoutParams(topTitleLayoutParams);
            topCloseLayoutParams = (RelativeLayout.LayoutParams) closeView.getLayoutParams();
            topCloseLayoutParams.leftMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
            closeView.setLayoutParams(topCloseLayoutParams);
            topClearLayoutParams = (RelativeLayout.LayoutParams) this.mClearView.getLayoutParams();
            topClearLayoutParams.rightMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
            this.mClearView.setLayoutParams(topClearLayoutParams);
        }
        this.mClearView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AnnotPanelModule.this.mDialog = new UITextEditDialog(AnnotPanelModule.this.mContext);
                AnnotPanelModule.this.mDialog.setTitle(AnnotPanelModule.this.mContext.getResources().getString(R.string.hm_clear));
                AnnotPanelModule.this.mDialog.getPromptTextView().setText(AnnotPanelModule.this.mContext.getResources().getString(R.string.rd_panel_clear_comment));
                AnnotPanelModule.this.mDialog.getInputEditText().setVisibility(8);
                AnnotPanelModule.this.mDialog.getOKButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        AnnotPanelModule.this.mAnnotPanel.clearAllNodes();
                        AnnotPanelModule.this.mDialog.dismiss();
                    }
                });
                AnnotPanelModule.this.mDialog.getCancelButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        AnnotPanelModule.this.mDialog.dismiss();
                    }
                });
                AnnotPanelModule.this.mDialog.show();
            }
        });
        this.mContentView = View.inflate(this.mContext, R.layout.panel_annot_content, null);
        this.mNoInfoView = (TextView) this.mContentView.findViewById(R.id.rv_panel_annot_noinfo);
        this.mSearchingTextView = (TextView) this.mContentView.findViewById(R.id.rv_panel_annot_searching);
        ListView listView = (ListView) this.mContentView.findViewById(R.id.rv_panel_annot_list);
        if (this.mPanelPopupWindow == null) {
            this.mPanelPopupWindow = new PopupWindow(this.mPanelHost.getContentView(), -1, -1, true);
            this.mPanelPopupWindow.setBackgroundDrawable(new ColorDrawable(ViewCompat.MEASURED_SIZE_MASK));
            this.mPanelPopupWindow.setAnimationStyle(R.style.View_Animation_LtoR);
            this.mPanelPopupWindow.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                }
            });
        }
        this.mAnnotPanel = new AnnotPanel(this, this.mContext, this.mPdfViewCtrl, this.mParent, this.mContentView, this.mItemMoreViewShow);
        listView.setAdapter(this.mAnnotPanel.getAdapter());
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!AppUtil.isFastDoubleClick() && AnnotPanelModule.this.mAnnotPanel.jumpToPage(position) && AnnotPanelModule.this.mPanelPopupWindow.isShowing()) {
                    AnnotPanelModule.this.mPanelPopupWindow.dismiss();
                }
            }
        });
        listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case 0:
                        boolean show = false;
                        int position = 0;
                        for (int i = 0; i < AnnotPanelModule.this.mAnnotPanel.getAdapter().getCount(); i++) {
                            if (((Boolean) AnnotPanelModule.this.mItemMoreViewShow.get(i)).booleanValue()) {
                                show = true;
                                position = i;
                                if (show) {
                                    AnnotPanelModule.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                                    AnnotPanelModule.this.mAnnotPanel.getAdapter().notifyDataSetChanged();
                                    AnnotPanelModule.this.isTouchHold = true;
                                    return true;
                                }
                            }
                        }
                        if (show) {
                            AnnotPanelModule.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                            AnnotPanelModule.this.mAnnotPanel.getAdapter().notifyDataSetChanged();
                            AnnotPanelModule.this.isTouchHold = true;
                            return true;
                        }
                        break;
                    case 1:
                    case 3:
                        break;
                }
                if (AnnotPanelModule.this.isTouchHold) {
                    AnnotPanelModule.this.isTouchHold = false;
                    return true;
                }
                return false;
            }
        });
        this.mPanelHost.addSpec(this);
        this.mPdfViewCtrl.registerDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.registerPageEventListener(this.mPageEventListener);
        DocumentManager.getInstance(this.mPdfViewCtrl).registerAnnotEventListener(this.mAnnotPanel.getAnnotEventListener());
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).registerConfigurationChangedListener(this.mConfigurationChangedListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPanelHost.removeSpec(this);
        this.mPdfViewCtrl.unregisterPageEventListener(this.mPageEventListener);
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).unregisterConfigurationChangedListener(this.mConfigurationChangedListener);
        return true;
    }

    public String getName() {
        return Module.MODULE_NAME_ANNOTPANEL;
    }

    private void resetClearButton() {
        if (!DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot() || this.mAnnotPanel.getCurrentStatus() == 1 || this.mAnnotPanel.getCurrentStatus() == 3) {
            this.mClearView.setEnabled(false);
            return;
        }
        this.mClearView.setVisibility(0);
        if (this.mAnnotPanel.getCount() > 0) {
            this.mClearView.setEnabled(true);
        } else {
            this.mClearView.setEnabled(false);
        }
    }

    public void pauseSearch(int pageIndex) {
        this.mPausedPageIndex = pageIndex;
    }

    public void showNoAnnotsView() {
        this.mContext.runOnUiThread(new Runnable() {
            public void run() {
                AnnotPanelModule.this.mClearView.setEnabled(false);
                AnnotPanelModule.this.mNoInfoView.setText(AppResource.getString(AnnotPanelModule.this.mContext, R.string.rv_panel_annot_noinformation));
                AnnotPanelModule.this.mNoInfoView.setVisibility(0);
            }
        });
    }

    public void hideNoAnnotsView() {
        this.mContext.runOnUiThread(new Runnable() {
            public void run() {
                if (AnnotPanelModule.this.mPdfViewCtrl.getDoc() != null && DocumentManager.getInstance(AnnotPanelModule.this.mPdfViewCtrl).canAddAnnot()) {
                    AnnotPanelModule.this.mClearView.setEnabled(true);
                }
                if (AnnotPanelModule.this.mNoInfoView.getVisibility() != 8) {
                    AnnotPanelModule.this.mNoInfoView.setVisibility(8);
                }
            }
        });
    }

    public void updateLoadedPage(int curPageIndex, int total) {
        if (this.mAnnotPanel.getCurrentStatus() == 4) {
            if (curPageIndex == 0 && total == 0) {
                this.mSearchingTextView.setVisibility(8);
            }
            if (this.mSearchingTextView.isShown()) {
                this.mSearchingTextView.setVisibility(8);
            }
            this.mNoInfoView.setText(this.mContext.getResources().getString(R.string.rv_panel_annot_noinformation));
            if (this.mAnnotPanel.getCount() > 0) {
                if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                    this.mClearView.setEnabled(true);
                }
                this.mNoInfoView.setVisibility(8);
                return;
            }
            this.mClearView.setEnabled(false);
            this.mNoInfoView.setVisibility(0);
        } else if (this.mAnnotPanel.getCurrentStatus() == 5) {
            this.mSearchingTextView.setVisibility(8);
            if (this.mAnnotPanel.getCount() == 0) {
                this.mNoInfoView.setText(this.mContext.getResources().getString(R.string.rv_panel_annot_noinformation));
                this.mNoInfoView.setVisibility(0);
                this.mClearView.setEnabled(false);
                return;
            }
            this.mClearView.setEnabled(true);
        } else {
            this.mNoInfoView.setVisibility(8);
            if (!this.mSearchingTextView.isShown()) {
                this.mSearchingTextView.setVisibility(0);
            }
            this.mSearchingTextView.setText(new StringBuilder(String.valueOf(AppResource.getString(this.mContext, R.string.rv_panel_annot_item_pagenum))).append(": ").append(curPageIndex).append(" / ").append(total).toString());
        }
    }

    public int getTag() {
        return 2;
    }

    public int getIcon() {
        return R.drawable.panel_tabing_annotation_selector;
    }

    public View getTopToolbar() {
        return this.mTopBarView;
    }

    public View getContentView() {
        return this.mContentView;
    }

    public void onActivated() {
        resetClearButton();
        switch (this.mAnnotPanel.getCurrentStatus()) {
            case 1:
                this.mAnnotPanel.setStatusPause(false);
                break;
            case 2:
                this.mNoInfoView.setText(this.mContext.getResources().getString(R.string.rv_panel_annot_loading_start));
                this.mNoInfoView.setVisibility(0);
                this.mAnnotPanel.startSearch(0);
                break;
            case 3:
                this.mAnnotPanel.setStatusPause(false);
                this.mAnnotPanel.startSearch(this.mPausedPageIndex);
                break;
            case 4:
                if (this.mSearchingTextView.getVisibility() != 8) {
                    this.mSearchingTextView.setVisibility(8);
                }
                if (this.mAnnotPanel.getCount() <= 0) {
                    this.mNoInfoView.setVisibility(0);
                    break;
                } else {
                    this.mNoInfoView.setVisibility(8);
                    break;
                }
        }
        if (this.mAnnotPanel.getAdapter().hasDataChanged()) {
            if (this.mAnnotPanel.getAdapter().isEmpty()) {
                showNoAnnotsView();
            }
            this.mAnnotPanel.getAdapter().notifyDataSetChanged();
            this.mAnnotPanel.getAdapter().resetDataChanged();
        }
    }

    public void onDeactivated() {
        if (this.mAnnotPanel.getCurrentStatus() == 1) {
            this.mAnnotPanel.setStatusPause(true);
        }
    }
}
