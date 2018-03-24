package com.foxit.uiextensions.modules.signature;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.foxit.uiextensions.modules.signature.SignatureEvent.ISignatureCallBack;

/* compiled from: SignatureEvent */
class SignatureDrawEvent extends SignatureEvent {
    public Bitmap mBitmap;
    public ISignatureCallBack mCallBack;
    public int mColor;
    public int mHeight;
    public RectF mRect;
    public float mThickness;
    public int mWidth;

    public SignatureDrawEvent(Bitmap bitmap, int type, int color, float thickness, ISignatureCallBack callBack) {
        this.mType = type;
        this.mBitmap = bitmap;
        this.mWidth = bitmap.getWidth();
        this.mHeight = bitmap.getHeight();
        this.mColor = color;
        this.mThickness = thickness;
        this.mCallBack = callBack;
        this.mRect = new RectF();
    }
}
