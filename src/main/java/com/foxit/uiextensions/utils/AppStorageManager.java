package com.foxit.uiextensions.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.storage.StorageManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class AppStorageManager {
    private static AppStorageManager mAppStorageManager = null;
    private Map<String, Boolean> mCacheMap;
    private Context mContext;

    public static AppStorageManager getInstance(Context context) {
        if (mAppStorageManager == null) {
            mAppStorageManager = new AppStorageManager(context);
        }
        return mAppStorageManager;
    }

    public AppStorageManager(Context context) {
        this.mContext = context;
        if (VERSION.SDK_INT >= 19) {
            this.mCacheMap = new HashMap(5);
        }
    }

    public File getCacheDir() {
        return this.mContext.getCacheDir();
    }

    public boolean checkStorageCanWrite(String filePath) {
        if (VERSION.SDK_INT < 19 || filePath.startsWith(Environment.getExternalStorageDirectory().getPath())) {
            return true;
        }
        for (String path : getVolumePaths()) {
            if (filePath.startsWith(path)) {
                if (this.mCacheMap != null && this.mCacheMap.containsKey(path)) {
                    return ((Boolean) this.mCacheMap.get(path)).booleanValue();
                }
                File file = new File(path, ".foxit-" + UUID.randomUUID());
                if (file.exists()) {
                    file.delete();
                }
                boolean result = file.mkdir();
                if (result) {
                    file.delete();
                }
                if (this.mCacheMap == null) {
                    return result;
                }
                this.mCacheMap.put(path, Boolean.valueOf(result));
                return result;
            }
        }
        return false;
    }

    @TargetApi(14)
    private List<String> getVolumePathsAboveVersion14() {
        NoSuchMethodException e;
        IllegalAccessException e2;
        IllegalArgumentException e3;
        InvocationTargetException e4;
        List<String> result = null;
        try {
            StorageManager storageManager = (StorageManager) this.mContext.getSystemService("storage");
            Method getPathsMethod = storageManager.getClass().getMethod("getVolumePaths", new Class[0]);
            Method getVolumeStateMethod = storageManager.getClass().getMethod("getVolumeState", new Class[]{String.class});
            String[] paths = (String[]) getPathsMethod.invoke(storageManager, new Object[0]);
            List<String> result2 = new ArrayList();
            try {
                for (String path : paths) {
                    if ("mounted".equals((String) getVolumeStateMethod.invoke(storageManager, new Object[]{path}))) {
                        result2.add(paths[r9]);
                    }
                }
                return result2;
            } catch (NoSuchMethodException e5) {
                e = e5;
                result = result2;
                e.printStackTrace();
                return result;
            } catch (IllegalAccessException e6) {
                e2 = e6;
                result = result2;
                e2.printStackTrace();
                return result;
            } catch (IllegalArgumentException e7) {
                e3 = e7;
                result = result2;
                e3.printStackTrace();
                return result;
            } catch (InvocationTargetException e8) {
                e4 = e8;
                result = result2;
                e4.printStackTrace();
                return result;
            }
        } catch (NoSuchMethodException e9) {
            e = e9;
            e.printStackTrace();
            return result;
        } catch (IllegalAccessException e10) {
            e2 = e10;
            e2.printStackTrace();
            return result;
        } catch (IllegalArgumentException e11) {
            e3 = e11;
            e3.printStackTrace();
            return result;
        } catch (InvocationTargetException e12) {
            e4 = e12;
            e4.printStackTrace();
            return result;
        }
    }

    public List<String> getVolumePaths() {
        List<String> volumList;
        String line;
        String element;
        File f;
        if (VERSION.SDK_INT >= 14) {
            volumList = getVolumePathsAboveVersion14();
            if (volumList != null && volumList.size() > 0) {
                return volumList;
            }
        }
        volumList = new ArrayList();
        String sdCard = Environment.getExternalStorageDirectory().getPath();
        if (sdCard != null) {
            volumList.add(sdCard);
        }
        File mountsFile = new File("/proc/mounts");
        if (mountsFile.exists()) {
            try {
                Scanner scanner;
                scanner = new Scanner(mountsFile);
                while (scanner.hasNext()) {
                    line = scanner.nextLine();
                    if (line.startsWith("/dev/block/vold/")) {
                        element = line.split(" ")[1];
                        if (!volumList.contains(element)) {
                            f = new File(element);
                            if (f.exists() && f.isDirectory()) {
                                volumList.add(element);
                            }
                        }
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (new File("/system/etc/vold.fstab").exists()) {
            try {
                scanner = new Scanner(mountsFile);
                while (scanner.hasNext()) {
                    line = scanner.nextLine();
                    if (line.startsWith("/dev/block/vold/")) {
                        element = line.split(" ")[1];
                        if (!volumList.contains(element)) {
                            f = new File(element);
                            if (f.exists() && f.isDirectory()) {
                                volumList.add(element);
                            }
                        }
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
            }
        }
        return volumList;
    }
}
