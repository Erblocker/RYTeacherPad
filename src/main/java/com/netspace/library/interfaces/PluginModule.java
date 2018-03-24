package com.netspace.library.interfaces;

import android.content.Context;

public interface PluginModule {
    boolean InitModule(Context context);

    String getName();

    Object getObject(String str);
}
