package com.foxit.sdk;

import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;

/* compiled from: Task */
class o extends Task {
    private PDFDoc a;
    private String b;
    private int c;

    protected o(d dVar, PDFDoc pDFDoc, String str, int i, CallBack callBack) {
        super(callBack);
        this.mDocManager = dVar;
        this.a = pDFDoc;
        this.b = str;
        this.c = i;
    }

    protected PDFDoc a() {
        return this.a;
    }

    protected void execute() {
        if (this.mStatus == 1) {
            this.mStatus = 2;
            try {
                if (this.a.saveAs(this.b, (long) this.c)) {
                    this.mErr = 0;
                    this.mStatus = 3;
                    return;
                }
                this.mErr = 6;
                this.mStatus = -1;
            } catch (PDFException e) {
                this.mErr = e.getLastError();
                this.mStatus = -1;
            }
        }
    }
}
