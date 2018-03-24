package com.foxit.uiextensions.modules.signature;

import android.content.Context;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.toolbar.impl.TopBarImpl;

public class SignatureSignListBar extends TopBarImpl {
    public SignatureSignListBar(Context context) {
        super(context);
        this.mRightSideInterval = (int) context.getResources().getDimension(R.dimen.ux_horz_right_margin_pad);
    }
}
