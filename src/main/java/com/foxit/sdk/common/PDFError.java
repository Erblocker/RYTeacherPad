package com.foxit.sdk.common;

public enum PDFError {
    NO_ERROR(0, "No error."),
    FILE_ERROR(1, "File error: not found or could not be opened."),
    FORMAT_ERROR(2, "Format error: invalid format or file corrupted."),
    PASSWORD_INVALID(3, "Invalid password. Please input again."),
    HANDLER_ERROR(4, "Handler error."),
    CERTIFICATE_ERROR(5, "This document is encrypted by digital certificate and current user doesn't have correct certificate."),
    UNKNOWN_ERROR(6, "Unknown error."),
    LICENSE_INVALID(7, "Invalid license."),
    PARAM_INVALID(8, "Invalid input parameters."),
    UNSUPPORTED(9, "Some types are unsupported."),
    OOM(10, "Out of memory error."),
    SECURITY_HANDLE_ERROR(11, "Security handler error: PDF document is encrypted by some unsupported security handler."),
    NOT_PARSED_ERROR(12, "Not parsed error: content has not been parsed yet. Usually, this represents PDF page has not been parsed yet."),
    DATA_NOT_FOUND(13, "Data cannot be found."),
    INVALID_OBJECT_TYPE(14, "Invalid object type."),
    DATA_CONFLICT(15, "Data or values conflict.");
    
    private final int code;
    private String message;

    private PDFError(int i, String str) {
        this.code = i;
        this.message = str;
    }

    public static PDFError valueOf(Integer num) {
        for (PDFError pDFError : values()) {
            if (num.equals(Integer.valueOf(pDFError.getCode()))) {
                return pDFError;
            }
        }
        throw new IllegalArgumentException("No matching constant for [ " + num + "]");
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
