package com.eclipsesource.v8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class LibraryLoader {
    static final String DELIMITER = System.getProperty("line.separator");
    static final String SEPARATOR = System.getProperty("file.separator");
    static final String SWT_LIB_DIR = ".j2v8";

    LibraryLoader() {
    }

    private static String computeLibraryShortName() {
        String osSuffix = getOS();
        return "j2v8" + "_" + osSuffix + "_" + getArchSuffix();
    }

    private static String computeLibraryFullName() {
        return "lib" + computeLibraryShortName() + "." + getOSFileExtension();
    }

    static void loadLibrary(String tempDirectory) {
        if (isAndroid()) {
            System.loadLibrary("j2v8");
            return;
        }
        StringBuffer message = new StringBuffer();
        String libShortName = computeLibraryShortName();
        String libFullName = computeLibraryFullName();
        String ideLocation = System.getProperty("user.dir") + SEPARATOR + "jni" + SEPARATOR + computeLibraryFullName();
        if (!load(libFullName, message) && !load(libShortName, message)) {
            if (!new File(ideLocation).exists() || !load(ideLocation, message)) {
                String path;
                if (tempDirectory != null) {
                    path = tempDirectory;
                } else {
                    path = System.getProperty("java.io.tmpdir");
                }
                if (!extract(path + SEPARATOR + libFullName, libFullName, message)) {
                    throw new UnsatisfiedLinkError("Could not load J2V8 library. Reasons: " + message.toString());
                }
            }
        }
    }

    static boolean load(String libName, StringBuffer message) {
        try {
            if (libName.indexOf(SEPARATOR) != -1) {
                System.load(libName);
            } else {
                System.loadLibrary(libName);
            }
            return true;
        } catch (UnsatisfiedLinkError e) {
            if (message.length() == 0) {
                message.append(DELIMITER);
            }
            message.append('\t');
            message.append(e.getMessage());
            message.append(DELIMITER);
            return false;
        }
    }

    static boolean extract(String fileName, String mappedName, StringBuffer message) {
        FileOutputStream os = null;
        InputStream is = null;
        File file = new File(fileName);
        boolean extracted = false;
        try {
            if (file.exists()) {
                file.delete();
            }
            is = LibraryLoader.class.getResourceAsStream("/" + mappedName);
            if (is == null) {
                return false;
            }
            extracted = true;
            byte[] buffer = new byte[4096];
            FileOutputStream os2 = new FileOutputStream(fileName);
            while (true) {
                try {
                    int read = is.read(buffer);
                    if (read == -1) {
                        break;
                    }
                    os2.write(buffer, 0, read);
                } catch (Throwable th) {
                    os = os2;
                }
            }
            os2.close();
            is.close();
            chmod("755", fileName);
            if (load(fileName, message)) {
                os = os2;
                return true;
            }
            return false;
        } catch (Throwable th2) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e2) {
                }
            }
            if (!extracted || !file.exists()) {
                return false;
            }
            file.delete();
            return false;
        }
    }

    static void chmod(String permision, String path) {
        if (!isWindows()) {
            try {
                Runtime.getRuntime().exec(new String[]{"chmod", permision, path}).waitFor();
            } catch (Throwable th) {
            }
        }
    }

    static String getOsName() {
        return System.getProperty("os.name") + System.getProperty("java.specification.vendor");
    }

    static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }

    static boolean isMac() {
        return getOsName().startsWith("Mac");
    }

    static boolean isLinux() {
        return getOsName().startsWith("Linux");
    }

    static boolean isNativeClient() {
        return getOsName().startsWith(Platform.NATIVE_CLIENT);
    }

    static boolean isAndroid() {
        return getOsName().contains("Android");
    }

    static String getArchSuffix() {
        String arch = System.getProperty("os.arch");
        if (arch.equals("i686")) {
            return "x86";
        }
        if (arch.equals("amd64")) {
            return "x86_64";
        }
        if (arch.equals(Platform.NATIVE_CLIENT)) {
            return "armv7l";
        }
        if (arch.equals("aarch64")) {
            return "armv7l";
        }
        return arch;
    }

    static String getOSFileExtension() {
        if (isWindows()) {
            return "dll";
        }
        if (isMac()) {
            return "dylib";
        }
        if (isLinux()) {
            return "so";
        }
        if (isNativeClient()) {
            return "so";
        }
        throw new UnsatisfiedLinkError("Unsupported platform: " + getOsName());
    }

    static String getOS() {
        if (isWindows()) {
            return "win32";
        }
        if (isMac()) {
            return Platform.MACOSX;
        }
        if (isLinux() && !isAndroid()) {
            return Platform.LINUX;
        }
        if (isAndroid()) {
            return Platform.ANDROID;
        }
        throw new UnsatisfiedLinkError("Unsupported platform: " + getOsName());
    }
}
