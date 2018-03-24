package com.netspace.library.interfaces;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import com.netspace.library.plugins.PluginsManager;

public interface IPluginModule {

    public interface ActivityPluginCallBack {
        boolean addTab(String str, Fragment fragment);

        DrawerLayout getDrawer();

        Menu getMenu();

        Toolbar getToolbar();

        ViewPager getViewPager();
    }

    Object activeObject(String str);

    void activityItem(int i);

    void activityLaunch(ActivityPluginCallBack activityPluginCallBack);

    String getName();

    Object getObject(String str);

    boolean initModule(Context context);

    void onIMMessage(String str, String str2);

    boolean setCurrentActivity(Activity activity);

    void setCurrentThemeID(int i);

    boolean setPluginsManager(PluginsManager pluginsManager);
}
