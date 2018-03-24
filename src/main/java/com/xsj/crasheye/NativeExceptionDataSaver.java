package com.xsj.crasheye;

import com.xsj.crasheye.util.Utils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class NativeExceptionDataSaver extends BaseDataSaver {
    NativeExceptionDataSaver() {
    }

    public synchronized void save(String jsonData) {
        if (Properties.crasheyeInitType == "unity") {
            Utils.writeFile(CrasheyeFileFilter.NativeErrorFile, jsonData);
        } else {
            Utils.writeFile(Properties.FILES_PATH + "/" + CrasheyeFileFilter.NATIVEPREFIX + String.valueOf(System.currentTimeMillis()) + CrasheyeFileFilter.POSTFIX, jsonData);
        }
    }

    public ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }
}
