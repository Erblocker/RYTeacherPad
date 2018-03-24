package com.xsj.crasheye.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.xsj.crasheye.Crasheye;
import com.xsj.crasheye.CrasheyeLogLevel;
import com.xsj.crasheye.Properties;
import com.xsj.crasheye.Properties.RemoteSettingsProps;
import com.xsj.crasheye.log.Logger;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class Utils {
    protected static final String CONNECTION = "connection";
    private static final int Debug = 20;
    private static final int Error = 60;
    private static final int Info = 30;
    protected static final String STATE = "state";
    private static final int Verbose = 10;
    private static final int Warning = 50;

    public static String getRandomSessionNumber() {
        String time = String.valueOf(System.currentTimeMillis());
        if (time == null) {
            return "";
        }
        if (time.isEmpty()) {
            return "";
        }
        if (time.length() >= 8) {
            return time.substring(time.length() - 8, time.length());
        }
        return time;
    }

    public static String getScreenOrientation(Context gContext) {
        String rotation = "NA";
        if (gContext == null) {
            return rotation;
        }
        try {
            Display display = ((WindowManager) gContext.getSystemService("window")).getDefaultDisplay();
            if (display == null) {
                return rotation;
            }
            switch (display.getRotation()) {
                case 0:
                    rotation = "Portrait";
                    break;
                case 1:
                    rotation = "LandscapeRight";
                    break;
                case 2:
                    rotation = "PortraitUpsideDown";
                    break;
                case 3:
                    rotation = "LandscapeLeft";
                    break;
            }
            return rotation;
        } catch (Exception ex) {
            Logger.logError(ex.getMessage());
            return rotation;
        }
    }

    public static String getScreenSize(Context gContext) {
        String strOutSize = "NA";
        if (gContext == null) {
            return strOutSize;
        }
        try {
            DisplayMetrics dm = gContext.getResources().getDisplayMetrics();
            if (dm == null) {
                return strOutSize;
            }
            int widthPixe = dm.widthPixels;
            int heightPixe = dm.heightPixels;
            strOutSize = String.format("%d * %d", new Object[]{Integer.valueOf(widthPixe), Integer.valueOf(heightPixe)});
            return strOutSize;
        } catch (Exception ex) {
            Logger.logError(ex.getMessage());
        }
    }

    public static int convertLoggingLevelToInt(CrasheyeLogLevel level) {
        if (level.equals(CrasheyeLogLevel.Debug)) {
            return 20;
        }
        if (level.equals(CrasheyeLogLevel.Error)) {
            return 60;
        }
        if (level.equals(CrasheyeLogLevel.Info)) {
            return 30;
        }
        if (level.equals(CrasheyeLogLevel.Verbose) || !level.equals(CrasheyeLogLevel.Warning)) {
            return 10;
        }
        return 50;
    }

    public static HashMap<String, String> getConnectionInfo(Context context) {
        HashMap<String, String> infoMap = new HashMap(2);
        infoMap.put("connection", "NA");
        infoMap.put(STATE, "NA");
        if (context != null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                if (packageManager == null) {
                    if (Crasheye.DEBUG) {
                        Logger.logError("PackageManager in CheckNetworkConnection is null!");
                    }
                } else if (packageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE", Properties.APP_PACKAGE) == 0) {
                    NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                    if (info != null) {
                        if (info.getSubtypeName() == null || info.getSubtypeName().length() == 0) {
                            infoMap.put("connection", info.getTypeName());
                        } else {
                            infoMap.put("connection", info.getSubtypeName());
                        }
                        infoMap.put(STATE, info.getState().toString());
                    } else {
                        infoMap.put("connection", "No connection");
                    }
                }
            } catch (Exception ex) {
                Logger.logError(ex.getMessage());
            }
        } else if (Crasheye.DEBUG) {
            Logger.logError("Context in getConnection is null!");
        }
        return infoMap;
    }

    public static synchronized boolean shouldSendPing(Context ctx) {
        boolean shouldSendPing;
        synchronized (Utils.class) {
            shouldSendPing = System.currentTimeMillis() - Properties.lastPingTime > ((long) (RemoteSettingsProps.sessionTime.intValue() * 1000));
        }
        return shouldSendPing;
    }

    public static EnumStateStatus isGPSOn(Context gContext) {
        return EnumStateStatus.NA;
    }

    public static final String getTime() {
        String time = String.valueOf(System.currentTimeMillis());
        try {
            time = String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000);
        } catch (Exception e) {
        }
        return time;
    }

    public static final long getTimeForLong() {
        return System.currentTimeMillis();
    }

    public static boolean checkForRoot() {
        int i = 0;
        String[] directories = new String[]{"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        int length = directories.length;
        while (i < length) {
            if (new File(directories[i] + "su").exists()) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static String MD5(String data) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(data.getBytes(), 0, data.length());
        return new BigInteger(1, m.digest()).toString(16);
    }

    public static String MD5(byte[] data) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(data, 0, data.length);
        return new BigInteger(1, m.digest()).toString(16);
    }

    public static String MD5(ByteBuffer data) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(data);
        return new BigInteger(1, m.digest()).toString(16);
    }

    public static final String getMilisFromStart() {
        return String.valueOf(System.currentTimeMillis() - Properties.TIMESTAMP);
    }

    public static final String getCarrier(Context context) {
        return "NA";
    }

    public static String readFile(String filePath) throws Exception {
        Exception e;
        Throwable th;
        if (filePath == null) {
            throw new IllegalArgumentException("filePath Argument is null");
        }
        StringBuilder sb1 = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            BufferedReader input = new BufferedReader(new FileReader(filePath));
            while (true) {
                try {
                    String line = input.readLine();
                    if (line == null) {
                        break;
                    }
                    sb1.append(line);
                } catch (Exception e2) {
                    e = e2;
                    bufferedReader = input;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = input;
                }
            }
            String stringBuilder = sb1.toString();
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e3) {
                    throw e3;
                }
            }
            return stringBuilder;
        } catch (Exception e4) {
            e = e4;
            try {
                throw e;
            } catch (Throwable th3) {
                th = th3;
            }
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e32) {
                throw e32;
            }
        }
        throw th;
    }

    public static void writeFile(String filePath, String content) {
        IOException ioe;
        Throwable th;
        if (filePath == null || content == null) {
            Logger.logError("Crasheye write file: filepath is null or content is null!");
            return;
        }
        BufferedWriter bWritter = null;
        try {
            BufferedWriter bWritter2 = new BufferedWriter(new FileWriter(filePath));
            try {
                bWritter2.append(content);
                bWritter2.flush();
                bWritter2.close();
                if (bWritter2 != null) {
                    try {
                        bWritter2.close();
                        bWritter = bWritter2;
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bWritter = bWritter2;
            } catch (IOException e2) {
                ioe = e2;
                bWritter = bWritter2;
                try {
                    ioe.printStackTrace();
                    if (bWritter != null) {
                        try {
                            bWritter.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bWritter != null) {
                        try {
                            bWritter.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bWritter = bWritter2;
                if (bWritter != null) {
                    bWritter.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            ioe = e4;
            ioe.printStackTrace();
            if (bWritter != null) {
                bWritter.close();
            }
        }
    }

    public static void deleteFile(String filePath) {
        if (filePath != null) {
            try {
                deleteFile(new File(filePath));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            try {
                file.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static boolean isRunningService(Context context) {
        if (context == null) {
            return false;
        }
        try {
            List<RunningAppProcessInfo> appProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
            String cProcessName = context.getApplicationInfo().packageName;
            if (cProcessName == null) {
                return false;
            }
            for (RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.pid == Process.myPid() && appProcess.processName.equals(cProcessName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static byte[] toByteArray(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("filePath Argument is null");
        }
        try {
            File file = new File(filePath);
            int size = (int) file.length();
            if (size == 0) {
                return null;
            }
            byte[] data = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(data, 0, data.length);
            buf.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final String readLogs() {
        int lines = Properties.LOG_LINES;
        if (lines < 0) {
            lines = 100;
        }
        String filter = Properties.LOG_FILTER;
        if (filter == null) {
            filter = "";
        }
        StringBuilder log = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("logcat -d " + filter).getInputStream()));
            ArrayList<String> linesList = new ArrayList();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                linesList.add(line);
            }
            if (linesList.size() == 0) {
                return "You must add the android.permission.READ_LOGS permission to your manifest file!";
            }
            int start = linesList.size() - lines;
            if (start < 0) {
                start = 0;
            }
            for (int index = start; index < linesList.size(); index++) {
                log.append(new StringBuilder(String.valueOf((String) linesList.get(index))).append("\n").toString());
            }
            return log.toString().replaceAll(Pattern.quote("}{^"), "}{ ^");
        } catch (Exception e) {
            Logger.logError("Error reading logcat output!");
            return e.getMessage();
        }
    }

    public static final HashMap<String, String> getMemoryInfo() {
        HashMap<String, String> map = new HashMap(2);
        boolean memTotalFound = false;
        boolean memFreeFound = false;
        try {
            InputStream in = new ProcessBuilder(new String[]{"/system/bin/cat", "/proc/meminfo"}).start().getInputStream();
            StringBuilder sb1 = new StringBuilder();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                sb1.append(new String(re));
                String[] lines = sb1.toString().split("kB");
                if (lines.length >= 2) {
                    for (String line : lines) {
                        if (!memTotalFound && line.contains("MemTotal:")) {
                            map.put("memTotal", String.valueOf(Float.valueOf(line.substring(line.indexOf(" "), line.lastIndexOf(" ")).trim()).floatValue() / 1024.0f));
                            memTotalFound = true;
                        }
                        if (!memFreeFound && line.contains("MemFree:")) {
                            map.put("memFree", String.valueOf(Float.valueOf(line.substring(line.indexOf(" "), line.lastIndexOf(" ")).trim()).floatValue() / 1024.0f));
                            memFreeFound = true;
                        }
                    }
                    in.close();
                    return map;
                }
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return map;
    }

    public static boolean allowedToSendData() {
        if (Properties.flushOnlyOverWiFi && !Properties.CONNECTION.equals("WIFI")) {
            return false;
        }
        return true;
    }

    public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] != input[pos + i]) {
                return false;
            }
        }
        return true;
    }

    public static List<byte[]> byteSplit(byte[] pattern, byte[] input) {
        List<byte[]> l = new ArrayList();
        int blockStart = 0;
        int i = 0;
        while (i < input.length) {
            if (isMatch(pattern, input, i)) {
                l.add(Arrays.copyOfRange(input, blockStart, i));
                blockStart = i + pattern.length;
                i = blockStart;
            }
            i++;
        }
        if (blockStart < input.length) {
            l.add(Arrays.copyOfRange(input, blockStart, input.length));
        }
        return l;
    }

    public static String getChannelIdByConfig(Context context, String appChannelId) {
        if (appChannelId != null && !appChannelId.equals("NA") && !appChannelId.isEmpty()) {
            return appChannelId;
        }
        String channelID = "NA";
        if (context == null) {
            Logger.logError("Context is null!");
            return channelID;
        }
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 128).metaData.getString("CRASHEYE_CHANNEL");
        } catch (Exception e) {
            return channelID;
        }
    }

    public static byte[] getGZipString(String data) {
        byte[] gzipData;
        Throwable th;
        String tempData = data;
        ByteArrayOutputStream baos = null;
        GZIPOutputStream gos = null;
        try {
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            try {
                GZIPOutputStream gos2 = new GZIPOutputStream(baos2);
                try {
                    gos2.write(tempData.getBytes());
                    gos2.close();
                    gzipData = baos2.toByteArray();
                    if (baos2 != null) {
                        try {
                            baos2.close();
                        } catch (IOException e) {
                        }
                    }
                    if (gos2 != null) {
                        try {
                            gos2.close();
                            gos = gos2;
                            baos = baos2;
                        } catch (IOException e2) {
                            gos = gos2;
                            baos = baos2;
                        }
                    } else {
                        baos = baos2;
                    }
                } catch (Exception e3) {
                    gos = gos2;
                    baos = baos2;
                    gzipData = null;
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (IOException e4) {
                        }
                    }
                    if (gos != null) {
                        try {
                            gos.close();
                        } catch (IOException e5) {
                        }
                    }
                    return gzipData;
                } catch (Throwable th2) {
                    th = th2;
                    gos = gos2;
                    baos = baos2;
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (IOException e6) {
                        }
                    }
                    if (gos != null) {
                        try {
                            gos.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e8) {
                baos = baos2;
                gzipData = null;
                if (baos != null) {
                    baos.close();
                }
                if (gos != null) {
                    gos.close();
                }
                return gzipData;
            } catch (Throwable th3) {
                th = th3;
                baos = baos2;
                if (baos != null) {
                    baos.close();
                }
                if (gos != null) {
                    gos.close();
                }
                throw th;
            }
        } catch (Exception e9) {
            gzipData = null;
            if (baos != null) {
                baos.close();
            }
            if (gos != null) {
                gos.close();
            }
            return gzipData;
        } catch (Throwable th4) {
            th = th4;
            if (baos != null) {
                baos.close();
            }
            if (gos != null) {
                gos.close();
            }
            throw th;
        }
        return gzipData;
    }

    public static String getSendReportUrl() {
        String urlNumber = "";
        if (RemoteSettingsProps.actionHost == null || RemoteSettingsProps.actionHost.intValue() < 0 || RemoteSettingsProps.actionHost.intValue() > 99) {
            urlNumber = "";
        } else {
            urlNumber = String.valueOf(RemoteSettingsProps.actionHost);
        }
        return "http://rp" + urlNumber + ".crasheye.cn";
    }

    public static String getAbsolutePath(Context context) {
        if (context == null) {
            return null;
        }
        try {
            if (context.getFilesDir() == null) {
                return null;
            }
            String absolutePath = context.getFilesDir().getAbsolutePath();
            if (absolutePath == null) {
                return null;
            }
            if (new File(absolutePath).isDirectory()) {
                return absolutePath;
            }
            return null;
        } catch (Exception ex) {
            if (Crasheye.DEBUG) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    public static String getParentFilePath(Context context) {
        String str = null;
        if (context != null) {
            try {
                if (context.getFilesDir() != null) {
                    File parentDir = context.getFilesDir().getParentFile();
                    if (parentDir != null) {
                        String parentFilePath = parentDir.getAbsolutePath();
                        if (parentFilePath != null && new File(parentFilePath).isDirectory()) {
                            str = new StringBuilder(String.valueOf(parentFilePath)).append("/lib/libmono.so").toString();
                        }
                    }
                }
            } catch (Exception ex) {
                if (Crasheye.DEBUG) {
                    ex.printStackTrace();
                }
            }
        }
        return str;
    }
}
