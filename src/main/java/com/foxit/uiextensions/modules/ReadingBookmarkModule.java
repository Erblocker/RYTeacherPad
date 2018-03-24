package com.foxit.uiextensions.modules;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
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
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.UIExtensionsManager.ConfigurationChangedListener;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.panel.PanelHost;
import com.foxit.uiextensions.controls.panel.PanelSpec;
import com.foxit.uiextensions.controls.panel.impl.PanelHostImpl;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.modules.ReadingBookmarkSupport.ReadingBookmarkNode;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.OnPageEventListener;
import java.util.ArrayList;
import java.util.Iterator;

public class ReadingBookmarkModule implements Module, PanelSpec {
    private static final float RD_PANEL_WIDTH_SCALE_H = 0.338f;
    private static final float RD_PANEL_WIDTH_SCALE_V = 0.535f;
    private boolean isTouchHold;
    private View mClearView;
    private ConfigurationChangedListener mConfigurationChangedListener = new ConfigurationChangedListener() {
        public void onConfigurationChanged(Configuration newConfig) {
            if (ReadingBookmarkModule.this.mPanelPopupWindow != null && ReadingBookmarkModule.this.mPanelPopupWindow.isShowing() && ReadingBookmarkModule.this.mPanelHost != null && ReadingBookmarkModule.this.mPanelHost.getCurrentSpec() == ReadingBookmarkModule.this) {
                ReadingBookmarkModule.this.mPanelPopupWindow.dismiss();
                ReadingBookmarkModule.this.show();
            }
        }
    };
    protected View mContentView;
    private Context mContext;
    private UITextEditDialog mDialog;
    private AppDisplay mDisplay;
    private final IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
        }

        public void onDocOpened(PDFDoc pdfDoc, int errCode) {
            if (errCode == PDFError.NO_ERROR.getCode()) {
                ReadingBookmarkModule.this.prepareSupport();
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
    private Boolean mIsPad;
    private boolean mIsReadingBookmark = false;
    protected ArrayList<Boolean> mItemMoreViewShow;
    private ArrayList<BaseItem> mMarkItemList;
    private final IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPageChanged(int oldPageIndex, int curPageIndex) {
            ReadingBookmarkModule.this.remarkItemState(curPageIndex);
        }

        public void onPageMoved(boolean success, int index, int dstIndex) {
            ReadingBookmarkModule.this.mSupport.getAdapter().onPageMoved(success, index, dstIndex);
            ReadingBookmarkModule.this.remarkItemState(ReadingBookmarkModule.this.mPdfViewCtrl.getCurrentPage());
        }

        public void onPagesRemoved(boolean success, int[] pageIndexes) {
            for (int i = 0; i < pageIndexes.length; i++) {
                ReadingBookmarkModule.this.mSupport.getAdapter().onPageRemoved(success, pageIndexes[i] - i);
            }
            ReadingBookmarkModule.this.remarkItemState(ReadingBookmarkModule.this.mPdfViewCtrl.getCurrentPage());
        }

        public void onPagesInserted(boolean success, int dstIndex, int[] range) {
            ReadingBookmarkModule.this.mSupport.getAdapter().onPagesInsert(success, dstIndex, range);
            ReadingBookmarkModule.this.remarkItemState(ReadingBookmarkModule.this.mPdfViewCtrl.getCurrentPage());
        }
    };
    private PanelHost mPanelHost;
    private PopupWindow mPanelPopupWindow = null;
    private ViewGroup mParent;
    protected PDFViewCtrl mPdfViewCtrl;
    private ListView mReadingBookmarkListView;
    private TextView mReadingBookmarkNoInfoTv;
    private RelativeLayout mReadingMarkContent;
    private ReadingBookmarkSupport mSupport;
    private View mTopBarView;

    public ReadingBookmarkModule(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        if (context == null || pdfViewCtrl == null) {
            throw new NullPointerException();
        }
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mItemMoreViewShow = new ArrayList();
        this.mMarkItemList = new ArrayList();
        this.mDisplay = new AppDisplay(this.mContext);
        this.mIsPad = Boolean.valueOf(this.mDisplay.isPad());
        this.mParent = parent;
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

    public void changeMarkItemState(boolean mark) {
        this.mIsReadingBookmark = mark;
        Iterator it = this.mMarkItemList.iterator();
        while (it.hasNext()) {
            ((BaseItem) it.next()).setSelected(this.mIsReadingBookmark);
        }
    }

    public void addMarkedItem(BaseItem item) {
        if (!this.mMarkItemList.contains(item)) {
            this.mMarkItemList.add(item);
            item.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ReadingBookmarkModule.this.mIsReadingBookmark = !ReadingBookmarkModule.this.isMarked(ReadingBookmarkModule.this.mPdfViewCtrl.getCurrentPage());
                    if (ReadingBookmarkModule.this.mIsReadingBookmark) {
                        ReadingBookmarkModule.this.addMark(ReadingBookmarkModule.this.mPdfViewCtrl.getCurrentPage());
                    } else {
                        ReadingBookmarkModule.this.removeMark(ReadingBookmarkModule.this.mPdfViewCtrl.getCurrentPage());
                    }
                    ReadingBookmarkModule.this.changeMarkItemState(ReadingBookmarkModule.this.mIsReadingBookmark);
                }
            });
        }
    }

    public void removeMarkedItem(BaseItem item) {
        if (this.mMarkItemList.contains(item)) {
            item.setOnClickListener(null);
            this.mMarkItemList.remove(item);
        }
    }

    private void prepareSupport() {
        if (this.mSupport == null) {
            this.mSupport = new ReadingBookmarkSupport(this);
            this.mReadingBookmarkListView.setAdapter(this.mSupport.getAdapter());
        }
    }

    private void remarkItemState(final int index) {
        ((Activity) this.mContext).runOnUiThread(new Runnable() {
            public void run() {
                Iterator it = ReadingBookmarkModule.this.mMarkItemList.iterator();
                while (it.hasNext()) {
                    ((BaseItem) it.next()).setSelected(ReadingBookmarkModule.this.isMarked(index));
                }
            }
        });
    }

    public void changeViewState(boolean enable) {
        this.mClearView.setEnabled(enable);
        if (enable) {
            this.mReadingBookmarkNoInfoTv.setVisibility(8);
        } else {
            this.mReadingBookmarkNoInfoTv.setVisibility(0);
        }
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
        this.mPanelHost.setCurrentSpec(0);
        this.mPanelPopupWindow.showAtLocation(this.mPdfViewCtrl, 51, 0, 0);
    }

    public boolean loadModule() {
        if (this.mPanelHost == null) {
            this.mPanelHost = new PanelHostImpl(this.mContext);
        }
        this.mTopBarView = View.inflate(this.mContext, R.layout.panel_bookmark_topbar, null);
        View closeView = this.mTopBarView.findViewById(R.id.panel_bookmark_close);
        TextView topTitle = (TextView) this.mTopBarView.findViewById(R.id.panel_bookmark_title);
        this.mClearView = this.mTopBarView.findViewById(R.id.panel_bookmark_clear);
        if (this.mIsPad.booleanValue()) {
            closeView.setVisibility(8);
        } else {
            closeView.setVisibility(0);
            closeView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (ReadingBookmarkModule.this.mPanelPopupWindow.isShowing()) {
                        ReadingBookmarkModule.this.mPanelPopupWindow.dismiss();
                    }
                }
            });
        }
        View topNormalView = this.mTopBarView.findViewById(R.id.panel_bookmark_rl_top);
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
                ReadingBookmarkModule.this.mDialog = new UITextEditDialog(ReadingBookmarkModule.this.mContext);
                ReadingBookmarkModule.this.mDialog.setTitle(ReadingBookmarkModule.this.mContext.getResources().getString(R.string.hm_clear));
                ReadingBookmarkModule.this.mDialog.getPromptTextView().setText(ReadingBookmarkModule.this.mContext.getResources().getString(R.string.rd_panel_clear_readingbookmarks));
                ReadingBookmarkModule.this.mDialog.getInputEditText().setVisibility(8);
                ReadingBookmarkModule.this.mDialog.getOKButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ReadingBookmarkModule.this.mSupport.clearAllNodes();
                        ReadingBookmarkModule.this.changeViewState(false);
                        ReadingBookmarkModule.this.changeMarkItemState(false);
                        ReadingBookmarkModule.this.mDialog.dismiss();
                    }
                });
                ReadingBookmarkModule.this.mDialog.getCancelButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ReadingBookmarkModule.this.mDialog.dismiss();
                    }
                });
                ReadingBookmarkModule.this.mDialog.show();
            }
        });
        this.mContentView = LayoutInflater.from(this.mContext).inflate(R.layout.panel_bookmark_main, null);
        this.mReadingMarkContent = (RelativeLayout) this.mContentView.findViewById(R.id.panel_bookmark_content_root);
        this.mReadingBookmarkListView = (ListView) this.mReadingMarkContent.findViewById(R.id.panel_bookmark_lv);
        this.mReadingBookmarkNoInfoTv = (TextView) this.mReadingMarkContent.findViewById(R.id.panel_nobookmark_tv);
        if (this.mPanelPopupWindow == null) {
            this.mPanelPopupWindow = new PopupWindow(this.mPanelHost.getContentView(), -1, -1, true);
            this.mPanelPopupWindow.setBackgroundDrawable(new ColorDrawable(ViewCompat.MEASURED_SIZE_MASK));
            this.mPanelPopupWindow.setAnimationStyle(R.style.View_Animation_LtoR);
            this.mPanelPopupWindow.setOnDismissListener(new OnDismissListener() {
                public void onDismiss() {
                }
            });
        }
        this.mReadingBookmarkListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!AppUtil.isFastDoubleClick()) {
                    ReadingBookmarkModule.this.mPdfViewCtrl.gotoPage(((ReadingBookmarkNode) ReadingBookmarkModule.this.mSupport.getAdapter().getItem(position)).getIndex());
                    if (ReadingBookmarkModule.this.mPanelPopupWindow.isShowing()) {
                        ReadingBookmarkModule.this.mPanelPopupWindow.dismiss();
                    }
                }
            }
        });
        this.mReadingBookmarkListView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case 0:
                        boolean show = false;
                        int position = 0;
                        for (int i = 0; i < ReadingBookmarkModule.this.mSupport.getAdapter().getCount(); i++) {
                            if (((Boolean) ReadingBookmarkModule.this.mItemMoreViewShow.get(i)).booleanValue()) {
                                show = true;
                                position = i;
                                if (show) {
                                    ReadingBookmarkModule.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                                    ReadingBookmarkModule.this.mSupport.getAdapter().notifyDataSetChanged();
                                    ReadingBookmarkModule.this.isTouchHold = true;
                                    return true;
                                }
                            }
                        }
                        if (show) {
                            ReadingBookmarkModule.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                            ReadingBookmarkModule.this.mSupport.getAdapter().notifyDataSetChanged();
                            ReadingBookmarkModule.this.isTouchHold = true;
                            return true;
                        }
                        break;
                    case 1:
                    case 3:
                        break;
                }
                if (ReadingBookmarkModule.this.isTouchHold) {
                    ReadingBookmarkModule.this.isTouchHold = false;
                    return true;
                }
                return false;
            }
        });
        this.mPanelHost.addSpec(this);
        this.mPdfViewCtrl.registerDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.registerPageEventListener(this.mPageEventListener);
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).registerConfigurationChangedListener(this.mConfigurationChangedListener);
        return true;
    }

    public boolean unloadModule() {
        this.mPanelHost.removeSpec(this);
        this.mPdfViewCtrl.unregisterDocEventListener(this.mDocEventListener);
        this.mPdfViewCtrl.unregisterPageEventListener(this.mPageEventListener);
        ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).unregisterConfigurationChangedListener(this.mConfigurationChangedListener);
        return true;
    }

    public String getName() {
        return Module.MODULE_NAME_BOOKMARK;
    }

    public void onActivated() {
        changeViewState(this.mSupport.getAdapter().getCount() != 0);
        if (this.mSupport.needRelayout()) {
            this.mSupport.getAdapter().notifyDataSetChanged();
        }
    }

    public void onDeactivated() {
    }

    public View getTopToolbar() {
        return this.mTopBarView;
    }

    public int getIcon() {
        return R.drawable.panel_tabing_readingmark_selector;
    }

    public int getTag() {
        return 0;
    }

    public View getContentView() {
        return this.mContentView;
    }

    public boolean isMarked(int pageIndex) {
        return this.mSupport.getAdapter().isMarked(pageIndex);
    }

    public void addMark(int index) {
        this.mSupport.addReadingBookmarkNode(index, String.format("Page %d", new Object[]{Integer.valueOf(index + 1)}));
    }

    public void removeMark(int index) {
        this.mSupport.removeReadingBookmarkNode(index);
    }
}
