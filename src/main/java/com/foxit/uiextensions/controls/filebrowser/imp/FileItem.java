package com.foxit.uiextensions.controls.filebrowser.imp;

public class FileItem {
    public static final int TYPE_ALL_PDF_FILE = 257;
    public static final int TYPE_ALL_PDF_FOLDER = 256;
    public static final int TYPE_CLOUD_SELECT_FILE = 65537;
    public static final int TYPE_CLOUD_SELECT_FOLDER = 65552;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_FOLDER = 16;
    public static final int TYPE_ROOT = 0;
    public static final int TYPE_TARGET_FILE = 1048577;
    public static final int TYPE_TARGET_FOLDER = 1048592;
    public boolean checked;
    public long createTime;
    public String date;
    public int fileCount;
    public long lastModifyTime;
    public long length;
    public String name;
    public String parentPath;
    public String path;
    public String pattern;
    public String size;
    public int type;

    public FileItem(FileItem item) {
        this.type = item.type;
        this.path = item.path;
        this.parentPath = item.parentPath;
        this.name = item.name;
        this.date = item.date;
        this.size = item.size;
        this.createTime = item.createTime;
        this.lastModifyTime = item.lastModifyTime;
        this.length = item.length;
        this.checked = item.checked;
        this.pattern = item.pattern;
        this.fileCount = item.fileCount;
    }
}
