package com.eclipsesource.v8;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class PlatformDetector {

    public static class Arch {
        public static String getName() {
            String archProperty = System.getProperty("os.arch");
            String archName = PlatformDetector.normalizeArch(archProperty);
            if (!archName.equals("unknown")) {
                return archName;
            }
            throw new UnsatisfiedLinkError("Unsupported arch: " + archProperty);
        }
    }

    public static class OS {
        public static String getName() {
            String osProperty = System.getProperty("os.name");
            String osName = PlatformDetector.normalizeOs(osProperty);
            String vendorProperty = System.getProperty("java.specification.vendor");
            if (PlatformDetector.normalize(vendorProperty).contains(Platform.ANDROID) || osName.contains(Platform.ANDROID)) {
                return Platform.ANDROID;
            }
            if (!osName.equals("unknown")) {
                return osName;
            }
            throw new UnsatisfiedLinkError("Unsupported platform/vendor: " + osProperty + " / " + vendorProperty);
        }

        public static boolean isWindows() {
            return getName().equals(Platform.WINDOWS);
        }

        public static boolean isMac() {
            return getName().equals(Platform.MACOSX);
        }

        public static boolean isLinux() {
            return getName().equals(Platform.LINUX);
        }

        public static boolean isNativeClient() {
            return getName().equals(Platform.NATIVE_CLIENT);
        }

        public static boolean isAndroid() {
            return getName().equals(Platform.ANDROID);
        }

        public static String getLibFileExtension() {
            if (isWindows()) {
                return "dll";
            }
            if (isMac()) {
                return "dylib";
            }
            if (isLinux() || isAndroid() || isNativeClient()) {
                return "so";
            }
            throw new UnsatisfiedLinkError("Unsupported platform library-extension for: " + getName());
        }
    }

    public static class Vendor {
        private static final String LINUX_ID_PREFIX = "ID=";
        private static final String[] LINUX_OS_RELEASE_FILES = new String[]{"/etc/os-release", "/usr/lib/os-release"};
        private static final String REDHAT_RELEASE_FILE = "/etc/redhat-release";

        public static String getName() {
            if (OS.isWindows()) {
                return "microsoft";
            }
            if (OS.isMac()) {
                return "apple";
            }
            if (OS.isLinux()) {
                return getLinuxOsReleaseId();
            }
            if (OS.isAndroid()) {
                return "google";
            }
            throw new UnsatisfiedLinkError("Unsupported vendor: " + getName());
        }

        private static String getLinuxOsReleaseId() {
            File file;
            for (String osReleaseFileName : LINUX_OS_RELEASE_FILES) {
                file = new File(osReleaseFileName);
                if (file.exists()) {
                    return parseLinuxOsReleaseFile(file);
                }
            }
            file = new File(REDHAT_RELEASE_FILE);
            if (file.exists()) {
                return parseLinuxRedhatReleaseFile(file);
            }
            throw new UnsatisfiedLinkError("Unsupported linux vendor: " + getName());
        }

        private static String parseLinuxOsReleaseFile(File file) {
            Throwable th;
            BufferedReader reader = null;
            try {
                String line;
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                String id = null;
                do {
                    try {
                        line = reader2.readLine();
                        if (line == null) {
                            break;
                        }
                    } catch (IOException e) {
                        reader = reader2;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                    }
                } while (!line.startsWith(LINUX_ID_PREFIX));
                id = PlatformDetector.normalizeOsReleaseValue(line.substring(LINUX_ID_PREFIX.length()));
                closeQuietly(reader2);
                reader = reader2;
                return id;
            } catch (IOException e2) {
                closeQuietly(reader);
                return null;
            } catch (Throwable th3) {
                th = th3;
                closeQuietly(reader);
                throw th;
            }
        }

        private static String parseLinuxRedhatReleaseFile(File file) {
            Throwable th;
            String id = null;
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                try {
                    String line = reader2.readLine();
                    if (line != null) {
                        line = line.toLowerCase(Locale.US);
                        if (line.contains("centos")) {
                            id = "centos";
                        } else if (line.contains("fedora")) {
                            id = "fedora";
                        } else if (line.contains("red hat enterprise linux")) {
                            id = "rhel";
                        } else {
                            closeQuietly(reader2);
                            reader = reader2;
                        }
                        closeQuietly(reader2);
                        reader = reader2;
                    } else {
                        closeQuietly(reader2);
                        reader = reader2;
                    }
                } catch (IOException e) {
                    reader = reader2;
                    closeQuietly(reader);
                    return id;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    closeQuietly(reader);
                    throw th;
                }
            } catch (IOException e2) {
                closeQuietly(reader);
                return id;
            } catch (Throwable th3) {
                th = th3;
                closeQuietly(reader);
                throw th;
            }
            return id;
        }

        private static void closeQuietly(Closeable obj) {
            if (obj != null) {
                try {
                    obj.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String normalizeOsReleaseValue(String value) {
        return value.trim().replace("\"", "");
    }

    private static String normalizeOs(String value) {
        value = normalize(value);
        if (value.startsWith("aix")) {
            return "aix";
        }
        if (value.startsWith("hpux")) {
            return "hpux";
        }
        if (value.startsWith("os400") && (value.length() <= 5 || !Character.isDigit(value.charAt(5)))) {
            return "os400";
        }
        if (value.startsWith(Platform.ANDROID)) {
            return Platform.ANDROID;
        }
        if (value.startsWith(Platform.LINUX)) {
            return Platform.LINUX;
        }
        if (value.startsWith(Platform.NATIVE_CLIENT)) {
            return Platform.NATIVE_CLIENT;
        }
        if (value.startsWith(Platform.MACOSX) || value.startsWith("osx")) {
            return Platform.MACOSX;
        }
        if (value.startsWith("freebsd")) {
            return "freebsd";
        }
        if (value.startsWith("openbsd")) {
            return "openbsd";
        }
        if (value.startsWith("netbsd")) {
            return "netbsd";
        }
        if (value.startsWith("solaris") || value.startsWith("sunos")) {
            return "sunos";
        }
        if (value.startsWith(Platform.WINDOWS)) {
            return Platform.WINDOWS;
        }
        return "unknown";
    }

    private static String normalizeArch(String value) {
        value = normalize(value);
        if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        }
        if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        }
        if (value.matches("^(ia64|itanium64)$")) {
            return "itanium_64";
        }
        if (value.matches("^(sparc|sparc32)$")) {
            return "sparc_32";
        }
        if (value.matches("^(sparcv9|sparc64)$")) {
            return "sparc_64";
        }
        if (value.matches("^(arm|arm32)$")) {
            return "arm_32";
        }
        if ("aarch64".equals(value)) {
            return "aarch_64";
        }
        if (value.matches("^(ppc|ppc32)$")) {
            return "ppc_32";
        }
        if ("ppc64".equals(value)) {
            return "ppc_64";
        }
        if ("ppc64le".equals(value)) {
            return "ppcle_64";
        }
        if ("s390".equals(value)) {
            return "s390_32";
        }
        if ("s390x".equals(value)) {
            return "s390_64";
        }
        return "unknown";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }
}
