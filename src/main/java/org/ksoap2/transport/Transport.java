package org.ksoap2.transport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import org.ksoap2.SoapEnvelope;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class Transport {
    protected static final String CONTENT_TYPE_SOAP_XML_CHARSET_UTF_8 = "application/soap+xml;charset=utf-8";
    protected static final String CONTENT_TYPE_XML_CHARSET_UTF_8 = "text/xml;charset=utf-8";
    protected static final String USER_AGENT = "ksoap2-android/2.6.0+";
    private int bufferLength;
    public boolean debug;
    protected Proxy proxy;
    public String requestDump;
    public String responseDump;
    protected int timeout;
    protected String url;
    private String xmlVersionTag;

    public abstract List call(String str, SoapEnvelope soapEnvelope, List list) throws IOException, XmlPullParserException;

    public abstract List call(String str, SoapEnvelope soapEnvelope, List list, File file) throws IOException, XmlPullParserException;

    public abstract ServiceConnection getServiceConnection() throws IOException;

    public Transport() {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
    }

    public Transport(String url) {
        this(null, url);
    }

    public Transport(String url, int timeout) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.url = url;
        this.timeout = timeout;
    }

    public Transport(String url, int timeout, int bufferLength) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.url = url;
        this.timeout = timeout;
        this.bufferLength = bufferLength;
    }

    public Transport(Proxy proxy, String url) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.proxy = proxy;
        this.url = url;
    }

    public Transport(Proxy proxy, String url, int timeout) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.proxy = proxy;
        this.url = url;
        this.timeout = timeout;
    }

    public Transport(Proxy proxy, String url, int timeout, int bufferLength) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.proxy = proxy;
        this.url = url;
        this.timeout = timeout;
        this.bufferLength = bufferLength;
    }

    protected void parseResponse(SoapEnvelope envelope, InputStream is) throws XmlPullParserException, IOException {
        XmlPullParser xp = new KXmlParser();
        xp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        xp.setInput(is, null);
        envelope.parse(xp);
        is.close();
    }

    protected byte[] createRequestData(SoapEnvelope envelope, String encoding) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(this.bufferLength);
        bos.write(this.xmlVersionTag.getBytes());
        XmlSerializer xw = new KXmlSerializer();
        xw.setOutput(bos, encoding);
        envelope.write(xw);
        xw.flush();
        bos.write(13);
        bos.write(10);
        bos.flush();
        return bos.toByteArray();
    }

    protected byte[] createRequestData(SoapEnvelope envelope) throws IOException {
        return createRequestData(envelope, null);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setXmlVersionTag(String tag) {
        this.xmlVersionTag = tag;
    }

    public void reset() {
    }

    public void call(String soapAction, SoapEnvelope envelope) throws IOException, XmlPullParserException {
        call(soapAction, envelope, null);
    }

    public String getHost() throws MalformedURLException {
        return new URL(this.url).getHost();
    }

    public int getPort() throws MalformedURLException {
        return new URL(this.url).getPort();
    }

    public String getPath() throws MalformedURLException {
        return new URL(this.url).getPath();
    }
}
