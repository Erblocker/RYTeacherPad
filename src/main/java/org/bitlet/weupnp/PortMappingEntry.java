package org.bitlet.weupnp;

public class PortMappingEntry {
    private String enabled;
    private int externalPort;
    private String internalClient;
    private int internalPort;
    private String portMappingDescription;
    private String protocol;
    private String remoteHost;

    public int getInternalPort() {
        return this.internalPort;
    }

    public void setInternalPort(int internalPort) {
        this.internalPort = internalPort;
    }

    public int getExternalPort() {
        return this.externalPort;
    }

    public void setExternalPort(int externalPort) {
        this.externalPort = externalPort;
    }

    public String getRemoteHost() {
        return this.remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getInternalClient() {
        return this.internalClient;
    }

    public void setInternalClient(String internalClient) {
        this.internalClient = internalClient;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getEnabled() {
        return this.enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getPortMappingDescription() {
        return this.portMappingDescription;
    }

    public void setPortMappingDescription(String portMappingDescription) {
        this.portMappingDescription = portMappingDescription;
    }
}
