package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;
import com.foxit.uiextensions.utils.AppDisplay;

public class SignatureCreateSignTitleBar extends TopBarImpl {
    public SignatureCreateSignTitleBar(Context context) {
        super(context);
        if (AppDisplay.getInstance(context).isPad()) {
            this.mRightSideInterval = this.mLeftSideInterval;
        } else {
            this.mLeftSideInterval = this.mRightSideInterval;
        }
    }
}
