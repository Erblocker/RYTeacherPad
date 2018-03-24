package com.foxit.uiextensions.controls.filebrowser;

import android.view.View;
import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import java.util.List;

public interface FileBrowser {
    void clearCheckedItems();

    List<FileItem> getCheckedItems();

    FileComparator getComparator();

    View getContentView();

    String getDisplayPath();

    void setEditState(boolean z);

    void setPath(String str);

    void updateDataSource(boolean z);
}
