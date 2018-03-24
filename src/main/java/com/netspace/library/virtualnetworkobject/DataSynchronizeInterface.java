package com.netspace.library.virtualnetworkobject;

import com.netspace.library.ui.StatusBarDisplayer;

public abstract class DataSynchronizeInterface {
    public long mLastCheckTime = 0;

    public abstract boolean hasNewData();

    public abstract boolean isDownloadInProgress();

    public abstract boolean isUploadInProgress();

    public abstract void resetFullDownLoadFlag();

    public abstract void setDisplayer(StatusBarDisplayer statusBarDisplayer);

    public abstract boolean startDownloadProcess();

    public abstract boolean startUploadProcess();

    public abstract void stopAllProcess();
}
