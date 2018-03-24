package org.ksoap2.transport;

import java.io.IOException;
import org.apache.http.protocol.HTTP;

public class HttpsServiceConnectionSEIgnoringConnectionClose extends HttpsServiceConnectionSE {
    public HttpsServiceConnectionSEIgnoringConnectionClose(String host, int port, String file, int timeout) throws IOException {
        super(host, port, file, timeout);
    }

    public void setRequestProperty(String key, String value) {
        if (!HTTP.CONN_DIRECTIVE.equalsIgnoreCase(key) || !"close".equalsIgnoreCase(value)) {
            super.setRequestProperty(key, value);
        }
    }
}
