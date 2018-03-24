package com.xsj.crasheye.pushstrategy;

import com.xsj.crasheye.ActionEvent;
import com.xsj.crasheye.CrasheyeFileFilter;
import com.xsj.crasheye.Properties;
import com.xsj.crasheye.util.Utils;
import java.io.File;
import java.io.FileFilter;

public class MergerSession {
    private static int sessionFileCount = 0;

    public static void MergerSessionFiles() {
        File[] files = new File(Properties.FILES_PATH).listFiles(new FileFilter() {
            public boolean accept(File fileName) {
                if (fileName.getName().startsWith(CrasheyeFileFilter.SESIONFIX) && fileName.getName().endsWith(CrasheyeFileFilter.POSTFIX)) {
                    return true;
                }
                return false;
            }
        });
        if (files != null && files.length > 1) {
            for (File file : files) {
                int fileCount = GetSessionCountByFileName(file.getName());
                if (DeleteOldSessionFiles(file) && fileCount > 0) {
                    sessionFileCount += fileCount;
                }
            }
            if (sessionFileCount > 0) {
                BuildMergerSession(sessionFileCount);
            }
        }
    }

    public static int GetSessionCountByFileName(String fileName) {
        int fileCount = 0;
        try {
            fileCount = Integer.valueOf(fileName.split("-")[2]).intValue();
        } catch (Exception e) {
        }
        return fileCount;
    }

    private static void BuildMergerSession(int sessionCount) {
        Properties.sessionCount = sessionCount;
        String jsonLine = ActionEvent.createPing().toJsonLine();
        CrasheyeFileFilter.SetFileCount(sessionCount);
        Utils.writeFile(CrasheyeFileFilter.createSessionNewFile(), jsonLine);
    }

    private static boolean DeleteOldSessionFiles(File file) {
        if (file == null) {
            return false;
        }
        try {
            if (!file.exists() || !file.isFile()) {
                return false;
            }
            file.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
