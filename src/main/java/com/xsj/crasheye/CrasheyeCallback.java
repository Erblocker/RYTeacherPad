package com.xsj.crasheye;

public interface CrasheyeCallback {
    void dataSaverResponse(DataSaverResponse dataSaverResponse);

    void lastBreath(Exception exception);

    void netSenderResponse(NetSenderResponse netSenderResponse);
}
