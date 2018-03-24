package com.foxit.view.toolbar.imp;

import android.content.Context;
import com.foxit.app.App;
import com.foxit.uiextensions.controls.toolbar.impl.BaseBarImpl;

public class TB_AnnotsBar extends BaseBarImpl {
    public TB_AnnotsBar(Context context) {
        super(context);
        this.mDefaultSpace = dip2px(4);
        if (App.instance().getDisplay().isPad()) {
            this.mDefaultSpace = dip2px(6);
        }
    }
}
