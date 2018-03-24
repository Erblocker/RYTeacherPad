package com.foxit.app;

import android.app.Application;
import android.content.Context;

public class AppMain extends Application {
    public void onCreate() {
        super.onCreate();
        App.instance().setApplicationContext(this);
        App.instance().loadModules();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
