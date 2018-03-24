package com.xsj.crasheye;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.xsj.crasheye.Crasheye.NDKExceptionCallback;
import com.xsj.crasheye.log.Logger;

public final class NativeExceptionHandler {
    private static volatile NativeExceptionHandler INSTANCE = null;
    private static boolean handleUserspaceSig = false;
    private static Handler handler;
    private static boolean initialized = false;
    private static String lastDmpFile;
    public static NDKExceptionCallback ndkExceptionCallback;
    private static Thread thread;

    private native boolean nativeInstallHandler(String str);

    private native boolean nativeInstallHandlerWithMono(String str, String str2);

    public native boolean nativeHandlerInstalled();

    public native void nativeReInstallHandler();

    public native void nativeSetUnhandleException();

    public native void nativeTestNativeCrash();

    public native void nativeWriteMinidump();

    private NativeExceptionHandler() {
    }

    public static NativeExceptionHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (NativeExceptionHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NativeExceptionHandler();
                }
            }
        }
        return INSTANCE;
    }

    private void handleUserspaceSig() {
        handleUserspaceSig = true;
        nativeSetUnhandleException();
        handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                try {
                    CrasheyeFileFilter.DeleteNativeFile();
                    NativeExceptionHandler.this.nativeReInstallHandler();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (!(NativeExceptionHandler.handler.hasMessages(1) || NativeExceptionHandler.this.nativeHandlerInstalled())) {
                            NativeExceptionHandler.handler.sendEmptyMessageDelayed(1, 500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });
        thread.setPriority(1);
        thread.start();
    }

    public boolean init() {
        if (initialized) {
            Logger.logWarning("native exception hanle is already init!");
            return true;
        } else if (!Properties.isPluginInitialized()) {
            return false;
        } else {
            try {
                System.loadLibrary("CrasheyeNDK");
                if (!nativeInstallHandler(Properties.FILES_PATH)) {
                    return false;
                }
                initialized = true;
                return true;
            } catch (UnsatisfiedLinkError e) {
                Logger.logError("load CrasheyeNDK so fail");
                return false;
            }
        }
    }

    public boolean initWithMono() {
        if (initialized) {
            Logger.logWarning("native exception hanle is already init!");
            return true;
        } else if (!Properties.isPluginInitialized()) {
            return false;
        } else {
            try {
                System.loadLibrary("CrasheyeNDK");
                if (Properties.FILES_PATH == null) {
                    Logger.logError("FILES_PATH is null, Mono init is fail!");
                    return false;
                } else if (Properties.LIB_MONOPATH == null) {
                    Logger.logError("LIB_MONOPATH is null, Mono init is fail!");
                    return false;
                } else if (!nativeInstallHandlerWithMono(Properties.FILES_PATH, Properties.LIB_MONOPATH)) {
                    return false;
                } else {
                    initialized = true;
                    return true;
                }
            } catch (UnsatisfiedLinkError e) {
                Logger.logError("load CrasheyeNDK so fail");
                return false;
            }
        }
    }

    public boolean initWithHandleUserspaceSig() {
        if (initialized) {
            Logger.logWarning("unity native exception handle is already init!");
            return true;
        } else if (!init()) {
            return false;
        } else {
            handleUserspaceSig();
            return true;
        }
    }

    public static void hanleNativeException(String dumpFile) {
        if (!initialized) {
            Logger.logWarning("native exception hanle is not init!");
        } else if (Properties.isPluginInitialized()) {
            lastDmpFile = dumpFile;
            ActionNativeError mCrashData = new ActionNativeError(dumpFile);
            if (ndkExceptionCallback != null) {
                ndkExceptionCallback.execute();
            }
            mCrashData.save(null);
            if (handleUserspaceSig && !handler.hasMessages(1)) {
                handler.sendEmptyMessageDelayed(1, 2000);
            }
        }
    }
}
