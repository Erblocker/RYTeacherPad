package com.foxit.sdk;

import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFPage;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Task {
    protected static final int PRIORITY_BACKGROUND = 1;
    protected static final int PRIORITY_COUNT = 7;
    protected static final int PRIORITY_LOWEST = 0;
    protected static final int PRIORITY_MODIFY = 6;
    protected static final int PRIORITY_NORMAL = 2;
    protected static final int PRIORITY_PATCH = 3;
    protected static final int PRIORITY_THUMBNAIL = 4;
    protected static final int PRIORITY_URGENT = 5;
    protected static final int STATUS_ERROR = -1;
    protected static final int STATUS_FINISHED = 3;
    protected static final int STATUS_OOM = -2;
    protected static final int STATUS_REDAY = 1;
    protected static final int STATUS_RUNNING = 2;
    protected ArrayList<CallBack> mAttachedCallbacks;
    protected CallBack mCallBack = null;
    protected boolean mCanceled = false;
    protected d mDocManager = null;
    protected int mErr = 0;
    protected int mPriority = 2;
    protected int mStatus = 1;
    protected int mThreadPriority = -1;

    public interface CallBack {
        void result(Task task);
    }

    protected abstract void execute();

    public Task(CallBack callBack) {
        this.mCallBack = callBack;
    }

    protected void reinit() {
        this.mStatus = 1;
        this.mErr = 0;
    }

    public String toString() {
        return "Task";
    }

    protected int getPriority() {
        return this.mPriority;
    }

    protected void setPriority(int i) {
        this.mPriority = i;
    }

    protected int getThreadPriority() {
        if (this.mThreadPriority >= 0) {
            return this.mThreadPriority;
        }
        switch (this.mPriority) {
            case 0:
                return 19;
            case 1:
                return 10;
            default:
                return 0;
        }
    }

    protected void setThreadPriority(int i) {
        this.mThreadPriority = i;
    }

    protected boolean canCancel() {
        return true;
    }

    protected void cancel() {
        this.mCanceled = true;
    }

    protected boolean isCanceled() {
        return this.mCanceled;
    }

    protected boolean isModify() {
        return false;
    }

    protected int getStatus() {
        return this.mStatus;
    }

    protected boolean exeSuccess() {
        return this.mStatus == 3;
    }

    protected int errorCode() {
        return this.mErr;
    }

    protected int extErrorCode() {
        return this.mErr;
    }

    protected void attachCallback(CallBack callBack) {
        if (this.mAttachedCallbacks == null) {
            this.mAttachedCallbacks = new ArrayList();
        }
        this.mAttachedCallbacks.add(callBack);
    }

    protected void deliverCallbacks(Task task) {
        if (this.mAttachedCallbacks != null) {
            Iterator it = this.mAttachedCallbacks.iterator();
            while (it.hasNext()) {
                task.attachCallback((CallBack) it.next());
            }
            this.mAttachedCallbacks.clear();
        }
    }

    protected void prepare() {
    }

    protected void finish() {
        if (this.mCallBack != null) {
            this.mCallBack.result(this);
        }
    }

    protected PDFPage getPage(PDFDoc pDFDoc, int i) throws PDFException {
        PDFPage page = pDFDoc.getPage(i);
        if (!page.isParsed()) {
            int startParse = page.startParse(0, null, false);
            while (startParse == 1) {
                startParse = page.continueParse();
            }
            if (startParse == 0) {
                return null;
            }
        }
        return page;
    }

    protected void closePage(PDFDoc pDFDoc, int i) throws PDFException {
        pDFDoc.closePage(i);
    }
}
