package com.foxit.sdk.common;

public abstract class FileRead {
    public abstract long getFileSize();

    public abstract byte[] read(long j, long j2);

    public abstract void release();
}
