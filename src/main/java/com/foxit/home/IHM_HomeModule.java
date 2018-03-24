package com.foxit.home;

import android.content.Context;
import android.view.View;

public interface IHM_HomeModule {
    public static final String HOME_MODULE_TAG_LOCAL = "HM_LOCAL";

    View getContentView(Context context);

    String getTag();

    View getTopToolbar(Context context);

    boolean isNewVersion();

    void loadHomeModule(Context context);

    void onActivated();

    void onDeactivated();

    boolean onWillDestroy();

    void unloadHomeModule(Context context);
}
