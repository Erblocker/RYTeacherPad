package com.netspace.library.utilities;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class HardwareInfo {
    public static String getTotalRAM() {
        IOException ex;
        String load = null;
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
            RandomAccessFile randomAccessFile;
            try {
                load = reader.readLine();
                randomAccessFile = reader;
            } catch (IOException e) {
                ex = e;
                randomAccessFile = reader;
                ex.printStackTrace();
                return load;
            }
        } catch (IOException e2) {
            ex = e2;
            ex.printStackTrace();
            return load;
        }
        return load;
    }

    public static String calculateMD5(File updateFile) {
        String output = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try {
                InputStream is = new FileInputStream(updateFile);
                byte[] buffer = new byte[8192];
                while (true) {
                    try {
                        int read = is.read(buffer);
                        if (read <= 0) {
                            break;
                        }
                        digest.update(buffer, 0, read);
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to process file for MD5", e);
                    } catch (Throwable th) {
                        try {
                            is.close();
                        } catch (IOException e2) {
                            Log.e("Login", "Exception on closing MD5 input stream", e2);
                        }
                    }
                }
                output = String.format("%32s", new Object[]{new BigInteger(1, digest.digest()).toString(16)}).replace(' ', '0');
                try {
                    is.close();
                } catch (IOException e22) {
                    Log.e("Login", "Exception on closing MD5 input stream", e22);
                }
            } catch (FileNotFoundException e3) {
                Log.e("Login", "Exception while getting FileInputStream", e3);
            }
        } catch (NoSuchAlgorithmException e4) {
            Log.e("Login", "Exception while gettingDigest", e4);
        }
        return output;
    }

    public static String getCPUInfo() {
        StringBuffer sb = new StringBuffer();
        String szResult = "";
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                while (true) {
                    String aLine = br.readLine();
                    if (aLine == null) {
                        break;
                    }
                    sb.append(new StringBuilder(String.valueOf(aLine)).append("\n").toString());
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString().replace("\t", "").replace("\n\n", "\n");
    }

    public static String getMD5usingShell(String szFile) {
        RootTools.debugMode = false;
        String szResult = null;
        if (!RootTools.isRootAvailable() || !RootTools.isAccessGiven()) {
            return null;
        }
        try {
            Shell shell = RootTools.getShell(true);
            if (!Shell.isAnyShellOpen()) {
                return null;
            }
            shell.add(new CommandCapture(0, "mount -o remount,rw /system")).waitForFinish();
            CommandCapture command2 = new CommandCapture(0, "md5 " + szFile);
            shell.add(command2).waitForFinish();
            szResult = command2.toString();
            if (szResult.indexOf(" ") != -1) {
                szResult = szResult.substring(0, szResult.indexOf(" "));
            }
            if (szResult.length() != 32) {
                szResult = null;
            }
            return szResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e2) {
            e2.printStackTrace();
        } catch (RootDeniedException e3) {
            e3.printStackTrace();
        } catch (IOException e4) {
            e4.printStackTrace();
        } catch (IllegalStateException e5) {
            e5.printStackTrace();
        }
    }

    public static String getTotalInternalMemory() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        return new StringBuilder(String.valueOf(String.valueOf((((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize())) / 1048576))).append("MB").toString();
    }

    public static int getNumCores() {
        try {
            return new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            }).length;
        } catch (Exception e) {
            return 1;
        }
    }

    public static String getVersionName(Context Context) {
        String szVersion = "";
        try {
            return Context.getPackageManager().getPackageInfo(Context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return szVersion;
        }
    }

    public static String getFilePath(Context Context) {
        String szFileName = "";
        try {
            return Context.getPackageManager().getPackageInfo(Context.getPackageName(), 0).applicationInfo.sourceDir;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return szFileName;
        }
    }

    public static String getSign(Context Context) {
        String szResult = "";
        try {
            szResult = Context.getPackageManager().getPackageInfo(Context.getPackageName(), 64).signatures[0].toCharsString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return szResult;
    }

    public static String getHardwareInfo(Context Context) {
        WifiManager wm = (WifiManager) Context.getSystemService("wifi");
        String szHardwareInfo = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("" + "BOARD: " + Build.BOARD + "\n")).append("BOOTLOADER: ").append(Build.BOOTLOADER).append("\n").toString())).append("BRAND: ").append(Build.BRAND).append("\n").toString())).append("CPU_ABI: ").append(Build.CPU_ABI).append("\n").toString())).append("CPU_ABI2: ").append(Build.CPU_ABI2).append("\n").toString())).append("DEVICE: ").append(Build.DEVICE).append("\n").toString())).append("DISPLAY: ").append(Build.DISPLAY).append("\n").toString())).append("FINGERPRINT: ").append(Build.FINGERPRINT).append("\n").toString())).append("HARDWARE: ").append(Build.HARDWARE).append("\n").toString())).append("HOST: ").append(Build.HOST).append("\n").toString())).append("ID: ").append(Build.ID).append("\n").toString())).append("MANUFACTURER: ").append(Build.MANUFACTURER).append("\n").toString())).append("MODEL: ").append(Build.MODEL).append("\n").toString())).append("PRODUCT: ").append(Build.PRODUCT).append("\n").toString())).append("RADIO: ").append(Build.RADIO).append("\n").toString())).append("SERIAL: ").append(Build.SERIAL).append("\n").toString())).append("TAGS: ").append(Build.TAGS).append("\n").toString())).append("TIME: ").append(Build.TIME).append("\n").toString())).append("TYPE: ").append(Build.TYPE).append("\n").toString())).append("UNKNOWN: unknown\n").toString())).append("USER: ").append(Build.USER).append("\n").toString())).append("VERSION_CODENAME: ").append(VERSION.CODENAME).append("\n").toString())).append("VERSION_RELEASE: ").append(VERSION.RELEASE).append("\n").toString())).append("VERSION_SDK_INT: ").append(VERSION.SDK_INT).append("\n").toString())).append("WifiMac: ").append(Utilities.getMacAddr()).append("\n").toString())).append("WifiSSID: ").append(wm.getConnectionInfo().getSSID()).append("\n").toString())).append(getTotalRAM()).append("\n").toString())).append(getCPUInfo()).append("\n").toString())).append("Internal: ").append(getTotalInternalMemory()).append("\n").toString())).append("CPUCores: ").append(String.valueOf(getNumCores())).append("\n").toString();
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) Context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        szHardwareInfo = new StringBuilder(String.valueOf(szHardwareInfo)).append("Screen: ").append(String.valueOf(metrics.widthPixels)).append("x").append(String.valueOf(metrics.heightPixels)).append("\n").toString();
        String szMD5 = calculateMD5(new File("/system/framework/services.jar"));
        if (szMD5 != null) {
            szHardwareInfo = new StringBuilder(String.valueOf(szHardwareInfo)).append("services.jar: ").append(szMD5).append("\n").toString();
        }
        szMD5 = calculateMD5(new File("/system/framework/framework.jar"));
        if (szMD5 != null) {
            szHardwareInfo = new StringBuilder(String.valueOf(szHardwareInfo)).append("framework.jar: ").append(szMD5).append("\n").toString();
        }
        szMD5 = getMD5usingShell("/dev/block/platform/dw_mmc/by-name/RECOVERY");
        if (szMD5 != null) {
            szHardwareInfo = new StringBuilder(String.valueOf(szHardwareInfo)).append("RECOVERY: ").append(szMD5).append("\n").toString();
        } else {
            szMD5 = getMD5usingShell("/dev/block/by-name/recovery");
            if (szMD5 != null) {
                szHardwareInfo = new StringBuilder(String.valueOf(szHardwareInfo)).append("RECOVERY: ").append(szMD5).append("\n").toString();
            }
        }
        szMD5 = getMD5usingShell("/dev/block/platform/dw_mmc/by-name/BOOT");
        if (szMD5 != null) {
            szHardwareInfo = new StringBuilder(String.valueOf(szHardwareInfo)).append("BOOT: ").append(szMD5).append("\n").toString();
        } else {
            szMD5 = getMD5usingShell("/dev/block/by-name/boot");
            if (szMD5 != null) {
                szHardwareInfo = new StringBuilder(String.valueOf(szHardwareInfo)).append("BOOT: ").append(szMD5).append("\n").toString();
            }
        }
        szHardwareInfo = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szHardwareInfo)).append("ClientVersion: ").append(getVersionName(Context)).append("\n").toString())).append("ClientSign: ").append(getSign(Context)).append("\n").toString())).append("ClientPath: ").append(getFilePath(Context)).append("\n").toString();
        szMD5 = calculateMD5(new File(getFilePath(Context)));
        if (szMD5 != null) {
            return new StringBuilder(String.valueOf(szHardwareInfo)).append("ClientMD5: ").append(szMD5).append("\n").toString();
        }
        return szHardwareInfo;
    }
}
