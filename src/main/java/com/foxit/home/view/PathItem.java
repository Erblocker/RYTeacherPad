package com.foxit.home.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.widget.TextView;
import com.foxit.app.App;
import com.foxit.home.R;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;

/* compiled from: HM_PathView */
class PathItem extends BaseItemImpl {
    public PathItem(Context context, String text, int imgRes) {
        super(context, text, imgRes, 12);
        setTextSize(App.instance().getDisplay().px2dp(App.instance().getApplicationContext().getResources().getDimension(R.dimen.ux_text_height_menu)));
        setInterval(6);
        this.mTextView.setSingleLine(true);
        this.mTextView.setEllipsize(TruncateAt.MIDDLE);
    }

    public TextView getTextView() {
        return this.mTextView;
    }
}
