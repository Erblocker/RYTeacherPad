package com.foxit.view.toolbar.imp;

import android.content.Context;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.uiextensions.controls.toolbar.impl.BaseBarImpl;
import com.foxit.uiextensions.utils.AppResource;

public class TB_EditDoneBar extends BaseBarImpl {
    public TB_EditDoneBar(Context context) {
        super(context);
        initDimens();
    }

    private void initDimens() {
        try {
            if (App.instance().getDisplay().isPad()) {
                this.mDefaultSpace = AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_toolbar_button_interval_small_pad);
            } else {
                this.mDefaultSpace = AppResource.getDimensionPixelSize(this.mContext, R.dimen.ux_toolbar_button_interval_small_phone);
            }
        } catch (Exception e) {
            this.mDefaultSpace = dip2px(4);
        }
    }
}
