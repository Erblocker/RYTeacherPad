package org.ksoap2.transport;

import java.io.IOException;

public class HttpResponseException extends IOException {
    private int statusCode;

    public HttpResponseException(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpResponseException(String detailMessage, int statusCode) {
        super(detailMessage);
        this.statusCode = statusCode;
    }

    public HttpResponseException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpResponseException(Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
