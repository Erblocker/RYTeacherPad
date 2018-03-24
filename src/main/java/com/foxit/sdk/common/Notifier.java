package com.foxit.sdk.common;

public abstract class Notifier {
    public abstract void onOutOfMemory();

    public abstract void release();
}
