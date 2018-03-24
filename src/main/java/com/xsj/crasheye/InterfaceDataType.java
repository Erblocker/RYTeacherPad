package com.xsj.crasheye;

import android.content.Context;

interface InterfaceDataType {
    void save(BaseDataSaver baseDataSaver);

    void send(Context context, NetSender netSender, boolean z);

    void send(NetSender netSender, boolean z);

    String toJsonLine();
}
