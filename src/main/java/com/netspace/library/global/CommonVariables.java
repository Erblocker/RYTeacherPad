package com.netspace.library.global;

import com.google.gson.annotations.Expose;
import com.netspace.library.interfaces.IMyiApplication;
import com.netspace.library.struct.ServerInfo;
import com.netspace.library.struct.Session;
import com.netspace.library.struct.UserInfo;

public class CommonVariables {
    public IMyiApplication MyiApplication;
    @Expose
    public ServerInfo ServerInfo;
    @Expose
    public Session Session;
    @Expose
    public UserInfo UserInfo;
}
