package com.foxit.sdk.pdf.form;

import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import com.foxit.sdk.pdf.PDFPage;

public abstract class FormFillerAssist {
    private int mElapse = 0;
    private Handler mHandler = null;
    private a mTimerThread = null;

    private class a implements Runnable {
        final /* synthetic */ FormFillerAssist a;
        private long b = 0;

        public a(FormFillerAssist formFillerAssist, long j) {
            this.a = formFillerAssist;
            this.b = j;
        }

        public void run() {
            FormsJNI.TimerFunc_callTimerFunc(this.b);
            this.a.mHandler.postDelayed(this, (long) this.a.mElapse);
        }
    }

    public abstract void refresh(PDFPage pDFPage, RectF rectF);

    public void release() {
    }

    public int getVersion() {
        return 1;
    }

    private void setTimer(long j, int i) {
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mTimerThread = new a(this, j);
        this.mElapse = i;
        this.mHandler.postDelayed(this.mTimerThread, (long) i);
    }

    private void killTimer() {
        this.mHandler.removeCallbacks(this.mTimerThread);
        this.mHandler = null;
        this.mTimerThread = null;
    }

    public void focusGotOnControl(FormControl formControl, String str) {
    }

    public void focusLostFromControl(FormControl formControl, String str) {
    }
}
