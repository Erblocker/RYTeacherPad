package com.netspace.library.utilities;

import android.content.Context;
import com.netspace.library.interfaces.PluginModule;
import dalvik.system.DexClassLoader;

public class PluginLoader {
    private Context m_Context;
    private PluginModule m_PluginModule;

    public PluginLoader(Context Context) {
        this.m_Context = Context;
    }

    public boolean LoadPlugin(String szPath, String szPackageName) {
        try {
            this.m_PluginModule = (PluginModule) new DexClassLoader(szPath, this.m_Context.getDir("dex", 0).getAbsolutePath(), null, getClass().getClassLoader()).loadClass(szPackageName).newInstance();
            this.m_PluginModule.InitModule(this.m_Context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Object GetObject(String szObjectName) {
        if (this.m_PluginModule != null) {
            return this.m_PluginModule.getObject(szObjectName);
        }
        return null;
    }
}
