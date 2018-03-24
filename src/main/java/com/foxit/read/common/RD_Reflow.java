package com.foxit.read.common;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.read.IRD_StateChangeListener;
import com.foxit.read.RD_Read;
import com.foxit.sdk.PDFViewCtrl.IDocEventListener;
import com.foxit.sdk.PDFViewCtrl.IPageEventListener;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.BottomBarImpl;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;
import com.foxit.uiextensions.modules.AnnotPanelModule;
import com.foxit.uiextensions.modules.PageNavigationModule;
import com.foxit.uiextensions.modules.ReadingBookmarkModule;
import com.foxit.uiextensions.textselect.TextSelectModule;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.OnPageEventListener;
import com.foxit.view.propertybar.IML_MultiLineBar;
import com.foxit.view.propertybar.IML_MultiLineBar.IML_ValueChangeListener;

public class RD_Reflow implements Module {
    private final float MAX_ZOOM = 8.0f;
    private final float MIN_ZOOM = 1.0f;
    private BaseItem mBackItem;
    private BaseItem mBookmarkItem;
    private Context mContext;
    private IDocEventListener mDocEventListener = new IDocEventListener() {
        public void onDocWillOpen() {
            RD_Reflow.this.initReflowBar();
        }

        public void onDocOpened(PDFDoc document, int errCode) {
            if (errCode == 0) {
                RD_Reflow.this.addBar();
                RD_Reflow.this.initValue();
                RD_Reflow.this.initMLBarValue();
                RD_Reflow.this.applyValue();
                RD_Reflow.this.registerMLListener();
                RD_Reflow.this.mRead.getDocViewer().registerPageEventListener(RD_Reflow.this.mPageEventListener);
                Module bookmarkModule = App.instance().getModuleByName(Module.MODULE_NAME_BOOKMARK);
                if (bookmarkModule != null && (bookmarkModule instanceof ReadingBookmarkModule)) {
                    ((ReadingBookmarkModule) bookmarkModule).addMarkedItem(RD_Reflow.this.mBookmarkItem);
                }
                RD_Reflow.this.onStatusChanged(RD_Reflow.this.mRead.getState(), RD_Reflow.this.mRead.getState());
            }
        }

        public void onDocWillClose(PDFDoc document) {
        }

        public void onDocClosed(PDFDoc document, int errCode) {
            RD_Reflow.this.removeBar();
            RD_Reflow.this.mRead.getDocViewer().unregisterPageEventListener(RD_Reflow.this.mPageEventListener);
            Module bookmarkModule = App.instance().getModuleByName(Module.MODULE_NAME_BOOKMARK);
            if (bookmarkModule != null && (bookmarkModule instanceof ReadingBookmarkModule)) {
                ((ReadingBookmarkModule) bookmarkModule).removeMarkedItem(RD_Reflow.this.mBookmarkItem);
            }
            RD_Reflow.this.unRegisterMLListener();
        }

        public void onDocWillSave(PDFDoc document) {
        }

        public void onDocSaved(PDFDoc document, int errCode) {
        }
    };
    private boolean mIsReflow;
    private BaseItem mListItem;
    private BaseItem mNextPageItem;
    private IPageEventListener mPageEventListener = new OnPageEventListener() {
        public void onPageChanged(int oldPageIndex, int curPageIndex) {
            RD_Reflow.this.resetNextPageItem();
            RD_Reflow.this.resetPrePageItem();
        }
    };
    private BaseItem mPicItem;
    private BaseItem mPrePageItem;
    private int mPreReflowMode;
    private int mPreViewMode;
    private RD_Read mRead;
    private BaseBar mReflowBottomBar;
    private IML_ValueChangeListener mReflowChangeListener = new IML_ValueChangeListener() {
        public void onValueChanged(int type, Object value) {
            if (type == 7) {
                RD_Reflow.this.mIsReflow = ((Boolean) value).booleanValue();
                int curLayout = RD_Reflow.this.mRead.getDocViewer().getPageLayoutMode();
                int curReflowMode = RD_Reflow.this.mRead.getDocViewer().getReflowMode();
                if (curLayout != 3) {
                    if (RD_Reflow.this.mRead.getDocMgr().getCurrentAnnot() != null) {
                        RD_Reflow.this.mRead.getDocMgr().setCurrentAnnot(null);
                    }
                    TextSelectModule module = (TextSelectModule) ((UIExtensionsManager) RD_Reflow.this.mRead.getDocViewer().getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_SELECTION);
                    if (module != null) {
                        module.triggerDismissMenu();
                    }
                    RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(3);
                    RD_Reflow.this.mRead.getDocViewer().setReflowMode(RD_Reflow.this.mPreReflowMode);
                    RD_Reflow.this.mRead.changeState(2);
                    RD_Reflow.this.mRead.getMainFrame().hideSettingBar();
                } else {
                    if (RD_Reflow.this.mPreViewMode == 3) {
                        RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(1);
                    } else {
                        RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(RD_Reflow.this.mPreViewMode);
                    }
                    RD_Reflow.this.mRead.changeState(1);
                }
                RD_Reflow.this.mPreViewMode = curLayout;
                RD_Reflow.this.mPreReflowMode = curReflowMode;
                ((PageNavigationModule) ((UIExtensionsManager) RD_Reflow.this.mRead.getDocViewer().getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV)).resetJumpView();
            }
        }

        public void onDismiss() {
        }

        public int getType() {
            return 7;
        }
    };
    private BaseBar mReflowTopBar;
    private float mScale = 1.0f;
    private IML_MultiLineBar mSettingBar;
    private IRD_StateChangeListener mStatusChangeListener = new IRD_StateChangeListener() {
        public void onStateChanged(int oldState, int newState) {
            int curLayout = RD_Reflow.this.mRead.getDocViewer().getPageLayoutMode();
            int curReflowMode = RD_Reflow.this.mRead.getDocViewer().getReflowMode();
            try {
                if (RD_Reflow.this.mRead.getState() == 2) {
                    if (curLayout != 3) {
                        RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(3);
                        RD_Reflow.this.mRead.getDocViewer().setReflowMode(RD_Reflow.this.mPreReflowMode);
                        if (RD_Reflow.this.mRead.getDocMgr().getCurrentAnnot() != null) {
                            RD_Reflow.this.mRead.getDocMgr().setCurrentAnnot(null);
                        }
                        RD_Reflow.this.mRead.getMainFrame().hideSettingBar();
                        RD_Reflow.this.mPreViewMode = curLayout;
                        RD_Reflow.this.mPreReflowMode = curReflowMode;
                        ((PageNavigationModule) App.instance().getModuleByName(Module.MODULE_NAME_PAGENAV)).resetJumpView();
                    }
                } else if (curLayout == 3) {
                    if (RD_Reflow.this.mPreViewMode == 3) {
                        RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(1);
                    } else {
                        RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(RD_Reflow.this.mPreViewMode);
                    }
                    RD_Reflow.this.mPreViewMode = curLayout;
                    RD_Reflow.this.mPreReflowMode = curReflowMode;
                    ((PageNavigationModule) App.instance().getModuleByName(Module.MODULE_NAME_PAGENAV)).resetJumpView();
                }
            } catch (Exception e) {
            }
            if (RD_Reflow.this.mRead.getDocViewer().getPageLayoutMode() == 3) {
                RD_Reflow.this.mSettingBar.setProperty(7, Boolean.valueOf(true));
                RD_Reflow.this.mIsReflow = true;
            } else {
                RD_Reflow.this.mSettingBar.setProperty(7, Boolean.valueOf(false));
                RD_Reflow.this.mIsReflow = false;
            }
            RD_Reflow.this.onStatusChanged(oldState, newState);
        }
    };
    private BaseItem mTitleItem;
    private BaseItem mZoomInItem;
    private BaseItem mZoomOutItem;

    class ReflowBottomBar extends BottomBarImpl {
        public ReflowBottomBar(Context context) {
            super(context);
        }
    }

    public RD_Reflow(Context context, RD_Read read) {
        this.mRead = read;
        this.mContext = context;
    }

    public boolean loadModule() {
        this.mSettingBar = this.mRead.getMainFrame().getSettingBar();
        this.mRead.getDocViewer().registerDocEventListener(this.mDocEventListener);
        this.mRead.registerStateChangeListener(this.mStatusChangeListener);
        return true;
    }

    private void addBar() {
        LayoutParams reflowTopLp = new LayoutParams(-2, -2);
        reflowTopLp.addRule(10);
        this.mRead.getMainFrame().getContentView().addView(this.mReflowTopBar.getContentView(), reflowTopLp);
        LayoutParams reflowBottomLp = new LayoutParams(-2, -2);
        reflowBottomLp.addRule(12);
        this.mRead.getMainFrame().getContentView().addView(this.mReflowBottomBar.getContentView(), reflowBottomLp);
        this.mReflowTopBar.getContentView().setVisibility(4);
        this.mReflowBottomBar.getContentView().setVisibility(4);
    }

    private void removeBar() {
        this.mRead.getMainFrame().getContentView().removeView(this.mReflowBottomBar.getContentView());
        this.mRead.getMainFrame().getContentView().removeView(this.mReflowTopBar.getContentView());
    }

    private void initReflowBar() {
        this.mReflowTopBar = new TopBarImpl(this.mContext);
        this.mReflowBottomBar = new ReflowBottomBar(this.mContext);
        this.mReflowBottomBar.setInterval(true);
        this.mBackItem = new BaseItemImpl(this.mContext);
        this.mTitleItem = new BaseItemImpl(this.mContext);
        this.mBookmarkItem = new BaseItemImpl(this.mContext);
        this.mPicItem = new BaseItemImpl(this.mContext);
        this.mZoomOutItem = new BaseItemImpl(this.mContext);
        this.mZoomInItem = new BaseItemImpl(this.mContext);
        this.mPrePageItem = new BaseItemImpl(this.mContext);
        this.mNextPageItem = new BaseItemImpl(this.mContext);
        this.mListItem = new BaseItemImpl(this.mContext);
        initItemsImgRes();
        initItemsOnClickListener();
        this.mReflowBottomBar.addView(this.mListItem, TB_Position.Position_CENTER);
        this.mReflowBottomBar.addView(this.mZoomInItem, TB_Position.Position_CENTER);
        this.mReflowBottomBar.addView(this.mZoomOutItem, TB_Position.Position_CENTER);
        this.mReflowBottomBar.addView(this.mPicItem, TB_Position.Position_CENTER);
        this.mReflowBottomBar.addView(this.mPrePageItem, TB_Position.Position_CENTER);
        this.mReflowBottomBar.addView(this.mNextPageItem, TB_Position.Position_CENTER);
        this.mReflowTopBar.addView(this.mBackItem, TB_Position.Position_LT);
        this.mReflowTopBar.addView(this.mTitleItem, TB_Position.Position_LT);
        this.mReflowTopBar.addView(this.mBookmarkItem, TB_Position.Position_RB);
        this.mReflowTopBar.setBackgroundColor(App.instance().getApplicationContext().getResources().getColor(R.color.ux_bg_color_toolbar_colour));
        this.mReflowBottomBar.setBackgroundColor(App.instance().getApplicationContext().getResources().getColor(R.color.ux_bg_color_toolbar_light));
    }

    private void initItemsImgRes() {
        this.mPicItem.setImageResource(R.drawable.rd_reflow_no_picture_selector);
        this.mZoomOutItem.setImageResource(R.drawable.rd_reflow_zoomout_selecter);
        this.mZoomInItem.setImageResource(R.drawable.rd_reflow_zoomin_selecter);
        this.mPrePageItem.setImageResource(R.drawable.rd_reflow_previous_selecter);
        this.mNextPageItem.setImageResource(R.drawable.rd_reflow_next_selecter);
        this.mListItem.setImageResource(R.drawable.rd_reflow_list_selecter);
        this.mBackItem.setImageResource(R.drawable.cloud_back);
        this.mTitleItem.setText(AppResource.getString(this.mContext, R.string.rd_reflow_topbar_title));
        this.mTitleItem.setTextSize(App.instance().getDisplay().px2dp((float) App.instance().getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.ux_text_height_title)));
        this.mTitleItem.setTextColorResource(R.color.ux_text_color_title_light);
        this.mBookmarkItem.setImageResource(R.drawable.bookmark_topbar_blue_add_selector);
    }

    private void resetPicItem() {
        if ((this.mRead.getDocViewer().getReflowMode() & 1) == 1) {
            this.mPicItem.setImageResource(R.drawable.rd_reflow_picture_selector);
        } else {
            this.mPicItem.setImageResource(R.drawable.rd_reflow_no_picture_selector);
        }
    }

    private void resetZoomOutItem() {
        if (isMinZoomScale()) {
            this.mZoomOutItem.setEnable(false);
            this.mZoomOutItem.setImageResource(R.drawable.rd_reflow_zoomout_pressed);
            return;
        }
        this.mZoomOutItem.setEnable(true);
        this.mZoomOutItem.setImageResource(R.drawable.rd_reflow_zoomout_selecter);
    }

    private void resetZoomInItem() {
        if (isMaxZoomScale()) {
            this.mZoomInItem.setEnable(false);
            this.mZoomInItem.setImageResource(R.drawable.rd_reflow_zoomin_pressed);
            return;
        }
        this.mZoomInItem.setEnable(true);
        this.mZoomInItem.setImageResource(R.drawable.rd_reflow_zoomin_selecter);
    }

    private void resetPrePageItem() {
        if (this.mRead.getDocViewer().getCurrentPage() == 0) {
            this.mPrePageItem.setImageResource(R.drawable.rd_reflow_left_pressed);
        } else {
            this.mPrePageItem.setImageResource(R.drawable.rd_reflow_previous_selecter);
        }
    }

    private void resetNextPageItem() {
        if (this.mRead.getDocViewer().getCurrentPage() + 1 == this.mRead.getDocViewer().getPageCount()) {
            this.mNextPageItem.setImageResource(R.drawable.rd_reflow_right_pressed);
        } else {
            this.mNextPageItem.setImageResource(R.drawable.rd_reflow_next_selecter);
        }
    }

    private void initItemsOnClickListener() {
        this.mPicItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if ((RD_Reflow.this.mRead.getDocViewer().getReflowMode() & 1) == 1) {
                    RD_Reflow.this.mRead.getDocViewer().setReflowMode(0);
                    RD_Reflow.this.mPicItem.setImageResource(R.drawable.rd_reflow_no_picture_selector);
                    return;
                }
                RD_Reflow.this.mRead.getDocViewer().setReflowMode(1);
                RD_Reflow.this.mPicItem.setImageResource(R.drawable.rd_reflow_picture_selector);
            }
        });
        this.mZoomOutItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RD_Reflow.this.isMinZoomScale()) {
                    RD_Reflow.this.mZoomOutItem.setEnable(false);
                    RD_Reflow.this.mZoomOutItem.setImageResource(R.drawable.rd_reflow_zoomout_pressed);
                    return;
                }
                RD_Reflow.this.mScale = Math.max(1.0f, RD_Reflow.this.mScale * 0.8f);
                RD_Reflow.this.mRead.getDocViewer().setZoom(RD_Reflow.this.mScale);
                RD_Reflow.this.resetZoomInItem();
                RD_Reflow.this.resetZoomOutItem();
            }
        });
        this.mZoomInItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RD_Reflow.this.isMaxZoomScale()) {
                    RD_Reflow.this.mZoomInItem.setEnable(false);
                    RD_Reflow.this.mZoomInItem.setImageResource(R.drawable.rd_reflow_zoomin_pressed);
                    return;
                }
                RD_Reflow.this.mScale = Math.min(8.0f, RD_Reflow.this.mScale * 1.25f);
                RD_Reflow.this.mRead.getDocViewer().setZoom(RD_Reflow.this.mScale);
                RD_Reflow.this.resetZoomInItem();
                RD_Reflow.this.resetZoomOutItem();
            }
        });
        this.mPrePageItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RD_Reflow.this.mRead.getDocViewer().getCurrentPage() - 1 >= 0) {
                    RD_Reflow.this.mRead.getDocViewer().gotoPrevPage();
                }
            }
        });
        this.mNextPageItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RD_Reflow.this.mRead.getDocViewer().getCurrentPage() + 1 < RD_Reflow.this.mRead.getDocViewer().getPageCount()) {
                    RD_Reflow.this.mRead.getDocViewer().gotoNextPage();
                }
            }
        });
        this.mBackItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int curLayout = RD_Reflow.this.mRead.getDocViewer().getPageLayoutMode();
                int curReflowMode = RD_Reflow.this.mRead.getDocViewer().getReflowMode();
                if (RD_Reflow.this.mPreViewMode == 3) {
                    RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(1);
                } else {
                    RD_Reflow.this.mRead.getDocViewer().setPageLayoutMode(RD_Reflow.this.mPreViewMode);
                }
                RD_Reflow.this.mRead.changeState(1);
                RD_Reflow.this.mPreViewMode = curLayout;
                RD_Reflow.this.mPreReflowMode = curReflowMode;
                RD_Reflow.this.mReflowBottomBar.getContentView().setVisibility(4);
                RD_Reflow.this.mReflowTopBar.getContentView().setVisibility(4);
                try {
                    RD_Reflow.this.mRead.getMainFrame().showToolbars();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ((PageNavigationModule) ((UIExtensionsManager) RD_Reflow.this.mRead.getDocViewer().getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PAGENAV)).resetJumpView();
            }
        });
        this.mListItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RD_Reflow.this.mRead.getMainFrame().showPanel();
            }
        });
    }

    private void initValue() {
        if (this.mRead.getDocViewer().getPageLayoutMode() == 3) {
            this.mIsReflow = true;
        } else {
            this.mIsReflow = false;
        }
        this.mPreViewMode = this.mRead.getDocViewer().getPageLayoutMode();
        this.mPreReflowMode = this.mRead.getDocViewer().getReflowMode();
    }

    private void initMLBarValue() {
        this.mSettingBar = this.mRead.getMainFrame().getSettingBar();
        this.mSettingBar.setProperty(7, Boolean.valueOf(this.mIsReflow));
    }

    private void applyValue() {
        if (this.mIsReflow) {
            this.mRead.changeState(2);
        }
    }

    private boolean isMaxZoomScale() {
        return this.mScale >= 8.0f;
    }

    private boolean isMinZoomScale() {
        return this.mScale <= 1.0f;
    }

    private void resetAnnotPanelView(boolean showAnnotPanel) {
        AnnotPanelModule annotPanelModule = (AnnotPanelModule) App.instance().getModuleByName(Module.MODULE_NAME_ANNOTPANEL);
        if (this.mRead.getMainFrame().getPanel() != null && annotPanelModule != null) {
            if (showAnnotPanel) {
                this.mRead.getMainFrame().getPanel().addSpec(annotPanelModule);
            } else {
                this.mRead.getMainFrame().getPanel().removeSpec(annotPanelModule);
            }
        }
    }

    private void registerMLListener() {
        this.mSettingBar.registerListener(this.mReflowChangeListener);
    }

    private void unRegisterMLListener() {
        this.mSettingBar.unRegisterListener(this.mReflowChangeListener);
    }

    private void onStatusChanged(int oldState, int newState) {
        if (this.mRead.getDocViewer().getDoc() != null) {
            if (this.mRead.getDocViewer().getPageLayoutMode() == 3) {
                if (this.mRead.getMainFrame().isToolbarsVisible()) {
                    this.mReflowBottomBar.getContentView().setVisibility(0);
                    this.mReflowTopBar.getContentView().setVisibility(0);
                } else {
                    this.mReflowBottomBar.getContentView().setVisibility(4);
                    this.mReflowTopBar.getContentView().setVisibility(4);
                }
                this.mScale = this.mRead.getDocViewer().getZoom();
                resetPicItem();
                resetZoomInItem();
                resetZoomOutItem();
                resetNextPageItem();
                resetPrePageItem();
                resetAnnotPanelView(false);
                return;
            }
            this.mReflowBottomBar.getContentView().setVisibility(4);
            this.mReflowTopBar.getContentView().setVisibility(4);
            resetAnnotPanelView(true);
        }
    }

    public boolean unloadModule() {
        this.mRead.getDocViewer().unregisterDocEventListener(this.mDocEventListener);
        return true;
    }

    public String getName() {
        return "Reflow";
    }
}
