package com.netspace.library.plugins;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.interfaces.IPluginModule;
import com.netspace.library.interfaces.IPluginModule.ActivityPluginCallBack;
import com.netspace.pad.library.R;
import dalvik.system.DexClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PluginsManager {
    private HashMap<String, IPluginModule> mMapPlugins = new HashMap();
    private ArrayList<ActivityPlugin> marrActivityPlugins = new ArrayList();
    private ArrayList<IPluginModule> marrIMMessagePlugin = new ArrayList();

    public static class ActivityPlugin {
        public IPluginModule plugin;
        public String szActivityName;
    }

    public boolean loadFromExternalAPKOrJar(String szAPKOrJarFullPath, String szClassName) {
        boolean bResult = false;
        if (this.mMapPlugins.containsKey(szClassName)) {
            return true;
        }
        try {
            return addPlugin(new DexClassLoader(szAPKOrJarFullPath, MyiBaseApplication.getDexPath().getAbsolutePath(), null, MyiBaseApplication.getBaseAppContext().getClassLoader()).loadClass(szClassName));
        } catch (Exception e) {
            e.printStackTrace();
            return bResult;
        }
    }

    public static boolean isAPKInstalled(String szPackageName) {
        try {
            MyiBaseApplication.getBaseAppContext().createPackageContext(szPackageName, 3);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Resources getResourceFromInstalledAPK(String szPackageName) {
        try {
            return MyiBaseApplication.getBaseAppContext().createPackageContext(szPackageName, 2).getResources();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clear() {
        this.mMapPlugins.clear();
        this.marrActivityPlugins.clear();
        this.marrIMMessagePlugin.clear();
    }

    public boolean addPlugin(Class<IPluginModule> plugin) {
        if (this.mMapPlugins.containsValue(plugin)) {
            return false;
        }
        IPluginModule pluginModule = new PluginWrapper(plugin);
        if (!pluginModule.initModule(MyiBaseApplication.getBaseAppContext())) {
            return false;
        }
        this.mMapPlugins.put(plugin.getClass().getName(), pluginModule);
        pluginModule.setPluginsManager(this);
        pluginModule.setCurrentThemeID(R.style.AppTheme);
        return true;
    }

    public boolean addPlugin(IPluginModule plugin) {
        if (this.mMapPlugins.containsValue(plugin)) {
            return false;
        }
        IPluginModule pluginModule = plugin;
        if (!pluginModule.initModule(MyiBaseApplication.getBaseAppContext())) {
            return false;
        }
        this.mMapPlugins.put(plugin.getClass().getName(), pluginModule);
        pluginModule.setPluginsManager(this);
        pluginModule.setCurrentThemeID(R.style.AppTheme);
        return true;
    }

    public boolean loadFromInstalledAPK(String szPackageName, String szClassName) {
        boolean bResult = false;
        if (this.mMapPlugins.containsKey(szClassName)) {
            return true;
        }
        try {
            return addPlugin(new DexClassLoader(MyiBaseApplication.getBaseAppContext().createPackageContext(szPackageName, 3).getApplicationInfo().sourceDir, MyiBaseApplication.getDexPath().getAbsolutePath(), null, MyiBaseApplication.getBaseAppContext().getClassLoader()).loadClass(szClassName));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return bResult;
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
            return bResult;
        }
    }

    public IPluginModule getPlugin(String szClassName) {
        return (IPluginModule) this.mMapPlugins.get(szClassName);
    }

    public boolean registerIntoActivity(String szActivityName, IPluginModule plugin) {
        ActivityPlugin data = new ActivityPlugin();
        data.szActivityName = szActivityName;
        data.plugin = plugin;
        this.marrActivityPlugins.add(data);
        return true;
    }

    public boolean registerIMMessage(IPluginModule plugin) {
        if (this.marrIMMessagePlugin.equals(plugin)) {
            return false;
        }
        this.marrIMMessagePlugin.add(plugin);
        return true;
    }

    public void registerCurrentActivity(Activity activity, ActivityPluginCallBack callback) {
        Iterator it;
        for (IPluginModule plugin : this.mMapPlugins.values()) {
            plugin.setCurrentActivity(activity);
        }
        if (callback != null) {
            it = this.marrActivityPlugins.iterator();
            while (it.hasNext()) {
                ActivityPlugin activityplugin = (ActivityPlugin) it.next();
                if (activityplugin.szActivityName.equalsIgnoreCase(activity.getClass().getName())) {
                    activityplugin.plugin.activityLaunch(callback);
                }
            }
        }
    }

    public void handleActivityClick(Activity activity, int nID) {
        Iterator it = this.marrActivityPlugins.iterator();
        while (it.hasNext()) {
            ActivityPlugin activityplugin = (ActivityPlugin) it.next();
            if (activityplugin.szActivityName.equalsIgnoreCase(activity.getClass().getName())) {
                activityplugin.plugin.activityItem(nID);
            }
        }
    }

    public void handleIMMessage(String szFrom, String szMessage) {
        Iterator it = this.marrIMMessagePlugin.iterator();
        while (it.hasNext()) {
            ((IPluginModule) it.next()).onIMMessage(szFrom, szMessage);
        }
    }
}
