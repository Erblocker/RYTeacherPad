package com.xsj.crasheye;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseDataSaver extends BaseExecutor implements InterfaceExecutor {
    public synchronized void save(String jsonData) {
    }

    public ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }
}
