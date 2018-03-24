package com.foxit.uiextensions.security.certificate;

import com.foxit.uiextensions.security.ICertificateSupport.CertificateInfo;

public class CertificateFileInfo {
    public CertificateInfo certificateInfo;
    public String fileName;
    public String filePath;
    public boolean isCertFile;
    public String issuer;
    public String password;
    public int permCode;
    public String publisher;
    public int radioButtonID;
    public boolean selected;
    public String serialNumber;

    public boolean equals(Object o) {
        if (o == null || !(o instanceof CertificateFileInfo)) {
            return false;
        }
        CertificateFileInfo fi = (CertificateFileInfo) o;
        if (fi.isCertFile != this.isCertFile || this.filePath == null) {
            return false;
        }
        return this.filePath.equals(fi.filePath);
    }
}
