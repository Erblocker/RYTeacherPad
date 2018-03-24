package com.netspace.library.ui;

import android.app.Activity;
import com.netspace.pad.library.R;

public class BusyUIDisplayer extends UIDisplayer {
    public BusyUIDisplayer(Activity Activity) {
        super(Activity);
        this.mOverlappedLayoutID = R.layout.layout_progress;
    }
}
