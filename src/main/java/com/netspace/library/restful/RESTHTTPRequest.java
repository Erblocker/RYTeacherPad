package com.netspace.library.restful;

import com.netspace.library.utilities.Utilities;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;

public class RESTHTTPRequest {
    private int mnPort;
    private String mszHost;
    private String mszMethod = HttpGet.METHOD_NAME;
    private String mszPath = "";
    private String mszProtocol = HttpHost.DEFAULT_SCHEME_NAME;

    public RESTHTTPRequest(RESTService service, String szURI) {
    }

    public RESTHTTPRequest(RESTService service, RESTRequest parentRequest, String szURI) {
    }

    public void setHost(String szHost, int nPort) {
        this.mszHost = szHost;
        this.mnPort = nPort;
    }

    public void setHost(String szUrl) {
        try {
            URL url = new URL(szUrl);
            this.mszHost = url.getHost();
            this.mnPort = url.getPort();
            this.mszPath = url.getPath();
        } catch (MalformedURLException e) {
            int nPos = szUrl.indexOf("/");
            if (nPos != -1) {
                this.mszHost = szUrl.substring(nPos);
                szUrl = szUrl.substring(0, nPos);
            }
            nPos = szUrl.indexOf(":");
            if (nPos == -1) {
                this.mszHost = szUrl;
                this.mnPort = 80;
                return;
            }
            this.mszHost = szUrl.substring(0, nPos);
            this.mnPort = Utilities.toInt(szUrl.substring(nPos + 1));
        }
    }

    public String getHostWithPort() {
        return this.mszHost + ":" + String.valueOf(this.mnPort);
    }

    public String getMethod() {
        return this.mszMethod;
    }

    public void setMethod(String szMethod) {
        this.mszMethod = szMethod;
    }

    public void setProtocol(String szProtocol) {
        this.mszProtocol = szProtocol;
    }

    public String getProtocol() {
        return this.mszProtocol;
    }

    public void setPath(String szPath) {
        this.mszPath = szPath;
    }

    public String getPath() {
        return this.mszPath;
    }
}
