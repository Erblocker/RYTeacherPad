package com.xsj.crasheye;

import com.xsj.crasheye.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SyncDataSaver extends BaseDataSaver {
    SyncDataSaver() {
    }

    public synchronized void save(String jsonData) {
        File file = new File(CrasheyeFileFilter.createNewFile());
        if (!(file == null || file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeFile(file.getAbsolutePath(), jsonData);
    }

    public ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }
}
