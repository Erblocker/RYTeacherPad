package com.netspace.library.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import java.lang.ref.WeakReference;

public class UIDisplayer {
    protected static final String DISPLAYERNAME = "UIDisplayer";
    protected WeakReference<Activity> mActivity;
    protected boolean mCancelable = true;
    protected boolean mCancelled = false;
    protected int mIconResID = 0;
    protected ViewGroup mOverlappedLayout;
    protected int mOverlappedLayoutID = 0;
    protected int mProgressCurrent = 0;
    protected ProgressDialog mProgressDialog;
    protected int mProgressMax = 0;
    protected String mText;
    protected String mTitle;

    public UIDisplayer(Activity Activity) {
        this.mActivity = new WeakReference(Activity);
    }

    public String getDisplayName() {
        return DISPLAYERNAME;
    }

    public void setText(String szText) {
        this.mText = szText;
    }

    public String getText() {
        return this.mText;
    }

    public void setIcon(int nIconResID) {
        this.mIconResID = nIconResID;
    }

    public void setLayoutID(int nLayoutID) {
        this.mOverlappedLayoutID = nLayoutID;
    }

    public void setTitle(String szTitle) {
        this.mTitle = szTitle;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setProgress(int nPos) {
        this.mProgressCurrent = nPos;
        if (this.mProgressDialog != null) {
            this.mProgressDialog.setProgress(nPos);
        }
    }

    public void increaseProgress() {
        this.mProgressCurrent++;
        setProgress(this.mProgressCurrent);
    }

    public void setProgressMax(int nMax) {
        this.mProgressMax = nMax;
        if (this.mProgressDialog != null) {
            this.mProgressDialog.setMax(this.mProgressMax);
        }
    }

    public void smartRemainProgressHandle(int nRemain) {
        if (nRemain > this.mProgressMax) {
            if (this.mProgressMax == 0) {
                this.mProgressCurrent = 0;
                showProgressBox(null);
            }
            setProgressMax(nRemain);
        } else if (this.mProgressMax > 0) {
            setProgress(this.mProgressMax - nRemain);
        }
        if (nRemain == 0) {
            hideProgressBox();
            this.mProgressMax = 0;
        }
    }

    public boolean showAlertBox() {
        Activity CurrentActivity = (Activity) this.mActivity.get();
        if (CurrentActivity == null) {
            return false;
        }
        new Builder(CurrentActivity).setTitle(this.mTitle).setCancelable(true).setMessage(this.mText).setNegativeButton("确定", null).show();
        return true;
    }

    public boolean showProgressBox(final OnClickListener OnCancelClick) {
        Activity CurrentActivity = (Activity) this.mActivity.get();
        if (CurrentActivity == null) {
            return false;
        }
        this.mProgressDialog = new ProgressDialog(CurrentActivity);
        this.mProgressDialog.setMessage(this.mText);
        this.mProgressDialog.setTitle(this.mTitle);
        if (this.mProgressMax == 0) {
            this.mProgressDialog.setIndeterminate(true);
            this.mProgressDialog.setProgressStyle(0);
        } else {
            this.mProgressDialog.setIndeterminate(false);
            this.mProgressDialog.setMax(this.mProgressMax);
        }
        this.mProgressDialog.setCancelable(false);
        if (this.mCancelable) {
            this.mProgressDialog.setButton(-2, "取消", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    UIDisplayer.this.mCancelled = true;
                    if (OnCancelClick != null) {
                        OnCancelClick.onClick(dialog, whichButton);
                    }
                    dialog.cancel();
                    UIDisplayer.this.mProgressDialog = null;
                }
            });
        }
        this.mProgressDialog.show();
        return true;
    }

    public boolean showOverlappedLayout() {
        Activity CurrentActivity = (Activity) this.mActivity.get();
        if (CurrentActivity == null) {
            return false;
        }
        if (this.mOverlappedLayout == null) {
            this.mOverlappedLayout = (ViewGroup) CurrentActivity.getLayoutInflater().inflate(this.mOverlappedLayoutID, null);
            CurrentActivity.addContentView(this.mOverlappedLayout, new LayoutParams(-1, -1));
        }
        return true;
    }

    public boolean hideOverlappedLayout() {
        if (((Activity) this.mActivity.get()) == null || this.mOverlappedLayout == null) {
            return false;
        }
        this.mOverlappedLayout.setVisibility(8);
        ((ViewGroup) this.mOverlappedLayout.getParent()).removeView(this.mOverlappedLayout);
        this.mOverlappedLayout = null;
        return true;
    }

    public void hideProgressBox() {
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
            this.mProgressCurrent = 0;
        }
    }

    public void shutDown() {
        hideOverlappedLayout();
        hideProgressBox();
    }
}
