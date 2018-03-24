package com.foxit.sdk.pdf;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.security.SecurityJNI;

public class SecurityHandler {
    private transient long a;
    protected transient boolean swigCMemOwn;

    protected SecurityHandler(long j, boolean z) {
        this.swigCMemOwn = z;
        this.a = j;
    }

    protected static long getCPtr(SecurityHandler securityHandler) {
        return securityHandler == null ? 0 : securityHandler.a;
    }

    protected synchronized void delete() {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                SecurityJNI.delete_SecurityHandler(this.a);
            }
            this.a = 0;
        }
    }

    public int getSecurityType() throws PDFException {
        if (this.a != 0) {
            return SecurityJNI.SecurityHandler_getSecurityType(this.a, this);
        }
        throw new PDFException(PDFError.HANDLER_ERROR);
    }

    public void release() throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        SecurityJNI.SecurityHandler_release(this.a, this);
        this.a = 0;
    }
}
