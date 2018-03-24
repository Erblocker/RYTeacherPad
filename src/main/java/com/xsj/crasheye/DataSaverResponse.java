package com.xsj.crasheye;

public class DataSaverResponse {
    private String data;
    private Exception exception;
    private String filepath;
    private Boolean savedSuccessfully;

    protected DataSaverResponse(String data, String filepath) {
        this.data = data;
        this.filepath = filepath;
    }

    public Exception getException() {
        return this.exception;
    }

    protected void setException(Exception exception) {
        this.exception = exception;
    }

    public Boolean getSavedSuccessfully() {
        return this.savedSuccessfully;
    }

    protected void setSavedSuccessfully(Boolean savedSuccessfully) {
        this.savedSuccessfully = savedSuccessfully;
    }

    public String getData() {
        return this.data;
    }

    public String getFilepath() {
        return this.filepath;
    }

    public String toString() {
        return "DataSaverResponse [data=" + this.data + ", filepath=" + this.filepath + ", exception=" + this.exception + ", savedSuccessfully=" + this.savedSuccessfully + "]";
    }
}
