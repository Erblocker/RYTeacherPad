package com.foxit.uiextensions.controls.filebrowser;

import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import java.util.Comparator;

public class FileComparator implements Comparator<FileItem> {
    public static final int ORDER_NAME_DOWN = 1;
    public static final int ORDER_NAME_UP = 0;
    public static final int ORDER_SIZE_DOWN = 5;
    public static final int ORDER_SIZE_UP = 4;
    public static final int ORDER_TIME_DOWN = 3;
    public static final int ORDER_TIME_UP = 2;
    private int mNaturalOrder = 0;

    public void setOrderBy(int orderBy) {
        this.mNaturalOrder = orderBy;
    }

    public int compare(FileItem lhs, FileItem rhs) {
        switch (this.mNaturalOrder) {
            case 1:
                if (lhs.type == rhs.type) {
                    return lhs.name.compareToIgnoreCase(rhs.name) * -1;
                }
                return rhs.type - lhs.type;
            case 2:
                if (lhs.type != rhs.type) {
                    return rhs.type - lhs.type;
                }
                if (lhs.lastModifyTime - rhs.lastModifyTime > 0) {
                    return 1;
                }
                if (lhs.lastModifyTime - rhs.lastModifyTime < 0) {
                    return -1;
                }
                return 0;
            case 3:
                if (lhs.type != rhs.type) {
                    return rhs.type - lhs.type;
                }
                if (rhs.lastModifyTime - lhs.lastModifyTime > 0) {
                    return 1;
                }
                if (rhs.lastModifyTime - lhs.lastModifyTime < 0) {
                    return -1;
                }
                return 0;
            case 4:
                if (lhs.type == rhs.type) {
                    return (int) (lhs.length - rhs.length);
                }
                return rhs.type - lhs.type;
            case 5:
                if (lhs.type == rhs.type) {
                    return (int) (rhs.length - lhs.length);
                }
                return rhs.type - lhs.type;
            default:
                if (lhs.type == rhs.type) {
                    return lhs.name.compareToIgnoreCase(rhs.name);
                }
                return rhs.type - lhs.type;
        }
    }
}
