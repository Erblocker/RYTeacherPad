package com.foxit.sdk;

import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;

/* compiled from: Task */
class b extends Task {
    private PDFDoc a;

    protected b(d dVar, PDFDoc pDFDoc, CallBack callBack) {
        super(callBack);
        this.mDocManager = dVar;
        this.a = pDFDoc;
    }

    public String toString() {
        return "CloseDocumentTask";
    }

    protected boolean exeSuccess() {
        if (errorCode() != 0) {
            return false;
        }
        return super.exeSuccess();
    }

    protected boolean canCancel() {
        return false;
    }

    protected void prepare() {
    }

    protected void execute() {
        if (this.mStatus != 1) {
            this.mErr = 6;
            return;
        }
        this.mStatus = 2;
        try {
            this.a.release();
            this.mErr = 0;
            this.mStatus = 3;
        } catch (PDFException e) {
            this.mErr = e.getLastError();
            this.mStatus = -1;
        }
    }

    protected PDFDoc a() {
        return this.a;
    }
}
