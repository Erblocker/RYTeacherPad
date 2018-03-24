package com.xsj.crasheye;

public class NetSenderResponse {
    private String data;
    private Exception exception;
    private int responseCode;
    private Boolean sentSuccessfully = Boolean.valueOf(false);
    private String serverResponse;
    private String url;

    protected NetSenderResponse(String url, String data) {
        this.url = url;
        this.data = data;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    protected void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Exception getException() {
        return this.exception;
    }

    protected void setException(Exception exception) {
        this.exception = exception;
    }

    public Boolean getSentSuccessfully() {
        return this.sentSuccessfully;
    }

    protected void setSentSuccessfully(Boolean sendSuccessfully) {
        this.sentSuccessfully = sendSuccessfully;
    }

    public String getServerResponse() {
        return this.serverResponse;
    }

    protected void setServerResponse(String serverResponse) {
        this.serverResponse = serverResponse;
    }

    public String getData() {
        return this.data;
    }

    protected void setData(String data) {
        this.data = data;
    }

    public String getUrl() {
        return this.url;
    }

    public String toString() {
        return "NetSenderResponse [exception=" + this.exception + ", sendSuccessfully=" + this.sentSuccessfully + ", serverResponse=" + this.serverResponse + ", data=" + this.data + ", url=" + this.url + ", responseCode=" + this.responseCode + "]";
    }
}
