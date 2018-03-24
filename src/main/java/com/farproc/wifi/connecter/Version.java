package com.farproc.wifi.connecter;

import android.os.Build.VERSION;

public class Version {
    public static final int SDK = get();

    private static int get() {
        try {
            return VERSION.class.getField("SDK_INT").getInt(null);
        } catch (NoSuchFieldException e) {
            return Integer.valueOf(VERSION.SDK).intValue();
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }
}
