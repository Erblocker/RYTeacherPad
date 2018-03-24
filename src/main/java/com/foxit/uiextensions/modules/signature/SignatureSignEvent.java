package com.foxit.uiextensions.modules.signature;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.uiextensions.modules.signature.SignatureEvent.ISignatureCallBack;

/* compiled from: SignatureEvent */
class SignatureSignEvent extends SignatureEvent {
    public Bitmap mBitmap;
    public ISignatureCallBack mCallBack;
    public PDFPage mPage;
    public RectF mRect;

    public SignatureSignEvent(PDFPage page, Bitmap bitmap, RectF rect, int type, ISignatureCallBack callback) {
        this.mType = type;
        this.mCallBack = callback;
        this.mPage = page;
        this.mBitmap = bitmap;
        this.mRect = rect;
    }
}
