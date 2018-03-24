package com.foxit.sdk.pdf.security;

public abstract class CertificateSecurityCallback extends a {
    public abstract String getPKCS12();

    public abstract byte[] getPasswordForPKCS12();

    public int getSecurityType() {
        return 2;
    }

    public void release() {
    }
}
