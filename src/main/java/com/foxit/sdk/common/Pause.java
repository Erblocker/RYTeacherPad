package com.foxit.sdk.common;

public abstract class Pause {
    private transient long swigCPtr;

    public abstract boolean needPauseNow();

    private synchronized void a() throws PDFException {
        if (this.swigCPtr != 0) {
            CommonJNI.Pause_release(this);
        }
        this.swigCPtr = 0;
    }

    public void release() throws PDFException {
        a();
    }
}
