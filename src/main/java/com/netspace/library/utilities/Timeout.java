package com.netspace.library.utilities;

import android.util.Log;

public class Timeout extends Thread {
    private static final String TAG = "Timeout";
    private OnTimeoutCallBack mCallBack;
    private boolean mbStop = false;
    private long mnTimeout = 0;

    public interface OnTimeoutCallBack {
        void onTimeout();
    }

    public Timeout(long nTimeoutSeconds) {
        this.mnTimeout = System.currentTimeMillis() + nTimeoutSeconds;
    }

    public void stopTimer() {
        this.mbStop = true;
    }

    public void setCallBack(OnTimeoutCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void run() {
        setName("Timeout work thread");
        boolean bTimeout = false;
        while (!this.mbStop) {
            if (System.currentTimeMillis() > this.mnTimeout) {
                bTimeout = true;
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (bTimeout) {
            Log.e(TAG, "Operation Time out.");
            if (this.mCallBack != null) {
                this.mCallBack.onTimeout();
            }
        }
    }
}
