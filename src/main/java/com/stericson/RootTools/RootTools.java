package com.stericson.RootTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.stericson.RootTools.containers.Mount;
import com.stericson.RootTools.containers.Permissions;
import com.stericson.RootTools.containers.Symlink;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.exceptions.RootToolsException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Executer;
import com.stericson.RootTools.execution.IResult;
import com.stericson.RootTools.execution.Shell;
import com.stericson.RootTools.internal.Remounter;
import com.stericson.RootTools.internal.RootToolsInternalMethods;
import com.stericson.RootTools.internal.Runner;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public final class RootTools {
    public static String customShell = "";
    public static boolean debugMode = false;
    public static int lastExitCode;
    public static List<String> lastFoundBinaryPaths = new ArrayList();
    private static RootToolsInternalMethods rim = null;
    public static int shellDelay = 0;
    public static boolean useRoot = true;
    public static String utilPath;

    public static abstract class Result implements IResult {
        private Serializable data = null;
        private int error = 0;
        private Process process = null;

        public abstract void onComplete(int i);

        public abstract void onFailure(Exception exception);

        public abstract void process(String str) throws Exception;

        public abstract void processError(String str) throws Exception;

        public Result setProcess(Process process) {
            this.process = process;
            return this;
        }

        public Process getProcess() {
            return this.process;
        }

        public Result setData(Serializable data) {
            this.data = data;
            return this;
        }

        public Serializable getData() {
            return this.data;
        }

        public Result setError(int error) {
            this.error = error;
            return this;
        }

        public int getError() {
            return this.error;
        }
    }

    public static void setRim(RootToolsInternalMethods rim) {
        rim = rim;
    }

    private static final RootToolsInternalMethods getInternals() {
        if (rim != null) {
            return rim;
        }
        RootToolsInternalMethods.getInstance();
        return rim;
    }

    public static boolean checkUtil(String util) {
        return getInternals().checkUtil(util);
    }

    public static void closeAllShells() throws IOException {
        Shell.closeAll();
    }

    public static void closeCustomShell() throws IOException {
        Shell.closeCustomShell();
    }

    public static void closeShell(boolean root) throws IOException {
        if (root) {
            Shell.closeRootShell();
        } else {
            Shell.closeShell();
        }
    }

    public static boolean copyFile(String source, String destination, boolean remountAsRw, boolean preserveFileAttributes) {
        return getInternals().copyFile(source, destination, remountAsRw, preserveFileAttributes);
    }

    public boolean deleteFileOrDirectory(String target, boolean remountAsRw) {
        return getInternals().deleteFileOrDirectory(target, remountAsRw);
    }

    public static boolean exists(String file) {
        return getInternals().exists(file);
    }

    public static void fixUtil(String util, String utilPath) {
        getInternals().fixUtil(util, utilPath);
    }

    public static boolean fixUtils(String[] utils) throws Exception {
        return getInternals().fixUtils(utils);
    }

    public static boolean findBinary(String binaryName) {
        return getInternals().findBinary(binaryName);
    }

    public static String getBusyBoxVersion(String path) {
        return getInternals().getBusyBoxVersion(path);
    }

    public static String getBusyBoxVersion() {
        return getBusyBoxVersion("");
    }

    public static List<String> getBusyBoxApplets() throws Exception {
        return getBusyBoxApplets("");
    }

    public static List<String> getBusyBoxApplets(String path) throws Exception {
        return getInternals().getBusyBoxApplets(path);
    }

    public static Shell getCustomShell(String shellPath, int timeout) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startCustomShell(shellPath, timeout);
    }

    public static Shell getCustomShell(String shellPath) throws IOException, TimeoutException, RootDeniedException {
        return getCustomShell(shellPath, 10000);
    }

    public static Permissions getFilePermissionsSymlinks(String file) {
        return getInternals().getFilePermissionsSymlinks(file);
    }

    public static String getInode(String file) {
        return getInternals().getInode(file);
    }

    public static ArrayList<Mount> getMounts() throws Exception {
        return getInternals().getMounts();
    }

    public static String getMountedAs(String path) throws Exception {
        return getInternals().getMountedAs(path);
    }

    public static Set<String> getPath() throws Exception {
        return getInternals().getPath();
    }

    public static Shell getShell(boolean root, int timeout, int retry) throws IOException, TimeoutException, RootDeniedException {
        if (root) {
            return Shell.startRootShell(timeout);
        }
        return Shell.startShell(timeout);
    }

    public static Shell getShell(boolean root, int timeout) throws IOException, TimeoutException, RootDeniedException {
        return getShell(root, timeout, 3);
    }

    public static Shell getShell(boolean root) throws IOException, TimeoutException, RootDeniedException {
        return getShell(root, 10000);
    }

    public static long getSpace(String path) {
        return getInternals().getSpace(path);
    }

    public static String getSymlink(String file) {
        return getInternals().getSymlink(file);
    }

    public static ArrayList<Symlink> getSymlinks(String path) throws Exception {
        return getInternals().getSymlinks(path);
    }

    public static String getWorkingToolbox() {
        return getInternals().getWorkingToolbox();
    }

    public static boolean hasEnoughSpaceOnSdCard(long updateSize) {
        return getInternals().hasEnoughSpaceOnSdCard(updateSize);
    }

    public static boolean hasUtil(String util, String box) {
        return getInternals().hasUtil(util, box);
    }

    public static boolean installBinary(Context context, int sourceId, String destName, String mode) {
        return getInternals().installBinary(context, sourceId, destName, mode);
    }

    public static boolean installBinary(Context context, int sourceId, String binaryName) {
        return installBinary(context, sourceId, binaryName, "700");
    }

    public static boolean isAppletAvailable(String applet, String path) {
        return getInternals().isAppletAvailable(applet, path);
    }

    public static boolean isAppletAvailable(String applet) {
        return isAppletAvailable(applet, "");
    }

    public static boolean isAccessGiven() {
        return getInternals().isAccessGiven();
    }

    public static boolean isBusyboxAvailable() {
        return findBinary("busybox");
    }

    public static boolean isNativeToolsReady(int nativeToolsId, Context context) {
        return getInternals().isNativeToolsReady(nativeToolsId, context);
    }

    public static boolean isProcessRunning(String processName) {
        return getInternals().isProcessRunning(processName);
    }

    public static boolean isRootAvailable() {
        return findBinary("su");
    }

    public static boolean killProcess(String processName) {
        return getInternals().killProcess(processName);
    }

    public static void offerBusyBox(Activity activity) {
        getInternals().offerBusyBox(activity);
    }

    public static Intent offerBusyBox(Activity activity, int requestCode) {
        return getInternals().offerBusyBox(activity, requestCode);
    }

    public static void offerSuperUser(Activity activity) {
        getInternals().offerSuperUser(activity);
    }

    public static Intent offerSuperUser(Activity activity, int requestCode) {
        return getInternals().offerSuperUser(activity, requestCode);
    }

    public static boolean remount(String file, String mountType) {
        return new Remounter().remount(file, mountType);
    }

    public static void restartAndroid() {
        log("Restart Android");
        killProcess("zygote");
    }

    public static void runBinary(Context context, String binaryName, String parameter) {
        new Runner(context, binaryName, parameter).start();
    }

    public static void runShellCommand(Shell shell, Command command) throws IOException {
        shell.add(command);
    }

    public static List<String> sendShell(String[] commands, int sleepTime, Result result, int timeout) throws IOException, RootToolsException, TimeoutException {
        return sendShell(commands, sleepTime, result, useRoot, timeout);
    }

    public static List<String> sendShell(String[] commands, int sleepTime, Result result, boolean useRoot, int timeout) throws IOException, RootToolsException, TimeoutException {
        return new Executer().sendShell(commands, sleepTime, result, useRoot, timeout);
    }

    public static List<String> sendShell(String[] commands, int sleepTime, int timeout) throws IOException, RootToolsException, TimeoutException {
        return sendShell(commands, sleepTime, null, timeout);
    }

    public static List<String> sendShell(String command, Result result, int timeout) throws IOException, RootToolsException, TimeoutException {
        return sendShell(new String[]{command}, 0, result, timeout);
    }

    public static List<String> sendShell(String command, int timeout) throws IOException, RootToolsException, TimeoutException {
        return sendShell(command, null, timeout);
    }

    public static void log(String msg) {
        log(null, msg, 3, null);
    }

    public static void log(String TAG, String msg) {
        log(TAG, msg, 3, null);
    }

    public static void log(String msg, int type, Exception e) {
        log(null, msg, type, e);
    }

    public static void log(String TAG, String msg, int type, Exception e) {
        if (msg != null && !msg.equals("") && debugMode) {
            if (TAG == null) {
                TAG = Constants.TAG;
            }
            switch (type) {
                case 1:
                    Log.v(TAG, msg);
                    return;
                case 2:
                    Log.e(TAG, msg, e);
                    return;
                case 3:
                    Log.d(TAG, msg);
                    return;
                default:
                    return;
            }
        }
    }
}
