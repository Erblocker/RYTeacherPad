package com.foxit.sdk.pdf.signature;

import android.graphics.Bitmap;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.Pause;

class SignaturesJNI {
    public static final native long Signature_SWIGUpcast(long j);

    public static final native boolean Signature_clearSignedData(long j, Signature signature);

    public static final native int Signature_continueSign(long j, Signature signature);

    public static final native int Signature_continueVerify(long j, Signature signature);

    public static final native long Signature_getAppearanceFlags(long j, Signature signature);

    public static final native Bitmap Signature_getBitmap(long j, Signature signature);

    public static final native int[] Signature_getByteRanges(long j, Signature signature);

    public static final native String Signature_getCertificateInfo(long j, Signature signature, String str);

    public static final native long Signature_getDocument(long j, Signature signature);

    public static final native String Signature_getKeyValue(long j, Signature signature, int i);

    public static final native long Signature_getSignatureDict(long j, Signature signature);

    public static final native DateTime Signature_getSigningTime(long j, Signature signature);

    public static final native long Signature_getState(long j, Signature signature);

    public static final native boolean Signature_isSigned(long j, Signature signature);

    public static final native void Signature_setAppearanceContent(long j, Signature signature, String str);

    public static final native void Signature_setAppearanceFlags(long j, Signature signature, long j2);

    public static final native void Signature_setBitmap(long j, Signature signature, Bitmap bitmap);

    public static final native void Signature_setKeyValue(long j, Signature signature, int i, String str);

    public static final native void Signature_setSigningTime(long j, Signature signature, DateTime dateTime);

    public static final native int Signature_startSign(long j, Signature signature, String str, String str2, byte[] bArr, int i, Pause pause, Object obj);

    public static final native int Signature_startVerify(long j, Signature signature, Pause pause, Object obj);

    public static final native void delete_Signature(long j);
}
