package com.stericson.RootTools.containers;

import java.io.File;

public class Symlink {
    protected final File file;
    protected final File symlinkPath;

    public Symlink(File file, File path) {
        this.file = file;
        this.symlinkPath = path;
    }

    public File getFile() {
        return this.file;
    }

    public File getSymlinkPath() {
        return this.symlinkPath;
    }
}
