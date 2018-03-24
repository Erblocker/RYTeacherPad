package com.netspace.library.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

public class FaceImageGetter implements ImageGetter {
    private Context mContext;

    public FaceImageGetter(Context context) {
        this.mContext = context;
    }

    public Drawable getDrawable(String source) {
        int nID = this.mContext.getResources().getIdentifier("smiley_" + source, "drawable", this.mContext.getPackageName());
        if (nID == 0) {
            return null;
        }
        Drawable d = this.mContext.getResources().getDrawable(nID);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        return d;
    }
}
