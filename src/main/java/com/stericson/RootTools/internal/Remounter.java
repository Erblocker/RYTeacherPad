package com.stericson.RootTools.internal;

import android.util.Log;
import com.stericson.RootTools.Constants;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.containers.Mount;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Remounter {
    public boolean remount(String file, String mountType) {
        if (file.endsWith("/") && !file.equals("/")) {
            file = file.substring(0, file.lastIndexOf("/"));
        }
        boolean foundMount = false;
        while (!foundMount) {
            try {
                Iterator i$ = RootTools.getMounts().iterator();
                while (i$.hasNext()) {
                    Mount mount = (Mount) i$.next();
                    RootTools.log(mount.getMountPoint().toString());
                    if (file.equals(mount.getMountPoint().toString())) {
                        foundMount = true;
                        break;
                    }
                }
                if (!foundMount) {
                    try {
                        file = new File(file).getParent().toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } catch (Exception e2) {
                if (RootTools.debugMode) {
                    e2.printStackTrace();
                }
                return false;
            }
        }
        Mount mountPoint = findMountPointRecursive(file);
        RootTools.log(Constants.TAG, "Remounting " + mountPoint.getMountPoint().getAbsolutePath() + " as " + mountType.toLowerCase());
        if (!mountPoint.getFlags().contains(mountType.toLowerCase())) {
            try {
                CommandCapture command = new CommandCapture(0, "busybox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "toolbox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath(), "/system/bin/toolbox mount -o remount," + mountType.toLowerCase() + " " + mountPoint.getDevice().getAbsolutePath() + " " + mountPoint.getMountPoint().getAbsolutePath());
                Shell.startRootShell().add(command);
                command.waitForFinish();
            } catch (Exception e3) {
            }
            mountPoint = findMountPointRecursive(file);
        }
        Log.i(Constants.TAG, mountPoint.getFlags() + " AND " + mountType.toLowerCase());
        if (mountPoint.getFlags().contains(mountType.toLowerCase())) {
            RootTools.log(mountPoint.getFlags().toString());
            return true;
        }
        RootTools.log(mountPoint.getFlags().toString());
        return false;
    }

    private Mount findMountPointRecursive(String file) {
        try {
            ArrayList<Mount> mounts = RootTools.getMounts();
            File path = new File(file);
            while (path != null) {
                Iterator i$ = mounts.iterator();
                while (i$.hasNext()) {
                    Mount mount = (Mount) i$.next();
                    if (mount.getMountPoint().equals(path)) {
                        return mount;
                    }
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e2) {
            if (RootTools.debugMode) {
                e2.printStackTrace();
            }
            return null;
        }
    }
}
