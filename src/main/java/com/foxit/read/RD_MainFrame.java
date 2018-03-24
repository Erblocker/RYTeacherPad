package com.foxit.read;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.read.common.RD_ScreenLock;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.line.LineConstants;
import com.foxit.uiextensions.controls.panel.PanelHost;
import com.foxit.uiextensions.controls.panel.impl.PanelHostImpl;
import com.foxit.uiextensions.controls.propertybar.PropertyBar;
import com.foxit.uiextensions.controls.propertybar.imp.PropertyBarImpl;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.ToolbarItemConfig;
import com.foxit.uiextensions.controls.toolbar.impl.BaseBarImpl;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.BottomBarImpl;
import com.foxit.uiextensions.controls.toolbar.impl.CircleItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;
import com.foxit.uiextensions.modules.ThumbnailModule;
import com.foxit.uiextensions.modules.signature.SignatureModule;
import com.foxit.uiextensions.modules.signature.SignatureToolHandler;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.view.menu.MoreMenuModule;
import com.foxit.view.menu.MoreMenuView;
import com.foxit.view.propertybar.IML_MultiLineBar;
import com.foxit.view.propertybar.IML_MultiLineBar.IML_ValueChangeListener;
import com.foxit.view.propertybar.IMT_MoreTools;
import com.foxit.view.propertybar.imp.ML_MultiLineBar;
import com.foxit.view.propertybar.imp.MT_MoreTools;
import com.foxit.view.toolbar.imp.TB_AnnotBar;
import com.foxit.view.toolbar.imp.TB_AnnotsBar;
import com.foxit.view.toolbar.imp.TB_EditDoneBar;
import java.util.ArrayList;
import java.util.Iterator;

public class RD_MainFrame implements IRD_MainFrame {
    private int HIDE_ANIMATION_TAG = 101;
    private int SHOW_ANIMATION_TAG = 100;
    private BaseBarImpl mAnnotCustomBottomBar;
    private ViewGroup mAnnotCustomBottomBarLayout;
    private BaseBarImpl mAnnotCustomTopBar;
    private ViewGroup mAnnotCustomTopBarLayout;
    CircleItemImpl mAnnotDoneBtn = null;
    private Activity mAttachedActivity;
    BaseItem mBackItem;
    private BaseBarImpl mBottomBar;
    private AnimationSet mBottomBarHideAnim;
    private ViewGroup mBottomBarLayout;
    private AnimationSet mBottomBarShowAnim;
    private boolean mCloseAttachedActivity = true;
    private Context mContext;
    private ViewGroup mDocViewerLayout;
    private BaseBarImpl mEditBar;
    private ViewGroup mEditBarLayout;
    CircleItemImpl mEditBtn = null;
    private BaseBarImpl mEditDoneBar;
    private ViewGroup mEditDoneBarLayout;
    private boolean mIsFullScreen;
    private boolean mIsPhone;
    private AnimationSet mMaskHideAnim;
    private AnimationSet mMaskShowAnim;
    private ViewGroup mMaskView;
    BaseItemImpl mMenuBtn = null;
    private MoreMenuModule mMoreMenuModule;
    private MT_MoreTools mMoreToolsBar;
    private PanelHost mPanel;
    CircleItemImpl mPanelBtn = null;
    private PopupWindow mPanelPopupWindow;
    private PropertyBar mPropertyBar;
    private IRD_Read mRead;
    CircleItemImpl mReadSignItem = null;
    private ViewGroup mRootView;
    private ML_MultiLineBar mSettingBar;
    CircleItemImpl mSettingBtn = null;
    private PopupWindow mSettingPopupWindow;
    private BaseItem mSignListItem;
    IML_ValueChangeListener mSingleChangeListener = new IML_ValueChangeListener() {
        public void onValueChanged(int type, Object value) {
            if (4 == type) {
                RD_MainFrame.this.mSinglePage = ((Boolean) value).booleanValue();
                RD_MainFrame.this.mSettingBar.setProperty(4, Boolean.valueOf(RD_MainFrame.this.mSinglePage));
                RD_MainFrame.this.applyValue();
            }
        }

        public void onDismiss() {
        }

        public int getType() {
            return 4;
        }
    };
    private boolean mSinglePage = true;
    private IRD_StateChangeListener mStateChangeListener = new IRD_StateChangeListener() {
        public void onStateChanged(int oldState, int newState) {
            RD_MainFrame.this.initAnimations();
            if (DocumentManager.getInstance(RD_MainFrame.this.mRead.getDocViewer()).canAddAnnot()) {
                RD_MainFrame.this.mEditBtn.setEnable(true);
            } else {
                RD_MainFrame.this.mEditBtn.setEnable(false);
            }
            if (DocumentManager.getInstance(RD_MainFrame.this.mRead.getDocViewer()).canAddAnnot() && DocumentManager.getInstance(RD_MainFrame.this.mRead.getDocViewer()).canSigning()) {
                if (RD_MainFrame.this.mReadSignItem != null) {
                    RD_MainFrame.this.mReadSignItem.setEnable(true);
                }
            } else if (RD_MainFrame.this.mReadSignItem != null) {
                RD_MainFrame.this.mReadSignItem.setEnable(false);
            }
            ArrayList<View> currentShowViews = new ArrayList();
            ArrayList<View> willShowViews = new ArrayList();
            Iterator it = RD_MainFrame.this.mStateLayoutList.iterator();
            while (it.hasNext()) {
                View view = (View) it.next();
                if (view.getVisibility() == 0) {
                    currentShowViews.add(view);
                }
            }
            switch (newState) {
                case 1:
                    if (RD_MainFrame.this.isToolbarsVisible()) {
                        willShowViews.add(RD_MainFrame.this.mTopBarLayout);
                        willShowViews.add(RD_MainFrame.this.mBottomBarLayout);
                        break;
                    }
                    break;
                case 4:
                    if (RD_MainFrame.this.isToolbarsVisible()) {
                        willShowViews.add(RD_MainFrame.this.mEditDoneBarLayout);
                        willShowViews.add(RD_MainFrame.this.mEditBarLayout);
                        break;
                    }
                    break;
                case 5:
                    if (RD_MainFrame.this.isToolbarsVisible()) {
                        willShowViews.add(RD_MainFrame.this.mAnnotCustomBottomBarLayout);
                        willShowViews.add(RD_MainFrame.this.mAnnotCustomTopBarLayout);
                        break;
                    }
                    break;
                case 6:
                    willShowViews.add(RD_MainFrame.this.mEditDoneBarLayout);
                    willShowViews.add(RD_MainFrame.this.mToolSetBarLayout);
                    ToolHandler toolHandler = ((UIExtensionsManager) RD_MainFrame.this.mRead.getUIExtensionsManager()).getCurrentToolHandler();
                    if (toolHandler != null) {
                        RD_MainFrame.this.mToolIconView.setImageResource(RD_MainFrame.this.getToolIcon(toolHandler));
                        RD_MainFrame.this.mToolNameTv.setText(RD_MainFrame.this.getToolName(toolHandler));
                        break;
                    }
                    break;
            }
            Iterator it2 = currentShowViews.iterator();
            while (it2.hasNext()) {
                view = (View) it2.next();
                if (!willShowViews.contains(view)) {
                    if (newState == oldState && view.getTag(RD_MainFrame.this.HIDE_ANIMATION_TAG) != null) {
                        view.startAnimation((AnimationSet) view.getTag(RD_MainFrame.this.HIDE_ANIMATION_TAG));
                    }
                    view.setVisibility(4);
                }
            }
            it2 = willShowViews.iterator();
            while (it2.hasNext()) {
                view = (View) it2.next();
                if (!currentShowViews.contains(view)) {
                    if (view.getTag(RD_MainFrame.this.SHOW_ANIMATION_TAG) != null) {
                        view.startAnimation((Animation) view.getTag(RD_MainFrame.this.SHOW_ANIMATION_TAG));
                    }
                    view.setVisibility(0);
                }
            }
            if (RD_MainFrame.this.mPanelPopupWindow.isShowing() || RD_MainFrame.this.mSettingPopupWindow.isShowing() || RD_MainFrame.this.mThirdMaskCounter > 0) {
                if (RD_MainFrame.this.mMaskView.getVisibility() != 0) {
                    RD_MainFrame.this.mRootView.removeView(RD_MainFrame.this.mMaskView);
                    RD_MainFrame.this.mRootView.addView(RD_MainFrame.this.mMaskView, new LayoutParams(-1, -1));
                    RD_MainFrame.this.mMaskView.setVisibility(0);
                    if (RD_MainFrame.this.mMaskView.getTag(RD_MainFrame.this.SHOW_ANIMATION_TAG) != null) {
                        RD_MainFrame.this.mMaskView.startAnimation((AnimationSet) RD_MainFrame.this.mMaskView.getTag(RD_MainFrame.this.SHOW_ANIMATION_TAG));
                    }
                }
            } else if (RD_MainFrame.this.mMaskView.getVisibility() != 8) {
                RD_MainFrame.this.mMaskView.setVisibility(8);
                if (RD_MainFrame.this.mMaskView.getTag(RD_MainFrame.this.HIDE_ANIMATION_TAG) != null) {
                    RD_MainFrame.this.mMaskView.startAnimation((AnimationSet) RD_MainFrame.this.mMaskView.getTag(RD_MainFrame.this.HIDE_ANIMATION_TAG));
                }
            }
        }
    };
    private ArrayList<View> mStateLayoutList;
    private int mThirdMaskCounter;
    IML_ValueChangeListener mThumbnailListener = new IML_ValueChangeListener() {
        public void onValueChanged(int type, Object value) {
            if (5 == type) {
                RD_MainFrame.this.showThumbnailDialog();
                RD_MainFrame.this.mRead.getMainFrame().hideSettingBar();
            }
        }

        public void onDismiss() {
        }

        public int getType() {
            return 5;
        }
    };
    private ImageView mToolIconView;
    private TextView mToolNameTv;
    private BaseBarImpl mToolSetBar;
    private ViewGroup mToolSetBarLayout;
    private TopBarImpl mTopBar;
    private AnimationSet mTopBarHideAnim;
    private ViewGroup mTopBarLayout;
    private AnimationSet mTopBarShowAnim;

    public RD_MainFrame(Context context, boolean isPhone) {
        this.mContext = context;
        this.mIsPhone = isPhone;
        this.mRootView = (ViewGroup) View.inflate(this.mContext, R.layout.rd_main_frame, null);
        this.mDocViewerLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_docview_ly);
        this.mTopBarLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_top_bar_ly);
        this.mBottomBarLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_bottom_bar_ly);
        this.mEditBarLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_edit_bar_ly);
        this.mEditDoneBarLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_annot_done_bar_ly);
        this.mToolSetBarLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_tool_set_bar_ly);
        this.mAnnotCustomTopBarLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_annot_custom_top_bar_ly);
        this.mAnnotCustomBottomBarLayout = (ViewGroup) this.mRootView.findViewById(R.id.read_annot_custom_bottom_bar_ly);
        this.mMaskView = (ViewGroup) this.mRootView.findViewById(R.id.read_mask_ly);
        this.mToolIconView = (ImageView) this.mRootView.findViewById(R.id.read_tool_icon);
        this.mToolNameTv = (TextView) this.mRootView.findViewById(R.id.read_tool_name_tv);
        int margin_left;
        if (AppDisplay.getInstance(context).isPad()) {
            margin_left = AppResource.getDimensionPixelSize(context, R.dimen.ux_horz_left_margin_pad);
        } else {
            margin_left = AppResource.getDimensionPixelSize(context, R.dimen.ux_horz_left_margin_phone);
        }
        this.mStateLayoutList = new ArrayList();
        this.mStateLayoutList.add(this.mTopBarLayout);
        this.mStateLayoutList.add(this.mBottomBarLayout);
        this.mStateLayoutList.add(this.mEditDoneBarLayout);
        this.mStateLayoutList.add(this.mEditBarLayout);
        this.mStateLayoutList.add(this.mToolSetBarLayout);
        this.mStateLayoutList.add(this.mAnnotCustomTopBarLayout);
        this.mStateLayoutList.add(this.mAnnotCustomBottomBarLayout);
    }

    public void init(RD_Read read) {
        this.mRead = read;
        this.mRead.registerStateChangeListener(this.mStateChangeListener);
        this.mTopBar = new TopBarImpl(this.mContext);
        this.mBottomBar = new BottomBarImpl(this.mContext);
        this.mEditBar = new TB_AnnotsBar(this.mContext);
        this.mEditDoneBar = new TB_EditDoneBar(this.mContext);
        this.mToolSetBar = new TB_AnnotBar(this.mContext);
        this.mAnnotCustomTopBar = new TopBarImpl(this.mContext);
        this.mAnnotCustomBottomBar = new BottomBarImpl(this.mContext);
        this.mPanel = new PanelHostImpl(this.mContext);
        this.mSettingBar = new ML_MultiLineBar(this.mContext);
        this.mSettingBar.init((RD_Read) this.mRead);
        this.mMoreToolsBar = new MT_MoreTools((RD_Read) this.mRead);
        this.mPropertyBar = new PropertyBarImpl(this.mContext, this.mRead.getDocViewer(), this.mRootView);
        this.mTopBar.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_bg_color_toolbar_light));
        this.mBottomBar.setBackgroundColor(this.mContext.getResources().getColor(R.color.ux_bg_color_toolbar_light));
        this.mBottomBar.setItemSpace(this.mContext.getResources().getDimensionPixelSize(R.dimen.rd_bottombar_button_space));
        this.mEditBar.setInterceptTouch(false);
        this.mEditBar.setOrientation(0);
        this.mEditDoneBar.setOrientation(0);
        this.mEditDoneBar.setInterceptTouch(false);
        this.mToolSetBar.setOrientation(0);
        this.mToolSetBar.setInterceptTouch(false);
        this.mAnnotCustomTopBar.setOrientation(0);
        this.mAnnotCustomTopBar.setInterceptTouch(false);
        this.mAnnotCustomBottomBar.setOrientation(0);
        this.mAnnotCustomBottomBar.setInterceptTouch(false);
        this.mTopBarLayout.addView(this.mTopBar.getContentView());
        this.mBottomBarLayout.addView(this.mBottomBar.getContentView());
        this.mEditBarLayout.addView(this.mEditBar.getContentView());
        this.mEditDoneBarLayout.addView(this.mEditDoneBar.getContentView());
        this.mToolSetBarLayout.addView(this.mToolSetBar.getContentView());
        this.mAnnotCustomTopBarLayout.addView(this.mAnnotCustomTopBar.getContentView());
        this.mAnnotCustomBottomBarLayout.addView(this.mAnnotCustomBottomBar.getContentView());
        setPanelView(this.mPanel.getContentView());
        setSettingView(this.mSettingBar.getRootView());
        initBottomBarBtns();
        initOtherView();
        initAnimations();
        this.mStateChangeListener.onStateChanged(this.mRead.getState(), this.mRead.getState());
    }

    private void initBottomBarBtns() {
        this.mPanelBtn = new CircleItemImpl(this.mContext);
        this.mSettingBtn = new CircleItemImpl(this.mContext);
        this.mEditBtn = new CircleItemImpl(this.mContext);
        int circleResId = R.drawable.rd_bar_circle_bg_selector;
        int textSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.ux_text_height_toolbar);
        int textColorResId = R.color.ux_text_color_body2_dark;
        int interval = this.mContext.getResources().getDimensionPixelSize(R.dimen.ux_toolbar_button_icon_text_vert_interval);
        this.mPanelBtn.setImageResource(R.drawable.rd_bar_panel_selector);
        this.mPanelBtn.setText(AppResource.getString(this.mRead.getMainFrame().getContext(), R.string.rd_bar_panel));
        this.mPanelBtn.setRelation(13);
        this.mPanelBtn.setCircleRes(circleResId);
        this.mPanelBtn.setInterval(interval);
        this.mPanelBtn.setTextSize(App.instance().getDisplay().px2dp((float) textSize));
        this.mPanelBtn.setTextColorResource(textColorResId);
        this.mBottomBar.addView(this.mPanelBtn, TB_Position.Position_CENTER);
        this.mPanelBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RD_MainFrame.this.showPanel();
            }
        });
        this.mSettingBtn.setImageResource(R.drawable.rd_bar_setting_selector);
        this.mSettingBtn.setText(AppResource.getString(this.mRead.getMainFrame().getContext(), R.string.rd_bar_setting));
        this.mSettingBtn.setRelation(13);
        this.mSettingBtn.setCircleRes(circleResId);
        this.mSettingBtn.setInterval(interval);
        this.mSettingBtn.setTextSize(App.instance().getDisplay().px2dp((float) textSize));
        this.mSettingBtn.setTextColorResource(textColorResId);
        this.mBottomBar.addView(this.mSettingBtn, TB_Position.Position_CENTER);
        this.mSettingBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RD_MainFrame.this.showSettingBar();
            }
        });
        this.mEditBtn.setImageResource(R.drawable.rd_bar_edit_selector);
        this.mEditBtn.setText(AppResource.getString(this.mRead.getMainFrame().getContext(), R.string.rd_bar_edit));
        this.mEditBtn.setRelation(13);
        this.mEditBtn.setCircleRes(circleResId);
        this.mEditBtn.setInterval(interval);
        this.mEditBtn.setTextSize(App.instance().getDisplay().px2dp((float) textSize));
        this.mEditBtn.setTextColorResource(textColorResId);
        this.mBottomBar.addView(this.mEditBtn, TB_Position.Position_CENTER);
        this.mEditBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                App.instance().getEventManager().onTriggerDismissMenu();
                RD_MainFrame.this.mRead.changeState(4);
            }
        });
        if (((UIExtensionsManager) this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PSISIGNATURE) != null) {
            this.mReadSignItem = new CircleItemImpl(App.instance().getApplicationContext());
            this.mReadSignItem.setImageResource(R.drawable.sg_selector);
            this.mReadSignItem.setText(AppResource.getString(this.mRead.getMainFrame().getContext(), R.string.rd_bar_sign));
            this.mReadSignItem.setRelation(13);
            this.mReadSignItem.setCircleRes(circleResId);
            this.mReadSignItem.setInterval(interval);
            this.mReadSignItem.setTextSize(App.instance().getDisplay().px2dp((float) textSize));
            this.mReadSignItem.setTextColorResource(textColorResId);
            this.mReadSignItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        Module module = ((UIExtensionsManager) RD_MainFrame.this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PSISIGNATURE);
                        if (module instanceof SignatureModule) {
                            ((UIExtensionsManager) RD_MainFrame.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(((SignatureModule) module).getToolHandler());
                        }
                        RD_MainFrame.this.resetAnnotCustomBottomBar();
                        RD_MainFrame.this.resetAnnotCustomTopBar();
                        RD_MainFrame.this.mRead.changeState(5);
                    }
                }
            });
        }
    }

    protected void resetAnnotCustomBottomBar() {
        this.mRead.getMainFrame().getAnnotCustomBottomBar().removeAllItems();
        this.mRead.getMainFrame().getAnnotCustomBottomBar().setBackgroundColor(this.mContext.getResources().getColor(com.foxit.uiextensions.R.color.ux_bg_color_toolbar_light));
        this.mRead.getMainFrame().getAnnotCustomBottomBar().setItemSpace(this.mContext.getResources().getDimensionPixelSize(com.foxit.uiextensions.R.dimen.rd_bottombar_button_space));
        this.mSignListItem = new BaseItemImpl(this.mContext) {
            public void onItemLayout(int l, int t, int r, int b) {
                if (App.instance().getDisplay().isPad() && RD_MainFrame.this.mRead.getMainFrame().getPropertyBar().isShowing()) {
                    Rect rect = new Rect();
                    RD_MainFrame.this.mSignListItem.getContentView().getGlobalVisibleRect(rect);
                    RD_MainFrame.this.mRead.getMainFrame().getPropertyBar().update(new RectF(rect));
                }
            }
        };
        this.mSignListItem.setImageResource(R.drawable.sg_list_selector);
        this.mSignListItem.setText(AppResource.getString(this.mContext, com.foxit.uiextensions.R.string.rv_sign_model));
        this.mSignListItem.setRelation(13);
        this.mSignListItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Rect rect = new Rect();
                RD_MainFrame.this.mSignListItem.getContentView().getGlobalVisibleRect(rect);
                Module module = ((UIExtensionsManager) RD_MainFrame.this.mRead.getUIExtensionsManager()).getModuleByName(Module.MODULE_NAME_PSISIGNATURE);
                if (module != null) {
                    ((SignatureToolHandler) ((SignatureModule) module).getToolHandler()).showSignList(new RectF(rect));
                }
            }
        });
        this.mRead.getMainFrame().getAnnotCustomBottomBar().addView(this.mSignListItem, TB_Position.Position_CENTER);
    }

    protected void resetAnnotCustomTopBar() {
        BaseBar annotCustomBar = this.mRead.getMainFrame().getAnnotCustomTopBar();
        annotCustomBar.removeAllItems();
        annotCustomBar.setBackgroundColor(this.mContext.getResources().getColor(com.foxit.uiextensions.R.color.ux_bg_color_toolbar_light));
        BaseItem closeItem = new BaseItemImpl(this.mContext);
        closeItem.setImageResource(com.foxit.uiextensions.R.drawable.rd_reflow_back_selector);
        closeItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!AppUtil.isFastDoubleClick()) {
                    ((UIExtensionsManager) RD_MainFrame.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                    RD_MainFrame.this.mRead.changeState(1);
                    RD_MainFrame.this.mRead.getDocViewer().invalidate();
                }
            }
        });
        BaseItem titleItem = new BaseItemImpl(this.mContext);
        titleItem.setText(AppResource.getString(this.mContext, com.foxit.uiextensions.R.string.sg_signer_title));
        titleItem.setTextSize(App.instance().getDisplay().px2dp(this.mContext.getResources().getDimension(com.foxit.uiextensions.R.dimen.ux_text_height_subhead)));
        titleItem.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_title_dark));
        annotCustomBar.addView(closeItem, TB_Position.Position_LT);
        annotCustomBar.addView(titleItem, TB_Position.Position_LT);
    }

    private void initOtherView() {
        this.mBackItem = new BaseItemImpl(this.mContext);
        this.mBackItem.setImageResource(R.drawable.rd_reflow_back_selector);
        this.mTopBar.addView(this.mBackItem, TB_Position.Position_LT);
        this.mBackItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                App.instance().getEventManager().onTriggerDismissMenu();
                RD_MainFrame.this.mRead.backToPrevActivity();
            }
        });
        this.mMenuBtn = new BaseItemImpl(this.mContext);
        this.mMenuBtn.setImageResource(R.drawable.rd_bar_more_selector);
        this.mMenuBtn.setTag(ToolbarItemConfig.ITEM_TOPBAR_MORE_TAG);
        this.mMenuBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RD_MainFrame.this.showMoreMenu();
            }
        });
        this.mAnnotDoneBtn = new CircleItemImpl(this.mContext);
        this.mAnnotDoneBtn.setImageResource(R.drawable.cloud_back);
        this.mAnnotDoneBtn.setCircleRes(R.drawable.rd_back_background);
        this.mEditDoneBar.addView(this.mAnnotDoneBtn, TB_Position.Position_LT);
        this.mAnnotDoneBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                App.instance().getEventManager().onTriggerDismissMenu();
                if (((UIExtensionsManager) RD_MainFrame.this.mRead.getUIExtensionsManager()).getCurrentToolHandler() != null) {
                    ((UIExtensionsManager) RD_MainFrame.this.mRead.getUIExtensionsManager()).setCurrentToolHandler(null);
                }
                RD_MainFrame.this.mRead.changeState(1);
                if (!RD_MainFrame.this.isToolbarsVisible()) {
                    RD_MainFrame.this.showToolbars();
                }
            }
        });
    }

    void initAnimations() {
        if (this.mTopBarShowAnim == null) {
            this.mTopBarShowAnim = new AnimationSet(true);
            this.mTopBarHideAnim = new AnimationSet(true);
            this.mBottomBarShowAnim = new AnimationSet(true);
            this.mBottomBarHideAnim = new AnimationSet(true);
            this.mMaskShowAnim = new AnimationSet(true);
            this.mMaskHideAnim = new AnimationSet(true);
        }
        if ((this.mTopBarShowAnim.getAnimations() == null || this.mTopBarShowAnim.getAnimations().size() <= 0) && this.mTopBarLayout.getHeight() != 0) {
            this.SHOW_ANIMATION_TAG = R.id.rd_show_animation_tag;
            this.HIDE_ANIMATION_TAG = R.id.rd_hide_animation_tag;
            TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, (float) (-this.mTopBarLayout.getHeight()), 0.0f);
            anim.setDuration(300);
            this.mTopBarShowAnim.addAnimation(anim);
            anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-this.mTopBarLayout.getHeight()));
            anim.setDuration(300);
            this.mTopBarHideAnim.addAnimation(anim);
            anim = new TranslateAnimation(0.0f, 0.0f, (float) this.mBottomBarLayout.getHeight(), 0.0f);
            anim.setDuration(300);
            this.mBottomBarShowAnim.addAnimation(anim);
            anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) this.mTopBarLayout.getHeight());
            anim.setDuration(300);
            this.mBottomBarHideAnim.addAnimation(anim);
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(300);
            this.mMaskShowAnim.addAnimation(alphaAnimation);
            alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(300);
            this.mMaskHideAnim.addAnimation(alphaAnimation);
            this.mTopBarLayout.setTag(this.SHOW_ANIMATION_TAG, this.mTopBarShowAnim);
            this.mTopBarLayout.setTag(this.HIDE_ANIMATION_TAG, this.mTopBarHideAnim);
            this.mBottomBarLayout.setTag(this.SHOW_ANIMATION_TAG, this.mBottomBarShowAnim);
            this.mBottomBarLayout.setTag(this.HIDE_ANIMATION_TAG, this.mBottomBarHideAnim);
            this.mEditDoneBarLayout.setTag(this.SHOW_ANIMATION_TAG, this.mTopBarShowAnim);
            this.mEditDoneBarLayout.setTag(this.HIDE_ANIMATION_TAG, this.mTopBarHideAnim);
            this.mEditBarLayout.setTag(this.SHOW_ANIMATION_TAG, this.mBottomBarShowAnim);
            this.mEditBarLayout.setTag(this.HIDE_ANIMATION_TAG, this.mBottomBarHideAnim);
            this.mToolSetBarLayout.setTag(this.SHOW_ANIMATION_TAG, this.mBottomBarShowAnim);
            this.mToolSetBarLayout.setTag(this.HIDE_ANIMATION_TAG, this.mBottomBarHideAnim);
            this.mAnnotCustomTopBarLayout.setTag(this.SHOW_ANIMATION_TAG, this.mTopBarShowAnim);
            this.mAnnotCustomTopBarLayout.setTag(this.HIDE_ANIMATION_TAG, this.mTopBarHideAnim);
            this.mAnnotCustomBottomBarLayout.setTag(this.SHOW_ANIMATION_TAG, this.mBottomBarShowAnim);
            this.mAnnotCustomBottomBarLayout.setTag(this.HIDE_ANIMATION_TAG, this.mBottomBarHideAnim);
            this.mMaskView.setTag(this.SHOW_ANIMATION_TAG, this.mMaskShowAnim);
            this.mMaskView.setTag(this.HIDE_ANIMATION_TAG, this.mMaskHideAnim);
        }
    }

    protected void setMoreMenuModule(MoreMenuModule moreMenuModule) {
        this.mMoreMenuModule = moreMenuModule;
    }

    private void resetPanelFocus(int tag) {
        if (tag >= 0) {
            this.mPanel.setCurrentSpec(tag);
        }
    }

    public void addDocView(View docView) {
        this.mDocViewerLayout.addView(docView);
    }

    public RelativeLayout getContentView() {
        return (RelativeLayout) this.mRootView;
    }

    public BaseBar getAnnotCustomTopBar() {
        return this.mAnnotCustomTopBar;
    }

    public BaseBar getAnnotCustomBottomBar() {
        return this.mAnnotCustomBottomBar;
    }

    public BaseBar getTopToolbar() {
        return this.mTopBar;
    }

    public PropertyBar getPropertyBar() {
        return this.mPropertyBar;
    }

    public BaseBar getBottomToolbar() {
        return this.mBottomBar;
    }

    public void showToolbars() {
        App.instance().getEventManager().triggerInteractTimer();
        this.mIsFullScreen = false;
        this.mRead.changeState(this.mRead.getState());
    }

    public void hideToolbars() {
        this.mIsFullScreen = true;
        this.mRead.changeState(this.mRead.getState());
    }

    public boolean isToolbarsVisible() {
        return !this.mIsFullScreen;
    }

    public PanelHost getPanel() {
        return this.mPanel;
    }

    public PopupWindow getPanelWindow() {
        return this.mPanelPopupWindow;
    }

    private void setPanelView(View view) {
        this.mPanelPopupWindow = new PopupWindow(view, -1, -1, true);
        this.mPanelPopupWindow.setBackgroundDrawable(new ColorDrawable(ViewCompat.MEASURED_SIZE_MASK));
        this.mPanelPopupWindow.setAnimationStyle(R.style.View_Animation_LtoR);
        this.mPanelPopupWindow.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                RD_MainFrame.this.mStateChangeListener.onStateChanged(RD_MainFrame.this.mRead.getState(), RD_MainFrame.this.mRead.getState());
            }
        });
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mPanelPopupWindow != null && this.mPanelPopupWindow.isShowing()) {
            hidePanel();
            showPanel();
        }
    }

    public void showPanel() {
        if (this.mPanel.getCurrentSpec() == null) {
            showPanel(1);
        } else {
            showPanel(this.mPanel.getCurrentSpec().getTag());
        }
    }

    public void showPanel(int TabTag) {
        boolean bVertical;
        int height;
        int width;
        App.instance().getEventManager().onTriggerDismissMenu();
        int viewWidth = this.mRootView.getWidth();
        int viewHeight = this.mRootView.getHeight();
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
        if (!this.mIsPhone) {
            float scale = 0.535f;
            if (width > height) {
                scale = 0.338f;
            }
            width = (int) (((float) App.instance().getDisplay().getScreenWidth()) * scale);
        }
        this.mPanelPopupWindow.setWidth(width);
        this.mPanelPopupWindow.setHeight(height);
        this.mPanelPopupWindow.setSoftInputMode(1);
        if (VERSION.SDK_INT >= 11) {
            this.mPanelPopupWindow.setSoftInputMode(48);
        }
        resetPanelFocus(TabTag);
        this.mPanelPopupWindow.showAtLocation(this.mRootView, 51, 0, 0);
        this.mStateChangeListener.onStateChanged(this.mRead.getState(), this.mRead.getState());
    }

    public void hidePanel() {
        if (this.mPanelPopupWindow.isShowing()) {
            this.mPanelPopupWindow.dismiss();
        }
    }

    public void showMoreMenu() {
        MoreMenuView view = this.mMoreMenuModule.getView();
        if (view != null) {
            view.show();
        }
    }

    public void hideMoreMenu() {
        MoreMenuView view = this.mMoreMenuModule.getView();
        if (view != null) {
            view.hide();
        }
    }

    public IML_MultiLineBar getSettingBar() {
        return this.mSettingBar;
    }

    private void setSettingView(View view) {
        this.mSettingPopupWindow = new PopupWindow(view, -1, -2, true);
        this.mSettingPopupWindow.setBackgroundDrawable(new ColorDrawable(ViewCompat.MEASURED_SIZE_MASK));
        this.mSettingPopupWindow.setAnimationStyle(R.style.View_Animation_BtoT);
        this.mSettingPopupWindow.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                RD_MainFrame.this.mStateChangeListener.onStateChanged(RD_MainFrame.this.mRead.getState(), RD_MainFrame.this.mRead.getState());
            }
        });
    }

    private void applyValue() {
        if (this.mSinglePage) {
            this.mRead.getDocViewer().setPageLayoutMode(1);
        } else {
            this.mRead.getDocViewer().setPageLayoutMode(2);
        }
    }

    private void showThumbnailDialog() {
        ((ThumbnailModule) App.instance().getModuleByName(Module.MODULE_NAME_THUMBNAIL)).show();
    }

    void showSettingBar() {
        if (this.mSettingBar != null) {
            App.instance().getEventManager().onTriggerDismissMenu();
            this.mSettingBar.getContentView().measure(0, 0);
            this.mSettingBar.registerListener(this.mSingleChangeListener);
            this.mSettingBar.registerListener(this.mThumbnailListener);
            this.mSettingBar.registerListener(((RD_ScreenLock) App.instance().getModuleByName(RD_ScreenLock.MODULE_NAME_SCREENLOCK)).getScreenLockListener());
            int height = this.mSettingBar.getContentView().getMeasuredHeight();
            this.mSettingPopupWindow.setWidth(this.mRootView.getWidth());
            this.mSettingPopupWindow.setHeight(height);
            this.mSettingPopupWindow.setSoftInputMode(1);
            if (VERSION.SDK_INT >= 11) {
                this.mSettingPopupWindow.setSoftInputMode(48);
            }
            this.mSettingPopupWindow.showAtLocation(this.mRootView, 48, 0, this.mRootView.getHeight() - height);
            this.mStateChangeListener.onStateChanged(this.mRead.getState(), this.mRead.getState());
            this.mSettingBar.setProperty(4, Boolean.valueOf(this.mSinglePage));
        }
    }

    public void updateSettingBar() {
        boolean bVertical = true;
        if (this.mSettingBar != null) {
            int screenHeight;
            int screenWidth;
            this.mSettingBar.getContentView().measure(0, 0);
            int barHeight = this.mSettingBar.getContentView().getMeasuredHeight();
            if (this.mContext.getResources().getConfiguration().orientation != 1) {
                bVertical = false;
            }
            int width = App.instance().getDisplay().getScreenWidth();
            int height = App.instance().getDisplay().getScreenHeight();
            if (bVertical) {
                screenHeight = Math.max(width, height);
                screenWidth = Math.min(width, height);
            } else {
                screenHeight = Math.min(width, height);
                screenWidth = Math.max(width, height);
            }
            this.mSettingPopupWindow.update(0, screenHeight - barHeight, screenWidth, barHeight);
        }
    }

    public void hideSettingBar() {
        if (this.mSettingPopupWindow != null && this.mSettingPopupWindow.isShowing()) {
            this.mSettingPopupWindow.dismiss();
        }
    }

    public IMT_MoreTools getMoreToolsBar() {
        return this.mMoreToolsBar;
    }

    public void showMaskView() {
        this.mThirdMaskCounter++;
        this.mStateChangeListener.onStateChanged(this.mRead.getState(), this.mRead.getState());
    }

    public void hideMaskView() {
        this.mThirdMaskCounter--;
        if (this.mThirdMaskCounter < 0) {
            this.mThirdMaskCounter = 0;
        }
        this.mStateChangeListener.onStateChanged(this.mRead.getState(), this.mRead.getState());
    }

    protected void resetMaskView() {
        if (this.mPanelPopupWindow.isShowing()) {
            hidePanel();
        }
        if (this.mMoreMenuModule != null) {
            hideMoreMenu();
        }
        if (this.mSettingPopupWindow.isShowing()) {
            hideSettingBar();
        }
        if (this.mMoreToolsBar.isShowing()) {
            this.mMoreToolsBar.dismiss();
        }
        if (isMaskViewShowing()) {
            hideMaskView();
        }
        this.mThirdMaskCounter = 0;
    }

    public boolean isMaskViewShowing() {
        return this.mMaskView.getVisibility() == 0 || this.mThirdMaskCounter > 0;
    }

    public boolean isEditBarShowing() {
        return this.mEditBarLayout != null && this.mEditBarLayout.getVisibility() == 0;
    }

    public BaseBar getEditBar() {
        return this.mEditBar;
    }

    public BaseBar getEditDoneBar() {
        return this.mEditDoneBar;
    }

    public BaseBar getToolSetBar() {
        return this.mToolSetBar;
    }

    public Activity getAttachedActivity() {
        return this.mAttachedActivity;
    }

    public void setAttachedActivity(Activity act) {
        this.mAttachedActivity = act;
    }

    public boolean getCloseAttachedActivity() {
        return this.mCloseAttachedActivity;
    }

    public Context getContext() {
        return this.mContext;
    }

    int getToolIcon(ToolHandler toolHandler) {
        int toolIcon = R.drawable.fx_item_detail;
        String type = toolHandler.getType();
        switch (type.hashCode()) {
            case -1926096327:
                if (type.equals(ToolHandler.TH_TYPE_SQUIGGLY)) {
                    return R.drawable.annot_tool_prompt_squiggly;
                }
                return toolIcon;
            case -1800392020:
                if (type.equals(ToolHandler.TH_TYPE_UNDERLINE)) {
                    return R.drawable.annot_tool_prompt_underline;
                }
                return toolIcon;
            case -569154298:
                if (type.equals(ToolHandler.TH_TYPE_NOTE)) {
                    return R.drawable.annot_tool_prompt_text;
                }
                return toolIcon;
            case -544481852:
                if (type.equals(ToolHandler.TH_TYPE_HIGHLIGHT)) {
                    return R.drawable.annot_tool_prompt_highlight;
                }
                return toolIcon;
            case -17942450:
                if (type.equals(ToolHandler.TH_TYPR_INSERTTEXT)) {
                    return R.drawable.annot_tool_prompt_insert;
                }
                return toolIcon;
            case 104122389:
                if (type.equals(ToolHandler.TH_TYPE_STAMP)) {
                    return R.drawable.annot_tool_prompt_stamp;
                }
                return toolIcon;
            case 148772420:
                if (type.equals(ToolHandler.TH_TYPE_LINE)) {
                    return R.drawable.annot_tool_prompt_line;
                }
                return toolIcon;
            case 204755855:
                if (type.equals(ToolHandler.TH_TYPE_ARROW)) {
                    return R.drawable.annot_tool_prompt_arrow;
                }
                return toolIcon;
            case 312777170:
                if (type.equals(ToolHandler.TH_TYPE_INK)) {
                    return R.drawable.annot_tool_prompt_pencil;
                }
                return toolIcon;
            case 566296516:
                if (type.equals(ToolHandler.TH_TYPE_REPLACE)) {
                    return R.drawable.annot_tool_prompt_replace;
                }
                return toolIcon;
            case 614611371:
                if (type.equals(ToolHandler.TH_TYPE_TYPEWRITER)) {
                    return R.drawable.annot_tool_prompt_typwriter;
                }
                return toolIcon;
            case 1542273768:
                if (type.equals(ToolHandler.TH_TYPE_CIRCLE)) {
                    return R.drawable.annot_tool_prompt_circle;
                }
                return toolIcon;
            case 1706453676:
                if (type.equals(ToolHandler.TH_TYPE_ERASER)) {
                    return R.drawable.annot_tool_prompt_eraser;
                }
                return toolIcon;
            case 1721424156:
                if (type.equals(ToolHandler.TH_TYPE_STRIKEOUT)) {
                    return R.drawable.annot_tool_prompt_strikeout;
                }
                return toolIcon;
            case 1995720379:
                if (type.equals(ToolHandler.TH_TYPE_SQUARE)) {
                    return R.drawable.annot_tool_prompt_square;
                }
                return toolIcon;
            case 2073171289:
                if (type.equals(ToolHandler.TH_TYPE_FileAttachment)) {
                    return R.drawable.annot_tool_prompt_fileattachment;
                }
                return toolIcon;
            default:
                return toolIcon;
        }
    }

    String getToolName(ToolHandler toolHandler) {
        String toolName = "-";
        String type = toolHandler.getType();
        switch (type.hashCode()) {
            case -1926096327:
                if (type.equals(ToolHandler.TH_TYPE_SQUIGGLY)) {
                    return "Squiggly";
                }
                return toolName;
            case -1800392020:
                if (type.equals(ToolHandler.TH_TYPE_UNDERLINE)) {
                    return "Underline";
                }
                return toolName;
            case -569154298:
                if (type.equals(ToolHandler.TH_TYPE_NOTE)) {
                    return "Note";
                }
                return toolName;
            case -544481852:
                if (type.equals(ToolHandler.TH_TYPE_HIGHLIGHT)) {
                    return "Highlight";
                }
                return toolName;
            case -17942450:
                if (type.equals(ToolHandler.TH_TYPR_INSERTTEXT)) {
                    return "Insert Text";
                }
                return toolName;
            case 104122389:
                if (type.equals(ToolHandler.TH_TYPE_STAMP)) {
                    return "Stamp";
                }
                return toolName;
            case 148772420:
                if (type.equals(ToolHandler.TH_TYPE_LINE)) {
                    return "Line";
                }
                return toolName;
            case 204755855:
                if (type.equals(ToolHandler.TH_TYPE_ARROW)) {
                    return LineConstants.SUBJECT_ARROW;
                }
                return toolName;
            case 312777170:
                if (type.equals(ToolHandler.TH_TYPE_INK)) {
                    return "Pencil";
                }
                return toolName;
            case 566296516:
                if (type.equals(ToolHandler.TH_TYPE_REPLACE)) {
                    return "Replace";
                }
                return toolName;
            case 614611371:
                if (type.equals(ToolHandler.TH_TYPE_TYPEWRITER)) {
                    return "Typewriter";
                }
                return toolName;
            case 1542273768:
                if (type.equals(ToolHandler.TH_TYPE_CIRCLE)) {
                    return "Oval";
                }
                return toolName;
            case 1706453676:
                if (type.equals(ToolHandler.TH_TYPE_ERASER)) {
                    return "Eraser";
                }
                return toolName;
            case 1721424156:
                if (type.equals(ToolHandler.TH_TYPE_STRIKEOUT)) {
                    return "Strikeout";
                }
                return toolName;
            case 1995720379:
                if (type.equals(ToolHandler.TH_TYPE_SQUARE)) {
                    return "Rectangle";
                }
                return toolName;
            case 2073171289:
                if (type.equals(ToolHandler.TH_TYPE_FileAttachment)) {
                    return "Attachment";
                }
                return toolName;
            default:
                return toolName;
        }
    }
}
