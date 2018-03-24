package com.stericson.RootTools.containers;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class Mount {
    final File mDevice;
    final Set<String> mFlags;
    final File mMountPoint;
    final String mType;

    public Mount(File device, File path, String type, String flagsStr) {
        this.mDevice = device;
        this.mMountPoint = path;
        this.mType = type;
        this.mFlags = new LinkedHashSet(Arrays.asList(flagsStr.split(",")));
    }

    public File getDevice() {
        return this.mDevice;
    }

    public File getMountPoint() {
        return this.mMountPoint;
    }

    public String getType() {
        return this.mType;
    }

    public Set<String> getFlags() {
        return this.mFlags;
    }

    public String toString() {
        return String.format("%s on %s type %s %s", new Object[]{this.mDevice, this.mMountPoint, this.mType, this.mFlags});
    }
}
