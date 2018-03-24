package com.foxit.uiextensions.security;

public interface ICertificateSupport {

    public static class CertificateInfo {
        public String emailAddress;
        public boolean expired;
        public String expiringDate;
        public String identity;
        public String issuer;
        public String issuerUniqueID;
        public boolean[] keyUsage;
        public String publisher;
        public String serialNumber;
        public String startDate;
        public int statusCode;
    }
}
