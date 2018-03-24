package com.netspace.library.interfaces;

import android.content.Intent;

public interface IComponents {

    public interface ComponentCallBack {
        void OnDataLoaded(String str, IComponents iComponents);

        void OnDataUploaded(String str, IComponents iComponents);

        void OnRequestIntent(Intent intent, IComponents iComponents);
    }

    String getData();

    void intentComplete(Intent intent);

    void setCallBack(ComponentCallBack componentCallBack);

    void setData(String str);

    void setLocked(boolean z);
}
