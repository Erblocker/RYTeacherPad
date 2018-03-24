package com.xsj.crasheye;

import com.xsj.crasheye.util.Utils;
import java.io.File;
import java.io.FileFilter;

public class CrasheyeFileFilter implements FileFilter {
    public static final String BREADCRUMBSFILE = ".breadcrumbs";
    public static final String CUSTOMFILE = ".custom";
    private static String FILECOUNT = VERSION;
    public static final String MONOSTACKFILE = ".monostack";
    public static final String NATIVEPREFIX = "native-CrasheyeSavedData-1-";
    public static final String NATIVESEPARATOR = "^@%*#~^";
    public static final String NativeErrorFile = (Properties.FILES_PATH + "/" + NATIVEPREFIX + String.valueOf(System.currentTimeMillis()) + POSTFIX);
    public static final String POSTFIX = ".json";
    private static final String PREFIX = "CrasheyeSavedData-1-";
    public static final String RAWNATIVEFILE = ".dmp";
    public static final String SESIONFIX = "session-CrasheyeSavedData-";
    public static final String SPLITSTRING = "\\^";
    private static final String VERSION = "1";
    private static CrasheyeFileFilter fileFilterSingleton = null;

    public boolean accept(File filename) {
        if (filename.getName().startsWith(PREFIX) && filename.getName().endsWith(POSTFIX)) {
            return true;
        }
        if (filename.getName().startsWith(SESIONFIX) && filename.getName().endsWith(POSTFIX)) {
            return true;
        }
        return false;
    }

    public static String createNewFile() {
        return Properties.FILES_PATH + "/" + PREFIX + String.valueOf(System.currentTimeMillis()) + POSTFIX;
    }

    public static void SetFileCount(int fileCount) {
        FILECOUNT = String.valueOf(fileCount);
    }

    public static String createSessionNewFile() {
        return Properties.FILES_PATH + "/" + new StringBuilder(SESIONFIX).append(FILECOUNT).append("-").toString() + String.valueOf(System.currentTimeMillis()) + POSTFIX;
    }

    public static CrasheyeFileFilter getInstance() {
        if (fileFilterSingleton == null) {
            fileFilterSingleton = new CrasheyeFileFilter();
        }
        return fileFilterSingleton;
    }

    public static void DeleteNativeFile() {
        Utils.deleteFile(NativeErrorFile);
    }
}
