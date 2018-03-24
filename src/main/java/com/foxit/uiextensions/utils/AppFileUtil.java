package com.foxit.uiextensions.utils;

import android.os.Environment;
import android.support.v4.media.session.PlaybackStateCompat;
import java.io.File;

public class AppFileUtil {
    private static AppFileUtil INSTANCE = new AppFileUtil();
    public Boolean isOOMHappened = Boolean.valueOf(false);

    public static boolean isSDAvailable() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static String getSDPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String formatFileSize(long fileSize) {
        if (fileSize < PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
            return Long.toString(fileSize) + "B";
        }
        if (fileSize < 1048576) {
            return new StringBuilder(String.valueOf(Float.toString(((float) Math.round((((float) fileSize) / 1024.0f) * 100.0f)) / 100.0f))).append("KB").toString();
        }
        if (fileSize < 1073741824) {
            return new StringBuilder(String.valueOf(Float.toString(((float) Math.round((((float) fileSize) / 1048576.0f) * 100.0f)) / 100.0f))).append("MB").toString();
        }
        return new StringBuilder(String.valueOf(Float.toString(((float) Math.round((((float) fileSize) / 1.07374182E9f) * 100.0f)) / 100.0f))).append("GB").toString();
    }

    public static long getFolderSize(String filePath) {
        long size = 0;
        File file = new File(filePath);
        if (!file.exists()) {
            return 0;
        }
        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File subFile : fileList) {
                if (subFile.isDirectory()) {
                    size += getFolderSize(subFile.getPath());
                } else {
                    size += subFile.length();
                }
            }
        }
        return size;
    }

    public static String getFileFolder(String filePath) {
        int index = filePath.lastIndexOf(47);
        if (index < 0) {
            return "";
        }
        return filePath.substring(0, index);
    }

    public static String getFileName(String filePath) {
        int index = filePath.lastIndexOf(47);
        return index < 0 ? filePath : filePath.substring(index + 1, filePath.length());
    }

    public static String getFileNameWithoutExt(String filePath) {
        String name = filePath.substring(filePath.lastIndexOf(47) + 1);
        int index = name.lastIndexOf(46);
        if (index > 0) {
            return name.substring(0, index);
        }
        return name;
    }

    public static String getFileDuplicateName(String filePath) {
        String newPath = filePath;
        while (new File(newPath).exists()) {
            String ext = newPath.substring(newPath.lastIndexOf(46));
            newPath = newPath.substring(0, newPath.lastIndexOf(46));
            int begin = 0;
            int end = newPath.length() - 1;
            if (newPath.charAt(end) == ')') {
                int i = end - 1;
                while (i >= 0) {
                    char c = newPath.charAt(i);
                    if (c != '(') {
                        if (c < '0' || c > '9') {
                            break;
                        }
                        i--;
                    } else {
                        begin = i;
                        break;
                    }
                }
            }
            if (begin <= 0 || end - begin >= 32) {
                newPath = new StringBuilder(String.valueOf(newPath)).append("(").append(1).append(")").append(ext).toString();
            } else {
                newPath = newPath.substring(0, begin) + "(" + (Integer.parseInt(newPath.substring(begin + 1, end), 10) + 1) + ")" + ext;
            }
        }
        return newPath;
    }

    public static boolean deleteFolder(File dirPath, boolean deleteHistory) {
        boolean flag = false;
        if (!dirPath.isDirectory()) {
            return 0;
        }
        File[] fileList = dirPath.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile()) {
                    flag = file.delete();
                } else if (file.isDirectory()) {
                    flag = deleteFolder(file, deleteHistory);
                }
                if (!flag) {
                    break;
                }
            }
        }
        return dirPath.delete();
    }

    public static AppFileUtil getInstance() {
        return INSTANCE;
    }

    private AppFileUtil() {
    }
}
