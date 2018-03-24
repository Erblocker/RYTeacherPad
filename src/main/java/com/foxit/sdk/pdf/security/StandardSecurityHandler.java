package com.foxit.sdk.pdf.security;

import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.SecurityHandler;

public class StandardSecurityHandler extends SecurityHandler {
    private transient long a;

    protected StandardSecurityHandler(long j, boolean z) {
        super(SecurityJNI.StdSecurityHandler_SWIGUpcast(j), z);
        this.a = j;
    }

    protected static long getCPtr(StandardSecurityHandler standardSecurityHandler) {
        return standardSecurityHandler == null ? 0 : standardSecurityHandler.a;
    }

    protected synchronized void delete() {
        if (this.a != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                SecurityJNI.delete_StdSecurityHandler(this.a);
            }
            this.a = 0;
        }
        super.delete();
    }

    public static StandardSecurityHandler create() {
        long StdSecurityHandler_create = SecurityJNI.StdSecurityHandler_create();
        return StdSecurityHandler_create == 0 ? null : new StandardSecurityHandler(StdSecurityHandler_create, false);
    }

    public boolean initialize(long j, byte[] bArr, byte[] bArr2, int i, int i2, boolean z) throws PDFException {
        if (this.a == 0) {
            throw new PDFException(PDFError.HANDLER_ERROR);
        }
        a(bArr, bArr2, i, i2);
        return SecurityJNI.StdSecurityHandler_initialize(this.a, this, j, bArr, bArr2, i, i2, z);
    }

    private void a(byte[] bArr, byte[] bArr2, int i, int i2) throws PDFException {
        if (bArr == null && bArr2 == null) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (i <= 0 || i > 2) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (i == 1 && i2 != 16) {
            throw new PDFException(PDFError.PARAM_INVALID);
        } else if (i == 2 && i2 != 16 && i2 != 32) {
            throw new PDFException(PDFError.PARAM_INVALID);
        }
    }
}
