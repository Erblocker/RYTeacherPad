package org.bitlet.weupnp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class GatewayDevice {
    private static final int DEFAULT_HTTP_RECEIVE_TIMEOUT = 7000;
    private static int httpReadTimeout = DEFAULT_HTTP_RECEIVE_TIMEOUT;
    private String controlURL;
    private String controlURLCIF;
    private String deviceType;
    private String deviceTypeCIF;
    private String eventSubURL;
    private String eventSubURLCIF;
    private String friendlyName;
    private InetAddress localAddress;
    private String location;
    private String manufacturer;
    private String modelDescription;
    private String modelName;
    private String modelNumber;
    private String presentationURL;
    private String sCPDURL;
    private String sCPDURLCIF;
    private String serviceType;
    private String serviceTypeCIF;
    private String st;
    private String urlBase;

    public void loadDescription() throws SAXException, IOException {
        String ipConDescURL;
        URLConnection urlConn = new URL(getLocation()).openConnection();
        urlConn.setReadTimeout(httpReadTimeout);
        XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(new GatewayDeviceHandler(this));
        parser.parse(new InputSource(urlConn.getInputStream()));
        if (this.urlBase == null || this.urlBase.trim().length() <= 0) {
            ipConDescURL = this.location;
        } else {
            ipConDescURL = this.urlBase;
        }
        int lastSlashIndex = ipConDescURL.indexOf(47, 7);
        if (lastSlashIndex > 0) {
            ipConDescURL = ipConDescURL.substring(0, lastSlashIndex);
        }
        this.sCPDURL = copyOrCatUrl(ipConDescURL, this.sCPDURL);
        this.controlURL = copyOrCatUrl(ipConDescURL, this.controlURL);
        this.controlURLCIF = copyOrCatUrl(ipConDescURL, this.controlURLCIF);
        this.presentationURL = copyOrCatUrl(ipConDescURL, this.presentationURL);
    }

    public static Map<String, String> simpleUPnPcommand(String url, String service, String action, Map<String, String> args) throws IOException, SAXException {
        String soapAction = "\"" + service + "#" + action + "\"";
        StringBuilder soapBody = new StringBuilder();
        soapBody.append("<?xml version=\"1.0\"?>\r\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><SOAP-ENV:Body><m:" + action + " xmlns:m=\"" + service + "\">");
        if (args != null && args.size() > 0) {
            for (Entry<String, String> entry : args.entrySet()) {
                soapBody.append("<" + ((String) entry.getKey()) + ">" + ((String) entry.getValue()) + "</" + ((String) entry.getKey()) + ">");
            }
        }
        soapBody.append("</m:" + action + ">");
        soapBody.append("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(HttpPost.METHOD_NAME);
        conn.setConnectTimeout(httpReadTimeout);
        conn.setReadTimeout(httpReadTimeout);
        conn.setDoOutput(true);
        conn.setRequestProperty(HTTP.CONTENT_TYPE, "text/xml");
        conn.setRequestProperty("SOAPAction", soapAction);
        conn.setRequestProperty(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
        byte[] soapBodyBytes = soapBody.toString().getBytes();
        conn.setRequestProperty(HTTP.CONTENT_LEN, String.valueOf(soapBodyBytes.length));
        conn.getOutputStream().write(soapBodyBytes);
        Map<String, String> nameValue = new HashMap();
        XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(new NameValueHandler(nameValue));
        if (conn.getResponseCode() == 500) {
            try {
                parser.parse(new InputSource(conn.getErrorStream()));
            } catch (SAXException e) {
            }
            conn.disconnect();
        } else {
            parser.parse(new InputSource(conn.getInputStream()));
            conn.disconnect();
        }
        return nameValue;
    }

    public boolean isConnected() throws IOException, SAXException {
        String connectionStatus = (String) simpleUPnPcommand(this.controlURL, this.serviceType, "GetStatusInfo", null).get("NewConnectionStatus");
        if (connectionStatus == null || !connectionStatus.equalsIgnoreCase("Connected")) {
            return false;
        }
        return true;
    }

    public String getExternalIPAddress() throws IOException, SAXException {
        return (String) simpleUPnPcommand(this.controlURL, this.serviceType, "GetExternalIPAddress", null).get("NewExternalIPAddress");
    }

    public boolean addPortMapping(int externalPort, int internalPort, String internalClient, String protocol, String description) throws IOException, SAXException {
        Map<String, String> args = new LinkedHashMap();
        args.put("NewRemoteHost", "");
        args.put("NewExternalPort", Integer.toString(externalPort));
        args.put("NewProtocol", protocol);
        args.put("NewInternalPort", Integer.toString(internalPort));
        args.put("NewInternalClient", internalClient);
        args.put("NewEnabled", Integer.toString(1));
        args.put("NewPortMappingDescription", description);
        args.put("NewLeaseDuration", Integer.toString(0));
        if (simpleUPnPcommand(this.controlURL, this.serviceType, "AddPortMapping", args).get("errorCode") == null) {
            return true;
        }
        return false;
    }

    public boolean getSpecificPortMappingEntry(int externalPort, String protocol, PortMappingEntry portMappingEntry) throws IOException, SAXException {
        portMappingEntry.setExternalPort(externalPort);
        portMappingEntry.setProtocol(protocol);
        Map<String, String> args = new LinkedHashMap();
        args.put("NewRemoteHost", "");
        args.put("NewExternalPort", Integer.toString(externalPort));
        args.put("NewProtocol", protocol);
        Map<String, String> nameValue = simpleUPnPcommand(this.controlURL, this.serviceType, "GetSpecificPortMappingEntry", args);
        if (nameValue.isEmpty() || nameValue.containsKey("errorCode") || !nameValue.containsKey("NewInternalClient") || !nameValue.containsKey("NewInternalPort")) {
            return false;
        }
        portMappingEntry.setProtocol(protocol);
        portMappingEntry.setEnabled((String) nameValue.get("NewEnabled"));
        portMappingEntry.setInternalClient((String) nameValue.get("NewInternalClient"));
        portMappingEntry.setExternalPort(externalPort);
        portMappingEntry.setPortMappingDescription((String) nameValue.get("NewPortMappingDescription"));
        portMappingEntry.setRemoteHost((String) nameValue.get("NewRemoteHost"));
        try {
            portMappingEntry.setInternalPort(Integer.parseInt((String) nameValue.get("NewInternalPort")));
        } catch (NumberFormatException e) {
        }
        return true;
    }

    public boolean getGenericPortMappingEntry(int index, PortMappingEntry portMappingEntry) throws IOException, SAXException {
        Map<String, String> args = new LinkedHashMap();
        args.put("NewPortMappingIndex", Integer.toString(index));
        Map<String, String> nameValue = simpleUPnPcommand(this.controlURL, this.serviceType, "GetGenericPortMappingEntry", args);
        if (nameValue.isEmpty() || nameValue.containsKey("errorCode")) {
            return false;
        }
        portMappingEntry.setRemoteHost((String) nameValue.get("NewRemoteHost"));
        portMappingEntry.setInternalClient((String) nameValue.get("NewInternalClient"));
        portMappingEntry.setProtocol((String) nameValue.get("NewProtocol"));
        portMappingEntry.setEnabled((String) nameValue.get("NewEnabled"));
        portMappingEntry.setPortMappingDescription((String) nameValue.get("NewPortMappingDescription"));
        try {
            portMappingEntry.setInternalPort(Integer.parseInt((String) nameValue.get("NewInternalPort")));
        } catch (Exception e) {
        }
        try {
            portMappingEntry.setExternalPort(Integer.parseInt((String) nameValue.get("NewExternalPort")));
        } catch (Exception e2) {
        }
        return true;
    }

    public Integer getPortMappingNumberOfEntries() throws IOException, SAXException {
        Integer portMappingNumber = null;
        try {
            portMappingNumber = Integer.valueOf((String) simpleUPnPcommand(this.controlURL, this.serviceType, "GetPortMappingNumberOfEntries", null).get("NewPortMappingNumberOfEntries"));
        } catch (Exception e) {
        }
        return portMappingNumber;
    }

    public boolean deletePortMapping(int externalPort, String protocol) throws IOException, SAXException {
        Map<String, String> args = new LinkedHashMap();
        args.put("NewRemoteHost", "");
        args.put("NewExternalPort", Integer.toString(externalPort));
        args.put("NewProtocol", protocol);
        Map<String, String> nameValue = simpleUPnPcommand(this.controlURL, this.serviceType, "DeletePortMapping", args);
        return true;
    }

    public InetAddress getLocalAddress() {
        return this.localAddress;
    }

    public void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    public String getSt() {
        return this.st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceTypeCIF() {
        return this.serviceTypeCIF;
    }

    public void setServiceTypeCIF(String serviceTypeCIF) {
        this.serviceTypeCIF = serviceTypeCIF;
    }

    public String getControlURL() {
        return this.controlURL;
    }

    public void setControlURL(String controlURL) {
        this.controlURL = controlURL;
    }

    public String getControlURLCIF() {
        return this.controlURLCIF;
    }

    public void setControlURLCIF(String controlURLCIF) {
        this.controlURLCIF = controlURLCIF;
    }

    public String getEventSubURL() {
        return this.eventSubURL;
    }

    public void setEventSubURL(String eventSubURL) {
        this.eventSubURL = eventSubURL;
    }

    public String getEventSubURLCIF() {
        return this.eventSubURLCIF;
    }

    public void setEventSubURLCIF(String eventSubURLCIF) {
        this.eventSubURLCIF = eventSubURLCIF;
    }

    public String getSCPDURL() {
        return this.sCPDURL;
    }

    public void setSCPDURL(String sCPDURL) {
        this.sCPDURL = sCPDURL;
    }

    public String getSCPDURLCIF() {
        return this.sCPDURLCIF;
    }

    public void setSCPDURLCIF(String sCPDURLCIF) {
        this.sCPDURLCIF = sCPDURLCIF;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceTypeCIF() {
        return this.deviceTypeCIF;
    }

    public void setDeviceTypeCIF(String deviceTypeCIF) {
        this.deviceTypeCIF = deviceTypeCIF;
    }

    public String getURLBase() {
        return this.urlBase;
    }

    public void setURLBase(String uRLBase) {
        this.urlBase = uRLBase;
    }

    public String getFriendlyName() {
        return this.friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelDescription() {
        return this.modelDescription;
    }

    public void setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
    }

    public String getPresentationURL() {
        return this.presentationURL;
    }

    public void setPresentationURL(String presentationURL) {
        this.presentationURL = presentationURL;
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelNumber() {
        return this.modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public static int getHttpReadTimeout() {
        return httpReadTimeout;
    }

    public static void setHttpReadTimeout(int milliseconds) {
        httpReadTimeout = milliseconds;
    }

    private String copyOrCatUrl(String dst, String src) {
        if (src == null) {
            return dst;
        }
        if (src.startsWith("http://")) {
            return src;
        }
        if (!src.startsWith("/")) {
            dst = dst + "/";
        }
        return dst + src;
    }
}
