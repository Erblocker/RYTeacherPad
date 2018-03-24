package com.foxit.sdk.pdf.signature;

import android.graphics.Bitmap;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.form.FormControl;
import com.foxit.sdk.pdf.objects.PDFDictionary;

public class Signature extends FormControl {
    public static final int e_digestSHA1 = 0;
    public static final int e_digestSHA256 = 1;
    public static final int e_digestSHA384 = 2;
    public static final int e_digestSHA512 = 3;
    public static final int e_signatureAPFlagBitmap = 128;
    public static final int e_signatureAPFlagDN = 16;
    public static final int e_signatureAPFlagFoxitFlag = 1;
    public static final int e_signatureAPFlagLabel = 2;
    public static final int e_signatureAPFlagLocation = 32;
    public static final int e_signatureAPFlagReason = 4;
    public static final int e_signatureAPFlagSigner = 64;
    public static final int e_signatureAPFlagSigningTime = 8;
    public static final int e_signatureAPFlagText = 256;
    public static final int e_signatureKeyNameContactInfo = 3;
    public static final int e_signatureKeyNameDN = 4;
    public static final int e_signatureKeyNameFilter = 6;
    public static final int e_signatureKeyNameLocation = 1;
    public static final int e_signatureKeyNameReason = 2;
    public static final int e_signatureKeyNameSigner = 0;
    public static final int e_signatureKeyNameSubFilter = 7;
    public static final int e_signatureKeyNameText = 5;
    public static final int e_signatureStateNoSignData = 512;
    public static final int e_signatureStateSigned = 2;
    public static final int e_signatureStateUnknown = 0;
    public static final int e_signatureStateUnsigned = 1;
    public static final int e_signatureStateVerifyChange = 128;
    public static final int e_signatureStateVerifyErrorByteRange = 64;
    public static final int e_signatureStateVerifyErrorData = 16;
    public static final int e_signatureStateVerifyIncredible = 256;
    public static final int e_signatureStateVerifyInvalid = 8;
    public static final int e_signatureStateVerifyIssueCurrent = 131072;
    public static final int e_signatureStateVerifyIssueExpire = 32768;
    public static final int e_signatureStateVerifyIssueRevoke = 16384;
    public static final int e_signatureStateVerifyIssueUncheck = 65536;
    public static final int e_signatureStateVerifyIssueUnknown = 8192;
    public static final int e_signatureStateVerifyIssueValid = 4096;
    public static final int e_signatureStateVerifyNoSupportWay = 32;
    public static final int e_signatureStateVerifyValid = 4;
    private transient long a;

    protected Signature(long j, boolean z) throws PDFException {
        super(SignaturesJNI.Signature_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(Signature signature) {
        return signature == null ? 0 : signature.a;
    }

    protected synchronized void resetHandle() {
        this.a = 0;
        super.resetHandle();
    }

    protected synchronized void delete() throws PDFException {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                SignaturesJNI.delete_Signature(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public boolean isSigned() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_isSigned(this.a, this);
        }
        throw new PDFException(4);
    }

    public int startSign(String str, String str2, byte[] bArr, int i, Pause pause, Object obj) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (str2 == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (i >= 0 && i <= 3) {
            return SignaturesJNI.Signature_startSign(this.a, this, str, str2, bArr, i, pause, obj);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public int continueSign() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_continueSign(this.a, this);
        }
        throw new PDFException(4);
    }

    public int startVerify(Pause pause, Object obj) throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_startVerify(this.a, this, pause, obj);
        }
        throw new PDFException(4);
    }

    public int continueVerify() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_continueVerify(this.a, this);
        }
        throw new PDFException(4);
    }

    public String getCertificateInfo(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str != null) {
            return SignaturesJNI.Signature_getCertificateInfo(this.a, this, str);
        } else {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }

    public int[] getByteRanges() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_getByteRanges(this.a, this);
        }
        throw new PDFException(4);
    }

    public long getState() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_getState(this.a, this);
        }
        throw new PDFException(4);
    }

    public boolean clearSignedData() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_clearSignedData(this.a, this);
        }
        throw new PDFException(4);
    }

    public PDFDoc getDocument() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long Signature_getDocument = SignaturesJNI.Signature_getDocument(this.a, this);
        return Signature_getDocument == 0 ? null : (PDFDoc) a.a(PDFDoc.class, Signature_getDocument, false);
    }

    public long getAppearanceFlags() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_getAppearanceFlags(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setAppearanceFlags(long j) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        SignaturesJNI.Signature_setAppearanceFlags(this.a, this, j);
    }

    public DateTime getSigningTime() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_getSigningTime(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setSigningTime(DateTime dateTime) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (dateTime == null) {
            throw new PDFException(8);
        } else {
            SignaturesJNI.Signature_setSigningTime(this.a, this, dateTime);
        }
    }

    public String getKeyValue(int i) throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_getKeyValue(this.a, this, i);
        }
        throw new PDFException(4);
    }

    public void setKeyValue(int i, String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null || i < 0 || i > 7) {
            throw new PDFException(8);
        } else {
            SignaturesJNI.Signature_setKeyValue(this.a, this, i, str);
        }
    }

    public Bitmap getBitmap() throws PDFException {
        if (this.a != 0) {
            return SignaturesJNI.Signature_getBitmap(this.a, this);
        }
        throw new PDFException(4);
    }

    public void setBitmap(Bitmap bitmap) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (bitmap == null) {
            throw new PDFException(8);
        } else {
            SignaturesJNI.Signature_setBitmap(this.a, this, bitmap);
        }
    }

    public PDFDictionary getSignatureDict() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        }
        long Signature_getSignatureDict = SignaturesJNI.Signature_getSignatureDict(this.a, this);
        return Signature_getSignatureDict == 0 ? null : (PDFDictionary) a.a(PDFDictionary.class, Signature_getSignatureDict, false);
    }

    public void setAppearanceContent(String str) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(4);
        } else if (str == null) {
            throw new PDFException(8);
        } else {
            SignaturesJNI.Signature_setAppearanceContent(this.a, this, str);
        }
    }
}
