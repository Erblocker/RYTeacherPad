package com.foxit.uiextensions.controls.panel;

import android.view.View;

public interface PanelSpec {
    public static final int PANELSPEC_TAG_ANNOTATIONS = 2;
    public static final int PANELSPEC_TAG_BOOKMARKS = 0;
    public static final int PANELSPEC_TAG_FILEATTACHMENTS = 3;
    public static final int PANELSPEC_TAG_OUTLINE = 1;

    View getContentView();

    int getIcon();

    int getTag();

    View getTopToolbar();

    void onActivated();

    void onDeactivated();
}
