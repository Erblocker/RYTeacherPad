package com.foxit.uiextensions.controls.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIMarqueeTextView;

public class UIDialog {
    protected View mContentView;
    private Context mContext;
    protected Dialog mDialog;
    protected UIMarqueeTextView mTitleView = ((UIMarqueeTextView) this.mContentView.findViewById(R.id.fx_dialog_title));

    public UIDialog(Context context, int layoutId, int theme, int width) {
        this.mContext = context;
        this.mDialog = new Dialog(context, theme);
        this.mContentView = View.inflate(context, layoutId, null);
        this.mDialog.setContentView(this.mContentView, new LayoutParams(width, -2));
        this.mDialog.setCanceledOnTouchOutside(true);
        AppUtil.fixBackgroundRepeat(this.mContentView);
    }

    UIDialog(Context context, int layoutId, int theme) {
        this.mDialog = new Dialog(context, theme);
        this.mContentView = View.inflate(context, layoutId, null);
        this.mDialog.setContentView(this.mContentView, new LayoutParams(AppDisplay.getInstance(this.mContext).getDialogWidth(), -2));
        this.mDialog.setCanceledOnTouchOutside(true);
        AppUtil.fixBackgroundRepeat(this.mContentView);
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    public void setTitle(String title) {
        if (title != null) {
            this.mTitleView.setText(title);
        }
    }

    public void setTitle(int title) {
        this.mTitleView.setText(title);
    }

    public void show() {
        AppDialogManager.getInstance().showAllowManager(this.mDialog, null);
    }

    public void dismiss() {
        AppDialogManager.getInstance().dismiss(this.mDialog);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mDialog.setOnDismissListener(listener);
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.mDialog.setOnCancelListener(listener);
    }
}
