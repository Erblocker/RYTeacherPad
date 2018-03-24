package com.foxit.sdk.pdf.security;

import com.foxit.sdk.pdf.SecurityHandler;

public class SecurityJNI {
    public static final native long CertificateSecurityHandler_SWIGUpcast(long j);

    public static final native long CertificateSecurityHandler_create();

    public static final native boolean CertificateSecurityHandler_initialize(long j, CertificateSecurityHandler certificateSecurityHandler, String[] strArr, int i, boolean z);

    public static final native int SecurityHandler_getSecurityType(long j, SecurityHandler securityHandler);

    public static final native void SecurityHandler_release(long j, SecurityHandler securityHandler);

    public static final native long StdSecurityHandler_SWIGUpcast(long j);

    public static final native long StdSecurityHandler_create();

    public static final native boolean StdSecurityHandler_initialize(long j, StandardSecurityHandler standardSecurityHandler, long j2, byte[] bArr, byte[] bArr2, int i, int i2, boolean z);

    public static final native void delete_CertificateSecurityHandler(long j);

    public static final native void delete_SecurityHandler(long j);

    public static final native void delete_StdSecurityHandler(long j);
}
