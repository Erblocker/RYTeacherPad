package com.foxit.uiextensions.security.digitalsignature;

import android.graphics.Bitmap;
import android.graphics.RectF;

public interface IDigitalSignatureUtil {
    void addCertList(IDigitalSignatureCallBack iDigitalSignatureCallBack);

    void addCertSignature(String str, String str2, Bitmap bitmap, RectF rectF, int i, IDigitalSignatureCreateCallBack iDigitalSignatureCreateCallBack);
}
