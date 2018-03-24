package com.netspace.library.utilities.displayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import com.netspace.library.ui.StatusBarDisplayer;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.SimpleBackgroundTask;
import com.netspace.library.utilities.SimpleBackgroundTask.ProgressDisplayer;

public class NotifyBarDisplayer implements ProgressDisplayer, OnClickListener {
    private Context mContext;
    private StatusBarDisplayer mStatusBarDisplayer;
    private SimpleBackgroundTask mTaskObj;

    public void init(Context context, SimpleBackgroundTask taskObject) {
        this.mContext = context;
        this.mTaskObj = taskObject;
        this.mStatusBarDisplayer = new StatusBarDisplayer(context);
        this.mStatusBarDisplayer.setIcon(UI.mSynchronizeIcon);
        this.mStatusBarDisplayer.setTitle("正在处理");
        this.mStatusBarDisplayer.setText("正在处理，请稍候...");
        this.mStatusBarDisplayer.showProgressBox(this);
    }

    public void setMessage(String szMessage) {
        if (this.mStatusBarDisplayer != null) {
            this.mStatusBarDisplayer.setText(szMessage);
        }
    }

    public void setTitle(String szTitle) {
        if (this.mStatusBarDisplayer != null) {
            this.mStatusBarDisplayer.setTitle(szTitle);
        }
    }

    public void setMax(int nMax) {
        if (this.mStatusBarDisplayer != null) {
            this.mStatusBarDisplayer.setProgressMax(nMax);
        }
    }

    public void setProgress(int nProgress) {
        if (this.mStatusBarDisplayer != null) {
            this.mStatusBarDisplayer.setProgress(nProgress);
        }
    }

    public void increaseProgress() {
        if (this.mStatusBarDisplayer != null) {
            this.mStatusBarDisplayer.increaseProgress();
        }
    }

    public void dismiss() {
        if (this.mStatusBarDisplayer != null) {
            this.mStatusBarDisplayer.shutDown();
            this.mStatusBarDisplayer = null;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mTaskObj.cancel(true);
    }
}
