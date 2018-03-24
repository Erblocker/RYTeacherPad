package com.foxit.sdk.common;

public class PDFException extends Exception {
    @Deprecated
    public static final int e_errCertificate = 5;
    @Deprecated
    public static final int e_errFile = 1;
    @Deprecated
    public static final int e_errFormat = 2;
    @Deprecated
    public static final int e_errHandler = 4;
    @Deprecated
    public static final int e_errInvalidLicense = 7;
    @Deprecated
    public static final int e_errNotParsed = 12;
    @Deprecated
    public static final int e_errOutOfMemory = 10;
    @Deprecated
    public static final int e_errParameter = 8;
    @Deprecated
    public static final int e_errPassword = 3;
    @Deprecated
    public static final int e_errSecurityHandler = 11;
    @Deprecated
    public static final int e_errSuccess = 0;
    @Deprecated
    public static final int e_errUnSupported = 9;
    @Deprecated
    public static final int e_errUnknown = 6;
    private static final long serialVersionUID = 1;
    private int a = 0;
    private String b = null;

    @Deprecated
    public PDFException(int i) {
        this.a = i;
        this.b = null;
    }

    public PDFException(PDFError pDFError) {
        this.a = pDFError.getCode();
        this.b = pDFError.getMessage();
    }

    public PDFException(int i, String str) {
        this.a = i;
        this.b = str;
    }

    public int getLastError() {
        return this.a;
    }

    public String getMessage() {
        return this.b != null ? this.b : PDFError.valueOf(Integer.valueOf(this.a)).getMessage();
    }

    public static String getErrorMessage(int i) {
        return PDFError.valueOf(Integer.valueOf(i)).getMessage();
    }
}
