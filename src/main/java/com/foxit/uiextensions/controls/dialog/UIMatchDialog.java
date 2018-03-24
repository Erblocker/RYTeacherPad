package com.foxit.uiextensions.controls.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DismissListener;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.BaseBar.TB_Position;
import com.foxit.uiextensions.controls.toolbar.BaseItem;
import com.foxit.uiextensions.controls.toolbar.BaseItem.ItemType;
import com.foxit.uiextensions.controls.toolbar.impl.BaseBarImpl;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;
import com.foxit.uiextensions.utils.AppDisplay;

public class UIMatchDialog extends Dialog implements MatchDialog {
    private BaseItem mBackItem;
    private int mButtonHeight;
    private LinearLayout mButtonsView;
    private LinearLayout mButtonsViewRoot;
    private LinearLayout mContentView;
    private LinearLayout mContentViewRoot;
    protected Context mContext;
    private DialogListener mDialogListener;
    private DismissListener mDismissListener;
    private Button mDlg_bt_cancel;
    private Button mDlg_bt_copy;
    private Button mDlg_bt_move;
    private Button mDlg_bt_ok;
    private Button mDlg_bt_open_only;
    private Button mDlg_bt_replace;
    private Button mDlg_bt_skip;
    private Button mDlg_bt_upload;
    private boolean mFullscreen;
    private int mHeight;
    protected boolean mIsPad;
    private String mLanguage;
    private DisplayMetrics mMetrics;
    private int mOrientation;
    private boolean mPopupStyle;
    private RelativeLayout mRootView;
    private boolean mShowMask;
    private boolean mShowSeparateLine;
    private BaseBar mTitleBar;
    private ImageView mTitleBlueLine;
    private BaseItem mTitleItem;
    private int mTitleStyle;
    private LinearLayout mTitleView;
    private View mView;

    public UIMatchDialog(Context context) {
        int i;
        boolean z = false;
        if (AppDisplay.getInstance(context).isPad()) {
            i = 0;
        } else {
            i = R.style.rd_dialog_fullscreen_style;
        }
        super(context, i);
        this.mShowSeparateLine = true;
        this.mFullscreen = false;
        this.mPopupStyle = false;
        this.mTitleStyle = 1;
        this.mHeight = -100;
        this.mShowMask = false;
        this.mIsPad = false;
        this.mContext = context;
        this.mIsPad = AppDisplay.getInstance(context).isPad();
        this.mShowSeparateLine = true;
        if (!this.mIsPad) {
            z = true;
        }
        this.mFullscreen = z;
        this.mMetrics = this.mContext.getResources().getDisplayMetrics();
        requestWindowFeature(1);
        initView();
    }

    public UIMatchDialog(Context context, boolean showSeparateLine) {
        int i;
        boolean z = false;
        if (AppDisplay.getInstance(context).isPad()) {
            i = 0;
        } else {
            i = R.style.rd_dialog_fullscreen_style;
        }
        super(context, i);
        this.mShowSeparateLine = true;
        this.mFullscreen = false;
        this.mPopupStyle = false;
        this.mTitleStyle = 1;
        this.mHeight = -100;
        this.mShowMask = false;
        this.mIsPad = false;
        this.mContext = context;
        this.mShowSeparateLine = showSeparateLine;
        if (!AppDisplay.getInstance(context).isPad()) {
            z = true;
        }
        this.mFullscreen = z;
        this.mMetrics = this.mContext.getResources().getDisplayMetrics();
        requestWindowFeature(1);
        initView();
    }

    public UIMatchDialog(Context context, int theme) {
        super(context, theme);
        this.mShowSeparateLine = true;
        this.mFullscreen = false;
        this.mPopupStyle = false;
        this.mTitleStyle = 1;
        this.mHeight = -100;
        this.mShowMask = false;
        this.mIsPad = false;
        this.mContext = context;
        this.mShowSeparateLine = true;
        if (theme == R.style.rd_dialog_fullscreen_style) {
            this.mFullscreen = true;
            this.mPopupStyle = false;
        } else {
            this.mFullscreen = false;
            this.mPopupStyle = true;
        }
        this.mMetrics = this.mContext.getResources().getDisplayMetrics();
        requestWindowFeature(1);
        initView();
    }

    public UIMatchDialog(Context context, int theme, boolean showSeparateLine) {
        super(context, theme);
        this.mShowSeparateLine = true;
        this.mFullscreen = false;
        this.mPopupStyle = false;
        this.mTitleStyle = 1;
        this.mHeight = -100;
        this.mShowMask = false;
        this.mIsPad = false;
        this.mContext = context;
        this.mIsPad = AppDisplay.getInstance(context).isPad();
        this.mShowSeparateLine = showSeparateLine;
        if (theme == R.style.rd_dialog_fullscreen_style) {
            this.mFullscreen = true;
            this.mPopupStyle = false;
        } else {
            this.mFullscreen = false;
            this.mPopupStyle = true;
        }
        this.mMetrics = this.mContext.getResources().getDisplayMetrics();
        requestWindowFeature(1);
        initView();
    }

    private void initView() {
        this.mLanguage = this.mContext.getResources().getConfiguration().locale.getLanguage();
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        this.mButtonHeight = (int) this.mContext.getResources().getDimension(R.dimen.ux_dialog_button_height);
        this.mView = LayoutInflater.from(this.mContext).inflate(R.layout.dlg_root, null, false);
        this.mRootView = (RelativeLayout) this.mView.findViewById(R.id.dlg_root);
        this.mTitleView = (LinearLayout) this.mRootView.findViewById(R.id.dlg_top_title);
        this.mContentViewRoot = (LinearLayout) this.mRootView.findViewById(R.id.dlg_contentview_root);
        this.mContentView = (LinearLayout) this.mRootView.findViewById(R.id.dlg_contentview);
        this.mButtonsViewRoot = (LinearLayout) this.mRootView.findViewById(R.id.dlg_buttonview);
        this.mTitleBlueLine = (ImageView) this.mRootView.findViewById(R.id.dlg_top_title_line_blue);
        LayoutParams contentViewRootParams = (LayoutParams) this.mContentViewRoot.getLayoutParams();
        if (this.mIsPad) {
            contentViewRootParams.topMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_pad);
        } else {
            contentViewRootParams.topMargin = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_phone);
        }
        this.mContentViewRoot.setLayoutParams(contentViewRootParams);
        this.mButtonsViewRoot.setPadding(0, 0, 0, AppDisplay.getInstance(this.mContext).dp2px(5.0f));
        this.mTitleBlueLine.setVisibility(8);
        if (this.mFullscreen) {
            this.mTitleStyle = 1;
            if (this.mShowSeparateLine) {
                this.mTitleBar = new TopBarImpl(this.mContext);
            } else {
                this.mTitleBar = new BaseBarImpl(this.mContext);
            }
        } else {
            this.mTitleStyle = 2;
            this.mTitleBar = new BaseBarImpl(this.mContext);
            if (this.mShowSeparateLine) {
                this.mTitleBlueLine.setVisibility(0);
            } else {
                this.mTitleBlueLine.setVisibility(8);
            }
        }
        this.mBackItem = new BaseItemImpl(this.mContext);
        this.mBackItem.setDisplayStyle(ItemType.Item_Image);
        if (this.mIsPad) {
            this.mBackItem.setImageResource(R.drawable.dlg_back_blue_selector);
        } else {
            this.mBackItem.setImageResource(R.drawable.cloud_back);
        }
        this.mBackItem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (UIMatchDialog.this.mDialogListener != null) {
                    UIMatchDialog.this.mDialogListener.onBackClick();
                }
                UIMatchDialog.this.dismiss();
            }
        });
        this.mTitleItem = new BaseItemImpl(this.mContext);
        this.mTitleItem.setDisplayStyle(ItemType.Item_Text);
        this.mTitleItem.setText("");
        this.mTitleItem.setTextColorResource(R.color.ux_text_color_title_light);
        this.mTitleItem.setTextSize(18.0f);
        if (this.mTitleStyle == 1) {
            setTitleStyleBlue();
        } else if (this.mTitleStyle == 2) {
            setTitleStyleWhite();
        } else {
            setTitleStyleBlue();
        }
        this.mTitleBar.addView(this.mBackItem, TB_Position.Position_LT);
        this.mTitleBar.addView(this.mTitleItem, TB_Position.Position_LT);
        this.mTitleView.addView(this.mTitleBar.getContentView());
        getWindow().setFlags(1024, 1024);
        super.setContentView(this.mView);
        if (!this.mFullscreen) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = AppDisplay.getInstance(this.mContext).getDialogWidth();
            params.height = getDialogHeight();
            getWindow().setAttributes(params);
        }
        setCanceledOnTouchOutside(true);
        setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (UIMatchDialog.this.mShowMask) {
                    UIMatchDialog.this.mShowMask = false;
                }
                if (UIMatchDialog.this.mDismissListener != null) {
                    UIMatchDialog.this.mDismissListener.onDismiss();
                }
            }
        });
    }

    public void setFullScreenWithStatusBar() {
        getWindow().addFlags(2048);
        getWindow().clearFlags(1024);
    }

    public void setButton(long buttons) {
        if (buttons == 0) {
            this.mButtonsViewRoot.removeAllViews();
            this.mButtonsViewRoot.setPadding(0, 0, 0, AppDisplay.getInstance(this.mContext).dp2px(5.0f));
            return;
        }
        this.mButtonsViewRoot.removeAllViews();
        ImageView separator = new ImageView(this.mContext);
        separator.setLayoutParams(new LinearLayout.LayoutParams(-1, 1));
        separator.setImageResource(R.color.ux_color_dialog_cutting_line);
        this.mButtonsViewRoot.addView(separator);
        this.mButtonsView = new LinearLayout(this.mContext);
        this.mButtonsView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        this.mButtonsView.setOrientation(0);
        this.mButtonsView.setGravity(16);
        this.mButtonsViewRoot.addView(this.mButtonsView);
        if ((1 & buttons) == 1) {
            this.mDlg_bt_cancel = new Button(this.mContext);
            addButton(this.mDlg_bt_cancel, this.mContext.getResources().getString(R.string.fx_string_cancel), 1);
        }
        if ((2 & buttons) == 2) {
            this.mDlg_bt_skip = new Button(this.mContext);
            addButton(this.mDlg_bt_skip, this.mContext.getResources().getString(R.string.fm_paste_skip), 2);
        }
        if ((16 & buttons) == 16) {
            this.mDlg_bt_replace = new Button(this.mContext);
            addButton(this.mDlg_bt_replace, this.mContext.getResources().getString(R.string.fm_paste_replace), 16);
        }
        if ((32 & buttons) == 32) {
            this.mDlg_bt_copy = new Button(this.mContext);
            addButton(this.mDlg_bt_copy, this.mContext.getResources().getString(R.string.fx_string_copy), 32);
        }
        if ((64 & buttons) == 64) {
            this.mDlg_bt_move = new Button(this.mContext);
            addButton(this.mDlg_bt_move, this.mContext.getResources().getString(R.string.fm_move), 64);
        }
        if ((4 & buttons) == 4) {
            this.mDlg_bt_ok = new Button(this.mContext);
            addButton(this.mDlg_bt_ok, this.mContext.getResources().getString(R.string.fx_string_ok), 4);
        }
        if ((8 & buttons) == 8) {
            this.mDlg_bt_open_only = new Button(this.mContext);
            addButton(this.mDlg_bt_open_only, this.mContext.getResources().getString(R.string.rv_emailreview_mergedlg_openbutton), 8);
        }
        if ((128 & buttons) == 128) {
            this.mDlg_bt_upload = new Button(this.mContext);
            addButton(this.mDlg_bt_upload, this.mContext.getResources().getString(R.string.cloud_toolbar_more_upload), 128);
        }
        if (this.mButtonsView.getChildCount() > 0) {
            this.mButtonsViewRoot.setPadding(0, 0, 0, 0);
            this.mButtonsView.getChildAt(0).setVisibility(8);
            int buttonNum = this.mButtonsView.getChildCount() / 2;
            if (buttonNum == 1) {
                this.mButtonsView.getChildAt((buttonNum * 2) - 1).setBackgroundResource(R.drawable.dialog_button_background_selector);
            } else if (buttonNum == 2) {
                this.mButtonsView.getChildAt(1).setBackgroundResource(R.drawable.dialog_left_button_background_selector);
                this.mButtonsView.getChildAt(3).setBackgroundResource(R.drawable.dialog_right_button_background_selector);
            } else if (buttonNum > 2) {
                this.mButtonsView.getChildAt(1).setBackgroundResource(R.drawable.dialog_left_button_background_selector);
                this.mButtonsView.getChildAt((buttonNum * 2) - 1).setBackgroundResource(R.drawable.dialog_right_button_background_selector);
                for (int i = 2; i < buttonNum; i++) {
                    this.mButtonsView.getChildAt((i * 2) - 1).setBackgroundResource(R.drawable.dlg_bt_bg_selector);
                }
            }
        } else {
            this.mButtonsViewRoot.setPadding(0, 0, 0, AppDisplay.getInstance(this.mContext).dp2px(5.0f));
            separator.setVisibility(8);
        }
        if (this.mButtonsView.getChildCount() == 6 && ((Long) this.mButtonsView.getChildAt(5).getTag()).longValue() == 8) {
            LinearLayout.LayoutParams cancelLayoutParams = (LinearLayout.LayoutParams) this.mButtonsView.getChildAt(1).getLayoutParams();
            cancelLayoutParams.width = -2;
            this.mButtonsView.getChildAt(1).setLayoutParams(cancelLayoutParams);
            LinearLayout.LayoutParams okLayoutParams = (LinearLayout.LayoutParams) this.mButtonsView.getChildAt(3).getLayoutParams();
            okLayoutParams.width = -2;
            this.mButtonsView.getChildAt(3).setLayoutParams(okLayoutParams);
            LinearLayout.LayoutParams openOnlyLayoutParams = (LinearLayout.LayoutParams) this.mButtonsView.getChildAt(5).getLayoutParams();
            openOnlyLayoutParams.width = -2;
            this.mButtonsView.getChildAt(5).setLayoutParams(openOnlyLayoutParams);
            return;
        }
        if (this.mDlg_bt_cancel != null) {
            cancelLayoutParams = (LinearLayout.LayoutParams) this.mDlg_bt_cancel.getLayoutParams();
            cancelLayoutParams.width = 0;
            this.mDlg_bt_cancel.setLayoutParams(cancelLayoutParams);
        }
        if (this.mDlg_bt_ok != null) {
            cancelLayoutParams = (LinearLayout.LayoutParams) this.mDlg_bt_ok.getLayoutParams();
            cancelLayoutParams.width = 0;
            this.mDlg_bt_ok.setLayoutParams(cancelLayoutParams);
        }
    }

    private void addButton(Button button, String title, long buttonType) {
        ImageView separator_vertical = new ImageView(this.mContext);
        separator_vertical.setLayoutParams(new LinearLayout.LayoutParams(1, -1));
        separator_vertical.setImageResource(R.color.ux_color_dialog_cutting_line);
        this.mButtonsView.addView(separator_vertical);
        button.setLayoutParams(new LinearLayout.LayoutParams(0, this.mButtonHeight, 1.0f));
        button.setBackgroundResource(R.drawable.dlg_bt_bg_selector);
        button.setGravity(17);
        button.setText(title);
        button.setTextSize(0, this.mContext.getResources().getDimension(R.dimen.ux_text_height_button));
        button.setTextColor(this.mContext.getResources().getColor(R.color.dlg_bt_text_selector));
        button.setTag(Long.valueOf(buttonType));
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (UIMatchDialog.this.mDialogListener != null) {
                    UIMatchDialog.this.mDialogListener.onResult(((Long) v.getTag()).longValue());
                }
            }
        });
        this.mButtonsView.addView(button);
    }

    public boolean isShowing() {
        return super.isShowing();
    }

    private void prepareShow() {
        resetForHeight();
    }

    public void showDialog() {
        prepareShow();
        AppDialogManager.getInstance().showAllowManager(this, null);
    }

    public void showDialogNoManage() {
        prepareShow();
    }

    public void showDialog(boolean showMask) {
        prepareShow();
        if (this != null && !isShowing()) {
            AppDialogManager.getInstance().showAllowManager(this, null);
            this.mShowMask = showMask;
        }
    }

    public void dismiss() {
        if (this != null && isShowing()) {
            super.dismiss();
        }
    }

    public View getRootView() {
        return this.mRootView;
    }

    public void setTitle(String title) {
        this.mTitleItem.setText(title);
    }

    public void setContentView(View view) {
        if (view != null) {
            this.mContentView.removeAllViews();
            this.mContentView.addView(view);
        }
    }

    public void setStyle(int style) {
        this.mTitleStyle = style;
        if (style == 1) {
            setTitleStyleBlue();
        } else if (style == 2) {
            setTitleStyleWhite();
        } else {
            this.mTitleStyle = 1;
            setTitleStyleBlue();
        }
    }

    private void setTitleStyleBlue() {
        this.mBackItem.setImageResource(R.drawable.cloud_back);
        this.mTitleItem.setTextColorResource(R.color.ux_text_color_title_light);
        if (this.mFullscreen) {
            getWindow().setBackgroundDrawableResource(R.color.ux_color_white);
            this.mTitleBar.setBackgroundResource(R.color.ux_bg_color_toolbar_colour);
            return;
        }
        getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
        this.mTitleBar.setBackgroundResource(R.drawable.dlg_title_bg_circle_corner_blue);
    }

    private void setTitleStyleWhite() {
        this.mBackItem.setImageResource(R.drawable.dlg_back_blue_selector);
        this.mTitleItem.setTextColorResource(R.color.ux_text_color_subhead_colour);
        if (this.mFullscreen) {
            getWindow().setBackgroundDrawableResource(R.color.ux_color_white);
            this.mTitleBar.setBackgroundResource(R.color.ux_color_white);
            return;
        }
        getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
        this.mTitleBar.setBackgroundResource(R.drawable.dlg_title_bg_circle_corner_white);
    }

    public void setBackButtonVisible(int visibility) {
        if (!AppDisplay.getInstance(this.mContext).isPad() && this.mFullscreen) {
            if (visibility == 0) {
                this.mTitleBar.removeItemByItem(this.mTitleItem);
                this.mTitleBar.addView(this.mTitleItem, TB_Position.Position_LT);
            } else {
                this.mTitleBar.removeItemByItem(this.mTitleItem);
                this.mTitleBar.addView(this.mTitleItem, TB_Position.Position_CENTER);
            }
        }
        this.mBackItem.getContentView().setVisibility(visibility);
    }

    public void setButtonEnable(boolean enable, long buttons) {
        if ((buttons & 1) == 1) {
            setEnable(this.mDlg_bt_cancel, enable);
        }
        if ((buttons & 2) == 2) {
            setEnable(this.mDlg_bt_skip, enable);
        }
        if ((buttons & 16) == 16) {
            setEnable(this.mDlg_bt_replace, enable);
        }
        if ((32 & buttons) == 32) {
            setEnable(this.mDlg_bt_copy, enable);
        }
        if ((64 & buttons) == 64) {
            setEnable(this.mDlg_bt_move, enable);
        }
        if ((buttons & 4) == 4) {
            setEnable(this.mDlg_bt_ok, enable);
        }
        if ((buttons & 8) == 8) {
            setEnable(this.mDlg_bt_open_only, enable);
        }
        if ((128 & buttons) == 128) {
            setEnable(this.mDlg_bt_upload, enable);
        }
    }

    private void setEnable(Button button, boolean enable) {
        if (button != null) {
            if (enable) {
                button.setTextColor(this.mContext.getResources().getColor(R.color.dlg_bt_text_selector));
            } else {
                button.setTextColor(this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
            }
            button.setEnabled(enable);
        }
    }

    public void setTitleBlueLineVisible(boolean visible) {
        if (visible) {
            this.mTitleBlueLine.setVisibility(0);
        } else {
            this.mTitleBlueLine.setVisibility(8);
        }
    }

    public void setWidth(int width) {
        if (!this.mFullscreen) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = width;
            getWindow().setAttributes(params);
        }
    }

    public void setHeight(int height) {
        if (!this.mFullscreen) {
            this.mHeight = height;
            WindowManager.LayoutParams params = getWindow().getAttributes();
            if (this.mHeight >= getDialogHeight() || !(this.mHeight > 0 || this.mHeight == -2 || this.mHeight == -1)) {
                params.height = getDialogHeight();
            } else {
                params.height = height;
            }
            getWindow().setAttributes(params);
        }
    }

    private void resetForHeight() {
        if (!this.mFullscreen && this.mHeight != -100) {
            this.mRootView.measure(0, 0);
            if (this.mHeight == -2) {
                int maxTempHeight = getDialogHeight() - this.mTitleView.getMeasuredHeight();
                if (this.mButtonsView != null) {
                    if (this.mButtonsView.getChildCount() > 0) {
                        maxTempHeight = (maxTempHeight - this.mButtonHeight) - 1;
                    } else {
                        maxTempHeight -= AppDisplay.getInstance(this.mContext).dp2px(5.0f);
                    }
                }
                if (this.mTitleBlueLine.getVisibility() != 8) {
                    maxTempHeight -= AppDisplay.getInstance(this.mContext).dp2px(1.5f);
                }
                if (this.mContentView.getMeasuredHeight() > maxTempHeight) {
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.height = getDialogHeight();
                    getWindow().setAttributes(params);
                    return;
                }
                LayoutParams contentViewRootParams = (LayoutParams) this.mContentViewRoot.getLayoutParams();
                contentViewRootParams.addRule(2, 0);
                this.mContentViewRoot.setLayoutParams(contentViewRootParams);
                LayoutParams buttonViewParams = (LayoutParams) this.mButtonsViewRoot.getLayoutParams();
                buttonViewParams.addRule(12, 0);
                buttonViewParams.addRule(3, R.id.dlg_contentview_root);
                this.mButtonsViewRoot.setLayoutParams(buttonViewParams);
            }
        }
    }

    public void setTitlePosition(TB_Position position) {
        if (position == TB_Position.Position_LT) {
            this.mTitleBar.removeItemByItem(this.mTitleItem);
            this.mTitleBar.addView(this.mTitleItem, TB_Position.Position_LT);
        } else if (position == TB_Position.Position_CENTER) {
            this.mTitleBar.removeItemByItem(this.mTitleItem);
            this.mTitleBar.addView(this.mTitleItem, TB_Position.Position_CENTER);
        } else if (position == TB_Position.Position_RB) {
            this.mTitleBar.removeItemByItem(this.mTitleItem);
            this.mTitleBar.addView(this.mTitleItem, TB_Position.Position_RB);
        } else {
            this.mTitleBar.removeItemByItem(this.mTitleItem);
            this.mTitleBar.addView(this.mTitleItem, TB_Position.Position_CENTER);
        }
    }

    public void setListener(DialogListener dialogListener) {
        this.mDialogListener = dialogListener;
    }

    public void setOnDLDismissListener(DismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }

    public int getDialogHeight() {
        return (this.mMetrics.heightPixels * 7) / 10;
    }
}
