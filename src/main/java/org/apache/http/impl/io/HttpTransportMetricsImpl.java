package org.apache.http.impl.io;

import org.apache.http.io.HttpTransportMetrics;

@Deprecated
public class HttpTransportMetricsImpl implements HttpTransportMetrics {
    public HttpTransportMetricsImpl() {
        throw new RuntimeException("Stub!");
    }

    public long getBytesTransferred() {
        throw new RuntimeException("Stub!");
    }

    public void setBytesTransferred(long count) {
        throw new RuntimeException("Stub!");
    }

    public void incrementBytesTransferred(long count) {
        throw new RuntimeException("Stub!");
    }

    public void reset() {
        throw new RuntimeException("Stub!");
    }
}
