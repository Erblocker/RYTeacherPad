package com.foxit.home.view;

import android.view.View;
import com.foxit.home.view.HM_PathView.pathChangedListener;

/* compiled from: HM_PathView */
interface IPathCtl {
    View getContentView();

    String getCurPath();

    void setPath(String str);

    void setPathChangedListener(pathChangedListener pathchangedlistener);
}
