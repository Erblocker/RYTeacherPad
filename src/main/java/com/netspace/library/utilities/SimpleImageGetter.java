package com.netspace.library.utilities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

public class SimpleImageGetter implements ImageGetter {
    private Context mContext;

    public SimpleImageGetter(Context Context) {
        this.mContext = Context;
    }

    public Drawable getDrawable(String source) {
        Drawable d = this.mContext.getResources().getDrawable(this.mContext.getResources().getIdentifier(source, "drawable", this.mContext.getPackageName()));
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        return d;
    }
}
