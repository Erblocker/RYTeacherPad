package com.netspace.library.struct;

import com.google.gson.annotations.Expose;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.parser.ServerConfigurationParser;

public class ServerInfo {
    @Expose
    public ServerConfigurationParser ServerConfiguration = new ServerConfigurationParser();
    @Expose
    public boolean bUseSSL = false;
    @Expose
    public String szResourceBaseURL = "";
    @Expose
    public String szServerAddress = "";

    public boolean isConfigured() {
        if (this.szServerAddress.isEmpty() || this.szResourceBaseURL.isEmpty()) {
            return false;
        }
        return true;
    }

    public String getServerHost() {
        String szResult = "";
        if (this.szServerAddress.indexOf(":") != -1) {
            return this.szServerAddress.substring(0, this.szServerAddress.indexOf(":"));
        }
        return szResult;
    }

    public int getServerPort() {
        String szResult = "0";
        if (this.szServerAddress.indexOf(":") != -1) {
            szResult = this.szServerAddress.substring(this.szServerAddress.indexOf(":") + 1);
        }
        return Integer.valueOf(szResult).intValue();
    }

    public String getServerURL() {
        return MyiBaseApplication.getProtocol() + "://" + this.szServerAddress;
    }
}
