package com.foxit.sdk.pdf.security;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.SecurityHandler;
import java.io.File;

public class CertificateSecurityHandler extends SecurityHandler {
    private transient long a;

    protected CertificateSecurityHandler(long j, boolean z) {
        super(SecurityJNI.CertificateSecurityHandler_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(CertificateSecurityHandler certificateSecurityHandler) {
        return certificateSecurityHandler == null ? 0 : certificateSecurityHandler.a;
    }

    protected void finalize() {
        delete();
    }

    protected synchronized void delete() {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                SecurityJNI.delete_CertificateSecurityHandler(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public static CertificateSecurityHandler create() {
        long CertificateSecurityHandler_create = SecurityJNI.CertificateSecurityHandler_create();
        return CertificateSecurityHandler_create == 0 ? null : new CertificateSecurityHandler(CertificateSecurityHandler_create, false);
    }

    public boolean initialize(String[] strArr, int i, boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        } else if (strArr == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else {
            int i2 = 0;
            while (i2 < strArr.length) {
                if (new File(strArr[i2]).exists()) {
                    i2++;
                } else {
                    throw new PDFException(PDFError.FILE_ERROR);
                }
            }
            if (i >= 1 && i <= 2) {
                return SecurityJNI.CertificateSecurityHandler_initialize(this.a, this, strArr, i, z);
            }
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }
}
