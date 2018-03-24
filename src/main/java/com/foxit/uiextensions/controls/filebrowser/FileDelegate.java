package com.foxit.uiextensions.controls.filebrowser;

import android.view.View;
import com.foxit.uiextensions.controls.filebrowser.imp.FileItem;
import java.util.List;

public interface FileDelegate<T extends FileItem> {
    List<T> getDataSource();

    void onItemClicked(View view, FileItem fileItem);

    void onItemsCheckedChanged(boolean z, int i, int i2);

    void onPathChanged(String str);
}
