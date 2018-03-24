package com.foxit.sdk;

import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;

/* compiled from: Task */
class h extends Task {
    protected int a;
    private String b = null;
    private byte[] c = null;
    private PDFDoc d = null;
    private byte[] e = null;

    protected h(d dVar, String str, byte[] bArr, CallBack callBack) {
        super(callBack);
        this.mDocManager = dVar;
        this.b = str;
        if (bArr == null) {
            this.c = null;
            return;
        }
        this.c = new byte[bArr.length];
        System.arraycopy(bArr, 0, this.c, 0, bArr.length);
    }

    protected h(d dVar, byte[] bArr, byte[] bArr2, CallBack callBack) {
        super(callBack);
        this.mDocManager = dVar;
        this.e = bArr;
        if (bArr2 == null) {
            this.c = null;
            return;
        }
        this.c = new byte[bArr2.length];
        System.arraycopy(bArr2, 0, this.c, 0, bArr2.length);
    }

    public String toString() {
        return "OpenDocumentTask";
    }

    protected boolean canCancel() {
        return false;
    }

    protected void prepare() {
    }

    protected void execute() {
        if (this.mStatus == 1) {
            this.mStatus = 2;
            try {
                if (this.d != null) {
                    this.d.release();
                }
                if (this.b != null) {
                    this.d = PDFDoc.createFromFilePath(this.b);
                } else if (this.e != null) {
                    this.d = PDFDoc.createFromMemory(this.e);
                }
                if (this.d == null) {
                    this.mErr = 6;
                    this.mStatus = -1;
                    if (this.mErr != 0 && this.d != null) {
                        try {
                            this.d.release();
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                        this.d = null;
                        return;
                    }
                    return;
                }
                this.d.load(this.c);
                this.a = this.d.getPageCount();
                if (this.a > 0) {
                    this.mStatus = 3;
                } else {
                    this.mStatus = -1;
                }
                this.mErr = 0;
                if (this.mErr != 0 && this.d != null) {
                    try {
                        this.d.release();
                    } catch (PDFException e2) {
                        e2.printStackTrace();
                    }
                    this.d = null;
                }
            } catch (PDFException e22) {
                this.mErr = e22.getLastError();
                this.mStatus = -1;
                if (this.mErr != 0 && this.d != null) {
                    try {
                        this.d.release();
                    } catch (PDFException e222) {
                        e222.printStackTrace();
                    }
                    this.d = null;
                }
            } catch (Throwable th) {
                if (!(this.mErr == 0 || this.d == null)) {
                    try {
                        this.d.release();
                    } catch (PDFException e3) {
                        e3.printStackTrace();
                    }
                    this.d = null;
                }
            }
        }
    }

    protected PDFDoc a() {
        return this.d;
    }
}
