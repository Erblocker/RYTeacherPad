package com.stericson.RootTools.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;
import com.stericson.RootTools.containers.Mount;
import com.stericson.RootTools.containers.Permissions;
import com.stericson.RootTools.containers.Symlink;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

public final class RootToolsInternalMethods {
    boolean instantiated = false;

    protected RootToolsInternalMethods() {
    }

    public static void getInstance() {
        RootTools.setRim(new RootToolsInternalMethods());
    }

    public boolean returnPath() throws TimeoutException {
        Exception e;
        try {
            CommandCapture commandCapture;
            String line;
            if (RootTools.exists("/data/local/tmp")) {
                commandCapture = null;
            } else {
                commandCapture = new CommandCapture(0, "mkdir /data/local/tmp");
                try {
                    Shell.startRootShell().add(commandCapture).waitForFinish();
                } catch (Exception e2) {
                    e = e2;
                    CommandCapture commandCapture2 = commandCapture;
                    if (RootTools.debugMode) {
                        RootTools.log("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return false;
                }
            }
            InternalVariables.path = new HashSet();
            String mountedas = RootTools.getMountedAs("/");
            RootTools.remount("/", "rw");
            Shell.startRootShell().add(new CommandCapture(0, "chmod 0777 /init.rc"));
            commandCapture = new CommandCapture(0, "dd if=/init.rc of=/data/local/tmp/init.rc");
            Shell.startRootShell().add(commandCapture);
            Shell.startRootShell().add(new CommandCapture(0, "chmod 0777 /data/local/tmp/init.rc")).waitForFinish();
            RootTools.remount("/", mountedas);
            LineNumberReader lnr = new LineNumberReader(new FileReader("/data/local/tmp/init.rc"));
            do {
                line = lnr.readLine();
                if (line == null) {
                    return false;
                }
                RootTools.log(line);
            } while (!line.contains("export PATH"));
            InternalVariables.path = new HashSet(Arrays.asList(line.substring(line.indexOf("/")).split(":")));
            return true;
        } catch (Exception e3) {
            e = e3;
            if (RootTools.debugMode) {
                RootTools.log("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
    }

    public ArrayList<Symlink> getSymLinks() throws FileNotFoundException, IOException {
        Throwable th;
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader("/data/local/symlinks.txt"));
            try {
                ArrayList<Symlink> symlink = new ArrayList();
                while (true) {
                    String line = lnr.readLine();
                    if (line == null) {
                        return symlink;
                    }
                    RootTools.log(line);
                    String[] fields = line.split(" ");
                    symlink.add(new Symlink(new File(fields[fields.length - 3]), new File(fields[fields.length - 1])));
                }
            } catch (Throwable th2) {
                th = th2;
                LineNumberReader lineNumberReader = lnr;
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            throw th;
        }
    }

    public Permissions getPermissions(String line) {
        String rawPermissions = line.split(" ")[0];
        if (rawPermissions.length() != 10 || ((rawPermissions.charAt(0) != '-' && rawPermissions.charAt(0) != 'd' && rawPermissions.charAt(0) != 'l') || ((rawPermissions.charAt(1) != '-' && rawPermissions.charAt(1) != 'r') || (rawPermissions.charAt(2) != '-' && rawPermissions.charAt(2) != 'w')))) {
            return null;
        }
        RootTools.log(rawPermissions);
        Permissions permissions = new Permissions();
        permissions.setType(rawPermissions.substring(0, 1));
        RootTools.log(permissions.getType());
        permissions.setUserPermissions(rawPermissions.substring(1, 4));
        RootTools.log(permissions.getUserPermissions());
        permissions.setGroupPermissions(rawPermissions.substring(4, 7));
        RootTools.log(permissions.getGroupPermissions());
        permissions.setOtherPermissions(rawPermissions.substring(7, 10));
        RootTools.log(permissions.getOtherPermissions());
        StringBuilder finalPermissions = new StringBuilder();
        finalPermissions.append(parseSpecialPermissions(rawPermissions));
        finalPermissions.append(parsePermissions(permissions.getUserPermissions()));
        finalPermissions.append(parsePermissions(permissions.getGroupPermissions()));
        finalPermissions.append(parsePermissions(permissions.getOtherPermissions()));
        permissions.setPermissions(Integer.parseInt(finalPermissions.toString()));
        return permissions;
    }

    public int parsePermissions(String permission) {
        int tmp;
        if (permission.charAt(0) == 'r') {
            tmp = 4;
        } else {
            tmp = 0;
        }
        RootTools.log("permission " + tmp);
        RootTools.log("character " + permission.charAt(0));
        if (permission.charAt(1) == 'w') {
            tmp += 2;
        } else {
            tmp += 0;
        }
        RootTools.log("permission " + tmp);
        RootTools.log("character " + permission.charAt(1));
        if (permission.charAt(2) == 'x') {
            tmp++;
        } else {
            tmp += 0;
        }
        RootTools.log("permission " + tmp);
        RootTools.log("character " + permission.charAt(2));
        return tmp;
    }

    public int parseSpecialPermissions(String permission) {
        int tmp = 0;
        if (permission.charAt(2) == 's') {
            tmp = 0 + 4;
        }
        if (permission.charAt(5) == 's') {
            tmp += 2;
        }
        if (permission.charAt(8) == 't') {
            tmp++;
        }
        RootTools.log("special permissions " + tmp);
        return tmp;
    }

    public boolean copyFile(String source, String destination, boolean remountAsRw, boolean preserveFileAttributes) {
        boolean result = true;
        if (remountAsRw) {
            try {
                RootTools.remount(destination, "RW");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        if (checkUtil("cp")) {
            RootTools.log("cp command is available!");
            if (preserveFileAttributes) {
                Shell.startRootShell().add(new CommandCapture(0, "cp -fp " + source + " " + destination)).waitForFinish();
            } else {
                Shell.startRootShell().add(new CommandCapture(0, "cp -f " + source + " " + destination)).waitForFinish();
            }
        } else if (checkUtil("busybox") && hasUtil("cp", "busybox")) {
            RootTools.log("busybox cp command is available!");
            if (preserveFileAttributes) {
                Shell.startRootShell().add(new CommandCapture(0, "busybox cp -fp " + source + " " + destination)).waitForFinish();
            } else {
                Shell.startRootShell().add(new CommandCapture(0, "busybox cp -f " + source + " " + destination)).waitForFinish();
            }
        } else if (checkUtil("cat")) {
            RootTools.log("cp is not available, use cat!");
            int filePermission = -1;
            if (preserveFileAttributes) {
                filePermission = getFilePermissionsSymlinks(source).getPermissions();
            }
            Shell.startRootShell().add(new CommandCapture(0, "cat " + source + " > " + destination)).waitForFinish();
            if (preserveFileAttributes) {
                Shell.startRootShell().add(new CommandCapture(0, "chmod " + filePermission + " " + destination)).waitForFinish();
            }
        } else {
            result = false;
        }
        if (!remountAsRw) {
            return result;
        }
        RootTools.remount(destination, "RO");
        return result;
    }

    public boolean checkUtil(String util) {
        if (RootTools.findBinary(util)) {
            List<String> binaryPaths = new ArrayList();
            binaryPaths.addAll(RootTools.lastFoundBinaryPaths);
            for (String path : binaryPaths) {
                Permissions permissions = RootTools.getFilePermissionsSymlinks(path + "/" + util);
                if (permissions != null) {
                    String permission;
                    if (Integer.toString(permissions.getPermissions()).length() > 3) {
                        permission = Integer.toString(permissions.getPermissions()).substring(1);
                    } else {
                        permission = Integer.toString(permissions.getPermissions());
                    }
                    if (permission.equals("755") || permission.equals("777") || permission.equals("775")) {
                        RootTools.utilPath = path + "/" + util;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean deleteFileOrDirectory(String target, boolean remountAsRw) {
        boolean result = true;
        if (remountAsRw) {
            try {
                RootTools.remount(target, "RW");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        CommandCapture command;
        if (hasUtil("rm", "toolbox")) {
            RootTools.log("rm command is available!");
            command = new CommandCapture(0, "rm -r " + target);
            Shell.startRootShell().add(command).waitForFinish();
            if (command.exitCode() != 0) {
                RootTools.log("target not exist or unable to delete file");
                result = false;
            }
        } else if (checkUtil("busybox") && hasUtil("rm", "busybox")) {
            RootTools.log("busybox cp command is available!");
            command = new CommandCapture(0, "busybox rm -rf " + target);
            Shell.startRootShell().add(command).waitForFinish();
            if (command.exitCode() != 0) {
                RootTools.log("target not exist or unable to delete file");
                result = false;
            }
        }
        if (!remountAsRw) {
            return result;
        }
        RootTools.remount(target, "RO");
        return result;
    }

    public boolean exists(String file) {
        final List<String> result = new ArrayList();
        Command command = new Command(0, new String[]{"ls " + file}) {
            public void output(int arg0, String arg1) {
                RootTools.log(arg1);
                result.add(arg1);
            }
        };
        try {
            if (Shell.isAnyShellOpen()) {
                Shell.getOpenShell().add(command).waitForFinish();
            } else {
                Shell.startShell().add(command).waitForFinish();
            }
            for (String line : result) {
                if (line.trim().equals(file)) {
                    return true;
                }
            }
            try {
                RootTools.closeShell(false);
            } catch (Exception e) {
            }
            result.clear();
            try {
                Shell.startRootShell().add(command).waitForFinish();
                List<String> final_result = new ArrayList();
                final_result.addAll(result);
                for (String line2 : final_result) {
                    if (line2.trim().equals(file)) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e2) {
                return false;
            }
        } catch (Exception e3) {
            return false;
        }
    }

    public void fixUtil(String util, String utilPath) {
        try {
            RootTools.remount("/system", "rw");
            if (RootTools.findBinary(util)) {
                List<String> paths = new ArrayList();
                paths.addAll(RootTools.lastFoundBinaryPaths);
                for (String path : paths) {
                    Shell.startRootShell().add(new CommandCapture(0, utilPath + " rm " + path + "/" + util)).waitForFinish();
                }
                Shell.startRootShell().add(new CommandCapture(0, utilPath + " ln -s " + utilPath + " /system/bin/" + util, utilPath + " chmod 0755 /system/bin/" + util)).waitForFinish();
            }
            RootTools.remount("/system", "ro");
        } catch (Exception e) {
        }
    }

    public boolean fixUtils(String[] utils) throws Exception {
        for (String util : utils) {
            if (!checkUtil(util)) {
                if (checkUtil("busybox")) {
                    if (hasUtil(util, "busybox")) {
                        fixUtil(util, RootTools.utilPath);
                    }
                } else if (!checkUtil("toolbox")) {
                    return false;
                } else {
                    if (hasUtil(util, "toolbox")) {
                        fixUtil(util, RootTools.utilPath);
                    }
                }
            }
        }
        return true;
    }

    public boolean findBinary(String binaryName) {
        boolean found = false;
        RootTools.lastFoundBinaryPaths.clear();
        List<String> list = new ArrayList();
        RootTools.log("Checking for " + binaryName);
        try {
            Set<String> paths = RootTools.getPath();
            if (paths.size() > 0) {
                for (String path : paths) {
                    if (RootTools.exists(path + "/" + binaryName)) {
                        RootTools.log(binaryName + " was found here: " + path);
                        list.add(path);
                        found = true;
                    } else {
                        RootTools.log(binaryName + " was NOT found here: " + path);
                    }
                }
            }
        } catch (TimeoutException e) {
            RootTools.log("TimeoutException!!!");
        } catch (Exception e2) {
            RootTools.log(binaryName + " was not found, more information MAY be available with Debugging on.");
        }
        if (!found) {
            RootTools.log("Trying second method");
            RootTools.log("Checking for " + binaryName);
            for (String where : new String[]{"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"}) {
                if (RootTools.exists(where + binaryName)) {
                    RootTools.log(binaryName + " was found here: " + where);
                    list.add(where);
                    found = true;
                } else {
                    RootTools.log(binaryName + " was NOT found here: " + where);
                }
            }
        }
        if (RootTools.debugMode) {
            for (String path2 : list) {
                RootTools.log("Paths: " + path2);
            }
        }
        Collections.reverse(list);
        RootTools.lastFoundBinaryPaths.addAll(list);
        return found;
    }

    public List<String> getBusyBoxApplets(String path) throws Exception {
        if (path != null && !path.endsWith("/") && !path.equals("")) {
            path = path + "/";
        } else if (path == null) {
            throw new Exception("Path is null, please specifiy a path");
        }
        final List<String> results = new ArrayList();
        Command command = new Command(3, new String[]{path + "busybox --list"}) {
            public void output(int id, String line) {
                if (id == 3 && !line.trim().equals("") && !line.trim().contains("not found")) {
                    results.add(line);
                }
            }
        };
        Shell.startRootShell().add(command);
        command.waitForFinish();
        return results;
    }

    public String getBusyBoxVersion(String path) {
        if (!(path.equals("") || path.endsWith("/"))) {
            path = path + "/";
        }
        RootTools.log("Getting BusyBox Version");
        InternalVariables.busyboxVersion = "";
        try {
            Command command = new Command(4, path + "busybox") {
                public void output(int id, String line) {
                    if (id == 4 && line.startsWith("BusyBox") && InternalVariables.busyboxVersion.equals("")) {
                        InternalVariables.busyboxVersion = line.split(" ")[1];
                    }
                }
            };
            Shell.startRootShell().add(command);
            command.waitForFinish();
            return InternalVariables.busyboxVersion;
        } catch (Exception e) {
            RootTools.log("BusyBox was not found, more information MAY be available with Debugging on.");
            return "";
        }
    }

    public long getConvertedSpace(String spaceStr) {
        double multiplier = 1.0d;
        try {
            StringBuffer sb = new StringBuffer();
            int i = 0;
            while (i < spaceStr.length()) {
                char c = spaceStr.charAt(i);
                if (Character.isDigit(c) || c == '.') {
                    sb.append(spaceStr.charAt(i));
                    i++;
                } else if (c == 'm' || c == 'M') {
                    multiplier = 1024.0d;
                    return (long) Math.ceil(Double.valueOf(sb.toString()).doubleValue() * multiplier);
                } else {
                    if (c == 'g' || c == 'G') {
                        multiplier = 1048576.0d;
                    }
                    return (long) Math.ceil(Double.valueOf(sb.toString()).doubleValue() * multiplier);
                }
            }
            return (long) Math.ceil(Double.valueOf(sb.toString()).doubleValue() * multiplier);
        } catch (Exception e) {
            return -1;
        }
    }

    public String getInode(String file) {
        try {
            Command command = new Command(5, "/data/local/ls -i " + file) {
                public void output(int id, String line) {
                    if (id == 5 && !line.trim().equals("") && Character.isDigit(line.trim().substring(0, 1).toCharArray()[0])) {
                        InternalVariables.inode = line.trim().split(" ")[0].toString();
                    }
                }
            };
            Shell.startRootShell().add(command);
            command.waitForFinish();
            return InternalVariables.inode;
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isAccessGiven() {
        try {
            RootTools.log("Checking for Root access");
            InternalVariables.accessGiven = false;
            Command command = new Command(2, "id") {
                public void output(int id, String line) {
                    if (id == 2) {
                        for (String userid : new HashSet(Arrays.asList(line.split(" ")))) {
                            RootTools.log(userid);
                            if (userid.toLowerCase().contains("uid=0")) {
                                InternalVariables.accessGiven = true;
                                RootTools.log("Access Given");
                                break;
                            }
                        }
                        if (!InternalVariables.accessGiven) {
                            RootTools.log("Access Denied?");
                        }
                    }
                }
            };
            Shell.startRootShell().add(command);
            command.waitForFinish();
            if (InternalVariables.accessGiven) {
                return true;
            }
            RootTools.shellDelay = 0;
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            RootTools.shellDelay = 0;
        }
    }

    public boolean isNativeToolsReady(int nativeToolsId, Context context) {
        RootTools.log("Preparing Native Tools");
        InternalVariables.nativeToolsReady = false;
        try {
            Installer installer = new Installer(context);
            if (installer.isBinaryInstalled("nativetools")) {
                InternalVariables.nativeToolsReady = true;
            } else {
                InternalVariables.nativeToolsReady = installer.installBinary(nativeToolsId, "nativetools", "700");
            }
            return InternalVariables.nativeToolsReady;
        } catch (IOException ex) {
            if (!RootTools.debugMode) {
                return false;
            }
            ex.printStackTrace();
            return false;
        }
    }

    public Permissions getFilePermissionsSymlinks(String file) {
        Permissions permissions = null;
        RootTools.log("Checking permissions for " + file);
        if (!RootTools.exists(file)) {
            return permissions;
        }
        RootTools.log(file + " was found.");
        try {
            Command command = new Command(1, "ls -l " + file, "busybox ls -l " + file, "/system/bin/failsafe/toolbox ls -l " + file, "toolbox ls -l " + file) {
                public void output(int id, String line) {
                    if (id == 1) {
                        String symlink_final = "";
                        if (line.split(" ")[0].length() == 10) {
                            RootTools.log("Line " + line);
                            try {
                                String[] symlink = line.split(" ");
                                if (symlink[symlink.length - 2].equals("->")) {
                                    RootTools.log("Symlink found.");
                                    symlink_final = symlink[symlink.length - 1];
                                }
                            } catch (Exception e) {
                            }
                            try {
                                InternalVariables.permissions = RootToolsInternalMethods.this.getPermissions(line);
                                if (InternalVariables.permissions != null) {
                                    InternalVariables.permissions.setSymlink(symlink_final);
                                }
                            } catch (Exception e2) {
                                RootTools.log(e2.getMessage());
                            }
                        }
                    }
                }
            };
            Shell.startRootShell().add(command);
            command.waitForFinish();
            return InternalVariables.permissions;
        } catch (Exception e) {
            RootTools.log(e.getMessage());
            return permissions;
        }
    }

    public ArrayList<Mount> getMounts() throws Exception {
        LineNumberReader lnr = new LineNumberReader(new FileReader("/proc/mounts"));
        ArrayList<Mount> mounts = new ArrayList();
        while (true) {
            String line = lnr.readLine();
            if (line == null) {
                break;
            }
            RootTools.log(line);
            String[] fields = line.split(" ");
            mounts.add(new Mount(new File(fields[0]), new File(fields[1]), fields[2], fields[3]));
        }
        InternalVariables.mounts = mounts;
        if (InternalVariables.mounts != null) {
            return InternalVariables.mounts;
        }
        throw new Exception();
    }

    public String getMountedAs(String path) throws Exception {
        InternalVariables.mounts = getMounts();
        if (InternalVariables.mounts != null) {
            Iterator i$ = InternalVariables.mounts.iterator();
            while (i$.hasNext()) {
                Mount mount = (Mount) i$.next();
                if (path.contains(mount.getMountPoint().getAbsolutePath())) {
                    RootTools.log((String) mount.getFlags().toArray()[0]);
                    return (String) mount.getFlags().toArray()[0];
                }
            }
            throw new Exception();
        }
        throw new Exception();
    }

    public Set<String> getPath() throws Exception {
        if (InternalVariables.path != null) {
            return InternalVariables.path;
        }
        if (returnPath()) {
            return InternalVariables.path;
        }
        throw new Exception();
    }

    public long getSpace(String path) {
        InternalVariables.getSpaceFor = path;
        boolean found = false;
        RootTools.log("Looking for Space");
        try {
            Command command = new Command(6, "df " + path) {
                public void output(int id, String line) {
                    if (id == 6 && line.contains(InternalVariables.getSpaceFor.trim())) {
                        InternalVariables.space = line.split(" ");
                    }
                }
            };
            Shell.startRootShell().add(command);
            command.waitForFinish();
        } catch (Exception e) {
        }
        if (InternalVariables.space != null) {
            RootTools.log("First Method");
            for (String spaceSearch : InternalVariables.space) {
                RootTools.log(spaceSearch);
                if (found) {
                    return getConvertedSpace(spaceSearch);
                }
                if (spaceSearch.equals("used,")) {
                    found = true;
                }
            }
            int count = 0;
            int targetCount = 3;
            RootTools.log("Second Method");
            if (InternalVariables.space[0].length() <= 5) {
                targetCount = 2;
            }
            for (String spaceSearch2 : InternalVariables.space) {
                RootTools.log(spaceSearch2);
                if (spaceSearch2.length() > 0) {
                    RootTools.log(spaceSearch2 + "Valid");
                    if (count == targetCount) {
                        return getConvertedSpace(spaceSearch2);
                    }
                    count++;
                }
            }
        }
        RootTools.log("Returning -1, space could not be determined.");
        return -1;
    }

    public String getSymlink(String file) {
        RootTools.log("Looking for Symlink for " + file);
        try {
            final List<String> results = new ArrayList();
            Command command = new Command(7, new String[]{"ls -l " + file}) {
                public void output(int id, String line) {
                    if (id == 7 && !line.trim().equals("")) {
                        results.add(line);
                    }
                }
            };
            Shell.startRootShell().add(command);
            command.waitForFinish();
            String[] symlink = ((String) results.get(0)).split(" ");
            if (symlink[symlink.length - 2].equals("->")) {
                RootTools.log("Symlink found.");
                String final_symlink = "";
                if (symlink[symlink.length - 1].equals("") || symlink[symlink.length - 1].contains("/")) {
                    return symlink[symlink.length - 1];
                }
                findBinary(symlink[symlink.length - 1]);
                if (RootTools.lastFoundBinaryPaths.size() > 0) {
                    return ((String) RootTools.lastFoundBinaryPaths.get(0)) + "/" + symlink[symlink.length - 1];
                }
                return symlink[symlink.length - 1];
            }
        } catch (Exception e) {
            if (RootTools.debugMode) {
                e.printStackTrace();
            }
        }
        RootTools.log("Symlink not found");
        return "";
    }

    public ArrayList<Symlink> getSymlinks(String path) throws Exception {
        if (checkUtil("find")) {
            CommandCapture command = new CommandCapture(0, "find " + path + " -type l -exec ls -l {} \\; > /data/local/symlinks.txt;");
            Shell.startRootShell().add(command);
            command.waitForFinish();
            InternalVariables.symlinks = getSymLinks();
            if (InternalVariables.symlinks != null) {
                return InternalVariables.symlinks;
            }
            throw new Exception();
        }
        throw new Exception();
    }

    public String getWorkingToolbox() {
        if (RootTools.checkUtil("busybox")) {
            return "busybox";
        }
        if (RootTools.checkUtil("toolbox")) {
            return "toolbox";
        }
        return "";
    }

    public boolean hasEnoughSpaceOnSdCard(long updateSize) {
        RootTools.log("Checking SDcard size and that it is mounted as RW");
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return false;
        }
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        if (updateSize < ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize())) {
            return true;
        }
        return false;
    }

    public boolean hasUtil(String util, String box) {
        InternalVariables.found = false;
        if (!box.endsWith("toolbox") && !box.endsWith("busybox")) {
            return false;
        }
        try {
            String str;
            String[] strArr = new String[1];
            if (box.endsWith("toolbox")) {
                str = box + " " + util;
            } else {
                str = box + " --list";
            }
            strArr[0] = str;
            final String str2 = box;
            final String str3 = util;
            RootTools.getShell(true).add(new Command(0, strArr) {
                public void output(int id, String line) {
                    if (str2.endsWith("toolbox")) {
                        if (line.contains("no such tool")) {
                            InternalVariables.found = true;
                        }
                    } else if (str2.endsWith("busybox") && line.contains(str3)) {
                        RootTools.log("Found util!");
                        InternalVariables.found = true;
                    }
                }
            }).waitForFinish(DeviceOperationRESTServiceProvider.TIMEOUT);
            if (InternalVariables.found) {
                RootTools.log("Box contains " + util + " util!");
                return true;
            }
            RootTools.log("Box does not contain " + util + " util!");
            return false;
        } catch (Exception e) {
            RootTools.log(e.getMessage());
            return false;
        }
    }

    public boolean installBinary(Context context, int sourceId, String destName, String mode) {
        try {
            return new Installer(context).installBinary(sourceId, destName, mode);
        } catch (IOException ex) {
            if (RootTools.debugMode) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public boolean isAppletAvailable(String applet, String binaryPath) {
        try {
            for (String aplet : getBusyBoxApplets(binaryPath)) {
                if (aplet.equals(applet)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            RootTools.log(e.toString());
            return false;
        }
    }

    public boolean isProcessRunning(final String processName) {
        RootTools.log("Checks if process is running: " + processName);
        try {
            Result result = new Result() {
                public void process(String line) throws Exception {
                    if (line.contains(processName)) {
                        setData(Integer.valueOf(1));
                    }
                }

                public void onFailure(Exception ex) {
                    setError(1);
                }

                public void onComplete(int diag) {
                }

                public void processError(String arg0) throws Exception {
                }
            };
            RootTools.sendShell(new String[]{"ps"}, 1, result, -1);
            if (result.getError() != 0 || result.getData() == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            RootTools.log(e.getMessage());
            return false;
        }
    }

    public boolean killProcess(final String processName) {
        RootTools.log("Killing process " + processName);
        try {
            Result result = new Result() {
                public void process(String line) throws Exception {
                    if (line.contains(processName)) {
                        Matcher psMatcher = InternalVariables.psPattern.matcher(line);
                        try {
                            if (psMatcher.find()) {
                                String pid = psMatcher.group(1);
                                if (getData() != null) {
                                    setData(getData() + " " + pid);
                                } else {
                                    setData((Serializable) pid);
                                }
                                RootTools.log("Found pid: " + pid);
                                return;
                            }
                            RootTools.log("Matching in ps command failed!");
                        } catch (Exception e) {
                            RootTools.log("Error with regex!");
                            e.printStackTrace();
                        }
                    }
                }

                public void onFailure(Exception ex) {
                    setError(1);
                }

                public void onComplete(int diag) {
                }

                public void processError(String arg0) throws Exception {
                }
            };
            RootTools.sendShell(new String[]{"ps"}, 1, result, -1);
            if (result.getError() != 0 || ((String) result.getData()) == null) {
                return false;
            }
            try {
                RootTools.sendShell(new String[]{"kill -9 " + ((String) result.getData())}, 1, -1);
                return true;
            } catch (Exception e) {
                RootTools.log(e.getMessage());
                return false;
            }
        } catch (Exception e2) {
            RootTools.log(e2.getMessage());
            return false;
        }
    }

    public void offerBusyBox(Activity activity) {
        RootTools.log("Launching Market for BusyBox");
        activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=stericson.busybox")));
    }

    public Intent offerBusyBox(Activity activity, int requestCode) {
        RootTools.log("Launching Market for BusyBox");
        Intent i = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=stericson.busybox"));
        activity.startActivityForResult(i, requestCode);
        return i;
    }

    public void offerSuperUser(Activity activity) {
        RootTools.log("Launching Market for SuperUser");
        activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.noshufou.android.su")));
    }

    public Intent offerSuperUser(Activity activity, int requestCode) {
        RootTools.log("Launching Market for SuperUser");
        Intent i = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=com.noshufou.android.su"));
        activity.startActivityForResult(i, requestCode);
        return i;
    }
}
