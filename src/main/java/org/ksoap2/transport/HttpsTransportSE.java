package org.ksoap2.transport;

import java.io.IOException;
import java.net.Proxy;

public class HttpsTransportSE extends HttpTransportSE {
    static final String PROTOCOL = "https";
    private static final String PROTOCOL_FULL = "https://";
    protected final String file;
    protected final String host;
    protected final int port;

    public HttpsTransportSE(String host, int port, String file, int timeout) {
        super(new StringBuffer().append(PROTOCOL_FULL).append(host).append(":").append(port).append(file).toString(), timeout);
        this.host = host;
        this.port = port;
        this.file = file;
    }

    public HttpsTransportSE(Proxy proxy, String host, int port, String file, int timeout) {
        super(proxy, new StringBuffer().append(PROTOCOL_FULL).append(host).append(":").append(port).append(file).toString());
        this.host = host;
        this.port = port;
        this.file = file;
        this.timeout = timeout;
    }

    public ServiceConnection getServiceConnection() throws IOException {
        return new HttpsServiceConnectionSE(this.proxy, this.host, this.port, this.file, this.timeout);
    }
}
