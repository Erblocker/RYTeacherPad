package com.netspace.library.interfaces;

import java.io.File;

public interface IDownloadStatus {
    boolean isCancelled();

    void onBeginDownload();

    void onDownloadComplete(File file);

    void onDownloadError(int i, String str);

    void onDownloadProgress(long j, long j2);

    void onProgressFileBlock(byte[] bArr, long j);

    void onProgressLineContent(String str);
}
