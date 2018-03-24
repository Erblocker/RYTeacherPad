package com.foxit.view.toolbar.imp;

import android.content.Context;
import android.view.View;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.uiextensions.controls.toolbar.impl.BaseBarImpl;

public class TB_AnnotBar extends BaseBarImpl {
    private int mSidesInterval = 16;

    public TB_AnnotBar(Context context) {
        super(context, 1);
        initDimens();
    }

    public View getContentView() {
        if (!this.mInterval) {
            if (this.mOrientation == 0) {
                this.mRootLayout.setPadding(dip2px_fromDimens(this.mSidesInterval), 0, dip2px_fromDimens(this.mSidesInterval), 0);
            } else {
                this.mRootLayout.setPadding(0, dip2px_fromDimens(this.mSidesInterval), 0, dip2px(5));
            }
        }
        return this.mRootLayout;
    }

    private void initDimens() {
        try {
            this.mSidesInterval = (int) App.instance().getApplicationContext().getResources().getDimension(R.dimen.ux_text_icon_distance_phone);
        } catch (Exception e) {
            this.mSidesInterval = dip2px(this.mSidesInterval);
        }
    }
}
