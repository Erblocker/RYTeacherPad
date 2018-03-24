package com.netspace.library.struct;

public class RESTSynchronizeError {
    public static final int STAGE_DOWNLOAD = 1;
    public static final int STAGE_LIST = 0;
    public static final int STAGE_UPLOAD = 2;
    public Throwable error;
    public int nCode = 0;
    public int nStage = 0;
    public String szFieldName;
}
