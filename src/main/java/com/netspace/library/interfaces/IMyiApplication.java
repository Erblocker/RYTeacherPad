package com.netspace.library.interfaces;

import com.netspace.library.servers.MJpegServer;

public interface IMyiApplication {
    public static final int SERVICE_HTTP = 4;
    public static final int SERVICE_IM = 1;
    public static final int SERVICE_MJPEG = 2;

    MJpegServer createScreenCaptureServer();

    String getAdditionalHardwareInfo();

    String getAppName();

    String[] getBlockedModules();

    String getClientID();

    int getMDMFlags();

    int getMjpegServerPort();

    int getRequiredService();

    boolean isAppRootRequired();

    void startAppBackgroundService();

    void startAppLogout();

    void startAppMainActivity();

    void stopAppBackgroundService();
}
