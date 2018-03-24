package com.foxit.uiextensions.utils;

import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.annots.AnnotHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ToolUtil {
    public static AnnotHandler getCurrentAnnotHandler(UIExtensionsManager UIExtensionsManager) {
        AnnotHandler annotHandler = null;
        try {
            Method method = UIExtensionsManager.class.getDeclaredMethod("getCurrentAnnotHandler", new Class[0]);
            method.setAccessible(true);
            try {
                annotHandler = (AnnotHandler) method.invoke(UIExtensionsManager, new Object[0]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            }
            method.setAccessible(false);
        } catch (SecurityException e4) {
            e4.printStackTrace();
        } catch (NoSuchMethodException e5) {
            e5.printStackTrace();
        }
        return annotHandler;
    }

    public static AnnotHandler getAnnotHandlerByType(UIExtensionsManager UIExtensionsManager, int type) {
        AnnotHandler annotHandler = null;
        try {
            Method method = UIExtensionsManager.class.getDeclaredMethod("getAnnotHandlerByType", new Class[]{Integer.TYPE});
            method.setAccessible(true);
            try {
                annotHandler = (AnnotHandler) method.invoke(UIExtensionsManager, new Object[]{Integer.valueOf(type)});
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            }
            method.setAccessible(false);
        } catch (SecurityException e4) {
            e4.printStackTrace();
        } catch (NoSuchMethodException e5) {
            e5.printStackTrace();
        }
        return annotHandler;
    }
}
