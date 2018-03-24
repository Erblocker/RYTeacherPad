package org.ksoap2.transport;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.protocol.HTTP;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.xmlpull.v1.XmlPullParserException;

public class KeepAliveHttpTransportSE extends HttpTransportSE {
    public KeepAliveHttpTransportSE(String url) {
        super(null, url);
    }

    public KeepAliveHttpTransportSE(Proxy proxy, String url) {
        super(proxy, url);
    }

    public KeepAliveHttpTransportSE(String url, int timeout) {
        super(url, timeout);
    }

    public KeepAliveHttpTransportSE(Proxy proxy, String url, int timeout) {
        super(proxy, url, timeout);
    }

    public KeepAliveHttpTransportSE(String url, int timeout, int contentLength) {
        super(url, timeout);
    }

    public KeepAliveHttpTransportSE(Proxy proxy, String url, int timeout, int contentLength) {
        super(proxy, url, timeout);
    }

    public List call(String soapAction, SoapEnvelope envelope, List headers) throws IOException, XmlPullParserException {
        if (headers == null) {
            headers = new ArrayList();
        }
        HeaderProperty ref = getHeader(headers, HTTP.CONN_DIRECTIVE);
        if (ref == null) {
            headers.add(new HeaderProperty(HTTP.CONN_DIRECTIVE, "keep-alive"));
        } else {
            ref.setValue("keep-alive");
        }
        return super.call(soapAction, envelope, headers);
    }

    protected HeaderProperty getHeader(List lista, String key) {
        if (lista == null) {
            return null;
        }
        for (int i = 0; i < lista.size(); i++) {
            HeaderProperty hp = (HeaderProperty) lista.get(i);
            if (key.equals(hp.getKey())) {
                return hp;
            }
        }
        return null;
    }
}
