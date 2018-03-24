package com.netspace.library.plugins;

import android.app.Activity;
import android.content.Context;
import com.netspace.library.interfaces.IPluginModule;
import com.netspace.library.interfaces.IPluginModule.ActivityPluginCallBack;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PluginWrapper implements IPluginModule {
    private Class<IPluginModule> mPluginClass;
    private Object mPluginObject;

    public PluginWrapper(Class<IPluginModule> PluginClass) {
        this.mPluginClass = PluginClass;
        try {
            this.mPluginObject = this.mPluginClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
    }

    private Method findMethod(String szMethodName) {
        try {
            for (Method method : this.mPluginClass.getDeclaredMethods()) {
                if (method.getName().contentEquals(szMethodName)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean initModule(Context Context) {
        boolean z = false;
        try {
            Method method = findMethod("initModule");
            if (method != null) {
                z = ((Boolean) method.invoke(this.mPluginObject, new Object[]{Context})).booleanValue();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
        return z;
    }

    public String getName() {
        try {
            return (String) findMethod("getName").invoke(this.mPluginObject, new Object[0]);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
        return null;
    }

    public Object getObject(String szObjectName) {
        try {
            Method method = findMethod("getObject");
            method.setAccessible(true);
            return method.invoke(this.mPluginObject, new Object[]{szObjectName});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public Object activeObject(String szObjectName) {
        try {
            Method method = findMethod("activeObject");
            method.setAccessible(true);
            return method.invoke(this.mPluginObject, new Object[]{szObjectName});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public boolean setCurrentActivity(Activity activity) {
        boolean z = false;
        try {
            Method method = findMethod("setCurrentActivity");
            if (method != null) {
                z = ((Boolean) method.invoke(this.mPluginObject, new Object[]{activity})).booleanValue();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
        return z;
    }

    public boolean setPluginsManager(PluginsManager manager) {
        boolean z = false;
        try {
            Method method = findMethod("setPluginsManager");
            if (method != null) {
                z = ((Boolean) method.invoke(this.mPluginObject, new Object[]{manager})).booleanValue();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
        return z;
    }

    public void setCurrentThemeID(int nThemeID) {
        try {
            Method method = findMethod("setCurrentThemeID");
            if (method != null) {
                method.invoke(this.mPluginObject, new Object[]{Integer.valueOf(nThemeID)});
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }

    public void activityLaunch(ActivityPluginCallBack callBack) {
        try {
            Method method = findMethod("activityLaunch");
            if (method != null) {
                method.invoke(this.mPluginObject, new Object[]{callBack});
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }

    public void activityItem(int nID) {
        try {
            Method method = findMethod("activityItem");
            if (method != null) {
                method.invoke(this.mPluginObject, new Object[]{Integer.valueOf(nID)});
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }

    public void onIMMessage(String szFrom, String szMessage) {
        try {
            Method method = findMethod("onIMMessage");
            if (method != null) {
                method.invoke(this.mPluginObject, new Object[]{szFrom, szMessage});
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }
}
