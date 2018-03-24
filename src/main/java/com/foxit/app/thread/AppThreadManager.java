package com.foxit.app.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class AppThreadManager {
    private Handler mMainThreadHandler;

    public Handler getMainThreadHandler() {
        if (this.mMainThreadHandler == null) {
            this.mMainThreadHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Runnable runnable = msg.obj;
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            };
        }
        return this.mMainThreadHandler;
    }

    public void startThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    public void startThread(AppAsyncTask task, Object... params) {
        task.execute(params);
    }
}
