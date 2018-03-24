package com.foxit.uiextensions.controls.panel.impl;

import android.view.View;
import com.foxit.uiextensions.controls.panel.PanelSpec;

public class PanelSpecImpl implements PanelSpec {
    private View mContentView;
    private int mIcon;
    private int mTag;
    private View mTopToolbar;

    public PanelSpecImpl(int icon, View topToolbar, View ContentView, int tag) {
        this.mIcon = icon;
        this.mTopToolbar = topToolbar;
        this.mContentView = ContentView;
        this.mTag = tag;
    }

    public int getTag() {
        return this.mTag;
    }

    public int getIcon() {
        return this.mIcon;
    }

    public View getTopToolbar() {
        return this.mTopToolbar;
    }

    public View getContentView() {
        return this.mContentView;
    }

    public void onActivated() {
    }

    public void onDeactivated() {
    }
}
