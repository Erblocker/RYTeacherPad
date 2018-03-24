package com.netspace.library.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import com.netspace.library.utilities.displayer.NotifyBarDisplayer;
import com.netspace.library.utilities.displayer.ProgressDialogDisplayer;

public class SimpleBackgroundTask extends AsyncTask<String, Integer, String> {
    private BackgroundTaskExecuteCallBack mCallBack;
    private Context mContext;
    private ProgressDisplayer mProgressDisplayer;
    private boolean mbAutoRecreate = false;
    private volatile boolean mbWorking = false;

    public interface BackgroundTaskExecuteCallBack {
        void onCancel();

        void onComplete();

        void onExecute(SimpleBackgroundTask simpleBackgroundTask, String... strArr);

        void onNewInstance(SimpleBackgroundTask simpleBackgroundTask);
    }

    public interface ProgressDisplayer {
        void dismiss();

        void increaseProgress();

        void init(Context context, SimpleBackgroundTask simpleBackgroundTask);

        void setMax(int i);

        void setMessage(String str);

        void setProgress(int i);

        void setTitle(String str);
    }

    public SimpleBackgroundTask(Context context, ProgressDisplayer progressDisplayer, boolean bAutoCreate, BackgroundTaskExecuteCallBack backgroundTaskExecuteCallBack) {
        this.mContext = context;
        this.mCallBack = backgroundTaskExecuteCallBack;
        this.mProgressDisplayer = progressDisplayer;
        this.mbAutoRecreate = bAutoCreate;
    }

    public static NotifyBarDisplayer getNotifyBarDisplayer() {
        return new NotifyBarDisplayer();
    }

    public static ProgressDialogDisplayer getProgressDialogDisplayer() {
        return new ProgressDialogDisplayer();
    }

    public void setProgressTitle(String szTitle) {
        this.mProgressDisplayer.setTitle(szTitle);
    }

    public void setProgressMessage(String szText) {
        this.mProgressDisplayer.setMessage(szText);
    }

    public void setProgressMax(int nMax) {
        this.mProgressDisplayer.setMax(nMax);
    }

    public void setProgress(int nProgress) {
        this.mProgressDisplayer.setProgress(nProgress);
    }

    public void increaseProgress() {
        this.mProgressDisplayer.increaseProgress();
    }

    public boolean isRunning() {
        if (this.mbWorking || getStatus() == Status.RUNNING) {
            return true;
        }
        return false;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        if (this.mProgressDisplayer != null) {
            this.mProgressDisplayer.init(this.mContext, this);
        }
    }

    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        if (this.mProgressDisplayer != null) {
            this.mProgressDisplayer.setProgress(progress[0].intValue());
        }
    }

    protected String doInBackground(String... params) {
        this.mbWorking = true;
        this.mCallBack.onExecute(this, params);
        this.mbWorking = false;
        if (this.mProgressDisplayer != null) {
            this.mProgressDisplayer.dismiss();
        }
        if (isCancelled()) {
            Utilities.runOnUIThread(this.mContext, new Runnable() {
                public void run() {
                    SimpleBackgroundTask.this.mCallBack.onCancel();
                }
            });
        } else {
            Utilities.runOnUIThread(this.mContext, new Runnable() {
                public void run() {
                    SimpleBackgroundTask.this.mCallBack.onComplete();
                }
            });
        }
        if (this.mbAutoRecreate) {
            this.mCallBack.onNewInstance(new SimpleBackgroundTask(this.mContext, this.mProgressDisplayer, this.mbAutoRecreate, this.mCallBack));
        }
        return null;
    }

    protected void onCancelled() {
        super.onCancelled();
        if (this.mProgressDisplayer != null) {
            this.mProgressDisplayer.dismiss();
        }
    }
}
