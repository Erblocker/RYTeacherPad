package com.netspace.library.utilities.displayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.netspace.library.utilities.SimpleBackgroundTask;
import com.netspace.library.utilities.SimpleBackgroundTask.ProgressDisplayer;

public class ProgressDialogDisplayer implements ProgressDisplayer {
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private SimpleBackgroundTask mTaskObj;

    public void init(Context context, SimpleBackgroundTask taskObject) {
        this.mContext = context;
        this.mTaskObj = taskObject;
        this.mProgressDialog = new ProgressDialog(this.mContext);
        this.mProgressDialog.setMessage("请稍候...");
        this.mProgressDialog.setTitle("正在处理");
        this.mProgressDialog.setIndeterminate(false);
        this.mProgressDialog.setProgressStyle(1);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setButton(-2, "取消", new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ProgressDialogDisplayer.this.mTaskObj.cancel(true);
            }
        });
        this.mProgressDialog.show();
    }

    public void setMessage(String szMessage) {
        this.mProgressDialog.setMessage(szMessage);
    }

    public void setTitle(String szTitle) {
        this.mProgressDialog.setTitle(szTitle);
    }

    public void setMax(int nMax) {
        this.mProgressDialog.setMax(nMax);
    }

    public void setProgress(int nProgress) {
        this.mProgressDialog.setProgress(nProgress);
    }

    public void increaseProgress() {
        this.mProgressDialog.incrementProgressBy(1);
    }

    public void dismiss() {
        this.mProgressDialog.dismiss();
    }
}
