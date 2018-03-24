package com.foxit.app;

import android.content.Context;
import com.foxit.app.event.AppEventManager;
import com.foxit.app.thread.AppThreadManager;
import com.foxit.sdk.common.Library;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;
import com.foxit.uiextensions.utils.AppDisplay;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static App INSTANCE = new App();
    private static int errCode;
    private static String key = "ezJvj1/GvGh39zvoP2Xsb3l6zn2JOVSnX07nABUcENPzCkt3zCVq70nWC+6ly9m7lXESBztFG/Qv9WGVaRUWjDZe1IhD5hKC2jhe+k2aTDPLR5eaPWiEaOtMO9OyuYIIGD5IBBlbeum2H0i3esvrcwvw7X3ti+XlQ1DwJS/A1BlVIdkbN/s+3M6SEXFXSunQKkwBc56+4T9VdIpQpda2GpKluwAaU+QuEwbbCImErZ9OptXkZjEy6Fm5YnlBvgXyAQLVEfsVZFU7FKTSATecXP4YK2ciDxN38oaM0WW/T+tstcKv1dVQqQCr5r+eGYKyZfMGAGWAPxRiHtkTaMHkMwZxlMiviN5A3TcJ4uxCVN9W/ou1kYYk9pUTh2iG2Hl0mujgN09MzGzwWu7Kdp7jxH8Obg0MP1jdvTQlK4EXotD00beGHuQIzcaPYYBT/rZ9ooKGebZxACWfgaueEGfqmDUHJRfKOShvS7BEgp09Ra5whQAC8Imy+rHqzV0uD8VYXX19xBeY/a+pT4Ia3z/7SUb4sMwl6fryRPlboC41r/UZMcVRD1alJBjgd208YEwZ3nsD9EtXqHPujI0P1/qL65GNMoeXyYVE5bjnOoqfh9i+9FK14c64l270JnXd+c1uOi+GTckWxbFRkHIu/wRuXXDHDk9WJP3M0OZiVxegIpxrSwiu53Sdm8LhFPNpdq+ikAew3H1qA+gXklNawIJOEgUuuFJH2eV5ttlE8B3cmwa2EYNkezwEbUdBGmi454FHQMI7TEnKXsXHb1eLEzjfbvLqkc48T4KG0RKe5QUah2Eni9r+GdKwv8dGa5kp3AQzaDdKgONvzI+M3SCQfNXUTWTDJf4h/zAU+lQ5dkhMaV48BZK+49FpU5K+5y8rAQoiLbeElzHOPDic7uro90Fwk5TJ4+NR84zWFaMwRQPbP/XgXyhPJF/1lOQhcfU/YiRWDR6ihYrT6oXXaaleugC9TXw4bQ4ANQlEucIpqXus5E6dQrkczuIMMff//iQrRNkyfQn0cfUgErIaQxBuqXavQ+O5EyrIN/PKg43/bGDg39HgWQdMK7+5P+Y3QiErgE+dw8/TTGD96KR/tjnhjcp8bQqjFAUuheHELFq5TwI4lujALDyEAMNO/txPdFvPIl5eTuBFyF3uxAKeOsdPysI69I+DloQGmlmMbxHvA3lC/hggRD90Woj64WMXRUG8yy61HwT3+cu/DWo2AcLrLENV";
    private static String sn = "XqKhhztYxS8P56M6hH9KjZyIEUcjMW4MxpFJO8mZaCEAUBnD6LMTjA==";
    private Context mContext;
    private AppDialogManager mDialogManager;
    private AppDisplay mDisplay;
    private AppEventManager mEventManager;
    private final List<Module> mModules = new ArrayList();
    private AppThreadManager mThreadManager;

    static {
        errCode = PDFError.NO_ERROR.getCode();
        System.loadLibrary("rdk");
        try {
            Library.init(sn, key);
        } catch (PDFException e) {
            errCode = e.getLastError();
        }
    }

    public static int getLicenseErrCode() {
        return errCode;
    }

    public AppDisplay getDisplay() {
        if (this.mDisplay == null) {
            this.mDisplay = new AppDisplay(this.mContext);
        }
        return this.mDisplay;
    }

    public AppEventManager getEventManager() {
        if (this.mEventManager == null) {
            this.mEventManager = new AppEventManager();
        }
        return this.mEventManager;
    }

    public AppThreadManager getThreadManager() {
        if (this.mThreadManager == null) {
            this.mThreadManager = new AppThreadManager();
        }
        return this.mThreadManager;
    }

    public AppDialogManager getDialogManager() {
        if (this.mDialogManager == null) {
            this.mDialogManager = AppDialogManager.getInstance();
        }
        return this.mDialogManager;
    }

    public Module getModuleByName(String name) {
        for (Module module : this.mModules) {
            if (module.getName().equals(name)) {
                return module;
            }
        }
        return null;
    }

    public void loadModules() {
        for (Module module : this.mModules) {
            module.loadModule();
        }
    }

    public void unloadModules() {
        for (Module module : this.mModules) {
            module.unloadModule();
        }
        this.mModules.clear();
    }

    public boolean registerModule(Module module) {
        return this.mModules.add(module);
    }

    public void setApplicationContext(Context context) {
        this.mContext = context;
    }

    public Context getApplicationContext() {
        return this.mContext;
    }

    public static App instance() {
        return INSTANCE;
    }

    private App() {
    }
}
